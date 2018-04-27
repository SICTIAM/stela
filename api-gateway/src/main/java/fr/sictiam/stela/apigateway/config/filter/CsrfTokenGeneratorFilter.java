package fr.sictiam.stela.apigateway.config.filter;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CsrfTokenGeneratorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

        // Spring Security will allow the Token to be included in this header name
        response.setHeader("X-CSRF-HEADER", token.getHeaderName());

        // this is the value of the token to be included as either a header or an HTTP
        // parameter
        response.setHeader("X-CSRF-TOKEN", token.getToken());

        filterChain.doFilter(request, response);
    }
}
