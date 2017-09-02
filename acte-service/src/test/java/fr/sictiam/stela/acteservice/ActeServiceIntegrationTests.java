package fr.sictiam.stela.acteservice;


import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActeServiceIntegrationTests extends BaseIntegrationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeServiceIntegrationTests.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ActeService acteService;

    @Autowired
    private LocalAuthorityService localAuthorityService;

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
        assertEquals(LocalDate.now(), acte.getDecision());
        assertTrue(acte.isPublic());
        assertEquals(2, acteService.getAnnexes(acteUuid).size());
        assertEquals(StatusType.CREATED, acte.getStatus());
    }

    @Test
    public void testGetAll() {
        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);

        try {
            // sleep some seconds to let async creation of the archive happens
            Thread.sleep(2000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        Acte[] actes = this.restTemplate.getForObject("/api/acte", Acte[].class);
        assertTrue(actes.length > 0);
        assertNotNull(actes[0].getStatus());
        assertNotNull(actes[0].getLastUpdateTime());
    }

    @Test
    public void testArchiveCreation() {

        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response =
                this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);
        String acteUuid = response.getBody();

        try {
            // sleep some seconds to let async creation of the archive happens
            Thread.sleep(2000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.ARCHIVE_CREATED);
        assertTrue(acteHistory.isPresent());
        assertNotNull(acteHistory.get().getFile());
        assertNotNull(acteHistory.get().getFileName());

        Acte acte = acteService.getByUuid(acteUuid);
        assertEquals(StatusType.ARCHIVE_SIZE_CHECKED, acte.getStatus());

        List<ActeHistory> acteHistories = acteService.getHistory(acteUuid);
        assertEquals(3, acteHistories.size());
        assertTrue(acteHistories.stream().anyMatch(acteHistory1 -> acteHistory1.getStatus().equals(StatusType.CREATED)));
        assertTrue(acteHistories.stream().anyMatch(acteHistory1 -> acteHistory1.getStatus().equals(StatusType.ARCHIVE_CREATED)));
        assertTrue(acteHistories.stream().anyMatch(acteHistory1 -> acteHistory1.getStatus().equals(StatusType.ARCHIVE_SIZE_CHECKED)));

        // uncomment to see the generated archive
        // printXmlMessage(acteHistory.get().getFile(), acteHistory.get().getFileName());
    }

    @Test
    public void testArchiveTooLarge() {

        MultiValueMap<String, Object> params = acteWithAttachments();
        // add another annexe to go above the 1MB limit set for the unit tests
        params.add("annexes", new ClassPathResource("data/Annexe_delib.pdf"));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response =
                this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);
        String acteUuid = response.getBody();

        try {
            // sleep some seconds to let async creation of the archive happens
            Thread.sleep(2000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.ARCHIVE_TOO_LARGE);
        assertTrue(acteHistory.isPresent());
    }

    @Test
    public void testCancellation() {
        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response =
                this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);
        String acteUuid = response.getBody();

        try {
            // sleep some seconds to let async creation of the archive happens
            Thread.sleep(2000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        this.restTemplate.postForEntity("/api/acte/{uuid}/status/cancel", null, null, acteUuid);

        try {
            // sleep some seconds to let async creation of the archive happens
            Thread.sleep(2000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        Acte acte = acteService.getByUuid(acteUuid);
        assertEquals(StatusType.ARCHIVE_SIZE_CHECKED, acte.getStatus());

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.CANCELLATION_ARCHIVE_CREATED);
        assertTrue(acteHistory.isPresent());
        assertNotNull(acteHistory.get().getFile());
        assertNotNull(acteHistory.get().getFileName());

        // uncomment to see the generated archive
        // printXmlMessage(acteHistory.get().getFile(), acteHistory.get().getFileName());
    }

    @Test
    public void createAndRetrieveLocalAuthority() {
        LocalAuthority localAuthority = new LocalAuthority("SICTIAM-Test", "999888777", "999", "1", "31");
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);
        assertEquals("SICTIAM-Test", localAuthorityService.getByUuid(localAuthority.getUuid()).getName());
        assertEquals(localAuthority.getUuid(), localAuthorityService.getByUuid(localAuthority.getUuid()).getUuid());
    }

    @Test
    public void partialLocalAuthorityUpdate() {
        LocalAuthority localAuthority = new LocalAuthority("SICTIAM-Test", "999888777", "999", "1", "31");
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);

        String input = "{\"canPublishRegistre\":\"true\", \"department\":\"006\"}";
        HttpEntity<String> patchData = new HttpEntity<>(input);

        this.restTemplate.patchForObject("/api/acte/localAuthority/{uuid}", patchData, String.class, localAuthority.getUuid());

        localAuthority = localAuthorityService.getByUuid(localAuthority.getUuid());

        assertEquals(true, localAuthority.getCanPublishRegistre());
        assertEquals(false, localAuthority.getCanPublishWebSite());
        assertEquals("006", localAuthority.getDepartment());
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
        return new Acte(RandomStringUtils.randomAlphabetic(15), LocalDate.now(), ActeNature.ARRETES_INDIVIDUELS, "COD001",
                "Title", true);
    }

    private void printXmlMessage(byte[] file, String filename) {
        try {
            FileCopyUtils.copy(file, new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Optional<ActeHistory> getActeHistoryForStatus(String acteUuid, StatusType status) {
        return acteService.getHistory(acteUuid).stream()
                .filter(ah -> ah.getStatus().equals(status))
                .findFirst();
    }
}
