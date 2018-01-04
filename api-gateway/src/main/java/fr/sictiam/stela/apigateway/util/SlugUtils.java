package fr.sictiam.stela.apigateway.util;

import javax.servlet.http.HttpServletRequest;

public class SlugUtils {

    public static String getSlugNameFromRequest(HttpServletRequest request) {
        return request.getServerName().split("\\.")[0];
    }
}
