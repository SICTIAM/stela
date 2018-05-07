package fr.sictiam.stela.apigateway.config.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import fr.sictiam.stela.apigateway.model.StelaUserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

public class AuthorizationHeaderFilter extends ZuulFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationHeaderFilter.class);

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1; // run before PreDecoration
    }

    @Override
    public boolean shouldFilter() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication == null || !(authentication instanceof OpenIdCAuthentication));
    }

    @Override
    public Object run() {
        LOGGER.debug("Adding Authorization header to downstream request");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OpenIdCAuthentication authenticationOpen = (OpenIdCAuthentication) authentication;
        RequestContext ctx = RequestContext.getCurrentContext();

        ctx.addZuulRequestHeader("Authorization", "Bearer " + authenticationOpen.getAccessToken());
        ctx.addZuulRequestHeader("ACR", authenticationOpen.getAcr());
        ctx.addZuulRequestHeader("STELA-Active-Token",
                ((StelaUserInfo) authenticationOpen.getUserInfo()).getStelaToken());
        return null;
    }
}
