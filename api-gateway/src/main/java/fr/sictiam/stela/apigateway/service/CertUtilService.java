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

import java.time.LocalDate;

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
        CertificateInfos certificateInfos = new CertificateInfos(
                request.getHeader("HTTP_X_SSL_CLIENT_M_SERIAL"),
                request.getHeader("HTTP_X_SSL_CLIENT_I_DN"),
                request.getHeader("HTTP_X_SSL_CLIENT_S_DN_CN"),
                request.getHeader("HTTP_X_SSL_CLIENT_S_DN_O"),
                request.getHeader("HTTP_X_SSL_CLIENT_S_DN_OU"),
                request.getHeader("HTTP_X_SSL_CLIENT_S_DN_EMAIL"),
                request.getHeader("HTTP_X_SSL_CLIENT_I_DN_CN"),
                request.getHeader("HTTP_X_SSL_CLIENT_I_DN_O"),
                request.getHeader("HTTP_X_SSL_CLIENT_I_DN_EMAIL"),
                StringUtils.isEmpty(request.getHeader("HTTP_X_SSL_CLIENT_NOT_BEFORE")) ? null
                        : LocalDate.parse(request.getHeader("HTTP_X_SSL_CLIENT_NOT_BEFORE")),
                StringUtils.isEmpty(request.getHeader("HTTP_X_SSL_CLIENT_NOT_AFTER")) ? null
                        : LocalDate.parse(request.getHeader("HTTP_X_SSL_CLIENT_NOT_AFTER")),
                StringUtils.isEmpty(request.getHeader("X-Ssl-Status")) ? null
                        : CertificateStatus.valueOf(request.getHeader("X-Ssl-Status"))
        );
        LOGGER.debug("CertInfos:");
        LOGGER.debug(certificateInfos.toString());
        return certificateInfos;
    }
}
