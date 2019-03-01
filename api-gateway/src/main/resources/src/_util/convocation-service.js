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