import React, {Suspense, lazy} from 'react'
import ReactDOM from 'react-dom'
import { I18nextProvider } from 'react-i18next'
import { Router } from 'react-router-dom'

import './index.css'
import history from './_util/history'
import i18n from './_util/i18n'
import registerServiceWorker from './registerServiceWorker'

import App from './App'
// import AppRoute from './AppRoute'

import LazyTemplate from './lazy/LazyTemplate'

import Validator from 'validatorjs'

const AppRoute = lazy(() => import('./AppRoute'))
Validator.useLang(window.localStorage.i18nextLng)

ReactDOM.render(
    <I18nextProvider i18n={i18n}>

        <Router history={history}>
            <App>
                <Suspense fallback={<LazyTemplate/>}>
                    <AppRoute />
                </Suspense>
            </App>
        </Router>

    </I18nextProvider>,
    document.getElementById('root')
)
registerServiceWorker()
