package fr.sictiam.stela.pesservice;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

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
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sictiam.stela.pesservice.dao.AdminRepository;
import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.dao.PesRetourRepository;
import fr.sictiam.stela.pesservice.model.Admin;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.PesRetour;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.scheduler.ReceiverTask;
import fr.sictiam.stela.pesservice.scheduler.RetryTask;
import fr.sictiam.stela.pesservice.scheduler.SenderTask;
import fr.sictiam.stela.pesservice.service.AdminService;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.LocalesService;
import fr.sictiam.stela.pesservice.service.NotificationService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PesServiceIntegrationTests extends BaseIntegrationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesServiceIntegrationTests.class);

    @Value("${application.jwt.secret}")
    String SECRET;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PesAllerService pesService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PesAllerRepository pesRepository;
    
    @Autowired
    private PesRetourRepository pesRetourRepository;

    @Autowired
    private PesHistoryRepository pesHistoryRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    LocalAuthorityService localAuthorityService;

    @Autowired
    private LocalesService localService;

    @Autowired
    private SenderTask senderTask;

    @Autowired
    private ReceiverTask receiverTask;
    
    @Autowired
    private RetryTask retryTask;

    @Autowired
    private ExternalRestService externalRestService;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);

    @Before
    public void beforeTests() {

        createAdmin();
        createLocalAuthority();
        pesRepository.deleteAll();
        pesRetourRepository.deleteAll();

    }

    public void createAdmin() {
        adminRepository.deleteAll();
        adminService.create(
                new Admin("7afb264b-759c-49af-a564-0d4851b1e6a8", true, LocalDateTime.now(), LocalDateTime.now()));
    }

    public void createLocalAuthority() {
        if (!localAuthorityService.getByName("SICTIAM-Test").isPresent()) {
            LocalAuthority localAuthority = new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1", "SICTIAM-Test",
                    "999888777", true);
            localAuthority.setServerCode("VHICE21");
            localAuthority.setSiret("20003531900017");
            localAuthorityService.createOrUpdate(localAuthority);

            String json = "{\"uuid\":\"4f146466-ea58-4e5c-851c-46db18ac173b\",\"localAuthority\":{\"uuid\":\""
                    + localAuthority.getUuid()
                    + "\",\"name\":\"SICTIAM-Test\",\"siren\":\"999888777\",\"activatedModules\":[\"ACTES\"]},\"agent\":{\"uuid\":\"158087ee-0a32-4acb-b521-8c0ed56ee43d\",\"sub\":\"5854b8b6-befd-4e6f-bf3d-8e35a9a5be00\",\"email\":\"john.doe@sictiam.com\",\"admin\":true,\"family_name\":\"Doe\",\"given_name\":\"John\"},\"admin\":true,\"groups\":[{\"uuid\":\"d6e6c438-8fc9-4146-9e42-b7f7d8ccb98c\",\"name\":\"aa\"}]}";

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.readTree(json);
                Mockito.when(externalRestService.getProfile("4f146466-ea58-4e5c-851c-46db18ac173b")).thenReturn(node);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            this.restTemplate.getRestTemplate()
                    .setInterceptors(Collections.singletonList((request1, body, execution) -> {

                        String jwtToken = Jwts.builder().setSubject(json)
                                .setExpiration(new Date(System.currentTimeMillis() + 500000))
                                .signWith(SignatureAlgorithm.HS512, SECRET).compact();

                        request1.getHeaders().add("STELA-Active-Token", jwtToken);

                        return execution.execute(request1, body);
                    }));

        }
    }

    @Test
    public void LocalAuthority() {
        Optional<LocalAuthority> localAuthority = localAuthorityService.getByName("SICTIAM-Test");

        assertThat(localAuthority.isPresent(), is(true));
        assertThat(localAuthority.get().getSiren(), is("999888777"));
    }

    @Test
    public void testSenderTask() throws IOException {
        PesAller pes = samplePesAller();

        pesService.updateStatus(pes.getUuid(), StatusType.CREATED);

        MockPesEventListener mockActeEventListener = new MockPesEventListener(StatusType.NOT_SENT);
        try {
            synchronized (mockActeEventListener) {
                mockActeEventListener.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
        List<PesHistory> pesHistories = pesHistoryRepository.findBypesUuidOrderByDate(pes.getUuid());

        assertThat(pesHistories, hasSize(2));
        assertThat(pesHistories, hasItem(Matchers.<PesHistory>hasProperty("status", is(StatusType.NOT_SENT))));
    }

    @Test
    public void sendTest()
            throws IOException, XPathExpressionException, JAXBException, SAXException, ParserConfigurationException {
        PesAller pes = samplePesAller();

        senderTask.send(pes);
    }

    @Test
    public void receiveTest() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {

        PesAller pes = samplePesAller();

        InputStream ackStream = new ClassPathResource("data/030004_180124163513-ACK-A2600191_A00DL4ZW_OK.xml")
                .getInputStream();
        receiverTask.readACK(ackStream, "030004_180124163513-ACK-A2600191_A00DL4ZW_OK.xml");

        MockPesEventListener mockPesEventListener = new MockPesEventListener(StatusType.NOTIFICATION_SENT);
        try {
            synchronized (mockPesEventListener) {
                mockPesEventListener.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }

        List<PesHistory> pesHistories = pesHistoryRepository.findBypesUuidOrderByDate(pes.getUuid());
        assertThat(pesHistories, hasSize(2));
        assertThat(pesHistories, hasItem(Matchers.<PesHistory>hasProperty("status", is(StatusType.ACK_RECEIVED))));
    }
    
    @Test
    public void receivePesRetourTest()
            throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {

        InputStream ackStream = new ClassPathResource(
                "data/PES2R_DEP_P_303_00_083110_20180124_20180124_20180125051547.xml").getInputStream();
        receiverTask.readPesRetour(ackStream, "PES2R_DEP_P_303_00_083110_20180124_20180124_20180125051547.xml");
        
        assertThat(pesRetourRepository.count(), is(1L));       
    }
    
    @Test
    public void retryTest() throws IOException {
        PesAller pes = samplePesAller();
        pesService.updateStatus(pes.getUuid(), StatusType.SENT);

        MockPesEventListener mockPesEventListener = new MockPesEventListener(StatusType.NOTIFICATION_SENT);
        try {
            synchronized (mockPesEventListener) {
                mockPesEventListener.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
        retryTask.resendBlockedFlux();
        
        MockPesEventListener mockPesEventListener2 = new MockPesEventListener(StatusType.RESENT);
        try {
            synchronized (mockPesEventListener2) {
                mockPesEventListener2.wait(4000);
            }
        } catch (Exception e) {
            fail("Should not have thrown an exception");
        }
        
        List<PesHistory> pesHistories = pesHistoryRepository.findBypesUuidOrderByDate(pes.getUuid());
        assertThat(pesHistories, hasSize(3));
        assertThat(pesHistories, hasItem(Matchers.<PesHistory>hasProperty("status", is(StatusType.RESENT))));
        assertThat(pesHistories, hasItem(Matchers.<PesHistory>hasProperty("status", is(StatusType.NOTIFICATION_SENT))));
                
    }
    
    @Test
    public void receivePesRetourUnknow()
            throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {

        InputStream ackStream = new ClassPathResource(
                "data/PES2R_DEP_P_unknow.xml").getInputStream();
        receiverTask.readPesRetour(ackStream, "PES2R_DEP_P_unknow.xml");
        
        assertThat(pesRetourRepository.count(), is(0L));       
    }

    private PesAller samplePesAller() throws IOException {
        Optional<LocalAuthority> localAuthority = localAuthorityService.getByName("SICTIAM-Test");
        PesAller pes = new PesAller();
        pes.setAttachmentOnly(false);
        pes.setComment("comment");
        pes.setCreation(LocalDateTime.now());
        pes.setGroupUuid("dd");
        pes.setLocalAuthority(localAuthority.get());
        pes.setProfileUuid("4f146466-ea58-4e5c-851c-46db18ac173b");

        InputStream in = new ClassPathResource("data/28000-2017-P-RN-22-1516807373820.xml").getInputStream();

        byte[] targetArray = new byte[in.available()];
        in.read(targetArray);

        Attachment pesSent = new Attachment(targetArray, "28000-2017-P-RN-22-1516807373820", in.available());
        pes.setAttachment(pesSent);
        pes = pesService.save(pes);
        return pes;
    }

    private Optional<PesHistory> getPesHistoryForStatus(String pesUuid, StatusType status) {
        return getPesHistoryForStatus(pesHistoryRepository.findBypesUuidOrderByDate(pesUuid), status);
    }

    private Optional<PesHistory> getPesHistoryForStatus(List<PesHistory> pesHistory, StatusType status) {
        return pesHistory.stream().filter(ah -> ah.getStatus().equals(status)).findFirst();
    }
}
