import React, { Component } from 'react'
import { translate } from 'react-i18next'
import { Segment, Form, TextArea, Grid, Button, Modal } from 'semantic-ui-react'
import PropTypes from 'prop-types'
import debounce from 'debounce'
import moment from 'moment'
import Validator from 'validatorjs'

import { getLocalAuthoritySlug } from '../_util/utils'

import { Page, FormField, InputTextControlled, InputFile, ValidationPopup } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import QuestionsForm from './QuestionsForm'
import RecipientForm from './RecipientForm'
import Breadcrumb from '../_components/Breadcrumb'

class ConvocationForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	state = {
	    errorTypePointing: false,
	    modalOpened: false,
	    fields: {
	        uuid: 'test',
	        date: '',
	        hour: '',
	        assemblyType: '',
	        assemblyPlace: '',
	        objet: '',
	        comment: '',
	        convocationAttachment: null,
	        customeProcuration: null,
	        questions: []
	    },
	    isFormValid: false,
	    allFormErrors: []
	}
	componentDidMount() {
	    this.validateForm(null)
	}
	validationRules = {
	    date: ['required', 'date'],
	    hour: ['required', 'regex:/^[0-9]{2}[:][0-9]{2}$/'],
	    /*assemblyType: 'required',*/
	    assemblyPlace: ['required'],
	    objet: 'required|max:500',
	    /*convocationAttachment: ['required']*/

	    /*number: ['required', 'max:15', 'regex:/^[A-Z0-9_]+$/'],
	    objet: 'required|max:500',
	    acteAttachment: 'required|acteAttachmentType'*/
	}
	submit = () => {
	    console.log('SUBMIT')
	}
	validateForm = debounce(() => {
	    const { t } = this.context
	    const data = {
	        date: this.state.fields.date,
	        hour: this.state.fields.hour,
	        /*assemblyType: this.state.fields.assemblyType,*/
	        assemblyPlace: this.state.fields.assemblyPlace,
	        objet: this.state.fields.objet,
	        /*annexes: this.state.fields.annexes,
	        convocationAttachment: this.state.fields.convocationAttachment*/
	    }

	    const attributeNames = {
	        date: t('convocation.fields.date'),
	        hour: t('convocation.fields.hour'),
	        /*assemblyType: 'this.state.fields.assemblyType',*/
	        assemblyPlace: t('convocation.fields.assembly_place'),
	        objet: t('convocation.fields.object'),
	        /*annexes: 'this.state.fields.annexes',
	        convocationAttachment: 'this.state.fields.convocationAttachment'*/
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
	    const fields = this.state.fields
	    fields[field] = value
	    this.setState({ fields: fields }, () => {
	        this.validateForm()
	        if (callback) callback()
	    })
	}
	closeModal = () => {
	    this.setState({modalOpened: false})
	}
	updateQuestions = (questions) => {
	    const fields = this.state.fields
	    fields['questions'] = questions
	    this.setState({fields})
	}
	render() {
	    const { t } = this.context

	    /* Accept File ? */
	    const acceptFileDocument = '.xml, .pdf, .jpg, .png'
	    const acceptCustomProcuration = '.xml, .pdf, .jpg, .png'
	    const acceptAnnexeFile = '.xml, .pdf, .jpg, .png'
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
	                            <FormField htmlFor={`${this.state.fields.uuid}_date`}
	                                label={t('convocation.fields.date')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_date`}
	                                    value={this.state.fields.date}
	                                    type='date'
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.date}
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
	                                    options={null}
	                                    value={this.state.fields.assemblyType}
	                                    onChange={this.handleFieldChange}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlForm={`${this.state.fields.uuid}_assemblyPlace`}
	                                label={t('convocation.fields.assembly_place')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    validationRule={this.validationRules.assemblyPlace}
	                                    id={`${this.state.fields.uuid}_assemblyPlace`}
	                                    fieldName={t('convocation.fields.assembly_place')}
	                                    onChange={this.handleFieldChange}
	                                    value={this.state.fields.assemblyPlace}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column computer='16'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_objet`}
	                                label={t('convocation.fields.object')}
                        			required={true}>
	                                <InputValidation id={`${this.state.fields.uuid}_objet`}
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    ariaRequired={true}
	                                    validationRule={this.validationRules.objet}
	                                    fieldName={t('convocation.fields.object')}
	                                    placeholder={t('convocation.fields.object_placeholder')}
	                                    value={this.state.fields.objet}
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
	                            <FormField htmlFor={`${this.state.fields.uuid}_recipient`}
	                                label={t('convocation.fields.recipient')} required={true}>
	                                <Grid>
	                                    <Grid.Column computer='8'>
	                                        <Modal open={this.state.modalOpened} trigger={<Button
	                                            onClick={() => this.setState({modalOpened: true})}
	                                            type='button'
	                                            id={`${this.state.fields.uuid}_recipient`}
	                                            compact basic primary>{t('convocation.new.add_recipients')}
	                                        </Button>}>
	                                            <RecipientForm onCloseModal={this.closeModal}></RecipientForm>
	                                        </Modal>
	                                    </Grid.Column>
	                                    <Grid.Column computer='8'>
	                                        <Button
	                                            type='button'
	                                            id={`${this.state.fields.uuid}_deleteRecipient`}
	                                            compact basic color='red'>{t('convocation.new.delete_all_recipients')}
	                                        </Button>
	                                    </Grid.Column>
	                                </Grid>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_procuration`}
	                                label={t('convocation.fields.default_procuration')}>
	                                <Button className="link" primary compact basic>{t('convocation.new.display')}</Button>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_customProcuration`}
	                                label={t('convocation.fields.custom_procuration')}>
	                                <InputFile labelClassName="primary"
	                                    htmlFor={`${this.state.fields.uuid}_customProcuration`}
	                                    label={`${t('api-gateway:form.add_a_file')}`}>
	                                    <input type="file" id={`${this.state.fields.uuid}_customProcuration`} accept={acceptCustomProcuration}
	                                        style={{ display: 'none' }} />
	                                </InputFile>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_document`}
	                                label={t('convocation.fields.convocation_document')} required={true}>
	                                <InputValidation id={`${this.state.fields.uuid}_document`}
	                                    labelClassName="primary"
	                                    type='file'
	                                    ariaRequired={true}
	                                    accept={acceptFileDocument}
	                                    value={this.state.fields.convocationAttachment}
	                                    label={`${t('api-gateway:form.add_a_file')}`}
	                                    fieldName={t('convocation.fields.convocation_document')} />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_annexes`}
	                                label={t('convocation.fields.annexes')}>
	                                <InputValidation id={`${this.state.fields.uuid}_annexes`}
	                                    labelClassName="primary"
	                                    type='file'
	                                    accept={acceptAnnexeFile}
	                                    value={this.state.fields.convocationAttachment}
	                                    label={`${t('api-gateway:form.add_a_file')}`}
	                                    fieldName={t('convocation.fields.annexes')} />
	                            </FormField>
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
export default translate(['convocation', 'api-gateway'])(ConvocationForm)