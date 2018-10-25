import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Segment, Button } from 'semantic-ui-react'
import { translate } from 'react-i18next'

import { checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'
import CertificateInfosUI from './CertificateInfosUI'

class CertificateInfos extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func,
        _fetchWithAuthzHandling: PropTypes.func
    }
    state = {
        certificate: {
            serial: '',
            issuer: '',
            subjectCommonName: '',
            subjectOrganization: '',
            subjectOrganizationUnit: '',
            subjectEmail: '',
            issuerCommonName: '',
            issuerOrganization: '',
            issuerEmail: '',
            issuedDate: '',
            expiredDate: '',
            status: ''
        }
    }
    componentDidMount() {
        const { _fetchWithAuthzHandling } = this.context
        _fetchWithAuthzHandling({ url: '/api/api-gateway/certInfos' })
            .then(response => response.json())
            .then(certificate => this.setState({ certificate }))
    }
    pairCertificate = () => {
        const { _fetchWithAuthzHandling, _addNotification } = this.context
        if (this.state.certificate.status === 'VALID') {
            _fetchWithAuthzHandling({ url: '/api/admin/certificate', method: 'POST', context: this.context })
                .then(checkStatus)
                .then(() => _addNotification(notifications.profile.certificatePairedSuccess))
                .catch(response => {
                    if (response.status === 412) {
                        _addNotification(notifications.profile.certificateNotValid)
                    } else if (response.status === 409) {
                        _addNotification(notifications.profile.certificateConflict)
                    } else {
                        response.json().then(json => {
                            _addNotification(notifications.defaultError, 'notifications.profile.title', json.message)
                        })
                    }
                })
        }
    }
    render() {
        const { t } = this.context
        const { certificate } = this.state
        const { pairedCertificate } = this.props
        const isPresent = certificate.status && certificate.status !== 'NONE'
        const isValid = certificate.status === 'VALID'
        const segmentStyle = isValid ? { paddingTop: '1em' } : {}
        const headerStyle = isValid ? { marginTop: '0.5em' } : {}
        const isCertificatePaired = pairedCertificate
            && certificate.serial === pairedCertificate.serial
            && certificate.issuer === pairedCertificate.issuer
        return (
            <Segment style={segmentStyle}>
                {(isValid && !isCertificatePaired) && (
                    <Button primary compact basic style={{ float: 'right' }} onClick={this.pairCertificate}>
                        {t('profile.certificate.pair')}
                    </Button>
                )}
                {isCertificatePaired && (
                    <span style={{ float: 'right', fontStyle: 'italic' }} onClick={this.pairCertificate}>
                        {t('profile.certificate.paired')}
                    </span>
                )}
                <h2 style={headerStyle}>{t('profile.certificate.title')}</h2>

                {isPresent && (
                    <CertificateInfosUI certificate={certificate} />
                )}
                {!isPresent && (
                    <p>{t('profile.no_certificate')}</p>
                )}
            </Segment>
        )
    }
}

export default translate(['api-gateway'])(CertificateInfos)