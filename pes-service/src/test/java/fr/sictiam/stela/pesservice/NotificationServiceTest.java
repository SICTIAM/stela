package fr.sictiam.stela.pesservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static fr.sictiam.stela.pesservice.service.util.JsonExtractorUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NotificationServiceTest {

    @Test
    public void testGetEmailFromNode() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String jsonString = getJsonNodeProfile(true);
        JsonNode node = mapper.readTree(jsonString);
        assertThat(extractEmailFromProfile(node), is("servicedemat@sictiam.fr"));

        String jsonString2 = getJsonNodeProfile(false);
        JsonNode node2 = mapper.readTree(jsonString2);
        assertThat(extractEmailFromProfile(node2), is("demat@sictiam.fr"));

    }

    private String getJsonNodeProfile(boolean isProfileMailNull) {
        return "{\n" +
                "   \"uuid\":\"6eecafe6-b93b-4672-ab15-53025f2b25cf\",\n" +
                "   \"localAuthority\":{\n" +
                "      \"uuid\":\"77c75063-c682-4050-bd0f-b2f01fa57483\",\n" +
                "      \"name\":\"Mairie de Saint Cézaire sur Siagne\",\n" +
                "      \"slugName\":\"mairie-de-saint-cezaire-sur-siagne\",\n" +
                "      \"siren\":\"210601183\",\n" +
                "      \"activatedModules\":[\n" +
                "         \"PES\",\n" +
                "         \"ACTES\"\n" +
                "      ]\n" +
                "   },\n" +
                "   \"agent\":{\n" +
                "      \"uuid\":\"f8120b43-f2b3-43f2-8d17-7a9e0ccc63c2\",\n" +
                "      \"email\":\"servicedemat@sictiam.fr\",\n" +
                "      \"admin\":true,\n" +
                "      \"certificate\":null,\n" +
                "      \"family_name\":\"SICTIAM\",\n" +
                "      \"given_name\":\"Administrateur démat\"\n" +
                "   },\n" +
                "   \"admin\":true,\n" +
                "   \"groups\":[],\n" +
                "   \"email\":" + (isProfileMailNull ? "null" : "\"demat@sictiam.fr\"") + ",\n" +
                "   \"notificationValues\":[]\n" +
                "}";
    }
}
