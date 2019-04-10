package fr.sictiam.stela.pesservice.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = LocalesService.class)
@ActiveProfiles("test")
public class LocalesServiceTest {

    @Autowired
    private LocalesService localService;

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
        assertThat(message, is("Déposé"));
    }

    @Test
    public void testGetVariableMessage() {

        String firstName = "John";
        String lastName = "Doe";
        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", firstName);
        variables.put("lastname", lastName);

        String text = localService.getMessage("fr", "pes_notification", "$.pes.unittest",
                variables);

        assertThat(text, is(
                "Bonjour John Doe, le test unitaire est passé"));
    }
}
