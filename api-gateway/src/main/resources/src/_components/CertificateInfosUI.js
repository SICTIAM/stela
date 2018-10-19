import React, { Fragment } from 'react'
import PropTypes from 'prop-types'
import { Header } from 'semantic-ui-react'
import moment from 'moment'

import { Field, FieldValue } from './UI'

const CertificateInfosUI = ({ certificate }, { t }) =>
    <Fragment>
        <Field htmlFor="serial" label={t('certificate.serial')}>
            <FieldValue id="serial">{certificate.serial}</FieldValue>
        </Field>
        <Field htmlFor="issuer" label={t('certificate.issuer')}>
            <FieldValue id="issuer">{certificate.issuer}</FieldValue>
        </Field>
        <Field htmlFor="issuedDate" label={t('certificate.issuedDate')}>
            <FieldValue id="issuedDate">{moment(certificate.issuedDate).format('DD/MM/YYYY')}</FieldValue>
        </Field>
        <Field htmlFor="expiredDate" label={t('certificate.expiredDate')}>
            <FieldValue id="expiredDate">{moment(certificate.expiredDate).format('DD/MM/YYYY')}</FieldValue>
        </Field>
        {certificate.status && (
            <Field htmlFor="status" label={t('profile.certificate.status')}>
                <FieldValue id="status">{certificate.status}</FieldValue>
            </Field>
        )}

        <Header as='h3' dividing>{t('certificate.subject')}</Header>
        <Field htmlFor="subjectCommonName" label={t('certificate.subjectCommonName')}>
            <FieldValue id="subjectCommonName">{certificate.subjectCommonName}</FieldValue>
        </Field>
        <Field htmlFor="subjectOrganization" label={t('certificate.subjectOrganization')}>
            <FieldValue id="subjectOrganization">{certificate.subjectOrganization}</FieldValue>
        </Field>
        <Field htmlFor="subjectOrganizationUnit" label={t('certificate.subjectOrganizationUnit')}>
            <FieldValue id="subjectOrganizationUnit">{certificate.subjectOrganizationUnit}</FieldValue>
        </Field>
        <Field htmlFor="subjectEmail" label={t('certificate.subjectEmail')}>
            <FieldValue id="subjectEmail">{certificate.subjectEmail}</FieldValue>
        </Field>

        <Header as='h3' dividing>{t('certificate.issuer')}</Header>
        <Field htmlFor="issuerCommonName" label={t('certificate.issuerCommonName')}>
            <FieldValue id="issuerCommonName">{certificate.issuerCommonName}</FieldValue>
        </Field>
        <Field htmlFor="issuerOrganization" label={t('certificate.issuerOrganization')}>
            <FieldValue id="issuerOrganization">{certificate.issuerOrganization}</FieldValue>
        </Field>
        <Field htmlFor="issuerEmail" label={t('certificate.issuerEmail')}>
            <FieldValue id="issuerEmail">{certificate.issuerEmail}</FieldValue>
        </Field>
    </Fragment>

CertificateInfosUI.contextTypes = {
    t: PropTypes.func
}
export default CertificateInfosUI