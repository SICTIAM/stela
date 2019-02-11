import updatesFile from '../updates.md'

const checkStatus = (response) => {
    if (response.status >= 200 && response.status < 300) {
        return response
    } else {
        throw response
    }
}

const capitalizeFirstLetter = (string) => {
    return string[0].toUpperCase() + string.slice(1)
}

const bytesToSize = (bytes) => {
    const sizes = ['octets', 'Ko', 'Mo', 'Go', 'To']
    if (bytes === 0) return 'n/a'
    const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)), 10)
    if (i === 0) return `${bytes} ${sizes[i]}`
    return `${(bytes / (1024 ** i)).toFixed(1)} ${sizes[i]}`
}

// TODO: add 403 controls
const fetchWithAuthzHandling = ({ url, method, body, query, context, headers }) => {
    const httpMethod = method || 'GET'
    const data = body || undefined
    const params = query || {}
    const queryParams = '?' + Object.keys(params)
        .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
        .join('&')
    const additionalHeaders = headers || {}
    let httpHeaders = {}
    if (httpMethod === 'POST' || httpMethod === 'PUT' || httpMethod === 'PATCH' || httpMethod === 'DELETE')
        httpHeaders = { [context.csrfTokenHeaderName]: context.csrfToken }
    httpHeaders = Object.assign({}, httpHeaders, additionalHeaders)

    return fetch(url + queryParams, {
        method: httpMethod,
        credentials: 'same-origin',
        headers: httpHeaders,
        body: data
    })
}

const updateField = (object, field, value) => {
    const treeField = field.split('.')
    const fieldName = treeField.shift()
    if (treeField.length !== 0) updateField(object[fieldName], treeField.join('.'), value)
    else {
        object[field] = value
    }
}

const updateChekboxField = (object, field) => {
    const treeField = field.split('.')
    const fieldName = treeField.shift()
    if (treeField.length !== 0) updateChekboxField(object[fieldName], treeField.join('.'))
    else {
        object[field] = !object[field]
    }
}

const handleFieldCheckboxChange = (that, field, callback) => {
    const fields = that.state.fields
    updateChekboxField(fields, field)
    that.setState({ fields: fields }, callback)
}

const handleFieldChange = (that, id, value, callback) => {
    callback = callback || null
    const fields = that.state.fields
    fields[id] = value
    that.setState({ fields: fields }, callback)
}

const getHistoryStatusTranslationKey = (moduleName, history) => {
    return `${moduleName}:${moduleName}.${history.status === 'SENT' && history.flux !== 'TRANSMISSION_ACTE'
        && moduleName === 'acte' ? `flux_status.${history.flux}_${history.status}` : `status.${history.status}`}`
}

const getRightsFromGroups = (groups = []) => {
    const rights = []
    groups.forEach(group =>
        group.rights.forEach(right => {
            if (!rights.includes(right)) rights.push(right)
        })
    )
    return rights
}

const rightsFeatureResolver = (userRights, allowedRights) => {
    if (!allowedRights || allowedRights.length === 0) return true
    for (let i in userRights) {
        if (allowedRights.includes(userRights[i])) return true
    }
    return false
}

const rightsModuleResolver = (userRights, moduleName) => {
    for (let i in userRights) {
        if (userRights[i].includes(moduleName)) return true
    }
    return false
}

const getLocalAuthoritySlug = () => {
    const authorizedPaths = ['/mentions-legales', '/registre-des-deliberations']
    const { pathname } = window.location
    const pathnameArray = pathname.split('/')
    if (pathnameArray.length > 1 && pathname !== '/' && !authorizedPaths.some(i => i.startsWith(pathname))) {
        return pathnameArray[1]
    }
    return null
}

const getMultiPahtFromSlug = () => {
    const localAuthoritySlug = getLocalAuthoritySlug()
    return localAuthoritySlug ? `/${localAuthoritySlug}` : ''
}

const isPDF = (filename) => {
    const tab = filename.toLowerCase().split('.')
    if(tab.length === 0) return false
    return tab[tab.length - 1] === 'pdf'
}

const toUniqueArray = array => [...new Set(array)]

const getUpdates = () =>
    fetch(updatesFile)
        .then(response => response.text())

const getLastUpdate = () =>
    fetch(updatesFile)
        .then(response => response.text())
        .then(updates => /(^#{3} (?:.|\n)*?)(?=\n#{3} )/.exec(updates)[0])

const handleSearchChange = (that, field, value) => {
    const search = that.state.search
    search[field] = value
    that.setState({search, offset: 0, currentPage: 0})
}

const getSearchData = (that) => {
    const { limit, offset, direction, column } = that.state
    const data = { limit, offset, direction, column }
    Object.keys(that.state.search)
        .filter(k => that.state.search[k] !== '')
        .map(k => data[k] = that.state.search[k])
}

const handlePageClick = (that, data, callback) => {
    const offset = Math.ceil(data.selected * that.state.limit)
    that.setState({ offset, currentPage: data.selected }, callback)
}

const updateItemPerPage = (that, limit, callback) => {
    that.setState({ limit, offset: 0, currentPage: 0 }, callback)
}

const sortTable = (that, clickedColumn, callback) => {
    const { column, direction } = that.state
    if (column !== clickedColumn) {
        that.setState({ column: clickedColumn, direction: 'ASC' }, callback)
        return
    }
    that.setState({ direction: direction === 'ASC' ? 'DESC' : 'ASC' }, callback)
}

const onSearch = (that, callback) => {
    that.setState({ offset: 0, currentPage: 0 }, callback)
}

const sortAlphabetically = (arrayOfObject, attribute) => {
    arrayOfObject.sort((a,b) => {
        if(a[attribute] < b[attribute]) { return -1 }
        if(a[attribute] > b[attribute]) { return 1 }
        return 0
    })

    return arrayOfObject
}

export {
    checkStatus,
    fetchWithAuthzHandling,
    handleFieldCheckboxChange,
    handleFieldChange,
    bytesToSize,
    capitalizeFirstLetter,
    getHistoryStatusTranslationKey,
    getRightsFromGroups,
    rightsFeatureResolver,
    rightsModuleResolver,
    updateField,
    updateChekboxField,
    getLocalAuthoritySlug,
    getMultiPahtFromSlug,
    isPDF,
    toUniqueArray,
    getUpdates,
    getLastUpdate,
    handleSearchChange,
    getSearchData,
    handlePageClick,
    updateItemPerPage,
    sortTable,
    onSearch,
    sortAlphabetically
}
