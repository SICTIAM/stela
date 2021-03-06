import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Form, Button, Segment, Label, Icon, Checkbox, TextArea } from 'semantic-ui-react'
import Validator from 'validatorjs'
import moment from 'moment'

import InputValidation from '../../_components/InputValidation'
import InputDatetime from '../../_components/InputDatetime'
import { notifications } from '../../_util/Notifications'
import { FieldInline, Page, InputTextControlled } from '../../_components/UI'
import { checkStatus, handleFieldChange } from '../../_util/utils'
import { withAuthContext } from '../../Auth'

class ActeModuleParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        newEmail: '',
        isFormValid: true,
        dateValidation: '',
        fields: {
            mainEmail: '',
            additionalEmails: [],
            miatAvailable: true,
            unavailabilityMiatStartDate: '',
            unavailabilityMiatEndDate: '',
            alertMessageDisplayed: false,
            alertMessage: ''
        }
    }
    validationRules = {
        mainEmail: 'required|email',
        unavailabilityMiatStartDate: 'required|date|dateOrder',
        unavailabilityMiatEndDate: 'required|date',
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/acte/admin' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                json.unavailabilityMiatStartDate = new moment.utc(json.unavailabilityMiatStartDate)
                json.unavailabilityMiatEndDate = new moment.utc(json.unavailabilityMiatEndDate)
                this.setState({ fields: json })
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.instance.title', json.message)
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
    handleCheckboxChange = (event, { id }) => {
        const { fields } = this.state
        fields[id] = !fields[id]
        this.setState({ fields })
    }
    validateForm = () => {
        this.setState({ isFormValid: this.validateEmail(this.state.fields.mainEmail) && this.validateDates().passes() })
    }
    validateEmail = (email) => {
        const validation = new Validator({ email }, { email: 'required|email' })
        return validation.passes()
    }
    validateDates = () => {
        const { unavailabilityMiatStartDate, unavailabilityMiatEndDate } = this.state.fields
        Validator.register('dateOrder',
            () => moment(this.state.fields.unavailabilityMiatStartDate).isSameOrBefore(this.state.fields.unavailabilityMiatEndDate),
            this.context.t('api-gateway:form.validation.date_begin_before_date_end'))
        const validation = new Validator({ unavailabilityMiatStartDate, unavailabilityMiatEndDate },
            { unavailabilityMiatStartDate: 'required|date|dateOrder', unavailabilityMiatEndDate: 'required|date' })
        return validation
    }
    updateDateValidation = () => {
        const validation = this.validateDates()
        validation.passes()
        this.setState({ dateValidation: validation.errors.first('unavailabilityMiatStartDate') || '' })
    }
    submitForm = (event) => {
        event.preventDefault()
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const data = JSON.stringify(this.state.fields)
        const headers = { 'Content-Type': 'application/json' }
        _fetchWithAuthzHandling({ url: '/api/acte/admin', method: 'PATCH', body: data, headers: headers, context: this.props.authContext })
            .then(checkStatus)
            .then(() => _addNotification(notifications.admin.moduleUpdated))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.admin.instance.title', text))
            })
    }
    render() {
        const { t } = this.context
        const listEmail = this.state.fields.additionalEmails.map((item, index) =>
            <Label basic key={index}>{item} <Icon name='delete' onClick={() => this.onRemoveMail(index)} /></Label>
        )
        return (
            <Page title={t('admin.modules.acte.module_settings.title')}>
                <Segment>
                    <Form onSubmit={this.submitForm}>
                        <FieldInline htmlFor='mainEmail' label={t('admin.modules.acte.module_settings.main_email')}>
                            <InputValidation id='mainEmail'
                                value={this.state.fields.mainEmail}
                                fieldName={t('admin.modules.acte.module_settings.main_email')}
                                validationRule='required|email'
                                onChange={(id, value) => handleFieldChange(this, id, value, this.validateForm)}
                                className='simpleInput' />
                        </FieldInline>
                        <FieldInline htmlFor='additionalEmail' label={t('admin.modules.acte.module_settings.additional_emails')}>
                            <div style={{ marginBottom: '0.5em' }}>
                                {listEmail.length > 0 ? listEmail : t('admin.modules.acte.module_settings.no_additional_email')}
                            </div>
                            <input id='additionalEmail'
                                onKeyPress={this.onkeyPress}
                                value={this.state.newEmail}
                                onChange={(e) => this.setState({ newEmail: e.target.value })}
                                className='simpleInput' />
                            <Button basic color='grey' style={{ marginLeft: '1em' }} onClick={(event) => this.addMail(event)}>
                                {t('api-gateway:form.add')}
                            </Button>
                        </FieldInline>
                        <FieldInline htmlFor='miatAvailable' label={t('admin.modules.acte.module_settings.miatAvailable')}>
                            <Checkbox id="miatAvailable"
                                toggle checked={this.state.fields.miatAvailable}
                                onChange={this.handleCheckboxChange} />
                        </FieldInline>
                        <FieldInline htmlFor='unavailabilityMiat' label={t('admin.modules.acte.module_settings.unavailabilityMiat')}>
                            <Form.Group style={{ marginBottom: 0, flexDirection: 'column' }} className='test'>
                                <div style={{ display: 'flex', flexDirection: 'row' }}>
                                    <label htmlFor='unavailabilityMiatStartDate' style={{ marginRight: '0.5em' }}>{t('api-gateway:form.from')}</label>
                                    <InputDatetime id='unavailabilityMiatStartDate'
                                        onBlur={this.updateDateValidation}
                                        value={this.state.fields.unavailabilityMiatStartDate}
                                        onChange={date => handleFieldChange(this, 'unavailabilityMiatStartDate', date, this.validateForm)} />
                                    <label htmlFor='unavailabilityMiatEndDate' style={{ marginLeft: '1em', marginRight: '0.5em' }}>
                                        {t('api-gateway:form.to')}
                                    </label>
                                    <InputDatetime id='unavailabilityMiatEndDate'
                                        onBlur={this.updateDateValidation}
                                        value={this.state.fields.unavailabilityMiatEndDate}
                                        onChange={date => handleFieldChange(this, 'unavailabilityMiatEndDate', date, this.validateForm)} />
                                </div>
                                {this.state.dateValidation && (
                                    <div style={{ display: 'flex', flexDirection: 'row' }}>
                                        <Label color='red' pointing>{this.state.dateValidation}</Label>
                                    </div>
                                )}
                            </Form.Group>
                        </FieldInline>
                        <FieldInline htmlFor='alertMessageDisplayed' label={t('admin.modules.acte.module_settings.alertMessageDisplayed')}>
                            <Checkbox id='alertMessageDisplayed'
                                toggle checked={this.state.fields.alertMessageDisplayed}
                                onChange={this.handleCheckboxChange} />
                        </FieldInline>
                        <FieldInline htmlFor='alertMessage' label={t('admin.modules.acte.module_settings.alertMessage')}>
                            <InputTextControlled component={TextArea}
                                id='alertMessage'
                                maxLength={250}
                                placeholder={`${t('admin.modules.acte.module_settings.alertMessage')}...`}
                                value={this.state.fields.alertMessage || ''}
                                onChange={(id, value) => handleFieldChange(this, id, value, this.validateForm)} />
                        </FieldInline>
                        <div style={{ textAlign: 'right' }}>
                            <Button basic primary disabled={!this.state.isFormValid} style={{ marginTop: '2em' }} type='submit'>
                                {t('api-gateway:form.update')}
                            </Button>
                        </div>
                    </Form>
                </Segment>
            </Page >
        )
    }
}

export default translate(['acte', 'api-gateway'])(withAuthContext(ActeModuleParams))