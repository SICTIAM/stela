/* eslint-disable no-unused-expressions */
/* eslint-disable indent */
import React, { createContext, Component } from 'react'
import PropTypes from 'prop-types'

import { checkStatus } from './_util/utils'
import { getRightsFromGroups } from './_util/utils'

import { notifications } from './_util/Notifications'

/** Auth Context
 * Content profile, right user, if user is logged in and user information
 * Set default value here
 * Context has two object: Provider and Consumer
 */
export const AuthContext = createContext({
    profile: null,
    userRights: null,
    isLoggedIn: null,
    user: null
})

/** export Consumer, to use easily when we need */
export const AuthConsumer = AuthContext.Consumer

/** This function inject context in component to use context outside render
 * Params: component
 * Return AuthConsumer with context in props authContext (we can acces by: this.props.authContext in Component)
 */
export const withAuthContext = (Component) => {
	return (props) => (
		<AuthConsumer>
			 {(context) => {
				return <Component {...props} authContext={context} />
			 }}
		</AuthConsumer>
	)
}

/** Provider */
class AuthProvider extends Component {
	static contextTypes = {
	    _fetchWithAuthzHandling: PropTypes.func,
	    _addNotification: PropTypes.func,
	}
	state = {
	    profile: null,
	    userRights: null,
	    isLoggedIn: null,
	    user: null
	}
	componentDidMount() {
	    this.checkAuthentication()
	}
	componentDidUpdate() {
	    //this.checkAuthentication()
	}
	/** Call /api/csrf-token
	 * check if user is logged in
	 * true -> get profile and agent
	 */
	async checkAuthentication() {
	    const {_fetchWithAuthzHandling } = this.context
	    _fetchWithAuthzHandling({ url: '/api/csrf-token' })
	        .then(response => {
				if((response.status !== 401) !== this.state.isLoggedIn) {
					this.setState({isLoggedIn: response.status !== 401}, () => {
						if(this.state.isLoggedIn) {
							this.getProfile()
							this.getUser()
						}
					})
				}
				return response
	        })
	        .then(response => response.headers)
	        .then(headers =>
	            this.setState({
	                csrfToken: headers.get('X-CSRF-TOKEN'),
	                csrfTokenHeaderName: headers.get('X-CSRF-HEADER')
	            })
	        )
	}
	getProfile = () => {
	    const {_fetchWithAuthzHandling, _addNotification } = this.context
	    _fetchWithAuthzHandling({ url: '/api/admin/profile' })
	        .then(checkStatus)
	        .then(response => response.json())
	        .then(profile => {
	            this.setState({
	            	profile,
	            	userRights: getRightsFromGroups(profile.groups)})
	        })
	        .catch(response => {
	            if(response.status !== 401) {
	                response.text().then(text => {
	                    _addNotification(notifications.defaultError, 'notifications.title', text)
	                })
	            }
	        })
	}

	getUser = () => {
	    const {_fetchWithAuthzHandling, _addNotification } = this.context
	     _fetchWithAuthzHandling({ url: '/api/admin/agent' })
	        .then(response => response.json())
			.then(json => this.setState({ user: json }))
			.catch(response => {
				if(response.status !== 401) {
				    response.text().then(text => {
				        _addNotification(notifications.defaultError, 'notifications.title', text)
				    })
				}
			})
	}

	render() {
	    return (
	        <AuthContext.Provider value={this.state}>
	            {this.props.children}
	        </AuthContext.Provider>
	    )
	}
}

export default AuthProvider