import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Grid, Segment, Header, Card } from 'semantic-ui-react'

import { File, InputFile } from '../_components/UI'
import { notifications } from '../_util/Notifications'
import { checkStatus } from '../_util/utils'

class DemandePiecesComplementaires extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    static defaultProps = {
        acteUuid: '',
        acteHistories: []
    }
    state = {
        files: [],
        asked: false
    }
    handleFileChange = (file) => {
        if (file) {
            const { files } = this.state
            files.push(file)
            this.setState({ files })
        }
    }
    deleteFile = ({ filename }) => {
        if (filename) {
            const { files } = this.state
            const newFiles = files.filter(file => file.name !== filename)
            this.setState({ files: newFiles })
        }
    }
    sendResponse = (reponseOrRejet) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const data = new FormData()
        const url = `/api/acte/${this.props.acteUuid}/pieces-complementaires/${reponseOrRejet}`
        this.state.files.forEach(file => data.append('files', file))
        _fetchWithAuthzHandling({ url, method: 'POST', body: data, context: this.context })
            .then(checkStatus)
            .then(() => {
                _addNotification(notifications.acte.piecesComplementairesAsked)
                this.setState({ asked: true })
            })
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    render() {
        const { t } = this.context
        const { acteHistories } = this.props
        const demandePiecesComplementairesHistory = acteHistories.find(acteHistory => acteHistory.status === 'DEMANDE_PIECE_COMPLEMENTAIRE_RECEIVED')
        const isReponseSent = acteHistories.find(acteHistory =>
            acteHistory.flux === 'TRANSMISSION_PIECES_COMPLEMENTAIRES' || acteHistory.flux === 'REFUS_EXPLICITE_TRANSMISSION_PIECES_COMPLEMENTAIRES')
        const archiveCreatedHistory = acteHistories.find(acteHistory =>
            (acteHistory.flux === 'TRANSMISSION_PIECES_COMPLEMENTAIRES' || acteHistory.flux === 'REFUS_PIECES_COMPLEMENTAIRE_ASKED')
            && acteHistory.status === 'ARCHIVE_CREATED')
        const files = this.state.files.map(file =>
            <File key={file.name} attachment={{ filename: file.name }} onDelete={this.deleteFile} />
        )
        return (
            <Segment color='orange'>
                <Grid>
                    <Grid.Column width={isReponseSent ? 8 : 13}>
                        <Header size='small'>{t('acte.page.lettre_observation.received')}</Header>
                        <File attachment={{ filename: demandePiecesComplementairesHistory.fileName }}
                            src={`/api/acte/${this.props.acteUuid}/history/${demandePiecesComplementairesHistory.uuid}/file`} />
                        {(!isReponseSent && !this.state.asked) &&
                            <div>
                                <Header size='small'>{t('acte.page.lettre_observation.to_send')}</Header>
                                <InputFile htmlFor='demandePiecesComplementairesFile' label={t('api-gateway:form.add_a_file')}>
                                    <input id='demandePiecesComplementairesFile'
                                        type='file'
                                        style={{ display: 'none' }}
                                        onChange={e => this.handleFileChange(e.target.files[0])} />
                                </InputFile>
                                <Card.Group style={{ marginTop: '0.5em' }}>
                                    {files.length > 0 && files}
                                </Card.Group>
                            </div>
                        }
                        {this.state.asked &&
                            <div>
                                <Header size='small'>{t('acte.page.pieces_complementaires.asked')}</Header>
                                <Card.Group style={{ marginTop: '0.5em' }}>
                                    {files.length > 0 && files}
                                </Card.Group>
                            </div>
                        }
                    </Grid.Column>
                    {isReponseSent &&
                        <Grid.Column width={8}>
                            <Header size='small'>
                                {t(`acte.page.pieces_complementaires.sent_${isReponseSent.status === 'REFUS_PIECES_COMPLEMENTAIRE_ASKED'
                                    ? 'rejet' : 'reponse'}`)}
                            </Header>
                            {archiveCreatedHistory &&
                                <File attachment={{ filename: archiveCreatedHistory.fileName }}
                                    src={`/api/acte/${this.props.acteUuid}/history/${archiveCreatedHistory.uuid}/file`} />
                            }
                        </Grid.Column>
                    }
                    {(!isReponseSent && !this.state.asked) &&
                        <Grid.Column width={3} style={{ display: 'flex', alignItems: 'center', flexDirection: 'column', justifyContent: 'center' }}>
                            <Button style={{ marginBottom: '1em' }} fluid disabled={this.state.files.length === 0}
                                onClick={() => this.sendResponse('reponse')} basic primary>
                                {t('acte.page.pieces_complementaires.submit_reponse')}
                            </Button>
                            <Button fluid disabled={this.state.files.length === 0} onClick={() => this.sendResponse('rejet')} basic negative>
                                {t('acte.page.pieces_complementaires.submit_rejet')}
                            </Button>
                        </Grid.Column>
                    }
                </Grid>
            </Segment>
        )
    }
}

export default translate(['acte', 'api-gateway'])(DemandePiecesComplementaires)