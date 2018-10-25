package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.admin.config.LocalDateDeserializer;
import fr.sictiam.stela.admin.model.UI.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import java.time.LocalDate;

@Entity
public class Certificate {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.CertificateViewPublic.class)
    private String uuid;

    @JsonView(Views.CertificateViewPublic.class)
    private String serial; // x-ssl-client-m-serial
    @JsonView(Views.CertificateViewPublic.class)
    private String issuer; // x-ssl-client-issuer-dn

    @JsonView(Views.CertificateViewPublic.class)
    private String subjectCommonName; // x-ssl-client-s-dn-cn
    @JsonView(Views.CertificateViewPublic.class)
    private String subjectOrganization; // x-ssl-client-s-dn-o
    @JsonView(Views.CertificateViewPublic.class)
    private String subjectOrganizationUnit; // x-ssl-client-s-dn-ou
    @JsonView(Views.CertificateViewPublic.class)
    private String subjectEmail; // x-ssl-client-s-dn-email

    @JsonView(Views.CertificateViewPublic.class)
    private String issuerCommonName; // x-ssl-client-i-dn-cn
    @JsonView(Views.CertificateViewPublic.class)
    private String issuerOrganization; // x-ssl-client-i-dn-o
    @JsonView(Views.CertificateViewPublic.class)
    private String issuerEmail; // x-ssl-client-i-dn-email

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonView(Views.CertificateViewPublic.class)
    private LocalDate issuedDate; // x-ssl-client-not-before
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonView(Views.CertificateViewPublic.class)
    private LocalDate expiredDate; // x-ssl-client-not-after

    @Transient
    private CertificateStatus status; // x-ssl-status

    public Certificate() {
    }

    public Certificate(String serial, String issuer, String subjectCommonName, String subjectOrganization,
            String subjectOrganizationUnit, String subjectEmail, String issuerCommonName, String issuerOrganization,
            String issuerEmail, LocalDate issuedDate, LocalDate expiredDate) {
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

    public String getUuid() {
        return uuid;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Certificate that = (Certificate) o;

        if (serial != null ? !serial.equals(that.serial) : that.serial != null) return false;
        return issuer != null ? issuer.equals(that.issuer) : that.issuer == null;
    }

    @Override
    public int hashCode() {
        int result = serial != null ? serial.hashCode() : 0;
        result = 31 * result + (issuer != null ? issuer.hashCode() : 0);
        return result;
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