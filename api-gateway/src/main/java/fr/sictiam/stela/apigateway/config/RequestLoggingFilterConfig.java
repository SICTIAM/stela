package fr.sictiam.stela.apigateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RequestLoggingFilterConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilterConfig.class);

    @Bean
    public CommonsRequestLoggingFilter logFilter() {

        return new CommonsRequestLoggingFilter() {

            @Override protected void beforeRequest(HttpServletRequest request, String message) {

                Map<String, Object> data = new HashMap<>();
                String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                        request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

                Map<String, String> headers = new HashMap<>();
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    headers.put(name, request.getHeader(name));
                }
                data.put("headers", headers);
                data.put("parameters", request.getParameterMap());

                ObjectMapper mapper = new ObjectMapper();
                try {
                    LOGGER.info("Incoming request : " + request.getMethod() + " " + url + " " + mapper.writeValueAsString(data));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            }

            @Override
            protected void afterRequest(HttpServletRequest request, String message) {
            }
        };
    }
}
