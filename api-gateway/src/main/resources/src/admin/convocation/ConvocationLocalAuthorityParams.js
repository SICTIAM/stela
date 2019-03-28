import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Button, Grid, Radio, Dropdown, Input } from 'semantic-ui-react'
import Validator from 'validatorjs'
import accepts from 'attr-accept'

import { withAuthContext } from '../../Auth'

import { getLocalAuthoritySlug, extractFieldNameFromId } from '../../_util/utils'
import { acceptFileDocumentConvocation } from '../../_util/constants'
import { notifications } from '../../_util/Notifications'
import ConvocationService from '../../_util/convocation-service'

import { Page, FormField, ValidationPopup, InputFile, File, LinkFile } from '../../_components/UI'
import TextEditor from '../../_components/TextEditor'
import Breadcrumb from '../../_components/Breadcrumb'
import DraggablePosition from '../../_components/DraggablePosition'
import StelaTable from '../../_components/StelaTable'
import CreateEditTagsFragment from './_components/CreateEditTagsFragment'

class ConvocationLocalAuthorityParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    validationRules = {
        fields: {
            residentThreshold: 'required',
            epci: 'required'
        },
        tags: {
            name: ['required','max:32'],
	    	color: 'required'
        }

    }
    state = {
        notificationMails: [],
        editTagsOpened: false,
        tagsEdition: {
            uuid: '',
            name:'',
            color: '',
            icon: ''
        },
        isSet: true,
        notificationMessage: {
            notificationType: '',
            body: '',
            localAuthorityUuid: '',
            subject: '',
            uuid: ''
        },
        placeholdersList: [],
        fields: {
            uuid: '',
            residentThreshold: null,
            procuration: null,
            defaultProcuration: null,
            epci: false,
            stampPosition: {
                x: 10,
                y: 10
            },
        },
        tags: {
            name:'',
            color: '',
            icon: ''
        },
        isFormValid: {
            general: false,
            tags: false,
            tagsEdition: false
        },
        formErrors: {
            general: [],
            tags: [],
            tagsEdition: []
        },
        tagsList: []
    }
    componentDidMount = async() => {
        this._convocationService = new ConvocationService()
        this.validateForm('fields')
        this.validateForm('tags')
        this.validateForm('tagsEdition')
        const localAuthorityResponse = await this._convocationService.getConfForLocalAuthority(this.props.authContext)
        const notificationMailsResponse = await this._convocationService.getNotificationMails(this.props.authContext)
        const notificationMails = notificationMailsResponse.map(notification => { return {text:notification.subject, value:notification.notificationType, uuid: notification.uuid} })
        const placeholdersListResponse = await this._convocationService.getNoficiationsPlaceholders(this.props.authContext)
        const tagsListResponse = await this._convocationService.getAllTags(this.props.authContext)
        this.setState({fields: localAuthorityResponse, notificationMails: notificationMails, placeholdersList: placeholdersListResponse, tagsList: tagsListResponse})

    }
    validateForm = (form) => {
        const { t } = this.context

        const attributeNames = {
            fields: {
                residentThreshold: t('api-gateway:admin.convocation.fields.residents_threshold'),
                epci: t('convocation.admin.modules.convocation.local_authority_settings.epci')
            },
            tags: {
                name: t('convocation.admin.modules.convocation.local_authority_settings.tags.name'),
                color: t('convocation.admin.modules.convocation.local_authority_settings.tags.color')
            },
            tagsEdition: {
                name: t('convocation.admin.modules.convocation.local_authority_settings.tags.name'),
                color: t('convocation.admin.modules.convocation.local_authority_settings.tags.color')
            }
        }
        const validatorFields = {
            fields: {
                residentThreshold: this.state.fields.residentThreshold,
                epci: this.state.fields.epci
            },
            tags: {
                name: this.state.tags.name,
                color: this.state.tags.color
            },
            tagsEdition: {
                name: this.state.tags.name,
                color: this.state.tags.color
            },
        }
	    const data = validatorFields[form]
	    const validationRules = this.validationRules[form]

	    const validation = new Validator(data, validationRules)
        validation.setAttributeNames(attributeNames[form])
        let isFormValid = {}
	    isFormValid[form] = validation.passes()
        const { formErrors } = this.state
        formErrors[form] = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, formErrors })
    }

    handleChangeRadio = (e, fieldsObject, value, field) => {
        const fields = this.state[fieldsObject]
        fields[field] = value === 'true'
        this.setState({fields}, this.validateForm(fieldsObject))
    }
	handleChangeDeltaPosition = (fieldsObject, position) => {
	    const { fields } = this.state
	    fields.stampPosition = position
	    this.setState({ fields }, this.validateForm(fieldsObject))
	}

    handleProcurationChange = (file, acceptType) => {
        if(file && this.acceptsFile(file, acceptType)) {
            const fields = this.state.fields
            fields.procuration = file
            this.setState({ fields }, this.validateForm)

        }
    }

	handleFieldChange = (fieldsObject, field, value) => {
	    field = extractFieldNameFromId(field)
	    const fields = this.state[fieldsObject]
	    fields[field] = value
	    this.setState({[fieldsObject]: fields}, this.validateForm(fieldsObject))
	}

    acceptsFile = (file, acceptType) => {
	    const { _addNotification, t } = this.context
	    if(accepts(file, acceptType)) {
	        return true
	    } else {
	        _addNotification(notifications.defaultError, 'notifications.convocation.title', t('api-gateway:form.validation.badextension'))
	        return false
	    }
    }

    deleteFile = () => {
	    const fields = this.state.fields
	    fields.procuration = null
	    this.setState({ fields }, this.validateForm)
    }

    handleNotificationTypeChange = async (id, type) => {
        const detailsNotificationResponse = await this._convocationService.getDetailNotificationMail(this.props.authContext, type)
        this.setState({isSet: false}, () => {
            this.setState({notificationMessage: detailsNotificationResponse})
        })
    }
    handleNotificationChange = async (field, value) => {
        const { notificationMessage } = this.state
        notificationMessage[field] = value
        this.setState({notificationMessage, isSet: true})
    }

    submitGeneralForm = async (e) => {
        const { _addNotification } = this.context
        e.preventDefault()
        await this._convocationService.saveConfForLocalAuthority(this.props.authContext, this.state.fields)
        if(this.state.fields.procuration) {
            const data = new FormData()
            data.append('procuration', this.state.fields.procuration)
            await this._convocationService.saveDefaultProcuration(this.props.authContext, data)
            _addNotification(notifications.admin.moduleConvocationUpdated)
        } else {
            _addNotification(notifications.admin.moduleConvocationUpdated)
        }

        const localAthorityResponse = await this._convocationService.getConfForLocalAuthority(this.props.authContext)
        this.setState({fields: localAthorityResponse})
    }
	openEditTags = (prop, row) => {
	    this.setState({editTagsOpened: true, tagsEdition: Object.assign({}, row)})
	}
	submitTags = async (e) => {
	    const { _addNotification } = this.context
	    e.preventDefault()
	    await this._convocationService.saveTags(this.props.authContext, this.state.tags)
	    _addNotification(this.state.tags.uuid ? notifications.admin.convocationTagAdded : notifications.admin.convocationTagUpdated)

	    const tagsListResponse = await this._convocationService.getAllTags(this.props.authContext)
	    this.setState({tagsList: tagsListResponse, tags: {name: '', icon: '', color: ''}})
	}

	updateTags = async (e) => {
	    const { _addNotification } = this.context
	    e.preventDefault()
	    await this._convocationService.saveTags(this.props.authContext, this.state.tagsEdition)
	    _addNotification(this.state.tags.uuid ? notifications.admin.convocationTagAdded : notifications.admin.convocationTagUpdated)

	    const tagsListResponse = await this._convocationService.getAllTags(this.props.authContext)
	    this.setState({tagsList: tagsListResponse, tagsEdition: {uuid: '', name: '', icon: '', color: ''}, editTagsOpened: false})
	}

	deleteTage = async (e) => {
	    const { _addNotification } = this.context
	    e.preventDefault()

	    await this._convocationService.deleteTag(this.props.authContext, this.state.tagsEdition.uuid)
	    _addNotification(notifications.admin.convocationTagDeleted)

	    const tagsListResponse = await this._convocationService.getAllTags(this.props.authContext)
	    this.setState({tagsList: tagsListResponse, tagsEdition: {uuid: '', name: '', icon: '', color: ''}, editTagsOpened: false})
	}
    submitNotificationMail = async (e) => {
        const { _addNotification } = this.context
        e.preventDefault()
        const notificationMailResponse = await this._convocationService.saveDetailNotificationMail(this.props.authContext, this.state.notificationMessage)
        this.setState({notificationMessage: notificationMailResponse})
        _addNotification(notifications.admin.notificationMailUpdated)
    }
    render() {
        const { t } = this.context
        const { notificationMessage, isFormValid, fields, notificationMails, formErrors, isSet, placeholdersList, tagsList, editTagsOpened } = this.state
        const localAuthoritySlug = getLocalAuthoritySlug()
        const submissionButton = (text, objectFields) => {
            return (
                <Button type='submit' primary basic disabled={!isFormValid[objectFields]}>
			    	{text}
                </Button>
            )
        }
        const colorDisplay = (color) => <div style={{backgroundColor: color, height: '30px', width: '30px', border: '1px solid #5e5e5e', marginRight: '20px', borderRadius: '50%'}}>

        </div>
        const metaData = [
            { property: 'uuid', displayed: false },
            { property: 'name', displayed: true, searchable: true, sortable: true, displayName: t('convocation.admin.modules.convocation.local_authority_settings.tags.name') },
            { property: 'color', displayed: true, searchable: false, sortable: false, displayComponent: colorDisplay, displayName: t('convocation.admin.modules.convocation.local_authority_settings.tags.color') },
            { property: 'icon', displayed: false, searchable: false, sortable: false, displayName: t('convocation.admin.modules.convocation.local_authority_settings.tags.icon') },
        ]

        return (
            <Page>
                <Breadcrumb
	                data={[
	                    {title: t('api-gateway:breadcrumb.admin_home'), url: `/${localAuthoritySlug}/admin/ma-collectivite`},
	                    {title: t('api-gateway:breadcrumb.convocation.convocation')}
	                ]}
	            />
                <Segment>
                    <h2 className='secondary'>{t('convocation.admin.modules.convocation.local_authority_settings.general_informations')}</h2>
                    <Form onSubmit={this.submitGeneralForm}>
                        <Grid>
                            <Grid.Column mobile="16" computer='8'>
                                <FormField htmlFor='residentThreshold'
                                    label={t('api-gateway:admin.convocation.fields.residents_threshold')}>
                                    <Radio
                                        label={t('api-gateway:yes')}
                                        value='true'
                                        name='residentThreshold'
                                        checked={fields.residentThreshold === true}
                                        onChange={(e, {value}) => this.handleChangeRadio(e, 'fields', value, 'residentThreshold')}
                                    ></Radio>
                                    <Radio
                                        label={t('api-gateway:no')}
                                        name='residentThreshold'
                                        value='false'
                                        checked={fields.residentThreshold === false}
                                        onChange={(e, {value}) => this.handleChangeRadio(e, 'fields', value, 'residentThreshold')}
                                    ></Radio>
                                </FormField>
                            </Grid.Column>
                            <Grid.Column mobile="16" computer='8'>
                                <FormField htmlFor='epci'
                                    label={t('convocation.admin.modules.convocation.local_authority_settings.epci')}>
                                    <Radio
                                        label={t('api-gateway:yes')}
                                        value='true'
                                        name='epci'
                                        checked={fields.epci === true}
                                        onChange={(e, {value}) => this.handleChangeRadio(e, 'fields', value, 'epci')}
                                    ></Radio>
                                    <Radio
                                        label={t('api-gateway:no')}
                                        name='epci'
                                        value='false'
                                        checked={fields.epci === false}
                                        onChange={(e, {value}) => this.handleChangeRadio(e, 'fields', value, 'epci')}
                                    ></Radio>
                                </FormField>
                            </Grid.Column>
                            <Grid.Column mobile='16' computer='16'>
                                <FormField htmlFor={`${fields.uuid}_procuration`}
	                                label={t('convocation.fields.default_procuration')}>
                                    {this.state.fields.defaultProcuration && (
                                        <p><LinkFile url='/api/convocation/local-authority/procuration' text={t('convocation.new.display')}/></p>
                                    )}
	                                <InputFile labelClassName="primary" htmlFor={`${fields.uuid}_procuration`}
	                                    label={`${t('api-gateway:form.add_a_file')}`}>
	                                    <input type="file"
	                                        id={`${fields.uuid}_procuration`}
	                                        accept={acceptFileDocumentConvocation}
	                                        multiple
	                                        onChange={(e) => this.handleProcurationChange(e.target.files[0], acceptFileDocumentConvocation)}
	                                        style={{ display: 'none' }}/>
	                                </InputFile>
	                            </FormField>
                                {fields.procuration && (
	                                <File attachment={{ filename: fields.procuration.name }} onDelete={() => this.deleteFile()} />
	                            )}
                            </Grid.Column>
                            <Grid.Column mobile="16" computer='16'>
                                <FormField htmlFor="positionPad" label={t('api-gateway:stamp_pad.title')}>
                                    <Grid>
                                        <Grid.Row style={{display:'flex', justifyContent:'space-around', alignItems:'center'}}>
                                            <DraggablePosition
                                                label={t('convocation.stamp_pad.pad_label')}
                                                height={300}
                                                width={190}
                                                showPercents={true}
                                                labelColor='#000'
                                                position={fields.stampPosition}
                                                handleChange={(position) => this.handleChangeDeltaPosition('fields', position)} />
                                        </Grid.Row>
                                    </Grid>
                                </FormField>
                            </Grid.Column>
                        </Grid>
                        <div className='footerForm'>
                            {formErrors.general.length > 0 &&
								<ValidationPopup errorList={formErrors.general}>
								    {submissionButton(t('api-gateway:form.update'), 'fields')}
								</ValidationPopup>
	                        }
	                        {formErrors.general.length === 0 && submissionButton(t('api-gateway:form.update'), 'fields')}
                        </div>
                    </Form>
                </Segment>
                <Segment>
                    <h2 className='secondary'>{t('convocation.admin.modules.convocation.local_authority_settings.documents_tags')}</h2>
                    <Form onSubmit={this.submitTags}>
                        <Grid>
                            <CreateEditTagsFragment
                                handleFieldChange={this.handleFieldChange}
                                validationRules={this.validationRules}
                                fields={fields} tags={this.state.tags}
                                fieldsObject='tags'/>
                            <Grid.Column computer='16'>
                                <div className='footerForm'>
                                    {formErrors.tags.length > 0 &&
										<ValidationPopup errorList={formErrors.tags}>
										    {submissionButton(t('api-gateway:form.add'), 'tags')}
										</ValidationPopup>
                                    }
                                    {formErrors.tags.length === 0 && submissionButton(t('api-gateway:form.add'), 'tags')}
                                </div>
                            </Grid.Column>
                        </Grid>
                    </Form>
                    <Form onSubmit={this.updateTags}>
                        <Grid>
                            <Grid.Column mobile='16' computer='8'>
                                <StelaTable
                                    id={`${this.state.fields.uuid}_tagsList`}
                                    data={tagsList}
                                    header={true}
                                    click={true}
                                    onClick={(e, prop, row) => this.openEditTags(prop, row)}
                                    keyProperty="uuid"
                                    noDataMessage={t('convocation.admin.modules.convocation.local_authority_settings.tags.no_tag')}
                                    search={true}
                                    metaData={metaData}
                                />
                            </Grid.Column>
                            {editTagsOpened && (                                    <Grid.Column mobile='16' computer='8'>
                                <h3 className='secondary'>{t('convocation.admin.modules.convocation.local_authority_settings.tags.edit_tag')}</h3>
                                <CreateEditTagsFragment
                                    handleFieldChange={this.handleFieldChange}
                                    validationRules={this.validationRules}
                                    fields={fields}
                                    tags={this.state.tagsEdition}
                                    fieldsObject='tagsEdition'/>
                                <div className='footerForm'>
                                    <Button type='button' negative basic onClick={this.deleteTage}>
                                        {t('api-gateway:form.delete')}
                                    </Button>
                                    {formErrors.tagsEdition.length > 0 &&
										<ValidationPopup errorList={formErrors.tagsEdition}>
										    {submissionButton(t('api-gateway:form.update'), 'tagsEdition')}
										</ValidationPopup>
                                    }
                                    {formErrors.tagsEdition.length === 0 && submissionButton(t('api-gateway:form.update'), 'tagsEdition')}
                                </div>
                            </Grid.Column>
                            )}
                        </Grid>
                    </Form>
                </Segment>
                <Segment>
                    <h2 className='secondary'>{t('convocation.admin.modules.convocation.local_authority_settings.notificationMessage')}</h2>
                    <Form onSubmit={this.submitNotificationMail}>
                        <Grid>
                            <Grid.Column mobile='16' computer='16'>
                                <Grid>
                                    <Grid.Column mobile='16' computer='8'>
                                        <FormField htmlFor={`${fields.uuid}_notification_email`}
                                            label={t('convocation.fields.notification_email')}>
                                            <Dropdown id={`${fields.uuid}_notification_email`}
                                                onChange={(event, data) => {
                                                    this.handleNotificationTypeChange(`${fields.uuid}_notification_email`, data.value)
                                                }}
                                                options={notificationMails}
                                                value={notificationMessage.notificationType}
                                                fluid selection>
                                            </Dropdown>
                                        </FormField>
                                    </Grid.Column>
                                    {notificationMessage.notificationType && notificationMessage.body && (
                                        <Grid.Column mobile='16' computer='16'>
                                            <FormField htmlFor={`${this.props.uuid}_subject`}
	                                            label={t('convocation.admin.modules.convocation.local_authority_settings.subject')}>
                                                <Input
                                                    id={`${this.props.uuid}_subject`}
                                                    value={notificationMessage.subject}
                                                    onChange={(e) => { this.handleNotificationChange('subject', e.target.value) } }/>
                                            </FormField>
                                            <div className='noZIndex'>
                                                <FormField htmlFor={`${this.props.uuid}_body`}
	                                            label={t('convocation.admin.modules.convocation.local_authority_settings.body')}>
                                                    <TextEditor
                                                        placeholdersList={placeholdersList}
                                                        onChange={value => this.handleNotificationChange('body', value)}
                                                        isSet={isSet}
                                                        format='html'
                                                        text={notificationMessage.body} />
                                                </FormField>
                                            </div>
                                        </Grid.Column>
                                    )}
                                </Grid>
                            </Grid.Column>
                        </Grid>
                        <div className='footerForm'>
                            <Button type='submit' primary basic disabled={!notificationMessage.notificationType}>
                                {t('api-gateway:form.update')}
                            </Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['convocation', 'api-gateway'])(withAuthContext(ConvocationLocalAuthorityParams))
