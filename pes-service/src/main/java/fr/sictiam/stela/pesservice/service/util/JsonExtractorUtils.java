package fr.sictiam.stela.pesservice.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

public class JsonExtractorUtils {

    public static String extractEmailFromProfile(JsonNode node) {
        return !node.get("email").isNull() && StringUtils.isNotBlank(node.get("email").asText())
                ? node.get("email").asText()
                : node.get("agent").get("email").asText();
    }
}
