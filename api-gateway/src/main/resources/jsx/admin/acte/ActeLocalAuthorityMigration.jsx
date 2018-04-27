import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon, Input } from 'semantic-ui-react'

import { Page, Field, FieldValue, MigrationSteps } from '../../_components/UI'
import { notifications } from '../../_util/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'
import { monthBeforeArchiving } from '../../_util/constants'

class ActeLocalAuthorityMigration extends Component {
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
            migration: {
                migrationUsers: 'NOT_DONE',
                migrationData: 'NOT_DONE',
                migrationUsersDeactivation: 'NOT_DONE'
            }
        },
        form: {
            email: '',
            siren: '',
            month: monthBeforeArchiving
        },
        status: 'init'
    }
    componentDidMount() {
        const url = `/api/acte/localAuthority/${this.props.uuid || 'current'}`
        fetchWithAuthzHandling({ url })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ fields: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
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
        const url = `/api/acte/localAuthority/${this.props.uuid || 'current'}/migration/${migrationType}`
        const data = this.getFormData()
        fetchWithAuthzHandling({ url, method: 'POST', query: data, context: this.context })
            .then(checkStatus)
            .then(() => {
                const { fields } = this.state
                if (fields.migration === null) fields.migration = {}
                fields.migration[migrationType] = 'ONGOING'
                this.setState({ fields })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    reset = (migrationType) => {
        const url = `/api/acte/localAuthority/${this.props.uuid || 'current'}/migration/${migrationType}/reset`
        fetchWithAuthzHandling({ url, method: 'POST', context: this.context })
            .then(checkStatus)
            .then(() => {
                const { fields } = this.state
                if (fields.migration === null) fields.migration = {}
                fields.migration[migrationType] = 'NOT_DONE'
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
        const { migration } = this.state.fields
        return (
            <Page title={t('admin.modules.acte.migration.title')}>
                <Segment>
                    <h2>{t('api-gateway:admin.local_authority.general_informations')}</h2>
                    <Field htmlFor="uuid" label={t('api-gateway:local_authority.uuid')}>
                        <FieldValue id="uuid">{this.state.fields.uuid}</FieldValue>
                    </Field>
                    <Field htmlFor="name" label={t('api-gateway:local_authority.name')}>
                        <FieldValue id="name">{this.state.fields.name}</FieldValue>
                    </Field>
                    <Field htmlFor="siren" label={t('api-gateway:local_authority.siren')}>
                        <FieldValue id="siren">{this.state.fields.siren}</FieldValue>
                    </Field>
                </Segment>

                <Segment>
                    <h2>{t('admin.modules.acte.migration.additional_options.title')}</h2>
                    <Field htmlFor='email' label={t('admin.modules.acte.migration.additional_options.email')}>
                        <Input id='email'
                            placeholder={`${t('api-gateway:agent.email')}...`}
                            value={this.state.form.email}
                            onChange={this.onFormChange} />
                    </Field>
                    <Field htmlFor='siren' label={t('admin.modules.acte.migration.additional_options.siren')}>
                        <Input id='siren'
                            placeholder={`${t('api-gateway:local_authority.siren')}...`}
                            value={this.state.form.siren}
                            onChange={this.onFormChange} />
                    </Field>
                    <Field htmlFor='month' label={t('admin.modules.acte.migration.additional_options.month')}>
                        <Input id='month'
                            type='number'
                            min='1'
                            value={this.state.form.month}
                            onChange={this.onFormChange} />
                    </Field>
                </Segment>
                <Segment>
                    <MigrationSteps
                        icon={<Icon name='users' />}
                        title={t('admin.modules.acte.migration.users_migration.title')}
                        description={t('admin.modules.acte.migration.users_migration.description')}
                        status={migration ? (migration.migrationUsers || 'NOT_DONE') : 'NOT_DONE'}
                        onClick={() => this.migrate('migrationUsers')}
                        reset={() => this.reset('migrationUsers')} />
                    <MigrationSteps
                        icon={<Icon name='database' />}
                        title={t('admin.modules.acte.migration.acte.title')}
                        description={t('admin.modules.acte.migration.acte.description')}
                        status={migration ? (migration.migrationData || 'NOT_DONE') : 'NOT_DONE'}
                        onClick={() => this.migrate('migrationData')}
                        reset={() => this.reset('migrationData')} />
                </Segment>
            </Page>
        )
    }
}

export default translate(['acte', 'api-gateway'])(ActeLocalAuthorityMigration)