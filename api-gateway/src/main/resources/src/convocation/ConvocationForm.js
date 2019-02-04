import React, { Component } from 'react'
import { translate } from 'react-i18next'
import { Segment, Form, TextArea, Grid, Button, Modal } from 'semantic-ui-react'
import PropTypes from 'prop-types'
import debounce from 'debounce'
import moment from 'moment'
import Validator from 'validatorjs'

import { getLocalAuthoritySlug, checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'
import history from '../_util/history'

import { withAuthContext } from '../Auth'

import ChipsList from '../_components/ChipsList'
import { Page, FormField, InputTextControlled, ValidationPopup } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import QuestionsForm from './QuestionsForm'
import RecipientForm from './RecipientForm'
import Breadcrumb from '../_components/Breadcrumb'

class ConvocationForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}
	state = {
	    errorTypePointing: false,
	    modalRecipentsOpened: false,
	    modalGuestOpened: false,
	    fields: {
	        uuid: '',
	        meetingDate: '',
	        hour: '',
	        assemblyType: '',
	        location: '',
	        subject: '',
	        comment: '',
	        convocationAttachment: null,
	        customeProcuration: null,
	        questions: [],
	        recipients: [],
	        guests: []
	    },
	    assemblyTypes: [],
	    isFormValid: false,
	    allFormErrors: []
	}
	componentDidMount() {
	    this.validateForm(null)
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    _fetchWithAuthzHandling({url: '/api/convocation/assembly-type/all', method: 'GET'})
	        .then(checkStatus)
	        .then(response => response.json())
	        .then(json => this.setState({assemblyTypes: json.map(item => { return {key: item.uuid, text: item.name, uuid: item.uuid, value: { 'uuid':item.uuid}}})}))
	        .catch(response => {
	            response.json().then(json => {
	                _addNotification(notifications.defaultError, 'notifications.title', json.message)
	            })
	        })
	}
	validationRules = {
	    meetingDate: ['required', 'date'],
	    hour: ['required', 'regex:/^[0-9]{2}[:][0-9]{2}$/'],
	    assemblyType: 'required',
	    location: ['required'],
	    subject: 'required|max:500',
	}
	submit = () => {
	    const { _fetchWithAuthzHandling, _addNotification } = this.context
	    const localAuthoritySlug = getLocalAuthoritySlug()

	    const parameters = this.state.fields
	    delete parameters.uuid
	    delete parameters.convocationAttachment
	    delete parameters.customeProcuration
	    parameters['meetingDate'] = moment(`${parameters['meetingDate'].format('YYYY-MM-DD')} ${parameters['hour']}`, 'YYYY-MM-DD HH:mm').format('YYYY-MM-DDTHH:mm:ss')
	    const headers = { 'Content-Type': 'application/json' }
	    _fetchWithAuthzHandling({url: '/api/convocation', method: 'POST', body: JSON.stringify(parameters), context: this.props.authContext, headers: headers})
	        .then(checkStatus)
	        .then(() => {
	            _addNotification(this.state.fields.uuid ? notifications.admin.assemblyTypeUpdated: notifications.convocation.sent)
	            history.push(`/${localAuthoritySlug}/convocation/liste-envoyees`)
	        })
	}
	validateForm = debounce(() => {
	    const { t } = this.context
	    const data = {
	        meetingDate: this.state.fields.meetingDate,
	        hour: this.state.fields.hour,
	        assemblyType: this.state.fields.assemblyType,
	        location: this.state.fields.location,
	        subject: this.state.fields.subject,
	    }

	    const attributeNames = {
	        meetingDate: t('convocation.fields.date'),
	        hour: t('convocation.fields.hour'),
	        assemblyType: 'this.state.fields.assemblyType',
	        location: t('convocation.fields.assembly_place'),
	        subject: t('convocation.fields.object'),
	    }
	    const validationRules = this.validationRules
	    //add validation format file

	    const validation = new Validator(data, validationRules)
	    validation.setAttributeNames(attributeNames)
	    const isFormValid = validation.passes()
	    const allFormErrors = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, allFormErrors })
	})
	extractFieldNameFromId = (str) => str.split('_').slice(-1)[0]
	handleFieldChange = (field, value, callback) => {
	    //To COMPLETE
	    //Set set for thid field
	    field = this.extractFieldNameFromId(field)
	    if(field === 'assemblyType') {
	        const { _fetchWithAuthzHandling, _addNotification } = this.context
	        _fetchWithAuthzHandling({url: `/api/convocation/assembly-type/${value.uuid}`, method: 'GET'})
	            .then(checkStatus)
	            .then(response => response.json())
	            .then(json => {
	                const fields = this.state.fields
	                fields['recipients'] = json.recipients
	                fields['location'] = json.location
	                this.setState({fields})
	            })
	            .catch(response => {
	                response.json().then(json => {
	                    _addNotification(notifications.defaultError, 'notifications.title', json.message)
	                })
	            })
	    }
	    const fields = this.state.fields
	    fields[field] = value
	    this.setState({ fields: fields }, () => {
	        this.validateForm()
	        if (callback) callback()
	    })
	}
	closeModal = () => {
	    this.setState({modalGuestOpened: false, modalRecipentsOpened: false})
	}
	addUsers = (selectedUser, field) => {
	    const fields = this.state.fields
	    fields[field] = selectedUser
	    this.setState({fields})
	    this.closeModal()
	}
	deleteRecipients = () => {
	    const fields = this.state.fields
	    fields['recipients'] = []
	    this.setState({fields})
	}
	updateQuestions = (questions) => {
	    const fields = this.state.fields
	    fields['questions'] = questions
	    this.setState({fields})
	}
	render() {
	    const { t } = this.context

	    const submissionButton =
            <Button type='submit' primary basic disabled={!this.state.isFormValid }>
                {t('api-gateway:form.send')}
            </Button>
	    const localAuthoritySlug = getLocalAuthoritySlug()
	    return (
	        <Page>
	            <Breadcrumb
	                data={[
	                    {title: t('api-gateway:breadcrumb.home'), url: `/${localAuthoritySlug}`},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation')},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation_creation')}
	                ]}
	            />
	            <Segment>
	                <Form onSubmit={this.submit}>
	                    <Grid>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_meetingDate`}
	                                label={t('convocation.fields.date')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_meetingDate`}
	                                    value={this.state.fields.meetingDate}
	                                    type='date'
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.meetingDate}
	                                    fieldName={t('convocation.fields.date')}
	                                    placeholder={t('convocation.fields.date_placeholder')}
	                                    isValidDate={(current) => current.isAfter(new moment())}
	                                    ariaRequired={true}/>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_hour`}
	                                label={t('convocation.fields.hour')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_hour`}
	                                    type='time'
	                                    dropdown={true}
	                                    placeholder={t('convocation.fields.hour_placeholder')}
	                                    ariaRequired={true}
	                                    validationRule={this.validationRules.hour}
	                                    value={this.state.fields.hour}
	                                    fieldName={t('convocation.fields.hour')}
	                                    onChange={this.handleFieldChange}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_assemblyType`}
	                                label={t('convocation.fields.assembly_type')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_assemblyType`}
	                                    type='dropdown'
	                                    validationRule={this.validationRules.assemblyType}
	                                    fieldName={t('convocation.fields.assembly_type')}
	                                    ariaRequired={true}
	                                    options={this.state.assemblyTypes}
	                                    value={this.state.fields.assemblyType}
	                                    onChange={this.handleFieldChange}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlForm={`${this.state.fields.uuid}_location`}
	                                label={t('convocation.fields.assembly_place')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    validationRule={this.validationRules.location}
	                                    id={`${this.state.fields.uuid}_location`}
	                                    fieldName={t('convocation.fields.assembly_place')}
	                                    onChange={this.handleFieldChange}
	                                    value={this.state.fields.location}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column computer='16'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_subject`}
	                                label={t('convocation.fields.object')}
                        			required={true}>
	                                <InputValidation id={`${this.state.fields.uuid}_subject`}
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    ariaRequired={true}
	                                    validationRule={this.validationRules.subject}
	                                    fieldName={t('convocation.fields.object')}
	                                    placeholder={t('convocation.fields.object_placeholder')}
	                                    value={this.state.fields.subject}
	                                    onChange={this.handleFieldChange}/>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column computer='16'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_comment`}
	                                label={t('convocation.fields.comment')}>
	                                <InputTextControlled component={TextArea}
	                                    id={`${this.state.fields.uuid}_comment`}
	                                    maxLength={250}
	                                    style={{ minHeight: '3em' }}
	                                    placeholder={`${t('convocation.fields.comment')}...`}
	                                    value={this.state.fields.comment}
	                                    onChange={this.handleFieldChange} />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='16'>
	                            <Grid>
	                                <Grid.Column computer='16'>
	                                    <FormField htmlFor={`${this.state.fields.uuid}_recipient`}
	                                        label={t('convocation.fields.recipient')}>
	                                        <Grid>
	                                            <Grid.Column computer='8'>
	                                                <Modal open={this.state.modalRecipentsOpened} trigger={<Button
	                                                    onClick={() => this.setState({modalRecipentsOpened: true})}
	                                                    type='button'
	                                                    disabled={!this.state.fields.assemblyType.uuid}
	                                                    id={`${this.state.fields.uuid}_recipient`}
	                                                    compact basic primary>{t('convocation.new.add_recipients')}
	                                                </Button>}>
	                                                    <RecipientForm
	                                                        onCloseModal={this.closeModal}
	                                                        onAdded={(selectedUser) => this.addUsers(selectedUser, 'recipients')}
	                                                        selectedUser={this.state.fields.recipients}
	                                                        uuid={this.state.fields.assemblyType.uuid}>
	                                                    </RecipientForm>
	                                                </Modal>
	                                            </Grid.Column>
	                                            <Grid.Column computer='8'>
	                                                <Button
	                                                    type='button'
	                                                    id={`${this.state.fields.uuid}_deleteRecipient`}
	                                                    onClick={this.deleteRecipients}
	                                                    compact basic color='red'>{t('convocation.new.delete_all_recipients')}
	                                                </Button>
	                                            </Grid.Column>
	                                        </Grid>
	                                    </FormField>
	                                    <ChipsList
	                                        list={this.state.fields.recipients}
	                                        labelText='email'
	                                        removable={false}
	                                        viewMoreText={t('convocation.new.view_more_recipients', {number: this.state.fields.recipients.length})}
	                                        viewLessText={t('convocation.new.view_less_recipients')}/>
	                                </Grid.Column>
	                            </Grid>

	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='16'>
	                            <QuestionsForm
	                                editable={true}
	                                questions={this.state.fields.questions}
	                                onUpdateQuestions={this.updateQuestions}
	                                uuid={this.state.fields.uuid}/>
	                        </Grid.Column>
	                    </Grid>
	                    <div className='footerForm'>
	                        <Button type="button" style={{ marginRight: '1em' }} onClick={e => this.deteleDraft(e)} basic color='red'>
	                            {t('api-gateway:form.cancel')}
	                        </Button>

	                        {this.state.allFormErrors.length > 0 &&
                                <ValidationPopup errorList={this.state.allFormErrors}>
                                    {submissionButton}
                                </ValidationPopup>
	                        }
	                        {this.state.allFormErrors.length === 0 && submissionButton}
	                    </div>
	                </Form>
	            </Segment>
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(ConvocationForm))