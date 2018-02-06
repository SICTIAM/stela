import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Form, Button, Segment, Label, Icon, Dropdown } from 'semantic-ui-react'
import Validator from 'validatorjs'

import { notifications } from '../../_util/Notifications'
import { Field, Page } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class PesLocalAuthorityParams extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        fields: {
            uuid: '',
            name: '',
            siren: '',
            serverCode: '',
            sirens: []
        },
        serverCodes: [],
        newSiren: '',
        isNewSirenValid: false
    }
    componentDidMount() {
        const uuid = this.props.uuid
        const url = uuid ? '/api/pes/localAuthority/' + uuid : '/api/pes/localAuthority/current'
        fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ fields: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.pes.title', json.message)
                })
            })
        fetchWithAuthzHandling({ url: '/api/pes/localAuthority/server-codes' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ serverCodes: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.pes.title', json.message)
                })
            })
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
    onRemoveSiren = (index) => {
        const fields = this.state.fields
        fields.sirens.splice(index, 1)
        this.setState({ fields: fields })
    }
    handleServerCodeChange = (e, { value }) => {
        const fields = this.state.fields
        fields.serverCode = value
        this.setState({ fields: fields })
    }
    validateSiren = (siren) => {
        const validation = new Validator({ siren: siren.replace(/\s/g, "") }, { siren: 'required|digits:9' })
        return validation.passes()
    }
    handleNewSirenChange = (value) => {
        this.setState({ newSiren: value, isNewSirenValid: this.validateSiren(value) })
    }
    submitForm = (event) => {
        event.preventDefault()
        const { serverCode, sirens } = this.state.fields
        const data = JSON.stringify({ serverCode, sirens: sirens.map(siren => siren.replace(/\s/g, "")) })
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        fetchWithAuthzHandling({ url: '/api/pes/localAuthority/' + this.state.fields.uuid, method: 'PATCH', body: data, headers: headers, context: this.context })
            .then(checkStatus)
            .then(() => this.context._addNotification(notifications.admin.localAuthorityUpdate))
            .catch(response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    render() {
        const { t } = this.context
        const listSiren = this.state.fields.sirens.map((siren, index) =>
            <Label basic key={index}>{siren} <Icon name='delete' onClick={() => this.onRemoveSiren(siren)} /></Label>
        )
        const serverCodes = this.state.serverCodes.map(serverCode => { return { key: serverCode, value: serverCode, text: serverCode } })
        return (
            <Page title={this.state.fields.name} >
                <Segment>
                    <h2>{t('admin.modules.pes.local_authority_settings.title')}</h2>
                    <Form onSubmit={this.submitForm}>
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
                            <div style={{ marginBottom: '0.5em' }}>{listSiren.length > 0 ? listSiren : t('admin.modules.pes.local_authority_settings.no_siren')}</div>
                            <input id='sirens'
                                onKeyPress={this.onkeyPress}
                                value={this.state.newSiren}
                                onChange={(e) => this.handleNewSirenChange(e.target.value)}
                                className='simpleInput' />
                            <Button basic color='grey' style={{ marginLeft: '1em' }} onClick={(event) => this.addSiren(event)}>{t('api-gateway:form.add')}</Button>
                        </Field>
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