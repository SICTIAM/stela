package fr.sictiam.stela.pesservice.soap.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SoapReturnGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoapReturnGenerator.class);

    public String generateReturn(String status, Object attributes) {
        return generateJson(Arrays.asList(status, attributes));
    }

    public String generateJson(List<Object> object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String body = mapper.writeValueAsString(object);
            return body;
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        return null;

    }

    public String connectionFailedReturn() {
        return generateReturn("NOK", "_ERROR_CONNEXION");
    }
}
