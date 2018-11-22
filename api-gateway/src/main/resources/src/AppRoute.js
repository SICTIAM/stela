import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { Route, Switch, Redirect } from 'react-router-dom'
import { Container, Loader } from 'semantic-ui-react'

import { getRightsFromGroups, rightsFeatureResolver, checkStatus } from './_util/utils'
import { notifications } from './_util/Notifications'

import ErrorPage from './_components/ErrorPage'
import MenuBar from './_components/MenuBar'
import TopBar from './_components/TopBar'
import Overlay from './_components/Overlay'
import Home from './Home'
import AlertMessage from './_components/AlertMessage'
import { UserProfile, AdminProfile } from './Profile'
import LegalNotice from './LegalNotice'
import Updates from './Updates'
import SelectLocalAuthority from './SelectLocalAuthority'
import Acte from './acte/Acte'
import ActePublic from './acte/ActePublic'
import ActeList from './acte/ActeList'
import ActePublicList from './acte/ActePublicList'
import DraftList from './acte/DraftList'
import NewActeSwitch from './acte/NewActeSwitch'
import PesRetourList from './pes/PesRetourList'
import PesList from './pes/PesList'
import NewPes from './pes/NewPes'
import Pes from './pes/Pes'
import AdminMenuBar from './admin/AdminMenuBar'
import AdminInstance from './admin/AdminInstance'
import GenericAccount from './admin/genericAccount/GenericAccount'
import GenericAccountList from './admin/genericAccount/GenericAccountList'
import AgentList from './admin/AgentList'
import LocalAuthorityList from './admin/localAuthority/LocalAuthorityList'
import LocalAuthority from './admin/localAuthority/LocalAuthority'
import Group from './admin/localAuthority/Group'
import LocalAuthorityCertificate from './admin/localAuthority/LocalAuthorityCertificate'
import ActeLocalAuthorityParams from './admin/acte/ActeLocalAuthorityParams'
import ActeLocalAuthorityMigration from './admin/acte/ActeLocalAuthorityMigration'
import PesLocalAuthorityParams from './admin/pes/PesLocalAuthorityParams'
import PesLocalAuthorityMigration from './admin/pes/PesLocalAuthorityMigration'
import AgentProfile from './admin/localAuthority/AgentProfile'
import ActeModuleParams from './admin/acte/ActeModuleParams'
import PesModuleParams from './admin/pes/PesModuleParams'

const PublicRoute = ({ component: Component, ...rest }) => (
    <Route {...rest} render={props => (
        <div>
            <header>
                <TopBar />
            </header>
            <div className="wrapperContainer">
                <aside>
                    <MenuBar />
                </aside>
                <Container className="mainContainer" as="main" id="content">
                    <Component {...props} {...props.match.params} />
                </Container>
            </div>
        </div>
    )}
    />
)
const AuthRoute = ({ component: Component, menu: Menu, admin, userRights, allowedRights, certificate, certRequired = false, ...rest },
    { isLoggedIn, isMenuOpened } ) => (
    <Route {...rest} render={props => (
        <div>
            <header>
                <TopBar admin={!!admin}/>
            </header>
            <div className="wrapperContainer">
                {isMenuOpened && (
                    <Overlay />
                )}
                <aside>
                    <Menu />
                </aside>
                <main>
                    <Container className="mainContainer" id="content">
                        {isLoggedIn === true && (
                            rightsFeatureResolver(userRights, allowedRights) ? (
                                certificate || !certRequired ? (
                                    <Fragment>
                                        <AlertMessage {...props} />
                                        <Component {...props} {...props.match.params} />
                                    </Fragment>
                                ) : (
                                    <ErrorPage error={'certificate_required'} />
                                )
                            ) : (
                                <ErrorPage error={403} />
                            )
                        )}
                        {isLoggedIn === false && (
                            <ErrorPage error={401} />
                        )}
                        {isLoggedIn === null && (
                            <Loader active inline="centered"></Loader>
                        )}
                    </Container>
                </main>
            </div>
        </div>
    )}
    />
)
AuthRoute.contextTypes = {
    isLoggedIn: PropTypes.bool,
    isMenuOpened: PropTypes.bool
}

