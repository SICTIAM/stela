import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import ReactMarkdown from 'react-markdown'
import { Segment } from 'semantic-ui-react'

import { fetchWithAuthzHandling } from './_util/utils'

class Home extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string
    }
    state = {
        welcomeMessage: ''
    }
    componentDidMount() {
        fetchWithAuthzHandling({ url: '/api/admin/instance/welcome-message' })
            .then(response => response.text())
            .then(json => this.setState({ welcomeMessage: json }))
    }
    render() {
        return (
            <Segment>
                <ReactMarkdown source={this.state.welcomeMessage} />
            </Segment>
        )
    }
}

export default translate(['api-gateway'])(Home)