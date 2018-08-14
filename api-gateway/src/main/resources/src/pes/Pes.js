import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Segment, Label } from 'semantic-ui-react'
import moment from 'moment'

import History from '../_components/History'
import { Field, Page, FieldValue, LoadingContent, LinkFile } from '../_components/UI'
import Anomaly from '../_components/Anomaly'
import ConfirmModal from '../_components/ConfirmModal'
import { notifications } from '../_util/Notifications'
import { checkStatus } from '../_util/utils'
import { anomalies, hoursBeforeResendPes } from '../_util/constants'

class Pes extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        pes: {
            uuid: '',
            creation: null,
            objet: '',
            comment: '',
            attachment: {
                uuid: '',
                filename: ''
            },
            pesHistories: [],
            localAuthority: {},
            profileUuid: '',
            pj: false
        },
        agent: '',
        fetchStatus: ''
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        this.setState({ fetchStatus: 'loading' })
        if (this.props.uuid) {
            _fetchWithAuthzHandling({ url: '/api/pes/' + this.props.uuid })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ pes: json, fetchStatus: 'fetched' }, this.getAgentInfos))
                .catch(response => {
                    this.setState({ fetchStatus: response.status === 404 ? 'pes.page.non_existing_pes' : 'api-gateway:error.default' })
                    response.json().then(json =>
                        _addNotification(notifications.defaultError, 'notifications.pes.title', json.message)
                    )
                })
        }
    }
    getAgentInfos = () => {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/profile/' + this.state.pes.profileUuid })
            .then(response => response.json())
            .then(json => this.setState({ agent: `${json.agent.given_name} ${json.agent.family_name}` }))
    }
    getStatusColor = status => {
        if (['ACK_RECEIVED'].includes(status)) return 'green'
        else if (anomalies.includes(status)) return 'red'
        else return 'blue'
    }
    reSendFlux = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/pes/resend/' + this.props.uuid, context: this.context })
            .then(checkStatus)
            .then(() => _addNotification(notifications.pes.sent))
            .catch(response =>
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.pes.title', text))
            )
    }
    render() {
        const { t } = this.context
        const { pes, agent } = this.state
        const lastHistory = pes.pesHistories[pes.pesHistories.length - 1]
        const canResend = lastHistory && (
            lastHistory.status === 'MAX_RETRY_REACH' || (
                (lastHistory.status === 'SENT' || lastHistory.status === 'RESENT' || lastHistory.status === 'MANUAL_RESENT') && (
                    moment(lastHistory.date).isSameOrBefore(
                        moment().subtract(hoursBeforeResendPes, 'hour')
                    )
                )
            )
        )
        return (
            <Page title={pes.objet}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Anomaly header={t('pes.page.title_anomaly')} lastHistory={lastHistory} />
                    <Segment>
                        <Label className="labelStatus" color={lastHistory ? this.getStatusColor(lastHistory.status) : 'blue'} ribbon>
                            {lastHistory && t(`pes.status.${lastHistory.status}`)}
                        </Label>
                        <div style={{ textAlign: 'right' }}>
                            {canResend && (
                                <ConfirmModal onConfirm={this.reSendFlux} text={t('pes.page.re_send_confirm')}>
                                    <Button type="submit" primary basic>
                                        {t('pes.page.re_send')}
                                    </Button>
                                </ConfirmModal>
                            )}
                        </div>
                        <Field htmlFor="objet" label={t('pes.fields.objet')}>
                            <FieldValue id="objet">{pes.objet}</FieldValue>
                        </Field>
                        {pes.comment && (
                            <Field htmlFor="comment" label={t('pes.fields.comment')}>
                                <FieldValue id="comment">{pes.comment}</FieldValue>
                            </Field>
                        )}
                        <Field htmlFor="creation" label={t('pes.fields.creation')}>
                            <FieldValue id="creation">
                                {moment(pes.creation).format('DD/MM/YYYY')}
                            </FieldValue>
                        </Field>
                        {agent && (
                            <Field htmlFor="agent" label={t('pes.fields.agent')}>
                                <FieldValue id="agent">{agent}</FieldValue>
                            </Field>
                        )}
                        <Field htmlFor="attachment" label={t('pes.fields.attachment')}>
                            <FieldValue id="attachment">
                                <LinkFile url={`/api/pes/${pes.uuid}/file`} text={pes.attachment.filename} />
                            </FieldValue>
                        </Field>
                    </Segment>
                    <History title={t('pes.page.historic')} moduleName="pes" emptyMessage={t('pes.page.no_history')}
                        history={this.state.pes.pesHistories} />
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(Pes)