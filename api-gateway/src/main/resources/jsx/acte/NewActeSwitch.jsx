import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Menu, Segment, Grid } from 'semantic-ui-react'

import NewActeForm from './NewActeForm'
import NewActeBatchedForm from './NewActeBatchedForm'
import { errorNotification } from '../_components/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'

class NewActeSwitch extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        fields: {
            uuid: '',
            mode: '',
            actes: []
        },
        status: ''
    }
    componentDidMount() {
        if (this.props.uuid) {
            fetchWithAuthzHandling({ url: `/api/acte/drafts/${this.props.uuid}` })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ fields: json }))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                    })
                })
        } else {
            const { fields } = this.state
            fields.mode = 'ACTE'
            this.setState({ fields })
        }
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
            <Segment>
                <Grid>
                    <Grid.Column width={12}><h1>{t('acte.new.title')}</h1></Grid.Column>
                    <Grid.Column width={4} style={{ textAlign: 'right' }}>
                        {renderIf(this.state.status)(
                            <span style={{ fontStyle: 'italic' }}>{t(`acte.new.formStatus.${this.state.status}`)}</span>
                        )}
                    </Grid.Column>
                </Grid>
                <Menu tabular>
                    {renderIf(this.state.fields.mode === 'ACTE' || !this.props.uuid)(
                        <Menu.Item id='ACTE' active={this.state.fields.mode === 'ACTE'} onClick={this.handleModeChange}>
                            {t('acte.new.acte')}
                        </Menu.Item>
                    )}
                    {renderIf(this.state.fields.mode === 'ACTE_BUDGETAIRE' || !this.props.uuid)(
                        <Menu.Item id='ACTE_BUDGETAIRE' active={this.state.fields.mode === 'ACTE_BUDGETAIRE'} onClick={this.handleModeChange}>
                            {t('acte.new.acte_budgetaire')}
                        </Menu.Item>
                    )}
                    {renderIf(this.state.fields.mode === 'ACTE_BATCH' || !this.props.uuid)(
                        <Menu.Item id='ACTE_BATCH' active={this.state.fields.mode === 'ACTE_BATCH'} onClick={this.handleModeChange}>
                            {t('acte.new.acte_batch')}
                        </Menu.Item>
                    )}
                </Menu>
                {renderIf(this.state.fields.mode === 'ACTE')(
                    <NewActeForm
                        uuid={uuid}
                        draftUuid={this.props.uuid}
                        mode={this.state.fields.mode}
                        setStatus={this.setStatus}
                        status={this.state.status} />
                )}
                {renderIf(this.state.fields.mode === 'ACTE_BUDGETAIRE')(
                    <NewActeForm
                        uuid={uuid}
                        draftUuid={this.props.uuid}
                        mode={this.state.fields.mode}
                        setStatus={this.setStatus}
                        status={this.state.status} />
                )}
                {renderIf(this.state.fields.mode === 'ACTE_BATCH')(
                    <NewActeBatchedForm
                        uuid={uuid}
                        setStatus={this.setStatus}
                        status={this.state.status} />
                )}
            </Segment>
        )
    }
}

export default translate(['acte', 'api-gateway'])(NewActeSwitch)