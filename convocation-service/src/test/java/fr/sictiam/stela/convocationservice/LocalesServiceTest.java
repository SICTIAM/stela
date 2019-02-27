package fr.sictiam.stela.convocationservice;

import fr.sictiam.stela.convocationservice.model.HistoryType;
import fr.sictiam.stela.convocationservice.service.LocalesService;
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
        String json = localService.getJsonTranslation("fr", "convocation");
        assertThat(json, notNullValue());

        String json_notif = localService.getJsonTranslation("fr", "convocation_notification");
        assertThat(json_notif, notNullValue());
    }

    @Test
    public void testGetSimpleMessage() {
        String message = localService.getMessage("fr", "convocation", "$.convocation.status.CREATED");
        assertThat(message, is("Créé"));
    }

    @Test
    public void testGetVariableMessage() {

        HistoryType historyType = HistoryType.SENT;
        String firstName = "John";
        String lastName = "Doe";
        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", firstName);
        variables.put("lastname", lastName);

        String text = localService.getMessage("fr", "convocation_notification",
                "$.convocation." + historyType.name() + ".body", variables);

        assertThat(text, is(
                "Bonjour John Doe, <br/> Votre convocation a bien été envoyée aux elus <br/> Cordialement le Sictiam"));
    }
}
