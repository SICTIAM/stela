import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Grid } from 'semantic-ui-react'

import ActeHistory from './ActeHistory'

class Acte extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    render() {
        const { t } = this.context
        const acte = {
            uuid: "041334b4-36df-4de0-9796-978306dc093f",
            number: "001",
            decision: 1499288997571,
            nature: "DELIBERATION",
            code: "1-0-0-1-0",
            title: "STELA 3 sera fini en Décembre",
            creation: 1499288997614,
            status: "CREATED",
            lastUpdateTime: 1499288997614,
            public: true
        }
        return (
            <div>
                <h1>{acte.title}</h1>

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
                    <Grid.Column width={13}><span id="nature">{acte.nature}</span></Grid.Column>
                </Grid>

                <Grid>
                    <Grid.Column width={3}><label htmlFor="code">{t('acte.fields.code')}</label></Grid.Column>
                    <Grid.Column width={13}><span id="code">{acte.code}</span></Grid.Column>
                </Grid>

                <ActeHistory />

            </div>
        )
    }
}

export default translate(['api-gateway'])(Acte)