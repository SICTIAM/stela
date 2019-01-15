package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.model.sitemap.XmlUrl;
import fr.sictiam.stela.apigateway.model.sitemap.XmlUrlSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

@RestController
@RequestMapping("/")
public class AppController {

    @Value("${application.url}")
    String applicationUrl;

    @ResponseBody
    @GetMapping("/robots.txt")
    public ResponseEntity getRobots(HttpServletResponse response) {
        SortedMap<String, String> robotsProperties = new TreeMap<String, String>() {{
            put("User-agent", "*");
            put("Disallow", "");
            put("Sitemap", applicationUrl + "/sitemap.xml");
        }};
        response.setHeader("Content-Disposition", "inline; filename=robots.txt");
        response.addHeader("Content-Type", "text/plain; charset=UTF-8");
        try {
            FileCopyUtils.copy(new ByteArrayInputStream(buildRobots(robotsProperties).getBytes()), response.getOutputStream());
            response.flushBuffer();
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @ResponseBody
    @GetMapping("/sitemap.xml")
    public XmlUrlSet sitemap() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();
        create(xmlUrlSet, "/", XmlUrl.Priority.HIGH);
        return xmlUrlSet;
    }

    private void create(XmlUrlSet xmlUrlSet, String link, XmlUrl.Priority priority) {
        xmlUrlSet.addUrl(new XmlUrl(applicationUrl + link, priority));
    }

    private String buildRobots(SortedMap<String, String> properties) {
        StringBuilder sb = new StringBuilder();
        properties.forEach((property, value) -> sb.append(property).append(": ").append(value).append("\n"));
        return sb.toString();
    }

}
