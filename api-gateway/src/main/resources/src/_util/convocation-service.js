import {notifications} from './Notifications'

export default class ConvocationService {

	// Get Received convocation with or without token
	getReceivedConvocation = async (context, uuid, paramsUrl) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context
	    const token = this.getTokenInUrl(paramsUrl)
	    try {
	        const response = await (await _fetchWithAuthzHandling({ url: `/api/convocation/received/${uuid}`, method: 'GET', query: token })).json()
	        return response
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	        throw error
	    }
	}

	getSentConvocation = async (context, uuid) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context
	    try {
	        const response = await (await _fetchWithAuthzHandling({ url: `/api/convocation/${uuid}`, method: 'GET'})).json()
	        return response
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	        throw error
	    }
	}

	getReceivedConvocationList = async (context, search, paramsUrl) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context
	    const token = this.getTokenInUrl(paramsUrl)
	    const query = token ? Object.assign(search, token) : search

	    try {
	        return await (await _fetchWithAuthzHandling({ url: '/api/convocation/received', method: 'GET', query: query })).json()
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	        throw error
	    }
	}

	getAllAssemblyType = async (context, paramsUrl) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context
	    const token = this.getTokenInUrl(paramsUrl)
	    try {
	        const response = await (await _fetchWithAuthzHandling({url: '/api/convocation/assembly-type/all', method: 'GET', query: token})).json()
	        return response
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	        throw error
	    }

	}

	getRecipients = async (context, uuid) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context
	    const url = uuid ? `/api/convocation/assembly-type/${uuid}/recipients` : '/api/convocation/local-authority/recipients'
	    try {
	        return await (await _fetchWithAuthzHandling({url: url, method: 'GET'})).json()
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	    }
	}

	saveAdditionnalQuestionResponse = async (context,uuid, value, question_uuid, paramsUrl) => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = context
	    const token = this.getTokenInUrl(paramsUrl)
	    try {
	        return await _fetchWithAuthzHandling({url: `/api/convocation/received/${uuid}/question/${question_uuid}/${value}`, method: 'PUT', context: context, query: token})
	    } catch(error) {
	        error.text().then(text => {
	            if(text) {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`${text}`))
	            } else {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`convocation.errors.convocation.${error.status}`))
	            }
	        })
	        throw error
	    }
	}

	savePresentResponse = async (context, uuid, response, paramsUrl) => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = context
	    const token = this.getTokenInUrl(paramsUrl)
	    let url = response.userUuid ? `/api/convocation/received/${uuid}/${response.response}/${response.userUuid}`: `/api/convocation/received/${uuid}/${response.response}`

	    try {
	        return await (await _fetchWithAuthzHandling({ url: url, method: 'PUT', context: context, query: token }))
	    } catch(error) {
	        error.text().then(text => {
	            if(text) {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`${text}`))
	            } else {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`convocation.errors.convocation.${error.status}`))
	            }
	        })
	        throw error
	    }
	}

	updateConvocation = async (context, uuid, parameters) => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = context
	    const headers = { 'Content-Type': 'application/json;charset=UTF-8', 'Accept': 'application/json, */*' }

	    try {
	        return await _fetchWithAuthzHandling({url: `/api/convocation/${uuid}`, method: 'PUT', body: JSON.stringify(parameters), context: context, headers: headers})
	    } catch(error) {
	        error.text().then(text => {
	            if(text) {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`${text}`))
	            } else {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`convocation.errors.convocation.${error.status}`))
	            }
	        })
	        throw error
	    }
	}

	updateDocumentsConvocation = async (context, uuid, data) => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = context

	    try {
	        return await _fetchWithAuthzHandling({url: `/api/convocation/${uuid}/upload`, method: 'PUT', body: data, context: context})
	    } catch(error) {
	        error.text().then(text => {
	            if(text) {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`${text}`))
	            } else {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`convocation.errors.convocation.${error.status}`))
	            }
	        })
	        throw error
	    }
	}

	cancelConvocation = async (context, uuid) => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = context

	    try {
	        return await _fetchWithAuthzHandling({url: `/api/convocation/${uuid}/cancel`, method: 'PUT', context: context})
	    } catch(error) {
	        error.text().then(text => {
	            if(text) {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`${text}`))
	            } else {
	                _addNotification(notifications.defaultError, 'notifications.title', t(`convocation.errors.${error.status}`))
	            }
	        })
	        throw error
	    }
	}

	saveRecipient = async (context, body, uuid, force) => {
	    const { _fetchWithAuthzHandling, _addNotification, t } = context
	    const headers = { 'Content-Type': 'application/json' }
	    const url = '/api/convocation/recipient' + (uuid ? `/${uuid}` : '')

	    try {
	        return await (await _fetchWithAuthzHandling({url: url, method: uuid ? 'PUT' : 'POST', headers: headers, body: body, context: context, query: {force: force}})).json()
	    } catch(error) {
	        error.json().then((json) => {
	            _addNotification(notifications.defaultError, 'api-gateway:notifications.admin.title', t(json.message))
	        })
	        throw error
	    }
	}

	getConfForLocalAuthority = async (context) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context

	    try {
	        return await (await _fetchWithAuthzHandling({url: '/api/convocation/local-authority'})).json()
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	        throw error
	    }

	}

	saveConfForLocalAuthority = async (context, data) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context
	    const headers = { 'Content-Type': 'application/json' }

	    try {
	        return await _fetchWithAuthzHandling({url: '/api/convocation/local-authority', method: 'PUT', headers: headers, body: JSON.stringify(data), context: context})
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	        throw error
	    }
	}

	saveDefaultProcuration = async (context, data) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context

	    try {
	        return await _fetchWithAuthzHandling({url: '/api/convocation/local-authority/procuration', method: 'POST', body: data, context: context})
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	        throw error
	    }
	}
	desactivateAllRecipients = async (context) => {
	    const { _fetchWithAuthzHandling, _addNotification } = context
	    const headers = { 'Content-Type': 'application/json' }
	    try {
	        return await _fetchWithAuthzHandling({url: '/api/convocation/recipient/deactivate-all', method: 'PUT', headers: headers, context: context})
	    } catch(error) {
	        error.json().then(json => {
	            _addNotification(notifications.defaultError, 'notifications.title', json.message)
	        })
	        throw error
	    }
	}

	getTokenInUrl = (search) => {
	    const arrayParams = search && search.substr(1).split('&')
	    const regex = /token/
	    const indexToken = arrayParams && arrayParams.findIndex((param) => {
	        return param.match(regex)
	    })
	    const token = indexToken !== undefined && indexToken !== '' && indexToken !== -1 && arrayParams[indexToken].split('=')[1]
	    return search && indexToken !== -1 ? {token: token} : null
	}


}