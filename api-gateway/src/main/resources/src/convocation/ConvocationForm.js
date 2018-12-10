import React, { Component } from 'react'
import { translate } from 'react-i18next'
import { Segment, Form, TextArea, Grid, Button, Modal } from 'semantic-ui-react'
import PropTypes from 'prop-types'
import debounce from 'debounce'
import moment from 'moment'
import Validator from 'validatorjs'

import { Page, FormField, InputTextControlled, InputFile, ValidationPopup } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import QuestionsForm from './QuestionsForm'
import ReceivesForm from './ReceivesForm'

class ConvocationForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	state = {
	    errorTypePointing: false,
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
	        date: 'this.state.fields.date',
	        hour: 'this.state.fields.hour',
	        /*assemblyType: 'this.state.fields.assemblyType',*/
	        assemblyPlace: 'this.state.fields.assemblyPlace',
	        objet: 'this.state.fields.objet',
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
                {t('api-gateway:form.submit')}
            </Button>

	    return (
	        <Page>
	            <Segment>
	                <Form onSubmit={this.submit}>
	                    <Grid>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_date`} label='Date de la séance' required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_date`}
	                                    value={this.state.fields.date}
	                                    type='date'
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.date}
	                                    fieldName='this.state.fields.date'
	                                    placeholder='jj/mm/aaaa'
	                                    isValidDate={(current) => current.isAfter(new moment())}
	                                    ariaRequired={true}/>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_hour`} label='Heure de la séance' required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_hour`}
	                                    type='time'
	                                    dropdown={true}
	                                    placeholder='hh:mm'
	                                    ariaRequired={true}
	                                    validationRule={this.validationRules.hour}
	                                    value={this.state.fields.hour}
	                                    fieldName='this.state.fields.hour'
	                                    onChange={this.handleFieldChange}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_assemblyType`} label='Type d assemblée' required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_assemblyType`}
	                                    type='dropdown'
	                                    validationRule={this.validationRules.assemblyType}
	                                    fieldName='this.state.fields.assemblyType'
	                                    ariaRequired={true}
	                                    options={null}
	                                    value={this.state.fields.assemblyType}
	                                    onChange={this.handleFieldChange}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlForm={`${this.state.fields.uuid}_assemblyPlace`} label='Lieu de l assemblée' required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    validationRule={this.validationRules.assemblyPlace}
	                                    id={`${this.state.fields.uuid}_assemblyPlace`}
	                                    fieldName='this.state.fields.assemblyPlace'
	                                    onChange={this.handleFieldChange}
	                                    value={this.state.fields.assemblyPlace}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column computer='16'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_objet`}
	                                label='Objet'
                        			required={true}>
	                                <InputValidation id={`${this.state.fields.uuid}_objet`}
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    ariaRequired={true}
	                                    validationRule={this.validationRules.objet}
	                                    fieldName='this.state.fields.assemblyPlace'
	                                    placeholder='L objet de la objet'
	                                    value={this.state.fields.objet}
	                                    onChange={this.handleFieldChange}/>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column computer='16'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_comment`}
	                                label='Commentaires'>
	                                <InputTextControlled component={TextArea}
	                                    id={`${this.state.fields.uuid}_comment`}
	                                    maxLength={250}
	                                    style={{ minHeight: '3em' }}
	                                    placeholder='Commentaires ...'
	                                    value={this.state.fields.comment}
	                                    onChange={this.handleFieldChange} />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='16'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_receives`}
	                                label='Destinataire' required={true}>
	                                <Grid>
	                                    <Grid.Column computer='8'>
	                                        <Modal trigger={<Button
	                                            type='button'
	                                            id={`${this.state.fields.uuid}_receives`}
	                                            compact basic primary>Ajouter un/des destinataires
	                                        </Button>}>
	                                            <ReceivesForm></ReceivesForm>
	                                        </Modal>
	                                    </Grid.Column>
	                                    <Grid.Column computer='8'>
	                                        <Button
	                                            type='button'
	                                            id={`${this.state.fields.uuid}_deleteReceives`}
	                                            compact basic color='red'>Supprimer tous les destinataires
	                                        </Button>
	                                    </Grid.Column>
	                                </Grid>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_procuration`}
	                                label='Modèle de procuration par défaut'>
	                                <Button className="link" primary compact basic>Visualiser</Button>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_customProcuration`}
	                                label='Modèle de procuration personnalisé'>
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
	                                label='Documents de la convocation' required={true}>
	                                <InputValidation id={`${this.state.fields.uuid}_document`}
	                                    labelClassName="primary"
	                                    type='file'
	                                    ariaRequired={true}
	                                    accept={acceptFileDocument}
	                                    value={this.state.fields.convocationAttachment}
	                                    label={`${t('api-gateway:form.add_a_file')}`}
	                                    fieldName='document' />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_annexes`}
	                                label='Annexe(s)'>
	                                <InputValidation id={`${this.state.fields.uuid}_annexes`}
	                                    labelClassName="primary"
	                                    type='file'
	                                    accept={acceptAnnexeFile}
	                                    value={this.state.fields.convocationAttachment}
	                                    label={`${t('api-gateway:form.add_a_file')}`}
	                                    fieldName='annexes' />
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
								Annuler
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
export default translate(['api-gateway'])(ConvocationForm)
//export default translate(['convocation', 'api-gateway'])(ConvocationForm)