package fr.sictiam.stela.convocationservice.filter;

import fr.sictiam.stela.convocationservice.dao.RecipientRepository;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.exception.TokenAuthenticationException;
import fr.sictiam.stela.convocationservice.service.ExternalRestService;
import org.apache.commons.compress.utils.Sets;
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

        String token;
        if ((token = request.getParameter("token")) != null) {
            Optional<Recipient> opt = recipientRepository.findByToken(token);
            if (!opt.isPresent()) {
                LOGGER.error("No user found with token {}", token);
                throw new TokenAuthenticationException();
            }

            // Set attributes
            Recipient recipient = opt.get();
            request.setAttribute("STELA-Current-Local-Authority-UUID", recipient.getLocalAuthority().getUuid());
            request.setAttribute("STELA-Current-Profile-Rights", Sets.newHashSet(Right.CONVOCATION_DISPLAY));
            request.setAttribute("STELA-Current-Recipient", recipient);
        }

        return true;
    }
}
