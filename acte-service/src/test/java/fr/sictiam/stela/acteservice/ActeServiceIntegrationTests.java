package fr.sictiam.stela.acteservice;


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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.ActeMode;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.Admin;
import fr.sictiam.stela.acteservice.model.Agent;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.model.Profile;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.WorkGroup;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.event.LocalAuthorityEvent;
import fr.sictiam.stela.acteservice.model.event.Module;
import fr.sictiam.stela.acteservice.model.ui.DraftUI;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.AdminService;
import fr.sictiam.stela.acteservice.service.DraftService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.LocalesService;
import fr.sictiam.stela.acteservice.service.NotificationService;


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
    
    @Autowired
    private ActeHistoryRepository acteHistoryRepository; 
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private LocalesService localService;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);


    @Before
    public void beforeTests() {
        
        createAdmin();
        createLocalAuthority();
        acteRepository.deleteAll();
 
    }
    
    public void createAdmin() {
        adminService.create(new Admin("7afb264b-759c-49af-a564-0d4851b1e6a8", "dev@sictiam.fr", null));
    }

    public void createLocalAuthority() {
        if (!localAuthorityService.getByName("SICTIAM-Test").isPresent()) {
            LocalAuthority localAuthority =
                    new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1", "SICTIAM-Test", "999888777", "999", "1", "31", true, true);
            try {
                MultipartFile codesMatieresFile = getMultipartResourceFile("data/exemple_codes_matieres.xml", "application/xml");
                localAuthority.setNomenclatureDate(LocalDate.now());
                localAuthority.setNomenclatureFile(codesMatieresFile.getBytes());
            } catch (IOException e) {
                LOGGER.error("Unable to add codes matieres file for {} : {}", localAuthority.getName(), e.toString());
            }
            LocalAuthority localAuthorityCreated =localAuthorityService.createOrUpdate(localAuthority);
            localAuthorityService.loadCodesMatieres(localAuthorityCreated.getUuid());
            
            WorkGroup workGroup =new WorkGroup("42a0076e-e941-4b5f-afe7-58cc293f2db4", localAuthorityCreated, "GlobalGroup");
            Set<WorkGroup> groups = new HashSet<>();
            groups.add(workGroup);
            localAuthorityCreated.setGroups(groups);
            
            Agent agent = new Agent("b0deb0ab-70d3-4b76-b14b-ab88cdaf7701","John", "Doe", "john.doe@fbi.fr");
            agent.setSub("4f146466-ea58-4e5c-851c-46db18ac173b");
            agent.setAdmin(false);
            
            
            Profile profile =new Profile("6f179af3-0b92-4383-9510-e9b24c91ae47", localAuthorityCreated, agent, false);
            Set<Profile> profiles = new HashSet<>();
            profiles.add(profile);
            agent.setProfiles(profiles);
            localAuthorityCreated.getGroups().add(workGroup);
            localAuthorityCreated.setProfiles(profiles);
            
            localAuthorityService.createOrUpdate(localAuthorityCreated);
            
            this.restTemplate.getRestTemplate().setInterceptors(
                    Collections.singletonList((request1, body, execution) -> {
                        request1.getHeaders()
                                .add("STELA-Sub", "4f146466-ea58-4e5c-851c-46db18ac173b");
                        request1.getHeaders()
                                .add("STELA-Current-Local-Authority-UUID", localAuthority.getUuid());
                        return execution.execute(request1, body);
                    }));
            
        }
    }
    
    @Test
    public void testCreateActe() {
        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);
        
        ResponseEntity<String> response =
                this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(response.getBody(), notNullValue());

        String acteUuid = response.getBody();
        Acte acte = acteService.getByUuid(acteUuid);

        assertThat(acte, notNullValue());
        assertThat(acte.getActeAttachment(), notNullValue());
        assertThat(acte.getNumber(), notNullValue());
        assertThat(acte.getActeAttachment().getFilename(), is("Delib.pdf"));
        assertThat(acte.getCode(), is("1-1-0-0-0"));
        assertThat(acte.getObjet(), is("Objet"));
        assertThat(acte.getDecision(), is(LocalDate.now()));
        assertThat(acte.isPublic(), is(true));
        assertThat(acteService.getAnnexes(acteUuid), hasSize(2));
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
        assertThat(actes, not(emptyArray()));
        assertThat(actes[0].getActeHistories().last().getStatus(), notNullValue());
        assertThat(actes[0].getActeHistories().last().getDate(), notNullValue());
    }

    @Test
    public void testArchiveCreation() {

        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);        
        
        ResponseEntity<String> response =
                this.restTemplate.exchange("/api/acte", HttpMethod.POST, request, String.class);
        String acteUuid = response.getBody();
        
        try {
            Thread.sleep(2000);           
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
  
        List<ActeHistory> acteHistories = acteHistoryRepository.findByacteUuidOrderByDate(acteUuid);
   
        assertThat(acteHistories, hasSize(6));
        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.ARCHIVE_CREATED))));
        
        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteHistories, StatusType.ARCHIVE_CREATED);
        assertThat(acteHistory.get().getFile(), notNullValue());
        assertThat(acteHistory.get().getFileName(), notNullValue());
        
        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.CREATED))));
        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.ANTIVIRUS_OK))));
        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.ARCHIVE_SIZE_CHECKED))));
        assertThat(acteHistories, hasItem(Matchers.<ActeHistory>hasProperty("status", is(StatusType.SENT))));
        assertThat(acteHistories.get(5).getStatus(), is(StatusType.NOTIFICATION_SENT));
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
        assertThat(acteHistory.isPresent(), is(true));
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
        assertThat(acte.getActeHistories().last().getStatus(), is(StatusType.NOTIFICATION_SENT));

        Optional<ActeHistory> acteHistory = getActeHistoryForStatus(acteUuid, StatusType.CANCELLATION_ARCHIVE_CREATED);
        assertThat(acteHistory.isPresent(), is(true));
        assertThat(acteHistory.get().getFile(), notNullValue());
        assertThat(acteHistory.get().getFileName(), notNullValue());

        ResponseEntity<String> newResponse = this.restTemplate.postForEntity("/api/acte/{uuid}/status/cancel", null, null, acteUuid);
        
        assertThat(newResponse.getStatusCode(), is(HttpStatus.FORBIDDEN));

        // uncomment to see the generated archive
        // printXmlMessage(acteHistory.get().getActeAttachment(), acteHistory.get().getFileName());
    }

    @Test
    public void handleLocalAuthoriyEvent() {
             
        Agent agent = new Agent("546ece6c-7bf9-4192-a73b-689193443d5d","John", "Doe", "john.doe@fbi.fr");
        agent.setSub("sub");
        agent.setAdmin(false);
        
        Profile profile = new Profile("be639bb7-1b20-452f-b67b-b706cfd6e5df", agent, false);
        Set<Profile> profiles = new HashSet<>();
        profiles.add(profile);
        
        WorkGroup workGroup = new WorkGroup("febb077f-c014-4f18-802f-8315726caa82", "GlobalGroup");
        workGroup.setProfiles(profiles);
        Set<WorkGroup> groups = new HashSet<>();
        groups.add(workGroup);
        
        LocalAuthority localAuthority = new LocalAuthority("d4055204-ce91-48a5-bb53-458bd543bc5a", "New-Test","siren", true,groups, profiles);
        
        LocalAuthorityEvent localAuthorityEvent =new LocalAuthorityEvent(localAuthority);
        localAuthorityEvent.setActivatedModules(Collections.singleton(Module.ACTES));
        localAuthorityService.handleEvent(localAuthorityEvent);
        assertThat(localAuthorityService.getByUuid(localAuthority.getUuid()).getName(), is("New-Test"));
        assertThat(localAuthority.getUuid(), is(localAuthorityService.getByUuid(localAuthority.getUuid()).getUuid()));    
        
    }

    @Test
    public void partialLocalAuthorityUpdate() {
        LocalAuthority localAuthority = new LocalAuthority("f11551be-e83b-47cb-b431-b57704bd7fad","Patch-Test", "999888777", "999", "1", "31");
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String input = "{\"canPublishRegistre\":\"true\", \"department\":\"006\", \"district\":\"1\", \"nature\":\"29\"}";
        HttpEntity<String> patchData = new HttpEntity<>(input, headers);

        this.restTemplate.patchForObject("/api/acte/localAuthority/{uuid}", patchData, String.class, localAuthority.getUuid());

        localAuthority = localAuthorityService.getByUuid(localAuthority.getUuid());

        assertThat(localAuthority.getCanPublishRegistre(), is(true));
        assertThat(localAuthority.getCanPublishWebSite(), is(false));
        assertThat(localAuthority.getDepartment(), is("006"));
        assertThat(localAuthority.getNature(), is("29"));
        
        localAuthorityService.delete(localAuthority);

    }

    @Test
    public void parseCodesMatieres() {
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        List<MaterialCode> codesMatieres = localAuthorityService.getCodesMatieres(localAuthority.getUuid());

        assertThat(codesMatieres, hasSize(5));
        assertThat(codesMatieres, hasItem(Matchers.<MaterialCode>hasProperty("code", is("1-1-0-0-0"))));
        assertThat(codesMatieres, hasItem(Matchers.<MaterialCode>hasProperty("label", is("Commande Publique / Marchés publics"))));
    }

    @Test
    public void createRetrieveAndLeaveDraft() {
        assertThat(draftService.getDraftUIs(), empty());
        assertThat(draftService.getActeDrafts(), empty());

        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();

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
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte acte = draftService.newDraft(localAuthority, ActeMode.ACTE);
        draftService.leaveActeDraft(setActeValues(acte), localAuthority);

        List<DraftUI> draftUIs = draftService.getDraftUIs();
        assertThat(draftUIs, hasSize(1));
        assertThat(draftUIs.get(0).getActes(), hasSize(1));
        assertThat(acte.getUuid(), is(draftUIs.get(0).getActes().get(0).getUuid()));
        assertThat(draftService.getActeDraftByUuid(acte.getUuid()),notNullValue());

        acte = draftService.getActeDraftByUuid(acte.getUuid());
        draftService.submitActeDraft(acte);
        assertThat(draftService.getDraftUIs(), empty());
        assertThat(acteService.getByUuid(acte.getUuid()),notNullValue());
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
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte acte = draftService.newDraft(localAuthority, ActeMode.ACTE);
        draftService.leaveActeDraft(setActeValues(acte), localAuthority);

        try {
            MultipartFile file = getMultipartResourceFile("data/Delib.pdf", "application/pdf");
            draftService.saveActeDraftFile(acte.getUuid(), file, localAuthority);
        } catch (IOException e) {
            LOGGER.error("Unable to add a file to the draft: {}", e.toString());
        }
        assertThat(draftService.getActeDraftByUuid(acte.getUuid()).getActeAttachment().getFilename(),is("Delib.pdf"));

        draftService.deleteActeDraftFile(acte.getUuid());
        assertThat(draftService.getActeDraftByUuid(acte.getUuid()).getActeAttachment(),nullValue());

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
        LocalAuthority localAuthority = localAuthorityService.getByName("SICTIAM-Test").get();

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

        draftService.sumitDraft(draft.getUuid());
        assertThat(draftService.getDraftUIs(), empty());
        assertThat(draftService.getActeDrafts(), empty());
        assertThat(acteRepository.findAll(), hasSize(2));
    }
    
    @Test
    public void testNotification() throws Exception {

        StatusType statusType = StatusType.SENT;
        String firstName = "John";
        String lastName = "Doe";
        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", firstName);
        variables.put("lastname", lastName);

        String body = localService.getMessage("fr", "acte_notification", "$.acte." + statusType.name() + ".body",
                variables);
        String subject = localService.getMessage("fr", "acte_notification", "$.acte." + statusType.name() + ".subject",
                variables);

        try {
            // sleep some seconds to let async creation of the archive happens
            Thread.sleep(2000);
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
        MultiValueMap<String, Object> params = acteWithAttachments();
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params);

        ResponseEntity<String> response = this.restTemplate.exchange("/api/acte", HttpMethod.POST, request,
                String.class);
        String acteUuid = response.getBody();

        ActeHistory history = new ActeHistory(acteUuid, StatusType.SENT);
        ActeHistoryEvent mockEvent = new ActeHistoryEvent(this, history);
        notificationService.sendMail(mockEvent);

        MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        assertThat(receivedMessages, not(emptyArray()));

        MimeMessage current = receivedMessages[0];
        assertThat(current, notNullValue());
        MimeMessageParser parser = new MimeMessageParser(current);
        parser.parse();
        assertThat(parser.getSubject(), is(subject));
        assertThat(current.getContent(), instanceOf(MimeMultipart.class));
        assertThat(parser.getHtmlContent(), is(body));
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
        return getActeHistoryForStatus(acteHistoryRepository.findByacteUuidOrderByDate(acteUuid), status);
    }
    
    private Optional<ActeHistory> getActeHistoryForStatus(List<ActeHistory> acte, StatusType status) {
        return acte.stream()
                .filter(ah -> ah.getStatus().equals(status))
                .findFirst();
    }
}
