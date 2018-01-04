import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Button, Menu, Dropdown, Container } from 'semantic-ui-react'
import { notifications } from '../_util/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'

class TopBar extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        t: PropTypes.func
    }
    state = {
        isUpdated: false,
        current: {
            agent: {
                family_name: '',
                given_name: '',
                email: '',
            },
            uuid: '',
            localAuthority: {
                name: ''
            }
        },
        profiles: []
    }
    refreshUser() {
        fetchWithAuthzHandling( { url: '/api/admin/profile' } )
            .then( checkStatus )
            .then( response => response.json() )
            .then( json => {
                this.setState( { current: json, isUpdated: true } )
            } )
            .catch( response => {
                response.json().then( json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                } )
            } )
        fetchWithAuthzHandling( { url: '/api/admin/agent' } )
            .then( checkStatus )
            .then( response => response.json() )
            .then( json => {
                this.setState( { profiles: json.profiles } )
            } )
            .catch( response => {
                response.json().then( json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                } )
            } )
    }
    render() {
        const { isLoggedIn, t } = this.context

        const listProfile = this.state.profiles.map(( item, index ) => {
            if ( item.uuid !== this.state.current.uuid )
                return <Dropdown.Item onClick={() => window.location.href = '/api/api-gateway/switch/' + item.uuid} value={item.uuid}>{item.localAuthority.name}</Dropdown.Item>
        })

        if ( isLoggedIn && !this.state.isUpdated ) {
            this.refreshUser()
        }
        return (
            <Menu className='topBar' fixed='top' secondary>
                <Container>
                    <Menu.Menu position='right'>

                        {isLoggedIn &&
                            <Dropdown item text={`${this.state.current.agent.given_name} ${this.state.current.agent.family_name}`}>
                                <Dropdown.Menu>
                                    <Dropdown.Item>{t('top_bar.params')}</Dropdown.Item>
                                    <Dropdown.Item onClick={() => window.location.href = '/logout'}>{t('top_bar.log_out')}</Dropdown.Item>
                                </Dropdown.Menu>
                            </Dropdown>
                        }
                        {isLoggedIn &&
                            <Dropdown item text={`${this.state.current.localAuthority.name}`}>
                                <Dropdown.Menu>
                                    {listProfile}
                                </Dropdown.Menu>
                            </Dropdown>
                        }
                        {!isLoggedIn &&
                            <Menu.Item>
                                <Button primary onClick={() => window.location.href = '/login'}>{t('top_bar.log_in')}</Button>
                            </Menu.Item>
                        }

                    </Menu.Menu>
                </Container>
            </Menu>
        )
    }
}

export default translate('api-gateway')(TopBar)