package fr.sictiam.stela.apigateway.service;

import fr.sictiam.stela.apigateway.model.Certificate;
import fr.sictiam.stela.apigateway.model.CertificateStatus;
import fr.sictiam.stela.apigateway.model.util.AuthorizationContextClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CertUtilService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertUtilService.class);

    @Value("${application.certVerificationEnabled}")
    boolean certVerificationEnabled;

    public boolean checkCert(String acr) {
        return !certVerificationEnabled || AuthorizationContextClasses.EIDAS_SUBSTANTIAL.getValue().equals(acr);
    }

    public boolean checkCert(HttpServletRequest request) {
        Certificate certificateInfos = getCertInfosFromHeaders(request);
        return !certVerificationEnabled || CertificateStatus.VALID.equals(certificateInfos.getStatus());
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
