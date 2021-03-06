import React, { Component, Fragment } from 'react'
import { HashLink as Link } from 'react-router-hash-link'
import PropTypes from 'prop-types'
import { translate } from 'react-i18next'
import ReactMarkdown from 'react-markdown'
import {Segment, Grid, Icon} from 'semantic-ui-react'
import moment from 'moment'
import { checkStatus } from './_util/utils'
import { notifications } from './_util/Notifications'

import { withAuthContext } from './Auth'
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
        certificate: {}
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling,_addNotification } = this.context
        _fetchWithAuthzHandling({ url: '/api/admin/instance/welcome-message' })
            .then(response => response.text())
            .then(json => this.setState({ welcomeMessage: json }))
        getLastUpdate()
            .then(lastUpdate => this.setState({ lastUpdate }))

        if(this.props.authContext.isLoggedIn) {
            _fetchWithAuthzHandling({url: '/api/api-gateway/certInfos'})
                .then(checkStatus)
                .then(response => response.json())
                .then(certificate => this.setState({certificate}))
                .catch(response => {
                    if (response.status !== 401) {
                        response.text().then(text => {
                            _addNotification(notifications.defaultError, 'notifications.title', text)
                        })
                    }
                })
        }
    }
    render() {
        const { t } = this.context
        const { authContext } = this.props
        const localAuthoritySlug = getLocalAuthoritySlug()
        const { certificate, welcomeMessage } = this.state
        const pairedCertificate = authContext.user && authContext.user.certificate
        const isCertificatePaired = pairedCertificate
            && certificate.serial === pairedCertificate.serial
            && certificate.issuer === pairedCertificate.issuer
            && certificate.status === 'VALID'
        let days = null
        if(isCertificatePaired && pairedCertificate.expiredDate) {
            let expirationDate = moment(pairedCertificate.expiredDate)
            let today = moment()
            let duration = moment.duration(expirationDate.diff(today))
            days = Math.trunc(duration.asDays())
        }

        const certificateError = (days && days <= 60) || (certificate.status !== 'VALID')

        return (
            <Fragment>
                {welcomeMessage &&
                    <Segment>
                        <ReactMarkdown source={welcomeMessage}/>
                    </Segment>
                }
                {(authContext.isLoggedIn && localAuthoritySlug) && (
                    <Grid columns={2}>
                        <Grid.Column largeScreen={10} computer={10} mobile={16}>
                            <Segment>
                                <h2>{t('last_update')}</h2>
                                <ReactMarkdown source={this.state.lastUpdate} />
                            </Segment>
                        </Grid.Column>
                        <Grid.Column largeScreen={6} computer={6} mobile={16}>
                            <Segment>
                                {certificate && (
                                    <Fragment>
                                        <h2>
                                            {t('profile.certificate.title')}
                                        </h2>
                                        <Icon.Group size={'large'}>
                                            <Icon name='file outline' color={certificateError ? 'red': (days && days <= 90 ?  'orange' : 'black')}/>
                                            <Icon corner name={certificateError ? 'close': 'checkmark'} color={certificateError? 'red': (days && days <= 90 ?  'orange' : 'black')}/>
                                        </Icon.Group>
                                        {certificate.status === 'NONE' && pairedCertificate && (
                                            <span className={'text-error'}>{t('certificate_not_inserted')}<br/></span>
                                        )}
                                        {certificate.status === 'NONE' && !pairedCertificate && (
                                            <span className={'text-error'}>{t('certificate_not_paired')}<br/></span>
                                        )}
                                        {certificate.status === 'VALID'  && !pairedCertificate && (
                                            <span className={'text-error'}>{t('certificate_not_paired')}<br/>
                                                <Link to={`/${localAuthoritySlug}/profil#certificate`}>{t('clic_to_paire')}<br/></Link>
                                            </span>
                                        )}
                                        {certificate.status === 'EXPIRED' && (
                                            <span className={'text-error'}>{t('error.certificate_required.EXPIRED')}<br/></span>
                                        )}
                                        {(certificate.status === 'VALID' && pairedCertificate &&
                                            (certificate.serial !== pairedCertificate.serial || certificate.issuer !== pairedCertificate.issuer )) && (
                                                <span>{t('certificate_not_paired_user')}<br/></span>
                                            )}
                                        {(certificate.status !== 'NONE' && certificate.status !== 'EXPIRED' && certificate.status !== 'VALID') && (
                                            <span className={'text-error'}>{t('notifications.admin.invalid_certificate')}</span>
                                        )}
                                        {isCertificatePaired && (
                                            <span style={{color: days && days <= 60 ? '#db2828': (days && days <= 90 ?  '#c5551c' : 'inherit')}}>
                                                <span>{t('certification_expiration', { days: days })} (<Link to={`/${localAuthoritySlug}/profil#certificate`}>{t('view_profil')}</Link>)</span>
                                                <br/>
                                                <a href="https://www.sictiam.fr/certificat-electronique/" target="_blank" rel="noopener noreferrer">{t('ask_certificate')}</a>
                                            </span>
                                        )}
                                    </Fragment>
                                )}
                            </Segment>
                        </Grid.Column>
                    </Grid>
                )}
            </Fragment>
        )
    }
}

export default translate(['api-gateway'])(withAuthContext(Home))
