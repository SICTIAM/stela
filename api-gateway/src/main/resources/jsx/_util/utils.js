
const checkStatus = (response) => {
    if (response.status >= 200 && response.status < 300) {
        return response
    } else {
        throw response
    }
}

// TODO: add 403 controls
const fetchWithAuthzHandling = ({ url, method, body, context, headers }) => {
    const httpMethod = method || "GET"
    const data = body || {}
    const additionalHeaders = headers || {}
    let httpHeaders = {}
    if (httpMethod === 'POST' || httpMethod === 'PUT' || httpMethod === 'PATCH' || httpMethod === 'DELETE')
        httpHeaders = { [context.csrfTokenHeaderName]: context.csrfToken }
    httpHeaders = Object.assign({}, httpHeaders, additionalHeaders)

    return fetch(url, {
        method: httpMethod,
        credentials: 'same-origin',
        headers: httpHeaders,
        body: data
    })
}


module.exports = { checkStatus, fetchWithAuthzHandling }