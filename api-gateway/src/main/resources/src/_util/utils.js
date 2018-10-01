
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

const handleFieldChange = (that, e, callback) => {
    callback = callback || null
    const { id, value } = e.target
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
    toUniqueArray
}