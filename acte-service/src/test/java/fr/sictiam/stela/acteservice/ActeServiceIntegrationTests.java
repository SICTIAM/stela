package fr.sictiam.stela.acteservice;


import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

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

    @Autowired
    private ActeRepository acteRepository;

    @Before
    public void createLocalAuthority() {
        if (!localAuthorityService.getByName("SICTIAM-Test").isPresent()) {
            LocalAuthority localAuthority =
                    new LocalAuthority("SICTIAM-Test", "999888777", "999", "1", "31", true, true);
            try {
                MultipartFile codesMatieresFile = getMultipartResourceFile("data/exemple_codes_matieres.xml", "application/xml");
                localAuthority.setNomenclatureDate(LocalDate.now());
                localAuthority.setNomenclatureFile(codesMatieresFile.getBytes());
            } catch (IOException e) {
                LOGGER.error("Unable to add codes matieres file for {} : {}", localAuthority.getName(), e.toString());
            }
            localAuthorityService.createOrUpdate(localAuthority);
        }
    }

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
        assertEquals("Delib.pdf", acte.getFile().getFilename());
        assertEquals("1-1-0-0-0", acte.getCode());
        assertEquals("Objet", acte.getObjet());
        assertEquals(LocalDate.now(), acte.getDecision());
        assertTrue(acte.isPublic());
        assertEquals(2, acteService.getAnnexes(acteUuid).size());
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
        assertNotNull(actes[0].getActeHistories().last().getStatus());
        assertNotNull(actes[0].getActeHistories().last().getDate());
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
        assertEquals(StatusType.ARCHIVE_SIZE_CHECKED, acte.getActeHistories().last().getStatus());

        SortedSet<ActeHistory> acteHistories = acte.getActeHistories();
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

        // manually add a ACK_RECEIVED history trace to allow for cancellation
        ActeHistory cancelAskedHistory = new ActeHistory(acteUuid, StatusType.ACK_RECEIVED);
        Acte acte = acteService.getByUuid(acteUuid);
        acte.getActeHistories().add(cancelAskedHistory);
        acteRepository.save(acte);

        this.restTemplate.postForEntity("/api/acte/{uuid}/status/cancel", null, null, acteUuid);

        try {
            // sleep some seconds to let async creation of the archive happens
            Thread.sleep(2000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        acte = acteService.getByUuid(acteUuid);
        assertEquals(StatusType.ARCHIVE_SIZE_CHECKED, acte.getActeHistories().last().getStatus());

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.CANCELLATION_ARCHIVE_CREATED);
        assertTrue(acteHistory.isPresent());
        assertNotNull(acteHistory.get().getFile());
        assertNotNull(acteHistory.get().getFileName());

        ResponseEntity<String> newResponse = this.restTemplate.postForEntity("/api/acte/{uuid}/status/cancel", null, null, acteUuid);

        assertEquals(HttpStatus.FORBIDDEN, newResponse.getStatusCode());

        // uncomment to see the generated archive
        // printXmlMessage(acteHistory.get().getFile(), acteHistory.get().getFileName());
    }

    @Test
    public void createAndRetrieveLocalAuthority() {
        LocalAuthority localAuthority = new LocalAuthority("New-Test", "999888777", "999", "1", "31");
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);
        assertEquals("New-Test", localAuthorityService.getByUuid(localAuthority.getUuid()).getName());
        assertEquals(localAuthority.getUuid(), localAuthorityService.getByUuid(localAuthority.getUuid()).getUuid());

        // cleanup our local production
        localAuthorityService.delete(localAuthority);
    }

    @Test
    public void partialLocalAuthorityUpdate() {
        LocalAuthority localAuthority = new LocalAuthority("Patch-Test", "999888777", "999", "1", "31");
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String input = "{\"canPublishRegistre\":\"true\", \"department\":\"006\", \"district\":\"1\", \"nature\":\"29\"}";
        HttpEntity<String> patchData = new HttpEntity<>(input, headers);

        this.restTemplate.patchForObject("/api/acte/localAuthority/{uuid}", patchData, String.class, localAuthority.getUuid());

        localAuthority = localAuthorityService.getByUuid(localAuthority.getUuid());

        assertEquals(true, localAuthority.getCanPublishRegistre());
        assertEquals(false, localAuthority.getCanPublishWebSite());
        assertEquals("006", localAuthority.getDepartment());
        assertEquals("29", localAuthority.getNature());

        // cleanup our local production
        localAuthorityService.delete(localAuthority);
    }

    @Test
    public void parseCodesMatieres() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Map<String, String> codesMatieres = localAuthorityService.getCodesMatieres(localAuthority.getUuid());

        assertEquals(5, codesMatieres.size());
        assertTrue(codesMatieres.containsKey("1-1-0-0-0"));
        assertEquals("Marchés publics", codesMatieres.get("1-1-0-0-0"));
        assertEquals("1-1-0-0-0", codesMatieres.keySet().iterator().next());
    }

    @Test
    public void createRetrieveAndCloseDraft() {
        assertEquals(0, acteService.getDrafts().size());
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte acte = acteService.saveDraft(new Acte(), localAuthority);

        Acte draft = acteService.getDraftByUuid(acte.getUuid());
        assertEquals(acte.getUuid(), draft.getUuid());
        assertTrue(draft.isDraft());

        draft.setObjet("Object draft");
        acteService.closeDraft(draft, localAuthority);
        assertEquals(1, acteService.getDrafts().size());

        draft.setObjet("");
        acteService.closeDraft(draft, localAuthority);
        assertEquals(0, acteService.getDrafts().size());
    }

    @Test
    public void sendDraft() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte draft = acteService.saveDraft(acte(), localAuthority);

        acteService.closeDraft(draft, localAuthority);
        assertEquals(1, acteService.getDrafts().size());
        assertEquals(draft.getUuid(), acteService.getDraftByUuid(draft.getUuid()).getUuid());

        acteService.sendDraft(draft.getUuid());
        assertEquals(0, acteService.getDrafts().size());
        assertEquals(draft.getUuid(), acteService.getByUuid(draft.getUuid()).getUuid());
    }

    @Test
    public void deleteDrafts() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte draft1 = acteService.saveDraft(acte(), localAuthority);
        Acte draft2 = acteService.saveDraft(acte(), localAuthority);
        Acte draft3 = acteService.saveDraft(acte(), localAuthority);

        assertEquals(3, acteService.getDrafts().size());
        acteService.deleteDrafts(Collections.singletonList(draft1.getUuid()));
        assertEquals(2, acteService.getDrafts().size());
        acteService.deleteDrafts(Collections.emptyList());
        assertEquals(0, acteService.getDrafts().size());
    }

    @Test
    public void draftFiles() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte draft = acteService.saveDraft(acte(), localAuthority);

        try {
            MultipartFile file = getMultipartResourceFile("data/Delib.pdf", "application/pdf");
            acteService.saveDraftFile(draft.getUuid(), file, localAuthority);
        } catch (IOException e) {
            LOGGER.error("Unable to add a file to the draft: {}", e.toString());
        }
        assertEquals("Delib.pdf", acteService.getDraftByUuid(draft.getUuid()).getFile().getFilename());

        acteService.deleteDraftFile(draft.getUuid());
        assertNull(acteService.getDraftByUuid(draft.getUuid()).getFile());

        try {
            MultipartFile file = getMultipartResourceFile("data/Annexe_delib.pdf", "application/pdf");
            acteService.saveDraftAnnexe(draft.getUuid(), file, localAuthority);
            acteService.saveDraftAnnexe(draft.getUuid(), file, localAuthority);
        } catch (IOException e) {
            LOGGER.error("Unable to add a file to the draft: {}", e.toString());
        }
        assertEquals(2, acteService.getDraftByUuid(draft.getUuid()).getAnnexes().size());

        Attachment annexe = acteService.getDraftByUuid(draft.getUuid()).getAnnexes().get(0);
        assertEquals("Annexe_delib.pdf", annexe.getFilename());

        acteService.deleteDraftAnnexe(draft.getUuid(), annexe.getUuid());
        assertEquals(1, acteService.getDraftByUuid(draft.getUuid()).getAnnexes().size());

        // cleanup our local production
        acteService.deleteDrafts(Collections.emptyList());
    }

    private MultiValueMap<String, Object> acteWithAttachments() {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("acte", acte());
        params.add("file", new ClassPathResource("data/Delib.pdf"));
        params.add("annexes", new ClassPathResource("data/Annexe_delib.pdf"));
        params.add("annexes", new ClassPathResource("data/Annexe_delib.pdf"));
        return params;
    }

    private MultipartFile getMultipartResourceFile(String filename, String contentType) throws IOException {
        File file = new ClassPathResource(filename).getFile();

        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile(file.getName(), file.getName(), contentType, IOUtils.toByteArray(input));
    }

    private Acte acte() {
        return new Acte(RandomStringUtils.randomAlphabetic(15), LocalDate.now(), ActeNature.ARRETES_INDIVIDUELS, "1-1-0-0-0",
                "Objet", true, true);
    }

    private void printXmlMessage(byte[] file, String filename) {
        try {
            FileCopyUtils.copy(file, new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Optional<ActeHistory> getActeHistoryForStatus(String acteUuid, StatusType status) {
        Acte acte = acteService.getByUuid(acteUuid);
        return acte.getActeHistories().stream()
                .filter(ah -> ah.getStatus().equals(status))
                .findFirst();
    }
}
