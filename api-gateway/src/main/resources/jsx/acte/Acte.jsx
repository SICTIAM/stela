import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import renderIf from 'render-if'
import moment from 'moment'
import { Grid, Segment, List, Checkbox } from 'semantic-ui-react'

import { errorNotification } from '../_components/Notifications'
import { Field } from '../_components/UI'
import history from '../_util/history'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import ActeHistory from './ActeHistory'
import ActeCancelButton from './ActeCancelButton'

class Acte extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        acteUI: {
            acte: {
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
    render() {
        const { t } = this.context
        const acteFetched = renderIf(this.state.acteFetched)
        const acteNotFetched = renderIf(!this.state.acteFetched)
        const acte = this.state.acteUI.acte
        const annexes = this.state.acteUI.acte.annexes.map(annexe =>
            <List.Item key={annexe.uuid}>
                <a target='_blank' href={`/api/acte/${acte.uuid}/annexe/${annexe.uuid}`}>{annexe.filename}</a>
            </List.Item>
        )
        return (
            <div>
                {acteFetched(
                    <div>
                        <Segment>
                            <Grid>
                                <Grid.Column width={13}><h1>{acte.objet}</h1></Grid.Column>
                                <Grid.Column width={3}>
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
                            <Field htmlFor="file" label={t('acte.fields.file')}>
                                <span id="file"><a target='_blank' href={`/api/acte/${acte.uuid}/file`}>{acte.filename}</a></span>
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

                            <ActeHistory history={this.state.acteUI.acte.acteHistories} />
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

export default translate(['acte'])(Acte)