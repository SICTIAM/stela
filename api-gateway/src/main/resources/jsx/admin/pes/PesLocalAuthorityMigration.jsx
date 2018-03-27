import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Icon } from 'semantic-ui-react'

import { Page, Field, FieldValue, MigrationSteps } from '../../_components/UI'
import { notifications } from '../../_util/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../../_util/utils'

class PesLocalAuthorityMigration extends Component {
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
            migrationStatus: ''
        },
        status: 'init'
    }
    componentDidMount() {
        const uuid = this.props.uuid
        const url = uuid ? '/api/pes/localAuthority' + uuid : '/api/pes/localAuthority/current'
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
    migrate = () => {
        const url = `/api/pes/localAuthority/${this.props.uuid || 'current'}/migration`
        fetchWithAuthzHandling({ url, method: 'POST', context: this.context })
            .then(checkStatus)
            .then(() => {
                const { fields } = this.state
                fields.migrationStatus = 'ONGOING'
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
        const status = this.state.fields.migrationStatus || 'NOT_DONE'
        return (
            <Page title='Migration du module Hélios'>
                <Segment style={{ height: '100%' }}>
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
                    <MigrationSteps
                        disabled
                        icon={<Icon name='users' />}
                        title='Migration des utilisateurs du module'
                        description='Récupération des utilisateurs du module PES de STELA 2'
                        status='init'
                        onClick={this.migrate} />
                    <MigrationSteps
                        icon={<Icon name='calculator' />}
                        title='Migration des PES'
                        description='Récupération des PES de STELA 2'
                        status={status}
                        onClick={this.migrate} />
                    <MigrationSteps
                        disabled
                        icon={<Icon.Group><Icon name='users' /><Icon corner name='delete' /> </Icon.Group>}
                        title='Désactivation des utilisateurs du module'
                        description='Désactivation des utilisateurs du module PES de STELA 2'
                        status='init'
                        onClick={this.migrate} />
                </Segment>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(PesLocalAuthorityMigration)