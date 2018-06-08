package fr.sictiam.stela.apigateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class CertificateInfos {

    private String serial; // x-ssl-client-m-serial
    private String issuer; // x-ssl-client-issuer-dn

    private String subjectCommonName; // x-ssl-client-s-dn-cn
    private String subjectOrganization; // x-ssl-client-s-dn-o
    private String subjectOrganizationUnit; // x-ssl-client-s-dn-ou
    private String subjectEmaill; // x-ssl-client-s-dn-email

    private String issuerCommonName; // x-ssl-client-i-dn-cn
    private String issuerOrganization; // x-ssl-client-i-dn-o
    private String issuerEmaill; // x-ssl-client-i-dn-email

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issuedDate; // x-ssl-client-not-before
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiredDate; // x-ssl-client-not-after

    private CertificateStatus status; // x-ssl-status

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

    @Override
    public String toString() {
        return "CertificateInfos{" +
                "serial='" + serial + '\'' +
                ", issuer='" + issuer + '\'' +
                ", subjectCommonName='" + subjectCommonName + '\'' +
                ", subjectOrganization='" + subjectOrganization + '\'' +
                ", subjectOrganizationUnit='" + subjectOrganizationUnit + '\'' +
                ", subjectEmaill='" + subjectEmaill + '\'' +
                ", issuerCommonName='" + issuerCommonName + '\'' +
                ", issuerOrganization='" + issuerOrganization + '\'' +
                ", issuerEmaill='" + issuerEmaill + '\'' +
                ", issuedDate=" + issuedDate +
                ", expiredDate=" + expiredDate +
                ", status=" + status +
                '}';
    }
}
