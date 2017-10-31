package fr.sictiam.stela.admin.config.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Component
public class LogRequestResponseFilter implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogRequestResponseFilter.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        traceRequest(request, body);
        ClientHttpResponse clientHttpResponse = execution.execute(request, body);
        traceResponse(clientHttpResponse);

        return clientHttpResponse;
    }

    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
        LOGGER.debug("request URI : " + request.getURI());
        LOGGER.debug("request method : " + request.getMethod());
        LOGGER.debug("request body : " + getRequestBody(body));
    }

    private String getRequestBody(byte[] body) throws UnsupportedEncodingException {
        if (body != null && body.length > 0) {
            return (new String(body, "UTF-8"));
        } else {
            return null;
        }
    }


    private void traceResponse(ClientHttpResponse response) throws IOException {
        LOGGER.debug("response status code: " + response.getStatusCode());
        LOGGER.debug("response status text: " + response.getStatusText());
        LOGGER.debug("response body : " + getBodyString(response));
    }

    private String getBodyString(ClientHttpResponse response) {
        try {
            if (response != null && response.getBody() != null) {
                StringBuilder inputStringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
                String line = bufferedReader.readLine();
                while (line != null) {
                    inputStringBuilder.append(line);
                    inputStringBuilder.append('\n');
                    line = bufferedReader.readLine();
                }
                return inputStringBuilder.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}