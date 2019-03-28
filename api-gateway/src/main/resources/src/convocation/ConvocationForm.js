import React, { Component, Fragment } from 'react'
import { translate } from 'react-i18next'
import { Segment, Form, TextArea, Grid, Button, Dropdown } from 'semantic-ui-react'
import PropTypes from 'prop-types'
import debounce from 'debounce'
import moment from 'moment'
import Validator from 'validatorjs'
import accepts from 'attr-accept'

import { getLocalAuthoritySlug, checkStatus, extractFieldNameFromId } from '../_util/utils'
import { notifications } from '../_util/Notifications'
import history from '../_util/history'
import { acceptFileDocumentConvocation } from '../_util/constants'
import ConvocationService from '../_util/convocation-service'
import AdminService from '../_util/admin-service'

import { withAuthContext } from '../Auth'

import ConfirmModal from '../_components/ConfirmModal'
import { Page, FormField, InputTextControlled, ValidationPopup, File, InputFile, LinkFile } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import QuestionsForm from './QuestionsForm'
import AddRecipientsGuestsFormFragment from './_components/AddRecipientsGuestsFormFragment'
import Breadcrumb from '../_components/Breadcrumb'

class ConvocationForm extends Component {
	static contextTypes = {
	    t: PropTypes.func,
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}
	state = {
	    errorTypePointing: false,
	    modalGuestOpened: false,
	    fields: {
	        uuid: '',
	        meetingDate: '',
	        hour: '',
	        assemblyType: '',
	        location: '',
	        subject: '',
	        comment: '',
	        questions: [],
	        recipients: [],
	        guests: [],
	        file: null,
	        annexes: [],
	        annexesTags: [],
	        sending: false,
	        defaultProcuration: null,
	        customProcuration: null,
	        useProcuration: false,
	        groupUuid: null
	    },
	    delayTooShort: false,
	    delay: null,
	    showAllAnnexes: false,
	    assemblyTypes: [],
	    isFormValid: false,
	    allFormErrors: [],
	    localAuthority: {
	        epci: false
	    },
	    groupList: [],
	    tagsList: []
	}
	componentDidMount = async () => {
	    this.validateForm(null)
	    this._convocationService = new ConvocationService()
	    this._adminService = new AdminService()

	    const localAuthorityResponse = await this._convocationService.getConfForLocalAuthority(this.props.authContext)
	    const groupResponse = await this._adminService.getGroups(this.props.authContext, 'CONVOCATION_DEPOSIT')

	    const assemblyTypesResponse = (await this._convocationService.getAllAssemblyType(this.props.authContext)).map(item => {
	        return {key: item.uuid, text: item.name, uuid: item.uuid, value: item.uuid}
	    })
	    const tagsListResponse = (await this._convocationService.getAllTags(this.props.authContext)).map(item => {
	        return {key: item.uuid, text: item.name, uuid: item.uuid, value: item.uuid}
	    })
	    this.setState({tagsList: tagsListResponse, assemblyTypes: assemblyTypesResponse, localAuthority: localAuthorityResponse, groupList: groupResponse})
	}
	validationRules = {
	    meetingDate: ['required', 'date'],
	    hour: ['required', 'regex:/^[0-9]{2}[:][0-9]{2}$/'],
	    assemblyType: 'required',
	    location: ['required'],
	    subject: 'required|max:500',
	    file: ['required'],
	    groupUuid: ['required']
	}
	checkDelay = () => {
	    if(this.state.fields.meetingDate && this.state.delay) {
	        const { fields, delay } = this.state
	        const now = moment()
	        const diff = fields.meetingDate.diff(now, 'days')
	        if(diff > delay) {
	            this.setState({delayTooShort: false})
	        } else {
	            this.setState({delayTooShort: true})
	        }
	    }
	}
	submit = async() => {
	    if(this.state.isFormValid) {
	        const { _addNotification } = this.context
	        const localAuthoritySlug = getLocalAuthoritySlug()

	        const parameters = Object.assign({}, this.state.fields)
	        delete parameters.uuid
	        delete parameters.file
	        delete parameters.annexes
	        parameters.guests.forEach((guest) => {
			   guest.guest = true
			   parameters.recipients.push(guest)
	        })
	        delete parameters.guests
	        delete parameters.annexesTags
	        parameters['assemblyType'] = {uuid: parameters.assemblyType}
	        parameters['groupUuid'] = parameters['groupUuid'] !== 'all' ? parameters['groupUuid'] : null
	        parameters['meetingDate'] = moment(`${parameters['meetingDate'].format('YYYY-MM-DD')} ${parameters['hour']}`, 'YYYY-MM-DD HH:mm').format('YYYY-MM-DDTHH:mm:ss')

	        const convocationResponse = await this._convocationService.createConvocation(this.props.authContext, parameters)
	        this.setState({sending: true})
	        _addNotification(notifications.convocation.created)
	        const data = new FormData()
	        data.append('file', this.state.fields.file)
	        if(this.state.fields.annexes.length > 0) {
	            this.state.fields.annexes.forEach(annexe => {
	                data.append('annexes', annexe)
	            })
	        }
	        if(this.state.fields.customProcuration) {
	            data.append('procuration', this.state.fields.customProcuration)
	        }

	        if(this.state.fields.annexesTags.length > 0) {
	            const annexesTags = this.state.fields.annexesTags.map((annexe) => {
	                const tags = annexe.tags.join('/')
	                return `${annexe.fileName}/${tags}`
	            })
	            data.append('tags', annexesTags)
	        }
	        await this._convocationService.updateDocumentsConvocation(this.props.authContext, convocationResponse.uuid, data, false)
	        _addNotification(notifications.convocation.sent)
	        history.push(`/${localAuthoritySlug}/convocation/liste-envoyees`)
	    }
	}
	validateForm = debounce(() => {
	    const { t } = this.context
	    const data = {
	        meetingDate: this.state.fields.meetingDate,
	        hour: this.state.fields.hour,
	        assemblyType: this.state.fields.assemblyType,
	        location: this.state.fields.location,
	        subject: this.state.fields.subject,
	        file: this.state.fields.file,
	        groupUuid: this.state.fields.groupUuid
	    }

	    const attributeNames = {
	        meetingDate: t('convocation.fields.date'),
	        hour: t('convocation.fields.hour'),
	        assemblyType: t('convocation.fields.assembly_type'),
	        location: t('convocation.fields.assembly_place'),
	        subject: t('convocation.fields.object'),
	        file: t('convocation.fields.convocation_document'),
	        groupUuid: t('convocation.fields.visible_by')
	    }
	    const validationRules = this.validationRules
	    //add validation format file

	    const validation = new Validator(data, validationRules)
	    validation.setAttributeNames(attributeNames)
	    const isFormValid = validation.passes()
	    const allFormErrors = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, allFormErrors })

