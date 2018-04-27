import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Message } from 'semantic-ui-react'
import moment from 'moment'

import { notifications } from '../_util/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'

class AlertMessage extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        alertMessageModules: {},
        alertMessageModulesDismissed: {}
    }
    componentDidMount() {
        this.clearStorageDate()
        fetchWithAuthzHandling({ url: '/api/api-gateway/alert-messages' })
            .then(checkStatus)
            .then(response => response.json())
            .then(alertMessageModules => {
                const { alertMessageModulesDismissed } = this.state
                const alertMessageModulesDismissedStorage = localStorage.getItem('alertMessageModulesDismissed')
                Object.entries(alertMessageModules).forEach(alertMessageModule => {
                    alertMessageModulesDismissed[alertMessageModule[0]] = alertMessageModulesDismissedStorage ?
                        JSON.parse(alertMessageModulesDismissedStorage)[alertMessageModule[0]] ?
                            JSON.parse(alertMessageModulesDismissedStorage)[alertMessageModule[0]] : false : false
                })
                this.setState({ alertMessageModules, alertMessageModulesDismissed })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.title', json.message)
                })
            })
    }
    clearStorageDate = () => {
        const alertMessageModulesDismissedStorage = localStorage.getItem('alertMessageModulesDismissed')
        const alertMessageModulesDismissedDateStorage = localStorage.getItem('alertMessageModulesDismissedDate')
        if (alertMessageModulesDismissedStorage && alertMessageModulesDismissedDateStorage) {
            const alertMessageModulesDismissed = JSON.parse(alertMessageModulesDismissedStorage)
            const alertMessageModulesDismissedDate = JSON.parse(alertMessageModulesDismissedDateStorage)
            Object.entries(alertMessageModulesDismissedDate).forEach(modulesDismissedDate => {
                if (moment().startOf('day').isAfter(modulesDismissedDate[1])) {
                    delete alertMessageModulesDismissed[modulesDismissedDate[0]]
                    delete alertMessageModulesDismissedDate[modulesDismissedDate[0]]
                }
            })
            localStorage.setItem('alertMessageModulesDismissed', JSON.stringify(alertMessageModulesDismissed))
            localStorage.setItem('alertMessageModulesDismissedDate', JSON.stringify(alertMessageModulesDismissedDate))
        }
    }
    updateStorageForModule = (storage, moduleName, value) => {
        const unParsedStorage = localStorage.getItem(storage)
        if (unParsedStorage) {
            const parsedStorage = JSON.parse(unParsedStorage)
            parsedStorage[moduleName] = value
            localStorage.setItem(storage, JSON.stringify(parsedStorage))
        } else {
            const newStorage = { [moduleName]: value }
            localStorage.setItem(storage, JSON.stringify(newStorage))
        }
    }
    handleDismiss = () => {
        const currentModule = this.getCurrentModule()
        this.updateStorageForModule('alertMessageModulesDismissedDate', currentModule, moment().format('YYYY-MM-DD'))
        this.updateStorageForModule('alertMessageModulesDismissed', currentModule, true)
        const { alertMessageModulesDismissed } = this.state
        alertMessageModulesDismissed[currentModule] = true
        this.setState({ alertMessageModulesDismissed })
    }
    getCurrentModule = () => this.props.location.pathname.split('/')[1]
    render() {
        const { t } = this.context
        const { alertMessageModules, alertMessageModulesDismissed } = this.state
        const currentModule = this.getCurrentModule()
        if (alertMessageModules[currentModule] && alertMessageModules[currentModule].alertMessageDisplayed && alertMessageModulesDismissed[currentModule] === false)
            return (
                <Message
                    className='error'
                    onDismiss={this.handleDismiss}
                    header={`${t('alert')} ${t(`modules.${currentModule.toUpperCase()}`)}`}
                    content={alertMessageModules[currentModule] ? alertMessageModules[currentModule].alertMessage : ''}
                />
            )
        return null
    }
}

export default translate(['api-gateway'])(AlertMessage)