package fr.sictiam.stela.admin;

import fr.sictiam.stela.admin.dao.AgentRepository;
import fr.sictiam.stela.admin.dao.CertificateRepository;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.Certificate;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.service.AgentService;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.WorkGroupService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdminServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminServiceIntegrationTests extends BaseIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceIntegrationTests.class);

    @Autowired
    LocalAuthorityService localAuthorityService;

    @Autowired
    AgentService agentService;

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    CertificateRepository certificateRepository;

    @Autowired
    WorkGroupService workGroupService;

    @Autowired
    ProfileService profileService;

    @Before
    public void beforeTests() {
        LocalAuthority localAuthority = new LocalAuthority("Test", "368569321", "test");

        localAuthority.addModule(Module.ACTES);
        OzwilloInstanceInfo ow = new OzwilloInstanceInfo("test", "test", "test", "test", "test", "test", "test",
                "test");
        localAuthority.setOzwilloInstanceInfo(ow);
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);
        WorkGroup workGroup = workGroupService.create(new WorkGroup(localAuthority, "GlobalGroup"));
        Set<WorkGroup> groups = new HashSet<>();
        groups.add(workGroup);
        localAuthority.setGroups(groups);

        Agent agent = new Agent("John", "Doe", "john.doe@fbi.fr");
        agent.setSub("sub");
        agent.setAdmin(false);
        agent.setInstanceId("test");
        agent = agentService.createAndAttach(agent).getAgent();

        Profile profile = profileService.createOrUpdate(new Profile(localAuthority, agent, false));
        Set<Profile> profiles = new HashSet<>();
        profiles.add(profile);

        localAuthorityService.createOrUpdate(localAuthority);
    }

    @Test
    public void testRetrieveData() {
        LocalAuthority localAuthority = localAuthorityService.findByName("Test").get();

        assertThat(localAuthority, notNullValue());
        assertThat(localAuthority.getName(), is("Test"));
        assertThat(localAuthority.getGroups(), hasSize(1));
        assertThat(localAuthority.getGroups().stream().findFirst().get().getName(), is("GlobalGroup"));
    }

    @Test
    public void testCertificate() {
        Certificate certificate = new Certificate(
                "1C5B96E1CD9241D8227D340DD97F9172CB60F623",
                "/C=FR/ST=Alpes-Maritimes/L=Vallauris/O=SICTIAM/CN=Certificats SICTIAM/emailAddress=internet@sictiam.fr",
                "Franck BOUCHER",
                "SICTIAM",
                "SICTIAM",
                "f.boucher@sictiam.fr",
                "Certificats SICTIAM",
                "SICTIAM",
                "internet@sictiam.fr",
                LocalDate.now(),
                LocalDate.now()
        );
        Agent agent = agentService.findBySub("sub").get();
        assertThat(agent.getCertificate(), nullValue());

        agent.setCertificate(certificate);
        agentRepository.save(agent);

        agent = agentService.findBySub("sub").get();
        assertThat(agent.getCertificate(), notNullValue());
        assertThat(agent.getCertificate().getSerial(), is("1C5B96E1CD9241D8227D340DD97F9172CB60F623"));
        assertThat(certificateRepository.findByUuid(agent.getCertificate().getUuid()).isPresent(), is(true));

        String certificateUuid = agent.getCertificate().getUuid();
        agent.setCertificate(null);
        agentRepository.save(agent);
        assertThat(certificateRepository.findByUuid(certificateUuid).isPresent(), is(false));
    }
}
