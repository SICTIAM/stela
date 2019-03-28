import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Label, Icon, Dropdown, Form, Button, Checkbox } from 'semantic-ui-react'

import { notifications } from '../../_util/Notifications'
import { FieldInline } from '../../_components/UI'
import { checkStatus } from '../../_util/utils'
import { withAuthContext } from '../../Auth'

class AgentProfile extends Component {
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
            uuid: '',
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
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const { localAuthorityUuid, uuid } = this.props
        if (uuid) {
            _fetchWithAuthzHandling({ url: `/api/admin/local-authority/${localAuthorityUuid || 'current'}/agent/${uuid}` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ fields: json }))
                .catch(response => {
                    response.json().then(json => {
                        _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                    })
                })
            _fetchWithAuthzHandling({ url: `/api/admin/local-authority/${localAuthorityUuid || 'current'}/group` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ allGroups: json }))
                .catch(response => {
                    response.json().then(json => {
                        _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                    })
                })
        }
    }
    submitForm = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const groupUuids = this.state.fields.groups.map(group => group.uuid)
        const body = JSON.stringify({ admin: this.state.fields.admin, groupUuids })
        const headers = { 'Content-Type': 'application/json' }
        const url = `/api/admin/profile/${this.state.fields.uuid}/rights`
        _fetchWithAuthzHandling({ url, method: 'PUT', body, headers, context: this.props.authContext })
            .then(checkStatus)
            .then(() => {
                this.props.authContext.getUser()
                _addNotification(notifications.admin.agentProfileUpdated)
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
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
        const groups = this.state.fields.groups.map(group =>
            <Label basic key={group.uuid}>{group.name} <Icon name='delete' onClick={() => this.removeGroup(group.uuid)} /></Label>
        )
        const groupOptions = this.state.allGroups
            .filter(group => !this.state.fields.groups.find(profileGroup => profileGroup.uuid === group.uuid))
            .map(group => {
                return { key: group.uuid, value: group.uuid, text: group.name }
            })
        return (
            <Form>
                <FieldInline htmlFor='admin' label={t('agent.local_authority_admin')}>
                    <Checkbox id='admin'
                        toggle checked={this.state.fields.admin}
                        onChange={this.handleCheckboxChange} />
                </FieldInline>
                <FieldInline htmlFor='groups' label={t('agent.groups')}>
                    <div style={{ marginBottom: '0.5em' }}>{groups.length > 0 ? groups : t('admin.agent.no_group')}</div>
                    <div style={{ marginBottom: '1em' }}>
                        <Dropdown id='groups' value='' placeholder={t('admin.agent.add_group')} fluid selection options={groupOptions}
                            onChange={this.handleChange} />
                    </div>
                </FieldInline>
                <div style={{ textAlign: 'right' }}>
                    <Button basic primary onClick={this.submitForm} type='submit'>{t('api-gateway:form.update')}</Button>
                </div>
            </Form>
        )
    }
}

export default translate(['api-gateway'])(withAuthContext(AgentProfile))