import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Form, Button, Segment, Label, Icon, Checkbox } from 'semantic-ui-react'
import Validator from 'validatorjs'
import moment from 'moment'

import InputValidation from '../../_components/InputValidation'
import InputDatetime from '../../_components/InputDatetime'
import { notifications } from '../../_util/Notifications'
import { Field } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class PesModuleParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        isFormValid: true,
        dateValidation: '',
        fields: {
            heliosAvailable: true,
            unavailabilityHeliosStartDate: '',
            unavailabilityHeliosEndDate: ''
        }
    }
    validationRules = {
        unavailabilityHeliosStartDate: 'required|date|dateOrder',
        unavailabilityHeliosEndDate: 'required|date',
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/pes/admin' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                json.unavailabilityHeliosStartDate = new moment.utc(json.unavailabilityHeliosStartDate)
                json.unavailabilityHeliosEndDate = new moment.utc(json.unavailabilityHeliosEndDate)
                this.setState({ fields: json })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.instance.title', json.message)
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
        const data = JSON.stringify(this.state.fields)
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: '/api/pes/admin', method: 'PATCH', body: data, headers: headers, context: this.context })
            .then(checkStatus)
            .then(() => this.context._addNotification(notifications.admin.moduleUpdated))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.admin.instance.title', text))
            })
    }
    render() {
        const { t } = this.context
      
        return (
            <Segment>
                <h1>{t('admin.modules.pes.module_settings.title')}</h1>
                <Form onSubmit={this.submitForm}>
                    
                    
                    <Field htmlFor='heliosAvailable' label={t('admin.modules.pes.module_settings.heliosAvailable')}>
                        <Checkbox id="heliosAvailable"
                            toggle checked={this.state.fields.heliosAvailable}
                            onChange={this.handleCheckboxChange} />
                    </Field>
                    <Field htmlFor='unavailabilityHelios' label={t('admin.modules.pes.module_settings.unavailabilityHelios')}>
                        <Form.Group style={{ marginBottom: 0, flexDirection: 'column' }} className='test'>
                            <div style={{ display: 'flex', flexDirection: 'row' }}>
                                <label htmlFor='unavailabilityHeliosStartDate' style={{ marginRight: '0.5em' }}>{t('api-gateway:form.from')}</label>
                                <InputDatetime id='unavailabilityHeliosStartDate'
                                    onBlur={this.updateDateValidation}
                                    value={this.state.fields.unavailabilityHeliosStartDate}
                                    onChange={date => this.handleFieldChange('unavailabilityHeliosStartDate', date)} />
                                <label htmlFor='unavailabilityHeliosEndDate' style={{ marginLeft: '1em', marginRight: '0.5em' }}>{t('api-gateway:form.to')}</label>
                                <InputDatetime id='unavailabilityHeliosEndDate'
                                    onBlur={this.updateDateValidation}
                                    value={this.state.fields.unavailabilityHeliosEndDate}
                                    onChange={date => this.handleFieldChange('unavailabilityHeliosEndDate', date)} />
                            </div>
                            {this.state.dateValidation &&
                                <div style={{ display: 'flex', flexDirection: 'row' }}>
                                    <Label basic color='red' pointing>{this.state.dateValidation}</Label>
                                </div>}
                        </Form.Group>
                    </Field>
                    <Button disabled={!this.state.isFormValid} style={{ marginTop: '2em' }} primary type='submit'>{t('api-gateway:form.update')}</Button>
                </Form>
            </Segment>
        )
    }
}

export default translate(['pes', 'api-gateway'])(PesModuleParams)