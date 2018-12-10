import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Button, Input } from 'semantic-ui-react'

import TextEditor from '../_components/TextEditor'
import { Page, FieldInline } from '../_components/UI'
import { notifications } from '../_util/Notifications'
import { checkStatus } from '../_util/utils'

class AdminInstance extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        fields: {
            contactEmail: '',
            reportUrl: '',
            welcomeMessage: '',
            legalNotice: ''
        }
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/instance' })
            .then(checkStatus)
            .then(response => response.json())
            .then(fields => this.setState({ fields }))
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.instance.title', json.message)
                })
            })
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        this.setState({ fields })
    }
    onWelcomeMessageChange = (welcomeMessage) => {
        const { fields } = this.state
        fields.welcomeMessage = welcomeMessage
        this.setState({ fields })
    }
    submitForm = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const headers = { 'Content-Type': 'application/json' }
        const body = JSON.stringify(this.state.fields)
        _fetchWithAuthzHandling({ url: '/api/admin/instance', method: 'PUT', body, headers, context: this.context })
            .then(checkStatus)
            .then(() => _addNotification(notifications.admin.instanceParamsUpdated))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.admin.instance.title', text))
            })
    }
    render() {
        const { t } = this.context
        return (
            <Page title={t('admin.instance_params.title')}>
                <Segment>
                    <Form onSubmit={this.submitForm}>

                        <h2 className='secondary'>{t('admin.instance_params.general_settings')}</h2>
                        <FieldInline htmlFor='contactEmail' label={t('admin.instance_params.contact_email')}>
                            <Input id='contactEmail'
                                value={this.state.fields.contactEmail || ''}
                                onChange={(e, data) => this.handleFieldChange('contactEmail', data.value)} />
                        </FieldInline>
                        <FieldInline htmlFor='reportUrl' label={t('admin.instance_params.report_url')}>
                            <Input id='reportUrl' fluid
                                placeholder='https://...'
                                value={this.state.fields.reportUrl || ''}
                                onChange={(e, data) => this.handleFieldChange('reportUrl', data.value)} />
                        </FieldInline>

                        <h2 className='secondary'>{t('admin.instance_params.welcome_message')}</h2>
                        <TextEditor
                            onChange={value => this.handleFieldChange('welcomeMessage', value)}
                            text={this.state.fields.welcomeMessage} />

                        <h2 className='secondary'>{t('admin.instance_params.legal_notice')}</h2>
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