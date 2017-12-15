import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { translate } from 'react-i18next'

import { Segment, List, Form, Button, Icon, Header, Confirm  } from 'semantic-ui-react'
import Validator from 'validatorjs'


import StelaTable from '../../_components/StelaTable'
import { errorNotification } from '../../_components/Notifications'
import { modules } from '../../_util/constants'
import { Field, ListItem } from '../../_components/UI'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class LocalAuthority extends Component {
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
            name: '',
            siren: '',
            activatedModules: [],
            groups: []
        },
        isConfirmModalOpen: false,
        confirmModalType: '',
        moduleToEdit: ''
    }
    componentDidMount() {
        const uuid = this.props.uuid
        const url = uuid ? '/api/admin/local-authority/' + uuid : '/api/admin/local-authority/current'
        fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ fields: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.admin.title'), this.context.t(json.message)))
                })
            })
    }
    activateModule = (moduleName) => this.editModule(moduleName, 'POST', () => {
        const { fields } = this.state
        if (!fields.activatedModules.includes(moduleName)) fields.activatedModules.push(moduleName)
        this.setState({ fields, isConfirmModalOpen: false })
    })
    deactivateModule = (moduleName) => this.editModule(moduleName, 'DELETE', () => {
        const { fields } = this.state
        const i = fields.activatedModules.indexOf(moduleName)
        if (i > -1) fields.activatedModules.splice(i, 1)
        this.setState({ fields, isConfirmModalOpen: false })
    })
    editModule = (moduleName, method, callback) => {
        const uuid = this.props.uuid
        const url = uuid ? `/api/admin/local-authority/${uuid}/${moduleName}` : `/api/admin/local-authority/current/${moduleName}`
        fetchWithAuthzHandling({ url, method: method, context: this.context })
            .then(checkStatus)
            .then(callback)
            .catch(response => {
                response.text().then(text => this.context._addNotification(errorNotification(this.context.t('notifications.admin.title'), this.context.t(text))))
            })
    }
    alertModal = (moduleToEdit, confirmModalType) => {
        this.setState({ isConfirmModalOpen: true, moduleToEdit, confirmModalType })
    }
    closeConfirmModal = () => this.setState({ isConfirmModalOpen: false })
    confirmModal = () => {
        const { confirmModalType, moduleToEdit } = this.state
        confirmModalType === 'activation' && this.activateModule(moduleToEdit)
        confirmModalType === 'deactivation' && this.deactivateModule(moduleToEdit)
    }    
    addNewGroup = (newGroup, callback) => {
        const headers = { 'Content-Type': 'application/json' }
        fetchWithAuthzHandling({ url: `/api/admin/local-authority/${this.state.fields.uuid}/group`, method: 'POST', body: newGroup, headers: headers, context: this.context })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState((prevState) => { prevState.fields.groups.push(json) }, callback))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.admin.title'), this.context.t(json.message)))
                })
            })
    }
    removeGroup = (uuid) => {
        fetchWithAuthzHandling({ url: `/api/admin/local-authority/${this.state.fields.uuid}/group/${uuid}`, method: 'DELETE', context: this.context })
            .then(checkStatus)
            .then(() => {
                const { fields } = this.state
                fields.groups = fields.groups.filter(group => group.uuid !== uuid)
                this.setState({ fields })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(errorNotification(this.context.t('notifications.admin.title'), this.context.t(json.message)))
                })
            })
    }
    render() {
        const { t } = this.context
        const { isConfirmModalOpen, confirmModalType, moduleToEdit } = this.state
        const confirmModalMessage = t(`admin.local_authority.confirmModal.${confirmModalType}`, { moduleName: t(`modules.${moduleToEdit}`) })
        const moduleList = modules.map(moduleName => {
            const isActivated = this.state.fields.activatedModules.includes(moduleName)
            const isActivatedUrl = this.props.uuid
                ? `/admin/collectivite/${this.state.fields.uuid}/${moduleName.toLowerCase()}`
                : `/admin/ma-collectivite/${moduleName.toLowerCase()}`
            return (
                <ListItem key={moduleName} title={t(`modules.${moduleName}`)} icon='cube' iconColor={isActivated ? 'green' : 'red'}>
                    {isActivated &&
                        <List.Content floated='right'>
                            <Button color='red' compact onClick={() => this.alertModal(moduleName, 'deactivation')}>{t('form.deactivate')}</Button>
                        </List.Content>
                    }
                    {!isActivated &&
                        <List.Content floated='right'>
                            <Button color='green' compact onClick={() => this.alertModal(moduleName, 'activation')}>{t('form.activate')}</Button>
                        </List.Content>
                    }
                    {isActivated &&
                        <List.Content floated='right'>
                            <Link to={isActivatedUrl} className='ui button compact'>{t('form.configure')}</Link>
                        </List.Content>
                    }
                </ListItem>
            )
        })
        const groupList = this.state.fields.groups.map(group =>
            <ListItem key={group.uuid} title={group.name} icon='users'>
                <List.Content floated='right'>
                    <Icon onClick={() => this.removeGroup(group.uuid)} name='remove' color='red' size='large' style={{ cursor: 'pointer' }} />
                </List.Content>
            </ListItem>
        )
        return (
            <Segment>
                <h1>{this.state.fields.name}</h1>

                <Segment>
                    <h2>{t('admin.local_authority.general_informations')}</h2>
                    <Field htmlFor="uuid" label={t('local_authority.uuid')}>
                        <span id="uuid">{this.state.fields.uuid}</span>
                    </Field>
                    <Field htmlFor="siren" label={t('local_authority.siren')}>
                        <span id="siren">{this.state.fields.siren}</span>
                    </Field>
                </Segment>

                <Segment>
                    <h2>{t('admin.local_authority.modules')}</h2>
                    <List divided relaxed verticalAlign='middle'>
                        {moduleList}
                    </List>
                    <Confirm
                        open={isConfirmModalOpen}
                        content={confirmModalMessage}
                        confirmButton={t('form.confirm')}
                        cancelButton={t('form.cancel')}
                        onCancel={this.closeConfirmModal}
                        onConfirm={this.confirmModal} />
                </Segment>

                <Segment>
                    <h2>{t('admin.local_authority.users')}</h2>
                    <StelaTable
                        data={this.state.fields.agents}
                        metaData={[
                            { property: 'uuid', displayed: false, searchable: false },
                            { property: 'family_name', displayed: true, displayName: t('agent.family_name'), searchable: true },
                            { property: 'given_name', displayed: true, displayName: t('agent.given_name'), searchable: true },
                            { property: 'email', displayed: true, displayName: t('agent.email'), searchable: true },
                        ]}
                        header={true}
                        link={`/admin/collectivite/${this.state.fields.uuid}/agent/`}
                        linkProperty='uuid'
                        noDataMessage={t('admin.local_authority.no_user')}
                        keyProperty='uuid' />
                </Segment>

                <Segment>
                    <h2>{t('admin.local_authority.groups')}</h2>
                    <AddGroup addNewGroup={this.addNewGroup} />
                    <List divided relaxed verticalAlign='middle'>
                        {groupList}
                    </List>
                </Segment>
            </Segment>
        )
    }
}

