import {notifications} from './Notifications'

export default class AdminService {

    getGroups = async (context) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        try {
            return await (await _fetchWithAuthzHandling({url: '/api/admin/profile/groups', context: context})).json()
        }catch(error){
            error.json().then(json => {
                _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
            })
        }
    }
}
