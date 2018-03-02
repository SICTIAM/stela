import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { translate } from 'react-i18next'
import { Segment, List, Button, Icon, Confirm, Grid } from 'semantic-ui-react'

import StelaTable from '../../_components/StelaTable'
import { notifications } from '../../_util/Notifications'
import { modules } from '../../_util/constants'
import { Field, ListItem, Page } from '../../_components/UI'
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
            .then(json => {
                //flaten agent properties for the table component
                var agents = [];
                for (var i in json.profiles) {
                    agents.push(json.profiles[i].agent);
                }
                json.agents = agents;
                this.setState({ fields: json })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
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
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.admin.title', text))
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
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
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
                <ListItem key={moduleName} title={t(`modules.${moduleName}`)} icon='setting' iconColor={isActivated ? 'green' : 'red'}>
                    {isActivated &&
                        <List.Content floated='right'>
                            <Button basic color='red' compact onClick={() => this.alertModal(moduleName, 'deactivation')}>{t('form.deactivate')}</Button>
                        </List.Content>
                    }
                    {!isActivated &&
                        <List.Content floated='right'>
                            <Button basic color='green' compact onClick={() => this.alertModal(moduleName, 'activation')}>{t('form.activate')}</Button>
                        </List.Content>
                    }
                    {isActivated &&
                        <List.Content floated='right'>
                            <Link to={isActivatedUrl} className='ui button compact basic primary'>{t('form.configure')}</Link>
                        </List.Content>
                    }
                </ListItem>
            )
        })
        const groupList = this.state.fields.groups.map(group =>
            <ListItem as={Link} to={`/admin/collectivite/${this.state.fields.uuid}/groupes/${group.uuid}`}
                key={group.uuid} title={group.name} icon='users' style={{ cursor: 'pointer' }}>
                <List.Content floated='right'>
                    <Icon onClick={() => this.removeGroup(group.uuid)} name='remove' color='red' size='large' style={{ cursor: 'pointer' }} />
                </List.Content>
            </ListItem>
        )
        return (
            <Page title={this.state.fields.name}>

                <Grid columns={2}>
                    <Grid.Column>
                        <Segment style={{ height: '100%' }}>
                            <h2>{t('admin.local_authority.general_informations')}</h2>
                            <Field htmlFor="uuid" label={t('local_authority.uuid')}>
                                <span id="uuid">{this.state.fields.uuid}</span>
                            </Field>
                            <Field htmlFor="siren" label={t('local_authority.siren')}>
                                <span id="siren">{this.state.fields.siren}</span>
                            </Field>
                        </Segment>
                    </Grid.Column>

                    <Grid.Column>
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
                    </Grid.Column>
                </Grid>

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
                    <div style={{ textAlign: 'right' }}>
                        <Link className='ui button basic primary' to={`/admin/collectivite/${this.state.fields.uuid}/groupes`}>
                            {t('admin.local_authority.new_group')}
                        </Link>
                    </div>
                    <List divided relaxed verticalAlign='middle'>
                        {groupList}
                    </List>
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(LocalAuthority)