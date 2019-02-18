import {notifications} from './Notifications'
import moment from 'moment'

export default class ActeService {

    deserializeActe = (acte) => {
        // Hacks to prevent affecting `null` values from the new empty returned acte
        if (!acte.nature) acte.nature = ''
        if (!acte.code) acte.code = ''
        if (!acte.codeLabel) acte.codeLabel = ''
        if (!acte.decision) {
            acte.decision = ''
        } else {
            acte.decision = moment(acte)
        }
        if (!acte.objet) acte.objet = ''
        if (!acte.number) acte.number = ''
        if (!acte.annexes) acte.annexes = []

        return acte
    }

    getActeByDraftUuidAndUuid = async (draftUuid, uuid, context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try {
            return await (await _fetchWithAuthzHandling({url: `/api/acte/drafts/${draftUuid}/${uuid}`, context: context})).json()
        }catch(error){
            error.json().then(json => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
            })
        }
    }

    getDepositFields = async (context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try {
            return await (await _fetchWithAuthzHandling({url: '/api/acte/localAuthority/depositFields', context: context})).json()
        }catch(error){
            error.json().then(json => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
            })
        }
    }

    getSubjectCode = async (context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try {
            return await (await _fetchWithAuthzHandling({url: '/api/acte/localAuthority/codes-matieres', context: context})).json()
        }catch(error){
            error.json().then(json => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
            })
        }
    }

    getAttachmentTypes = async (nature, subjectCode, context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try {
            const headers = { 'Content-Type': 'application/json' }
            return await (await _fetchWithAuthzHandling({ url: `/api/acte/attachment-types/${nature}/${subjectCode}`, headers: headers, context: context })).json()
        }catch(error){
            error.json().then(json => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
            })
        }
    }

    createNewActeDraftByActeMode = async (acteMode, context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try {
            return await (await _fetchWithAuthzHandling({url: `/api/acte/draft/${acteMode}`, context: context})).json()
        }catch(error){
            error.json().then(json => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
            })
        }
    }

    saveDraft = async (draftUuid, uuid, acteData, context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try {
            const headers = { 'Content-Type': 'application/json' }
            return await (await _fetchWithAuthzHandling({ url: `/api/acte/drafts/${draftUuid}/${uuid}`, body: JSON.stringify(acteData), headers: headers, method: 'PUT', context: context })).text()
        }catch(error){
            error.text().then(json => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
            })
        }
    }

    saveDraftAttachment = async (data, draftUuid, uuid, type, context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try {
            return await (await _fetchWithAuthzHandling({url: `/api/acte/drafts/${draftUuid}/${uuid}/${type}`, body: data, method: 'POST', context: context})).json()
        }catch(response){
            if (response.status === 400) {
                response.json().then(json =>
                    _addNotification(notifications.defaultError, 'notifications.acte.title',
                        json.errors[0].defaultMessage))
            } else {
                response.text().then(text =>
                    _addNotification(notifications.defaultError, 'notifications.acte.title', text))
            }
        }
    }

    postActeForm =  async (draftUuid, uuid, context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try{
            return await (await _fetchWithAuthzHandling({url: `/api/acte/drafts/${draftUuid}/${uuid}`, method: 'POST', context: context})).text()
        }catch (e) {
            if (e.status === 400) {
                e.json().then(json =>
                    _addNotification(notifications.defaultError, 'notifications.acte.title', json.errors[0].defaultMessage)
                )
            } else {
                e.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
            }
        }
    }

    updateAttachmentType = async (draftUuid, uuid, code, context, annexeUuid) => {
        const {_fetchWithAuthzHandling, _addNotification} = context
        let url = ''
        if (annexeUuid) {
            url = `/api/acte/drafts/${draftUuid}/${uuid}/file/type/${code}`
        } else {
            url = `/api/acte/drafts/${draftUuid}/${uuid}/annexe/${annexeUuid}/type/${code}`
        }

        try {
            return await _fetchWithAuthzHandling({url: url, method: 'PUT', context: context})
        } catch (error) {
            error.text().then(json => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
            })
        }

    }

    deleteDraft = async (draftUuid, context) => {
        const {_fetchWithAuthzHandling, _addNotification} = context
        try {
            const headers = {'Content-Type': 'application/json'}
            return await _fetchWithAuthzHandling({ url: '/api/acte/drafts', body: JSON.stringify([draftUuid]), headers: headers, method: 'DELETE', context: context})
        }catch(error){
            error.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
        }
    }


    deleteAttachmentTypes = async (draftUuid, uuid, context) => {
        const {_fetchWithAuthzHandling, _addNotification} = context
        try {
            return await _fetchWithAuthzHandling({ url: `/api/acte/drafts/${draftUuid}/${uuid}/types`, method: 'DELETE', context: context})
        }catch(error){
            error.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
        }
    }

    deleteDraftAttachment = async (draftUuid, uuid, context, annexeUuid) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        let url = ''
        if(annexeUuid){
            url = `/api/acte/drafts/${draftUuid}/${uuid}/annexe/${annexeUuid}`
        }else{
            url = `/api/acte/drafts/${draftUuid}/${uuid}/file`
        }

        try {
            return await (await _fetchWithAuthzHandling({ url: url, method: 'DELETE', context: context})).text()
        }catch(error){
            error.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
        }
    }

}
