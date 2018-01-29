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

import { fetchWithAuthzHandling } from './_util/utils'
import history from './_util/history'
import i18n from './_util/i18n'
import ErrorPage from './_components/ErrorPage'
import MenuBar from './_components/MenuBar'
import TopBar from './_components/TopBar'
import Home from './Home'
import Profile from './Profile'
import SelectLocalAuthority from './SelectLocalAuthority'
import Acte from './acte/Acte'
import ActeList from './acte/ActeList'
import DraftList from './acte/DraftList'
import NewActeSwitch from './acte/NewActeSwitch'
import PesList from './pes/PesList'
import NewPes from './pes/NewPes'
import AdminMenuBar from './admin/AdminMenuBar'
import AdminDashboard from './admin/AdminDashboard'
import AgentList from './admin/AgentList'
import LocalAuthorityList from './admin/localAuthority/LocalAuthorityList'
import LocalAuthority from './admin/localAuthority/LocalAuthority'
import ActeLocalAuthorityParams from './admin/acte/ActeLocalAuthorityParams'
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
const AuthRoute = ({ component: Component, menu: Menu, ...rest }, { isLoggedIn }) => (
    <Route {...rest} render={(props) =>
        <div>
            <TopBar />
            <div className='wrapperContainer'>
                <Menu />
                <Container className='mainContainer'>
                    {isLoggedIn
                        ? <Component {...props} {...props.match.params} />
                        : <ErrorPage error={401} />}
                </Container>
            </div>
        </div>
    } />
)
AuthRoute.contextTypes = {
    isLoggedIn: PropTypes.bool
}

const AppRoute = () =>
    <Switch>
        <PublicRoute exact path='/' component={Home} />

        <Route path='/choix-collectivite' component={SelectLocalAuthority} />

        <AuthRoute path='/profil' component={Profile} menu={MenuBar} />

        <Route exact path='/actes'>
            <Redirect to="/actes/liste" />
        </Route>
        <AuthRoute path='/actes/liste' component={ActeList} menu={MenuBar} />
        <AuthRoute path='/actes/brouillons/:uuid' component={NewActeSwitch} menu={MenuBar} />
        <AuthRoute path='/actes/brouillons' component={DraftList} menu={MenuBar} />
        <AuthRoute path='/actes/nouveau' component={NewActeSwitch} menu={MenuBar} />
        <AuthRoute path='/actes/:uuid' component={Acte} menu={MenuBar} />

        <Route exact path='/pes'>
            <Redirect to="/pes/liste" />
        </Route>
        <AuthRoute path='/pes/liste' component={PesList} menu={MenuBar} />
        <AuthRoute path='/pes/nouveau' component={NewPes} menu={MenuBar} />

        <Route exact path='/admin'>
            <Redirect to="/admin/tableau-de-bord" />
        </Route>
        <AuthRoute path='/admin/tableau-de-bord' component={AdminDashboard} menu={AdminMenuBar} />
        <AuthRoute path='/admin/agents' component={AgentList} menu={AdminMenuBar} />
        <AuthRoute path='/admin/ma-collectivite/actes' component={ActeLocalAuthorityParams} menu={AdminMenuBar} />
        <AuthRoute path='/admin/ma-collectivite' component={LocalAuthority} menu={AdminMenuBar} />
        <AuthRoute path='/admin/collectivite/:localAuthorityUuid/agent/:uuid' component={AgentProfile} menu={AdminMenuBar} />
        <AuthRoute path='/admin/collectivite/:uuid/actes' component={ActeLocalAuthorityParams} menu={AdminMenuBar} />
        <AuthRoute path='/admin/collectivite/:uuid' component={LocalAuthority} menu={AdminMenuBar} />
        <AuthRoute path='/admin/collectivite' component={LocalAuthorityList} menu={AdminMenuBar} />
        <AuthRoute path='/admin/actes/parametrage-module' component={ActeModuleParams} menu={AdminMenuBar} />
        <AuthRoute path='/admin/pes/parametrage-module' component={PesModuleParams} menu={AdminMenuBar} />
        
        <PublicRoute path='/admin/*' component={() => <ErrorPage error={404} />} menu={AdminMenuBar} />
        <PublicRoute path='/*' component={() => <ErrorPage error={404} />} menu={MenuBar} />
    </Switch>

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