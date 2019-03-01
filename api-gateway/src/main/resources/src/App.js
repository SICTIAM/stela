import React, { Component } from 'react'
import PropTypes from 'prop-types'
import NotificationSystem from 'react-notification-system'
import { translate } from 'react-i18next'

import './semantic/dist/semantic.min.css'
import AuthProvider from './Auth'

import {fetchWithAuthzHandling} from './_util/utils'

class App extends Component {
    constructor() {
        super()
        this._notificationSystem = null
    }
    static contextTypes = {
        t: PropTypes.func
    }
    static propTypes = {
        children: PropTypes.element.isRequired
    }
    static childContextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        isMenuOpened: PropTypes.bool,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func,
        _openMenu: PropTypes.func
    }
    state = {
        csrfToken: '',
        csrfTokenHeaderName: '',
        localAuthoritySlug: '',
        isMenuOpened: false
    }
    NotificationStyle = {
        Containers: {
            DefaultStyle: {
                width: 400
            }
        }
    }
    getChildContext() {
        return {
            csrfToken: this.state.csrfToken,
            csrfTokenHeaderName: this.state.csrfTokenHeaderName,
            t: this.context.t,
            isMenuOpened: this.state.isMenuOpened,
            _openMenu: this._openMenu,
            _addNotification: this._addNotification,
            _fetchWithAuthzHandling: fetchWithAuthzHandling
        }
    }
    _openMenu = () => {
        this.setState({isMenuOpened: !this.state.isMenuOpened})
    }
    _addNotification = (notification, title, message) => {
        const { t } = this.context
        notification.title = t(title ? title : notification.title)
        notification.message = t(message ? message : notification.message)
        if (this._notificationSystem) {
            this._notificationSystem.addNotification(notification)
        }
    }

    render() {
        return (
            <div>
                <NotificationSystem ref={n => (this._notificationSystem = n)} style={this.NotificationStyle} />
                <AuthProvider>
                    {this.props.children}
                </AuthProvider>
            </div>
        )
    }
}

export default translate('api-gateway')(App)
