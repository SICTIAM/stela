import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import { Route, Switch, Redirect } from 'react-router-dom';
import { Container } from 'semantic-ui-react';

import {
  fetchWithAuthzHandling,
  getRightsFromGroups,
  rightsFeatureResolver
} from './_util/utils';

import ErrorPage from './_components/ErrorPage';
import MenuBar from './_components/MenuBar';
import TopBar from './_components/TopBar';
import Home from './Home';
import AlertMessage from './_components/AlertMessage';
import { UserProfile, AdminProfile } from './Profile';
import LegalNotice from './LegalNotice';
import SelectLocalAuthority from './SelectLocalAuthority';
import Acte from './acte/Acte';
import ActePublic from './acte/ActePublic';
import ActeList from './acte/ActeList';
import ActePublicList from './acte/ActePublicList';
import DraftList from './acte/DraftList';
import NewActeSwitch from './acte/NewActeSwitch';
import PesRetourList from './pes/PesRetourList';
import PesList from './pes/PesList';
import NewPes from './pes/NewPes';
import Pes from './pes/Pes';
import AdminMenuBar from './admin/AdminMenuBar';
import AdminInstance from './admin/AdminInstance';
import GenericAccountCreation from './admin/GenericAccountCreation';
import AgentList from './admin/AgentList';
import LocalAuthorityList from './admin/localAuthority/LocalAuthorityList';
import LocalAuthority from './admin/localAuthority/LocalAuthority';
import Group from './admin/localAuthority/Group';
import ActeLocalAuthorityParams from './admin/acte/ActeLocalAuthorityParams';
import ActeLocalAuthorityMigration from './admin/acte/ActeLocalAuthorityMigration';
import PesLocalAuthorityParams from './admin/pes/PesLocalAuthorityParams';
import PesLocalAuthorityMigration from './admin/pes/PesLocalAuthorityMigration';
import AgentProfile from './admin/localAuthority/AgentProfile';
import ActeModuleParams from './admin/acte/ActeModuleParams';
import PesModuleParams from './admin/pes/PesModuleParams';

const PublicRoute = ({ component: Component, ...rest }) => (
  <Route
    {...rest}
    render={props => (
      <div>
        <TopBar />
        <div className="wrapperContainer">
          <MenuBar />
          <Container className="mainContainer">
            <Component {...props} {...props.match.params} />
          </Container>
        </div>
      </div>
    )}
  />
);

const AuthRoute = (
  {
    component: Component,
    menu: Menu,
    admin,
    userRights,
    allowedRights,
    certificate,
    certRequired = false,
    ...rest
  },
  { isLoggedIn }
) => (
  <Route
    {...rest}
    render={props => (
      <div>
        <TopBar admin={!!admin} />
        <div className="wrapperContainer">
          <Menu />
          <Container className="mainContainer">
            {isLoggedIn ? (
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
            ) : (
              <ErrorPage error={401} />
            )}
          </Container>
        </div>
      </div>
    )}
  />
);
AuthRoute.contextTypes = {
  isLoggedIn: PropTypes.bool
};

