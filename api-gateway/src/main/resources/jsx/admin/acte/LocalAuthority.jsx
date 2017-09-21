import React, { Component } from 'react'
import PropTypes from 'prop-types'
import renderIf from 'render-if'
import { translate } from 'react-i18next'
import { Checkbox, Form, Button } from 'semantic-ui-react'

import { errorNotification, localAuthorityUpdateSuccess } from '../../_components/Notifications'
import { Field } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling, handleFieldCheckboxChange, handleFieldChange } from '../../_util/utils'

class LocalAuthority extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        constantFields: {
            uuid: '',
            name: '',
            siren: '',
            nomenclatureDate: null
        },
        fields: {
            department: '',
            district: '',
            nature: '',
            canPublishRegistre: false,
            canPublishWebSite: false
        },
        localAuthorityFetched: false
    }
    style = {
        marginTop: 1 + 'em'
    }
    componentDidMount() {
        const uuid = this.props.uuid
        if (uuid !== '') {
            fetchWithAuthzHandling({ url: '/api/acte/localAuthority/' + uuid })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.updateState(json))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                    })
                    history.push('/admin/actes/parametrage-collectivite')
                })
        }
    }
    updateState = (json) => {
        const { uuid, name, siren, department, district, nature, nomenclatureDate, canPublishRegistre, canPublishWebSite } = json
        const constantFields = Object.assign({}, { uuid, name, siren, nomenclatureDate })
        const fields = Object.assign({}, { department, district, nature, canPublishRegistre, canPublishWebSite })
        this.setState({ constantFields: constantFields, fields: fields, localAuthorityFetched: true })
    }
    submitForm = (event) => {
        event.preventDefault()
        const data = JSON.stringify(this.state.fields)
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        fetchWithAuthzHandling({ url: '/api/acte/localAuthority/' + this.state.constantFields.uuid, method: 'PUT', body: data, headers: headers, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                this.context._addNotification(localAuthorityUpdateSuccess(this.context.t))
                this.updateState(json)
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    render() {
        const { t } = this.context
        const localAuthorityFetched = renderIf(this.state.localAuthorityFetched)
        return (
            localAuthorityFetched(
                <div>
                    <h1>{this.state.constantFields.name}</h1>

                    <h2>{t('admin.modules.acte.local_authority_settings.general_informations')}</h2>

                    <Form onSubmit={this.submitForm}>
                        <Field htmlFor="uuid" label={t('local_authority.uuid')}>
                            <span id="uuid">{this.state.constantFields.uuid}</span>
                        </Field>
                        <Field htmlFor="siren" label={t('local_authority.siren')}>
                            <span id="siren">{this.state.constantFields.siren}</span>
                        </Field>

                        <Field htmlFor="department" label={t('local_authority.department')}>
                            <Form.Input id='department' value={this.state.fields.department} onChange={e => handleFieldChange(this, e)} width={5} />
                        </Field>

                        <Field htmlFor="district" label={t('local_authority.district')}>
                            <Form.Input id='district' value={this.state.fields.district} onChange={e => handleFieldChange(this, e)} width={5} />
                        </Field>

                        <Field htmlFor="nature" label={t('local_authority.nature')}>
                            <Form.Input id='nature' value={this.state.fields.nature} onChange={e => handleFieldChange(this, e)} width={5} />
                        </Field>
                        <Field htmlFor="nomenclatureDate" label={t('local_authority.nomenclatureDate')}>
                            <span id="nomenclatureDate">{this.state.constantFields.nomenclatureDate}</span>
                        </Field>
                        <Field htmlFor="canPublishRegistre" label={t('local_authority.canPublishRegistre')}>
                            <Checkbox id="canPublishRegistre" toggle checked={this.state.fields.canPublishRegistre} onChange={e => handleFieldCheckboxChange(this, 'canPublishRegistre')} />
                        </Field>
                        <Field htmlFor='canPublishWebSite' label={t('local_authority.canPublishWebSite')}>
                            <Checkbox id="canPublishWebSite" toggle checked={this.state.fields.canPublishWebSite} onChange={e => handleFieldCheckboxChange(this, 'canPublishWebSite')} />
                        </Field>
                        <Button style={this.style} type='submit'>{t('form.update')}</Button>
                    </Form>
                </div>
            )
        )
    }
}

export default translate(['api-gateway'])(LocalAuthority)