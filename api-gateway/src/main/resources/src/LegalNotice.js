import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import ReactMarkdown from 'react-markdown'
import { Segment } from 'semantic-ui-react'

import { Page } from './_components/UI'

class LegalNotice extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        legalNotice: ''
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/instance/legal-notice' })
            .then(response => response.text())
            .then(json => this.setState({ legalNotice: json }))
    }
    render() {
        const { t } = this.context
        return (
            <Page title={t('legal_notice')}>
                <Segment>
                    <ReactMarkdown source={this.state.legalNotice} />
                </Segment>
            </Page>
        )
    }
}

export default translate(['api-gateway'])(LegalNotice)