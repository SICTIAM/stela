import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Grid, Header, Form, Segment, Dropdown } from 'semantic-ui-react'

import { checkStatus, fetchWithAuthzHandling } from './_util/utils'
import { notifications } from './_util/Notifications'

class SelectLocalAuthority extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        t: PropTypes.func
    }
    state = {
        localAuthorities: [],
        selected: {}
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/admin/local-authority/all' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                this.setState({ localAuthorities: json })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    submit = () => window.location.href = '/api/api-gateway/loginWithSlug/' + this.state.selected.slugName
    onChange = (event, { value }) => {
        const selected = this.state.localAuthorities.find(localAuthority => localAuthority.uuid === value)
        this.setState({ selected })
    }
    render() {
        const { t } = this.context
        const localAuthoritiesOptions = this.state.localAuthorities.map(localAuthority => {
            return { key: localAuthority.uuid, value: localAuthority.uuid, text: localAuthority.name }
        })
        return (
            <div style={{ marginTop: '5em' }}>
                <Grid textAlign='center' verticalAlign='middle' >
                    <Grid.Column style={{ maxWidth: 450 }}>
                        <Header as='h2' textAlign='center'>{t('select_localAuthority')}</Header>
                        <Form onSubmit={this.submit} size='large'>
                            <Segment>
                                <Dropdown style={{ marginBottom: '1em' }} placeholder={`${t('form.search')}...`} fluid search selection
                                    options={localAuthoritiesOptions}
                                    onChange={this.onChange} />
                                <Button disabled={!this.state.selected.uuid} primary fluid size='large'>{t('top_bar.log_in')}</Button>
                            </Segment>
                        </Form>
                    </Grid.Column>
                </Grid>
            </div>
        )
    }
}

export default translate('api-gateway')(SelectLocalAuthority)