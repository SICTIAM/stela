package fr.sictiam.stela.apigateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Certificate {

    private String serial; // x-ssl-client-m-serial
    private String issuer; // x-ssl-client-issuer-dn

    private String subjectCommonName; // x-ssl-client-s-dn-cn
    private String subjectOrganization; // x-ssl-client-s-dn-o
    private String subjectOrganizationUnit; // x-ssl-client-s-dn-ou
    private String subjectEmail; // x-ssl-client-s-dn-email

    private String issuerCommonName; // x-ssl-client-i-dn-cn
    private String issuerOrganization; // x-ssl-client-i-dn-o
    private String issuerEmail; // x-ssl-client-i-dn-email

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issuedDate; // x-ssl-client-not-before
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiredDate; // x-ssl-client-not-after

    private CertificateStatus status; // x-ssl-status

    public Certificate() {
    }

    public Certificate(String serial, String issuer, String subjectCommonName, String subjectOrganization,
            String subjectOrganizationUnit, String subjectEmail, String issuerCommonName, String issuerOrganization,
            String issuerEmail, LocalDate issuedDate, LocalDate expiredDate, CertificateStatus status) {
        this.serial = serial;
        this.issuer = issuer;
        this.subjectCommonName = subjectCommonName;
        this.subjectOrganization = subjectOrganization;
        this.subjectOrganizationUnit = subjectOrganizationUnit;
        this.subjectEmail = subjectEmail;
        this.issuerCommonName = issuerCommonName;
        this.issuerOrganization = issuerOrganization;
        this.issuerEmail = issuerEmail;
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

    public String getSubjectEmail() {
        return subjectEmail;
    }

    public String getIssuerCommonName() {
        return issuerCommonName;
    }

    public String getIssuerOrganization() {
        return issuerOrganization;
    }

    public String getIssuerEmail() {
        return issuerEmail;
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

    @Override
    public String toString() {
        return "Certificate{" +
                "serial='" + serial + '\'' +
                ", issuer='" + issuer + '\'' +
                ", subjectCommonName='" + subjectCommonName + '\'' +
                ", subjectOrganization='" + subjectOrganization + '\'' +
                ", subjectOrganizationUnit='" + subjectOrganizationUnit + '\'' +
                ", subjectEmail='" + subjectEmail + '\'' +
                ", issuerCommonName='" + issuerCommonName + '\'' +
                ", issuerOrganization='" + issuerOrganization + '\'' +
                ", issuerEmail='" + issuerEmail + '\'' +
                ", issuedDate=" + issuedDate +
                ", expiredDate=" + expiredDate +
                ", status=" + status +
                '}';
    }
}
