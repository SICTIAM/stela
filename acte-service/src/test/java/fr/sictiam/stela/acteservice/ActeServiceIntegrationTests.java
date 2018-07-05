package fr.sictiam.stela.acteservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.dao.ActeExportRepository;
import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.AdminRepository;
import fr.sictiam.stela.acteservice.dao.AttachmentRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.event.LocalAuthorityEvent;
import fr.sictiam.stela.acteservice.model.ui.DraftUI;
import fr.sictiam.stela.acteservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.AdminService;
import fr.sictiam.stela.acteservice.service.ArchiverService;
import fr.sictiam.stela.acteservice.service.DraftService;
import fr.sictiam.stela.acteservice.service.ExternalRestService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.LocalesService;
import fr.sictiam.stela.acteservice.service.NotificationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActeServiceIntegrationTests extends BaseIntegrationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeServiceIntegrationTests.class);

    @Value("${application.jwt.secret}")
    String SECRET;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ActeService acteService;

    @Autowired
    private DraftService draftService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private LocalAuthorityService localAuthorityService;

    @Autowired
    private ActeRepository acteRepository;

    @Autowired
    private ActeHistoryRepository acteHistoryRepository;

    @Autowired
    private ActeExportRepository acteExportRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LocalesService localService;

    @Autowired
    private ExternalRestService externalRestService;

    @Autowired
    private ArchiverService archiverService;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);

    @Before
    public void beforeTests() {

        createAdmin();
        createLocalAuthority();
        acteRepository.deleteAll();

    }

    public void createAdmin() {
        adminRepository.deleteAll();
        adminService.create(new Admin("7afb264b-759c-49af-a564-0d4851b1e6a8", "stelasictiam.test@gmail.com", null, true,
                LocalDateTime.now(), LocalDateTime.now(), false, ""));
    }

    public void createLocalAuthority() {
        if (!localAuthorityService.getByName("SICTIAM TEST").isPresent()) {
            LocalAuthority localAuthority = new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1", "SICTIAM TEST",
                    "214400152", "044", "1", "31", true, true);
            try {
                MultipartFile codesMatieresFile = getMultipartResourceFile("data/exemple_codes_matieres.xml",
                        "application/xml");
                localAuthority.setNomenclatureDate(LocalDate.of(2001, 1, 1));
                localAuthority.setNomenclatureFile(codesMatieresFile.getBytes());
            } catch (IOException e) {
                LOGGER.error("Unable to add codes matieres file for {} : {}", localAuthority.getName(), e.toString());
            }
            LocalAuthority localAuthorityCreated = localAuthorityService.createOrUpdate(localAuthority);
            localAuthorityService.loadClassification(localAuthorityCreated.getUuid());

            localAuthorityService.createOrUpdate(localAuthorityCreated);

            String profile1 = "{" + "\"uuid\":\"4f146466-ea58-4e5c-851c-46db18ac173b\","
                    + "\"localAuthorityNotifications\":[\"ACTES\"]," + "\"localAuthority\":{" + "\"uuid\":\""
                    + localAuthority.getUuid() + "\"," + "\"name\":\"SICTIAM TEST\"," + "\"siren\":\"214400152\","
                    + "\"activatedModules\":[\"ACTES\"]" + "}," + "\"agent\":{"
                    + "\"uuid\":\"158087ee-0a32-4acb-b521-8c0ed56ee43d\","
                    + "\"sub\":\"5854b8b6-befd-4e6f-bf3d-8e35a9a5be00\"," + "\"email\":\"john.doe@sictiam.com\","
                    + "\"admin\":true," + "\"family_name\":\"Doe\"," + "\"given_name\":\"John\"" + "},"
                    + "\"email\":\"john.doe@sictiam.com\"," + "\"admin\":true," + "\"notificationValues\":[" + "{"
                    + "\"name\":\"ACTE_SENT\"," + "\"active\":true" + "}," + "{" + "\"name\":\"ACTE_CANCELLED\","
                    + "\"active\":true" + "}" + "]," + "\"groups\":[" + "{"
                    + "\"uuid\":\"d6e6c438-8fc9-4146-9e42-b7f7d8ccb98c\"," + "\"name\":\"aa\","
                    + "\"rights\":[\"ACTES_DEPOSIT\", \"ACTES_DEPOSIT\"]" + "}" + "]" + "}";

            String profile2 = "{" + "\"uuid\":\"4f146466-ea58-4e5c-851c-46db18ac887b\","
                    + "\"localAuthorityNotifications\":[\"ACTES\"]," + "\"localAuthority\":{" + "\"uuid\":\""
                    + localAuthority.getUuid() + "\"," + "\"name\":\"SICTIAM TEST\"," + "\"siren\":\"214400152\","
                    + "\"activatedModules\":[\"ACTES\"]" + "}," + "\"agent\":{"
                    + "\"uuid\":\"442087ee-0a32-4acb-b521-8c0ed56ee43d\","
                    + "\"sub\":\"4424b8b6-befd-4e6f-bf3d-8e35a9a5be00\"," + "\"email\":\"Laurent.Rojmeko@gmail.com\","
                    + "\"admin\":true," + "\"family_name\":\"De Rojmeko\"," + "\"given_name\":\"Laurent\"" + "},"
                    + "\"email\":\"Laurent.Rojmeko@sictiam.com\"," + "\"admin\":true," + "\"notificationValues\":["
                    + "{" + "\"name\":\"ACTE_SENT\"," + "\"active\":true" + "}," + "{" + "\"name\":\"ACTE_CANCELLED\","
                    + "\"active\":true" + "}" + "],"
                    + "\"groups\":[{\"uuid\":\"d6e6c438-8fc9-4146-9e42-b7f7d8ccb98c\",\"name\":\"aa\"}]" + "}";
            String profilesJson = "[" + profile1 + "," + profile2 + "]";
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.readTree(profile1);
                JsonNode profilesNode = objectMapper.readTree(profilesJson);
                Mockito.when(externalRestService.getProfile("4f146466-ea58-4e5c-851c-46db18ac173b")).thenReturn(node);
                Mockito.when(externalRestService.getProfiles(localAuthority.getUuid())).thenReturn(profilesNode);

            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            this.restTemplate.getRestTemplate()
                    .setInterceptors(Collections.singletonList((request1, body, execution) -> {

                        String jwtToken = Jwts.builder().setSubject(profile1)
                                .setExpiration(new Date(System.currentTimeMillis() + 500000))
                                .signWith(SignatureAlgorithm.HS512, SECRET).compact();

                        request1.getHeaders().add("STELA-Active-Token", jwtToken);

                        return execution.execute(request1, body);
                    }));

        }
    }

    @Test
    public void testCreateActe() {
        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response = this.restTemplate.exchange("/api/acte", HttpMethod.POST, request,
                String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(response.getBody(), notNullValue());

        String acteUuid = response.getBody();
        Acte acte = acteService.getByUuid(acteUuid);

        assertThat(acte, notNullValue());
        assertThat(acte.getActeAttachment(), notNullValue());
        assertThat(acte.getNumber(), notNullValue());
        assertThat(acte.getActeAttachment().getFilename(), is("Delib.pdf"));
        assertThat(acte.getCode(), is("1-1-1-0-0"));
        assertThat(acte.getObjet(), is("Objet"));
        assertThat(acte.getDecision(), is(LocalDate.now()));
        assertThat(acte.isPublic(), is(true));
        assertThat(acteService.getAnnexes(acteUuid), hasSize(2));
    }

    @Test
    public void testGetAll() {
        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        MockActeEventListener mockActeEventListener = new MockActeEventListener(StatusType.ARCHIVE_CREATED);
        this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);

        try {
            synchronized (mockActeEventListener) {
                mockActeEventListener.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        SearchResultsUI searchResultsUIs = this.restTemplate.getForObject("/api/acte", SearchResultsUI.class);
        assertThat(searchResultsUIs.getResults(), not(empty()));
    }

    @Test
    public void testArchiveCreation() {

        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response = this.restTemplate.exchange("/api/acte", HttpMethod.POST, request,
                String.class);
        String acteUuid = response.getBody();

        MockActeEventListener mockActeEventListener = new MockActeEventListener(StatusType.NOTIFICATION_SENT);
        try {
            synchronized (mockActeEventListener) {
                mockActeEventListener.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        List<ActeHistory> acteHistories = acteHistoryRepository.findByacteUuidOrderByDate(acteUuid);

        assertThat(acteHistories, hasSize(7));
        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.ARCHIVE_CREATED))));

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteHistories, StatusType.ARCHIVE_CREATED);
        assertThat(acteHistory.get().getFile(), notNullValue());
        assertThat(acteHistory.get().getFileName(), notNullValue());

        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.CREATED))));
        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.ANTIVIRUS_OK))));
        assertThat(acteHistories,
                hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.ARCHIVE_SIZE_CHECKED))));
        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.SENT))));
        assertThat(acteHistories.get(6).getStatus(), is(StatusType.NOTIFICATION_SENT));
        // uncomment to see the generated archive
        // printXmlMessage(acteHistory.get().getActeAttachment(),
        // acteHistory.get().getFileName());
    }

    @Test
    public void testArchiveTooLarge() {

        MultiValueMap<String, Object> params = acteWithAttachments();
        // add another annexe to go above the 1MB limit set for the unit tests
        params.add("annexes", new ClassPathResource("data/Annexe_delib.pdf"));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response = this.restTemplate.exchange("/api/acte", HttpMethod.POST, request,
                String.class);
        String acteUuid = response.getBody();

        MockActeEventListener mockActeEventListener = new MockActeEventListener(StatusType.ARCHIVE_TOO_LARGE);
        try {
            synchronized (mockActeEventListener) {
                mockActeEventListener.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.ARCHIVE_TOO_LARGE);
        assertThat(acteHistory.isPresent(), is(true));
    }

    @Test
    public void testCancellation() {
        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response = this.restTemplate.exchange("/api/acte", HttpMethod.POST, request,
                String.class);
        String acteUuid = response.getBody();

        MockActeEventListener mockActeEventListener = new MockActeEventListener(StatusType.NOTIFICATION_SENT);
        try {
            synchronized (mockActeEventListener) {
                mockActeEventListener.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        // manually add a ACK_RECEIVED history trace to allow for cancellation
        ActeHistory cancelAskedHistory = new ActeHistory(acteUuid, StatusType.ACK_RECEIVED);
        Acte acte = acteService.getByUuid(acteUuid);
        acte.getActeHistories().add(cancelAskedHistory);
        acteRepository.save(acte);

        this.restTemplate.postForEntity("/api/acte/{uuid}/status/cancel", null, null, acteUuid);

        MockActeEventListener mockActeEventListener2 = new MockActeEventListener(StatusType.NOTIFICATION_SENT);
        try {
            synchronized (mockActeEventListener2) {
                mockActeEventListener2.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        acte = acteService.getByUuid(acteUuid);
        assertThat(acte.getActeHistories().last().getStatus(), is(StatusType.NOTIFICATION_SENT));

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.CANCELLATION_ARCHIVE_CREATED);
        assertThat(acteHistory.isPresent(), is(true));
        assertThat(acteHistory.get().getFile(), notNullValue());
        assertThat(acteHistory.get().getFileName(), notNullValue());

        ResponseEntity<String> newResponse = this.restTemplate.postForEntity("/api/acte/{uuid}/status/cancel", null,
                null, acteUuid);

        assertThat(newResponse.getStatusCode(), is(HttpStatus.FORBIDDEN));

        // uncomment to see the generated archive
        // printXmlMessage(acteHistory.get().getActeAttachment(),
        // acteHistory.get().getFileName());
    }

    @Test
    public void handleLocalAuthoriyEvent() throws IOException {

        LocalAuthority localAuthority = new LocalAuthority("d4055204-ce91-48a5-bb53-458bd543bc5a", "New-Test", "siren",
                true);

        LocalAuthorityEvent localAuthorityEvent = new LocalAuthorityEvent(localAuthority);
        localAuthorityEvent.setActivatedModules(Collections.singleton("ACTES"));
        localAuthorityService.handleEvent(localAuthorityEvent);
        assertThat(localAuthorityService.getByUuid(localAuthority.getUuid()).getName(), is("New-Test"));
        assertThat(localAuthority.getUuid(), is(localAuthorityService.getByUuid(localAuthority.getUuid()).getUuid()));

    }

    @Test
    public void partialLocalAuthorityUpdate() {
        LocalAuthority localAuthority = new LocalAuthority("f11551be-e83b-47cb-b431-b57704bd7fad", "Patch-Test",
                "214400152", "999", "1", "31");
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String input = "{\"canPublishRegistre\":\"true\", \"department\":\"006\", \"district\":\"1\", \"nature\":\"29\"}";
        HttpEntity<String> patchData = new HttpEntity<>(input, headers);

        this.restTemplate.patchForObject("/api/acte/localAuthority/{uuid}", patchData, String.class,
                localAuthority.getUuid());

        localAuthority = localAuthorityService.getByUuid(localAuthority.getUuid());

        assertThat(localAuthority.getCanPublishRegistre(), is(true));
        assertThat(localAuthority.getCanPublishWebSite(), is(false));
        assertThat(localAuthority.getDepartment(), is("006"));
        assertThat(localAuthority.getNature(), is("29"));

        localAuthorityService.delete(localAuthority);

    }

    @Test
    public void parseCodesMatieres() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();
        List<MaterialCode> codesMatieres = localAuthorityService.getCodesMatieres(localAuthority.getUuid());

        assertThat(codesMatieres, hasSize(237));
        assertThat(codesMatieres, hasItem(Matchers.<MaterialCode>hasProperty("code", is("1-1-1-0-0"))));
        assertThat(codesMatieres, hasItem(Matchers.<MaterialCode>hasProperty("label",
                is("Commande Publique / Marchés publics / marchés sur appel d'offres"))));
    }

    @Test
    public void createRetrieveAndLeaveDraft() {
        assertThat(draftService.getDraftUIs(), empty());
        assertThat(draftService.getActeDrafts(), empty());

        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();

        Acte acte = draftService.newDraft(localAuthority, ActeMode.ACTE);
        DraftUI draft = draftService.getDraftUIs().get(0);
        assertThat(draft.getUuid(), is(acte.getDraft().getUuid()));

        acte.setObjet("Object draft");
        acte = draftService.saveActeDraft(acte, localAuthority);
        assertThat("Object draft", is(acte.getObjet()));

        acte.setObjet("");
        draftService.leaveActeDraft(acte, localAuthority);

        assertThat(draftService.getDraftUIs(), empty());
        assertThat(draftService.getActeDrafts(), empty());
    }

    @Test
    public void sendDraft() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();
        Acte acte = draftService.newDraft(localAuthority, ActeMode.ACTE);
        draftService.leaveActeDraft(setActeValues(acte), localAuthority);

        List<DraftUI> draftUIs = draftService.getDraftUIs();
        assertThat(draftUIs, hasSize(1));
        assertThat(draftUIs.get(0).getActes(), hasSize(1));
        assertThat(acte.getUuid(), is(draftUIs.get(0).getActes().get(0).getUuid()));
        assertThat(draftService.getActeDraftByUuid(acte.getUuid()), notNullValue());

        acte = draftService.getActeDraftByUuid(acte.getUuid());
        draftService.submitActeDraft(acte);
        assertThat(draftService.getDraftUIs(), empty());
        assertThat(acteService.getByUuid(acte.getUuid()), notNullValue());
    }

    @Test
    public void deleteDrafts() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();

        Acte acte1 = draftService.newDraft(localAuthority, ActeMode.ACTE);
        Acte acte2 = draftService.newDraft(localAuthority, ActeMode.ACTE);
        Acte acte3 = draftService.newDraft(localAuthority, ActeMode.ACTE);
        draftService.leaveActeDraft(setActeValues(acte1), localAuthority);
        draftService.leaveActeDraft(setActeValues(acte2), localAuthority);
        draftService.leaveActeDraft(setActeValues(acte3), localAuthority);

        assertEquals(3, draftService.getDraftUIs().size());
        assertThat(draftService.getDraftUIs(), hasSize(3));
        draftService.deleteDrafts(Collections.singletonList(acte1.getDraft().getUuid()));
        assertThat(draftService.getDraftUIs(), hasSize(2));
        draftService.deleteActeDraftByUuid(acte2.getUuid());
        assertThat(draftService.getDraftUIs(), hasSize(1));
        draftService.deleteDrafts(Collections.emptyList());
        assertThat(draftService.getDraftUIs(), empty());
    }

    @Test
    public void draftFiles() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();
        Acte acte = draftService.newDraft(localAuthority, ActeMode.ACTE);
        draftService.leaveActeDraft(setActeValues(acte), localAuthority);

        try {
            MultipartFile file = getMultipartResourceFile("data/Delib.pdf", "application/pdf");
            draftService.saveActeDraftFile(acte.getUuid(), file, localAuthority);
        } catch (IOException e) {
            LOGGER.error("Unable to add a file to the draft: {}", e.toString());
        }
        assertThat(draftService.getActeDraftByUuid(acte.getUuid()).getActeAttachment().getFilename(), is("Delib.pdf"));

        draftService.deleteActeDraftFile(acte.getUuid());
        assertThat(draftService.getActeDraftByUuid(acte.getUuid()).getActeAttachment(), nullValue());

        try {
            MultipartFile file = getMultipartResourceFile("data/Annexe_delib.pdf", "application/pdf");
            draftService.saveActeDraftAnnexe(acte.getUuid(), file, localAuthority);
            draftService.saveActeDraftAnnexe(acte.getUuid(), file, localAuthority);
        } catch (IOException e) {
            LOGGER.error("Unable to add a file to the draft: {}", e.toString());
        }
        assertThat(draftService.getActeDraftByUuid(acte.getUuid()).getAnnexes(), hasSize(2));

        Attachment annexe = draftService.getActeDraftByUuid(acte.getUuid()).getAnnexes().get(0);
        assertThat(annexe.getFilename(), is("Annexe_delib.pdf"));

        draftService.deleteActeDraftAnnexe(acte.getUuid(), annexe.getUuid());
        assertThat(draftService.getActeDraftByUuid(acte.getUuid()).getAnnexes(), hasSize(1));

        // cleanup our local production
        draftService.deleteDrafts(Collections.emptyList());
    }

    @Test
    public void batchedActes() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();

        DraftUI draft = draftService.newBatchedDraft(localAuthority);
        assertThat(draftService.getDraftByUuid(draft.getUuid()), notNullValue());
        assertThat(draft.getActes(), hasSize(1));
        assertThat(draftService.getActeDraftByUuid(draft.getActes().get(0).getUuid()), notNullValue());

        draftService.leaveActeDraft(draftService.getActeDraftByUuid(draft.getActes().get(0).getUuid()), localAuthority);
        assertThat(draftService.getDraftUIs(), empty());
        assertThat(draftService.getActeDrafts(), empty());

        draft = draftService.newBatchedDraft(localAuthority);
        draftService.newActeForDraft(draft.getUuid(), localAuthority);
        draft = draftService.getDraftActesUI(draft.getUuid());
        assertThat(draft.getActes(), hasSize(2));

        Acte acte1 = draftService.getActeDraftByUuid(draft.getActes().get(0).getUuid());
        Acte acte2 = draftService.getActeDraftByUuid(draft.getActes().get(1).getUuid());
        draftService.saveActeDraft(setActeValues(acte1), localAuthority);
        draftService.saveActeDraft(setActeValues(acte2), localAuthority);

        draftService.sumitDraft(draft.getUuid(), "4f146466-ea58-4e5c-851c-46db18ac173b");
        assertThat(draftService.getDraftUIs(), empty());
        assertThat(draftService.getActeDrafts(), empty());
        assertThat(acteRepository.findAll(), hasSize(2));
    }

    @Test
    public void testNotification() throws Exception {

        StatusType statusType = StatusType.SENT;
        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", "John");
        variables.put("lastname", "Doe");

        Map<String, String> variables2 = new HashMap<>();
        variables2.put("firstname", "Laurent");
        variables2.put("lastname", "De Rojmeko");

        String bodyCopy = localService.getMessage("fr", "acte_notification",
                "$.acte.copy." + statusType.name() + ".body", variables2);
        String subjectCopy = localService.getMessage("fr", "acte_notification",
                "$.acte.copy." + statusType.name() + ".subject", variables2);

        String body = localService.getMessage("fr", "acte_notification", "$.acte." + statusType.name() + ".body",
                variables);
        String subject = localService.getMessage("fr", "acte_notification", "$.acte." + statusType.name() + ".subject",
                variables);

        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response = this.restTemplate.exchange("/api/acte", HttpMethod.POST, request,
                String.class);
        String acteUuid = response.getBody();

        ActeHistory history = new ActeHistory(acteUuid, StatusType.SENT);
        ActeHistoryEvent mockEvent = new ActeHistoryEvent(this, history);
        notificationService.proccessEvent(mockEvent);

        MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        assertThat(receivedMessages, not(emptyArray()));
        assertThat(receivedMessages.length, is(2));
        MimeMessage current = receivedMessages[0];
        assertThat(current, notNullValue());
        MimeMessageParser parser = new MimeMessageParser(current);
        parser.parse();
        assertThat(parser.getSubject(), is(subjectCopy));
        assertThat(current.getContent(), instanceOf(MimeMultipart.class));
        assertThat(parser.getHtmlContent(), is(bodyCopy));

        MimeMessage secondMsg = receivedMessages[1];
        assertThat(secondMsg, notNullValue());
        MimeMessageParser secondParser = new MimeMessageParser(secondMsg);
        secondParser.parse();
        assertThat(secondParser.getSubject(), is(subject));
        assertThat(secondMsg.getContent(), instanceOf(MimeMultipart.class));
        assertThat(secondParser.getHtmlContent(), is(body));
    }

    @Test
    public void testSend() throws Exception {
        InputStream in = new ClassPathResource("data/SIC-EACT--210600730--20180115-1.tar.gz").getInputStream();

        byte[] targetArray = new byte[in.available()];
        in.read(targetArray);

        HttpStatus status = acteService.send(targetArray, "SIC-EACT--210600730--20180115-1.tar.gz");

        assertThat(status, is(HttpStatus.OK));
    }

    @Test
    public void testAttachmentType() throws Exception {
        Set<AttachmentType> attachmentTypes = localAuthorityService.getAttachmentTypeAvailable(ActeNature.AUTRES,
                "639fd48c-93b9-4569-a414-3b372c71e0a1", "1-1-0-0-0");
        assertThat(attachmentTypes, hasSize(2));
        assertThat(attachmentTypes, hasItem(Matchers.<AttachmentType>hasProperty("code", is("99_SE"))));
    }

    @Test
    public void askNomenclature() throws Exception {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();
        assertThat(acteService.askNomenclature(localAuthority, false), is(HttpStatus.OK));

    }

    @Test
    public void isMiatAccessibleTest() {
        assertThat(adminService.isMiatAvailable(), is(true));

        Admin admin = adminService.getAdmin();
        admin.setMiatAvailable(false);
        adminService.updateAdmin(admin);
        assertThat(adminService.isMiatAvailable(), is(false));

        admin.setMiatAvailable(true);
        admin.setUnavailabilityMiatStartDate(LocalDateTime.now().minusDays(1));
        admin.setUnavailabilityMiatEndDate(LocalDateTime.now().plusDays(1));
        adminService.updateAdmin(admin);
        assertThat(adminService.isMiatAvailable(), is(false));
    }

    @Test
    public void sendActeToPastell() throws IOException {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();
        Acte acte = new Acte();
        acte = setActeValues(acte);
        acte.setCreation(LocalDateTime.now());
        acte.setLocalAuthority(localAuthority);
        acte.setActeHistories(Collections.emptySortedSet());
        acte = acteRepository.save(acte);

        SortedSet<ActeHistory> acteHistories = new TreeSet<>();
        acteHistories.add(new ActeHistory(acte.getUuid(), StatusType.SENT,
                LocalDateTime.now(), null, Flux.TRANSMISSION_ACTE));
        MultipartFile xmlFile = getMultipartResourceFile("data/006-210600235-20180522-684-AI-1-2_5279.xml",
                "application/xml");
        acteHistories.add(new ActeHistory(acte.getUuid(), StatusType.ACK_RECEIVED,
                LocalDateTime.now(), xmlFile.getBytes(), "ACK.xml"));
        acte.setActeHistories(acteHistories);
        acte = acteRepository.save(acte);

        ArchiveSettings archiveSettings =
                new ArchiveSettings(true, "https://pastell.partenaires.libriciel.fr", "13", "stela", "stela05", 2);
        archiverService.archiveActe(acte, archiveSettings);
        acte = acteService.getByUuid(acte.getUuid());

        assertThat(acte.getArchive(), notNullValue());
        assertThat(acte.getArchive().getStatus(), is(ArchiveStatus.SENT));
        assertThat(acte.getActeHistories().last().getStatus(), is(StatusType.SENT_TO_SAE));
    }

    @Test
    public void deleteActeFilesTest() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM TEST").get();
        Acte acte = new Acte();
        acte = setActeValues(acte);
        acte.setCreation(LocalDateTime.now());
        acte.setLocalAuthority(localAuthority);
        acte.setActeHistories(Collections.emptySortedSet());
        acte = acteRepository.save(acte);

        String acteAttachmentUuid = acte.getActeAttachment().getUuid();
        String annexeUuid = acte.getAnnexes().get(0).getUuid();

        assertThat(acteService.getByUuid(acte.getUuid()).getActeAttachment(), notNullValue());
        assertThat(acteService.getByUuid(acte.getUuid()).getAnnexes(), not(empty()));
        assertThat(attachmentRepository.findByUuid(acteAttachmentUuid).isPresent(), is(true));
        assertThat(attachmentRepository.findByUuid(annexeUuid).isPresent(), is(true));

        archiverService.deleteActeFiles(acte);
        assertThat(acteService.getByUuid(acte.getUuid()).getActeAttachment(), nullValue());
        assertThat(acteService.getByUuid(acte.getUuid()).getAnnexes(), empty());
        assertThat(attachmentRepository.findByUuid(acteAttachmentUuid).isPresent(), is(false));
        assertThat(attachmentRepository.findByUuid(annexeUuid).isPresent(), is(false));
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
        Acte acte = new Acte(RandomStringUtils.randomAlphabetic(15), LocalDate.now(), ActeNature.ARRETES_INDIVIDUELS,
                "1-1-1-0-0", "Objet", true, true);
        acte.setProfileUuid("4f146466-ea58-4e5c-851c-46db18ac173b");
        return acte;
    }

    private Acte setActeValues(Acte acte) {
        acte.setNumber(RandomStringUtils.randomAlphabetic(15).toUpperCase());
        acte.setDecision(LocalDate.now());
        acte.setNature(ActeNature.ARRETES_INDIVIDUELS);
        acte.setCode("1-1-1-0-0");
        acte.setObjet("Objet");
        acte.setPublic(true);
        acte.setPublicWebsite(true);
        acte.setProfileUuid("4f146466-ea58-4e5c-851c-46db18ac173b");
        try {
            MultipartFile multipartFile = getMultipartResourceFile("data/Delib.pdf", "application/pdf");
            Attachment attachment = new Attachment(multipartFile.getBytes(), multipartFile.getOriginalFilename(),
                    multipartFile.getSize());
            acte.setActeAttachment(attachment);
            acte.setAnnexes(Collections.singletonList(attachment));
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
        return getActeHistoryForStatus(acteHistoryRepository.findByacteUuidOrderByDate(acteUuid), status);
    }

    private Optional<ActeHistory> getActeHistoryForStatus(List<ActeHistory> acte, StatusType status) {
        return acte.stream().filter(ah -> ah.getStatus().equals(status)).findFirst();
    }
}
