package fr.sictiam.stela.apigateway.config.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class RequestLoggerFilter extends OncePerRequestFilter {

    private static Logger LOGGER = LoggerFactory.getLogger(RequestLoggerFilter.class);

    private static final List<MediaType> VISIBLE_TYPES = Arrays.asList(
            MediaType.valueOf("text/*"),
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml"),
            MediaType.MULTIPART_FORM_DATA
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
        }
    }

    protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            beforeRequest(request, response);
            filterChain.doFilter(request, response);
        } finally {
            afterRequest(request, response);
            response.copyBodyToResponse();
        }
    }

    protected void beforeRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (LOGGER.isInfoEnabled()) {

        }
    }

    protected void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (LOGGER.isInfoEnabled()) {
            logRequestHeader(request);
            logRequestBody(request);
            //logResponse(response, request.getRemoteAddr() + "|<");
        }
    }

    private static void logRequestHeader(ContentCachingRequestWrapper request) {
        ObjectMapper mapper = new ObjectMapper();
        String url =
                request.getMethod() + " " + request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                        request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        String queryString = request.getQueryString();

        LOGGER.info("Incoming request: {}", url);

        Map<String, String[]> parameters = request.getParameterMap();
        if (parameters.size() > 0) {
            try {
                LOGGER.info("Parameters: {}", mapper.writeValueAsString(parameters));
            } catch (JsonProcessingException e) {
                LOGGER.error("Json mapping error: {}", e.getMessage());
            }
        }
    }

    private static void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            logContent(content, request.getContentType(), request.getCharacterEncoding(), "body:");
        }
    }

    /* Not use for the moment
    private static void logResponse(ContentCachingResponseWrapper response, String prefix) {
        int status = response.getStatus();
        LOGGER.info("{} {} {}", prefix, status, HttpStatus.valueOf(status).getReasonPhrase());
        response.getHeaderNames().forEach(headerName ->
                response.getHeaders(headerName).forEach(headerValue ->
                        LOGGER.info("{} {}: {}", prefix, headerName, headerValue)));
        LOGGER.info("{}", prefix);
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            logContent(content, response.getContentType(), response.getCharacterEncoding());
        }
    }
    */

    private static void logContent(byte[] content, String contentType, String contentEncoding, String prefix) {
        MediaType mediaType = MediaType.valueOf(contentType);
        boolean visible = VISIBLE_TYPES.stream().anyMatch(visibleType -> visibleType.includes(mediaType));
        if (visible) {
            try {
                String contentString = new String(content, contentEncoding);
                Stream.of(contentString.split("\r\n|\r|\n")).forEach(line -> LOGGER.info("{} {}", prefix, line));
            } catch (UnsupportedEncodingException e) {
                LOGGER.info("[{} bytes content]", content.length);
            }
        } else {
            LOGGER.info("[{} bytes content]", content.length);
        }
    }

    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        } else {
            return new ContentCachingRequestWrapper(request);
        }
    }

    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }
}