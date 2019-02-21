import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import {Button, Form, Checkbox, Icon, Dropdown, Grid} from 'semantic-ui-react'
import Validator from 'validatorjs'
import debounce from 'debounce'
import moment from 'moment'
import accepts from 'attr-accept'

import { FormField, ValidationPopup, DragAndDropFile, InputFile } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import { notifications } from '../_util/Notifications'
import history from '../_util/history'
import {checkStatus, handleFieldCheckboxChange, getLocalAuthoritySlug, bytesToSize, sortAlphabetically, sum} from '../_util/utils'
import { natures, materialCodeBudgetaire } from '../_util/constants'
import { withAuthContext } from '../Auth'
import ActeService from '../_util/acte-service'
import AdminService from '../_util/admin-service'
import FormFiles from '../_components/FormFiles'

class NewActeForm extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    static defaultProps = {
        mode: 'ACTE',
        shouldUnmount: true,
        nature: '',
        decision: ''
    }
    state = {
        fields: {
            uuid: null,
            number: '',
            decision: '',
            nature: '',
            groupUuid: null,
            code: '',
            objet: '',
            acteAttachment: null,
            annexes: [],
            public: false,
            publicWebsite: false,
            draft: {
                uuid: ''
            }
        },
        depositFields: {
            publicField: false,
            publicWebsiteField: false
        },
        attachmentTypes: [],
        groups: [],
        codesMatieres: [],
        isFormValid: false,
        formErrors: [],
        formFilesErrors: [],
        acteFetched: false
    }
    validationRules = {
        number: ['required', 'max:15', 'regex:/^[A-Z0-9_]+$/'],
        objet: 'required|max:500',
        nature: 'required',
        code: 'required',
        decision: ['required', 'date'],
        acteAttachment: 'required|acteAttachmentType'
    }

    componentDidMount = async () => {
        this._acteService = new ActeService()
        this._adminService = new AdminService()
        const {mode, draftUuid, uuid} = this.props

        let acteResponse = {}
        if (!draftUuid && !uuid) {
            acteResponse = await this._acteService.createNewActeDraftByActeMode(mode, this.context)
        } else if (draftUuid && uuid){
            acteResponse = await this._acteService.getActeByDraftUuidAndUuid(draftUuid, uuid, this.context)
        }

        acteResponse = this._acteService.deserializeActe(acteResponse)
        const depositFields = await this._acteService.getDepositFields(this.context)
        const subjectCodes = await this._acteService.getSubjectCode(this.context)

        if (!this.props.draftUuid && !this.props.uuid && depositFields.publicField) {
            acteResponse.public = true
        }
        const groups = await this._adminService.getGroups(this.context)

        this.setState({ fields: acteResponse, acteFetched: true, codesMatieres: subjectCodes, depositFields, groups }, () => this.reloadActe())

    }

    componentWillReceiveProps = async (nextProps) => {
        const { fields, acteFetched } = this.state
        if (nextProps.draftUuid !== this.props.draftUuid && nextProps.uuid !== this.props.uuid && !acteFetched) {
            const acteResponse = await this._acteService.getActeByDraftUuidAndUuid(nextProps.draftUuid, nextProps.uuid, this.context)
            this.reloadActe(acteResponse)
        }
        if (nextProps.mode === 'ACTE_BATCH' && (fields.nature !== nextProps.nature || fields.decision !== nextProps.decision)) {
            if (fields.nature !== nextProps.nature) this.removeAttachmentTypes()
            const shouldFetchTypes = fields.nature !== nextProps.nature && nextProps.nature && fields.code
            fields.nature = nextProps.nature
            fields.decision = nextProps.decision
            this.setState({ fields }, () => {
                if(shouldFetchTypes) this.fetchAttachmentTypes()
                this.validateForm()
            })
        }
    }
    componentWillUnmount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        if (this.state.fields.draft && this.props.shouldUnmount) {
            const acteData = this.getActeData()
            const headers = { 'Content-Type': 'application/json' }
            const url = `/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/leave`
            _fetchWithAuthzHandling({ url, body: JSON.stringify(acteData), headers: headers, method: 'PUT', context: this.props.authContext })
                .then(checkStatus)
                .catch(response => {
                    response.text().then(text => _addNotification(notifications.defaultError, 'notifications.acte.title', text))
                })
        }
        this.validateForm.clear()
        this.saveDraft.clear()
    }
    reloadActe = () => {
        const {fields} = this.state
        if (fields.nature && fields.code) this.fetchAttachmentTypes()
        this.updateGroup()
        this.validateForm()
    }
    fetchAttachmentTypes = async () => {
        const {nature, code : subjectCode} = this.state.fields
        const attachmentTypes = await this._acteService.getAttachmentTypes(nature, subjectCode, this.props.authContext)
        this.setState({ attachmentTypes: sortAlphabetically(attachmentTypes, 'code')})
    }
    updateGroup = () => {
        const { fields, groups } = this.state
        if (!fields.groupUuid) {
            fields.groupUuid = groups.length > 0 ? groups[0].uuid : 'all_group'
            this.setState({ fields })
        }
    }
    getActeData = () => {
        const acteData = Object.assign({}, this.state.fields)
        if (acteData['nature'] === '') acteData['nature'] = null
        if (!acteData['decision']) {
            acteData['decision'] = null
        } else {
            acteData['decision'] = moment(acteData['decision']).format('YYYY-MM-DD')
        }
        if (acteData['groupUuid'] === 'all_group') acteData['groupUuid'] = ''
        return acteData
    }
    extractFieldNameFromId = (str) => str.split('_').slice(-1)[0]
    handleFieldChange = (field, value, callback) => {
        field = this.extractFieldNameFromId(field)
        const fields = this.state.fields
        fields[field] = value
        if (field === 'nature' && value === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS') {
            fields['public'] = false
            fields['publicWebsite'] = false
            if (!fields.code.startsWith(materialCodeBudgetaire)) fields.code = ''
        }
        if ((field === 'objet' || field === 'number') && this.props.mode === 'ACTE_BATCH') this.props.setField(this.state.fields.uuid, field, value)
        this.props.setStatus('', this.state.fields.uuid)
        this.setState({ fields: fields }, () => {
            this.validateForm()
            this.saveDraft()
            if (callback) callback()
            if ((field === 'code' && this.state.fields.nature !== '')
                || (field === 'nature' && this.props.mode !== 'ACTE_BATCH' && this.state.fields.code !== '')) {
                this.fetchAttachmentTypes()
                this.fetchRemoveActeAttachmentTypes()
            }
        })
    }
    validateActeAttachmentType = () => {
        const { annexes, acteAttachment } = this.state.fields
        let annexesValidation = true
        annexes.forEach(annexe => {
            if (!annexe.attachmentTypeCode) annexesValidation = false
        })
        return acteAttachment && acteAttachment.attachmentTypeCode && annexesValidation
    }
    fileAccept = (file, accept) => {
        const { _addNotification, t } = this.context
        if(file.type === 'application/x-moz-file' || accepts(file, accept)) {
            return true
        } else {
            _addNotification(notifications.defaultError, 'notifications.acte.title', t('api-gateway:form.validation.badextension'))
            return false
        }
    }
    onDropActeAttachment = (acceptedFiles, rejectedFiles) => {
        const { _addNotification, t } = this.context
        if(rejectedFiles.length === 0) {
            this.saveDraftFile(acceptedFiles[0], null, 'file')
        } else {
            _addNotification(notifications.defaultError, 'notifications.acte.title', t('api-gateway:form.validation.badextension'))
        }
    }
    onDropAnnexe = (acceptedFiles, rejectedFiles) => {
        const { _addNotification, t } = this.context
        if(rejectedFiles.length === 0) {
            this.saveDraftFile(acceptedFiles[0], null, 'annexe')
        } else {
            _addNotification(notifications.defaultError, 'notifications.acte.title', t('api-gateway:form.validation.badextension'))
        }
    }
    validateForm = debounce(() => {
        const { t } = this.context
        const data = {
            number: this.state.fields.number,
            objet: this.state.fields.objet,
            nature: this.state.fields.nature,
            decision: this.state.fields.decision,
            acteAttachment: this.state.fields.acteAttachment,
            annexes: this.state.fields.annexes,
            code: this.state.fields.code
        }
        const attributeNames = {
            number: t('acte.fields.number'),
            objet: t('acte.fields.objet'),
            nature: t('acte.fields.nature'),
            decision: t('acte.fields.decision'),
            acteAttachment: t('acte.fields.acteAttachment'),
            annexes: t('acte.fields.annexes'),
            code: t('acte.fields.code')
        }
        const validationRules = this.validationRules
        if (this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' || this.props.mode === 'ACTE_BUDGETAIRE')
            validationRules.annexes = 'required'
        Validator.register('acteAttachmentType', this.validateActeAttachmentType, this.context.t('acte.new.PJ_types_validation'))
        const validation = new Validator(data, validationRules)
        validation.setAttributeNames(attributeNames)
        const isFormValid = validation.passes()
        const formErrors = Object.values(validation.errors.all()).map(errors => errors[0])
        this.setState({ isFormValid, formErrors, formFilesErrors: [validation.errors.all().nature, validation.errors.all().code] })
        if (this.props.mode === 'ACTE_BATCH') {
            this.props.setFormValidForId(isFormValid, this.state.fields.uuid, formErrors)
        }
    }, 500)
    saveDraft = debounce(async (callback) => {
        const {fields} = this.state
        if(fields.number || fields.objet || fields.decision ) {
            const acteData = this.getActeData()
            const acteUuid = await this._acteService.saveDraft(acteData.draft.uuid, acteData.uuid, acteData, this.props.authContext)
            fields.uuid = acteUuid
            this.setState({ fields }, callback)
            this.props.setStatus('saved', this.state.fields.uuid)
        }
    }, 3000)
    saveDraftFile = (file, accept, type) => {
        const {fields} = this.state
        if(accept) {
            this.fileAccept(file, accept) && this.saveDraftAttachment(file, fields.draft.uuid, fields.uuid, type)
        } else {
            this.saveDraftAttachment(file, fields.draft.uuid, fields.uuid, type)
        }
    }
    saveDraftAttachment = async (file, draftUuid, uuid, type) => {
        if (file) {
            this.props.setStatus('saving', this.state.fields.uuid)
            const data = new FormData()
            data.append('file', file)
            data.append('nature', this.state.fields.nature)
            try {
                // TODO: Work only Attachments without file content, so it can scale with the multiple acte form
                const acteData = this.getActeData()
                await this._acteService.saveDraft(acteData.draft.uuid, acteData.uuid, acteData, this.props.authContext)

                const response = await this._acteService.saveDraftAttachment(data, draftUuid, uuid, type, this.props.authContext)
                this.props.setStatus('saved', this.state.fields.uuid)
                const acte = this._acteService.deserializeActe(response)
                this.setState({ fields: acte, acteFetched: true }, this.validateForm)
            }catch(error){
                this.props.setStatus('', this.state.fields.uuid)
                this.validateForm()
            }
        }
    }
    deleteDraftAttachment = async (fileDetails, type) => {
        this.props.setStatus('saving', this.state.fields.uuid)
        const {fields} = this.state
        try{
            await this._acteService.deleteDraftAttachment(fields.draft.uuid, fields.uuid, this.props.authContext, type === 'annexe' ? fileDetails.uuid : null)
            if (type === 'annexe') {
                const annexes = fields.annexes.filter(annexe => annexe.uuid !== fileDetails.uuid)
                fields['annexes'] = annexes
            } else {
                fields['acteAttachment'] = null
            }
            this.setState({ fields }, this.validateForm)
            this.props.setStatus('saved', this.state.fields.uuid)
        }catch(error){
            this.props.setStatus('', this.state.fields.uuid)
        }
    }
    onAttachmentTypeChange = async (code, annexeUuid) => {
        try {
            const {fields} = this.state
            if (!!annexeUuid) {
                await this._acteService.updateAttachmentType(fields.draft.uuid, fields.uuid, code, this.props.authContext, annexeUuid)
                const annexe = this.state.fields.annexes.find(annexe => annexe.uuid === annexeUuid)
                annexe.attachmentTypeCode = code
            } else {
                await this._acteService.updateAttachmentType(fields.draft.uuid, fields.uuid, code, this.props.authContext)
                fields.acteAttachment.attachmentTypeCode = code
            }
            this.setState({fields}, this.validateForm)
            this.props.setStatus('saved', this.state.fields.uuid)
        }catch(error){
            this.props.setStatus('', this.state.fields.uuid)
            this.validateForm()
        }
    }
    removeAttachmentTypes = () => {
        const fields = this.state.fields
        if (fields.acteAttachment) fields.acteAttachment.attachmentTypeCode = ''
        fields.annexes.forEach(annexe => annexe.attachmentTypeCode = '')
        this.setState({ fields }, this.validateForm)
        this.props.setStatus('saved', this.state.fields.uuid)
    }
    fetchRemoveActeAttachmentTypes = async () => {
        const {fields} = this.state
        try {
            await this._acteService.deleteAttachmentTypes(fields.draft.uuid, fields.uuid, this.props.authContext)
            this.removeAttachmentTypes()
        }catch(e){
            this.props.setStatus('', this.state.fields.uuid)
            this.validateForm()
        }
    }
    deteleDraft = async (event) => {
        event.preventDefault()
        const localAuthoritySlug = getLocalAuthoritySlug()
        const { _addNotification } = this.context
        const { fields } = this.state

        const regex = /brouillons/g
        await this._acteService.deleteDraft(fields.draft.uuid, this.props.authContext)
        fields['draft'] = false
        this.setState({ fields })

        _addNotification(notifications.acte.draftDeleted)
        if (this.props.path.match(regex)) {
            history.push(`/${localAuthoritySlug}/actes/brouillons`)
        } else {
            history.push(`/${localAuthoritySlug}/actes/liste`)
        }
    }
    saveAndSubmitForm = (event) => {
        event.preventDefault()
        if (this.props.status !== 'saved') this.saveDraft(this.submitForm)
        else this.submitForm()
    }
    submitForm = async () => {
        const localAuthoritySlug = getLocalAuthoritySlug()
        const { _addNotification } = this.context
        const {fields} = this.state

        this.props.setStatus('saving')
        const acteUuid = await this._acteService.postActeForm(fields.draft.uuid, fields.uuid, this.props.authContext)

        fields['draft'] = false
        this.props.setStatus('saved')
        this.setState({ fields }, () => {
            _addNotification(notifications.acte.sent)
            history.push(`/${localAuthoritySlug}/actes/liste/${acteUuid}`)
        })
    }

    render() {
        const { t } = this.context
        const isPublicFieldDisabled = !this.state.depositFields.publicField
        const isFormSaving = this.props.status === 'saving'
        const natureOptions = natures.map(nature =>
            ({ key: nature, value: nature, text: t(`acte.nature.${nature}`) })
        )
        const codeOptions = this.state.codesMatieres
            .filter(materialCode => this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' || this.props.mode === 'ACTE_BUDGETAIRE'
                ? materialCode.code.startsWith(materialCodeBudgetaire)
                : true)
            .map(materialCode =>
                ({ key: materialCode.code, value: materialCode.code, text: materialCode.code + ' - ' + materialCode.label })
            )
        const attachmentTypes = this.state.attachmentTypes.map(attachmentType =>
            ({ key: attachmentType.uuid, value: attachmentType.code, text: attachmentType.code + ' - ' + attachmentType.label })
        )
        const groupOptions = this.state.groups.map(group =>
            ({ key: group.uuid, value: group.uuid, text: group.name })
        )
        // Hack : semantic ui dropdown doesn't support empty value yet (https://github.com/Semantic-Org/Semantic-UI-React/issues/1748)
        groupOptions.push({ key: 'all_group', value: 'all_group', text: t('acte.new.every_group') })
        const groupOptionValue = this.state.fields.groupUuid === null ? groupOptions[0].value : this.state.fields.groupUuid
        const acceptFile = this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' ? '.xml' : '.pdf, .jpg, .png'
        const acceptAnnexes = this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' ? '.pdf, .jpg, .png' : '.pdf, .xml, .jpg, .png'
        const submissionButton =
            <Button type='submit' primary basic disabled={!this.state.isFormValid || isFormSaving} loading={isFormSaving}>
                <Icon name={'paper plane'}/>
                {t('api-gateway:form.submit')}
            </Button>
        return (
            (this.props.mode !== 'ACTE_BATCH' || this.props.active === this.state.fields.uuid) && (
                <Form onSubmit={this.saveAndSubmitForm}>
                    <Grid columns={3}>
                        <Grid.Column mobile={16} tablet={16} computer={4}>
                            <FormField htmlFor={`${this.state.fields.uuid}_number`} label={t('acte.fields.number')}>
                                <InputValidation id={`${this.state.fields.uuid}_number`}
                                    placeholder={t('acte.fields.number') + '...'}
                                    ariaRequired={true}
                                    helper={t('acte.help_text.number')}
                                    value={this.state.fields.number}
                                    onChange={(id, value) => this.handleFieldChange(id,value.toUpperCase())}
                                    validationRule={this.validationRules.number}
                                    fieldName={t('acte.fields.number')} />
                            </FormField>
                        </Grid.Column>
                        <Grid.Column mobile={16} tablet={16} computer={5}>
                            <FormField htmlFor={`${this.state.fields.uuid}_objet`} label={t('acte.fields.objet')}>
                                <InputValidation id={`${this.state.fields.uuid}_objet`}
                                    ariaRequired={true}
                                    placeholder={t('acte.fields.objet') + '...'}
                                    value={this.state.fields.objet}
                                    onChange={this.handleFieldChange}
                                    validationRule={this.validationRules.objet}
                                    fieldName={t('acte.fields.objet')} />
                            </FormField>
                        </Grid.Column>
                        {(this.props.mode !== 'ACTE_BATCH') && (
                            <Grid.Column mobile={16} tablet={16} computer={7}>
                                <FormField htmlFor={`${this.state.fields.uuid}_nature`} label={t('acte.fields.nature')}>
                                    <InputValidation id={`${this.state.fields.uuid}_nature`}
                                        type='dropdown'
                                        search={true}
                                        disabled={this.props.mode === 'ACTE_BUDGETAIRE'}
                                        placeholder={`${t('acte.fields.nature')}...`}
                                        ariaRequired={true}
                                        value={this.state.fields.nature}
                                        onChange={this.handleFieldChange}
                                        validationRule={this.validationRules.nature}
                                        fieldName={t('acte.fields.nature')}
                                        options={natureOptions}
                                    />
                                </FormField>
                            </Grid.Column>
                        )}

                        {this.props.mode !== 'ACTE_BATCH' && (
                            <Grid.Column mobile={16} tablet={16} computer={4}>
                                <FormField htmlFor={`${this.state.fields.uuid}_decision`} label={t('acte.fields.decision')}>
                                    <InputValidation
                                        id={`${this.state.fields.uuid}_decision`}
                                        type='date'
                                        ariaRequired={true}
                                        value={this.state.fields.decision ? this.state.fields.decision : moment()}
                                        onChange={this.handleFieldChange}
                                        validationRule={this.validationRules.decision}
                                        fieldName={t('acte.fields.decision')}
                                        isValidDate={(current) => current.isBefore(new moment())} />
                                </FormField>
                            </Grid.Column>
                        )}

                        {this.props.mode !== 'ACTE_BATCH' && (
                            <Grid.Column mobile={16} tablet={16} computer={5}>
                                <FormField htmlFor={`${this.state.fields.uuid}_groupUuid`} label={t('acte.fields.group')}
                                    helpText={t('acte.help_text.group')}>
                                    <Dropdown id={`${this.state.fields.uuid}_groupUuid`}
                                        value={groupOptionValue}
                                        onChange={(event, { id, value }) => this.handleFieldChange(id, value)}
                                        options={groupOptions}
                                        fluid selection />
                                </FormField>
                            </Grid.Column>
                        )}

                        <Grid.Column mobile={16} tablet={16} computer={7}>
                            <FormField htmlFor={`${this.state.fields.uuid}_code`} label={t('acte.fields.code')} helpText={t('acte.help_text.code')}>
                                <InputValidation id={`${this.state.fields.uuid}_code`}
                                    type='dropdown'
                                    placeholder={`${t('acte.fields.code')}...`}
                                    search={true}
                                    ariaRequired={true}
                                    value={this.state.fields.code}
                                    onChange={this.handleFieldChange}
                                    validationRule={this.validationRules.code}
                                    fieldName={t('acte.fields.code')}
                                    options={codeOptions}
                                    ariaLabel="Acte code" />
                            </FormField>
                        </Grid.Column>
                    </Grid>


                    <Grid centered columns={3}>
                        <Grid.Column textAlign={'center'} computer={16}>
                            <FormField htmlFor={`${this.state.fields.uuid}_acteAttachment`} label={t('acte.fields.acteAttachment')}
                                helpText={t('acte.help_text.acteAttachment', { acceptFile })}>
                                <DragAndDropFile
                                    key={`${this.state.fields.uuid}_acteAttachment`}
                                    multiple={false}
                                    acceptFile={acceptFile}
                                    onDrop={this.onDropActeAttachment}
                                    disableClick={true}>
                                    <InputValidation id={`${this.state.fields.uuid}_acteAttachment`}
                                        labelClassName="primary"
                                        type='file'
                                        icon={false}
                                        ariaRequired={true}
                                        accept={acceptFile}
                                        onChange={(file, accept) => this.saveDraftFile(file, accept, 'file')}
                                        value={this.state.fields.acteAttachment}
                                        validationRule={this.validationRules.acteAttachment}
                                        label={`${t('api-gateway:form.or')} ${t('api-gateway:form.add_a_file')}`}
                                        fieldName={t('acte.fields.acteAttachment')} />
                                </DragAndDropFile>
                            </FormField>
                        </Grid.Column>

                        {this.state.fields.acteAttachment && (
                            <Grid.Column textAlign={'center'} computer={16}>
                                <FormFiles files={[this.state.fields.acteAttachment]}
                                    attachmentTypeOptions={attachmentTypes}
                                    onAttachmentTypeChange={(e, { value }) => this.onAttachmentTypeChange(value)}
                                    errors={this.state.formFilesErrors}
                                    onDelete={(res) => this.deleteDraftAttachment(res)}

                                />
                            </Grid.Column>
                        )}
                    </Grid>
                    <Grid centered columns={1}>
                        <Grid.Column textAlign={'center'} column={16}>
                            <FormField htmlFor={`${this.state.fields.uuid}_annexes`} label={t('acte.fields.annexes')}
                                optionalLabelText={t('acte.fields.optional_field')}
                                helpText={t('acte.help_text.annexes', { acceptAnnexes })}
                                required={this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' || this.props.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS'}>
                                <DragAndDropFile
                                    key={`${this.state.fields.uuid}_annexes`}
                                    acceptFile={acceptAnnexes}
                                    onDrop={this.onDropAnnexe}
                                    disableClick={true}
                                    multiple={false}>
                                    <InputFile icon={false} labelClassName="primary" htmlFor={`${this.state.fields.uuid}_annexes`} label={`${t('api-gateway:form.or')} ${t('api-gateway:form.add_a_file')}`}>
                                        <input type="file" aria-required={this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' || this.props.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS'} id={`${this.state.fields.uuid}_annexes`} accept={acceptAnnexes}
                                            onChange={e => this.saveDraftFile(e.target.files[0], acceptAnnexes, 'annexe')} style={{ display: 'none' }} />
                                    </InputFile>
                                </DragAndDropFile>
                            </FormField>
                            {this.state.fields.annexes.length > 0 && (
                                <Grid.Column textAlign={'center'} computer={16}>
                                    <FormFiles files={this.state.fields.annexes}
                                        attachmentTypeOptions={attachmentTypes}
                                        onAttachmentTypeChange={(e, { value }, annexeUuid) => this.onAttachmentTypeChange(value, annexeUuid)}
                                        errors={this.state.formFilesErrors}
                                        onDelete={(res) => this.deleteDraftAttachment(res, 'annexe')}
                                    />
                                </Grid.Column>
                            )}
                        </Grid.Column>
                    </Grid>
                    {(this.state.fields.nature !== 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS'
                    && this.props.nature !== 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS') && (
                            <Grid columns={3} style={{ marginBottom: 'auto' }}>
                                <Grid.Column>
                                    <FormField label={t('acte.fields.public')}
                                        helpText={t('acte.help_text.public')}>
                                        <Checkbox id={`${this.state.fields.uuid}_public`} disabled={isPublicFieldDisabled}
                                            checked={this.state.fields.public} toggle
                                            onChange={e => handleFieldCheckboxChange(this, 'public', this.saveDraft)} aria-label={t('acte.help_text.public')}/>
                                    </FormField>
                                </Grid.Column>
                                {this.state.depositFields.publicWebsiteField && (
                                    <Grid.Column>
                                        <FormField htmlFor={`${this.state.fields.uuid}_publicWebsite`} label={t('acte.fields.publicWebsite')}
                                            helpText={t('acte.help_text.publicWebsite')}>
                                            <Checkbox id={`${this.state.fields.uuid}_publicWebsite`} checked={this.state.fields.publicWebsite}
                                                onChange={e => handleFieldCheckboxChange(this, 'publicWebsite', this.saveDraft)} toggle />
                                        </FormField>
                                    </Grid.Column>
                                )}
                            </Grid>
                        )
                    }

                    <FormField label={t('acte.fields.multipleChannels')} helpText={t('acte.help_text.multipleChannels')}>
                        <Checkbox id={`${this.state.fields.uuid}_multipleChannels`}
                            checked={this.state.fields.multipleChannels} toggle
                            onChange={e => handleFieldCheckboxChange(this, 'multipleChannels', this.saveDraft)} aria-label={t('acte.help_text.multipleChannels')}/>
                    </FormField>
                    {this.props.mode !== 'ACTE_BATCH' && (
                        <div style={{ textAlign: 'right' }}>
                            {(sum(this.state.fields.annexes, 'size') !== 0 || this.state.fields.acteAttachment !== null) && (
                                <label style={{ fontSize: '1em', color: 'rgba(0,0,0,0.4)', marginRight: '10px'}}>
                                    {this.context.t('acte.help_text.annexes_size')} {bytesToSize(sum(this.state.fields.annexes, 'size') + (this.state.fields.acteAttachment ? this.state.fields.acteAttachment.size : 0))} / 150Mo
                                </label>
                            )}
                            {this.state.formErrors.length === 0 && submissionButton}
                            {this.state.formErrors.length > 0 &&
                            <ValidationPopup errorList={this.state.formErrors}>
                                {submissionButton}
                            </ValidationPopup>
                            }
                            {this.state.fields.uuid && (
                                <Button type="button" style={{ marginRight: '1em' }} onClick={e => this.deteleDraft(e)} compact basic color='red'
                                    disabled={isFormSaving} loading={isFormSaving}>
                                    <Icon name={'trash'}/>
                                    {t('api-gateway:form.delete_draft')}
                                </Button>
                            )}
                        </div>
                    )}
                </Form>
            )
        )
    }
}

export default translate(['acte', 'api-gateway'])(withAuthContext(NewActeForm))
