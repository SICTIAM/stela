import React, { Component, Fragment } from 'react'
import { translate } from 'react-i18next'
import PropTypes from 'prop-types'
import { Button, Form, Grid, Confirm } from 'semantic-ui-react'
import debounce from 'debounce'
import Validator from 'validatorjs'

import { notifications } from '../../_util/Notifications'
import ConvocationService from '../../_util/convocation-service'

import { ValidationPopup, FormField } from '../../_components/UI'
import InputValidation from '../../_components/InputValidation'

import { withAuthContext } from '../../Auth'


class UserFormFragment extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _addNotification: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func
	}
	static propTypes = {
	    onSubmit: PropTypes.func,
	    onCancel: PropTypes.func,
	    fields: PropTypes.object,
	    preventParentSubmit: PropTypes.bool
	}
	static defaultProps = {
	    fields: null,
	    preventParentSubmit: false
	}
	validationRules = {
	    firstname: 'required',
	    lastname: 'required',
	    // eslint-disable-next-line no-useless-escape
	    email: ['required', 'regex:/^[a-zA-Z0-9.!#$%&\'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/'],
	    phoneNumber: ['regex:/^(0|\\+33)[1-9]([-. ]?[0-9]{2}){4}$/']
	}
	state = {
	    isConfirmModalOpen: false,
	    formErrors: [],
	    errorTypePointing: false,
	    isFormValid: false,
	    fields: {
	        uuid: null,
	        firstname: '',
	        lastname: '',
	        email: '',
	        phoneNumber: ''
	    }
	}
	componentDidMount = () => {
	    this._convocationService = new ConvocationService()
	    const { fields } = this.props
	    if(fields) {
	        this.setState({fields})
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
	closeConfirmModal = () => this.setState({ isConfirmModalOpen: false })
	confirm = () => {
	    this.forceSubmit()
	    this.closeConfirmModal()
	}
	createBodyParams = () => {
	    const parameters = Object.assign({}, this.state.fields)
	    delete parameters.uuid
	    delete parameters.active
	    delete parameters.assemblyTypes
	    delete parameters.inactivityDate

	    return JSON.stringify(parameters)
	}
	submitForm = async() => {
	    const { _addNotification } = this.context
	    const parameters = this.createBodyParams()
	    _addNotification(notifications.admin.email_validation_in_progress)
	    try {
	        const recipientResponse = await this._convocationService.saveRecipient(this.props.authContext, parameters, this.state.fields.uuid, false)
	        _addNotification(notifications.admin.email_validation_success)
	        _addNotification(this.state.fields.uuid ? notifications.admin.recipientUpdated : notifications.admin.recipientCreated)
	        this.props.onSubmit(recipientResponse)
	    } catch(error) {
	        if(error.status === 400) {
	            this.setState({ isConfirmModalOpen: true })
	        }
	    }
	}
	forceSubmit = async() => {
	    const { _addNotification } = this.context

	    const parameters = this.createBodyParams()
	    const recipientResponse = await this._convocationService.saveRecipient(this.props.authContext, parameters, this.state.fields.uuid, true)
	    _addNotification(this.state.fields.uuid ? notifications.admin.recipientUpdated : notifications.admin.recipientCreated)
	    this.props.onSubmit(recipientResponse)
	}
	render() {
	    const { t } = this.context
	    const submissionButton =
			<Button type={this.props.preventParentSubmit ? 'button' :'submit'} onClick={this.props.preventParentSubmit ? this.submitForm : null} primary basic disabled={!this.state.isFormValid}>
			    {t('api-gateway:form.send')}
			</Button>
	    return (
	        <Fragment>
	            <Confirm
	                open={this.state.isConfirmModalOpen}
	                content={t('api-gateway:notifications.convocation.admin.email_validation.error')}
	                confirmButton={t('api-gateway:form.confirm')}
	                cancelButton={t('api-gateway:form.cancel')}
	                onCancel={this.closeConfirmModal}
	                onConfirm={this.confirm} />
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
	                                label={t('convocation.admin.modules.convocation.recipient_config.phonenumber')}>
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
	                            <Button type="button" style={{ marginRight: '1em' }} onClick={e => this.props.onCancel(e)} basic color='red'>
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
	        </Fragment>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(UserFormFragment))