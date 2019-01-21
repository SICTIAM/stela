import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Segment, Label } from 'semantic-ui-react'
import moment from 'moment'

import History from '../_components/History'
import { FieldInline, Page, FieldValue, LoadingContent, LinkFile } from '../_components/UI'
import Anomaly from '../_components/Anomaly'
import ConfirmModal from '../_components/ConfirmModal'
import { notifications } from '../_util/Notifications'
import { checkStatus } from '../_util/utils'
import { anomalies, hoursBeforeResendPes } from '../_util/constants'
import { withAuthContext } from '../Auth'

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
            pj: false,
            lastHistoryDate: '',
            lastHistoryStatus: '',
            sesileClasseurUrl: null
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
        _fetchWithAuthzHandling({ url: '/api/pes/resend/' + this.props.uuid, context: this.props.authContext })
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
        const regex = '/(1984|1968)/'
        const anomalyType = lastHistory && lastHistory.errors[0] &&lastHistory.errors[0].message.match(regex) ? 'warning' : 'negative'
        return (
            <Page title={pes.objet}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Anomaly type={anomalyType} header={lastHistory && t('pes.status.'+lastHistory.status)} lastHistory={lastHistory} />
                    <Segment>
                        <Label className="labelStatus" color={pes.lastHistoryStatus ? this.getStatusColor(pes.lastHistoryStatus) : 'blue'} ribbon>
                            {pes.lastHistoryStatus && t(`pes.status.${pes.lastHistoryStatus}`)}
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
                        <FieldInline htmlFor="objet" label={t('pes.fields.objet')}>
                            <FieldValue id="objet">{pes.objet}</FieldValue>
                        </FieldInline>
                        {pes.comment && (
                            <FieldInline htmlFor="comment" label={t('pes.fields.comment')}>
                                <FieldValue id="comment">{pes.comment}</FieldValue>
                            </FieldInline>
                        )}
                        <FieldInline htmlFor="creation" label={t('pes.fields.creation')}>
                            <FieldValue id="creation">
                                {moment(pes.creation).format('DD/MM/YYYY')}
                            </FieldValue>
                        </FieldInline>
                        {agent && (
                            <FieldInline htmlFor="agent" label={t('pes.fields.agent')}>
                                <FieldValue id="agent">{agent}</FieldValue>
                            </FieldInline>
                        )}
                        <FieldInline htmlFor="attachment" label={t('pes.fields.attachment')}>
                            <FieldValue id="attachment">
                                <LinkFile url={`/api/pes/${pes.uuid}/file`} text={pes.attachment.filename} />
                            </FieldValue>
                        </FieldInline>
                    </Segment>
                    <History title={t('pes.page.historic')} moduleName="pes" emptyMessage={t('pes.page.no_history')}
                        history={this.state.pes.pesHistories} sesileClasseurUrl={pes.sesileClasseurUrl} />
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(withAuthContext(Pes))
