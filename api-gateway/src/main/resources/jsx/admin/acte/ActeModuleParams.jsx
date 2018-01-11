import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Form, Button, Segment, Label, Icon, Checkbox } from 'semantic-ui-react'
import Validator from 'validatorjs'
import moment from 'moment'

import InputValidation from '../../_components/InputValidation'
import InputDatetime from '../../_components/InputDatetime'
import { notifications } from '../../_util/Notifications'
import { Field, FormField } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class ActeModuleParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        newEmail: '',
        isFormValid: true,
        fields: {
            mainEmail: '',
            additionalEmails: [],
            miatAccessible: true,
            inaccessibilityMiatStartDate: '',
            inaccessibilityMiatEndDate: ''
        }
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/acte/admin' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                json.inaccessibilityMiatStartDate = new moment.utc(json.inaccessibilityMiatStartDate)
                json.inaccessibilityMiatEndDate = new moment.utc(json.inaccessibilityMiatEndDate)
                this.setState({ fields: json })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.instance.title', json.message)
                })
            })
    }
    onkeyPress = (event) => {
        // prevent from sending the form on 'Enter' to add the email to the list
        if (event.key === 'Enter') {
            this.addMail(event)
        }
    }
    addMail = (event) => {
        event.preventDefault()
        if (this.validateEmail(this.state.newEmail)) {
            const fields = this.state.fields
            fields.additionalEmails.push(this.state.newEmail)
            this.setState({ newEmail: '', fields: fields })
        }
    }
    onRemoveMail = (index) => {
        const fields = this.state.fields
        fields.additionalEmails.splice(index, 1)
        this.setState({ fields: fields })
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        this.setState({ fields: fields }, this.validateForm)
    }
    handleCheckboxChange = (event, { id }) => {
        const { fields } = this.state
        fields[id] = !fields[id]
        this.setState({ fields })
    }
    validateForm = () => {
        this.setState({ isFormValid: this.validateEmail(this.state.fields.mainEmail) })
    }
    validateEmail = (email) => {
        const validation = new Validator({ email }, { email: 'required|email' })
        return validation.passes()
    }
    submitForm = (event) => {
        event.preventDefault()
        const data = JSON.stringify(this.state.fields)
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: '/api/acte/admin', method: 'PATCH', body: data, headers: headers, context: this.context })
            .then(checkStatus)
            .then(() => this.context._addNotification(notifications.admin.moduleUpdated))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.admin.instance.title', text))
            })
    }
    render() {
        const { t } = this.context
        const listEmail = this.state.fields.additionalEmails.map((item, index) =>
            <Label basic key={index}>{item} <Icon name='delete' onClick={() => this.onRemoveMail(index)} /></Label>
        )
        return (
            <Segment>
                <h1>{t('admin.modules.acte.module_settings.title')}</h1>
                <Form onSubmit={this.submitForm}>
                    <Field htmlFor='mainEmail' label={t('admin.modules.acte.module_settings.main_email')}>
                        <InputValidation id='mainEmail'
                            value={this.state.fields.mainEmail}
                            fieldName={t('admin.modules.acte.module_settings.main_email')}
                            validationRule='required|email'
                            onChange={this.handleFieldChange}
                            className='simpleInput' />
                    </Field>
                    <Field htmlFor='additionalEmail' label={t('admin.modules.acte.module_settings.additional_emails')}>
                        <div>{listEmail.length > 0 ? listEmail : t('admin.modules.acte.module_settings.no_additional_email')}</div>
                        <input id='additionalEmail'
                            onKeyPress={this.onkeyPress}
                            value={this.state.newEmail}
                            onChange={(e) => this.setState({ newEmail: e.target.value })}
                            className='simpleInput' />
                        <Button style={{ marginLeft: '1em' }} onClick={(event) => this.addMail(event)}>{t('api-gateway:form.add')}</Button>
                    </Field>
                    <Field htmlFor='miatAccessible' label={t('admin.modules.acte.module_settings.miatAccessible')}>
                        <Checkbox id="miatAccessible"
                            toggle checked={this.state.fields.miatAccessible}
                            onChange={this.handleCheckboxChange} />
                    </Field>
                    <Field htmlFor='inaccessibilityMiat' label={t('admin.modules.acte.module_settings.inaccessibilityMiat')}>
                        <Form.Group style={{ marginBottom: 0 }}>
                            <FormField htmlFor='inaccessibilityMiatStartDate' label={t('api-gateway:form.from')}>
                                <InputDatetime id='inaccessibilityMiatStartDate'
                                    value={this.state.fields.inaccessibilityMiatStartDate}
                                    onChange={date => this.handleFieldChange('inaccessibilityMiatStartDate', date)} />
                            </FormField>
                            <FormField htmlFor='inaccessibilityMiatEndDate' label={t('api-gateway:form.to')}>
                                <InputDatetime id='inaccessibilityMiatEndDate'
                                    value={this.state.fields.inaccessibilityMiatEndDate}
                                    onChange={date => this.handleFieldChange('inaccessibilityMiatEndDate', date)} />
                            </FormField>
                        </Form.Group>
                    </Field>
                    <Button style={{ marginTop: '2em' }} primary type='submit'>{t('api-gateway:form.update')}</Button>
                </Form>
            </Segment>
        )
    }
}

export default translate(['acte', 'api-gateway'])(ActeModuleParams)