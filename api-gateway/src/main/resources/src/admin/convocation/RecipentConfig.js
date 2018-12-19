import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import Validator from 'validatorjs'
import debounce from 'debounce'
import moment from 'moment'
import { Segment, Button, Form, Grid, Message } from 'semantic-ui-react'

import { notifications } from '../../_util/Notifications'
import { checkStatus, getLocalAuthoritySlug } from '../../_util/utils'
import history from '../../_util/history'
import { Page, ValidationPopup, FormField } from '../../_components/UI'
import InputValidation from '../../_components/InputValidation'

class RecipentConfig extends Component {
	static contextTypes = {
	    csrfToken: PropTypes.string,
	    csrfTokenHeaderName: PropTypes.string,
	    t: PropTypes.func,
	    _addNotification: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func
	}
	validationRules = {
	    firstname: 'required',
	    lastname: 'required',
	    email: ['required', 'regex:/^[a-zA-Z0-9.!#$%&\'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/'],
	    phoneNumber: ['required', 'regex:/^(0|\\+33)[1-9]([-. ]?[0-9]{2}){4}$/']
	}
	state = {
	    formErrors: [],
	    errorTypePointing: false,
	    isFormValid: false,
	    fields: {
	        uuid: '',
	        firstname: '',
	        lastname: '',
	        email: '',
	        phoneNumber: '',
	        active: true,
	        assemblyTypes: [],
	        inactivityDate: null
	    }
	}
	componentDidMount() {
	    const { _fetchWithAuthzHandling } = this.context
	    this.validateForm()
	    const uuid = this.props.uuid
	    if(uuid !== '') {
	        _fetchWithAuthzHandling({ url: '/api/convocation/recipient/' + uuid })
	            .then(checkStatus)
	            .then(response => response.json())
	            .then(json => {
	                const fields = this.state.fields
	                Object.keys(fields).forEach(function (key) {
	                    fields[key] = json[key]
	                })
	                this.setState({fields})
	            })
	            .catch(response => {
	                //TO DO ERROR
	            })
	    }
	}
	extractFieldNameFromId = (str) => str.split('_').slice(-1)[0]
	handleFieldChange = (field, value, callback) => {
	    //Set set for thid field
	    field = this.extractFieldNameFromId(field)
	    const fields = this.state.fields
	    fields[field] = value
	    this.setState({ fields: fields }, () => {
	        this.validateForm()
	        if (callback) callback()
	    })
	}
	submitForm = () => {
	    const { t, _fetchWithAuthzHandling, _addNotification } = this.context
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    const parameters = Object.assign({}, this.state.fields)
	    delete parameters.uuid
	    delete parameters.active
	    delete parameters.uuid
	    delete parameters.assemblyTypes
	    delete parameters.inactivityDate

	    if(this.state.fields.uuid) {
	        const headers = { 'Content-Type': 'application/json' }
	        _fetchWithAuthzHandling({url: `/api/convocation/recipient/${this.state.fields.uuid}`, method: 'PUT', headers: headers, body: JSON.stringify(parameters), context: this.context})
	            .then(checkStatus)
	            .then(() => {
	                history.push(`/${localAuthoritySlug}/admin/convocation/destinataire/liste-destinataires`)
	                _addNotification(notifications.admin.recipientUpdated)
	            })
	            .catch(response => {
	                response.json().then((json) => {
	                    _addNotification(notifications.defaultError, 'api-gateway:notifications.admin.title', t(`convocation.${json.message}`))
	                })
	            })
	    } else {
	        const headers = { 'Content-Type': 'application/x-www-form-urlencoded' }
	        _fetchWithAuthzHandling({url: '/api/convocation/recipient', method: 'POST', headers: headers, query: parameters, context: this.context})
	            .then(checkStatus)
	            .then(() => {
	                history.push(`/${localAuthoritySlug}/admin/convocation/destinataire/liste-destinataires`)
	                _addNotification(notifications.admin.recipientCreated)
	            })
	            .catch(response => {
	                response.json().then((json) => {
	                    _addNotification(notifications.defaultError, 'api-gateway:notifications.admin.title', t(`convocation.${json.message}`))
	                })
	            })
	    }
	}
	cancel = () => {
	    history.goBack()
	}
	validateForm = debounce(() => {
	    const { t } = this.context
	    const data = {
	        firstname: this.state.fields.firstname,
	        lastname: this.state.fields.lastname,
	        email: this.state.fields.email,
	        phoneNumber: this.state.fields.phoneNumber
	    }
	    const attributeNames = {
	        firstname: t('convocation.admin.modules.convocation.recipient.firstname'),
	        lastname: t('convocation.admin.modules.convocation.recipient.lastname'),
	        email: t('convocation.admin.modules.convocation.recipient.email'),
	        phoneNumber: t('convocation.admin.modules.convocation.recipient.number')
	    }
	    const validationRules = this.validationRules

	    const validation = new Validator(data, validationRules)
	    validation.setAttributeNames(attributeNames)
	    const isFormValid = validation.passes()
	    const formErrors = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, formErrors })
	}, 500)

	render () {
	    const { t } = this.context
	    const submissionButton =
			<Button type='submit' primary basic disabled={!this.state.isFormValid }>
			    {t('api-gateway:form.send')}
			</Button>
	    return (
	        <Page>
	            {!this.state.fields.active && (
	                <Message warning>
	                    <Message.Header style={{ marginBottom: '0.5em'}}>{t('convocation.admin.modules.convocation.recipient_config.inactive_recipient_title')}</Message.Header>
	                    <p>{t('convocation.admin.modules.convocation.recipient_config.inactive_recipient_content', {date: moment(this.state.fields.inactivityDate).format('DD/MM/YYYY')})}</p>
	                </Message>
	            )}
	            <Segment>
	                <Form onSubmit={this.submitForm}>
	                    <Grid>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_firstname`}
	                                label={t('convocation.admin.modules.convocation.recipient_config.firstname')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_firstname`}
	                                    value={this.state.fields.firstname}
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.firstname}
	                                    fieldName={t('convocation.admin.modules.convocation.recipient_config.firstname')}
	                                    ariaRequired={true}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_lastname`}
	                                label={t('convocation.admin.modules.convocation.recipient_config.lastname')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_lastname`}
	                                    value={this.state.fields.lastname}
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.lastname}
	                                    fieldName={t('convocation.admin.modules.convocation.recipient_config.lastname')}
	                                    ariaRequired={true}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_email`}
	                                label={t('convocation.admin.modules.convocation.recipient_config.email')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_email`}
	                                    value={this.state.fields.email}
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.email}
	                                    fieldName={t('convocation.admin.modules.convocation.recipient_config.email')}
	                                    ariaRequired={true}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_phoneNumber`}
	                                label={t('convocation.admin.modules.convocation.recipient_config.phonenumber')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_phoneNumber`}
	                                    value={this.state.fields.phoneNumber}
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.phoneNumber}
	                                    fieldName={t('convocation.admin.modules.convocation.recipient_config.phonenumber')}
	                                    ariaRequired={true}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                    </Grid>
	                    <div className='footerForm'>
	                        {this.state.fields.uuid && (
	                            <Button type="button" style={{ marginRight: '1em' }} onClick={e => this.cancel(e)} basic color='red'>
	                                {t('api-gateway:form.cancel')}
	                            </Button>
	                        )}
	                        {this.state.formErrors.length > 0 &&
								<ValidationPopup errorList={this.state.formErrors}>
								    {submissionButton}
								</ValidationPopup>
	                        }
	                        {this.state.formErrors.length === 0 && submissionButton}
	                    </div>
	                </Form>
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(RecipentConfig)