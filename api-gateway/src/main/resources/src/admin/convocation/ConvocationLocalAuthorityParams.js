import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Form, Button, Grid, Radio, Dropdown, Input } from 'semantic-ui-react'
import Validator from 'validatorjs'
import accepts from 'attr-accept'

import { withAuthContext } from '../../Auth'

import { getLocalAuthoritySlug } from '../../_util/utils'
import { acceptFileDocumentConvocation } from '../../_util/constants'
import { notifications } from '../../_util/Notifications'
import ConvocationService from '../../_util/convocation-service'

import { Page, FormField, ValidationPopup, InputFile, File, LinkFile } from '../../_components/UI'
import TextEditor from '../../_components/TextEditor'
import Breadcrumb from '../../_components/Breadcrumb'
import DraggablePosition from '../../_components/DraggablePosition'

class ConvocationLocalAuthorityParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    validationRules = {
        residentThreshold: 'required',
        epci: 'required'
    }
    state = {
        notificationMails: [],
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
        isFormValid: false,
        formErrors: []
    }
    componentDidMount = async() => {
        this._convocationService = new ConvocationService()

        const localAuthorityResponse = await this._convocationService.getConfForLocalAuthority(this.props.authContext)
        const notificationMailsResponse = await this._convocationService.getNotificationMails(this.props.authContext)
        const notificationMails = notificationMailsResponse.map(notification => { return {text:notification.subject, value:notification.notificationType, uuid: notification.uuid} })
        const placeholdersListResponse = await this._convocationService.getNoficiationsPlaceholders(this.props.authContext)
        this.setState({fields: localAuthorityResponse, notificationMails: notificationMails, placeholdersList: placeholdersListResponse})

    }
    validateForm = () => {
	    const { t } = this.context
	    const data = {
            residentThreshold: this.state.fields.residentThreshold,
            epci: this.state.fields.epci
	    }
	    const attributeNames = {
            residentThreshold: t('api-gateway:admin.convocation.fields.residents_threshold'),
            epci: t('convocation.admin.modules.convocation.local_authority_settings.epci')
	    }
	    const validationRules = this.validationRules

	    const validation = new Validator(data, validationRules)
	    validation.setAttributeNames(attributeNames)
	    const isFormValid = validation.passes()
	    const formErrors = Object.values(validation.errors.all()).map(errors => errors[0])
	    this.setState({ isFormValid, formErrors })
    }

    handleChangeRadio = (e, value, field) => {
        const fields = this.state.fields
        fields[field] = value === 'true'
        this.setState({fields}, this.validateForm())
    }
	handleChangeDeltaPosition = (position) => {
	    const { fields } = this.state
	    fields.stampPosition = position
	    this.setState({ fields }, this.validateForm())
	}

    handleProcurationChange = (file, acceptType) => {
        if(file && this.acceptsFile(file, acceptType)) {
            const fields = this.state.fields
            fields.procuration = file
            this.setState({ fields }, this.validateForm)

        }
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
    submitNotificationMail = async (e) => {
        const { _addNotification } = this.context
        e.preventDefault()
        const notificationMailResponse = await this._convocationService.saveDetailNotificationMail(this.props.authContext, this.state.notificationMessage)
        this.setState({notificationMessage: notificationMailResponse})
        _addNotification(notifications.admin.notificationMailUpdated)
    }
    render() {
        const { t } = this.context
        const { notificationMessage, isFormValid, fields, notificationMails, formErrors, isSet, placeholdersList } = this.state
        const localAuthoritySlug = getLocalAuthoritySlug()
        const submissionButton =
			<Button type='submit' primary basic disabled={!isFormValid}>
			    {t('api-gateway:form.update')}
			</Button>
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
                                        onChange={(e, {value}) => this.handleChangeRadio(e, value, 'residentThreshold')}
                                    ></Radio>
                                    <Radio
                                        label={t('api-gateway:no')}
                                        name='residentThreshold'
                                        value='false'
                                        checked={fields.residentThreshold === false}
                                        onChange={(e, {value}) => this.handleChangeRadio(e, value, 'residentThreshold')}
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
                                        onChange={(e, {value}) => this.handleChangeRadio(e, value, 'epci')}
                                    ></Radio>
                                    <Radio
                                        label={t('api-gateway:no')}
                                        name='epci'
                                        value='false'
                                        checked={fields.epci === false}
                                        onChange={(e, {value}) => this.handleChangeRadio(e, value, 'epci')}
                                    ></Radio>
                                </FormField>
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
                                                handleChange={this.handleChangeDeltaPosition} />
                                        </Grid.Row>
                                    </Grid>
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
                        </Grid>
                        <div className='footerForm'>
                            {formErrors.length > 0 &&
								<ValidationPopup errorList={formErrors}>
								    {submissionButton}
								</ValidationPopup>
	                        }
	                        {formErrors.length === 0 && submissionButton}
                        </div>
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
