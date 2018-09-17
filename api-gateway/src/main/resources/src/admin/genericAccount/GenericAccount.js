import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Label, Icon, Dropdown, Form, Button, Checkbox } from 'semantic-ui-react'

import history from '../../_util/history'
import { Field, Page } from '../../_components/UI'
import ConfirmModal from '../../_components/ConfirmModal'
import InputPassword from '../../_components/InputPassword'
import { notifications } from '../../_util/Notifications'
import { checkStatus, getLocalAuthoritySlug } from '../../_util/utils'

class GenericAccountCreation extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    static defaultProps = {
        uuid: ''
    }
    state = {
        fields: {
            software: '',
            email: '',
            password: '',
            serial: '',
            vendor: '',
            localAuthorities: [],
        },
        keepPassword: true,
        allLocalAuthorities: [],
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        if(this.props.uuid) {
            _fetchWithAuthzHandling({ url: `/api/admin/generic_account/${this.props.uuid}` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => {
                    json.password = ''
                    this.setState({ fields: json })
                })
                .catch(response => {
                    response.text().then(text => _addNotification(notifications.defaultError, 'notifications.admin.title', text))
                })
        }
        _fetchWithAuthzHandling({ url: '/api/admin/local-authority/all' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => { this.setState({ allLocalAuthorities: json }) })
    }
    submitForm = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const genericAccount = this.state.fields
        const localAuthoritySlug = getLocalAuthoritySlug()
        genericAccount.localAuthoritySirens = genericAccount.localAuthorities.map(localAuthority => localAuthority.uuid)
        delete genericAccount.localAuthorities

        const body = JSON.stringify(genericAccount)
        const headers = { 'Content-Type': 'application/json' }
        const method = this.props.uuid ? 'PUT' : 'POST'
        const url = `/api/admin/generic_account${this.props.uuid && `/${this.props.uuid}`}`
        _fetchWithAuthzHandling({ url, method, body, headers, context: this.context })
            .then(checkStatus)
            .then(() => {
                _addNotification(this.props.uuid ? notifications.admin.generic_account_updated : notifications.admin.generic_account_created)
                history.push(`/${localAuthoritySlug}/admin/compte-generique/liste`)
            })
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.admin.title', text))
            })
    }

    handleChange = (event, { value }) => {
        const { allLocalAuthorities, fields } = this.state
        const localAuthority = allLocalAuthorities.find(localAuthority => localAuthority.uuid === value)
        fields.localAuthorities.push(localAuthority)
        this.setState({ fields })
    }
    removeGroup = (uuid) => {
        const { fields } = this.state
        fields.localAuthorities = fields.localAuthorities.filter(localAuthority => localAuthority.uuid !== uuid)
        this.setState({ fields })
    }
    handleFieldChange = (field, value) => {
        const { fields } = this.state
        fields[field] = value
        this.setState({ fields: fields })
    }
    toggleKeepPassword = () => {
        const keepPassword = !this.state.keepPassword
        const fields = this.state.fields
        fields.password = ''
        this.setState({ fields, keepPassword })
    }
    delete = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const localAuthoritySlug = getLocalAuthoritySlug()
        if(this.props.uuid) {
            _fetchWithAuthzHandling({ url:`/api/admin/generic_account/${this.props.uuid}`, method: 'DELETE', context: this.context })
                .then(checkStatus)
                .then(() => {
                    _addNotification(notifications.admin.generic_account_deleted)
                    history.push(`/${localAuthoritySlug}/admin/compte-generique/liste`)
                })
                .catch(response => {
                    response.text().then(text => _addNotification(notifications.defaultError, 'notifications.admin.title', text))
                })
        }
    }
    render() {
        const { t } = this.context
        const genericAccount = this.state.fields
        const localAuthorities = this.state.fields.localAuthorities.map(localAuthority =>
            <Label basic key={localAuthority.uuid}>{localAuthority.name} <Icon name='delete' onClick={() => this.removeGroup(localAuthority.uuid)} /></Label>
        )
        const localAuthorityOptions = this.state.allLocalAuthorities
            .filter(localAuthority => !this.state.fields.localAuthorities.find(profileGroup => profileGroup.uuid === localAuthority.uuid))
            .map(localAuthority => {
                return { key: localAuthority.uuid, value: localAuthority.uuid, text: localAuthority.name }
            })
        return (
            <Page title={t('admin.generic_account.new.title')} >
                <Segment>
                    <Form>
                        <Field htmlFor='software' label={t('admin.generic_account.fields.software')}>
                            <input id='software' required value={genericAccount.software} onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>
                        <Field htmlFor='email' label={t('admin.generic_account.fields.email')}>
                            <input id='email' required value={genericAccount.email} onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>
                        <Field htmlFor='password' label={t('admin.generic_account.fields.password')}>
                            <InputPassword id='password' value={this.state.fields.password} disabled={!!this.props.uuid && this.state.keepPassword} fluid required onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                            {this.props.uuid &&
                                <Checkbox style={{marginTop: '0.5em'}} checked={this.state.keepPassword} onChange={this.toggleKeepPassword} label={t('admin.generic_account.keep_password')} />
                            }
                        </Field>
                        <Field htmlFor='serial' label={t('admin.generic_account.fields.serial')}>
                            <input id='serial' value={genericAccount.serial} onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>
                        <Field htmlFor='vendor' label={t('admin.generic_account.fields.vendor')}>
                            <input id='vendor' value={genericAccount.vendor} onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>

                        <Field htmlFor='localAuthorities' label={t('admin.generic_account.fields.localAuthorities')}>
                            <div style={{ marginBottom: '0.5em' }}>{localAuthorities.length > 0 ? localAuthorities : t('admin.generic_account.new.no_localAuthorities')}</div>
                            <div style={{ marginBottom: '1em' }}>
                                <Dropdown id='localAuthorities' value='' placeholder={t('admin.generic_account.new.add_localAuthority')} fluid selection options={localAuthorityOptions} onChange={this.handleChange} />
                            </div>
                        </Field>
                        <div style={{ textAlign: 'right' }}>
                            {this.props.uuid &&
                                <ConfirmModal onConfirm={this.delete} text={t('admin.generic_account.delete_confirm')}>
                                    <Button basic color={'red'}>{t('api-gateway:form.delete')}</Button>
                                </ConfirmModal>
                            }
                            <Button basic primary onClick={this.submitForm} type='submit'>{t(`api-gateway:form.${this.props.uuid ? 'update' : 'create'}`)}</Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(GenericAccountCreation)