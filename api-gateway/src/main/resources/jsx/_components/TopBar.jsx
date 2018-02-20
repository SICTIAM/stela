import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Link } from 'react-router-dom'
import { Button, Menu, Dropdown, Container, Icon, Popup } from 'semantic-ui-react'

import { notifications } from '../_util/Notifications'
import { checkStatus, fetchWithAuthzHandling } from '../_util/utils'
import history from '../_util/history'

class TopBar extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        user: PropTypes.object,
        t: PropTypes.func
    }
    state = {
        isMainDomain: true,
        isUpdated: false,
        currentProfile: {
            uuid: '',
            localAuthority: {
                name: ''
            }
        },
        profiles: []
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/api-gateway/isMainDomain' })
            .then(checkStatus)
            .then(response => response.json())
            .then(isMainDomain => this.setState({ isMainDomain }))
    }
    fetchUserInfo = () => {
        fetchWithAuthzHandling({ url: '/api/admin/profile' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                if (json.uuid) this.setState({ currentProfile: json, isUpdated: true })
            })
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
        fetchWithAuthzHandling({ url: '/api/admin/agent/profiles' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ profiles: json }))
            .catch(response => {
                response.json().then(json => {
                    this.context._addNotification(notifications.defaultError, 'notifications.admin.title', json.message)
                })
            })
    }
    login = () => {
        if (!this.state.isMainDomain) window.location.href = '/login'
        else history.push('/choix-collectivite')
    }
    render() {
        const { isLoggedIn, t, user } = this.context
        const listProfile = this.state.profiles.map(profile => profile.uuid !== this.state.currentProfile.uuid &&
            <Dropdown.Item key={profile.uuid} onClick={() => window.location.href = '/api/api-gateway/switch/' + profile.uuid} value={profile.uuid}>
                <Icon name='building' size='large' /> {profile.localAuthority.name}
            </Dropdown.Item>
        )
        const trigger = <Button basic className={this.props.admin ? 'rosso' : 'anatra'}><Icon name='user circle outline' size='large' /> {`${user && user.given_name} ${user && user.family_name}`}</Button>
        const triggerLA = <Button basic color='grey'><Icon name='building' size='large' /> {`${this.state.currentProfile.localAuthority.name}`} <Icon style={{ marginLeft: '0.5em', marginRight: 0 }} name='caret down' /></Button>
        // FIXME : isLoggedIn in the context is not reliable (false then true)
        if (isLoggedIn && !this.state.isUpdated) this.fetchUserInfo()
        return (
            <Menu className={`topBar ${this.props.admin ? 'rosso' : 'anatra'}`} fixed='top' secondary>

                <Menu.Item className='appTitle' as={Link} to="/" header>
                    <h1 style={{ textAlign: 'center' }}><img src={process.env.PUBLIC_URL + '/img/logo_stela.png'} alt="STELA" /></h1>
                </Menu.Item>

                <Container>
                    <Menu.Menu position='right'>

                        {(isLoggedIn && listProfile.length > 1) &&
                            <Menu.Item>
                                <Dropdown basic trigger={triggerLA} icon={false}>
                                    <Dropdown.Menu>
                                        {listProfile}
                                    </Dropdown.Menu>
                                </Dropdown>
                            </Menu.Item>
                        }
                        {isLoggedIn &&
                            <Menu.Item>
                                <Popup style={{ padding: 0 }} trigger={trigger} on='click' position='bottom center'>
                                    <Menu vertical>
                                        <Menu.Item as={Link} to='/profil'><span><Icon name='user' /> {t('top_bar.profile')}</span></Menu.Item>
                                        <Menu.Item as={Link} to='/admin'><span><Icon name='settings' /> {t('top_bar.admin')}</span></Menu.Item>
                                        <Menu.Item onClick={() => window.location.href = '/logout'}><span><Icon name='sign out' /> {t('top_bar.log_out')}</span></Menu.Item>
                                    </Menu>
                                </Popup>
                            </Menu.Item>
                        }
                        {!isLoggedIn &&
                            <Menu.Item>
                                <Button basic className='anatra' onClick={this.login}>{t('top_bar.log_in')}</Button>
                            </Menu.Item>
                        }

                    </Menu.Menu>
                </Container>
            </Menu >
        )
    }
}

export default translate('api-gateway')(TopBar)