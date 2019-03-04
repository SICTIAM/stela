import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import moment from 'moment'
import { Grid, Segment, List, Checkbox, Label, Dropdown, Button, Popup } from 'semantic-ui-react'

import CourrierSimple from './CourrierSimple'
import Defere from './Defere'
import LettreObservation from './LettreObservation'
import DemandePiecesComplementaires from './DemandePiecesComplementaires'
import DraggablePosition from '../_components/DraggablePosition'
import { FieldInline, Page, FieldValue, LoadingContent, LinkFile } from '../_components/UI'
import Anomaly from '../_components/Anomaly'
import ConfirmModal from '../_components/ConfirmModal'
import History from '../_components/History'
import { notifications } from '../_util/Notifications'
import { checkStatus, getHistoryStatusTranslationKey, isPDF } from '../_util/utils'
import { anomalies, hoursBeforeResendActe } from '../_util/constants'
import ActeCancelButton from './ActeCancelButton'
import { withAuthContext } from '../Auth'

class Acte extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
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
        agent: '',
        agentGroups: [],
        fetchStatus: '',
        republished: false,
        thumbnail: {
            image:'',
            orientation: ''
        },
        thumbnailStatus: 'loading',
        isCertificateValid: false
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        this.setState({ fetchStatus: 'loading' })
        const uuid = this.props.uuid
        if (uuid !== '') {
            _fetchWithAuthzHandling({ url: '/api/acte/' + uuid })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({ acteUI: json, fetchStatus: 'fetched' }, () => {
                    this.getAgentInfos()
                    this.checkCertificateIsValid()
                }))
                .catch(response => {
                    this.setState({ fetchStatus: response.status === 404 ? 'acte.page.non_existing_act' : 'api-gateway:error.default' })
                    response.json().then(json => {
                        _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                    })
                })


        }
    }

    checkCertificateIsValid = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        if(this.props.authContext.isLoggedIn) {
            _fetchWithAuthzHandling({url: '/api/admin/certificate/is-valid'})
                .then(checkStatus)
                .then(response => response.json())
                .then(certificate => {
                    this.setState({isCertificateValid: certificate})
                })
                .catch(response => {
                    response.text().then(text => {
                        _addNotification(notifications.defaultError, 'notifications.title', text)
                    })
                })
        }
    }

    fetchThumbnail = () => {
        const { _fetchWithAuthzHandling} = this.context
        const {thumbnailStatus} = this.state
        const uuid = this.props.uuid

        if(thumbnailStatus !== 'fetched') {
            _fetchWithAuthzHandling({url: `/api/acte/${uuid}/file/thumbnail`})
                .then(checkStatus)
                .then(res => res.json())
                .then(json => {
                    let stampPosition = this.state.acteUI.stampPosition
                    if (json.orientation === 'LANDSCAPE') {
                        this.setState(prevState =>
                            ({
                                thumbnail: json,
                                thumbnailStatus: 'fetched',
                                acteUI: {
                                    ...prevState.acteUI,
                                    stampPosition: {x: stampPosition.y, y: stampPosition.x}
                                }
                            }))
                    } else {
                        this.setState({thumbnail: json,thumbnailStatus: 'fetched'})
                    }
                })
                .catch(err => {
                    this.setState({thumbnailStatus: 'loading'})
                    console.error(err)
                })
        }
    };


    getAgentInfos = () => {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/profile/' + this.state.acteUI.acte.profileUuid })
            .then(response => response.json())
            .then(json => this.setState({ agent: `${json.agent.given_name} ${json.agent.family_name}`, agentGroups: json.groups }))
    }
    agentBelongsToTheGroup = () => {
        const {agentGroups, acteUI} = this.state
        return agentGroups.map(group =>  group.uuid).includes(acteUI.acte.groupUuid)
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
    republish = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const uuid = this.props.uuid
        if (uuid !== '') {
            _fetchWithAuthzHandling({ url: '/api/acte/' + uuid + '/republish', method: 'POST', context: this.props.authContext })
                .then(checkStatus)
                .then(() => {
                    _addNotification(notifications.acte.republished)
                    this.setState({ republished: true })
                })
                .catch(response => {
                    response.text().then(text => _addNotification(notifications.acte.republishedError))
                })
        }
    }
    fileLinks = (url, filename) => {
        const { t } = this.context
        const { acteACK } =this.state.acteUI
        return (acteACK && isPDF(filename)) ? <Fragment><LinkFile url={`${url}/stamped`} text={filename} /> (<LinkFile url={url} text={t('acte.page.original')} />)</Fragment>
            : <LinkFile url={url} text={filename} ariaLabel={'Download '+filename}/>
    }

    thumbnailSize = () => {
        let height, width = 0
        if(this.state.thumbnail.orientation ==='LANDSCAPE'){
            height = 190
            width = 300
        }else {
            height = 300
            width =  190
        }

        return {height: height, width: width}
    }

    draggableBoxSize = () => {
        let boxHeight, boxWidth = 0
        if(this.state.thumbnail.orientation ==='LANDSCAPE'){
            boxHeight = 70
            boxWidth = 25
        }else {
            boxHeight = 25
            boxWidth =  70
        }

        return {boxHeight: boxHeight, boxWidth: boxWidth}
    }

    render() {
        const { t } = this.context
        const { acteACK, acte } = this.state.acteUI
        const { thumbnailStatus } = this.state
        const lastMetierHistory = {
            date: acte.lastHistoryDate,
            status: acte.lastHistoryStatus,
            flux: acte.lastHistoryFlux
        }
        const lastHistory = acte.acteHistories[acte.acteHistories.length - 1]
        const annexes = this.state.acteUI.acte.annexes.map(annexe =>
            <List.Item key={annexe.uuid}>
                <FieldValue>
                    {this.fileLinks(`/api/acte/${acte.uuid}/annexe/${annexe.uuid}`, annexe.filename)}
                </FieldValue>
            </List.Item>
        )
        const isAgentBelongsToTheGroup = this.agentBelongsToTheGroup()
        const isActeAttachmentPDF = acte.acteAttachment.filename && acte.acteAttachment.filename.endsWith('.pdf')

        const {height : thumbnailHeight, width: thumbnailWidth} = this.thumbnailSize()
        const {boxWidth, boxHeight} = this.draggableBoxSize()

        const stampPosition = (
            <div>
                <DraggablePosition
                    style={{ marginBottom: '0.5em' }}
                    backgroundImage={'data:image/png;base64,' + this.state.thumbnail.image.trim()}
                    imageOrientation={this.state.thumbnail.orientation}
                    label={t('acte:stamp_pad.pad_label')}
                    height={thumbnailHeight}
                    width={thumbnailWidth}
                    labelColor='#000'
                    position={this.state.acteUI.stampPosition}
                    handleChange={this.handleChangeDeltaPosition}
                    boxHeight={boxHeight}AR
                    boxWidth={boxWidth}/>
                <div style={{ textAlign: 'center' }}>
                    <a className='ui primary primary icon button' target='_blank' aria-label={t('api-gateway:form.download')}
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
        const isDemandePiecesComplementaires = this.state.acteUI.acte.acteHistories.some(acteHistory =>
            acteHistory.status === 'DEMANDE_PIECE_COMPLEMENTAIRE_RECEIVED'
        )
        const canRepublish = lastHistory && !this.state.republished && (anomalies.includes(lastHistory.status) ||
            (lastHistory.status === 'SENT' && moment(lastHistory.date).isSameOrBefore(moment().subtract(hoursBeforeResendActe, 'hour'))))
        const dropdownButton = <Button basic color='primary'>{t('api-gateway:form.download')}</Button>

        return (
            <Page title={acte.objet}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Anomaly header={lastHistory && t('acte.status.'+lastHistory.status)}  lastHistory={lastHistory} />

                    {isDefere && (
                        <Defere acteUuid={this.props.uuid} acteHistories={this.state.acteUI.acte.acteHistories} />
                    )}
                    {isDemandePiecesComplementaires && (
                        <DemandePiecesComplementaires acteUuid={this.props.uuid} acteHistories={this.state.acteUI.acte.acteHistories} />
                    )}
                    {isLettreObservation && (
                        <LettreObservation acteUuid={this.props.uuid} acteHistories={this.state.acteUI.acte.acteHistories} />
                    )}
                    {isCourrierSimple && (
                        <CourrierSimple acteUuid={this.props.uuid} acteHistories={this.state.acteUI.acte.acteHistories} />
                    )}

                    <Segment>
                        <Label className='labelStatus' color={acte.lastHistoryStatus ? this.getStatusColor(acte.lastHistoryStatus) : 'primary anatra'} ribbon>
                            {acte.lastHistoryStatus && t(getHistoryStatusTranslationKey('acte', lastMetierHistory))}
                        </Label>
                        <div style={{ textAlign: 'right' }}>
                            <Dropdown basic direction='left' trigger={dropdownButton} icon={false} onClick={this.fetchThumbnail}>
                                <Dropdown.Menu>
                                    {acteACK &&  isActeAttachmentPDF && (
                                        <Dropdown.Item>
                                            <LoadingContent fetchStatus={thumbnailStatus}>
                                                <Popup content={stampPosition} on='click' position='left center'
                                                    trigger={<Dropdown item icon='none'
                                                        text={t('acte:stamp_pad.download_stamped_acte')}/>}
                                                />
                                            </LoadingContent>
                                        </Dropdown.Item>
                                    )}
                                    {acteACK && (
                                        <a className='item' aria-label={t('acte.page.download_justificative')} href={`/api/acte/${acte.uuid}/AR_${acte.uuid}.pdf`} target='_blank'>
                                            {t('acte.page.download_justificative')}
                                        </a>
                                    )}
                                    <a className='item' aria-label={t('acte.page.download_original')} href={`/api/acte/${acte.uuid}/file`} target='_blank'>
                                        {t('acte.page.download_original')}
                                    </a>
                                </Dropdown.Menu>
                            </Dropdown>
                            {canRepublish && (
                                <ConfirmModal onConfirm={this.republish} text={t('acte.page.republish_confirm')}>
                                    <Button basic color={'orange'}>{t('acte.page.republish')}</Button>
                                </ConfirmModal>
                            )}

                            {this.state.isCertificateValid && isAgentBelongsToTheGroup &&
                                <ActeCancelButton isCancellable={this.state.acteUI.acteACK} uuid={this.state.acteUI.acte.uuid}/>
                            }
                        </div>

                        <FieldInline htmlFor="number" label={t('acte.fields.number')}>
                            <FieldValue id="number">{acte.number}</FieldValue>
                        </FieldInline>
                        <FieldInline htmlFor="decision" label={t('acte.fields.decision')}>
                            <FieldValue id="decision">{moment(acte.decision).format('DD/MM/YYYY')}</FieldValue>
                        </FieldInline>
                        <FieldInline htmlFor="nature" label={t('acte.fields.nature')}>
                            <FieldValue id="nature">{t(`acte.nature.${acte.nature}`)}</FieldValue>
                        </FieldInline>
                        <FieldInline htmlFor="code" label={t('acte.fields.code')}>
                            <FieldValue id="code">{acte.codeLabel} ({acte.code})</FieldValue>
                        </FieldInline>
                        {this.state.agent && (
                            <FieldInline htmlFor='agent' label={t('acte.fields.agent')}>
                                <FieldValue id='agent'>{this.state.agent}</FieldValue>
                            </FieldInline>
                        )}
                        {acte.acteAttachment && (
                            <Grid>
                                <Grid.Column width={4}>
                                    <label style={{ verticalAlign: 'middle' }} htmlFor="acteAttachment">{t('acte.fields.acteAttachment')}</label>
                                </Grid.Column>
                                <Grid.Column width={12}>
                                    <FieldValue id="acteAttachment">
                                        {this.fileLinks(`/api/acte/${acte.uuid}/file`, acte.acteAttachment.filename)}
                                    </FieldValue>
                                </Grid.Column>
                            </Grid>
                        )}
                        {annexes.length > 0 && (
                            <FieldInline htmlFor="annexes" label={t('acte.fields.annexes')}>
                                <List id="annexes">
                                    {annexes}
                                </List>
                            </FieldInline>
                        )}
                        <FieldInline htmlFor='multipleChannels' label={t('acte.fields.multipleChannels')}>
                            <Checkbox label={<div className='box'></div>} id="multipleChannels" checked={acte.multipleChannels} disabled />
                        </FieldInline>
                        <FieldInline htmlFor='public' label={t('acte.fields.public')}>
                            <Checkbox label={<div className='box'></div>} id="public" checked={acte.public} disabled />
                        </FieldInline>

                        <Grid>
                            <Grid.Column width={4}><label htmlFor='publicWebsite'>{t('acte.fields.publicWebsite')}</label></Grid.Column>
                            <Grid.Column width={12}><Checkbox label={<div className='box'></div>} id="publicWebsite" checked={acte.publicWebsite} disabled /></Grid.Column>
                        </Grid>
                    </Segment>
                    <History
                        title={t('acte.page.historic')}
                        moduleName='acte'
                        emptyMessage={t('acte.page.no_history')}
                        history={this.state.acteUI.acte.acteHistories} />
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['acte', 'api-gateway'])(withAuthContext(Acte))
