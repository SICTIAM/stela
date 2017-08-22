import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import { Grid, Segment } from 'semantic-ui-react'

import { errorNotification } from '../_components/Notifications'
import ActeHistory from './ActeHistory'
import ActeCancelButton from './ActeCancelButton'
import history from '../_util/history'

class Acte extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        acteUI: {
            acte: {},
            history: {},
            cancellable: false
        },
        acteFetched: false
    }
    checkStatus = (response) => {
        if (response.status >= 200 && response.status < 300) {
            return response
        } else {
            throw response
        }
    }
    componentDidMount() {
        const uuid = this.props.uuid
        if (uuid !== '') {
            fetch('/api/acte/' + uuid, { credentials: 'same-origin' })
                .then(this.checkStatus)
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
    render() {
        const { t } = this.context
        const acteFetched = renderIf(this.state.acteFetched)
        const acteNotFetched = renderIf(!this.state.acteFetched)
        const acte = this.state.acteUI.acte
        return (
            <div>
                {acteFetched(
                    <div>
                        <Segment>
                            <Grid>
                                <Grid.Column width={13}><h1>{acte.title}</h1></Grid.Column>
                                <Grid.Column width={3}>
                                    <ActeCancelButton isCancellable={this.state.acteUI.cancellable} uuid={this.state.acteUI.acte.uuid} />
                                </Grid.Column>
                            </Grid>

                            <Grid>
                                <Grid.Column width={3}><label htmlFor="number">{t('acte.fields.number')}</label></Grid.Column>
                                <Grid.Column width={13}><span id="number">{acte.number}</span></Grid.Column>
                            </Grid>

                            <Grid>
                                <Grid.Column width={3}><label htmlFor="decision">{t('acte.fields.decision')}</label></Grid.Column>
                                <Grid.Column width={13}><span id="decision">{acte.decision}</span></Grid.Column>
                            </Grid>
                            <Grid>
                                <Grid.Column width={3}><label htmlFor="nature">{t('acte.fields.nature')}</label></Grid.Column>
                                <Grid.Column width={13}><span id="nature">{t(`acte.nature.${acte.nature}`)}</span></Grid.Column>
                            </Grid>

                            <Grid>
                                <Grid.Column width={3}><label htmlFor="code">{t('acte.fields.code')}</label></Grid.Column>
                                <Grid.Column width={13}><span id="code">{acte.code}</span></Grid.Column>
                            </Grid>

                            <Grid>
                                <Grid.Column width={3}><label htmlFor="file">{t('acte.fields.file')}</label></Grid.Column>
                                <Grid.Column width={13}><span id="file">{acte.filename}</span></Grid.Column>
                            </Grid>

                            <ActeHistory history={this.state.acteUI.history} />
                        </Segment>
                    </div>
                )}
                {acteNotFetched(
                    <p>{t('acte.page.non_existent_act')}</p>
                )}
            </div>
        )
    }
}

export default translate(['api-gateway'])(Acte)