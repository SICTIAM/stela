package fr.sictiam.stela.pesservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.LocalesService;

public class LocalesServiceTest {

    LocalesService localService = new LocalesService();

    @Test
    public void testGetLocalesFR() {
        String json = localService.getJsonTranslation("fr", "pes");
        assertThat(json, notNullValue());

        String json_notif = localService.getJsonTranslation("fr", "pes_notification");
        assertThat(json_notif, notNullValue());
    }

    @Test
    public void testGetSimpleMessage() {
        String message = localService.getMessage("fr", "pes", "$.pes.status.CREATED");
        assertThat(message, is("Créé"));
    }

    @Test
    public void testGetVariableMessage() {

        StatusType statusType = StatusType.ACK_RECEIVED;
        String firstName = "John";
        String lastName = "Doe";
        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", firstName);
        variables.put("lastname", lastName);

        String text = localService.getMessage("fr", "pes_notification", "$.pes." + statusType.name() + ".body",
                variables);

        assertThat(text, is("Bonjour John Doe, <br/> Votre pes a bien été reçu par la préfecture <br/> Cordialement le Sictiam"));
    }
}
