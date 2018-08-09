import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Grid, Segment, Header } from 'semantic-ui-react'

import { File, InputFile } from '../_components/UI'
import { notifications } from '../_util/Notifications'
import { checkStatus } from '../_util/utils'

class CourrierSimple extends Component {
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
    sendResponse = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        const data = new FormData()
        data.append('file', this.state.file)
        _fetchWithAuthzHandling({ url: `/api/acte/${this.props.acteUuid}/courrier-simple`, method: 'POST', body: data, context: this.context })
            .then(checkStatus)
            .then(() => {
                _addNotification(notifications.acte.courrierSimpleAsked)
                this.setState({ asked: true })
            })
            .catch(response => {
                response.text().then(text => _addNotification(notifications.defaultError, 'notifications.pes.title', text))
            })
    }
    render() {
        const { t } = this.context
        const { acteHistories } = this.props
        const courrierSimpleHistory = acteHistories.find(acteHistory =>
            acteHistory.flux === 'COURRIER_SIMPLE' && acteHistory.status === 'COURRIER_SIMPLE_RECEIVED')
        const isReponseSent = acteHistories.some(acteHistory => acteHistory.flux === 'REPONSE_COURRIER_SIMPLE')
        const reponseAskedHistory = acteHistories.find(acteHistory =>
            acteHistory.flux === 'REPONSE_COURRIER_SIMPLE' && acteHistory.status === 'REPONSE_COURRIER_SIMPLE_ASKED')
        return (
            <Segment color='orange'>
                <Grid>
                    <Grid.Column width={isReponseSent ? 8 : 7}>
                        <Header size='small'>{t('acte.page.courrier_simple.received')}</Header>
                        <File attachment={{ filename: courrierSimpleHistory.fileName }}
                            src={`/api/acte/${this.props.acteUuid}/history/${courrierSimpleHistory.uuid}/file`} />
                    </Grid.Column>
                    <Grid.Column width={isReponseSent ? 8 : 7}>
                        {(!isReponseSent && !this.state.asked) &&
                            <div>
                                <Header size='small'>{t('acte.page.courrier_simple.to_send')}</Header>
                                {!this.state.file &&
                                    <InputFile htmlFor='courrierSimpleFile' label={t('api-gateway:form.add_a_file')}>
                                        <input id='courrierSimpleFile'
                                            type='file'
                                            style={{ display: 'none' }}
                                            onChange={e => this.handleFileChange(e.target.files[0])} />
                                    </InputFile>
                                }
                                {this.state.file &&
                                    <File attachment={{ filename: this.state.file.name }} onDelete={this.deleteFile} />
                                }
                            </div>
                        }
                        {isReponseSent &&
                            <div>
                                <Header size='small'>{t('acte.page.courrier_simple.sent')}</Header>
                                <File attachment={{ filename: reponseAskedHistory.fileName }}
                                    src={`/api/acte/${this.props.acteUuid}/history/${reponseAskedHistory.uuid}/file`} />
                            </div>
                        }
                        {this.state.asked &&
                            <div>
                                <Header size='small'>{t('acte.page.courrier_simple.asked')}</Header>
                                <File attachment={{ filename: this.state.file.name }} />
                            </div>
                        }
                    </Grid.Column>
                    {(!isReponseSent && !this.state.asked) &&
                        <Grid.Column width={2} style={{ display: 'flex', alignItems: 'center' }}>
                            <Button disabled={!this.state.file} onClick={this.sendResponse} basic primary>{t('api-gateway:form.submit')}</Button>
                        </Grid.Column>
                    }
                </Grid>
            </Segment>
        )
    }
}

export default translate(['acte', 'api-gateway'])(CourrierSimple)