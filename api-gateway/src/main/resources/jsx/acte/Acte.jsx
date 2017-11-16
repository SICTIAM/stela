import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import moment from 'moment'
import { Grid, Segment, List, Checkbox, Label, Icon } from 'semantic-ui-react'

import { errorNotification } from '../_components/Notifications'
import { Field } from '../_components/UI'
import history from '../_util/history'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import { anomalies } from '../_util/constants'
import ActeHistory from './ActeHistory'
import ActeAnomaly from './ActeAnomaly'
import ActeCancelButton from './ActeCancelButton'

class Acte extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        acteUI: {
            acte: {
                acteAttachment: {},
                annexes: [],
                acteHistories: []
            },
            history: {},
            cancellable: false
        },
        acteFetched: false
    }
    componentDidMount() {
        const uuid = this.props.uuid
        if (uuid !== '') {
            fetchWithAuthzHandling({ url: '/api/acte/' + uuid })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ acteUI: json, acteFetched: true }))
                .catch(response => {
                    response.json().then(json => {
                        console.log(json)
                        this.context._addNotification(errorNotification(this.context.t('notifications.acte.title'), this.context.t(json.message)))
                    })
                    history.push('/acte')
                })
        }
    }
    getStatusColor = (status) => {
        if (['ACK_RECEIVED'].includes(status)) return 'green'
        else if (anomalies.includes(status)) return 'red'
        else return 'blue'
    }
    render() {
        const { t } = this.context
        const acteFetched = renderIf(this.state.acteFetched)
        const acteNotFetched = renderIf(!this.state.acteFetched)
        const acte = this.state.acteUI.acte
        const lastHistory = acte.acteHistories[acte.acteHistories.length - 1]
        const annexes = this.state.acteUI.acte.annexes.map(annexe =>
            <List.Item key={annexe.uuid}>
                <a target='_blank' href={`/api/acte/${acte.uuid}/annexe/${annexe.uuid}`}>{annexe.filename}</a>
            </List.Item>
        )
        return (
            <div>
                {acteFetched(
                    <div>
                        <ActeAnomaly lastHistory={lastHistory} />
                        <Segment>
                            <Label className='labelStatus' color={lastHistory ? this.getStatusColor(lastHistory.status) : 'blue'} ribbon>{lastHistory && t(`acte.status.${lastHistory.status}`)}</Label>
                            <Grid>
                                <Grid.Column width={12}><h1>{acte.objet}</h1></Grid.Column>
                                <Grid.Column width={4} style={{ textAlign: 'right' }}>
                                    {renderIf(lastHistory && lastHistory.status === 'ACK_RECEIVED')(
                                        <a className='ui blue basic icon button' href={`/api/acte/${acte.uuid}/AR_${acte.uuid}.pdf`} target='_blank' title='Télécharger le justificatif'><Icon name='download' /></a>
                                    )}
                                    <ActeCancelButton isCancellable={this.state.acteUI.cancellable} uuid={this.state.acteUI.acte.uuid} />
                                </Grid.Column>
                            </Grid>

                            <Field htmlFor="number" label={t('acte.fields.number')}>
                                <span id="number">{acte.number}</span>
                            </Field>
                            <Field htmlFor="decision" label={t('acte.fields.decision')}>
                                <span id="decision">{moment(acte.decision).format('DD/MM/YYYY')}</span>
                            </Field>
                            <Field htmlFor="nature" label={t('acte.fields.nature')}>
                                <span id="nature">{t(`acte.nature.${acte.nature}`)}</span>
                            </Field>
                            <Field htmlFor="code" label={t('acte.fields.code')}>
                                <span id="code">{acte.codeLabel} ({acte.code})</span>
                            </Field>
                            <Field htmlFor="filename" label={t('acte.fields.file')}>
                                <span id="filename"><a target='_blank' href={`/api/acte/${acte.uuid}/file`}>{acte.acteAttachment.filename}</a></span>
                            </Field>
                            <Field htmlFor="annexes" label={t('acte.fields.annexes')}>
                                {renderIf(annexes.length > 0)(
                                    <List id="annexes">
                                        {annexes}
                                    </List>
                                )}
                            </Field>
                            <Field htmlFor="public" label={t('acte.fields.public')}>
                                <Checkbox id="public" checked={acte.public} disabled />
                            </Field>

                            <Grid>
                                <Grid.Column width={4}><label htmlFor="publicWebsite">{t('acte.fields.publicWebsite')}</label></Grid.Column>
                                <Grid.Column width={12}><Checkbox id="publicWebsite" checked={acte.publicWebsite} disabled /></Grid.Column>
                            </Grid>
                        </Segment>
                        <ActeHistory history={this.state.acteUI.acte.acteHistories} />
                    </div>
                )}
                {acteNotFetched(
                    <p>{t('acte.page.non_existent_act')}</p>
                )}
            </div>
        )
    }
}

export default translate(['acte'])(Acte)