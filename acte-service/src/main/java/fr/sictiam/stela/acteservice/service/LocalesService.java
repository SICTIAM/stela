package fr.sictiam.stela.acteservice.service;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalesService.class);

    private Map<String, String> jsons = new HashMap<>();

    public String getJsonTranslation(String lng, String ns) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ClassPathResource resource = new ClassPathResource("/locales/" + lng + "/" + ns + ".json");
            FileCopyUtils.copy(resource.getInputStream(), bos);
        } catch (Exception e) {
            LOGGER.error("Unable to load json translation file: {}", e);
        }
        String jsonString = bos.toString();
        jsons.put(lng + "_" + ns, jsonString);
        return jsonString;
    }

    public String getMessage(String lng, String ns, String path) {
        String key = lng + "_" + ns;
        String json = jsons.containsKey(key) ? jsons.get(key) : getJsonTranslation(lng, ns);
        return JsonPath.read(json, path);
    }

    public String getMessage(String lng, String ns, String path, Map<String, String> variables) {
        String value = getMessage(lng, ns, path);
        if (!variables.isEmpty())
            value = StrSubstitutor.replace(value, variables);
        return value;
    }
}
