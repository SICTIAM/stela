import React, { Fragment } from 'react'
import PropTypes from 'prop-types'
import { Header } from 'semantic-ui-react'
import moment from 'moment'

import { FieldInline, FieldValue } from './UI'

const CertificateInfosUI = ({ certificate }, { t }) =>
    <Fragment>
        <FieldInline htmlFor="serial" label={t('certificate.serial')}>
            <FieldValue id="serial">{certificate.serial}</FieldValue>
        </FieldInline>
        <FieldInline htmlFor="issuer" label={t('certificate.issuer')}>
            <FieldValue id="issuer">{certificate.issuer}</FieldValue>
        </FieldInline>
        <FieldInline htmlFor="issuedDate" label={t('certificate.issuedDate')}>
            <FieldValue id="issuedDate">{moment(certificate.issuedDate).format('DD/MM/YYYY')}</FieldValue>
        </FieldInline>
        <FieldInline htmlFor="expiredDate" label={t('certificate.expiredDate')}>
            <FieldValue id="expiredDate">{moment(certificate.expiredDate).format('DD/MM/YYYY')}</FieldValue>
        </FieldInline>
        {certificate.status && (
            <FieldInline htmlFor="status" label={t('profile.certificate.status')}>
                <FieldValue id="status">{certificate.status}</FieldValue>
            </FieldInline>
        )}

        <Header as='h3' dividing>{t('certificate.subject')}</Header>
        <FieldInline htmlFor="subjectCommonName" label={t('certificate.subjectCommonName')}>
            <FieldValue id="subjectCommonName">{certificate.subjectCommonName}</FieldValue>
        </FieldInline>
        <FieldInline htmlFor="subjectOrganization" label={t('certificate.subjectOrganization')}>
            <FieldValue id="subjectOrganization">{certificate.subjectOrganization}</FieldValue>
        </FieldInline>
        <FieldInline htmlFor="subjectOrganizationUnit" label={t('certificate.subjectOrganizationUnit')}>
            <FieldValue id="subjectOrganizationUnit">{certificate.subjectOrganizationUnit}</FieldValue>
        </FieldInline>
        <FieldInline htmlFor="subjectEmail" label={t('certificate.subjectEmail')}>
            <FieldValue id="subjectEmail">{certificate.subjectEmail}</FieldValue>
        </FieldInline>

        <Header as='h3' dividing>{t('certificate.issuer')}</Header>
        <FieldInline htmlFor="issuerCommonName" label={t('certificate.issuerCommonName')}>
            <FieldValue id="issuerCommonName">{certificate.issuerCommonName}</FieldValue>
        </FieldInline>
        <FieldInline htmlFor="issuerOrganization" label={t('certificate.issuerOrganization')}>
            <FieldValue id="issuerOrganization">{certificate.issuerOrganization}</FieldValue>
        </FieldInline>
        <FieldInline htmlFor="issuerEmail" label={t('certificate.issuerEmail')}>
            <FieldValue id="issuerEmail">{certificate.issuerEmail}</FieldValue>
        </FieldInline>
    </Fragment>

CertificateInfosUI.contextTypes = {
    t: PropTypes.func
}
export default CertificateInfosUI