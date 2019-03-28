import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {translate} from 'react-i18next'
import moment from 'moment'
import {Grid, Segment, List, Label, Dropdown, Button, Popup} from 'semantic-ui-react'

import DraggablePosition from '../_components/DraggablePosition'
import {FieldInline, Page, FieldValue, LoadingContent, LinkFile} from '../_components/UI'
import {notifications} from '../_util/Notifications'
import {checkStatus} from '../_util/utils'

class ActePublic extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        fields: {
            localAuthority: {
                name: ''
            },
            acteAttachment: {},
            annexes: [],
            acteHistories: []
        },
        stampPosition: {
            x: 10,
            y: 10
        },
        fetchStatus: '',
        thumbnail: {
            image: '',
            orientation: ''
        },
        thumbnailStatus: 'loading'
    }

    componentDidMount() {
        const {_fetchWithAuthzHandling, _addNotification} = this.context
        this.setState({fetchStatus: 'loading'})
        const uuid = this.props.uuid
        if (uuid !== '') {
            _fetchWithAuthzHandling({url: '/api/acte/public/' + uuid})
                .then(checkStatus)
                .then(response => response.json())
                .then(json => this.setState({fields: json, fetchStatus: 'fetched'}))
                .catch(response => {
                    this.setState({fetchStatus: response.status === 404 ? 'acte.page.non_existing_act' : 'api-gateway:error.default'})
                    response.json().then(json => {
                        _addNotification(notifications.defaultError, 'notifications.acte.title', json.message)
                    })
                })
        }
    }

    fetchThumbnail = () => {
        const {_fetchWithAuthzHandling} = this.context
        const {thumbnailStatus} = this.state
        const uuid = this.props.uuid

        if (thumbnailStatus !== 'fetched') {
            _fetchWithAuthzHandling({url: `/api/acte/${uuid}/file/thumbnail`})
                .then(checkStatus)
                .then(res => res.json())
                .then(json => {
                    this.setState({thumbnail: json, thumbnailStatus: 'fetched'})
                })
                .catch(err => {
                    this.setState({thumbnailStatus: 'loading'})
                    console.error(err)
                })
        }
    };

    thumbnailSize = () => {
        let height, width = 0
        if (this.state.thumbnail.orientation === 'LANDSCAPE') {
            height = 190
            width = 300
        } else {
            height = 300
            width = 190
        }

        return {height: height, width: width}
    }

    handleChangeDeltaPosition = (stampPosition) => this.setState({stampPosition})

    render() {
        const {t} = this.context
        const acte = this.state.fields
        const historyAR = acte.acteHistories.find(acteHistory => acteHistory.status === 'ACK_RECEIVED')
        const dropDownButton = <Button basic color='blue'>{t('api-gateway:form.download')}</Button>
        const annexes = this.state.fields.annexes.map(annexe =>
            <List.Item key={annexe.uuid}>
                <FieldValue>
                    <LinkFile url={`/api/acte/public/${acte.uuid}/annexe/${annexe.uuid}`} text={annexe.filename}/>
                </FieldValue>
            </List.Item>
        )
        const {height: thumbnailHeight, width: thumbnailWidth} = this.thumbnailSize()
        const stampPosition = (
            <div>
                <DraggablePosition
                    style={{marginBottom: '0.5em'}}
                    backgroundImage={'data:image/png;base64,' + this.state.thumbnail.image.trim()}
                    label={t('acte:stamp_pad.pad_label')}
                    height={thumbnailHeight}
                    width={thumbnailWidth}
                    labelColor='#000'
                    position={this.state.stampPosition}
                    handleChange={this.handleChangeDeltaPosition}/>
                <div style={{textAlign: 'center'}}>
                    <a className='ui blue icon button' target='_blank'
                        href={`/api/acte/public/${acte.uuid}/file/stamped?x=${this.state.stampPosition.x}&y=${this.state.stampPosition.y}`}>
                        {t('api-gateway:form.download')}
                    </a>
                </div>
            </div>
        )
        const isActeAttachmentPDF = acte && acte.acteAttachment && acte.acteAttachment.filename && acte.acteAttachment.filename.endsWith('.pdf')

        return (
            <Page title={acte.objet}>
                <LoadingContent fetchStatus={this.state.fetchStatus}>
                    <Segment>
                        <Label className='labelStatus' color={'green'}
                            ribbon>{t('acte:acte.status.ACK_RECEIVED')}</Label>
                        <div style={{textAlign: 'right'}}>
                            <Dropdown basic direction='left' trigger={dropDownButton} icon={false}
                                onClick={this.fetchThumbnail}>
                                <Dropdown.Menu>
                                    <a className='item' href={`/api/acte/public/${acte.uuid}/file`} target='_blank'>
                                        {t('acte.page.download_original')}
                                    </a>
                                    <a className='item' href={`/api/acte/public/${acte.uuid}/AR_${acte.uuid}.pdf`}
                                        target='_blank'>
                                        {t('acte.page.download_justificative')}
                                    </a>
                                    <Dropdown.Item>
                                        <LoadingContent fetchStatus={this.state.thumbnailStatus}>
                                            <Popup content={stampPosition} on='click' position='left center'
                                                trigger={<Dropdown item icon='none'
                                                    text={t('acte:stamp_pad.download_stamped_acte')}/>}
                                            />
                                        </LoadingContent>
                                    </Dropdown.Item>
                                </Dropdown.Menu>
                            </Dropdown>
                        </div>

                        <FieldInline htmlFor="localAuthority" label={t('acte.fields.localAuthority')}>
                            <FieldValue id="localAuthority">{acte.localAuthority.name}</FieldValue>
                        </FieldInline>
                        <FieldInline htmlFor="ack" label={t('acte:acte.status.ACK_RECEIVED')}>
                            <FieldValue id="ack">{historyAR && moment(historyAR.date).format('DD/MM/YYYY')}</FieldValue>
                        </FieldInline>
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
                        {acte.acteAttachment && isActeAttachmentPDF && (
                            <Grid>
                                <Grid.Column width={4}>
                                    <label style={{verticalAlign: 'middle'}}
                                        htmlFor="acteAttachment">{t('acte.fields.acteAttachment')}</label>
                                </Grid.Column>
                                <Grid.Column width={12}>
                                    <FieldValue id="acteAttachment">
                                        <LinkFile url={`/api/acte/public/${acte.uuid}/file`}
                                            text={acte.acteAttachment.filename}/>
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
                    </Segment>
                </LoadingContent>
            </Page>
        )
    }
}

export default translate(['acte', 'api-gateway'])(ActePublic)
