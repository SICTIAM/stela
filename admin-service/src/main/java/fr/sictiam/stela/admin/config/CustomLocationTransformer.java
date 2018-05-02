package fr.sictiam.stela.admin.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.transport.http.WsdlDefinitionHandlerAdapter;

import javax.servlet.http.HttpServletRequest;

public class CustomLocationTransformer extends WsdlDefinitionHandlerAdapter {

    @Value("${application.url}")
    String applicationUrl;

    @Override
    protected String transformLocation(String location, HttpServletRequest request) {
        StringBuilder url = new StringBuilder(applicationUrl);
        url.append("/externalws/");
        String siren = StringUtils.removeStart(request.getRequestURI().split("/")[2], "sys");
        url.append(siren);
        if (location.startsWith("/")) {
            // a relative path, prepend the context path
            url.append(request.getContextPath()).append(location);
            return url.toString();
        } else {
            int idx = location.indexOf("://");
            if (idx != -1) {
                // a full url
                idx = location.indexOf('/', idx + 3);
                if (idx != -1) {
                    String path = location.substring(idx);
                    url.append(path);
                    return url.toString();
                }
            }
        }
        // unknown location, return the original
        return location;
    }
}