class AddGroup extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        newGroup: '',
        isFormValid: false
    }
    validationRules = {
        name: 'required'
    }
    addNewGroup = () => this.props.addNewGroup(this.state.newGroup, () => this.setState({ newGroup: '' }))
    handleFieldChange = (e) => {
        const { value } = e.target
        this.setState({ newGroup: value }, this.validateForm)
    }
    validateForm = () => {
        const validation = new Validator({ name: this.state.newGroup }, { name: 'required' })
        this.setState({ isFormValid: validation.passes() })
    }
    render() {
        const { t } = this.context
        return (
            <Form>
                <Form.Field inline>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', marginBottom: '0.5em' }}>
                        <label htmlFor='name' style={{ marginLeft: '1em' }}>{t('admin.local_authority.new_group')}</label>
                        <div style={{ marginLeft: '1em' }}>
                            <input id='name'
                                placeholder={t('group.name') + '...'}
                                value={this.state.newGroup}
                                onChange={this.handleFieldChange} />
                        </div>
                        <Button style={{ marginLeft: '1em' }} onClick={this.addNewGroup} primary disabled={!this.state.isFormValid} type='submit'>{t('form.add')}</Button>
                    </div>
                </Form.Field>
            </Form>
        )
    }
}

export default translate(['api-gateway'])(LocalAuthority)