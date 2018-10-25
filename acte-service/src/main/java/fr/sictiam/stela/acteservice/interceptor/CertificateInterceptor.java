package fr.sictiam.stela.acteservice.interceptor;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.util.Certificate;
import fr.sictiam.stela.acteservice.service.ExternalRestService;
import fr.sictiam.stela.acteservice.service.exceptions.CertificateAuthenticationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CertificateInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private ExternalRestService externalRestService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Certificate certificate = (Certificate)request.getAttribute("STELA-Certificate");

        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            throw new CertificateAuthenticationFailedException();
        request.setAttribute("STELA-Local-Authority", localAuthority);
        return true;
    }
}
