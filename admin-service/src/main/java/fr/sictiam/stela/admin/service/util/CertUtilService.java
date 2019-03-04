package fr.sictiam.stela.admin.service.util;

import fr.sictiam.stela.admin.model.Certificate;
import fr.sictiam.stela.admin.model.CertificateStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CertUtilService {

    @Value("${application.certVerificationEnabled}")
    boolean certVerificationEnabled;

    public boolean checkCert(Certificate certificate, Certificate pairedCertificate) {
        return !certVerificationEnabled || (
                CertificateStatus.VALID.equals(certificate.getStatus()) && certificate.equals(pairedCertificate)
        );
    }

    public CertificateStatus getVerifiedStatus(Certificate certificate, Certificate pairedCertificate) {
        if (!CertificateStatus.VALID.equals(certificate.getStatus())) return certificate.getStatus();
        if (!certificate.equals(pairedCertificate)) return CertificateStatus.NOT_PAIRED;
        return certificate.getStatus();
    }

    public Certificate getCertInfosFromHeaders(HttpServletRequest request) {
        return new Certificate(
                request.getHeader("x-ssl-client-m-serial"),
                request.getHeader("x-ssl-client-issuer-dn"),
                request.getHeader("x-ssl-client-s-dn-cn"),
                request.getHeader("x-ssl-client-s-dn-o"),
                request.getHeader("x-ssl-client-s-dn-ou"),
                request.getHeader("x-ssl-client-s-dn-email"),
                request.getHeader("x-ssl-client-i-dn-cn"),
                request.getHeader("x-ssl-client-i-dn-o"),
                request.getHeader("x-ssl-client-i-dn-email"),
                haDateToLocalDate(request.getHeader("x-ssl-client-not-before")),
                haDateToLocalDate(request.getHeader("x-ssl-client-not-after")),
                StringUtils.isEmpty(request.getHeader("x-ssl-status")) ? CertificateStatus.NONE
                        : CertificateStatus.valueOf(request.getHeader("x-ssl-status"))
        );
    }

    private LocalDate haDateToLocalDate(String timestampZ) {
        if (StringUtils.isEmpty(timestampZ)) return null;
        return LocalDateTime
                .parse(timestampZ.replace("Z", ""), DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                .toLocalDate();
    }
}
