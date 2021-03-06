package fr.sictiam.stela.acteservice;

import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.service.LocalesService;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class LocalesServiceTest {

    LocalesService localService = new LocalesService();

    @Test
    public void testGetLocalesFR() {
        String json = localService.getJsonTranslation("fr", "acte");
        assertThat(json, notNullValue());

        String json_notif = localService.getJsonTranslation("fr", "acte_notification");
        assertThat(json_notif, notNullValue());
    }

    @Test
    public void testGetSimpleMessage() {
        String message = localService.getMessage("fr", "acte", "$.acte.status.CREATED");
        assertThat(message, is("Déposé"));
    }

    @Test
    public void testGetVariableMessage() {

        StatusType statusType = StatusType.ACK_RECEIVED;
        String firstName = "John";
        String lastName = "Doe";
        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", firstName);
        variables.put("lastname", lastName);

        String text = localService.getMessage("fr", "acte_notification", "$.acte.unittest",
                variables);

        assertThat(text,
                is("Bonjour John Doe, le test unitaire est passé"));
    }
}
