import React, { Component } from 'react'
import { render } from 'react-dom';
import { I18nextProvider } from 'react-i18next'
import { Router, Route, Switch, Redirect } from 'react-router-dom'
import PropTypes from 'prop-types'
import { Container } from 'semantic-ui-react'
import NotificationSystem from 'react-notification-system'

import 'semantic-ui-css/semantic.min.css';
import '../styles/index.css';

import history from './_util/history'
import i18n from './_util/i18n'
import MenuBar from './_components/MenuBar'
import TopBar from './_components/TopBar'
import Footer from './_components/Footer'
import Home from './Home'
import Acte from './acte/Acte'
import ActeList from './acte/ActeList'
import NewActe from './acte/NewActe'
import PesList from './pes/PesList'
import NewPes from './pes/NewPes'
import AdminMenuBar from './admin/AdminMenuBar'
import AdminDashboard from './admin/AdminDashboard'
import LocalAuthorityList from './admin/acte/LocalAuthorityList'
import LocalAuthority from './admin/acte/LocalAuthority'

import Validator from 'validatorjs'
Validator.useLang(window.localStorage.i18nextLng);

class App extends Component {
    constructor() {
        super()
        this._notificationSystem = null
    }
    static propTypes = {
        children: PropTypes.element.isRequired
    }
    static childContextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        csrfToken: '',
        csrfTokenHeaderName: ''
    }
    getChildContext() {
        return {
            csrfToken: this.state.csrfToken,
            csrfTokenHeaderName: this.state.csrfTokenHeaderName,
            t: this.t,
            _addNotification: this._addNotification
        }
    }
    _addNotification = (notification) => {
        if (this._notificationSystem) {
            this._notificationSystem.addNotification(notification)
        }
    }
    componentDidMount() {
        fetch('/api/csrf-token', { credentials: 'same-origin' })
            .then(response => response.headers)
            .then(headers =>
                this.setState({ csrfToken: headers.get('X-CSRF-TOKEN'), csrfTokenHeaderName: headers.get('X-CSRF-HEADER') }))
    }
    render() {
        return (
            <div>
                <TopBar />
                <NotificationSystem ref={n => this._notificationSystem = n} />
                <div className='wrapperContainer'>
                    {this.props.children}
                </div>
                <Footer />
            </div>
        )
    }
}

const FrontApp = ({ children }) =>
    <div>
        <MenuBar />
        <Container className='mainContainer'>
            {children}
        </Container>
    </div>

const AdminApp = ({ children }) =>
    <div>
        <AdminMenuBar />
        <Container className='mainContainer'>
            {children}
        </Container>
    </div>

const AppRoute = () =>
    <Switch>
        <Route exact path='/' render={() => <FrontApp><Home /></FrontApp>} />

        <Route exact path='/actes'>
            <Redirect to="/actes/liste" />
        </Route>
        <Route path='/actes/liste' render={() => <FrontApp><ActeList /></FrontApp>} />
        <Route path='/actes/nouveau' render={() => <FrontApp><NewActe /></FrontApp>} />
        <Route path='/actes/:uuid' render={({ match }) => <FrontApp><Acte uuid={match.params.uuid} /></FrontApp>} />

        <Route exact path='/pes'>
            <Redirect to="/pes/liste" />
        </Route>
        <Route path='/pes/liste' render={() => <FrontApp><PesList /></FrontApp>} />
        <Route path='/pes/nouveau' render={() => <FrontApp><NewPes /></FrontApp>} />

        <Route exact path='/admin'>
            <Redirect to="/admin/tableau-de-bord" />
        </Route>
        <Route path='/admin/tableau-de-bord' render={() => <AdminApp><AdminDashboard /></AdminApp>} />
        <Route path='/admin/actes/parametrage-collectivite/:uuid' render={({ match }) => <AdminApp><LocalAuthority uuid={match.params.uuid} /></AdminApp>} />
        <Route path='/admin/actes/parametrage-collectivite' render={() => <AdminApp><LocalAuthorityList /></AdminApp>} />
    </Switch>

render((
    <I18nextProvider i18n={i18n}>
        <Router history={history}>
            <App>
                <AppRoute />
            </App>
        </Router>
    </I18nextProvider>
), document.getElementById('app'))