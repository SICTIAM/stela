import React, { Component } from 'react'
import PropTypes from 'prop-types'
import renderIf from 'render-if'
import { translate } from 'react-i18next'
import { Checkbox, Segment } from 'semantic-ui-react'

import { errorNotification } from '../../_components/Notifications'
import InputButton from '../../_components/InputButton'
import { Field } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class LocalAuthority extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        localAuthority: {},
        localAuthorityFetched: false
    }
    componentDidMount() {
        const uuid = this.props.uuid
        if (uuid !== '') {
            fetchWithAuthzHandling({ url: '/api/acte/localAuthority/' + uuid })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => {
                    this.setState({ localAuthority: json, localAuthorityFetched: true })
                })
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                    })
                    history.push('/admin/actes/parametrage-collectivite')
                })
        }
    }
    handleCheckboxChange = (field) => {
        const newValue = !this.state.localAuthority[field]
        this.updateChange(field, newValue)
    }
    handleChange = (e) => {
        const { id, value } = e.target
        this.updateChange(id, value)
    }
    updateChange = (field, newValue) => {
        const uuid = this.state.localAuthority.uuid
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }

        fetchWithAuthzHandling({ url: '/api/acte/localAuthority/' + uuid, body: JSON.stringify({ [field]: newValue }), method: 'PATCH', headers: headers, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(value => {
                const localAuthority = this.state.localAuthority
                let newFieldValue = value[field]
                localAuthority[field] = newFieldValue
                this.setState({ localAuthority: localAuthority })
            })
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(text))))
            })
    }
    render() {
        const { t } = this.context
        const localAuthorityFetched = renderIf(this.state.localAuthorityFetched)
        return (
            <div>
                {localAuthorityFetched(
                    <Segment>
                        <h1>{this.state.localAuthority.name}</h1>

                        <h2>{t('admin.modules.acte.local_authority_settings.general_informations')}</h2>

                        <Field htmlFor="uuid" label={t('local_authority.uuid')}>
                            <span id="uuid">{this.state.localAuthority.uuid}</span>
                        </Field>
                        <Field htmlFor="siren" label={t('local_authority.siren')}>
                            <span id="siren">{this.state.localAuthority.siren}</span>
                        </Field>
                        <Field htmlFor="department" label={t('local_authority.department')}>
                            <InputButton id="department"
                                value={this.state.localAuthority.department}
                                handleChange={this.handleInputChange}
                                validateInput={this.updateChange} />
                        </Field>
                        <Field htmlFor="district" label={t('local_authority.district')}>
                            <InputButton id="district"
                                value={this.state.localAuthority.district}
                                handleChange={this.handleInputChange}
                                validateInput={this.updateChange} />
                        </Field>
                        <Field htmlFor="nature" label={t('local_authority.nature')}>
                            <InputButton id="nature"
                                value={this.state.localAuthority.nature}
                                handleChange={this.handleInputChange}
                                validateInput={this.updateChange} />
                        </Field>
                        <Field htmlFor="nomenclatureDate" label={t('local_authority.nomenclatureDate')}>
                            <span id="nomenclatureDate">{this.state.localAuthority.nomenclatureDate}</span>
                        </Field>
                        <Field htmlFor="canPublishRegistre" label={t('local_authority.canPublishRegistre')}>
                            <Checkbox id="canPublishRegistre" toggle checked={this.state.localAuthority.canPublishRegistre} onChange={e => this.handleCheckboxChange('canPublishRegistre')} />
                        </Field>
                        <Field htmlFor="canPublishWebSite" label={t('local_authority.canPublishWebSite')}>
                            <Checkbox id="canPublishWebSite" toggle checked={this.state.localAuthority.canPublishWebSite} onChange={e => this.handleCheckboxChange('canPublishWebSite')} />
                        </Field>
                    </Segment>
                )}
            </div>
        )
    }
}

export default translate(['api-gateway'])(LocalAuthority)