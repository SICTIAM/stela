package fr.sictiam.stela.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.model.UI.Views;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

public class JsonViewTest {

    @Test
    public void testLocalAuthView() throws IOException {

        LocalAuthority localAuthority = new LocalAuthority("Test", "368569321", "test");
        localAuthority.addModule(Module.ACTES);
        OzwilloInstanceInfo ow = new OzwilloInstanceInfo("test", "test", "test", "test", "test", "test", "test",
                "test");
        localAuthority.setOzwilloInstanceInfo(ow);
        WorkGroup workGroup = new WorkGroup(localAuthority, "GlobalGroup");
        Set<WorkGroup> groups = new HashSet<>();

        Agent agent = new Agent("John", "Doe", "john.doe@fbi.fr");
        agent.setSub("sub");
        agent.setAdmin(false);

        Profile profile = new Profile(localAuthority, agent, false);
        Set<Profile> profiles = new HashSet<>();
        profile.setGroups(groups);
        profiles.add(profile);
        workGroup.setProfiles(profiles);
        groups.add(workGroup);
        localAuthority.setProfiles(profiles);
        localAuthority.setGroups(groups);
        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writerWithView(Views.LocalAuthorityView.class).writeValueAsString(localAuthority);

        assertThat(result, notNullValue());

        LocalAuthority localAuthorityRead = mapper.readValue(result, LocalAuthority.class);
        assertThat(localAuthorityRead.getGroups(), hasSize(1));
        assertThat(localAuthorityRead.getGroups().stream().findFirst().get().getProfiles(), hasSize(1));
        assertThat(localAuthorityRead.getProfiles(), hasSize(1));
        assertThat(localAuthorityRead.getProfiles().stream().findFirst().get().getGroups(), empty());
    }

}
