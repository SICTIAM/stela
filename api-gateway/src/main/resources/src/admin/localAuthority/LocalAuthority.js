import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { translate } from 'react-i18next'
import { Segment, List, Button, Icon, Grid } from 'semantic-ui-react'

import StelaTable from '../../_components/StelaTable'
import { notifications } from '../../_util/Notifications'
import { modules } from '../../_util/constants'
import ConfirmModal from '../../_components/ConfirmModal'
import { FieldInline, FieldValue, ListItem, Page } from '../../_components/UI'
import {checkStatus, getLocalAuthoritySlug, sortAlphabetically} from '../../_util/utils'
import { withAuthContext } from '../../Auth'

class LocalAuthority extends Component {
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
            name: '',
            siren: '',
            activatedModules: [],
            groups: [],
            certificates: []
        }
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const uuid = this.props.uuid
        const url = uuid ? '/api/admin/local-authority/' + uuid : '/api/admin/local-authority/current'
        _fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                //flaten agent properties for the table component
                json.agents = json.profiles.map(profile => profile.agent)
                this.setState({ fields: json })
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    activateModule = (moduleName) => this.editModule(moduleName, 'POST', () => {
        const { fields } = this.state
        if (!fields.activatedModules.includes(moduleName)) fields.activatedModules.push(moduleName)
        this.setState({ fields })
    })
    deactivateModule = (moduleName) => this.editModule(moduleName, 'DELETE', () => {
        const { fields } = this.state
        const i = fields.activatedModules.indexOf(moduleName)
        if (i > -1) fields.activatedModules.splice(i, 1)
        this.setState({ fields })
    })
    editModule = (moduleName, method, callback) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const uuid = this.props.uuid
        const url = uuid ? `/api/admin/local-authority/${uuid}/${moduleName}` : `/api/admin/local-authority/current/${moduleName}`
        _fetchWithAuthzHandling({ url, method: method, context: this.props.authContext })
            .then(checkStatus)
            .then(callback)
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.admin.title', text))
            })
    }
    removeGroup = (event, uuid) => {
        event.preventDefault()
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: `/api/admin/local-authority/group/${uuid}`, method: 'DELETE', context: this.props.authContext })
            .then(checkStatus)
            .then(() => {
                const { fields } = this.state
                fields.groups = fields.groups.filter(group => group.uuid !== uuid)
                this.setState({ fields })
                _addNotification(notifications.admin.groupDeleted)
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    render() {
        const { t } = this.context
        const localAuthoritySlug = getLocalAuthoritySlug()
        const emailCertificateDisplay = email => email ||
            <span style={{ fontStyle: 'italic' }}>{t('admin.local_authority.no_certificate_email')}</span>
        /** Module List */
        const moduleList = modules.map(moduleName => {
            const isActivated = this.state.fields.activatedModules.includes(moduleName)
            const isActivatedUrl = this.props.uuid
                ? `/${localAuthoritySlug}/admin/collectivite/${this.state.fields.uuid}/${moduleName.toLowerCase()}`
                : `/${localAuthoritySlug}/admin/ma-collectivite/${moduleName.toLowerCase()}`
            const migrationUrl = isActivatedUrl + '/migration'
            return (
                <ListItem key={moduleName} title={t(`modules.${moduleName}`)} icon='setting' iconColor={isActivated ? 'green' : 'red'}>
                    {isActivated && (
                        <List.Content floated='right'>
                            <ConfirmModal onConfirm={() => this.deactivateModule(moduleName)}
                                text={t('admin.local_authority.confirmModal.deactivation', { moduleName: t(`modules.${moduleName}`) })}>
                                <Button basic color='red' compact>{t('form.deactivate')}</Button>
                            </ConfirmModal>
                        </List.Content>
                    )}
                    {!isActivated && (
                        <List.Content floated='right'>
                            <ConfirmModal onConfirm={() => this.activateModule(moduleName)}
                                text={t('admin.local_authority.confirmModal.activation', { moduleName: t(`modules.${moduleName}`) })}>
                                <Button basic color='green' compact>{t('form.activate')}</Button>
                            </ConfirmModal>
                        </List.Content>
                    )}
                    {isActivated && (
                        <List.Content floated='right'>
                            <Link to={isActivatedUrl} className='ui button compact basic primary'>{t('form.configure')}</Link>
                        </List.Content>
                    )}
                    {isActivated && (
                        <List.Content floated='right'>
                            <Link to={migrationUrl} className='ui button compact basic primary'>{t('migration.title')}</Link>
                        </List.Content>
                    )}
                </ListItem>
            )
        })
        /** End Module List */
        const groupList = this.state.fields.groups.map(group =>
            <ListItem as={Link} to={`/${localAuthoritySlug}/admin/${this.props.uuid ? `collectivite/${this.state.fields.uuid}` : 'ma-collectivite'}/groupes/${group.uuid}`}
                key={group.uuid} title={group.name} icon='users' style={{ cursor: 'pointer' }}>
                <List.Content floated='right'>
                    <Icon onClick={e => this.removeGroup(e, group.uuid)} name='remove' color='red' size='large' style={{ cursor: 'pointer' }} />
                </List.Content>
            </ListItem>
        )
        return (
            <Page title={this.state.fields.name}>

                <Grid columns={2}>
                    <Grid.Column>
                        <Segment style={{ height: '100%' }}>
                            <h2 className='secondary'>{t('admin.local_authority.general_informations')}</h2>
                            <FieldInline htmlFor="uuid" label={t('local_authority.uuid')}>
                                <FieldValue id="uuid">{this.state.fields.uuid}</FieldValue>
                            </FieldInline>
                            <FieldInline htmlFor="siren" label={t('local_authority.siren')}>
                                <FieldValue id="siren">{this.state.fields.siren}</FieldValue>
                            </FieldInline>
                        </Segment>
                    </Grid.Column>

                    <Grid.Column>
                        <Segment>
                            <h2 className='secondary'>{t('admin.local_authority.modules')}</h2>
                            <List divided relaxed verticalAlign='middle'>
                                {moduleList}
                            </List>
                        </Segment>
                    </Grid.Column>
                </Grid>

                <Segment>
                    <h2 className='secondary'>{t('admin.local_authority.users')}</h2>
                    <StelaTable
                        data={this.state.fields.agents && sortAlphabetically(this.state.fields.agents, 'family_name')}
                        metaData={[
                            { property: 'uuid', displayed: false, searchable: false },
                            { property: 'family_name', displayed: true, displayName: t('agent.family_name'), searchable: true },
                            { property: 'given_name', displayed: true, displayName: t('agent.given_name'), searchable: true },
                            { property: 'email', displayed: true, displayName: t('agent.email'), searchable: true },
                        ]}
                        header={true}
                        link={this.props.uuid ? `/${localAuthoritySlug}/admin/collectivite/${this.state.fields.uuid}/agent/` : `/${localAuthoritySlug}/admin/ma-collectivite/agent/`}
                        linkProperty='uuid'
                        noDataMessage={t('admin.local_authority.no_user')}
                        keyProperty='uuid' />
                </Segment>

                <Segment>
                    <h2 className='secondary'>{t('admin.local_authority.groups')}</h2>
                    <div style={{ textAlign: 'right' }}>
                        <Link className='ui button basic primary' to={`/${localAuthoritySlug}/admin/collectivite/${this.state.fields.uuid}/groupes`}>
                            {t('admin.local_authority.new_group')}
                        </Link>
                    </div>
                    <List divided relaxed verticalAlign='middle'>
                        {groupList}
                    </List>
                </Segment>


                <Segment>
                    <h2 className='secondary'>{t('admin.local_authority.certificates')}</h2>
                    <div style={{ textAlign: 'right' }}>
                        <Link className='ui button basic primary' to={`/${localAuthoritySlug}/admin/collectivite/${this.state.fields.uuid}/certificats/nouveau`}>
                            {t('admin.local_authority.new_certificate')}
                        </Link>
                    </div>
                    <StelaTable
                        data={this.state.fields.certificates}
                        search={false}
                        metaData={[
                            { property: 'uuid', displayed: false },
                            { property: 'subjectCommonName', displayed: true, displayName: t('certificate.subjectCommonName'), displayComponent: emailCertificateDisplay },
                            { property: 'subjectEmail', displayed: true, displayName: t('certificate.subjectEmail'), displayComponent: emailCertificateDisplay },
                            { property: 'issuedDate', displayed: true, displayName: t('certificate.issuedDate') },
                            { property: 'expiredDate', displayed: true, displayName: t('certificate.expiredDate') },
                        ]}
                        header={true}
                        link={this.props.uuid ? `/${localAuthoritySlug}/admin/collectivite/${this.state.fields.uuid}/certificats/` : `/${localAuthoritySlug}/admin/ma-collectivite/certificats/`}
                        linkProperty='uuid'
                        noDataMessage={t('admin.local_authority.no_certificate')}
                        keyProperty='uuid' />
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(withAuthContext(LocalAuthority))
