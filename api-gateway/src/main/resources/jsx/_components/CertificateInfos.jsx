import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Segment, Header } from 'semantic-ui-react'
import { translate } from 'react-i18next'

import { fetchWithAuthzHandling } from '../_util/utils'
import { Field, FieldValue } from './UI'

class CertificateInfos extends Component {
    static contextTypes = {
        t: PropTypes.func
    }
    state = {
        certificate: {
            serial: '',
            issuer: '',
            subjectCommonName: '',
            subjectOrganization: '',
            subjectOrganizationUnit: '',
            subjectEmaill: '',
            issuerCommonName: '',
            issuerOrganization: '',
            issuerEmaill: '',
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
    render() {
        const { t } = this.context
        const { certificate } = this.state

        return (
            <Segment>
                <h2>{t('profile.certificate.title')}</h2>

                <Field htmlFor="serial" label={t('profile.certificate.serial')}>
                    <FieldValue id="serial">{certificate.serial}</FieldValue>
                </Field>
                <Field htmlFor="issuer" label={t('profile.certificate.issuer')}>
                    <FieldValue id="issuer">{certificate.issuer}</FieldValue>
                </Field>
                <Field htmlFor="issuedDate" label={t('profile.certificate.issuedDate')}>
                    <FieldValue id="issuedDate">{certificate.issuedDate}</FieldValue>
                </Field>
                <Field htmlFor="expiredDate" label={t('profile.certificate.expiredDate')}>
                    <FieldValue id="expiredDate">{certificate.expiredDate}</FieldValue>
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
                <Field htmlFor="subjectEmaill" label={t('profile.certificate.subjectEmaill')}>
                    <FieldValue id="subjectEmaill">{certificate.subjectEmaill}</FieldValue>
                </Field>

                <Header as='h3' dividing>{t('profile.certificate.issuer')}</Header>
                <Field htmlFor="issuerCommonName" label={t('profile.certificate.issuerCommonName')}>
                    <FieldValue id="issuerCommonName">{certificate.issuerCommonName}</FieldValue>
                </Field>
                <Field htmlFor="issuerOrganization" label={t('profile.certificate.issuerOrganization')}>
                    <FieldValue id="issuerOrganization">{certificate.issuerOrganization}</FieldValue>
                </Field>
                <Field htmlFor="issuerEmaill" label={t('profile.certificate.issuerEmaill')}>
                    <FieldValue id="issuerEmaill">{certificate.issuerEmaill}</FieldValue>
                </Field>

            </Segment>
        )
    }
}

export default translate(['api-gateway'])(CertificateInfos)