import React, { Component } from 'react'
import { render } from 'react-dom';
import { I18nextProvider } from 'react-i18next'
import { Router, Route, Switch } from 'react-router-dom'
import PropTypes from 'prop-types'
import { Container } from 'semantic-ui-react'
import NotificationSystem from 'react-notification-system'

import 'semantic-ui-css/semantic.min.css';
import '../styles/index.css';

import history from './util/history'
import i18n from './util/i18n'
import MenuBar from './MenuBar'
import Home from './Home'
import Footer from './Footer'
import Miat from './Miat'
import PesList from './PesList'

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
        <Route path='/miat' component={Miat} />
        <Route path='/pes' component={PesList} />
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