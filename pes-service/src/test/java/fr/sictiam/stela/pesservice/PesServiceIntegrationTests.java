package fr.sictiam.stela.pesservice;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.compress.utils.IOUtils;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sictiam.stela.pesservice.dao.AdminRepository;
import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.dao.PesRepository;
import fr.sictiam.stela.pesservice.model.Admin;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.AdminService;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.LocalesService;
import fr.sictiam.stela.pesservice.service.NotificationService;
import fr.sictiam.stela.pesservice.service.PesService;
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
    private PesService pesService;

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PesRepository pesRepository;    
    
    @Autowired
    private PesHistoryRepository pesHistoryRepository; 
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    LocalAuthorityService localAuthorityService;
    
    @Autowired
    private LocalesService localService;
    
    @Autowired
    private ExternalRestService externalRestService;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);
    
    @Before
    public void beforeTests() {
        
        createAdmin();
        createLocalAuthority();
        pesRepository.deleteAll();
 
    }
    
    public void createAdmin() {
        adminRepository.deleteAll();
        adminService.create(
                new Admin("7afb264b-759c-49af-a564-0d4851b1e6a8", true, LocalDateTime.now(), LocalDateTime.now()));
    }

    public void createLocalAuthority() {
        if (!localAuthorityService.getByName("SICTIAM-Test").isPresent()) {
            LocalAuthority localAuthority =
                    new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1", "SICTIAM-Test", "999888777", true);
            
            LocalAuthority localAuthorityCreated =localAuthorityService.createOrUpdate(localAuthority);
                    
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
       Optional<LocalAuthority> localAuthority= localAuthorityService.getByName("SICTIAM-Test");
       
       assertThat(localAuthority.isPresent(), is(true));
       assertThat(localAuthority.get().getSiren(), is("999888777"));
    }
    
    private MultipartFile getMultipartResourceFile(String filename, String contentType) throws IOException {
        File file = new ClassPathResource(filename).getFile();

        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile(file.getName(), file.getName(), contentType, IOUtils.toByteArray(input));
    }

    

    private void printXmlMessage(byte[] file, String filename) {
        try {
            FileCopyUtils.copy(file, new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Optional<PesHistory> getPesHistoryForStatus(String acteUuid, StatusType status) {
        return getPesHistoryForStatus(pesHistoryRepository.findBypesUuidOrderByDate(acteUuid), status);
    }
    
    private Optional<PesHistory> getPesHistoryForStatus(List<PesHistory> pesHistory, StatusType status) {
        return pesHistory.stream()
                .filter(ah -> ah.getStatus().equals(status))
                .findFirst();
    }
}
