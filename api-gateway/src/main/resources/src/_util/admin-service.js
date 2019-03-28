import {notifications} from './Notifications'

export default class AdminService {


    getGroups = async (context, right = null) => {
        const { _fetchWithAuthzHandling, _addNotification } = context
        const url = right ? `/api/admin/profile/groups/${right}` : '/api/admin/profile/groups'
        try {
            return await (await _fetchWithAuthzHandling({url: url, context: context})).json()
        }catch(error){
            error.json().then(json => {
                _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
            })
        }
    }
}
