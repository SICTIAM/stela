import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Header, Label, Icon, Dropdown, Form, Button } from 'semantic-ui-react'

import { errorNotification, groupsUpdatedSuccess } from '../../_components/Notifications'
import { Field } from '../../_components/UI'
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
            groups: []
        },
        allGroups: [],
        newGroup: ''
    }
    componentDidMount() {
        const { localAuthorityUuid, uuid } = this.props
        if (localAuthorityUuid && uuid) {
            fetchWithAuthzHandling({ url: `/api/admin/local-authority/${localAuthorityUuid}/agent/${uuid}` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ fields: json }))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(errorNotification(this.context.t('notifications.admin.title'), this.context.t(json.message)))
                    })
                })
            fetchWithAuthzHandling({ url: `/api/admin/local-authority/${localAuthorityUuid}/group` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ allGroups: json }))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(errorNotification(this.context.t('notifications.admin.title'), this.context.t(json.message)))
                    })
                })
        }
    }
    submitForm = () => {
        const body = JSON.stringify(this.state.fields.groups.map(group => group.uuid))
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/admin/profile/${this.state.fields.uuid}/group`, method: 'PUT', body, headers, context: this.context })
            .then(checkStatus)
            .then(() =>
                this.context._addNotification(groupsUpdatedSuccess(this.context.t))
            )
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.admin.title'), this.context.t(json.message)))
                })
            })
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
            <Segment>
                <Header as='h1'>
                    <Header.Content>
                        {`${agent.given_name} ${agent.family_name}`}
                        <Header.Subheader>de {localAuthority.name}</Header.Subheader>
                    </Header.Content>
                </Header>
                <Form>
                    <Field htmlFor='email' label={t('agent.email')}>
                        <span id='email'>{agent.email}</span>
                    </Field>

                    <Field htmlFor='groups' label={t('agent.groups')}>
                        <div style={{ marginBottom: '0.5em' }}>{groups.length > 0 ? groups : t('admin.agent.groups')}</div>
                        <div style={{ marginBottom: '1em' }}>
                            <Dropdown id='groups' value='' placeholder={t('admin.agent.add_group')} fluid selection options={groupOptions} onChange={this.handleChange} />
                        </div>
                    </Field>
                    <Button onClick={this.submitForm} primary type='submit'>{t('api-gateway:form.update')}</Button>
                </Form>
            </Segment>
        )
    }
}

export default translate(['api-gateway'])(AgentProfile)