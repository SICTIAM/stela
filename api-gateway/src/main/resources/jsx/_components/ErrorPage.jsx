import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'

import { fetchWithAuthzHandling, checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'

class ErrorPage extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        certStatus: 'default'
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/admin/certificate/verified-status' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ certStatus: json }))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.title', text))
            })
    }
    render() {
        const { t } = this.context
        const errorContent = this.props.error === 'certificate_required'
            ? t(`error.certificate_required.${this.state.certStatus}`)
            : t(`error.${this.props.error}.content`)
        return (
            <div>
                <h1>{t(`error.${this.props.error}.title`)}</h1>
                <p>{errorContent}</p>
            </div>
        )
    }
}

export default translate('api-gateway')(ErrorPage)


