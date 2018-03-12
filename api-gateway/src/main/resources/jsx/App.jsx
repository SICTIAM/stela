import React, { Component } from 'react'
import { render } from 'react-dom';
import { I18nextProvider } from 'react-i18next'
import { Router, Route, Switch, Redirect } from 'react-router-dom'
import PropTypes from 'prop-types'
import { Container } from 'semantic-ui-react'
import NotificationSystem from 'react-notification-system'
import { translate } from 'react-i18next'

import 'semantic-ui-css/semantic.min.css';
import '../styles/index.css';

import { fetchWithAuthzHandling, getRightsFromGroups, rightsResolver } from './_util/utils'
import history from './_util/history'
import i18n from './_util/i18n'
import ErrorPage from './_components/ErrorPage'
import MenuBar from './_components/MenuBar'
import TopBar from './_components/TopBar'
import Home from './Home'
import AlertMessage from './_components/AlertMessage'
import { UserProfile, AdminProfile } from './Profile'
import SelectLocalAuthority from './SelectLocalAuthority'
import Acte from './acte/Acte'
import ActeList from './acte/ActeList'
import DraftList from './acte/DraftList'
import NewActeSwitch from './acte/NewActeSwitch'
import PesRetourList from './pes/PesRetourList'
import PesList from './pes/PesList'
import NewPes from './pes/NewPes'
import Pes from './pes/Pes'
import AdminMenuBar from './admin/AdminMenuBar'
import AdminInstance from './admin/AdminInstance'
import AgentList from './admin/AgentList'
import LocalAuthorityList from './admin/localAuthority/LocalAuthorityList'
import LocalAuthority from './admin/localAuthority/LocalAuthority'
import Group from './admin/localAuthority/Group'
import ActeLocalAuthorityParams from './admin/acte/ActeLocalAuthorityParams'
import PesLocalAuthorityParams from './admin/pes/PesLocalAuthorityParams'
import AgentProfile from './admin/localAuthority/AgentProfile'
import ActeModuleParams from './admin/acte/ActeModuleParams'
import PesModuleParams from './admin/pes/PesModuleParams'


import Validator from 'validatorjs'
Validator.useLang(window.localStorage.i18nextLng);

class App extends Component {
    constructor() {
        super()
        this._notificationSystem = null
    }
    static contextTypes = {
        t: PropTypes.func
    }
    static propTypes = {
        children: PropTypes.element.isRequired
    }
    static childContextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        isLoggedIn: PropTypes.bool,
        user: PropTypes.object,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        csrfToken: '',
        csrfTokenHeaderName: '',
        isLoggedIn: false,
        user: {}
    }
    NotificationStyle = {
        Containers: {
            DefaultStyle: {
                width: 400
            },
        }
    }
    getChildContext() {
        return {
            csrfToken: this.state.csrfToken,
            csrfTokenHeaderName: this.state.csrfTokenHeaderName,
            isLoggedIn: this.state.isLoggedIn,
            user: this.state.user,
            t: this.t,
            _addNotification: this._addNotification
        }
    }
    _addNotification = (notification, title, message) => {
        const { t } = this.context
        notification.title = t(title ? title : notification.title)
        notification.message = t(message ? message : notification.message)
        if (this._notificationSystem) {
            this._notificationSystem.addNotification(notification)
        }
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/csrf-token' })
            .then(response => {
                // TODO: Improve (coockies..) 
                this.setState({ isLoggedIn: response.status !== 401 }, this.fetchUser)
                return response
            })
            .then(response => response.headers)
            .then(headers =>
                this.setState({ csrfToken: headers.get('X-CSRF-TOKEN'), csrfTokenHeaderName: headers.get('X-CSRF-HEADER') }))
    }
    fetchUser = () => {
        if (this.state.isLoggedIn)
            fetchWithAuthzHandling({ url: '/api/admin/agent' })
                .then(response => response.json())
                .then(json => this.setState({ user: json }))
    }
    render() {
        return (
            <div>
                <NotificationSystem ref={n => this._notificationSystem = n} style={this.NotificationStyle} />
                {this.props.children}
            </div>
        )
    }
}

const PublicRoute = ({ component: Component, ...rest }) => (
    <Route {...rest} render={(props) =>
        <div>
            <TopBar />
            <div className='wrapperContainer'>
                <MenuBar />
                <Container className='mainContainer'>
                    <Component {...props} />
                </Container>
            </div>
        </div>
    } />
)
const AuthRoute = ({ component: Component, menu: Menu, admin, userRights, allowedRights, ...rest }, { isLoggedIn }) => (
    <Route {...rest} render={(props) =>
        <div>
            <TopBar admin={!!admin} />
            <div className='wrapperContainer'>
                <Menu />
                <Container className='mainContainer'>
                    <AlertMessage {...props} />
                    {isLoggedIn
                        ? rightsResolver(userRights, allowedRights)
                            ? <Component {...props} {...props.match.params} />
                            : <ErrorPage error={403} />
                        : <ErrorPage error={401} />}
                </Container>
            </div>
        </div>
    } />
)
AuthRoute.contextTypes = {
    isLoggedIn: PropTypes.bool
}

