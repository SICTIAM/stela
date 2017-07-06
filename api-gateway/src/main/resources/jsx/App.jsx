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
import ActeList from './acte/ActeList'
import NewActe from './acte/NewActe'
import PesList from './pes/PesList'
import NewPes from './pes/NewPes'

class App extends Component {
    constructor() {
        super()
        this._notificationSystem = null
    }
    static propTypes = {
        children: PropTypes.element.isRequired
    }
    static childContextTypes = {
        t: PropTypes.func,
        _addNotification: PropTypes.func
    }
    getChildContext() {
        return {
            t: this.t,
            _addNotification: this._addNotification
        }
    }
    _addNotification = (notification) => {
        if (this._notificationSystem) {
            this._notificationSystem.addNotification(notification)
        }
    }
    render() {
        return (
            <div>
                <TopBar />
                <MenuBar />
                <NotificationSystem ref={n => this._notificationSystem = n} />
                <Container className='mainContainer'>
                    {this.props.children}
                </Container>
                <Footer />
            </div>
        )
    }
}

const AppRoute = () =>
    <Switch>
        <Route exact path='/' component={Home} />

        <Route exact path='/acte'>
            <Redirect to="/acte/list" />
        </Route>
        <Route path='/acte/list' component={ActeList} />
        <Route path='/acte/new' component={NewActe} />

        <Route exact path='/pes'>
            <Redirect to="/pes/list" />
        </Route>
        <Route path='/pes/list' component={PesList} />
        <Route path='/pes/new' component={NewPes} />
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