package fr.sictiam.stela.acteservice;


import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.ArchiveService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActeServiceIntegrationTests extends BaseIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ActeService acteService;

    @Autowired
    private ArchiveService archiveService;

    @Test
    public void testCreateActe() {
        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response =
                this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);
        assertEquals(response.getStatusCode(), HttpStatus.CREATED);
        assertNotNull(response.getBody());

        String acteUuid = response.getBody();
        Acte acte = acteService.getByUuid(acteUuid);

        assertNotNull(acte);
        assertNotNull(acte.getFile());
        assertNotNull(acte.getNumber());
        assertEquals("Delib.pdf", acte.getFilename());
        assertEquals("COD001", acte.getCode());
        assertEquals("Title", acte.getTitle());
        assertTrue(acte.isPublic());
        assertEquals(2, acteService.getAnnexes(acteUuid).size());
        assertEquals(StatusType.CREATED, acte.getStatus());
    }

    @Test
    public void testArchiveCreation() {

        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response =
                this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);
        String acteUuid = response.getBody();

        try {
            archiveService.createArchive();
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        Acte acte = acteService.getByUuid(acteUuid);
        assertEquals(StatusType.ARCHIVE_CREATED, acte.getStatus());
        assertNotNull(acte.getArchiveName());
        assertNotNull(acte.getArchive());

        // TODO temp
        try {
            FileCopyUtils.copy(acte.getArchive(), new File(acte.getArchiveName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MultiValueMap<String, Object> acteWithAttachments() {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("acte", acte());
        params.add("file", new ClassPathResource("data/Delib.pdf"));
        params.add("annexes", new ClassPathResource("data/Annexe_delib.pdf"));
        params.add("annexes", new ClassPathResource("data/Annexe_delib.pdf"));
        return params;
    }

    private Acte acte() {
        return new Acte(UUID.randomUUID().toString(), new Date(), ActeNature.ARRETES_INDIVIDUELS, "COD001",
                "Title", true);
    }
}
