import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Button, Form, Checkbox, Card, Dropdown } from 'semantic-ui-react'
import Validator from 'validatorjs'
import debounce from 'debounce'

import { FormField, File, InputFile } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import { notifications } from '../_util/Notifications'
import history from '../_util/history'
import { checkStatus, fetchWithAuthzHandling, handleFieldCheckboxChange } from '../_util/utils'
import { natures } from '../_util/constants'
import moment from 'moment'

class NewActeForm extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
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
            groupUuid: '',
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
        acteFetched: false
    }
    validationRules = {
        number: ['required', 'max:15', 'regex:/^[a-zA-Z0-9_]+$/'],
        objet: 'required|max:500',
        nature: 'required',
        code: 'required',
        decision: ['required', 'date'],
        acteAttachment: 'required|acteAttachmentType'
    }
    customErrorMessages = {
        regex: this.context.t('api-gateway:form.validation.regex_alpha_num_underscore', { fieldName: this.context.t('acte.fields.number') })
    }
    componentDidMount() {
        if (!this.props.draftUuid && !this.props.uuid) this.fetchActe(`/api/acte/draft/${this.props.mode}`)
        fetchWithAuthzHandling({ url: '/api/acte/localAuthority/depositFields' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ depositFields: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
        fetchWithAuthzHandling({ url: '/api/acte/localAuthority/codes-matieres' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ codesMatieres: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
        if (this.props.mode !== 'ACTE_BATCH') {
            fetchWithAuthzHandling({ url: '/api/admin/profile/groups' })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ groups: json }))
                .catch(response => {
                    response.json().then(json => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', json.message))
                })
        }
    }
    componentWillReceiveProps(nextProps) {
        const { fields, acteFetched } = this.state
        if (nextProps.draftUuid && nextProps.uuid && !acteFetched) {
            this.fetchActe(`/api/acte/drafts/${nextProps.draftUuid}/${nextProps.uuid}`)
        }
        if (nextProps.mode === 'ACTE_BATCH' && (fields.nature !== nextProps.nature || fields.decision !== nextProps.decision)) {
            fields.nature = nextProps.nature
            fields.decision = nextProps.decision
            if (fields.nature !== nextProps.nature) this.removeAttachmentTypes()
            this.setState({ fields }, this.validateForm)
        }
    }
    componentWillUnmount() {
        if (this.state.fields.draft && this.props.shouldUnmount) {
            const acteData = this.getActeData()
            const headers = { 'Content-Type': 'application/json' }
            fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/leave`, body: JSON.stringify(acteData), headers: headers, method: 'PUT', context: this.context })
                .then(checkStatus)
                .catch(response => {
                    response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
                })
        }
        this.validateForm.clear()
        this.saveDraft.clear()
    }
    fetchActe = (url) => {
        fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                this.loadActe(json, () => {
                    if (json.nature && this.props.mode !== 'ACTE_BATCH') this.fetchAttachmentTypes()
                    this.validateForm()
                })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
    }
    fetchAttachmentTypes = () => {
        const nature = this.state.fields.nature
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/acte/attachment-types/${nature}`, headers: headers, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ attachmentTypes: json }))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    loadActe = (acte, callback) => {
        // Hacks to prevent affecting `null` values from the new empty returned acte
        if (!acte.nature) acte.nature = ''
        if (!acte.code) acte.code = ''
        if (!acte.codeLabel) acte.codeLabel = ''
        if (!acte.decision) acte.decision = ''
        if (!acte.groupUuid) acte.groupUuid = ''
        if (!acte.objet) acte.objet = ''
        if (!acte.number) acte.number = ''
        if (!acte.annexes) acte.annexes = []
        this.setState({ fields: acte, acteFetched: true }, callback)
    }
    getActeData = () => {
        const acteData = Object.assign({}, this.state.fields)
        if (acteData['nature'] === '') acteData['nature'] = null
        if (acteData['decision'] === '') acteData['decision'] = null
        return acteData
    }
    extractFieldNameFromId = (str) => str.split('_').slice(-1)[0]
    handleFieldChange = (field, value) => {
        field = this.extractFieldNameFromId(field)
        const fields = this.state.fields
        fields[field] = value
        if (field === 'nature' && value === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS') {
            fields['public'] = false
            fields['publicWebsite'] = false
        }
        if ((field === 'objet' || field === 'number') && this.props.mode === 'ACTE_BATCH') this.props.setField(this.state.fields.uuid, field, value)
        this.props.setStatus('', this.state.fields.uuid)
        this.setState({ fields: fields }, () => {
            this.validateForm()
            this.saveDraft()
            if (field === 'nature' && this.props.mode !== 'ACTE_BATCH') {
                this.fetchAttachmentTypes()
                this.fetchRemoveAttachmentTypes()
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
    validateForm = debounce(() => {
        const data = {
            number: this.state.fields.number,
            objet: this.state.fields.objet,
            nature: this.state.fields.nature,
            decision: this.state.fields.decision,
            acteAttachment: this.state.fields.acteAttachment,
            code: this.state.fields.code
        }
        Validator.register('acteAttachmentType', this.validateActeAttachmentType, this.context.t('acte.new.PJ_types_validation'))
        const validation = new Validator(data, this.validationRules)
        const isFormValid = validation.passes()
        this.setState({ isFormValid })
        if (this.props.mode === 'ACTE_BATCH') {
            this.props.setFormValidForId(isFormValid, this.state.fields.uuid)
        }
    }, 500)
    saveDraft = debounce((callback) => {
        const acteData = this.getActeData()
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${acteData.draft.uuid}/${acteData.uuid}`, body: JSON.stringify(acteData), headers: headers, method: 'PUT', context: this.context })
            .then(checkStatus)
            .then(response => response.text())
            .then(acteUuid => {
                const fields = this.state.fields
                fields['uuid'] = acteUuid
                this.setState({ fields }, callback)
                this.props.setStatus('saved', this.state.fields.uuid)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }, 3000)
    saveDraftFile = (file) => this.saveDraftAttachment(file, `/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/file`)
    saveDraftAnnexe = (file) => this.saveDraftAttachment(file, `/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/annexe`)
    saveDraftAttachment = (file, url) => {
        if (file) {
            this.props.setStatus('saving', this.state.fields.uuid)
            const data = new FormData()
            data.append('file', file)
            fetchWithAuthzHandling({ url: url, body: data, method: 'POST', context: this.context })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => {
                    // TODO: Work only Attachments without file content, so it can scale with the multiple acte form
                    this.props.setStatus('saved', this.state.fields.uuid)
                    this.loadActe(json, this.validateForm)
                })
                .catch(response => {
                    response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
                    this.props.setStatus('', this.state.fields.uuid)
                    this.validateForm()
                })
        }
    }
    deleteDraftFile = () => this.deleteDraftAttachment(`/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/file`)
    deleteDraftAnnexe = (annexe) => this.deleteDraftAttachment(`/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/annexe/${annexe.uuid}`, annexe.uuid)
    deleteDraftAttachment = (url, annexeUuid) => {
        this.props.setStatus('saving', this.state.fields.uuid)
        fetchWithAuthzHandling({ url: url, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                const fields = this.state.fields
                if (annexeUuid) {
                    const annexes = this.state.fields.annexes.filter(annexe => annexe.uuid !== annexeUuid)
                    fields['annexes'] = annexes
                } else {
                    fields['acteAttachment'] = null
                }
                this.setState({ fields }, this.validateForm)
                this.props.setStatus('saved', this.state.fields.uuid)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
                this.props.setStatus('', this.state.fields.uuid)
            })
    }
    onFileAttachmentTypeChange = (code) => this.onAttachmentTypeChange(`/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/file/type/${code}`, code)
    onAnnexeAttachmentTypeChange = (code, annexeUuid) => this.onAttachmentTypeChange(`/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/annexe/${annexeUuid}/type/${code}`, code, annexeUuid)
    onAttachmentTypeChange = (url, code, annexeUuid) => {
        fetchWithAuthzHandling({ url, method: 'PUT', context: this.context })
            .then(checkStatus)
            .then(() => {
                const fields = this.state.fields
                if (annexeUuid) {
                    const annexe = this.state.fields.annexes.find(annexe => annexe.uuid === annexeUuid)
                    annexe.attachmentTypeCode = code
                } else {
                    fields.acteAttachment.attachmentTypeCode = code
                }
                this.setState({ fields }, this.validateForm)
                this.props.setStatus('saved', this.state.fields.uuid)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
                this.props.setStatus('', this.state.fields.uuid)
                this.validateForm()
            })
    }
    removeAttachmentTypes = () => {
        const fields = this.state.fields
        if (fields.acteAttachment) fields.acteAttachment.attachmentTypeCode = ''
        fields.annexes.forEach(annexe => annexe.attachmentTypeCode = '')
        this.setState({ fields }, this.validateForm)
        this.props.setStatus('saved', this.state.fields.uuid)
    }
    fetchRemoveAttachmentTypes = () => {
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.draft.uuid}/types`, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(this.removeAttachmentTypes)
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
                this.props.setStatus('', this.state.fields.uuid)
                this.validateForm()
            })
    }
    deteleDraft = (event) => {
        event.preventDefault()
        const draftUuid = this.state.fields.draft.uuid
        const { fields } = this.state
        fields['draft'] = false
        this.setState({ fields })
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: '/api/acte/drafts', body: JSON.stringify([draftUuid]), headers: headers, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                this.context._addNotification(notifications.acte.draftDeleted)
                history.push('/actes/liste')
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    saveAndSubmitForm = (event) => {
        event.preventDefault()
        if (this.props.status !== 'saved') this.saveDraft(this.submitForm)
        else this.submitForm()
    }
    submitForm = () => {
        const fields = this.state.fields
        fields['draft'] = false
        this.setState({ fields })
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${fields.draft.uuid}/${fields.uuid}`, method: 'POST', context: this.context })
            .then(checkStatus)
            .then(response => response.text())
            .then(acteUuid => {
                this.context._addNotification(notifications.acte.sent)
                history.push('/actes/' + acteUuid)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.acte.title', text))
            })
    }
    render() {
        const { t } = this.context
        const isPublicFieldDisabled = !this.state.depositFields.publicField
        const isFormSaving = this.props.status === 'saving'
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        const codeOptions = this.state.codesMatieres.map(materialCode => {
            const object = { key: materialCode.code, value: materialCode.code, text: materialCode.code + " - " + materialCode.label }
            return object
        })
        const attachmentTypeSource = this.props.mode === 'ACTE_BATCH' ? this.props.attachmentTypes : this.state.attachmentTypes
        const attachmentTypes = attachmentTypeSource.map(attachmentType => {
            return { key: attachmentType.uuid, value: attachmentType.code, text: attachmentType.label }
        })
        const groupOptions = this.state.groups.map(group =>
            <option key={group.uuid} value={group.uuid}>{group.name}</option>
        )
        const fileAttachmentTypeDropdown = (attachmentTypes.length > 0 && this.state.fields.acteAttachment) &&
            <Dropdown fluid selection
                placeholder={t('acte.new.PJ_types')}
                options={attachmentTypes}
                value={this.state.fields.acteAttachment.attachmentTypeCode}
                onChange={(e, { value }) => this.onFileAttachmentTypeChange(value)} />
        const annexes = this.state.fields.annexes.map(annexe => {
            const extraContent = attachmentTypes.length > 0 &&
                <Dropdown fluid selection
                    placeholder={t('acte.new.PJ_types')}
                    options={attachmentTypes}
                    value={annexe.attachmentTypeCode}
                    onChange={(e, { value }) => this.onAnnexeAttachmentTypeChange(value, annexe.uuid)} />
            return (
                <File
                    key={`${this.state.fields.uuid}_${annexe.uuid}`}
                    attachment={annexe}
                    onDelete={this.deleteDraftAnnexe}
                    extraContent={extraContent && extraContent} />
            )
        })
        const acceptFile = this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' ? ".xml" : ".pdf, .jpg, .png"
        const acceptAnnexes = this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' ? ".pdf, .jpg, .png" : ".pdf, .xml, .jpg, .png"
        return (
            renderIf(this.props.mode !== 'ACTE_BATCH' || this.props.active === this.state.fields.uuid)(
                <Form onSubmit={this.saveAndSubmitForm}>
                    <FormField htmlFor={`${this.state.fields.uuid}_number`} label={t('acte.fields.number')}>
                        <InputValidation id={`${this.state.fields.uuid}_number`}
                            placeholder={t('acte.fields.number') + '...'}
                            value={this.state.fields.number}
                            onChange={this.handleFieldChange}
                            validationRule={this.validationRules.number}
                            fieldName={t('acte.fields.number')} />
                    </FormField>
                    <FormField htmlFor={`${this.state.fields.uuid}_objet`} label={t('acte.fields.objet')}>
                        <InputValidation id={`${this.state.fields.uuid}_objet`}
                            placeholder={t('acte.fields.objet') + '...'}
                            value={this.state.fields.objet}
                            onChange={this.handleFieldChange}
                            validationRule={this.validationRules.objet}
                            fieldName={t('acte.fields.objet')} />
                    </FormField>
                    {renderIf(this.props.mode !== 'ACTE_BATCH')(
                        <FormField htmlFor={`${this.state.fields.uuid}_decision`} label={t('acte.fields.decision')}>
                            <InputValidation id={`${this.state.fields.uuid}_decision`}
                                type='date'
                                placeholder='aaaa-mm-jj'
                                value={this.state.fields.decision}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.decision}
                                fieldName={t('acte.fields.decision')}
                                max={moment().format('YYYY-MM-DD')} />
                        </FormField>
                    )}
                    {renderIf(this.props.mode !== 'ACTE_BATCH')(
                        <FormField htmlFor={`${this.state.fields.uuid}_group`} label={t('acte.fields.group')}>
                            <select id='groupUuid' value={this.state.fields.groupUuid} onChange={e => this.handleFieldChange('groupUuid', e.target.value)}>
                                <option value=''>{t('acte.new.every_group')}</option>
                                {groupOptions}
                            </select>
                        </FormField>
                    )}
                    {renderIf(this.props.mode !== 'ACTE_BUDGETAIRE' && this.props.mode !== 'ACTE_BATCH')(
                        <FormField htmlFor={`${this.state.fields.uuid}_nature`} label={t('acte.fields.nature')}>
                            <InputValidation id={`${this.state.fields.uuid}_nature`}
                                type='select'
                                value={this.state.fields.nature}
                                onChange={this.handleFieldChange}
                                validationRule={this.validationRules.nature}
                                fieldName={t('acte.fields.nature')}>
                                <option value='' disabled>{t('acte.new.choose')}</option>
                                {natureOptions}
                            </InputValidation>
                        </FormField>
                    )}
                    <FormField htmlFor={`${this.state.fields.uuid}_code`} label={t('acte.fields.code')}>
                        <InputValidation id={`${this.state.fields.uuid}_code`}
                            type='dropdown'
                            value={this.state.fields.code}
                            onChange={this.handleFieldChange}
                            validationRule={this.validationRules.code}
                            fieldName={t('acte.fields.code')}
                            options={codeOptions} />
                    </FormField>
                    <FormField htmlFor={`${this.state.fields.uuid}_acteAttachment`} label={t('acte.fields.acteAttachment')}>
                        <InputValidation id={`${this.state.fields.uuid}_acteAttachment`}
                            type='file'
                            accept={acceptFile}
                            onChange={this.saveDraftFile}
                            value={this.state.fields.acteAttachment}
                            validationRule={this.validationRules.acteAttachment}
                            label={t('api-gateway:form.add_a_file')}
                            fieldName={t('acte.fields.acteAttachment')} />
                    </FormField>
                    {renderIf(this.state.fields.acteAttachment)(
                        <File
                            key={`${this.state.fields.uuid}_acteAttachment`}
                            attachment={this.state.fields.acteAttachment}
                            onDelete={this.deleteDraftFile}
                            extraContent={fileAttachmentTypeDropdown && fileAttachmentTypeDropdown} />
                    )}
                    <FormField htmlFor={`${this.state.fields.uuid}_annexes`} label={t('acte.fields.annexes')}>
                        <InputFile htmlFor={`${this.state.fields.uuid}_annexes`} label={t('api-gateway:form.add_a_file')}>
                            <input type="file" id={`${this.state.fields.uuid}_annexes`} accept={acceptAnnexes} onChange={e => this.saveDraftAnnexe(e.target.files[0])}
                                style={{ display: 'none' }} />
                        </InputFile>
                    </FormField>
                    {renderIf(this.state.fields.annexes.length > 0)(
                        <Card.Group>
                            {annexes}
                        </Card.Group>
                    )}
                    {renderIf(this.state.fields.nature !== 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' && this.props.nature !== 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS')(
                        <div>
                            <FormField htmlFor={`${this.state.fields.uuid}_public`} label={t('acte.fields.public')}>
                                <Checkbox id={`${this.state.fields.uuid}_public`} disabled={isPublicFieldDisabled} checked={this.state.fields.public} onChange={e => handleFieldCheckboxChange(this, 'public', this.saveDraft)} toggle />
                            </FormField>
                            {renderIf(this.state.depositFields.publicWebsiteField)(
                                <FormField htmlFor={`${this.state.fields.uuid}_publicWebsite`} label={t('acte.fields.publicWebsite')}>
                                    <Checkbox id={`${this.state.fields.uuid}_publicWebsite`} checked={this.state.fields.publicWebsite} onChange={e => handleFieldCheckboxChange(this, 'publicWebsite', this.saveDraft)} toggle />
                                </FormField>
                            )}
                        </div>
                    )}
                    {renderIf(this.props.mode !== 'ACTE_BATCH')(
                        <div style={{ textAlign: 'right' }}>
                            {renderIf(this.state.fields.uuid)(
                                <Button style={{ marginRight: '1em' }} onClick={e => this.deteleDraft(e)} compact basic color='red' disabled={isFormSaving} loading={isFormSaving}>
                                    {t('api-gateway:form.delete_draft')}
                                </Button>
                            )}
                            <Button type='submit' primary basic disabled={!this.state.isFormValid || isFormSaving} loading={isFormSaving}>{t('api-gateway:form.submit')}</Button>
                        </div>
                    )}
                </Form>
            )
        )
    }
}

export default translate(['acte', 'api-gateway'])(NewActeForm)