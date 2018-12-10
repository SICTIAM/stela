import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Form, Button, Segment, Label, Checkbox, TextArea } from 'semantic-ui-react'
import Validator from 'validatorjs'
import moment from 'moment'

import InputDatetime from '../../_components/InputDatetime'
import { notifications } from '../../_util/Notifications'
import { FieldInline, Page, InputTextControlled } from '../../_components/UI'
import { checkStatus } from '../../_util/utils'

class PesModuleParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        isFormValid: true,
        dateValidation: '',
        fields: {
            heliosAvailable: true,
            unavailabilityHeliosStartDate: '',
            unavailabilityHeliosEndDate: '',
            alertMessageDisplayed: false,
            alertMessage: ''
        }
    }
    validationRules = {
        unavailabilityHeliosStartDate: 'required|date|dateOrder',
        unavailabilityHeliosEndDate: 'required|date',
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/pes/admin' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                json.unavailabilityHeliosStartDate = new moment.utc(json.unavailabilityHeliosStartDate)
                json.unavailabilityHeliosEndDate = new moment.utc(json.unavailabilityHeliosEndDate)
                this.setState({ fields: json })
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.pes.title', json.message)
                })
            })
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
        this.setState({ isFormValid: this.validateDates().passes() })
    }

    validateDates = () => {
        const { unavailabilityHeliosStartDate, unavailabilityHeliosEndDate } = this.state.fields
        Validator.register('dateOrder',
            () => moment(this.state.fields.unavailabilityHeliosStartDate).isSameOrBefore(this.state.fields.unavailabilityHeliosEndDate),
            this.context.t('api-gateway:form.validation.date_begin_before_date_end'))
        const validation = new Validator({ unavailabilityHeliosStartDate, unavailabilityHeliosEndDate },
            { unavailabilityHeliosStartDate: 'required|date|dateOrder', unavailabilityHeliosEndDate: 'required|date' })
        return validation
    }
    updateDateValidation = () => {
        const validation = this.validateDates()
        validation.passes()
        this.setState({ dateValidation: validation.errors.first('unavailabilityHeliosStartDate') || '' })
    }
    submitForm = (event) => {
        event.preventDefault()
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const data = JSON.stringify(this.state.fields)
        const headers = { 'Content-Type': 'application/json' }
        _fetchWithAuthzHandling({ url: '/api/pes/admin', method: 'PATCH', body: data, headers: headers, context: this.context })
            .then(checkStatus)
            .then(() => _addNotification(notifications.admin.moduleUpdated))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.admin.instance.title', text))
            })
    }
    render() {
        const { t } = this.context

        return (
            <Page title={t('admin.modules.pes.module_settings.title')}>
                <Segment>
                    <Form onSubmit={this.submitForm}>
                        <FieldInline htmlFor='heliosAvailable' label={t('admin.modules.pes.module_settings.heliosAvailable')}>
                            <Checkbox id="heliosAvailable"
                                toggle checked={this.state.fields.heliosAvailable}
                                onChange={this.handleCheckboxChange} />
                        </FieldInline>
                        <FieldInline htmlFor='unavailabilityHelios' label={t('admin.modules.pes.module_settings.unavailabilityHelios')}>
                            <Form.Group style={{ marginBottom: 0, flexDirection: 'column' }} className='test'>
                                <div style={{ display: 'flex', flexDirection: 'row' }}>
                                    <label htmlFor='unavailabilityHeliosStartDate' style={{ marginRight: '0.5em' }}>
                                        {t('api-gateway:form.from')}
                                    </label>
                                    <InputDatetime id='unavailabilityHeliosStartDate'
                                        onBlur={this.updateDateValidation}
                                        value={this.state.fields.unavailabilityHeliosStartDate}
                                        onChange={date => this.handleFieldChange('unavailabilityHeliosStartDate', date)} />
                                    <label htmlFor='unavailabilityHeliosEndDate' style={{ marginLeft: '1em', marginRight: '0.5em' }}>
                                        {t('api-gateway:form.to')}
                                    </label>
                                    <InputDatetime id='unavailabilityHeliosEndDate'
                                        onBlur={this.updateDateValidation}
                                        value={this.state.fields.unavailabilityHeliosEndDate}
                                        onChange={date => this.handleFieldChange('unavailabilityHeliosEndDate', date)} />
                                </div>
                                {this.state.dateValidation && (
                                    <div style={{ display: 'flex', flexDirection: 'row' }}>
                                        <Label color='red' pointing>{this.state.dateValidation}</Label>
                                    </div>
                                )}
                            </Form.Group>
                        </FieldInline>
                        <FieldInline htmlFor='alertMessageDisplayed' label={t('admin.modules.pes.module_settings.alertMessageDisplayed')}>
                            <Checkbox id='alertMessageDisplayed'
                                toggle checked={this.state.fields.alertMessageDisplayed}
                                onChange={this.handleCheckboxChange} />
                        </FieldInline>
                        <FieldInline htmlFor='alertMessage' label={t('admin.modules.pes.module_settings.alertMessage')}>
                            <InputTextControlled component={TextArea}
                                id='alertMessage'
                                maxLength={250}
                                placeholder={`${t('admin.modules.pes.module_settings.alertMessage')}...`}
                                value={this.state.fields.alertMessage || ''}
                                onChange={this.handleFieldChange} />
                        </FieldInline>
                        <div style={{ textAlign: 'right' }}>
                            <Button basic primary disabled={!this.state.isFormValid} style={{ marginTop: '2em' }} type='submit'>
                                {t('api-gateway:form.update')}
                            </Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(PesModuleParams)