class AppRoute extends Component {
  state = {
    userRights: [],
    certificate: false
  };
  componentDidMount() {
    fetchWithAuthzHandling({ url: '/api/admin/profile' })
      .then(response => response.json())
      .then(profile => {
        const userRights = getRightsFromGroups(profile.groups);
        if (profile.admin) userRights.push('LOCAL_AUTHORITY_ADMIN');
        if (profile.agent.admin) userRights.push('ADMIN');
        this.setState({ userRights });
      });
    fetchWithAuthzHandling({ url: '/api/admin/certificate/is-valid' })
      .then(response => response.json())
      .then(certificate => this.setState({ certificate }));
  }
  render() {
    const params = this.state;
    return (
      <Switch>
        <PublicRoute exact path="/" {...params} component={Home} />

        <Route
          path="/choix-collectivite"
          {...params}
          component={SelectLocalAuthority}
        />
        <PublicRoute
          path="/mentions-legales"
          {...params}
          component={LegalNotice}
          menu={MenuBar}
        />
        <PublicRoute
          path="/registre-des-deliberations/:uuid"
          {...params}
          component={ActePublic}
          menu={MenuBar}
        />
        <PublicRoute
          path="/registre-des-deliberations"
          {...params}
          component={ActePublicList}
          menu={MenuBar}
        />

        <AuthRoute
          path="/profil"
          {...params}
          component={UserProfile}
          menu={MenuBar}
        />

        <Route exact path="/actes">
          <Redirect to="/actes/liste" />
        </Route>
        <AuthRoute
          path="/actes/liste"
          {...params}
          allowedRights={['ACTES_DEPOSIT', 'ACTES_DISPLAY']}
          component={ActeList}
          menu={MenuBar}
        />
        <AuthRoute
          path="/actes/brouillons/:uuid"
          {...params}
          allowedRights={['ACTES_DEPOSIT']}
          component={NewActeSwitch}
          menu={MenuBar}
          certRequired
        />
        <AuthRoute
          path="/actes/brouillons"
          {...params}
          allowedRights={['ACTES_DEPOSIT']}
          component={DraftList}
          menu={MenuBar}
        />
        <AuthRoute
          path="/actes/nouveau"
          {...params}
          allowedRights={['ACTES_DEPOSIT']}
          component={NewActeSwitch}
          menu={MenuBar}
          certRequired
        />
        <AuthRoute
          path="/actes/:uuid"
          {...params}
          allowedRights={['ACTES_DEPOSIT', 'ACTES_DISPLAY']}
          component={Acte}
          menu={MenuBar}
        />

        <Route exact path="/pes">
          <Redirect to="/pes/liste" />
        </Route>
        <AuthRoute
          path="/pes/retour/liste"
          {...params}
          allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']}
          component={PesRetourList}
          menu={MenuBar}
        />
        <AuthRoute
          path="/pes/liste"
          {...params}
          allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']}
          component={PesList}
          menu={MenuBar}
        />
        <AuthRoute
          path="/pes/nouveau"
          {...params}
          allowedRights={['PES_DEPOSIT']}
          component={NewPes}
          menu={MenuBar}
          certRequired
        />
        <AuthRoute
          path="/pes/:uuid"
          {...params}
          allowedRights={['PES_DEPOSIT', 'PES_DISPLAY']}
          component={Pes}
          menu={MenuBar}
        />

        <Route exact path="/admin">
          <Redirect to="/admin/ma-collectivite" />
        </Route>
        <AuthRoute
          path="/admin/creation-generique"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={GenericAccountCreation}
          menu={AdminMenuBar}
          admin={true}
        />

        <AuthRoute
          path="/admin/parametrage-instance"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={AdminInstance}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/agents/:uuid"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={AdminProfile}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/agents"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={AgentList}
          menu={AdminMenuBar}
          admin={true}
        />

        <AuthRoute
          path="/admin/ma-collectivite/agent/:uuid"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={AgentProfile}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/ma-collectivite/groupes/:uuid"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={Group}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/ma-collectivite/groupes"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={Group}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/ma-collectivite/actes/migration"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={ActeLocalAuthorityMigration}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/ma-collectivite/actes"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={ActeLocalAuthorityParams}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/ma-collectivite/pes/migration"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={PesLocalAuthorityMigration}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/ma-collectivite/pes"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={PesLocalAuthorityParams}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/ma-collectivite"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={LocalAuthority}
          menu={AdminMenuBar}
          admin={true}
        />

        <AuthRoute
          path="/admin/collectivite/:localAuthorityUuid/agent/:uuid"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={AgentProfile}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/collectivite/:localAuthorityUuid/groupes/:uuid"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={Group}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/collectivite/:localAuthorityUuid/groupes"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={Group}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/collectivite/:uuid/actes/migration"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={ActeLocalAuthorityMigration}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/collectivite/:uuid/actes"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={ActeLocalAuthorityParams}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/collectivite/:uuid/pes/migration"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={PesLocalAuthorityMigration}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/collectivite/:uuid/pes"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={PesLocalAuthorityParams}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/collectivite/:uuid"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={LocalAuthority}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/collectivite"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={LocalAuthorityList}
          menu={AdminMenuBar}
          admin={true}
        />

        <AuthRoute
          path="/admin/actes/parametrage-module"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={ActeModuleParams}
          menu={AdminMenuBar}
          admin={true}
        />
        <AuthRoute
          path="/admin/pes/parametrage-module"
          {...params}
          allowedRights={['LOCAL_AUTHORITY_ADMIN']}
          component={PesModuleParams}
          menu={AdminMenuBar}
          admin={true}
        />

        <PublicRoute
          path="/admin/*"
          component={() => <ErrorPage error={404} />}
          menu={AdminMenuBar}
        />
        <PublicRoute
          path="/*"
          component={() => <ErrorPage error={404} />}
          menu={MenuBar}
        />
      </Switch>
    );
  }
}

export default AppRoute;
