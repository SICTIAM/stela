import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { Segment, Header, Button } from 'semantic-ui-react'
import { translate } from 'react-i18next'
import moment from 'moment'

import { fetchWithAuthzHandling, checkStatus } from '../_util/utils'
import { notifications } from '../_util/Notifications'
import { Field, FieldValue } from './UI'

class CertificateInfos extends Component {
    static contextTypes = {
        csrfToken: PropTypes.string,
        csrfTokenHeaderName: PropTypes.string,
        t: PropTypes.func,
        _addNotification: PropTypes.func
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
        fetchWithAuthzHandling({ url: '/api/api-gateway/certInfos' })
            .then(response => response.json())
            .then(certificate => this.setState({ certificate }))
    }
    pairCertificate = () => {
        if (this.state.certificate.status === 'VALID') {
            fetchWithAuthzHandling({ url: '/api/admin/profile/certificate', method: 'POST', context: this.context })
                .then(checkStatus)
                .then(() => this.context._addNotification(notifications.profile.certificatePairedSuccess))
                .catch(response => {
                    if (response.status === 412) {
                        this.context._addNotification(notifications.profile.certificateNotValid)
                    } else if (response.status === 409) {
                        this.context._addNotification(notifications.profile.certificateConflict)
                    } else {
                        response.json().then(json => {
                            this.context._addNotification(notifications.defaultError, 'notifications.profile.title', json.message)
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
        const isCertificatePaired = certificate.serial === pairedCertificate.serial && certificate.issuer === pairedCertificate.issuer
        return (
            <Segment style={segmentStyle}>
                {(isValid && !isCertificatePaired) &&
                    <Button primary compact basic style={{ float: 'right' }} onClick={this.pairCertificate}>{t('profile.certificate.pair')}</Button>
                }
                {isCertificatePaired &&
                    <span style={{ float: 'right', fontStyle: 'italic' }} onClick={this.pairCertificate}>{t('profile.certificate.paired')}</span>
                }
                < h2 style={headerStyle}>{t('profile.certificate.title')}</h2>

                {isPresent &&
                    <Fragment>
                        <Field htmlFor="serial" label={t('profile.certificate.serial')}>
                            <FieldValue id="serial">{certificate.serial}</FieldValue>
                        </Field>
                        <Field htmlFor="issuer" label={t('profile.certificate.issuer')}>
                            <FieldValue id="issuer">{certificate.issuer}</FieldValue>
                        </Field>
                        <Field htmlFor="issuedDate" label={t('profile.certificate.issuedDate')}>
                            <FieldValue id="issuedDate">{moment(certificate.issuedDate).format('DD/MM/YYYY')}</FieldValue>
                        </Field>
                        <Field htmlFor="expiredDate" label={t('profile.certificate.expiredDate')}>
                            <FieldValue id="expiredDate">{moment(certificate.expiredDate).format('DD/MM/YYYY')}</FieldValue>
                        </Field>
                        <Field htmlFor="status" label={t('profile.certificate.status')}>
                            <FieldValue id="status">{certificate.status}</FieldValue>
                        </Field>

                        <Header as='h3' dividing>{t('profile.certificate.subject')}</Header>
                        <Field htmlFor="subjectCommonName" label={t('profile.certificate.subjectCommonName')}>
                            <FieldValue id="subjectCommonName">{certificate.subjectCommonName}</FieldValue>
                        </Field>
                        <Field htmlFor="subjectOrganization" label={t('profile.certificate.subjectOrganization')}>
                            <FieldValue id="subjectOrganization">{certificate.subjectOrganization}</FieldValue>
                        </Field>
                        <Field htmlFor="subjectOrganizationUnit" label={t('profile.certificate.subjectOrganizationUnit')}>
                            <FieldValue id="subjectOrganizationUnit">{certificate.subjectOrganizationUnit}</FieldValue>
                        </Field>
                        <Field htmlFor="subjectEmail" label={t('profile.certificate.subjectEmail')}>
                            <FieldValue id="subjectEmail">{certificate.subjectEmail}</FieldValue>
                        </Field>

                        <Header as='h3' dividing>{t('profile.certificate.issuer')}</Header>
                        <Field htmlFor="issuerCommonName" label={t('profile.certificate.issuerCommonName')}>
                            <FieldValue id="issuerCommonName">{certificate.issuerCommonName}</FieldValue>
                        </Field>
                        <Field htmlFor="issuerOrganization" label={t('profile.certificate.issuerOrganization')}>
                            <FieldValue id="issuerOrganization">{certificate.issuerOrganization}</FieldValue>
                        </Field>
                        <Field htmlFor="issuerEmail" label={t('profile.certificate.issuerEmail')}>
                            <FieldValue id="issuerEmail">{certificate.issuerEmail}</FieldValue>
                        </Field>
                    </Fragment>
                }
                {!isPresent &&
                    <p>{t('profile.no_certificate')}</p>
                }
            </Segment>
        )
    }
}

export default translate(['api-gateway'])(CertificateInfos)