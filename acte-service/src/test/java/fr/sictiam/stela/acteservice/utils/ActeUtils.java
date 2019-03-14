package fr.sictiam.stela.acteservice.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class ActeUtils {

    public static MultiValueMap<String, Object> acteWithAttachments() {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("acte", acte());
        params.add("file", new ClassPathResource("data/Delib.pdf"));
        params.add("annexes", new ClassPathResource("data/Annexe_delib.pdf"));
        params.add("annexes", new ClassPathResource("data/Annexe_delib.pdf"));
        return params;
    }

    public static Acte acte() {
        Acte acte = new Acte(RandomStringUtils.randomAlphabetic(15), LocalDate.now(), ActeNature.ARRETES_INDIVIDUELS,
                "1-1-1-0-0", "Objet", true, true);
        acte.setProfileUuid("4f146466-ea58-4e5c-851c-46db18ac173b");
        return acte;
    }

    public static LocalAuthority localAuthority() {
        return new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1", "SICTIAM TEST", "214400152", true);
    }

    public static JsonNode profileNode() throws IOException {
        String profile =
                "{ " +
                    "\"email\": \"agent@sictiam.fr\"," +
                    "\"agent\": " +
                        "{" +
                            "\"given_name\" : \"Agent\"," +
                            "\"family_name\" : \"SICTIAM\"" +
                        "}," +
                    "\"notificationValues\" : [" +
                        "{" +
                            "\"name\" : \"ACTES_SENT\"," +
                            "\"uuid\" : \"notif1-uuid\"," +
                            "\"active\" : \"true\"" +
                        "}" +
                    "]" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(profile);
    }
}
