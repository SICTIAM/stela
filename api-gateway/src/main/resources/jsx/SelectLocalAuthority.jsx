import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Grid, Header, Form, Segment, Dropdown, Card } from 'semantic-ui-react'

import { checkStatus, fetchWithAuthzHandling } from './_util/utils'
import { notifications } from './_util/Notifications'

class SelectLocalAuthority extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        t: PropTypes.func
    }
    state = {
        localAuthorities: [],
        lastUsedLocalAuths: [],
        selected: {}
    }
    componentDidMount() {
        fetchWithAuthzHandling( { url: '/api/admin/local-authority/all' } )
            .then( checkStatus )
            .then( response => response.json() )
            .then( json => {
                this.setState( { localAuthorities: json } )
            } )
            .catch( response => {
                response.json().then( json => {
                    this.context._addNotification( notifications.defaultError, 'notifications.admin.title', json.message )
                } )
            } )

        var lastUsedLocalAuths = localStorage.getItem( 'lastUsedLocalAuths' );
        if ( lastUsedLocalAuths ) {
            this.setState( { lastUsedLocalAuths: JSON.parse( lastUsedLocalAuths ) } )
        }
    }
    submit = () => {

        var lastUsedLocalAuths = localStorage.getItem( 'lastUsedLocalAuths' )

        var localAuth = this.state.selected;
        localAuth.date = new Date().toLocaleDateString()
        if ( lastUsedLocalAuths ) {
            lastUsedLocalAuths = JSON.parse( lastUsedLocalAuths )
            var index = -1;
            for ( var i = 0; i < lastUsedLocalAuths.length; i++ ) {
                if ( lastUsedLocalAuths[i].uuid == localAuth.uuid ) {
                    index = i;
                    break;
                }
            }
            if ( index > -1 ) {
                lastUsedLocalAuths.splice( index, 1 )
            } else {
                if ( lastUsedLocalAuths.length >= 5 ) {
                    lastUsedLocalAuths.pop()
                }
            }
            lastUsedLocalAuths.unshift( localAuth );
        } else {
            lastUsedLocalAuths = [localAuth]
        }

        localStorage.setItem( 'lastUsedLocalAuths', JSON.stringify( lastUsedLocalAuths ) )
        window.location.href = '/api/api-gateway/loginWithSlug/' + this.state.selected.slugName

    }
    onChange = ( event, { value } ) => {
        const selected = this.state.localAuthorities.find( localAuthority => localAuthority.uuid === value )
        this.setState( { selected } )
    }
    render() {
        const { t } = this.context
        const localAuthoritiesOptions = this.state.localAuthorities.map( localAuthority => {
            return { key: localAuthority.uuid, value: localAuthority.uuid, text: localAuthority.name }
        } )

        const lastLocalAuthorities = this.state.lastUsedLocalAuths.map( localAuthority =>
            <Card
                href={'/api/api-gateway/loginWithSlug/' + localAuthority.slugName}
                header={localAuthority.name}
                meta={localAuthority.siren}
                description={localAuthority.date}
            />
        )
        return (
            <div style={{ marginTop: '5em' }}>
                <Header as='h2' textAlign='center'>{t( 'last_localAuthorities' )}</Header>
                <Grid textAlign='center' verticalAlign='middle' >
                    <Grid.Row >
                        <Card.Group >
                            {lastLocalAuthorities}
                        </Card.Group>
                    </Grid.Row>
                    <Grid.Row >
                        <Grid.Column style={{ maxWidth: 450 }}>
                            <Header as='h2' textAlign='center'>{t( 'select_localAuthority' )}</Header>
                            <Form onSubmit={this.submit} size='large'>
                                <Segment>
                                    <Dropdown style={{ marginBottom: '1em' }} placeholder={`${t( 'form.search' )}...`} fluid search selection
                                        options={localAuthoritiesOptions}
                                        onChange={this.onChange} />
                                    <Button disabled={!this.state.selected.uuid} primary fluid size='large'>{t( 'top_bar.log_in' )}</Button>
                                </Segment>
                            </Form>
                        </Grid.Column>
                    </Grid.Row>
                </Grid>
            </div>
        )
    }
}

export default translate( 'api-gateway' )( SelectLocalAuthority)