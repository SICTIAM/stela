import React, { Component } from 'react';
import PropTypes from 'prop-types';
import NotificationSystem from 'react-notification-system';
import { translate } from 'react-i18next';

import 'semantic-ui-css/semantic.min.css';

import { fetchWithAuthzHandling } from './_util/utils';

class App extends Component {
  constructor() {
    super();
    this._notificationSystem = null;
  }
  static contextTypes = {
    t: PropTypes.func
  };
  static propTypes = {
    children: PropTypes.element.isRequired
  };
  static childContextTypes = {
    csrfToken: PropTypes.string,
    csrfTokenHeaderName: PropTypes.string,
    isLoggedIn: PropTypes.bool,
    user: PropTypes.object,
    t: PropTypes.func,
    _addNotification: PropTypes.func
  };
  state = {
    csrfToken: '',
    csrfTokenHeaderName: '',
    isLoggedIn: false,
    user: {}
  };
  NotificationStyle = {
    Containers: {
      DefaultStyle: {
        width: 400
      }
    }
  };
  getChildContext() {
    return {
      csrfToken: this.state.csrfToken,
      csrfTokenHeaderName: this.state.csrfTokenHeaderName,
      isLoggedIn: this.state.isLoggedIn,
      user: this.state.user,
      t: this.t,
      _addNotification: this._addNotification
    };
  }
  _addNotification = (notification, title, message) => {
    const { t } = this.context;
    notification.title = t(title ? title : notification.title);
    notification.message = t(message ? message : notification.message);
    if (this._notificationSystem) {
      this._notificationSystem.addNotification(notification);
    }
  };
  componentDidMount() {
    fetchWithAuthzHandling({ url: '/api/csrf-token' })
      .then(response => {
        // TODO: Improve (coockies..)
        this.setState({ isLoggedIn: response.status !== 401 }, this.fetchUser);
        return response;
      })
      .then(response => response.headers)
      .then(headers =>
        this.setState({
          csrfToken: headers.get('X-CSRF-TOKEN'),
          csrfTokenHeaderName: headers.get('X-CSRF-HEADER')
        })
      );
  }
  fetchUser = () => {
    if (this.state.isLoggedIn)
      fetchWithAuthzHandling({ url: '/api/admin/agent' })
        .then(response => response.json())
        .then(json => this.setState({ user: json }));
  };
  render() {
    return (
      <div>
        <NotificationSystem
          ref={n => (this._notificationSystem = n)}
          style={this.NotificationStyle}
        />
        {this.props.children}
      </div>
    );
  }
}

export default translate('api-gateway')(App);
