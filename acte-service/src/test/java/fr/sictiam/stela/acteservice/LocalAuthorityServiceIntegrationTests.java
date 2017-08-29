package fr.sictiam.stela.acteservice;


import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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

@Category(LocalAuthorityServiceIntegrationTests.class)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocalAuthorityServiceIntegrationTests extends BaseIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LocalAuthorityService localAuthorityService;


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

        String newNomenclatureDate = LocalDateTime.now().toString();
        String input = "{\"canPublishRegistre\":\"true\", \"department\":\"006\"}";
        HttpEntity<String> patchData = new HttpEntity<>(input);

        this.restTemplate.patchForObject("/api/acte/localAuthority/{uuid}", patchData, String.class, localAuthority.getUuid());

        localAuthority = localAuthorityService.getByUuid(localAuthority.getUuid());

        assertEquals(true, localAuthority.getCanPublishRegistre());
        assertEquals(false, localAuthority.getCanPublishWebSite());
        assertEquals("006", localAuthority.getDepartment());
    }
}
