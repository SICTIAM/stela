package fr.sictiam.stela.acteservice;


import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.ui.DraftUI;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.DraftService;
import fr.sictiam.stela.acteservice.service.AdminService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Ignore;
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
import java.util.*;

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
    private DraftService draftService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private LocalAuthorityService localAuthorityService;

    @Autowired
    private ActeRepository acteRepository;

    @Before
    public void beforeTests() {
        createAdmin();
        createLocalAuthority();
    }
    
    public void createAdmin() {
        adminService.create(new Admin("7afb264b-759c-49af-a564-0d4851b1e6a8", "dev@sictiam.fr", null));
    }

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
            LocalAuthority localAuthorityCreated =localAuthorityService.createOrUpdate(localAuthority);
            localAuthorityService.loadCodesMatieres(localAuthorityCreated.getUuid());
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
        assertNotNull(acte.getActeAttachment());
        assertNotNull(acte.getNumber());
        assertEquals("Delib.pdf", acte.getActeAttachment().getFilename());
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
            Thread.sleep(4000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.ARCHIVE_CREATED);
        assertTrue(acteHistory.isPresent());
        assertNotNull(acteHistory.get().getFile());
        assertNotNull(acteHistory.get().getFileName());

        Acte acte = acteService.getByUuid(acteUuid);
        assertEquals(StatusType.SENT, acte.getActeHistories().last().getStatus());

        SortedSet<ActeHistory> acteHistories = acte.getActeHistories();
        assertEquals(4, acteHistories.size());
        assertTrue(acteHistories.stream().anyMatch(acteHistory1 -> acteHistory1.getStatus().equals(StatusType.CREATED)));
        assertTrue(acteHistories.stream().anyMatch(acteHistory1 -> acteHistory1.getStatus().equals(StatusType.ARCHIVE_CREATED)));
        assertTrue(acteHistories.stream().anyMatch(acteHistory1 -> acteHistory1.getStatus().equals(StatusType.SENT)));

        // uncomment to see the generated archive
        // printXmlMessage(acteHistory.get().getActeAttachment(), acteHistory.get().getFileName());
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
            Thread.sleep(4000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        acte = acteService.getByUuid(acteUuid);
        assertEquals(StatusType.SENT, acte.getActeHistories().last().getStatus());

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.CANCELLATION_ARCHIVE_CREATED);
        assertTrue(acteHistory.isPresent());
        assertNotNull(acteHistory.get().getFile());
        assertNotNull(acteHistory.get().getFileName());

        ResponseEntity<String> newResponse = this.restTemplate.postForEntity("/api/acte/{uuid}/status/cancel", null, null, acteUuid);

        assertEquals(HttpStatus.FORBIDDEN, newResponse.getStatusCode());

        // uncomment to see the generated archive
        // printXmlMessage(acteHistory.get().getActeAttachment(), acteHistory.get().getFileName());
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
        List<MaterialCode> codesMatieres = localAuthorityService.getCodesMatieres(localAuthority.getUuid());

        assertEquals(5, codesMatieres.size());
        assertTrue(codesMatieres.stream().anyMatch(materialCode -> materialCode.getCode().equals("1-1-0-0-0")));
        assertTrue(codesMatieres.stream().anyMatch(materialCode -> materialCode.getLabel().equals("Commande Publique / Marchés publics")));
    }

    @Test
    public void createRetrieveAndLeaveDraft() {
        assertEquals(0, draftService.getDraftUIs().size());
        assertEquals(0, draftService.getActeDrafts().size());

        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();

        Acte acte = draftService.newDraft(localAuthority, ActeMode.ACTE);
        DraftUI draft = draftService.getDraftUIs().get(0);
        assertEquals(draft.getUuid(), acte.getDraft().getUuid());

        acte.setObjet("Object draft");
        acte = draftService.saveActeDraft(acte, localAuthority);
        assertEquals("Object draft", acte.getObjet());


        acte.setObjet("");
        draftService.leaveActeDraft(acte, localAuthority);

        assertEquals(0, draftService.getDraftUIs().size());
        assertEquals(0, draftService.getActeDrafts().size());
    }

    @Test
    public void sendDraft() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte acte = draftService.newDraft(localAuthority, ActeMode.ACTE);
        draftService.leaveActeDraft(setActeValues(acte), localAuthority);

        List<DraftUI> draftUIs = draftService.getDraftUIs();
        assertEquals(1, draftUIs.size());
        assertEquals(1, draftUIs.get(0).getActes().size());
        assertEquals(acte.getUuid(), draftUIs.get(0).getActes().get(0).getUuid());
        assertNotNull(draftService.getActeDraftByUuid(acte.getUuid()));

        acte = draftService.getActeDraftByUuid(acte.getUuid());
        draftService.submitActeDraft(acte);
        assertEquals(0, draftService.getDraftUIs().size());
        assertNotNull(acteService.getByUuid(acte.getUuid()));
    }

    @Test
    public void deleteDrafts() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();

        Acte acte1 = draftService.newDraft(localAuthority, ActeMode.ACTE);
        Acte acte2 = draftService.newDraft(localAuthority, ActeMode.ACTE);
        Acte acte3 = draftService.newDraft(localAuthority, ActeMode.ACTE);
        draftService.leaveActeDraft(setActeValues(acte1), localAuthority);
        draftService.leaveActeDraft(setActeValues(acte2), localAuthority);
        draftService.leaveActeDraft(setActeValues(acte3), localAuthority);

        assertEquals(3, draftService.getDraftUIs().size());
        draftService.deleteDrafts(Collections.singletonList(acte1.getDraft().getUuid()));
        assertEquals(2, draftService.getDraftUIs().size());
        draftService.deleteActeDraftByUuid(acte2.getUuid());
        assertEquals(1, draftService.getDraftUIs().size());
        draftService.deleteDrafts(Collections.emptyList());
        assertEquals(0, draftService.getDraftUIs().size());
    }

    @Test
    public void draftFiles() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte acte = draftService.newDraft(localAuthority, ActeMode.ACTE);
        draftService.leaveActeDraft(setActeValues(acte), localAuthority);

        try {
            MultipartFile file = getMultipartResourceFile("data/Delib.pdf", "application/pdf");
            draftService.saveActeDraftFile(acte.getUuid(), file, localAuthority);
        } catch (IOException e) {
            LOGGER.error("Unable to add a file to the draft: {}", e.toString());
        }
        assertEquals("Delib.pdf", draftService.getActeDraftByUuid(acte.getUuid()).getActeAttachment().getFilename());

        draftService.deleteActeDraftFile(acte.getUuid());
        assertNull(draftService.getActeDraftByUuid(acte.getUuid()).getActeAttachment());

        try {
            MultipartFile file = getMultipartResourceFile("data/Annexe_delib.pdf", "application/pdf");
            draftService.saveActeDraftAnnexe(acte.getUuid(), file, localAuthority);
            draftService.saveActeDraftAnnexe(acte.getUuid(), file, localAuthority);
        } catch (IOException e) {
            LOGGER.error("Unable to add a file to the draft: {}", e.toString());
        }
        assertEquals(2, draftService.getActeDraftByUuid(acte.getUuid()).getAnnexes().size());

        Attachment annexe = draftService.getActeDraftByUuid(acte.getUuid()).getAnnexes().get(0);
        assertEquals("Annexe_delib.pdf", annexe.getFilename());

        draftService.deleteActeDraftAnnexe(acte.getUuid(), annexe.getUuid());
        assertEquals(1, draftService.getActeDraftByUuid(acte.getUuid()).getAnnexes().size());

        // cleanup our local production
        draftService.deleteDrafts(Collections.emptyList());
    }

    @Test
    public void batchedActes() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();

        DraftUI draft = draftService.newBatchedDraft(localAuthority);
        assertNotNull(draftService.getDraftByUuid(draft.getUuid()));
        assertEquals(1, draft.getActes().size());
        assertNotNull(draftService.getActeDraftByUuid(draft.getActes().get(0).getUuid()));

        draftService.leaveActeDraft(draftService.getActeDraftByUuid(draft.getActes().get(0).getUuid()), localAuthority);
        assertEquals(0, draftService.getDraftUIs().size());
        assertEquals(0, draftService.getActeDrafts().size());

        draft = draftService.newBatchedDraft(localAuthority);
        draftService.newActeForDraft(draft.getUuid(), localAuthority);
        draft = draftService.getDraftActesUI(draft.getUuid());
        assertEquals(2, draft.getActes().size());

        Acte acte1 = draftService.getActeDraftByUuid(draft.getActes().get(0).getUuid());
        Acte acte2 = draftService.getActeDraftByUuid(draft.getActes().get(1).getUuid());
        draftService.saveActeDraft(setActeValues(acte1), localAuthority);
        draftService.saveActeDraft(setActeValues(acte2), localAuthority);

        draftService.sumitDraft(draft.getUuid());
        assertEquals(0, draftService.getDraftUIs().size());
        assertEquals(0, draftService.getActeDrafts().size());
        assertEquals(2, acteRepository.findAll().size());
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

    private Acte setActeValues(Acte acte) {
        acte.setNumber(RandomStringUtils.randomAlphabetic(15));
        acte.setDecision(LocalDate.now());
        acte.setNature(ActeNature.ARRETES_INDIVIDUELS);
        acte.setCode("1-1-0-0-0");
        acte.setObjet("Objet");
        acte.setPublic(true);
        acte.setPublicWebsite(true);
        try {
            MultipartFile multipartFile = getMultipartResourceFile("data/Delib.pdf", "application/pdf");
            Attachment attachment = new Attachment(multipartFile.getBytes(), multipartFile.getOriginalFilename(), multipartFile.getSize());
            acte.setActeAttachment(attachment);
        } catch (IOException e) {
            LOGGER.error("Error while trying to load an acteAttachment");
        }
        return acte;
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
