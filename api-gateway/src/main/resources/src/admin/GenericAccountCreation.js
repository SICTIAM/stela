import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Label, Icon, Dropdown, Form, Button } from 'semantic-ui-react'
import history from '../_util/history'

import { Field, Page } from '../_components/UI'
import { notifications } from '../_util/Notifications'
import { checkStatus } from '../_util/utils'

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
        allLocalAuthorities: [],
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/local-authority/all' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => { this.setState({ allLocalAuthorities: json }) })
    }
    submitForm = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const genericAccount = this.state.fields
        genericAccount.localAuthorities = genericAccount.localAuthorities.map(localAuthority => localAuthority.uuid)

        const body = JSON.stringify(genericAccount)
        const headers = { 'Content-Type': 'application/json' }
        _fetchWithAuthzHandling({ url: '/api/admin/generic_account', method: 'POST', body, headers, context: this.context })
            .then(checkStatus)
            .then(() => {
                _addNotification(notifications.admin.generic_account_created)
                history.push('/admin/ma-collectivite')
            }
            )
            .catch(response => {
                response.text().then(text => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', text)
                })
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

    render() {
        const { t } = this.context
        const localAuthorities = this.state.fields.localAuthorities.map(localAuthority =>
            <Label basic key={localAuthority.uuid}>{localAuthority.name} <Icon name='delete' onClick={() => this.removeGroup(localAuthority.uuid)} /></Label>
        )
        const localAuthorityOptions = this.state.allLocalAuthorities
            .filter(localAuthority => !this.state.fields.localAuthorities.find(profileGroup => profileGroup.uuid === localAuthority.uuid))
            .map(localAuthority => {
                return { key: localAuthority.uuid, value: localAuthority.uuid, text: localAuthority.name }
            })
        return (
            <Page title={t('admin.generic_account.title')} >
                <Segment>
                    <Form>
                        <Field htmlFor='software' label={t('admin.generic_account.software')}>
                            <input id='software' required onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>
                        <Field htmlFor='email' label={t('admin.generic_account.email')}>
                            <input id='email' required onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>
                        <Field htmlFor='password' label={t('admin.generic_account.password')}>
                            <input id='password' required type="password" onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>
                        <Field htmlFor='serial' label={t('admin.generic_account.serial')}>
                            <input id='serial' onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>
                        <Field htmlFor='vendor' label={t('admin.generic_account.vendor')}>
                            <input id='vendor' onChange={e => this.handleFieldChange(e.target.id, e.target.value)} />
                        </Field>

                        <Field htmlFor='localAuthorities' label={t('admin.generic_account.localAuthorities')}>
                            <div style={{ marginBottom: '0.5em' }}>{localAuthorities.length > 0 ? localAuthorities : t('admin.generic_account.no_localAuthorities')}</div>
                            <div style={{ marginBottom: '1em' }}>
                                <Dropdown id='localAuthorities' value='' placeholder={t('admin.generic_account.add_localAuthority')} fluid selection options={localAuthorityOptions} onChange={this.handleChange} />
                            </div>
                        </Field>
                        <div style={{ textAlign: 'right' }}>
                            <Button basic primary onClick={this.submitForm} type='submit'>{t('api-gateway:form.create')}</Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(GenericAccountCreation)