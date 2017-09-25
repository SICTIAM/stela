
const checkStatus = (response) => {
    if (response.status >= 200 && response.status < 300) {
        return response
    } else {
        throw response
    }
}

// TODO: add 403 controls
const fetchWithAuthzHandling = ({ url, method, body, query, context, headers }) => {
    const httpMethod = method || "GET"
    const data = body || {}
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

const handleFieldCheckboxChange = (that, field) => {
    const fields = that.state.fields
    fields[field] = !fields[field]
    that.setState({ fields: fields })
}

const handleFieldChange = (that, e) => {
    const { id, value } = e.target
    const fields = that.state.fields
    fields[id] = value
    that.setState({ fields: fields })
}

module.exports = { checkStatus, fetchWithAuthzHandling, handleFieldCheckboxChange, handleFieldChange }