import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Form, Checkbox } from 'semantic-ui-react'
import { errorNotification, acteSentSuccess } from '../_components/Notifications'

import history from '../_util/history'

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
    checkStatus = (response) => {
        if (response.status >= 200 && response.status < 300) {
            return response
        } else {
            throw response
        }
    }
    submitForm = (event) => {
        event.preventDefault()
        const data = new FormData()
        data.append('acte', JSON.stringify(this.state.fields))
        data.append('file', this.state.file)
        const annexesList = [...this.state.annexes]
        annexesList.map(annexe => data.append('annexes', annexe))

        fetch('/api/acte', {
            credentials: 'same-origin',
            headers: {
                [this.context.csrfTokenHeaderName]: this.context.csrfToken
            },
            method: 'POST',
            body: data
        })
            .then(this.checkStatus)
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
                    <Form.Field>
                        <label htmlFor='number'>{t('acte.fields.number')}</label>
                        <input id='number' placeholder='Numéro...' value={this.state.fields.number} onChange={e => this.handleFieldChange('number', e.target.value)} required />
                    </Form.Field>
                    <Form.Field>
                        <label htmlFor='title'>{t('acte.fields.title')}</label>
                        <input id='title' placeholder='Titre...' value={this.state.fields.title} onChange={e => this.handleFieldChange('title', e.target.value)} required />
                    </Form.Field>
                    <Form.Field>
                        <label htmlFor='decision'>{t('acte.fields.decision')}</label>
                        <input id='decision' type='date' placeholder='aaaa-mm-jj' value={this.state.fields.decision} onChange={e => this.handleFieldChange('decision', e.target.value)} required />
                    </Form.Field>
                    <Form.Field>
                        <label htmlFor='nature'>{t('acte.fields.nature')}</label>
                        <select id='nature' value={this.state.fields.nature} onChange={e => this.handleFieldChange('nature', e.target.value)} required>
                            <option value='' disabled>{t('acte.new.choose')}</option>
                            {natureOptions}
                        </select>
                    </Form.Field>
                    <Form.Field>
                        <label htmlFor='code'>{t('acte.fields.code')}</label>
                        <input id='code' placeholder='Code matière...' value={this.state.fields.code} onChange={e => this.handleFieldChange('code', e.target.value)} required />
                    </Form.Field>
                    <Form.Field>
                        <label htmlFor='file'>{t('acte.fields.file')}</label>
                        <input type="file" id='file' onChange={e => this.handleFileChange('file', e.target.files[0])} required />
                    </Form.Field>
                    <Form.Field>
                        <label htmlFor='annexes'>{t('acte.fields.annexes')}</label>
                        <input type="file" id='annexes' onChange={e => this.handleFileChange('annexes', e.target.files)} multiple />
                    </Form.Field>
                    <Form.Field>
                        <label htmlFor='public'>{t('acte.fields.public')}</label>
                        <Checkbox id='public' checked={this.state.fields.public} onChange={e => this.handleCheckboxChange('public')} toggle />
                    </Form.Field>
                    <Button type='submit'>{t('acte.new.submit')}</Button>
                </Form>
            </div>
        )
    }
}

export default translate(['api-gateway'])(NewActe)