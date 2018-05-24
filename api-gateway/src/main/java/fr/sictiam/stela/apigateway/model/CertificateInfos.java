package fr.sictiam.stela.apigateway.model;

import java.time.LocalDate;

public class CertificateInfos {

    private String serial; // HTTP_X_SSL_CLIENT_M_SERIAL
    private String issuer; // HTTP_X_SSL_CLIENT_I_DN

    private String subjectCommonName; // HTTP_X_SSL_CLIENT_S_DN_CN
    private String subjectOrganization; //HTTP_X_SSL_CLIENT_S_DN_O
    private String subjectOrganizationUnit; // HTTP_X_SSL_CLIENT_S_DN_OU
    private String subjectEmaill; // HTTP_X_SSL_CLIENT_S_DN_EMAIL

    private String issuerCommonName; // HTTP_X_SSL_CLIENT_I_DN_CN
    private String issuerOrganization; //HTTP_X_SSL_CLIENT_I_DN_O
    private String issuerEmaill; // HTTP_X_SSL_CLIENT_I_DN_EMAIL

    private LocalDate issuedDate; // HTTP_X_SSL_CLIENT_NOT_BEFORE
    private LocalDate expiredDate; // HTTP_X_SSL_CLIENT_NOT_AFTER

    private CertificateStatus status; // X-Ssl-Status

    public CertificateInfos() {
    }

    public CertificateInfos(String serial, String issuer, String subjectCommonName, String subjectOrganization,
            String subjectOrganizationUnit, String subjectEmaill, String issuerCommonName, String issuerOrganization,
            String issuerEmaill, LocalDate issuedDate, LocalDate expiredDate, CertificateStatus status) {
        this.serial = serial;
        this.issuer = issuer;
        this.subjectCommonName = subjectCommonName;
        this.subjectOrganization = subjectOrganization;
        this.subjectOrganizationUnit = subjectOrganizationUnit;
        this.subjectEmaill = subjectEmaill;
        this.issuerCommonName = issuerCommonName;
        this.issuerOrganization = issuerOrganization;
        this.issuerEmaill = issuerEmaill;
        this.issuedDate = issuedDate;
        this.expiredDate = expiredDate;
        this.status = status;
    }

    public String getSerial() {
        return serial;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getSubjectCommonName() {
        return subjectCommonName;
    }

    public String getSubjectOrganization() {
        return subjectOrganization;
    }

    public String getSubjectOrganizationUnit() {
        return subjectOrganizationUnit;
    }

    public String getSubjectEmaill() {
        return subjectEmaill;
    }

    public String getIssuerCommonName() {
        return issuerCommonName;
    }

    public String getIssuerOrganization() {
        return issuerOrganization;
    }

    public String getIssuerEmaill() {
        return issuerEmaill;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public CertificateStatus getStatus() {
        return status;
    }
}
