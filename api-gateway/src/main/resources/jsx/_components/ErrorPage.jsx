import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'

import { fetchWithAuthzHandling, checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'

class ErrorPage extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        certInfos: {
            status: ''
        }
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/api-gateway/certInfos' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ certInfos: json }))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.title', text))
            })
    }
    render() {
        const { t } = this.context
        const errorContent = this.props.error === 'certificate_required'
            ? t(`error.certificate_required.${this.state.certInfos.status}`)
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


