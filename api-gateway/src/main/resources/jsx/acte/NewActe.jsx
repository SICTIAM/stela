import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Form, Checkbox } from 'semantic-ui-react'

import { FormField } from '../_components/UI'
import { errorNotification, acteSentSuccess } from '../_components/Notifications'
import history from '../_util/history'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'

class NewActe extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        fields: {
            uuid: null,
            number: '',
            creation: null,
            decision: '',
            nature: '',
            lastUpdateTime: null,
            code: '',
            title: '',
            public: true,
            publicWebsite: true,
            status: null,
        },
        file: null,
        annexes: []
    }
    handleFileChange = (field, file) => {
        this.setState({ [field]: file })
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        fields[field] = value
        this.setState({ fields: fields })
    }
    handleCheckboxChange = (field) => {
        const fields = this.state.fields
        fields[field] = !fields[field]
        this.setState({ fields: fields })
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
        const natures = [
            "DELIBERATIONS",
            "ARRETES_REGLEMENTAIRES",
            "ARRETES_INDIVIDUELS",
            "CONTRATS_ET_CONVENTIONS",
            "DOCUMENTS_BUDGETAIRES_ET_FINANCIERS",
            "AUTRES"
        ]
        const natureOptions = natures.map(nature =>
            <option key={nature} value={nature}>{t(`acte.nature.${nature}`)}</option>
        )
        return (
            <div>
                <h1>{t('acte.new.title')}</h1>
                <Form onSubmit={this.submitForm}>
                    <FormField htmlFor='number' label={t('acte.fields.number')}>
                        <input id='number' placeholder='Numéro...' value={this.state.fields.number} onChange={e => this.handleFieldChange('number', e.target.value)} required />
                    </FormField>
                    <FormField htmlFor='title' label={t('acte.fields.title')}>
                        <input id='title' placeholder='Titre...' value={this.state.fields.title} onChange={e => this.handleFieldChange('title', e.target.value)} required />
                    </FormField>
                    <FormField htmlFor='decision' label={t('acte.fields.decision')}>
                        <input id='decision' type='date' placeholder='aaaa-mm-jj' value={this.state.fields.decision} onChange={e => this.handleFieldChange('decision', e.target.value)} required />
                    </FormField>
                    <FormField htmlFor='nature' label={t('acte.fields.nature')}>
                        <select id='nature' value={this.state.fields.nature} onChange={e => this.handleFieldChange('nature', e.target.value)} required>
                            <option value='' disabled>{t('acte.new.choose')}</option>
                            {natureOptions}
                        </select>
                    </FormField>
                    <FormField htmlFor='code' label={t('acte.fields.code')}>
                        <input id='code' placeholder='Code matière...' value={this.state.fields.code} onChange={e => this.handleFieldChange('code', e.target.value)} required />
                    </FormField>
                    <FormField htmlFor='file' label={t('acte.fields.file')}>
                        <input type="file" id='file' onChange={e => this.handleFileChange('file', e.target.files[0])} required />
                    </FormField>
                    <FormField htmlFor='annexes' label={t('acte.fields.annexes')}>
                        <input type="file" id='annexes' onChange={e => this.handleFileChange('annexes', e.target.files)} multiple />
                    </FormField>
                    <FormField htmlFor='public' label={t('acte.fields.public')}>
                        <Checkbox id='public' checked={this.state.fields.public} onChange={e => this.handleCheckboxChange('public')} toggle />
                    </FormField>
                    <FormField htmlFor='publicWebsite' label={t('acte.fields.publicWebsite')}>
                        <Checkbox id='publicWebsite' checked={this.state.fields.publicWebsite} onChange={e => this.handleCheckboxChange('publicWebsite')} toggle />
                    </FormField>
                    <Button type='submit'>{t('acte.new.submit')}</Button>
                </Form>
            </div>
        )
    }
}

export default translate(['api-gateway'])(NewActe)