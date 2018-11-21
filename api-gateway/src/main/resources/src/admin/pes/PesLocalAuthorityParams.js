import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Form, Button, Segment, Label, Icon, Dropdown, Input, Checkbox } from 'semantic-ui-react'
import Validator from 'validatorjs'

import { notifications } from '../../_util/Notifications'
import { Field, Page } from '../../_components/UI'
import { checkStatus, handleFieldCheckboxChange, updateField } from '../../_util/utils'

class PesLocalAuthorityParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        fields: {
            uuid: '',
            name: '',
            siren: '',
            serverCode: '',
            token: '',
            secret: '',
            genericProfileUuid: '',
            sesileSubscription: false,
            sesileNewVersion: false,
            sirens: [],
            archiveSettings: {
                archiveActivated: false,
                pastellUrl: '',
                daysBeforeArchiving: '',
                pastellEntity: '',
                pastellLogin: '',
                pastellPassword: ''
            }
        },
        serverCodes: [],
        profiles: [],
        newSiren: '',
        isNewSirenValid: false
    }
    componentDidMount() {
        const uuid = this.props.uuid
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const adminUrl = uuid ? `/api/admin/local-authority/${uuid}` : '/api/admin/local-authority/current'
        _fetchWithAuthzHandling({ url: adminUrl })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                this.setState({ profiles: json.profiles })
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })

        const url = uuid ? '/api/pes/localAuthority/' + uuid : '/api/pes/localAuthority/current'
        _fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.loadData(json))
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.pes.title', json.message)
                })
            })
        _fetchWithAuthzHandling({ url: '/api/pes/localAuthority/server-codes' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ serverCodes: json }))
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.pes.title', json.message)
                })
            })

    }
    loadData = (fields) => {
        if (fields.archiveSettings === null) {
            fields.archiveSettings = {
                archiveActivated: false,
                pastellUrl: '',
                daysBeforeArchiving: '',
                pastellEntity: '',
                pastellLogin: '',
                pastellPassword: ''
            }
        }
        this.setState({ fields })
    }
    onkeyPress = (event) => {
        // prevent from sending the form on 'Enter'
        if (event.key === 'Enter') this.addSiren(event)
    }
    addSiren = (event) => {
        event.preventDefault()
        const { fields, newSiren } = this.state
        if (this.validateSiren(newSiren)) {
            fields.sirens.push(newSiren)
            this.setState({ newSiren: '', fields: fields })
        }
    }
    onRemoveSiren = (siren) => {
        const fields = this.state.fields
        const index = fields.sirens.indexOf(siren)
        fields.sirens.splice(index, 1)
        this.setState({ fields: fields })
    }
    handleServerCodeChange = (e, { value }) => {
        const fields = this.state.fields
        fields.serverCode = value
        this.setState({ fields: fields })
    }
    handleFieldChange = (field, value) => {
        const fields = this.state.fields
        updateField(fields, field, value)
        this.setState({ fields: fields }, this.validateForm)
    }
    sesileSubscriptionChange = (checked) => {
        const fields = this.state.fields
        fields.sesileSubscription = checked
        this.setState({ fields: fields })
    }
    sesileConfigurationChange = (e, { id, value }) => {
        const fields = this.state.fields
        fields[id] = value
        this.setState({ fields: fields })
    }
    validateSiren = (siren) => {
        const validation = new Validator({ siren: siren.replace(/\s/g, '') }, { siren: 'required|digits:9' })
        return validation.passes()
    }
    handleNewSirenChange = (value) => {
        this.setState({ newSiren: value, isNewSirenValid: this.validateSiren(value) })
    }
    submitForm = (event) => {
        event.preventDefault()
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        // TODO: Improve code quality
        const { serverCode, sirens, secret, token, sesileSubscription, sesileNewVersion, genericProfileUuid, archiveSettings } = this.state.fields

        const data = JSON.stringify({ serverCode, token, secret, sesileSubscription, sesileNewVersion, genericProfileUuid, archiveSettings,
            sirens: sirens.map(siren => siren.replace(/\s/g, '')) })
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        const url = `/api/pes/localAuthority/${this.state.fields.uuid}`
        _fetchWithAuthzHandling({ url, method: 'PATCH', body: data, headers: headers, context: this.context })
            .then(checkStatus)
            .then(() => _addNotification(notifications.admin.localAuthorityPesUpdate))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    verifyTokens = e => {
        e.preventDefault()
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const data = new FormData()
        data.append('token', this.state.fields.token)
        data.append('secret', this.state.fields.secret)
        data.append('sesileNewVersion', this.state.fields.sesileNewVersion)
        _fetchWithAuthzHandling({ url: '/api/pes/sesile/verify-tokens', method: 'POST', body: data, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(tokenValid => _addNotification(tokenValid ? notifications.admin.sesileValidTokens : notifications.admin.sesileInvalidTokens))
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    render() {
        const { t } = this.context
        const listSiren = this.state.fields.sirens.map((siren, index) =>
            <Label basic key={index}>{siren} <Icon name='delete' onClick={() => this.onRemoveSiren(siren)} /></Label>
        )
        const serverCodes = this.state.serverCodes.map(serverCode => { return { key: serverCode, value: serverCode, text: serverCode } })
        const profiles = this.state.profiles.map(profile => {
            return {
                key: profile.uuid,
                value: profile.uuid,
                text: `${profile.agent.given_name} ${profile.agent.family_name}`
            }
        })
        return (
            <Page title={this.state.fields.name} >
                <Segment>
                    <Form onSubmit={this.submitForm}>

                        <h2 className='secondary'>{t('admin.modules.pes.local_authority_settings.title')}</h2>
                        <Field htmlFor='serverCode' label={t('admin.modules.pes.local_authority_settings.serverCode')}>
                            <Dropdown compact search selection
                                id='serverCode'
                                className='simpleInput'
                                options={serverCodes}
                                value={this.state.fields.serverCode}
                                onChange={this.handleServerCodeChange}
                                placeholder={`${t('admin.modules.pes.local_authority_settings.serverCode')}...`} />
                        </Field>
                        <Field htmlFor='sirens' label={t('admin.modules.pes.local_authority_settings.sirens')}>
                            <div style={{ marginBottom: '0.5em' }}>
                                {listSiren.length > 0 ? listSiren : t('admin.modules.pes.local_authority_settings.no_siren')}
                            </div>
                            <input id='sirens'
                                onKeyPress={this.onkeyPress}
                                value={this.state.newSiren}
                                onChange={(e) => this.handleNewSirenChange(e.target.value)}
                                className='simpleInput' />
                            <Button basic color='grey' style={{ marginLeft: '1em' }} onClick={(event) => this.addSiren(event)}>
                                {t('api-gateway:form.add')}
                            </Button>
                        </Field>

                        <h2 className='secondary'>{t('admin.modules.pes.local_authority_settings.paull_parameters')}</h2>
                        <Field htmlFor='genericProfileUuid' label={t('admin.modules.pes.local_authority_settings.genericProfileUuid')}>
                            <Dropdown compact search selection
                                id='genericProfileUuid'
                                className='simpleInput'
                                options={profiles}
                                value={this.state.fields.genericProfileUuid}
                                onChange={this.sesileConfigurationChange}
                                placeholder={`${t('admin.modules.pes.local_authority_settings.genericProfileUuid')}...`} />
                        </Field>
                        <Field htmlFor='sesileSubscription' label={t('admin.modules.pes.local_authority_settings.sesile.subscription')}>
                            <Checkbox toggle id='sesileSubscription'
                                checked={this.state.fields.sesileSubscription}
                                onChange={((e, { checked }) => this.sesileSubscriptionChange(checked))} />
                        </Field>
                        {(this.state.fields.sesileSubscription) && (
                            <Fragment>
                                <Field htmlFor='sesileNewVersion' label={t('admin.modules.pes.local_authority_settings.sesile.newVersion')}>
                                    <Checkbox toggle id='sesileNewVersion'
                                        checked={this.state.fields.sesileNewVersion}
                                        onChange={e => handleFieldCheckboxChange(this, 'sesileNewVersion')} />
                                </Field>
                                <Field htmlFor='token' label={t('admin.modules.pes.local_authority_settings.sesile.token')}>
                                    <Input id='token' style={{ width: '25em' }}
                                        placeholder={t('admin.modules.pes.local_authority_settings.sesile.token')}
                                        value={this.state.fields.token}
                                        required={this.state.fields.sesileSubscription}
                                        onChange={this.sesileConfigurationChange} />
                                </Field>
                                <Field htmlFor='secret' label={t('admin.modules.pes.local_authority_settings.sesile.secret')}>
                                    <Input id='secret' style={{ width: '25em' }}
                                        placeholder={t('admin.modules.pes.local_authority_settings.sesile.secret')}
                                        value={this.state.fields.secret}
                                        required={this.state.fields.sesileSubscription}
                                        onChange={this.sesileConfigurationChange} />
                                </Field>
                                <Field htmlFor='verifyTokens' label={t('admin.modules.pes.local_authority_settings.sesile.verifyTokens')}>
                                    <Button id='verifyTokens' basic color='grey' onClick={this.verifyTokens}>
                                        {t('api-gateway:form.verify')}
                                    </Button>
                                </Field>
                            </Fragment>
                        )}

                        <h2 className='secondary'>{t('admin.modules.pes.local_authority_settings.archive_parameters')}</h2>
                        <Field htmlFor="archiveActivated" label={t('api-gateway:local_authority.archiveActivated')}>
                            <Checkbox id="archiveActivated" toggle checked={this.state.fields.archiveSettings.archiveActivated}
                                onChange={e => handleFieldCheckboxChange(this, 'archiveSettings.archiveActivated')} />
                        </Field>
                        {this.state.fields.archiveSettings.archiveActivated && (
                            <Fragment>
                                <Field htmlFor='daysBeforeArchiving' label={t('api-gateway:local_authority.daysBeforeArchiving')}>
                                    <Input id='daysBeforeArchiving'
                                        type='number'
                                        value={this.state.fields.archiveSettings.daysBeforeArchiving || ''}
                                        onChange={(e, data) => this.handleFieldChange('archiveSettings.daysBeforeArchiving', data.value)} />
                                </Field>
                                <Field htmlFor='pastellUrl' label={t('api-gateway:local_authority.pastellUrl')}>
                                    <Input id='pastellUrl' fluid
                                        placeholder='https://...'
                                        value={this.state.fields.archiveSettings.pastellUrl || ''}
                                        onChange={(e, data) => this.handleFieldChange('archiveSettings.pastellUrl', data.value)} />
                                </Field>
                                <Field htmlFor='pastellEntity' label={t('api-gateway:local_authority.pastellEntity')}>
                                    <Input id='pastellEntity'
                                        value={this.state.fields.archiveSettings.pastellEntity || ''}
                                        onChange={(e, data) => this.handleFieldChange('archiveSettings.pastellEntity', data.value)} />
                                </Field>
                                <Field htmlFor='pastellLogin' label={t('api-gateway:local_authority.pastellLogin')}>
                                    <Input id='pastellLogin'
                                        value={this.state.fields.archiveSettings.pastellLogin || ''}
                                        onChange={(e, data) => this.handleFieldChange('archiveSettings.pastellLogin', data.value)} />
                                </Field>
                                <Field htmlFor='pastellPassword' label={t('api-gateway:local_authority.pastellPassword')}>
                                    <Input id='pastellPassword'
                                        type='password'
                                        value={this.state.fields.archiveSettings.pastellPassword || ''}
                                        onChange={(e, data) => this.handleFieldChange('archiveSettings.pastellPassword', data.value)} />
                                </Field>
                            </Fragment>
                        )}
                        <div style={{ textAlign: 'right' }}>
                            <Button basic primary type='submit'>{t('api-gateway:form.update')}</Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(PesLocalAuthorityParams)