	    this.checkDelay()
	})
	handleFieldChange = (field, value, callback) => {
	    //Set set for thid field
	    field = extractFieldNameFromId(field)
	    if(field === 'assemblyType') {
	        const { _fetchWithAuthzHandling, _addNotification } = this.context
	        _fetchWithAuthzHandling({url: `/api/convocation/assembly-type/${value}`, method: 'GET'})
	            .then(checkStatus)
	            .then(response => response.json())
	            .then(json => {
	                const fields = this.state.fields
	                fields.recipients = json.recipients
	                fields.location = json.location
	                fields.useProcuration = json.useProcuration
	                this.setState({fields, delay: json.delay})
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

	updateUser = (fields) => {
	    this.setState({fields})
	}
	updateQuestions = (questions) => {
	    const fields = this.state.fields
	    fields['questions'] = questions
	    this.setState({fields})
	}

	acceptsFile = (file, acceptType) => {
	    const { _addNotification, t } = this.context
	    if(accepts(file, acceptType)) {
	        return true
	    } else {
	        _addNotification(notifications.defaultError, 'notifications.convocation.title', t('api-gateway:form.validation.bad_extension_file', {name: file.name}))
	        return false
	    }
	}

	handleFileChange = (field, file, acceptType) => {
	    if(this.acceptsFile(file, acceptType)) {
	        const fields = this.state.fields

	        if (file) {
	            fields[field] = file
	            this.setState({ fields }, this.validateForm)
	        }
	    }
	}


	handleAnnexeChange = (file, acceptType) => {
	    const fields = this.state.fields
	    for(let i = 0; i < file.length; i++) {
	        if(this.acceptsFile(file[i], acceptType)) {
	            fields['annexes'].push(file[i])
	        }
	    }

	    this.setState({ fields })
	}

	deleteFile = (field) => {
	    const fields = this.state.fields
	    fields[field] = null
	    this.setState({ fields }, this.validateForm)
	}

	deleteAnnexe = (index, annexeName) => {
	    const fields = this.state.fields
	    fields['annexes'].splice(index, 1)
	    const idAnnexeTags = fields['annexesTags'].findIndex((annexes) => {
	        return annexes.fileName === annexeName
	    })
	    fields['annexesTags'].splice(idAnnexeTags, 1)
	    this.setState({ fields })
	}

	goBack = () => {
	    history.goBack()
	}

	handleTagChange = (fileName, tags) => {
	    const { fields } = this.state

	    const idAnnexeTags = fields['annexesTags'].findIndex((annexe) => {
	        return annexe.fileName === fileName
	    })
	    if(idAnnexeTags === -1) {
	        fields['annexesTags'].push({fileName: fileName, tags: tags})
	    } else {
	        fields['annexesTags'][idAnnexeTags]['tags'] = tags
	    }
	    this.setState({ fields })
	}

	render() {
	    const { t } = this.context
	    const { tagsList } = this.state

	    const submissionButton = this.state.delayTooShort ?
	        <ConfirmModal onConfirm={this.submit} text={t('convocation.new.delayTooShort', {number: this.state.delay})}>
	            <Button type='button' basic color={'orange'} disabled={!this.state.isFormValid}>{t('api-gateway:form.send')}</Button>
	        </ConfirmModal> :
	        <Button type='submit' primary basic disabled={!this.state.isFormValid}>
	            {t('api-gateway:form.send')}
	        </Button>
	    const localAuthoritySlug = getLocalAuthoritySlug()

	    const annexesToDisplay = !this.state.showAllAnnexes && this.state.fields.annexes.length > 3 ? this.state.fields.annexes.slice(0,3) : this.state.fields.annexes
	    const annexes = annexesToDisplay.map((annexe, index)=> {
	        return (
	            <File
	                key={`${this.state.fields.uuid}_${annexe.name}`}
	                attachment={{ filename: annexe.name }}
	                onDelete={() => this.deleteAnnexe(index, annexe.name)}
	                extraContent={<Dropdown
	                    placeholder={t('convocation.fields.pick_tag')}
	                    fluid
	                    multiple
	                    search
	                    selection
	                    options={tagsList}
	                    onChange={(e, { value }) => this.handleTagChange(annexe.name, value)}/>}/>
	        )
	    })

	    const groupOptions = this.state.groupList.map(group =>
	        ({ key: group.uuid, value: group.uuid, text: group.name })
	    )

	    groupOptions.unshift({key: 'all', value: 'all', text: t('api-gateway:form.all')})
	    return (
	        <Page>
	            <Breadcrumb
	                data={[
	                    {title: t('api-gateway:breadcrumb.home'), url: `/${localAuthoritySlug}`},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation')},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation_creation')}
	                ]}
	            />
	            <Form onSubmit={this.submit} className='mt-14'>
	                {/* Global information (date, assembly type, Object, comment, ...) */}
	            	<Segment>
	                    <Grid>
	                        <Grid.Column mobile='16' computer='6'>
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
	                        <Grid.Column mobile='16' computer='6'>
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
	                        <Grid.Column mobile={16} tablet={16} computer={4}>
	                            <FormField htmlFor={`${this.state.fields.uuid}_groupUuid`} label={t('convocation.fields.visible_by')}required>
	                                <Dropdown id={`${this.state.fields.uuid}_groupUuid`}
	                                    value={this.state.fields.groupUuid}
	                                    onChange={(event, { id, value }) => this.handleFieldChange(id, value)}
	                                    options={groupOptions}
	                                    fluid selection />
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
	                    </Grid>
	                </Segment>
	                {/* Questions */}
	                <Segment>
	                    <Grid>
	                        <Grid.Column mobile='16' computer='16'>
	                            {this.state.initialRank !== null && (
	                                <QuestionsForm
	                                    editable={true}
	                                    questions={this.state.fields.questions}
	                                    initialRank={this.state.initialRank}
	                                    onUpdateQuestions={this.updateQuestions}
	                                />
	                            )}
	                        </Grid.Column>
	                    </Grid>
	                </Segment>
	                {/* Documents */}
	                <Segment>
	                    <Grid>
	                        {this.state.fields.useProcuration && (
	                            <Fragment>
	                                <Grid.Column mobile='16' computer='8'>
	                                    <FormField htmlFor={`${this.state.fields.uuid}_procuration`}
	                                        label={t('convocation.fields.default_procuration')}>
	                                        <LinkFile url='/api/convocation/local-authority/procuration' text={t('convocation.new.display')}/>
	                                    </FormField>
	                                </Grid.Column>
	                                <Grid.Column mobile='16' computer='8'>
	                                    <FormField htmlFor={`${this.state.fields.uuid}_customProcuration`}
	                                        label={t('convocation.fields.custom_procuration')}>
	                                        <InputFile labelClassName="primary" htmlFor={`${this.state.fields.uuid}_customProcuration`}
	                                            label={`${t('api-gateway:form.add_a_file')}`}>
	                                            <input type="file"
	                                                id={`${this.state.fields.uuid}_customProcuration`}
	                                                accept={acceptFileDocumentConvocation}
	                                                onChange={(e) => this.handleFileChange('customProcuration', e.target.files[0], acceptFileDocumentConvocation)}
	                                                style={{ display: 'none' }}/>
	                                        </InputFile>
	                                    </FormField>
	                                    {this.state.fields.customProcuration && (
	                                        <File attachment={{ filename: this.state.fields.customProcuration.name }} onDelete={() => this.deleteFile('customProcuration')} />
	                                    )}
	                                </Grid.Column>
	                            </Fragment>
	                        )}
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_file`}
	                                label={t('convocation.fields.convocation_document')} required={true}>
	                                <InputValidation id={`${this.state.fields.uuid}_file`}
	                                    labelClassName="primary"
	                                    type='file'
	                                    ariaRequired={true}
	                                    accept={acceptFileDocumentConvocation}
	                                    onChange={(file, accept) => this.handleFileChange('file', file, accept)}
	                                    value={this.state.fields.file}
	                                    label={`${t('api-gateway:form.add_a_file')}`}
	                                    fieldName={t('convocation.fields._file')} />
	                            </FormField>
	                            {this.state.fields.file && (
	                                <File attachment={{ filename: this.state.fields.file.name }} onDelete={() => this.deleteFile('file')} />
	                            )}
	                        </Grid.Column>
	                        <Grid.Column mobile='16' computer='8'>
	                            <FormField htmlFor={`${this.state.fields.uuid}_annexes`}
	                                label={t('convocation.fields.annexes')}>
	                                <InputFile labelClassName="primary" htmlFor={`${this.state.fields.uuid}_annexes`}
	                                    label={`${t('api-gateway:form.add_a_file')}`}>
	                                    <input type="file"
	                                        id={`${this.state.fields.uuid}_annexes`}
	                                        accept={acceptFileDocumentConvocation}
	                                        multiple
	                                        onChange={(e) => this.handleAnnexeChange(e.target.files, acceptFileDocumentConvocation)}
	                                        style={{ display: 'none' }}/>
	                                </InputFile>
	                            </FormField>
	                            {this.state.fields.annexes.length > 0 && (
	                                <div>
	                                    {annexes}
	                                    {this.state.fields.annexes.length > 3 && (
	                                        <div className='mt-15'>
	                                            <Button onClick={() => this.setState({showAllAnnexes: !this.state.showAllAnnexes})} className="link" primary compact basic>
	                                                {this.state.showAllAnnexes && (
	                                                    <span>{t('convocation.new.show_less_annexes')}</span>
	                                                )}
	                                                {!this.state.showAllAnnexes && (
	                                                    <span>{t('convocation.new.show_all_annexes', {number: this.state.fields.annexes.length})}</span>
	                                                )}
	                                            </Button>
	                                        </div>
	                                    )}

	                                </div>
	                            )}
	                        </Grid.Column>
	                    </Grid>
	                </Segment>
	                {/* Recipients */}
	                <AddRecipientsGuestsFormFragment epci={this.state.localAuthority.epci} fields={this.state.fields} updateUser={this.updateUser} disabledRecipientsEdit={!this.state.fields.assemblyType}/>
	                <div className='footerForm'>
	                    <Button type="button" style={{ marginRight: '1em' }} onClick={e => this.goBack()} basic color='red'>
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
	        </Page>
	    )
	}
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(ConvocationForm))