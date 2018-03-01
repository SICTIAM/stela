package fr.sictiam.stela.convocationservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.convocationservice.dao.AdminRepository;
import fr.sictiam.stela.convocationservice.dao.ConvocationHistoryRepository;
import fr.sictiam.stela.convocationservice.model.Admin;
import fr.sictiam.stela.convocationservice.model.ConvocationHistory;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.StatusType;
import fr.sictiam.stela.convocationservice.service.AdminService;
import fr.sictiam.stela.convocationservice.service.ExternalRestService;
import fr.sictiam.stela.convocationservice.service.LocalAuthorityService;
import fr.sictiam.stela.convocationservice.service.LocalesService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PesServiceIntegrationTests extends BaseIntegrationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(PesServiceIntegrationTests.class);

	@Value("${application.jwt.secret}")
	String SECRET;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	LocalAuthorityService localAuthorityService;

	@Autowired
	private LocalesService localService;

	@Autowired
	private ConvocationHistoryRepository convocationHistoryRepository;

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private AdminService adminService;

	@Autowired
	private ExternalRestService externalRestService;

	@Rule
	public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);

	@Before
	public void beforeTests() {

		createAdmin();
		createLocalAuthority();

	}

	public void createAdmin() {
		adminRepository.deleteAll();
		adminService.create(new Admin("7afb264b-759c-49af-a564-0d4851b1e6a8", false, ""));
	}

	public void createLocalAuthority() {
		if (!localAuthorityService.getByName("SICTIAM-Test").isPresent()) {
			LocalAuthority localAuthority = new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1", "SICTIAM-Test",
					"999888777", true);
			localAuthorityService.createOrUpdate(localAuthority);

			String profile1 = "{" + "\"uuid\":\"4f146466-ea58-4e5c-851c-46db18ac173b\","
					+ "\"localAuthorityNotifications\":[\"PES\"]," + "\"localAuthority\":{" + "\"uuid\":\""
					+ localAuthority.getUuid() + "\"," + "\"name\":\"SICTIAM-Test\"," + "\"siren\":\"999888777\","
					+ "\"activatedModules\":[\"PES\"]" + "}," + "\"agent\":{"
					+ "\"uuid\":\"158087ee-0a32-4acb-b521-8c0ed56ee43d\","
					+ "\"sub\":\"5854b8b6-befd-4e6f-bf3d-8e35a9a5be00\"," + "\"email\":\"john.doe@sictiam.com\","
					+ "\"admin\":true," + "\"family_name\":\"Doe\"," + "\"given_name\":\"John\"" + "},"
					+ "\"email\":\"john.doe@sictiam.com\"," + "\"admin\":true," + "\"notificationValues\":[" + "{"
					+ "\"name\":\"PES_ACK_RECEIVED\"," + "\"active\":true" + "}," + "{" + "\"name\":\"PES_SENT\","
					+ "\"active\":true" + "}" + "]," + "\"groups\":[" + "{"
					+ "\"uuid\":\"d6e6c438-8fc9-4146-9e42-b7f7d8ccb98c\"," + "\"name\":\"aa\","
					+ "\"rights\":[\"PES_ADMIN\"]" + "}" + "]" + "}";

			String profile2 = "{" + "\"uuid\":\"4f146466-ea58-4e5c-851c-46db18ac887b\","
					+ "\"localAuthorityNotifications\":[\"PES\"]," + "\"localAuthority\":{" + "\"uuid\":\""
					+ localAuthority.getUuid() + "\"," + "\"name\":\"SICTIAM-Test\"," + "\"siren\":\"999888777\","
					+ "\"activatedModules\":[\"PES\"]" + "}," + "\"agent\":{"
					+ "\"uuid\":\"442087ee-0a32-4acb-b521-8c0ed56ee43d\","
					+ "\"sub\":\"4424b8b6-befd-4e6f-bf3d-8e35a9a5be00\"," + "\"email\":\"Laurent.Rojmeko@gmail.com\","
					+ "\"admin\":true," + "\"family_name\":\"De Rojmeko\"," + "\"given_name\":\"Laurent\"" + "},"
					+ "\"email\":\"Laurent.Rojmeko@sictiam.com\"," + "\"admin\":true," + "\"notificationValues\":["
					+ "{" + "\"name\":\"PES_ACK_RECEIVED\"," + "\"active\":true" + "}," + "{" + "\"name\":\"PES_SENT\","
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
	public void LocalAuthority() {
		Optional<LocalAuthority> localAuthority = localAuthorityService.getByName("SICTIAM-Test");

		assertThat(localAuthority.isPresent(), is(true));
		assertThat(localAuthority.get().getSiren(), is("999888777"));
	}

	private Optional<ConvocationHistory> getConvocationHistoryForStatus(String pesUuid, StatusType status) {
		return getConvocationHistoryForStatus(convocationHistoryRepository.findByconvocationUuidOrderByDate(pesUuid),
				status);
	}

	private Optional<ConvocationHistory> getConvocationHistoryForStatus(List<ConvocationHistory> pesHistory,
			StatusType status) {
		return pesHistory.stream().filter(ah -> ah.getStatus().equals(status)).findFirst();
	}
}
