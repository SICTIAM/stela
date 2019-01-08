import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Link } from 'react-router-dom'
import { Button, Menu, Dropdown, Container, Icon, Popup } from 'semantic-ui-react'

import { notifications } from '../_util/Notifications'
import { checkStatus, getLocalAuthoritySlug, getMultiPahtFromSlug } from '../_util/utils'
import history from '../_util/history'

class TopBar extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        user: PropTypes.object,
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func,
        _openMenu: PropTypes.func,
        isMenuOpened: PropTypes.bool
    }
    state = {
        isMainDomain: true,
        isUpdated: false,
        currentProfile: {
            uuid: '',
            admin: false,
            localAuthority: {
                name: ''
            }
        },
        selectedProfil: null,
        profiles: []
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/api-gateway/isMainDomain' })
            .then(checkStatus)
            .then(response => response.json())
            .then(isMainDomain => this.setState({ isMainDomain }))
    }
    fetchUserInfo = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/profile' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                if (json.uuid) this.setState({ currentProfile: json, isUpdated: true, selectedProfil: json.uuid })
            })
            .catch(response => {
                response.json().then(json => _addNotification(notifications.defaultError, 'notifications.admin.title', json.message))
            })
        _fetchWithAuthzHandling({ url: '/api/admin/agent/profiles' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ profiles: json }))
            .catch(response => {
                response.json().then(json => _addNotification(notifications.defaultError, 'notifications.admin.title', json.message))
            })
    }
    login = () => {
        if (!this.state.isMainDomain) {
            const localAuthoritySlug = getLocalAuthoritySlug()
            window.location.href = '/api/api-gateway/loginWithSlug/' + localAuthoritySlug
        }
        else history.push('/choix-collectivite')
    }
    updateSelectedProfil = (event, value) => {
        if(event.keyCode && event.keyCode !== 13) {
            this.setState({selectedProfil: value})
        }
        else {
            window.location.href = '/api/api-gateway/switch/' + value
        }
    }
    render() {
        const { isLoggedIn, t, user, _openMenu, isMenuOpened } = this.context
        const multiPath = getMultiPahtFromSlug()
        const listProfile = this.state.profiles.map(profile => {
            return {
                text: profile.localAuthority.name,
                value: profile.uuid,
                key: profile.uuid,
                icon: 'building'
            }
        }
        )
        const trigger = (
            <Button basic className={this.props.admin ? 'secondary' : 'primary'}>
                <Icon name="user circle outline" size="large" />{' '}
                {`${user && user.given_name} ${user && user.family_name}`}
            </Button>
        )
        // FIXME : isLoggedIn in the context is not reliable (false then true)
        if (isLoggedIn && !this.state.isUpdated) this.fetchUserInfo()
        return (
            <Menu className={`topBar ${this.props.admin ? 'secondary' : 'primary'}`} fixed="top" secondary onClick={() => {isMenuOpened && _openMenu()}}>
                <a href="#content" className="skip">{t('api-gateway:skip_to_content')}</a>
                <Icon name="bars" onClick={_openMenu} className='buger-menu primary'></Icon>
                <Menu.Item className="appTitle" as={Link} to={`${multiPath}/`} header>
                    <h1 style={{ textAlign: 'center' }}>
                        <img src={process.env.PUBLIC_URL + '/img/logo_stela.png'} alt="STELA" />
                    </h1>
                </Menu.Item>
                <Container>
                    <Menu.Menu position="right">
                        {(isLoggedIn && listProfile.length > 1) && (
                            <Menu.Item className='liste-profil'>
                                <Dropdown
                                    style={{minWidth: '18em'}}
                                    search selection
                                    options={listProfile}
                                    value={this.state.selectedProfil}
                                    selectOnBlur={true}
                                    text={this.state.currentProfile.localAuthority.name}
                                    onChange={(event, { value }) => this.updateSelectedProfil(event, value)}
                                />
                            </Menu.Item>
                        )}
                        {isLoggedIn && (
                            <Menu.Item>
                                <Popup style={{ padding: 0 }} trigger={trigger} on="click" position="bottom center">
                                    <Menu vertical>
                                        <Menu.Item className="primary" as={Link} to={`${multiPath}/profil`}>
                                            <span><Icon name="user" /> {t('top_bar.profile')}</span>
                                        </Menu.Item>
                                        {this.state.currentProfile.admin && (
                                            <Menu.Item className="primary" as={Link} to={this.props.admin ? `${multiPath}/` : `${multiPath}/admin`}>
                                                <span>
                                                    <Icon name={this.props.admin ? 'reply' : 'settings'} />{' '}
                                                    {t(`top_bar.${this.props.admin ? 'back_to_app' : 'admin'}`)}
                                                </span>
                                            </Menu.Item>
                                        )}
                                        <Menu.Item className="primary" onClick={() => (window.location.href = '/logout')}>
                                            <span><Icon name="sign out" /> {t('top_bar.log_out')}</span>
                                        </Menu.Item>
                                    </Menu>
                                </Popup>
                            </Menu.Item>
                        )}
                        {!isLoggedIn && (
                            <Menu.Item>
                                <Button basic className="primary" onClick={this.login}>
                                    {t('top_bar.log_in')}
                                </Button>
                            </Menu.Item>
                        )}
                    </Menu.Menu>
                </Container>
            </Menu>
        )
    }
}

export default translate('api-gateway')(TopBar)
