import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Grid, Radio, Button, Modal } from 'semantic-ui-react'
import Validator from 'validatorjs'
import debounce from 'debounce'

import InputValidation from '../../_components/InputValidation'
import ReceivesForm from '../../convocation/ReceivesForm'

import { Page, FormField, ValidationPopup} from '../../_components/UI'
import ChipsList from '../../_components/ChipsList'

class AssemblyTypeConfig extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	}
	validationRules = {
	    type: 'required',
	    place: 'required',
	    convocationDelay: ['required', 'regex:/^[0-9]+$/'],
	    reminderTime: ['required', 'regex:/^[0-9]+$/']
	}
	state = {
	    formErrors: [],
	    isFormValid: false,
	    errorTypePointing: false,
	    fields: {
	        uuid: '',
	        type: '',
	        place: '',
	        convocationDelay: '',
	        reminderTime: '0',
	        procuration: false,
	        status: false,
	        receives: []
	    },
	    modalOpened: false
	}
	submit = () => {
	    console.log('SUBMIT')
	}
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
	validateForm = debounce(() => {
	    const { t } = this.context
	    const data = {
	        type: this.state.fields.type,
	        place: this.state.fields.place,
	        convocationDelay: this.state.fields.convocationDelay,
	        reminderTime: this.state.fields.reminderTime
	    }
	    const attributeNames = {
	        type: t('convocation.admin.modules.convocation.assembly_type_config.type'),
	        place: t('convocation.admin.modules.convocation.assembly_type_config.place'),
	        convocationDelay: t('convocation.admin.modules.convocation.assembly_type_config.convocation_delay'),
	        reminderTime: t('convocation.admin.modules.convocation.assembly_type_config.reminder_time')
	    }
	    const validationRules = this.validationRules

	    const validation = new Validator(data, validationRules)
	    validation.setAttributeNames(attributeNames)
	    const isFormValid = validation.passes()
	    const formErrors = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, formErrors })
	}, 500)
	closeModal = () => {
	    this.setState({modalOpened: false})
	}
	addReceives = (selectedUser) => {
	    const fields = this.state.fields
	    fields['receives'] = fields['receives'].concat(selectedUser)
	    this.setState({fields})
	    this.closeModal()
	}
	removeReceives = (index) => {
	    const fields = this.state.fields
	    fields['receives'].splice(index, 1)
	    this.setState({fields})
	}
	render () {
	    const { t } = this.context
	    const submissionButton =
			<Button type='submit' primary basic disabled={!this.state.isFormValid }>
			    {t('api-gateway:form.send')}
			</Button>
	    return (
	        <Page>
	            <Segment>
	                <Form onSubmit={this.submit}>
	                    <Grid>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_type`}
	                                label={t('convocation.admin.modules.convocation.assembly_type_config.type')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_type`}
	                                    value={this.state.fields.type}
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.type}
	                                    fieldName={t('convocation.admin.modules.convocation.assembly_type_config.type')}
	                                    ariaRequired={true}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_place`}
	                                label={t('convocation.admin.modules.convocation.assembly_type_config.place')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_place`}
	                                    value={this.state.fields.place}
	                                    onChange={this.handleFieldChange}
	                                    validationRule={this.validationRules.place}
	                                    fieldName={t('convocation.admin.modules.convocation.assembly_type_config.place')}
	                                    ariaRequired={true}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_convocationDelay`}
	                                label={t('convocation.admin.modules.convocation.assembly_type_config.convocation_delay')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_convocationDelay`}
	                                    validationRule={this.validationRules.convocationDelay}
	                                    value={this.state.fields.convocationDelay}
	                                    type='number'
	                                    onChange={this.handleFieldChange}
	                                    fieldName={t('convocation.admin.modules.convocation.assembly_type_config.convocation_delay')}
	                                    ariaRequired={true}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_reminderTime`}
	                                label={t('convocation.admin.modules.convocation.assembly_type_config.reminder_time')} required={true}>
	                                <InputValidation
	                                    errorTypePointing={this.state.errorTypePointing}
	                                    id={`${this.state.fields.uuid}_reminderTime`}
	                                    validationRule={this.validationRules.reminderTime}
	                                    value={this.state.fields.reminderTime}
	                                    type='number'
	                                    onChange={this.handleFieldChange}
	                                    fieldName={t('convocation.admin.modules.convocation.assembly_type_config.reminder_time')}
	                                    ariaRequired={true}
	                                />
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_status`}
	                                label={t('convocation.admin.modules.convocation.assembly_type_config.status')}>
	                                <Radio toggle className='secondary'
	                                    value={this.state.fields.status}/>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column mobile="16" computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_procuration`}
	                                label={t('convocation.admin.modules.convocation.assembly_type_config.procuration')}>
	                                <Radio toggle className='secondary'
	                                    value={this.state.fields.procuration}/>
	                            </FormField>
	                        </Grid.Column>
	                        <Grid.Column computer='16'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_receives`}
	                                label={t('convocation.admin.modules.convocation.assembly_type_config.receives')} required={true}>
	                                <Modal open={this.state.modalOpened} trigger={<Button
	                                    	onClick={() => this.setState({modalOpened: true})}
	                                    	type='button'
	                                    	id={`${this.state.fields.uuid}_receives`}
	                                    	compact basic primary>{t('convocation.new.add_receives')}
	                                    </Button>}>
	                                    <ReceivesForm
	                                        onCloseModal={this.closeModal}
	                                        onAdded={(selectedUser) => this.addReceives(selectedUser)}>
	                                    </ReceivesForm>
	                                </Modal>
	                            </FormField>
	                            <ChipsList
	                                list={this.state.fields.receives}
	                                labelText='email'
	                                removable={true}
	                                onRemoveChip={this.removeReceives}
	                                viewMoreText={t('convocation.new.view_more_receives', {number: this.state.fields.receives.length})}
	                                viewLessText={t('convocation.new.view_less_receives')}/>
	                        </Grid.Column>
	                    </Grid>
	                    <div className='footerForm'>
	                        <Button type="button" style={{ marginRight: '1em' }} onClick={e => this.deteleDraft(e)} basic color='red'>
	                            {t('api-gateway:form.cancel')}
	                        </Button>

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

export default translate(['convocation', 'api-gateway'])(AssemblyTypeConfig)