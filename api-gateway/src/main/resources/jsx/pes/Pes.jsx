import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button ,Segment, Label } from 'semantic-ui-react'
import moment from 'moment'

import History from '../_components/History'
import { Field, Page } from '../_components/UI'
import Anomaly from '../_components/Anomaly'
import { notifications } from '../_util/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import { anomalies } from '../_util/constants'

class Pes extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
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
            pj: false
        },
        fetched: false
    }
    componentDidMount() {
        if (this.props.uuid) {
            fetchWithAuthzHandling({ url: '/api/pes/' + this.props.uuid })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ pes: json, fetched: true }))
                .catch(response => {
                    response.json().then(json => {
                        this.context._addNotification(notifications.defaultError, 'notifications.pes.title', json.message)
                    })
                })
        }
    }
    getStatusColor = (status) => {
        if (['ACK_RECEIVED'].includes(status)) return 'green'
        else if (anomalies.includes(status)) return 'red'
        else return 'blue'
    }
    reSendFlux = () => {
        fetchWithAuthzHandling({ url: '/api/pes/resend/' + this.props.uuid, context: this.context })
            .then(checkStatus)
            .then(response => {
                    this.context._addNotification(notifications.pes.sent)
                    history.push('/pes/' + this.props.uuid,)
                })
            .catch( response => {
                response.text().then(text => this.context._addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    render() {
        const { t } = this.context
        const { pes, fetched } = this.state
        const lastHistory = pes.pesHistories[pes.pesHistories.length - 1]
        return (
            <Page title={pes.objet}>
                {fetched &&
                    <div>
                        <Anomaly header={t('pes.page.title_anomaly')} lastHistory={lastHistory} />
                        <Segment>
                            <Label className='labelStatus' color={lastHistory ? this.getStatusColor(lastHistory.status) : 'blue'} ribbon>{lastHistory && t(`pes.status.${lastHistory.status}`)}</Label>
                             <div style={{ textAlign: 'right' }}>
                                {(lastHistory && lastHistory.status === 'MAX_RETRY_REACH') &&
                                    <Button type='submit' primary basic onClick={this.reSendFlux}>{t('pes.page.re_send')}</Button>
                                }
                            </div>
                            <Field htmlFor='objet' label={t('pes.fields.objet')}>
                                <span id='objet'>{pes.objet}</span>
                            </Field>
                            <Field htmlFor='comment' label={t('pes.fields.comment')}>
                                <span id='comment'>{pes.comment}</span>
                            </Field>
                            <Field htmlFor='creation' label={t('pes.fields.creation')}>
                                <span id='creation'>{moment(pes.creation).format('DD/MM/YYYY')}</span>
                            </Field>
                            <Field htmlFor='attachment' label={t('pes.fields.objet')}>
                                <a id='attachment' target='_blank' href={`/api/pes/${pes.uuid}/file`}>{pes.attachment.filename}</a>
                            </Field>
                        </Segment>
                        <History
                            title={t('pes.page.historic')}
                            moduleName='pes'
                            emptyMessage={t('pes.page.no_history')}
                            history={this.state.pes.pesHistories} />
                    </div>
                }
                {!fetched && <p>{t('pes.page.non_existing_pes')}</p>}
            </Page>
        )
    }
}

export default translate(['pes', 'api-gateway'])(Pes)