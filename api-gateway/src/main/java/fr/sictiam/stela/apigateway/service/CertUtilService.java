package fr.sictiam.stela.apigateway.service;

import fr.sictiam.stela.apigateway.model.CertificateInfos;
import fr.sictiam.stela.apigateway.model.CertificateStatus;
import fr.sictiam.stela.apigateway.model.util.AuthorizationContextClasses;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class CertUtilService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertUtilService.class);

    @Value("${application.certVerificationEnabled}")
    boolean certVerificationEnabled;

    public boolean checkCert(String acr) {
        return !certVerificationEnabled || AuthorizationContextClasses.EIDAS_SUBSTANTIAL.getValue().equals(acr);
    }

    public boolean checkCert(HttpServletRequest request) {
        CertificateInfos certificateInfos = getCertInfosFromHeaders(request);
        return !certVerificationEnabled || CertificateStatus.VALID.equals(certificateInfos.getStatus());
    }

    public CertificateInfos getCertInfosFromHeaders(HttpServletRequest request) {
        return new CertificateInfos(
                request.getHeader("x-ssl-client-m-serial"),
                request.getHeader("x-ssl-client-issuer-dn"),
                request.getHeader("x-ssl-client-s-dn-cn"),
                request.getHeader("x-ssl-client-s-dn-o"),
                request.getHeader("x-ssl-client-s-dn-ou"),
                request.getHeader("x-ssl-client-s-dn-email"),
                request.getHeader("x-ssl-client-i-dn-cn"),
                request.getHeader("x-ssl-client-i-dn-o"),
                request.getHeader("x-ssl-client-i-dn-email"),
                timestampToLocalDate(request.getHeader("x-ssl-client-not-before").replace("Z", "")),
                timestampToLocalDate(request.getHeader("x-ssl-client-not-after").replace("Z", "")),
                StringUtils.isEmpty(request.getHeader("x-ssl-status")) ? CertificateStatus.NONE
                        : CertificateStatus.valueOf(request.getHeader("x-ssl-status"))
        );
    }

    private LocalDate timestampToLocalDate(String timestamp) {
        if (StringUtils.isEmpty(timestamp)) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(timestamp)), ZoneOffset.UTC).toLocalDate();
    }
}
