import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Grid, Segment, Header } from 'semantic-ui-react'

import { File, InputFile } from '../_components/UI'
import { notifications } from '../_util/Notifications'
import { checkStatus } from '../_util/utils'
import { withAuthContext } from '../Auth'

class LetteObservation extends Component {
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
        file: null,
        asked: false
    }
    handleFileChange = (file) => {
        if (file) this.setState({ file })
    }
    deleteFile = () => {
        this.setState({ file: null })
    }
    sendResponse = (reponseOrRejet) => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const data = new FormData()
        const url = `/api/acte/${this.props.acteUuid}/lettre-observation/${reponseOrRejet}`
        data.append('file', this.state.file)
        _fetchWithAuthzHandling({ url, method: 'POST', body: data, context: this.props.authContext })
            .then(checkStatus)
            .then(() => {
                _addNotification(notifications.acte.lettreObservationAsked)
                this.setState({ asked: true })
            })
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    render() {
        const { t } = this.context
        const { acteHistories } = this.props
        const lettreObservationHistory = acteHistories.find(acteHistory => acteHistory.status === 'LETTRE_OBSERVATION_RECEIVED')
        const reponseAskedHistory = acteHistories.find(acteHistory =>
            acteHistory.status === 'REPONSE_LETTRE_OBSEVATION_ASKED' || acteHistory.status === 'REJET_LETTRE_OBSERVATION_ASKED')
        return (
            <Segment color='orange'>
                <Grid>
                    <Grid.Column width={reponseAskedHistory ? 8 : 7}>
                        <Header size='small'>{t('acte.page.lettre_observation.received')}</Header>
                        <File attachment={{ filename: lettreObservationHistory.fileName }}
                            src={`/api/acte/${this.props.acteUuid}/history/${lettreObservationHistory.uuid}/file`} />
                    </Grid.Column>
                    <Grid.Column width={reponseAskedHistory ? 8 : 6}>
                        {(!reponseAskedHistory && !this.state.asked) && (
                            <div>
                                <Header size='small'>{t('acte.page.lettre_observation.to_send')}</Header>
                                {!this.state.file && (
                                    <InputFile htmlFor='lettreObservationFile' label={t('api-gateway:form.add_a_file')}>
                                        <input id='lettreObservationFile'
                                            type='file'
                                            style={{ display: 'none' }}
                                            onChange={e => this.handleFileChange(e.target.files[0])} />
                                    </InputFile>
                                )}
                                {this.state.file && (
                                    <File attachment={{ filename: this.state.file.name }} onDelete={this.deleteFile} />
                                )}
                            </div>
                        )}
                        {reponseAskedHistory && (
                            <div>
                                <Header size='small'>
                                    {t(`acte.page.lettre_observation.sent_${reponseAskedHistory.status === 'REJET_LETTRE_OBSERVATION_ASKED'
                                        ? 'rejet' : 'reponse'}`)}
                                </Header>
                                <File attachment={{ filename: reponseAskedHistory.fileName }}
                                    src={`/api/acte/${this.props.acteUuid}/history/${lettreObservationHistory.uuid}/file`} />
                            </div>
                        )}
                        {this.state.asked && (
                            <div>
                                <Header size='small'>{t('acte.page.lettre_observation.asked')}</Header>
                                <File attachment={{ filename: this.state.file.name }} />
                            </div>
                        )}
                    </Grid.Column>
                    {(!reponseAskedHistory && !this.state.asked) && (
                        <Grid.Column width={3}
                            style={{ display: 'flex', alignItems: 'center', flexDirection: 'column', justifyContent: 'space-around' }}>
                            <Button fluid disabled={!this.state.file} onClick={() => this.sendResponse('reponse')} basic primary>
                                {t('acte.page.lettre_observation.submit_reponse')}
                            </Button>
                            <Button fluid disabled={!this.state.file} onClick={() => this.sendResponse('rejet')} basic negative>
                                {t('acte.page.lettre_observation.submit_rejet')}
                            </Button>
                        </Grid.Column>
                    )}
                </Grid>
            </Segment>
        )
    }
}

export default translate(['acte', 'api-gateway'])(withAuthContext(LetteObservation))