class AppRoute extends Component {
    static contextTypes = {
        _fetchWithAuthzHandling: PropTypes.func,
        _addNotification: PropTypes.func
    }
    state = {
        userRights: [],
        certificate: false
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/profile' })
            .then(checkStatus)
            .then(response => response.json())
            .then(profile => {
                const userRights = getRightsFromGroups(profile.groups)
                if (profile.admin) userRights.push('LOCAL_AUTHORITY_ADMIN')
                if (profile.agent.admin) userRights.push('ADMIN')
                this.setState({ userRights })
            })
            .catch(response => {
                if(response.status !== 401) {
                    response.text().then(text => {
                        _addNotification(notifications.defaultError, 'notifications.title', text)
                    })
                }
            })
        _fetchWithAuthzHandling({ url: '/api/admin/certificate/is-valid' })
            .then(checkStatus)
            .then(response => response.json())
            .then(certificate => this.setState({ certificate }))
            .catch(response => {
                if(response.status !== 401) {
                    response.text().then(text => {
                        _addNotification(notifications.defaultError, 'notifications.title', text)
                    })
                }
            })
    }
    render() {
        const params = this.state

        // TODO: Fix redirects, cf https://github.com/ReactTraining/react-router/pull/5209
        return (
            <Switch>
                <PublicRoute exact path="/" {...params} component={Home} />
                <Route exact path="/choix-collectivite" {...params} component={SelectLocalAuthority} />
                <PublicRoute path="/mentions-legales" {...params} component={LegalNotice} menu={MenuBar} />
                <PublicRoute path="/registre-des-deliberations/:uuid" {...params} component={ActePublic} menu={MenuBar} />
                <PublicRoute path="/registre-des-deliberations" {...params} component={ActePublicList} menu={MenuBar} />

                <AuthRoute path="/:localAuthoritySlug/mentions-legales" {...params} component={LegalNotice} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/registre-des-deliberations/:uuid" {...params} component={ActePublic} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/registre-des-deliberations" {...params} component={ActePublicList} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/notes-de-mise-a-jour" {...params} component={Updates} menu={MenuBar} />

                <AuthRoute path="/:localAuthoritySlug/profil" {...params} component={UserProfile} menu={MenuBar} />

                <Route exact path="/:localAuthoritySlug/actes" render={props => (
                    <Redirect to={`/${props.match.params.localAuthoritySlug}/actes/liste`} />
                )} />
                <AuthRoute path="/:localAuthoritySlug/actes/liste/:uuid" {...params} allowedRights={['ACTES_DEPOSIT', 'ACTES_DISPLAY']} component={Acte} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/actes/liste" {...params} allowedRights={['ACTES_DEPOSIT', 'ACTES_DISPLAY']} component={ActeList} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/actes/brouillons/:uuid" {...params} allowedRights={['ACTES_DEPOSIT']} component={NewActeSwitch} menu={MenuBar} certRequired />
                <AuthRoute path="/:localAuthoritySlug/actes/brouillons" {...params} allowedRights={['ACTES_DEPOSIT']} component={DraftList} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/actes/nouveau" {...params} allowedRights={['ACTES_DEPOSIT']} component={NewActeSwitch} menu={MenuBar} certRequired />
                <Route exact path="/:localAuthoritySlug/pes" render={props => (
                    <Redirect to={`/${props.match.params.localAuthoritySlug}/pes/liste`} />
                )} />
                <AuthRoute path="/:localAuthoritySlug/pes/retour/liste" {...params} allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']} component={PesRetourList} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/pes/liste/:uuid" {...params} allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']} component={Pes} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/pes/liste" {...params} allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']} component={PesList} menu={MenuBar} />
                <AuthRoute path="/:localAuthoritySlug/pes/nouveau" {...params} allowedRights={['PES_DEPOSIT']} component={NewPes} menu={MenuBar} certRequired />

                <Route exact path="/:localAuthoritySlug/admin" render={props => (
                    <Redirect to={`/${props.match.params.localAuthoritySlug}/admin/ma-collectivite`} />
                )} />
                <AuthRoute path="/:localAuthoritySlug/admin/compte-generique/nouveau" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={GenericAccount} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/compte-generique/liste" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={GenericAccountList} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/compte-generique/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={GenericAccount} menu={AdminMenuBar} admin={true} />

                <AuthRoute path="/:localAuthoritySlug/admin/parametrage-instance" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AdminInstance} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/agents/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AdminProfile} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/agents" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AgentList} menu={AdminMenuBar} admin={true} />

                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/agent/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AdminProfile} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/certificats/nouveau" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthorityCertificate} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/certificats/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthorityCertificate} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/groupes/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={Group} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/groupes" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={Group} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/actes/migration" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={ActeLocalAuthorityMigration} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/actes" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={ActeLocalAuthorityParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/pes/migration" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={PesLocalAuthorityMigration} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite/pes" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={PesLocalAuthorityParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/ma-collectivite" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthority} menu={AdminMenuBar} admin={true} />

                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:localAuthorityUuid/agent/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={AgentProfile} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:localAuthorityUuid/certificats/nouveau" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthorityCertificate} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:localAuthorityUuid/certificats/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthorityCertificate} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:localAuthorityUuid/groupes/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={Group} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:localAuthorityUuid/groupes" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={Group} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:uuid/actes/migration" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={ActeLocalAuthorityMigration} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:uuid/actes" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={ActeLocalAuthorityParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:uuid/pes/migration" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={PesLocalAuthorityMigration} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:uuid/pes" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={PesLocalAuthorityParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite/:uuid" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthority} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/collectivite" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={LocalAuthorityList} menu={AdminMenuBar} admin={true} />

                <AuthRoute path="/:localAuthoritySlug/admin/actes/parametrage-module" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={ActeModuleParams} menu={AdminMenuBar} admin={true} />
                <AuthRoute path="/:localAuthoritySlug/admin/pes/parametrage-module" {...params} allowedRights={['LOCAL_AUTHORITY_ADMIN']} component={PesModuleParams} menu={AdminMenuBar} admin={true} />

                <PublicRoute path="/:localAuthoritySlug/admin/*" component={() => <ErrorPage error={404} />} menu={AdminMenuBar} />

                <AuthRoute path="/:localAuthoritySlug" {...params} allowedRights={[]} component={Home} menu={MenuBar} />
                <PublicRoute path="/*" component={() => <ErrorPage error={404} />} menu={MenuBar} />
            </Switch>
        )
    }
}

export default AppRoute
