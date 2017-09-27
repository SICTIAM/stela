import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Button, Form, Checkbox, Menu } from 'semantic-ui-react'
import Validator from 'validatorjs'

import { FormField } from '../_components/UI'
import InputValidation from '../_components/InputValidation'
import { errorNotification, acteSentSuccess } from '../_components/Notifications'
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
            creation: null,
            decision: '',
            nature: '',
            code: '',
            objet: '',
            public: false,
            publicWebsite: false
        },
        depositFields: {
            publicField: false,
            publicWebsiteField: false
        },
        codesMatieres: [],
        file: null,
        annexes: [],
        isFormValid: false
    }
    validationRules = {
        number: ['required', 'max:15', 'regex:/^[a-zA-Z0-9_]+$/'],
        objet: 'required|max:500',
        nature: 'required',
        code: 'required',
        decision: ['required', 'date'],
        file: 'required'
    }
    customErrorMessages = {
        regex: this.context.t('form.validation.regex_alpha_num_underscore', { fieldName: this.context.t('acte.fields.number') })
    }
    componentDidMount() {
        fetch('/api/acte/localAuthority/depositFields', { credentials: 'same-origin' })
            .then(this.checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ depositFields: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })

        fetch('/api/acte/localAuthority/codes-matieres', { credentials: 'same-origin' })
            .then(this.checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ codesMatieres: json }))
            .catch(response => {
                debugger;
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                })
            })
    }
    handleFileChange = (field, file) => {
        this.setState({ [field]: file }, this.validateForm)
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        if (field === 'nature' && value === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS') {
            fields['public'] = false
            fields['publicWebsite'] = false
        }
        this.setState({ fields: fields }, this.validateForm)
    }
    handleModeChange = (e, { id }) => {
        const fields = this.state.fields
        if (id === 'newActeBudgetaire') {
            fields['nature'] = 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS'
            fields['public'] = false
            fields['publicWebsite'] = false
        }
        else fields['nature'] = ''
        this.setState({ mode: id, fields: fields })
    }
    validateForm = () => {
        const data = {
            number: this.state.fields.number,
            objet: this.state.fields.objet,
            nature: this.state.fields.nature,
            decision: this.state.fields.decision,
            file: this.state.file,
            code: this.state.fields.code
        }
        const validation = new Validator(data, this.validationRules)
        this.setState({ isFormValid: validation.passes() })
    }
    submitForm = (event) => {
        event.preventDefault()
        const data = new FormData()
        data.append('acte', JSON.stringify(this.state.fields))
        data.append('file', this.state.file)
        const annexesList = [...this.state.annexes]
        annexesList.map(annexe => data.append('annexes', annexe))

        fetchWithAuthzHandling({ url: '/api/acte', method: 'POST', body: data, context: this.context })
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
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        const codeOptions = Object.entries(this.state.codesMatieres).map(([key, value]) => {
            const object = {key: key, value: key, text: key + " - " + value}
            return object
        })
        const acceptFile = this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' ? ".xml" : ".pdf, .jpg, .png"
        const acceptAnnexes = this.state.fields.nature === 'DOCUMENTS_BUDGETAIRES_ET_FINANCIERS' ? ".pdf, .jpg, .png" : ".pdf, .xml, .jpg, .png"
        return (
            <div>
                <h1>{t('acte.new.title')}</h1>
                <Menu tabular>
                    <Menu.Item id='newActe' active={this.state.mode === 'newActe'} onClick={this.handleModeChange}>
                        {t('acte.new.acte')}
                    </Menu.Item>
                    <Menu.Item id='newActeBudgetaire' active={this.state.mode === 'newActeBudgetaire'} onClick={this.handleModeChange}>
                        {t('acte.new.acte_budgetaire')}
                    </Menu.Item>
                </Menu>
                <Form onSubmit={this.submitForm}>
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
                    <FormField htmlFor='file' label={t('acte.fields.file')}>
                        <InputValidation id='file'
                            type='file'
                            value={this.state.file}
                            accept={acceptFile}
                            onChange={this.handleFileChange}
                            validationRule={this.validationRules.code}
                            fieldName={t('acte.fields.file')} />
                    </FormField>
                    <FormField htmlFor='annexes' label={t('acte.fields.annexes')}>
                        <input type="file" id='annexes' accept={acceptAnnexes} onChange={e => this.handleFileChange('annexes', e.target.files)} multiple />
                    </FormField>
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
                    <Button type='submit' disabled={!this.state.isFormValid}>{t('form.submit')}</Button>
                </Form>
            </div>
        )
    }
}

export default translate(['api-gateway'])(NewActe)