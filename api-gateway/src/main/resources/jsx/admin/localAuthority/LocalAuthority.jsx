import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { translate } from 'react-i18next'
import { Segment, List, Header } from 'semantic-ui-react'

import StelaTable from '../../_components/StelaTable'
import { errorNotification } from '../../_components/Notifications'
import { modules } from '../../_util/constants'
import { Field } from '../../_components/UI'
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
        }
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
    render() {
        const { t } = this.context
        const moduleList = modules.map(moduleName => {
            const isActivated = this.state.fields.activatedModules.includes(moduleName)
            const isActivatedUrl = this.props.uuid
                ? `/admin/collectivite/${this.state.fields.uuid}/${moduleName.toLowerCase()}`
                : `/admin/ma-collectivite/${moduleName.toLowerCase()}`
            return (
                <List.Item key={moduleName}>
                    {isActivated &&
                        <List.Content floated='right'>
                            <Link to={isActivatedUrl} className='ui button compact'>{t('form.configure')}</Link>
                        </List.Content>
                    }
                    <List.Icon name='cube' size='large' color={isActivated ? 'green' : 'red'} />
                    <List.Content><Header size='small'>{t(`modules.${moduleName}`)}</Header></List.Content>
                </List.Item>
            )
        })
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
                        noDataMessage={t('admin.local_authority.no_user')}
                        keyProperty='uuid' />
                </Segment>

                <Segment>
                    <h2>{t('admin.local_authority.groups')}</h2>
                    <StelaTable
                        data={this.state.fields.groups}
                        metaData={[
                            { property: 'uuid', displayed: false, searchable: false },
                            { property: 'name', displayed: true, displayName: t('group.name'), searchable: true }
                        ]}
                        header={true}
                        noDataMessage={t('admin.local_authority.no_group')}
                        keyProperty='uuid' />
                </Segment>
            </Segment>
        )
    }
}

export default translate(['api-gateway'])(LocalAuthority)