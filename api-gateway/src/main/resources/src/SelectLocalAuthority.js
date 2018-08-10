import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Grid, Header, Form, Segment, Dropdown, Card } from 'semantic-ui-react'

import { checkStatus } from './_util/utils'
import { notifications } from './_util/Notifications'

class SelectLocalAuthority extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        localAuthorities: [],
        lastUsedLocalAuths: [],
        selected: {}
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/local-authority/all' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                this.setState({ localAuthorities: json })
            })
            .catch(response => {
                response.json().then(json => _addNotification(notifications.defaultError, 'notifications.admin.title', json.message))
            })

        const lastUsedLocalAuths = localStorage.getItem('lastUsedLocalAuths')
        if (lastUsedLocalAuths) {
            this.setState({ lastUsedLocalAuths: JSON.parse(lastUsedLocalAuths) })
        }
    }
    submit = () => {
        const lastUsedLocalAuthsStorage = localStorage.getItem('lastUsedLocalAuths')
        const localAuth = this.state.selected
        localAuth.date = new Date().toLocaleDateString()
        let lastUsedLocalAuths = {}
        if (lastUsedLocalAuthsStorage) {
            lastUsedLocalAuths = JSON.parse(lastUsedLocalAuthsStorage)
            const index = lastUsedLocalAuths.findIndex(lastUsedLocalAuths => lastUsedLocalAuths.uuid === localAuth.uuid)
            if (index > -1) lastUsedLocalAuths.splice(index, 1)
            if (lastUsedLocalAuths.length >= 5) lastUsedLocalAuths.pop()
            lastUsedLocalAuths.unshift(localAuth)
        } else {
            lastUsedLocalAuths = [localAuth]
        }
        localStorage.setItem('lastUsedLocalAuths', JSON.stringify(lastUsedLocalAuths))
        window.location.href = '/api/api-gateway/loginWithSlug/' + this.state.selected.slugName
    }
    onChange = (event, { value }) => {
        const selected = this.state.localAuthorities.find(localAuthority => localAuthority.uuid === value)
        this.setState({ selected })
    }
    render() {
        const { t } = this.context
        const localAuthoritiesOptions = this.state.localAuthorities.map(localAuthority => {
            return { key: localAuthority.uuid, value: localAuthority.uuid, text: localAuthority.name }
        })

        const lastLocalAuthorities = this.state.lastUsedLocalAuths.map(localAuthority =>
            <Card
                key={localAuthority.uuid}
                href={'/api/api-gateway/loginWithSlug/' + localAuthority.slugName}
                header={localAuthority.name}
                meta={localAuthority.siren}
                description={localAuthority.date}
            />
        )
        return (
            <div style={{ marginTop: '5em' }}>
                {lastLocalAuthorities.length > 0 &&
                    <Grid textAlign='center' verticalAlign='middle' >
                        <Header as='h2'>{t('last_localAuthorities')}</Header>
                        <Grid.Row >
                            <Card.Group >
                                {lastLocalAuthorities}
                            </Card.Group>
                        </Grid.Row>
                    </Grid>
                }
                <Grid textAlign='center' verticalAlign='middle' >
                    <Grid.Row >
                        <Grid.Column style={{ maxWidth: 450 }}>
                            <Header as='h2' textAlign='center'>{t('select_localAuthority')}</Header>
                            <Form onSubmit={this.submit} size='large'>
                                <Segment>
                                    <Dropdown style={{ marginBottom: '1em' }} placeholder={`${t('form.search')}...`} fluid search selection
                                        options={localAuthoritiesOptions}
                                        onChange={this.onChange} />
                                    <Button primary basic disabled={!this.state.selected.uuid} fluid size='large'>{t('top_bar.log_in')}</Button>
                                </Segment>
                            </Form>
                        </Grid.Column>
                    </Grid.Row>
                </Grid>
            </div>
        )
    }
}

export default translate('api-gateway')(SelectLocalAuthority)