import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import moment from 'moment'
import { Grid, Segment, List, Checkbox, Label, Dropdown, Button, Popup } from 'semantic-ui-react'

import CourrierSimple from './CourrierSimple'
import Defere from './Defere'
import LettreObservation from './LettreObservation'
import DemandePiecesComplementaires from './DemandePiecesComplementaires'
import DraggablePosition from '../_components/DraggablePosition'
import { Field, Page, FieldValue } from '../_components/UI'
import Anomaly from '../_components/Anomaly'
import History from '../_components/History'
import { notifications } from '../_util/Notifications'
import { checkStatus, fetchWithAuthzHandling, getHistoryStatusTranslationKey } from '../_util/utils'
import { anomalies } from '../_util/constants'
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
            acteACK: false,
            lastMetierHistory: {
                status: '',
                flux: ''
            },
            stampPosition: {
                x: 10,
                y: 10
            }
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
                        this.context._addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                    })
                })
        }
    }
    handleChangeDeltaPosition = (stampPosition) => {
        const { acteUI } = this.state
        acteUI.stampPosition = stampPosition
        this.setState({ acteUI })
    }
    getStatusColor = (status) => {
        if (['ACK_RECEIVED'].includes(status)) return 'green'
        else if (anomalies.includes(status)) return 'red'
        else return 'blue'
    }
    render() {
        const { t } = this.context
        const { acteACK, lastMetierHistory } = this.state.acteUI
        const acteFetched = renderIf(this.state.acteFetched)
        const acteNotFetched = renderIf(!this.state.acteFetched)
        const acte = this.state.acteUI.acte
        const lastHistory = acte.acteHistories[acte.acteHistories.length - 1]
        const annexes = this.state.acteUI.acte.annexes.map(annexe =>
            <List.Item key={annexe.uuid}>
                <FieldValue><a target='_blank' href={`/api/acte/${acte.uuid}/annexe/${annexe.uuid}`}>{annexe.filename}</a></FieldValue>
            </List.Item>
        )
        const stampPosition = (
            <div>
                <DraggablePosition
                    style={{ marginBottom: '0.5em' }}
                    backgroundImage={`/api/acte/${acte.uuid}/file/thumbnail`}
                    label={t('acte.stamp_pad.pad_label')}
                    height={300}
                    width={190}
                    labelColor='#000'
                    position={this.state.acteUI.stampPosition}
                    handleChange={this.handleChangeDeltaPosition} />
                <div style={{ textAlign: 'center' }}>
                    <a className='ui blue icon button' target='_blank'
                        href={`/api/acte/${acte.uuid}/file/stamped?x=${this.state.acteUI.stampPosition.x}&y=${this.state.acteUI.stampPosition.y}`}>
                        {t('api-gateway:form.download')}
                    </a>
                </div>
            </div >
        )
        // TODO: Find a way to have a LEFT submenu instead of the popup
        const isCourrierSimple = this.state.acteUI.acte.acteHistories.some(acteHistory => acteHistory.flux === 'COURRIER_SIMPLE')
        const isDefere = this.state.acteUI.acte.acteHistories.some(acteHistory => acteHistory.status === 'DEFERE_RECEIVED')
        const isLettreObservation = this.state.acteUI.acte.acteHistories.some(acteHistory => acteHistory.status === 'LETTRE_OBSERVATION_RECEIVED')
        const isDemandePiecesComplementaires = this.state.acteUI.acte.acteHistories.some(acteHistory => acteHistory.status === 'DEMANDE_PIECE_COMPLEMENTAIRE_RECEIVED')
        return (
            <Page title={acte.objet}>
                {acteFetched(
                    <div>
                        <Anomaly header={t('acte.history.message')} lastHistory={lastHistory} />

                        {isDefere &&
                            <Defere acteUuid={this.props.uuid} acteHistories={this.state.acteUI.acte.acteHistories} />
                        }
                        {isDemandePiecesComplementaires &&
                            <DemandePiecesComplementaires acteUuid={this.props.uuid} acteHistories={this.state.acteUI.acte.acteHistories} />
                        }
                        {isLettreObservation &&
                            <LettreObservation acteUuid={this.props.uuid} acteHistories={this.state.acteUI.acte.acteHistories} />
                        }
                        {isCourrierSimple &&
                            <CourrierSimple acteUuid={this.props.uuid} acteHistories={this.state.acteUI.acte.acteHistories} />
                        }

                        <Segment>
                            <Label className='labelStatus' color={lastMetierHistory ? this.getStatusColor(lastMetierHistory.status) : 'blue'} ribbon>
                                {lastMetierHistory && t(getHistoryStatusTranslationKey('acte', lastMetierHistory))}
                            </Label>
                            <div style={{ textAlign: 'right' }}>
                                <Dropdown basic trigger={<Button basic color='blue'>{t('api-gateway:form.download')}</Button>} icon={false}>
                                    <Dropdown.Menu>
                                        <a className='item' href={`/api/acte/${acte.uuid}/file`} target='_blank'>
                                            {t('acte.page.download_original')}
                                        </a>

                                        {(acteACK) &&
                                            <a className='item' href={`/api/acte/${acte.uuid}/AR_${acte.uuid}.pdf`} target='_blank'>
                                                {t('acte.page.download_justificative')}
                                            </a>
                                        }
                                        {(acteACK) &&
                                            <Dropdown.Item>
                                                <Popup content={stampPosition} on='click' position='left center'
                                                    trigger={<Dropdown item icon='none' text={t('acte.stamp_pad.download_stamped_acte')} />}
                                                />
                                            </Dropdown.Item>
                                        }
                                    </Dropdown.Menu>
                                </Dropdown>

                                <ActeCancelButton isCancellable={this.state.acteUI.acteACK} uuid={this.state.acteUI.acte.uuid} />
                            </div>

                            <Field htmlFor="number" label={t('acte.fields.number')}>
                                <FieldValue id="number">{acte.number}</FieldValue>
                            </Field>
                            <Field htmlFor="decision" label={t('acte.fields.decision')}>
                                <FieldValue id="decision">{moment(acte.decision).format('DD/MM/YYYY')}</FieldValue>
                            </Field>
                            <Field htmlFor="nature" label={t('acte.fields.nature')}>
                                <FieldValue id="nature">{t(`acte.nature.${acte.nature}`)}</FieldValue>
                            </Field>
                            <Field htmlFor="code" label={t('acte.fields.code')}>
                                <FieldValue id="code">{acte.codeLabel} ({acte.code})</FieldValue>
                            </Field>
                            <Grid>
                                <Grid.Column width={4}>
                                    <label style={{ verticalAlign: 'middle' }} htmlFor="acteAttachment">{t('acte.fields.acteAttachment')}</label>
                                </Grid.Column>
                                <Grid.Column width={12}>
                                    <FieldValue id="acteAttachment">
                                        <a target='_blank' href={`/api/acte/${acte.uuid}/file`}>{acte.acteAttachment.filename}</a>
                                    </FieldValue>
                                </Grid.Column>
                            </Grid>
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
                        <History
                            title={t('acte.page.historic')}
                            moduleName='acte'
                            emptyMessage={t('acte.page.no_history')}
                            history={this.state.acteUI.acte.acteHistories} />
                    </div>
                )}
                {acteNotFetched(
                    <p>{t('acte.page.non_existing_act')}</p>
                )}
            </Page>
        )
    }
}

export default translate(['acte', 'api-gateway'])(Acte)