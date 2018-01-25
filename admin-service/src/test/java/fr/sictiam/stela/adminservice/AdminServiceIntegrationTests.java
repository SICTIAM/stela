package fr.sictiam.stela.adminservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import fr.sictiam.stela.admin.AdminServiceApplication;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.service.AgentService;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.WorkGroupService;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdminServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminServiceIntegrationTests extends BaseIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceIntegrationTests.class);

    @Autowired
    LocalAuthorityService localAuthorityService;

    @Autowired
    AgentService agentService;
    
    @Autowired
    WorkGroupService workGroupService;
    
    @Autowired
    ProfileService profileService;

    @Before
    public void beforeTests() {
        LocalAuthority localAuthority = new LocalAuthority("Test", "368569321", "test");
        localAuthority.addModule(Module.ACTES);
        OzwilloInstanceInfo ow=new OzwilloInstanceInfo("test", "test", "test", "test", "test", "test", "test");
        localAuthority.setOzwilloInstanceInfo(ow);
        localAuthority =localAuthorityService.createOrUpdate(localAuthority);
        WorkGroup workGroup = workGroupService.create(new WorkGroup(localAuthority, "GlobalGroup"));
        Set<WorkGroup> groups = new HashSet<>();
        groups.add(workGroup);
        localAuthority.setGroups(groups);
        
        Agent agent = new Agent("John", "Doe", "john.doe@fbi.fr");
        agent.setSub("sub");
        agent.setAdmin(false);
        agent = agentService.createAndAttach(agent).getAgent();
        
        Profile profile = profileService.create(new Profile(localAuthority, agent, false));
        Set<Profile> profiles = new HashSet<>();
        profiles.add(profile);
        
        localAuthorityService.createOrUpdate(localAuthority);
    }

    @Test
    public void testRetrieveData() {
        LocalAuthority localAuthority= localAuthorityService.findByName("Test").get();

        assertThat(localAuthority, notNullValue());
        assertThat(localAuthority.getName(), is("Test"));
        assertThat(localAuthority.getGroups(), hasSize(1));
        assertThat(localAuthority.getGroups().stream().findFirst().get().getName(), is("GlobalGroup"));
    }
}
