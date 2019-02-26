package fr.sictiam.stela.apigateway.config.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Component
public class RequestLoggerFilter extends AbstractRequestLoggingFilter {

    private static Logger LOGGER = LoggerFactory.getLogger(RequestLoggerFilter.class);

    @PostConstruct
    public void setDefaultProperties() {
        setIncludeQueryString(true);
        setIncludePayload(true);
        setIncludeClientInfo(true);
        setIncludeHeaders(true);
        setMaxPayloadLength(5000);
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        LOGGER.debug(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        // nothing more to say here
    }
}