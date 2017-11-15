import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Grid, Button, Form, Checkbox, Menu, Segment, Card } from 'semantic-ui-react'
import Validator from 'validatorjs'
import debounce from 'debounce'

import { FormField, File, InputFile } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import { errorNotification, acteSentSuccess, draftDeletedSuccess } from '../_components/Notifications'
import history from '../_util/history'
import { checkStatus, fetchWithAuthzHandling, handleFieldCheckboxChange } from '../_util/utils'
import { natures } from '../_util/constants'
import moment from 'moment'

class NewActe extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        mode: 'newActe',
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
            draft: true
        },
        depositFields: {
            publicField: false,
            publicWebsiteField: false
        },
        codesMatieres: [],
        isFormValid: false,
        formStatus: ''
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
        const url = this.props.uuid ? '/api/acte/drafts/' + this.props.uuid : '/api/acte/draft'
        fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                if (json.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS') this.setState({ mode: 'newActeBudgetaire' })
                this.loadActe(json, this.validateForm)
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })

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
    componentWillUnmount() {
        if (this.state.fields.draft) {
            const acteData = this.getActeData()
            const headers = { 'Content-Type': 'application/json' }
            fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.state.fields.uuid}/leave`, body: JSON.stringify(acteData), headers: headers, method: 'PUT', context: this.context })
                .then(checkStatus)
                .catch(response => {
                    response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
                })
        }
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
        this.setState({ fields: acte }, callback)
    }
    getActeData = () => {
        const acteData = Object.assign({}, this.state.fields)
        if (acteData['nature'] === '') acteData['nature'] = null
        if (acteData['decision'] === '') acteData['decision'] = null
        return acteData
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        if (field === 'nature' && value === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS') {
            fields['public'] = false
            fields['publicWebsite'] = false
        }
        this.setState({ fields: fields, formStatus: '' }, () => {
            this.validateForm()
            this.saveDraft()
        })
    }
    handleModeChange = (e, { id }) => {
        const fields = this.state.fields
        if (id === 'newActeBudgetaire') {
            fields['nature'] = 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS'
            fields['public'] = false
            fields['publicWebsite'] = false
        }
        else fields['nature'] = ''
        this.setState({ mode: id, fields: fields }, () => {
            this.validateForm()
            this.saveDraft()
        })
    }
    validateForm = () => {
        const data = {
            number: this.state.fields.number,
            objet: this.state.fields.objet,
            nature: this.state.fields.nature,
            decision: this.state.fields.decision,
            acteAttachment: this.state.fields.acteAttachment,
            code: this.state.fields.code
        }
        const validation = new Validator(data, this.validationRules)
        this.setState({ isFormValid: validation.passes() })
    }
    saveDraft = debounce((callback) => {
        const acteData = this.getActeData()
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${acteData.uuid}`, body: JSON.stringify(acteData), headers: headers, method: 'PUT', context: this.context })
            .then(checkStatus)
            .then(response => response.text())
            .then(acteUuid => {
                const fields = this.state.fields
                fields['uuid'] = acteUuid
                this.setState({ fields: fields, formStatus: 'saved' }, callback)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }, 3000)
    saveDraftFile = (file) => this.saveDraftAttachment(file, `/api/acte/drafts/${this.state.fields.uuid}/file`)
    saveDraftAnnexe = (file) => this.saveDraftAttachment(file, `/api/acte/drafts/${this.state.fields.uuid}/annexe`)
    saveDraftAttachment = (file, url) => {
        if (file) {
            this.setState({ formStatus: 'saving' })
            const data = new FormData()
            data.append('file', file)
            fetchWithAuthzHandling({ url: url, body: data, method: 'POST', context: this.context })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => {
                    // TODO: Work only Attachments without file content, so it can scale with the multiple acte form
                    this.loadActe(json)
                    this.setState({ formStatus: 'saved' }, this.validateForm)
                })
                .catch(response => {
                    response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
                    this.setState({ formStatus: '' }, this.validateForm)
                })
        }
    }
    deleteDraftFile = () => this.deleteDraftAttachment(`/api/acte/drafts/${this.state.fields.uuid}/file`)
    deleteDraftAnnexe = (annexeUuid) => this.deleteDraftAttachment(`/api/acte/drafts/${this.state.fields.uuid}/annexe/${annexeUuid}`, annexeUuid)
    deleteDraftAttachment = (url, annexeUuid) => {
        this.setState({ formStatus: 'saving' })
        fetchWithAuthzHandling({ url: url, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                const fields = this.state.fields
                if (annexeUuid) {
                    const annexes = this.state.fields.annexes.filter(annexe => annexe.uuid !== annexeUuid)
                    fields['annexes'] = annexes
                    this.setState({ fields: fields, formStatus: 'saved' }, this.validateForm)
                } else {
                    fields['acteAttachment'] = null
                    this.setState({ fields: fields, formStatus: 'saved' }, this.validateForm)
                }
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
                this.setState({ formStatus: '' })
            })
    }
    deteleDraft = () => {
        const fields = this.state.fields
        fields['draft'] = false
        this.setState({ fields })
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: '/api/acte/drafts', body: JSON.stringify([this.state.fields.uuid]), headers: headers, method: 'DELETE', context: this.context })
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
        if (this.state.formStatus !== 'saved') this.saveDraft(this.submitForm)
        else this.submitForm()
    }
    submitForm = () => {
        const fields = this.state.fields
        fields['draft'] = false
        this.setState({ fields })
        fetchWithAuthzHandling({ url: `/api/acte/drafts/${fields.uuid}`, method: 'POST', context: this.context })
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
        const isFormSending = this.state.formStatus === 'sending'
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        const codeOptions = Object.entries(this.state.codesMatieres).map(([key, value]) => {
            const object = { key: key, value: key, text: key + " - " + value }
            return object
        })
        const annexes = this.state.fields.annexes.map(annexe =>
            <File key={annexe.uuid} attachment={annexe} onDelete={this.deleteDraftAnnexe} />
        )
        const acceptFile = this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' ? ".xml" : ".pdf, .jpg, .png"
        const acceptAnnexes = this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' ? ".pdf, .jpg, .png" : ".pdf, .xml, .jpg, .png"
        return (
            <Segment>
                <Grid>
                    <Grid.Column width={10}><h1>{t('acte.new.title')}</h1></Grid.Column>
                    <Grid.Column width={6} style={{ textAlign: 'right' }}>
                        {renderIf(this.state.formStatus)(
                            <span style={{ fontStyle: 'italic' }}>{t(`acte.new.formStatus.${this.state.formStatus}`)}</span>
                        )}
                        {renderIf(this.state.fields.uuid)(
                            <Button style={{ marginLeft: '1em' }} onClick={this.deteleDraft} compact basic color='red' disabled={isFormSending} loading={isFormSending}>
                                {t('api-gateway:form.delete_draft')}
                            </Button>
                        )}
                    </Grid.Column>
                </Grid>
                <Menu tabular>
                    <Menu.Item id='newActe' active={this.state.mode === 'newActe'} onClick={this.handleModeChange}>
                        {t('acte.new.acte')}
                    </Menu.Item>
                    <Menu.Item id='newActeBudgetaire' active={this.state.mode === 'newActeBudgetaire'} onClick={this.handleModeChange}>
                        {t('acte.new.acte_budgetaire')}
                    </Menu.Item>
                </Menu>
                <Form onSubmit={this.saveAndSubmitForm}>
                    <FormField htmlFor='number' label={t('acte.fields.number')}>
                        <InputValidation id='number'
                            placeholder={t('acte.fields.number') + '...'}
                            value={this.state.fields.number}
                            onChange={this.handleFieldChange}
                            validationRule={this.validationRules.number}
                            fieldName={t('acte.fields.number')} />
                    </FormField>
                    <FormField htmlFor='objet' label={t('acte.fields.objet')}>
                        <InputValidation id='objet'
                            placeholder={t('acte.fields.objet') + '...'}
                            value={this.state.fields.objet}
                            onChange={this.handleFieldChange}
                            validationRule={this.validationRules.objet}
                            fieldName={t('acte.fields.objet')} />
                    </FormField>
                    <FormField htmlFor='decision' label={t('acte.fields.decision')}>
                        <InputValidation id='decision'
                            type='date'
                            placeholder='aaaa-mm-jj'
                            value={this.state.fields.decision}
                            onChange={this.handleFieldChange}
                            validationRule={this.validationRules.decision}
                            fieldName={t('acte.fields.decision')}
                            max={moment().format('YYYY-MM-DD')} />
                    </FormField>
                    {renderIf(this.state.mode === 'newActe')(
                        <FormField htmlFor='nature' label={t('acte.fields.nature')}>
                            <InputValidation id='nature'
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
                    <FormField htmlFor='code' label={t('acte.fields.code')}>
                        <InputValidation id='code'
                            type='dropdown'
                            value={this.state.fields.code}
                            onChange={this.handleFieldChange}
                            validationRule={this.validationRules.code}
                            fieldName={t('acte.fields.code')}
                            options={codeOptions} />
                    </FormField>
                    <FormField htmlFor='acteAttachment' label={t('acte.fields.acteAttachment')}>
                        <InputValidation id='acteAttachment'
                            type='file'
                            accept={acceptFile}
                            onChange={this.saveDraftFile}
                            value={this.state.fields.acteAttachment}
                            validationRule={this.validationRules.acteAttachment}
                            label={t('api-gateway:form.add_a_file')}
                            fieldName={t('acte.fields.acteAttachment')} />
                    </FormField>
                    {renderIf(this.state.fields.acteAttachment)(
                        <File attachment={this.state.fields.acteAttachment} onDelete={this.deleteDraftFile} />
                    )}
                    <FormField htmlFor='annexes' label={t('acte.fields.annexes')}>
                        <InputFile htmlFor='annexes' label={t('api-gateway:form.add_a_file')}>
                            <input type="file" id='annexes' accept={acceptAnnexes} onChange={e => this.saveDraftAnnexe(e.target.files[0])}
                                style={{ display: 'none' }} />
                        </InputFile>
                    </FormField>
                    {renderIf(this.state.fields.annexes.length > 0)(
                        <Card.Group>
                            {annexes}
                        </Card.Group>
                    )}
                    {renderIf(this.state.fields.nature !== 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS')(
                        <FormField htmlFor='public' label={t('acte.fields.public')}>
                            <Checkbox id='public' disabled={isPublicFieldDisabled} checked={this.state.fields.public} onChange={e => handleFieldCheckboxChange(this, 'public')} toggle />
                        </FormField>
                    )}
                    {renderIf(this.state.depositFields.publicWebsiteField && this.state.fields.nature !== 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS')(
                        <FormField htmlFor='publicWebsite' label={t('acte.fields.publicWebsite')}>
                            <Checkbox id='publicWebsite' checked={this.state.fields.publicWebsite} onChange={e => handleFieldCheckboxChange(this, 'publicWebsite')} toggle />
                        </FormField>
                    )}
                    <Button type='submit' disabled={!this.state.isFormValid || isFormSending} loading={isFormSending}>{t('api-gateway:form.submit')}</Button>
                </Form>
            </Segment>
        )
    }
}

export default translate(['acte', 'api-gateway'])(NewActe)