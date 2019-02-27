package fr.sictiam.stela.apigateway.config;

import com.netflix.hystrix.exception.HystrixTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

public class DefaultFallbackProvider implements FallbackProvider {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultFallbackProvider.class);

    @Override
    public String getRoute() {
        return "*";
    }

    private ClientHttpResponse response(final HttpStatus status, final Optional<String> bodyMessage) {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() {
                return status;
            }

            @Override
            public int getRawStatusCode() {
                return status.value();
            }

            @Override
            public String getStatusText() {
                return status.getReasonPhrase();
            }

            @Override
            public void close() {
            }

            @Override
            public InputStream getBody() {
                String jsonBody = bodyMessage
                        .map(errorMessage -> String.format("{ \"error_detail\" : \"%s\" }", bodyMessage))
                        .orElse("{}");
                return new ByteArrayInputStream(jsonBody.getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        LOGGER.error("Got error {} on route {}, switching to fallback response", cause.getMessage(), route);
        if (cause instanceof HystrixTimeoutException) {
            return response(HttpStatus.GATEWAY_TIMEOUT, Optional.empty());
        } else {
            return response(HttpStatus.INTERNAL_SERVER_ERROR,
                    cause.getMessage() != null ? Optional.of("internal server error - " + cause.getMessage()) : Optional.empty());
        }
    }
}