import React, { Component, Fragment } from 'react'
import { HashLink as Link } from 'react-router-hash-link'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import ReactMarkdown from 'react-markdown'
import { Segment, Grid } from 'semantic-ui-react'
import moment from 'moment'
import { checkStatus } from './_util/utils'
import { notifications } from './_util/Notifications'

import { getLastUpdate, getLocalAuthoritySlug } from './_util/utils'

class Home extends Component {
    static contextTypes = {
        isLoggedIn: PropTypes.bool,
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func,
        _addNotification: PropTypes.func,
    }
    state = {
        welcomeMessage: '',
        lastUpdate: '',
        certificate: {},
        pairedCertificate: {}
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling,_addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/instance/welcome-message' })
            .then(response => response.text())
            .then(json => this.setState({ welcomeMessage: json }))
        getLastUpdate()
            .then(lastUpdate => this.setState({ lastUpdate }))
        _fetchWithAuthzHandling({ url: '/api/admin/agent' })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => this.setState({ pairedCertificate: json.certificate }))
            .catch(response => {
                if(response.status !== 401) {
                    response.text().then(text => {
                        _addNotification(notifications.defaultError, 'notifications.title', text)
                    })
                }
            })
        _fetchWithAuthzHandling({ url: '/api/api-gateway/certInfos' })
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
        const { t, isLoggedIn } = this.context
        const localAuthoritySlug = getLocalAuthoritySlug()
        const isCertificatePaired = this.state.pairedCertificate
            && this.state.certificate.serial === this.state.pairedCertificate.serial
            && this.state.certificate.issuer === this.state.pairedCertificate.issuer
        let days = null
        if(isCertificatePaired && this.state.pairedCertificate.expiredDate) {
            let expirationDate = moment(this.state.pairedCertificate.expiredDate)
            let today = moment()
            let duration = moment.duration(expirationDate.diff(today))
            days = Math.trunc(duration.asDays())
        }
        return (
            <Fragment>
                <Segment>
                    <ReactMarkdown source={this.state.welcomeMessage} />
                </Segment>
                {(isLoggedIn && localAuthoritySlug && isCertificatePaired) && (
                    <Grid columns={2}>
                        <Grid.Column largeScreen={10} computer={10} mobile={16}>
                            <Segment>
                                <h2>{t('last_update')}</h2>
                                <ReactMarkdown source={this.state.lastUpdate} />
                            </Segment>
                        </Grid.Column>
                        <Grid.Column largeScreen={6} computer={6} mobile={16}>
                            <Segment>
                                <span style={{color: days && days <=60 ? '#db2828': 'inherit'}}>{t('certification_expiration', { days: days })} (<Link to={`/${localAuthoritySlug}/profil#certificate`}>{t('view_profil')}</Link>)</span>
                                <br/>
                                <a href="https://www.sictiam.fr/certificat-electronique/" target="_blank" rel="noopener noreferrer">{t('ask_certificate')}</a>
                            </Segment>
                        </Grid.Column>
                    </Grid>
                )}
                {(isLoggedIn && localAuthoritySlug && !isCertificatePaired) && (
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