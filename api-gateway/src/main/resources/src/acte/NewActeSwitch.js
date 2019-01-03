import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Menu, Segment } from 'semantic-ui-react'

import NewActeForm from './NewActeForm'
import NewActeBatchedForm from './NewActeBatchedForm'
import { Page } from '../_components/UI'
import Anomaly from '../_components/Anomaly'
import { notifications } from '../_util/Notifications'
import { checkStatus } from '../_util/utils'

class NewActeSwitch extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        fields: {
            uuid: '',
            mode: '',
            actes: []
        },
        status: '',
        hasAttachmentTypes: false,
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        if (this.props.uuid) {
            _fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.props.uuid}` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ fields: json }))
                .catch(response => {
                    response.json().then(json => {
                        _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                    })
                })
        } else {
            const { fields } = this.state
            fields.mode = 'ACTE'
            this.setState({ fields })
        }
        _fetchWithAuthzHandling({ url: '/api/acte/localAuthority/current/hasAttachmentTypes'})
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                this.setState({hasAttachmentTypes: json})
            })
            .catch(response => {
                response.json().then(json => {
                    _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                })
            })
    }
    handleModeChange = (e, { id }) => {
        const { fields } = this.state
        fields.mode = id
        this.setState({ mode: id, status: '' })
    }
    setStatus = (status) => this.setState({ status })
    render() {
        const { t } = this.context
        const uuid = this.props.uuid
            ? this.state.fields.mode === 'ACTE_BATCH' ? this.props.uuid
                : this.state.fields.actes[0] ? this.state.fields.actes[0].uuid : ''
            : ''
        return (
            <Page title={t('acte.new.title')}>
                {!this.state.hasAttachmentTypes && (
                    <Anomaly type='warning' header={t('api-gateway:notifications.acte.anomaly_attachment_types')} lastHistory={{status: 'FILE_ERROR', message:t('api-gateway:notifications.acte.no_attachment_types')}}/>
                )}
                <Segment>
                    <Menu tabular>
                        {(this.state.fields.mode === 'ACTE' || !this.props.uuid) && (
                            <Menu.Item id='ACTE' active={this.state.fields.mode === 'ACTE'} onClick={this.handleModeChange}>
                                {t('acte.new.acte')}
                            </Menu.Item>
                        )}
                        {(this.state.fields.mode === 'ACTE_BUDGETAIRE' || !this.props.uuid) && (
                            <Menu.Item id='ACTE_BUDGETAIRE' active={this.state.fields.mode === 'ACTE_BUDGETAIRE'} onClick={this.handleModeChange}>
                                {t('acte.new.acte_budgetaire')}
                            </Menu.Item>
                        )}
                        {(this.state.fields.mode === 'ACTE_BATCH' || !this.props.uuid) && (
                            <Menu.Item id='ACTE_BATCH' active={this.state.fields.mode === 'ACTE_BATCH'} onClick={this.handleModeChange}>
                                {t('acte.new.acte_batch')}
                            </Menu.Item>
                        )}
                        {this.state.status && (
                            <Menu.Menu position='right'>
                                <span className='item' style={{ fontStyle: 'italic' }}>{t(`acte.new.formStatus.${this.state.status}`)}</span>
                            </Menu.Menu>
                        )}
                    </Menu>
                    {this.state.fields.mode === 'ACTE' && (
                        <NewActeForm
                            uuid={uuid}
                            draftUuid={this.props.uuid}
                            mode={this.state.fields.mode}
                            setStatus={this.setStatus}
                            status={this.state.status}
                            path={this.props.location.pathname} />
                    )}
                    {this.state.fields.mode === 'ACTE_BUDGETAIRE' && (
                        <NewActeForm
                            uuid={uuid}
                            draftUuid={this.props.uuid}
                            mode={this.state.fields.mode}
                            setStatus={this.setStatus}
                            status={this.state.status}
                            path={this.props.location.pathname} />
                    )}
                    {this.state.fields.mode === 'ACTE_BATCH' && (
                        <NewActeBatchedForm
                            uuid={uuid}
                            setStatus={this.setStatus}
                            status={this.state.status}
                            path={this.props.location.pathname} />
                    )}
                </Segment>
            </Page>
        )
    }
}

export default translate(['acte', 'api-gateway'])(NewActeSwitch)