import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import ReactMarkdown from 'react-markdown'
import { Segment } from 'semantic-ui-react'

import { getUpdates } from './_util/utils'

class Updates extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        updates: ''
    }
    componentDidMount() {
        getUpdates()
            .then(updates => this.setState({ updates }))
    }
    render() {
        const { t } = this.context
        return (
            <Segment>
                <h2>{t('release_notes')}</h2>
                <ReactMarkdown source={this.state.updates} />
            </Segment>
        )
    }
}

export default translate(['api-gateway'])(Updates)