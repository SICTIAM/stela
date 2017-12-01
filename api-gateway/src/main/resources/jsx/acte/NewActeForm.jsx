import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Button, Form, Checkbox, Card } from 'semantic-ui-react'
import Validator from 'validatorjs'
import debounce from 'debounce'

import { FormField, File, InputFile } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import { errorNotification, acteSentSuccess, draftDeletedSuccess } from '../_components/Notifications'
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
        acteAttachment: 'required'
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
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })

        fetchWithAuthzHandling({ url: '/api/acte/localAuthority/codes-matieres' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ codesMatieres: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })
    }
    componentWillReceiveProps(nextProps) {
        const { fields, acteFetched } = this.state
        if (nextProps.draftUuid && nextProps.uuid && !acteFetched) {
            this.fetchActe(`/api/acte/drafts/${nextProps.draftUuid}/${nextProps.uuid}`)
        }
        if (nextProps.mode === 'ACTE_BATCH' && (fields.nature !== nextProps.nature || fields.decision !== nextProps.decision)) {
            fields.nature = nextProps.nature
            fields.decision = nextProps.decision
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
                    response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
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
                this.loadActe(json, this.validateForm)
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })
    }
    loadActe = (acte, callback) => {
        // Hacks to prevent affecting `null` values from the new empty returned acte
        if (!acte.nature) acte.nature = ''
        if (!acte.code) acte.code = ''
        if (!acte.codeLabel) acte.codeLabel = ''
        if (!acte.decision) acte.decision = ''
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
        })
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
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
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
                    response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
                    this.props.setStatus('', this.state.fields.uuid)
                    this.validateForm()
                })
        }
    }
    deleteDraftFile = () => this.deleteDraftAttachment(`/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/file`)
    deleteDraftAnnexe = (annexeUuid) => this.deleteDraftAttachment(`/api/acte/drafts/${this.state.fields.draft.uuid}/${this.state.fields.uuid}/annexe/${annexeUuid}`, annexeUuid)
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
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
                this.props.setStatus('', this.state.fields.uuid)
            })
    }
    deteleDraft = () => {
        const fields = this.state.fields
        fields['draft'] = false
        this.setState({ fields })
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: '/api/acte/drafts', body: JSON.stringify([this.state.fields.draft.uuid]), headers: headers, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                this.context._addNotification(draftDeletedSuccess(this.context.t))
                history.push('/actes/liste')
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
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
                this.context._addNotification(acteSentSuccess(this.context.t))
                history.push('/actes/' + acteUuid)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    render() {
        const { t } = this.context
        const isPublicFieldDisabled = !this.state.depositFields.publicField
        const isFormSaving = this.props.status === 'saving'
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        const codeOptions = Object.entries(this.state.codesMatieres).map(([key, value]) => {
            const object = { key: key, value: key, text: key + " - " + value }
            return object
        })
        const annexes = this.state.fields.annexes.map(annexe =>
            <File key={`${this.state.fields.uuid}_${annexe.uuid}`} attachment={annexe} onDelete={this.deleteDraftAnnexe} />
        )
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
                        <File key={`${this.state.fields.uuid}_acteAttachment`} attachment={this.state.fields.acteAttachment} onDelete={this.deleteDraftFile} />
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
                                <Checkbox id={`${this.state.fields.uuid}_public`} disabled={isPublicFieldDisabled} checked={this.state.fields.public} onChange={e => handleFieldCheckboxChange(this, 'public')} toggle />
                            </FormField>
                            {renderIf(this.state.depositFields.publicWebsiteField)(
                                <FormField htmlFor={`${this.state.fields.uuid}_publicWebsite`} label={t('acte.fields.publicWebsite')}>
                                    <Checkbox id={`${this.state.fields.uuid}_publicWebsite`} checked={this.state.fields.publicWebsite} onChange={e => handleFieldCheckboxChange(this, 'publicWebsite')} toggle />
                                </FormField>
                            )}
                        </div>
                    )}
                    {renderIf(this.props.mode !== 'ACTE_BATCH')(
                        <div>
                            <Button type='submit' disabled={!this.state.isFormValid || isFormSaving} loading={isFormSaving}>{t('api-gateway:form.submit')}</Button>
                            {renderIf(this.state.fields.uuid)(
                                <Button style={{ marginLeft: '1em' }} onClick={this.deteleDraft} compact basic color='red' disabled={isFormSaving} loading={isFormSaving}>
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

export default translate(['acte', 'api-gateway'])(NewActeForm)