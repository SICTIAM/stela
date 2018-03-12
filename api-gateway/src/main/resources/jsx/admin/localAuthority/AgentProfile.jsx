import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Label, Icon, Dropdown, Form, Button, Checkbox } from 'semantic-ui-react'

import { notifications } from '../../_util/Notifications'
import { Field, Page } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class AgentProfile extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    static defaultProps = {
        uuid: ''
    }
    state = {
        fields: {
            uuid: '',
            agent: {
                family_name: '',
                given_name: '',
                email: '',
            },
            localAuthority: {
                name: ''
            },
            groups: [],
            admin: false
        },
        allGroups: [],
        newGroup: ''
    }
    componentDidMount() {
        const { localAuthorityUuid, uuid } = this.props
        if (uuid) {
            fetchWithAuthzHandling({ url: `/api/admin/local-authority/${localAuthorityUuid || 'current'}/agent/${uuid}` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ fields: json }))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                    })
                })
            fetchWithAuthzHandling({ url: `/api/admin/local-authority/${localAuthorityUuid || 'current'}/group` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ allGroups: json }))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                    })
                })
        }
    }
    submitForm = () => {
        const groupUuids = this.state.fields.groups.map(group => group.uuid)
        const body = JSON.stringify({ admin: this.state.fields.admin, groupUuids })
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/admin/profile/${this.state.fields.uuid}/rights`, method: 'PUT', body, headers, context: this.context })
            .then(checkStatus)
            .then(() =>
                this.context._addNotification(notifications.admin.agentProfileUpdated)
            )
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    handleCheckboxChange = (event, { id }) => {
        const { fields } = this.state
        fields[id] = !fields[id]
        this.setState({ fields })
    }
    handleChange = (event, { value }) => {
        const { allGroups, fields } = this.state
        const group = allGroups.find(group => group.uuid === value)
        fields.groups.push(group)
        this.setState({ fields })
    }
    removeGroup = (uuid) => {
        const { fields } = this.state
        fields.groups = fields.groups.filter(group => group.uuid !== uuid)
        this.setState({ fields })
    }
    render() {
        const { t } = this.context
        const { agent, localAuthority } = this.state.fields
        const groups = this.state.fields.groups.map(group =>
            <Label basic key={group.uuid}>{group.name} <Icon name='delete' onClick={() => this.removeGroup(group.uuid)} /></Label>
        )
        const groupOptions = this.state.allGroups
            .filter(group => !this.state.fields.groups.find(profileGroup => profileGroup.uuid === group.uuid))
            .map(group => {
                return { key: group.uuid, value: group.uuid, text: group.name }
            })
        return (
            <Page title={`${agent.given_name} ${agent.family_name}`} subtitle={`de ${localAuthority.name}`}>
                <Segment>
                    <Form>
                        <Field htmlFor='email' label={t('agent.email')}>
                            <span id='email'>{agent.email}</span>
                        </Field>
                        <Field htmlFor='admin' label={t('agent.local_authority_admin')}>
                            <Checkbox id='admin'
                                toggle checked={this.state.fields.admin}
                                onChange={this.handleCheckboxChange} />
                        </Field>
                        <Field htmlFor='groups' label={t('agent.groups')}>
                            <div style={{ marginBottom: '0.5em' }}>{groups.length > 0 ? groups : t('admin.agent.no_group')}</div>
                            <div style={{ marginBottom: '1em' }}>
                                <Dropdown id='groups' value='' placeholder={t('admin.agent.add_group')} fluid selection options={groupOptions} onChange={this.handleChange} />
                            </div>
                        </Field>
                        <div style={{ textAlign: 'right' }}>
                            <Button basic primary onClick={this.submitForm} type='submit'>{t('api-gateway:form.update')}</Button>
                        </div>
                    </Form>
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(AgentProfile)