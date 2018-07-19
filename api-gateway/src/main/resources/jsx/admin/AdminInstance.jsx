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
        fields: {
            welcomeMessage: '',
            legalNotice: ''
        }
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/admin/instance' })
            .then(checkStatus)
            .then(response => response.json())
            .then(text => this.setState({ fields: text }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.instance.title', json.message)
                })
            })
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        this.setState({ fields })
    }
    onWelcomeMessageChange = ( welcomeMessage ) => {
        const { fields } = this.state
        fields.welcomeMessage = welcomeMessage
        this.setState({ fields })
    }
    submitForm = () => {
        const headers = { 'Content-Type': 'application/json' }
        const body = JSON.stringify(this.state.fields)
        fetchWithAuthzHandling({ url: '/api/admin/instance', method: 'PUT', body, headers, context: this.context })
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
                    <Form onSubmit={this.submitForm}>

                        <h2>{t('admin.instance_params.welcome_message')}</h2>
                        <TextEditor
                            onChange={value => this.handleFieldChange('welcomeMessage', value)}
                            text={this.state.fields.welcomeMessage} />

                        <h2>{t('admin.instance_params.legal_notice')}</h2>
                        <TextEditor
                            onChange={value => this.handleFieldChange('legalNotice', value)}
                            text={this.state.fields.legalNotice} />

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