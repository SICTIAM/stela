import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Button } from 'semantic-ui-react'

import TextEditor from '../_components/TextEditor'
import { Page } from '../_components/UI'
import { notifications } from '../_util/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'

class AdminInstance extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        welcomeMessage: ''
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/admin/instance/welcome-message' })
            .then(checkStatus)
            .then(response => response.text())
            .then(text => this.setState({ welcomeMessage: text }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.instance.title', json.message)
                })
            })
    }
    onWelcomeMessageChange = (welcomeMessage) => {
        this.setState({ welcomeMessage })
    }
    submitForm = () => {
        const headers = { 'Content-Type': 'text/plain' }
        fetchWithAuthzHandling({ url: '/api/admin/instance/welcome-message', method: 'PUT', body: this.state.welcomeMessage, headers: headers, context: this.context })
            .then(checkStatus)
            .then(() => this.context._addNotification(notifications.admin.instanceParamsUpdated))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.admin.instance.title', text))
            })
    }
    render() {
        const { t } = this.context
        return (
            <Page title={t('admin.instance_params.title')}>
                <Segment>
                    <h2>{t('admin.instance_params.welcome_message')}</h2>
                    <Form onSubmit={this.submitForm}>
                        <TextEditor
                            onChange={this.onWelcomeMessageChange}
                            text={this.state.welcomeMessage} />
                        <div style={{ textAlign: 'right' }}>
                            <Button basic primary style={{ marginTop: '2em' }} type='submit'>{t('form.update')}</Button>
                        </div>
                    </Form>
                </Segment>
            </Page >
        )
    }
}

export default translate(['api-gateway'])(AdminInstance)