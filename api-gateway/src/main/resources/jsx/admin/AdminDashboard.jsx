import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Segment, Label, Statistic, Grid } from 'semantic-ui-react'

class AdminDashboard extends Component {
    static propTypes = {
        t: PropTypes.func.isRequired
    }
    render() {
        const { t } = this.props

        return (
            <div>
                <h1>Tableau de bord</h1>
                <Segment raised>
                    <Label as='a' color='blue' ribbon>Ces dernière 24h</Label>
                    <Statistic.Group widths='four'>
                        <Statistic>
                            <Statistic.Value>159</Statistic.Value>
                            <Statistic.Label>actes envoyés</Statistic.Label>
                        </Statistic>

                        <Statistic>
                            <Statistic.Value>427</Statistic.Value>
                            <Statistic.Label>pes envoyés</Statistic.Label>
                        </Statistic>

                        <Statistic color='red'>
                            <Statistic.Value>15</Statistic.Value>
                            <Statistic.Label>actes bloqués en reception</Statistic.Label>
                        </Statistic>

                        <Statistic>
                            <Statistic.Value>3</Statistic.Value>
                            <Statistic.Label>convocations envoyées</Statistic.Label>
                        </Statistic>
                    </Statistic.Group>
                </Segment>

                <Segment raised>
                    <Grid columns='six'>
                        <Grid.Row>
                            <Grid.Column textAlign='center'><Label color='green'>Liaison Ministère</Label></Grid.Column>
                            <Grid.Column textAlign='center'><Label color='green'>Liaison Préfécture</Label></Grid.Column>
                            <Grid.Column textAlign='center'><Label color='red'>Liaison Archivage</Label></Grid.Column>
                            <Grid.Column textAlign='center'><Label color='green'>Liaison Datacore</Label></Grid.Column>
                            <Grid.Column textAlign='center'><Label color='orange'>Liaison serveurs mail</Label></Grid.Column>
                            <Grid.Column textAlign='center'><Label color='green'>Liaison Sesile</Label></Grid.Column>
                        </Grid.Row>
                    </Grid>
                </Segment>

                <h2>Les dernières collectivités créées</h2>
                <p>[WIP]</p>

                <h2>Les dernières utilisateurs créés</h2>
                <p>[WIP]</p>
            </div>
        )
    }
}

export default translate(['api-gateway'])(AdminDashboard)