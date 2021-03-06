import updatesFile from '../updates.md'
import moment from 'moment'
import {unauthorizedRequestAllowed} from './constants'


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


const customFetch = async (URL, options) => {
    try {
        const response = await fetch(URL, options)
        if(!response.ok){
            throw response
        }
        return response
    }catch (errorResponse) {
        console.error(`Error : ${errorResponse.status} on ${errorResponse.url}`)
        const localAuthority = options.headers.localAuthoritySlug
        const errorUrl = errorResponse.url.split('/').slice(3).join('/')
        switch (errorResponse.status) {
        case 401:
        case 403:
            if(!unauthorizedRequestAllowed.includes(errorUrl)) {
                const redirectPath = localAuthority ?
                    `/api/api-gateway/loginWithSlug/${localAuthority}`:
                    '/'
                window.location.replace(redirectPath)
            } else {
                throw errorResponse
            }
            break
        default:
            throw errorResponse
        }
    }
}

const fetchWithAuthzHandling = ({ url, method, body, query, context, headers }) => {
    const httpMethod = method || 'GET'
    const data = body || undefined
    const params = query || null
    const queryParams = params ? '?' + Object.keys(params)
        .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
        .join('&') : ''
    const additionalHeaders = headers || {}
    let httpHeaders = {}
    if (httpMethod === 'POST' || httpMethod === 'PUT' || httpMethod === 'PATCH' || httpMethod === 'DELETE') {
        httpHeaders = { [context.csrfTokenHeaderName]: context.csrfToken }
    }
    const localAuthoritySlug = getLocalAuthoritySlug()
    if (localAuthoritySlug) {
        httpHeaders.localAuthoritySlug = localAuthoritySlug
    }
    httpHeaders = Object.assign({}, httpHeaders, additionalHeaders)

    return customFetch(url + queryParams, {
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

const handleFieldChange = (that, field, value, callback) => {
    callback = callback || null
    const fields = that.state.fields
    updateField(fields, field, value)
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
        if(a[attribute] && b[attribute]) {
            if (a[attribute].toLowerCase() < b[attribute].toLowerCase()) {
                return -1
            }
            if (a[attribute].toLowerCase() > b[attribute].toLowerCase()) {
                return 1
            }
            return 0
        }else {
            return -1
        }
    })

    return arrayOfObject
}
const convertDateBackFormatToUIFormat = (date, format = 'DD/MM/YYYY') => {
    return moment(date, 'YYYY-MM-DDTHH:mm:ss').format(format)
}

const sum = (items, prop) => {
    if (items == null) {
        return 0
    }
    return items.reduce( (a, b) => {
        return b[prop] == null ? a : a + b[prop]
    }, 0)
}

const extractFieldNameFromId = (str) => {
    return str.split('_').slice(-1)[0]
}

const objectsIsEquivalent = (obj1, obj2) => {
    //Loop through properties in object 1
    if(typeof (obj1) === 'object') {
        for (let p in obj1) {
            //Check property exists on both objects
            if (typeof (obj2) !== 'object' || obj1.hasOwnProperty(p) !== obj2.hasOwnProperty(p)) return false

            switch (typeof (obj1[p])) {
            //Deep compare objects
            case 'object':
                if (typeof (obj2) === 'object' && !objectsIsEquivalent(obj1[p], obj2[p])) return false
                break
                //Compare function code
            case 'function':
                if (typeof (obj2[p]) === 'undefined' || (p !== 'compare' && obj1[p].toString() !== obj2[p].toString())) return false
                break
                //Compare values
            default:
                if (obj1[p] !== obj2[p]) return false
            }
        }
    }

    //Check object 2 for any extra properties
    if(typeof (obj2) === 'object') {
        for (let p in obj2) {
            if (obj1 && typeof (obj1[p]) === 'undefined') return false
        }
    }

    return true
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
    sortAlphabetically,
    convertDateBackFormatToUIFormat,
    extractFieldNameFromId,
    sum,
    objectsIsEquivalent
}
