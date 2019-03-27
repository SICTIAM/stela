package fr.sictiam.stela.pesservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.sesile.Action;
import fr.sictiam.stela.pesservice.model.sesile.Classeur;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurStatus;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

public class TestDataGenerator {

    public static Optional<LocalAuthority> localAuthority() {
        LocalAuthority localAuthority = new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1", "SICTIAM TEST",
                "214400152", true);
        localAuthority.setGenericProfileUuid("generic-profile-uuid");
        return Optional.of(localAuthority);
    }

    public static JsonNode profileNode() throws IOException {
        String profile = "{ \"email\": \"agent@sictiam.fr\" }";
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(profile);
    }

    public static JsonNode profileWithAgentNode() throws IOException {
        String profile = "{ \"email\": null, \"agent\": { \"email\": \"dev@sictiam.fr\" } }";
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(profile);
    }

    public static Classeur classeur() throws ParseException {
        Classeur classeur = new Classeur();
        classeur.setStatus(ClasseurStatus.FINALIZED);
        Date actionDate = new SimpleDateFormat("dd/MM/yyyy").parse("20/03/2019");
        classeur.setActions(Collections.singletonList(new Action(1, "agent-pes", actionDate, "signature", "RAS")));
        return classeur;
    }
}
