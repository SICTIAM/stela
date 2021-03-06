import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon, Input } from 'semantic-ui-react'

import { Page, FieldInline, FieldValue, MigrationSteps } from '../../_components/UI'
import { notifications } from '../../_util/Notifications'
import { checkStatus } from '../../_util/utils'
import { withAuthContext } from '../../Auth'

class PesLocalAuthorityMigration extends Component {
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
            migration: {
                migrationUsers: 'NOT_DONE',
                migrationData: 'NOT_DONE',
                migrationUsersDeactivation: 'NOT_DONE'
            }
        },
        form: {
            email: '',
            siren: ''
        },
        status: 'init'
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const url = `/api/pes/localAuthority/${this.props.uuid || 'current'}`
        _fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ fields: json }))
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    onFormChange = (e, { id, value }) => {
        const { form } = this.state
        form[id] = value
        this.setState({ form })
    }
    getFormData = () => {
        const data = {}
        Object.keys(this.state.form)
            .filter(k => this.state.form[k] !== '')
            .map(k => data[k] = this.state.form[k])
        return data
    }
    migrate = (migrationType) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const url = `/api/pes/localAuthority/${this.props.uuid || 'current'}/migration/${migrationType}`
        const data = this.getFormData()
        _fetchWithAuthzHandling({ url, method: 'POST', query: data, context: this.props.authContext })
            .then(checkStatus)
            .then(() => {
                const { fields } = this.state
                if (fields.migration === null) fields.migration = {}
                fields.migration[migrationType] = 'ONGOING'
                this.setState({ fields })
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    reset = (migrationType) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const url = `/api/pes/localAuthority/${this.props.uuid || 'current'}/migration/${migrationType}/reset`
        _fetchWithAuthzHandling({ url, method: 'POST', context: this.props.authContext })
            .then(checkStatus)
            .then(() => {
                const { fields } = this.state
                if (fields.migration === null) fields.migration = {}
                fields.migration[migrationType] = 'NOT_DONE'
                this.setState({ fields })
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    render() {
        const { t } = this.context
        const { migration } = this.state.fields
        return (
            <Page title={t('admin.modules.pes.migration.title')}>
                <Segment>
                    <h2 className='secondary'>{t('api-gateway:admin.local_authority.general_informations')}</h2>
                    <FieldInline htmlFor="uuid" label={t('api-gateway:local_authority.uuid')}>
                        <FieldValue id="uuid">{this.state.fields.uuid}</FieldValue>
                    </FieldInline>
                    <FieldInline htmlFor="name" label={t('api-gateway:local_authority.name')}>
                        <FieldValue id="name">{this.state.fields.name}</FieldValue>
                    </FieldInline>
                    <FieldInline htmlFor="siren" label={t('api-gateway:local_authority.siren')}>
                        <FieldValue id="siren">{this.state.fields.siren}</FieldValue>
                    </FieldInline>
                </Segment>

                <Segment>
                    <h2 className='secondary'>{t('admin.modules.pes.migration.additional_options.title')}</h2>
                    <FieldInline htmlFor='email' label={t('admin.modules.pes.migration.additional_options.email')}>
                        <Input id='email'
                            placeholder={`${t('api-gateway:agent.email')}...`}
                            value={this.state.form.email}
                            onChange={this.onFormChange} />
                    </FieldInline>
                    <FieldInline htmlFor='siren' label={t('admin.modules.pes.migration.additional_options.siren')}>
                        <Input id='siren'
                            placeholder={`${t('api-gateway:local_authority.siren')}...`}
                            value={this.state.form.siren}
                            onChange={this.onFormChange} />
                    </FieldInline>
                </Segment>
                <Segment>
                    <MigrationSteps
                        icon={<Icon name='users' />}
                        title={t('admin.modules.pes.migration.users_migration.title')}
                        description={t('admin.modules.pes.migration.users_migration.description')}
                        status={migration ? (migration.migrationUsers || 'NOT_DONE') : 'NOT_DONE'}
                        onClick={() => this.migrate('migrationUsers')}
                        reset={() => this.reset('migrationUsers')} />
                    <MigrationSteps
                        icon={<Icon name='database' />}
                        title={t('admin.modules.pes.migration.pes.title')}
                        description={t('admin.modules.pes.migration.pes.description')}
                        status={migration ? (migration.migrationData || 'NOT_DONE') : 'NOT_DONE'}
                        onClick={() => this.migrate('migrationData')}
                        reset={() => this.reset('migrationData')} />
                </Segment>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(withAuthContext(PesLocalAuthorityMigration))