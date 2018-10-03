import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import ReactMarkdown from 'react-markdown'
import { Segment } from 'semantic-ui-react'

import { getLastUpdate, getLocalAuthoritySlug } from './_util/utils'

class Home extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        welcomeMessage: '',
        lastUpdate: ''
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/instance/welcome-message' })
            .then(response => response.text())
            .then(json => this.setState({ welcomeMessage: json }))
        getLastUpdate()
            .then(lastUpdate => this.setState({ lastUpdate }))
    }
    render() {
        const { t, isLoggedIn } = this.context
        const localAuthoritySlug = getLocalAuthoritySlug()
        return (
            <Fragment>
                <Segment>
                    <ReactMarkdown source={this.state.welcomeMessage} />
                </Segment>
                {(isLoggedIn && localAuthoritySlug) && (
                    <Segment>
                        <h2>{t('last_update')}</h2>
                        <ReactMarkdown source={this.state.lastUpdate} />
                    </Segment>
                )}
            </Fragment>
        )
    }
}

export default translate(['api-gateway'])(Home)