class AppRoute extends Component {
    state = {
        userRights: []
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/admin/profile' })
            .then(response => response.json())
            .then(profile => {
                const userRights = getRightsFromGroups(profile.groups)
                if (profile.admin) userRights.push('LOCAL_AUTHORITY_ADMIN')
                if (profile.agent.admin) userRights.push('ADMIN')
                this.setState({ userRights })
            })
    }
    render() {
        const { userRights } = this.state
        return (
            <Switch>
                <PublicRoute exact path='/' component={Home} />

                <Route path='/choix-collectivite' component={SelectLocalAuthority} />

                <AuthRoute path='/profil' component={UserProfile} menu={MenuBar} />

                <Route exact path='/actes'>
                    <Redirect to='/actes/liste' />
                </Route>
                <AuthRoute path='/actes/liste' userRights={userRights} allowedRights={['ACTES_DEPOSIT', 'ACTES_DISPLAY']} component={ActeList} menu={MenuBar} />
                <AuthRoute path='/actes/brouillons/:uuid' userRights={userRights} allowedRights={['ACTES_DEPOSIT']} component={NewActeSwitch} menu={MenuBar} />
                <AuthRoute path='/actes/brouillons' userRights={userRights} allowedRights={['ACTES_DEPOSIT']} component={DraftList} menu={MenuBar} />
                <AuthRoute path='/actes/nouveau' userRights={userRights} allowedRights={['ACTES_DEPOSIT']} component={NewActeSwitch} menu={MenuBar} />
                <AuthRoute path='/actes/:uuid' userRights={userRights} allowedRights={['ACTES_DEPOSIT', 'ACTES_DISPLAY']} component={Acte} menu={MenuBar} />

                <Route exact path='/pes'>
                    <Redirect to="/pes/liste" />
                </Route>
                <AuthRoute path='/pes/retour/liste' userRights={userRights} allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']} component={PesRetourList} menu={MenuBar} />
                <AuthRoute path='/pes/liste' userRights={userRights} allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']} component={PesList} menu={MenuBar} />
                <AuthRoute path='/pes/nouveau' userRights={userRights} allowedRights={['PES_DEPOSIT']} component={NewPes} menu={MenuBar} />
                <AuthRoute path='/pes/:uuid' userRights={userRights} allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']} component={Pes} menu={MenuBar} />

                <Route exact path='/admin'>
                    <Redirect to="/admin/ma-collectivite" />
                </Route>
                <AuthRoute path='/admin/parametrage-instance' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AdminInstance} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/agents/:uuid' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AdminProfile} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/agents' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AgentList} menu={AdminMenuBar} admin={true} />

                <AuthRoute path='/admin/ma-collectivite/agent/:uuid' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AgentProfile} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/ma-collectivite/groupes/:uuid' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={Group} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/ma-collectivite/groupes' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={Group} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/ma-collectivite/actes' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={ActeLocalAuthorityParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/ma-collectivite/pes' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={PesLocalAuthorityParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/ma-collectivite' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthority} menu={AdminMenuBar} admin={true} />

                <AuthRoute path='/admin/collectivite/:localAuthorityUuid/agent/:uuid' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AgentProfile} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/collectivite/:localAuthorityUuid/groupes/:uuid' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={Group} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/collectivite/:localAuthorityUuid/groupes' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={Group} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/collectivite/:uuid/actes' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={ActeLocalAuthorityParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/collectivite/:uuid/pes' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={PesLocalAuthorityParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/collectivite/:uuid' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthority} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/collectivite' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthorityList} menu={AdminMenuBar} admin={true} />

                <AuthRoute path='/admin/actes/parametrage-module' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={ActeModuleParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path='/admin/pes/parametrage-module' userRights={userRights} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={PesModuleParams} menu={AdminMenuBar} admin={true} />

                <PublicRoute path='/admin/*' component={() => <ErrorPage error={404} />} menu={AdminMenuBar} />
                <PublicRoute path='/*' component={() => <ErrorPage error={404} />} menu={MenuBar} />
            </Switch>
        )
    }
}

const TranslatedApp = translate('api-gateway')(App)

render((
    <I18nextProvider i18n={i18n}>
        <Router history={history}>
            <TranslatedApp>
                <AppRoute />
            </TranslatedApp>
        </Router>
    </I18nextProvider>
), document.getElementById('app'))