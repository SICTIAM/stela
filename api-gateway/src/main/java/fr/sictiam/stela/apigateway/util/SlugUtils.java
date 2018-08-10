package fr.sictiam.stela.apigateway.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class SlugUtils {

    public static String getSlugNameFromRequest(HttpServletRequest request) {
        return request.getServerName().split("\\.")[0];
    }

    public static String getSlugNameFromParamsOrHeaders(HttpServletRequest request) {
        String slugParam = request.getParameter("localAuthoritySlug");
        return StringUtils.isNotBlank(slugParam) ? slugParam : request.getHeader("localAuthoritySlug");
    }
}
