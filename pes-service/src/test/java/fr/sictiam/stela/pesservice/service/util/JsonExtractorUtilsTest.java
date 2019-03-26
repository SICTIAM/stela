package fr.sictiam.stela.pesservice.service.util;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static fr.sictiam.stela.pesservice.TestDataGenerator.*;
import static fr.sictiam.stela.pesservice.service.util.JsonExtractorUtils.*;
import static org.junit.Assert.*;

@ActiveProfiles("test")
public class JsonExtractorUtilsTest {

    @Test
    public void shouldExtractMailFromProfile() throws IOException {
        String email = extractEmailFromProfile(profileNode());
        assertEquals("agent@sictiam.fr", email);
    }

    @Test
    public void shouldExtractMailFromAgent() throws IOException {
        String email = extractEmailFromProfile(profileWithAgentNode());
        assertEquals("dev@sictiam.fr", email);
    }
}
