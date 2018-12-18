package fr.sictiam.stela.convocationservice.filter;

import fr.sictiam.stela.convocationservice.dao.RecipientRepository;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.exception.TokenAuthenticationException;
import fr.sictiam.stela.convocationservice.service.ExternalRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

public class TokenInterceptor extends HandlerInterceptorAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(TokenInterceptor.class);

    @Autowired
    private ExternalRestService externalRestService;

    @Autowired
    private RecipientRepository recipientRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        LOGGER.info("TokenInterceptor::preHandle ");
        request.getParameterMap().forEach((s, strings) -> LOGGER.info("param {} : {}", s, strings));
        String token;
        if ((token = request.getParameter("token")) != null) {
            Optional<Recipient> opt = recipientRepository.findByToken(token);
            if (!opt.isPresent()) {
                LOGGER.error("No user found with token {}", token);
                throw new TokenAuthenticationException();
            }
        }
        //request.setAttribute("STELA-Local-Authority", localAuthority);
        return true;
    }
}
