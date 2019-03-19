import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import { Link } from 'react-router-dom'
import { Button, Menu, Dropdown, Container, Icon, Popup } from 'semantic-ui-react'

import { withAuthContext } from '../Auth'

import { notifications } from '../_util/Notifications'
import { checkStatus, getLocalAuthoritySlug, getMultiPahtFromSlug, rightsFeatureResolver } from '../_util/utils'
import history from '../_util/history'

class TopBar extends Component {
    static contextTypes = {
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func,
        _openMenu: PropTypes.func,
        isMenuOpened: PropTypes.bool
    }
    state = {
        isMainDomain: true,
        selectedProfil: null,
        defaultProfil: null,
        profiles: []
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/api-gateway/isMainDomain' })
            .then(checkStatus)
            .then(response => response.json())
            .then(isMainDomain => this.setState({ isMainDomain }))
    }
    componentDidUpdate() {
        // QuickFix
        // context sometimes doen't load in ComponentDidMount
        if (this.props.authContext.profile && this.props.authContext.profile.uuid && this.props.authContext.profile.uuid !== this.state.defaultProfil) {
            this.setState({ defaultProfil: this.props.authContext.profile.uuid, selectedProfil: this.props.authContext.profile.uuid })
            this.fetchUserInfo()
        }
    }
    fetchUserInfo = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
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
        else if(value !== this.state.defaultProfil) {
            //redirect only if new profile is selected
            window.location.href = '/api/api-gateway/switch/' + value
        }
    }
    render() {
        const { t, _openMenu, isMenuOpened } = this.context
        const { isLoggedIn, user, profile, userRights } = this.props.authContext
        const multiPath = getMultiPahtFromSlug()
        const listProfile = this.state.profiles.map(profile => {
            return {
                text: profile.localAuthorityName,
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
        const urlAdmin = this.props.admin ? `${multiPath}/` : (profile && profile.admin ?`${multiPath}/admin/` : `${multiPath}/admin/ma-collectivite/convocation`)
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
                                    text={profile.localAuthority.name}
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
                                        {profile && (profile.admin || (!profile.admin && rightsFeatureResolver(userRights, ['CONVOCATION_ADMIN'])))&& (
                                            <Menu.Item className="primary" as={Link} to={urlAdmin}>
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

export default translate(['api-gateway'])(withAuthContext(TopBar))
