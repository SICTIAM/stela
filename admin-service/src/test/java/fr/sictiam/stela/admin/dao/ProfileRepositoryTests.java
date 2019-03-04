package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.Profile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest(
        excludeAutoConfiguration = { FlywayAutoConfiguration.class }
)
public class ProfileRepositoryTests {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void findBySirenAdnEmailInsensitive() {

        // Quite a lot of boilerplate code
        // Hopefully, it will be fixed with the admin model refactoring
        LocalAuthority localAuthority = new LocalAuthority("SICTIAM", "siren", "sictiam)");
        localAuthority.setOzwilloInstanceInfo(new OzwilloInstanceInfo());
        localAuthority = entityManager.persist(localAuthority);
        Agent agent = new Agent("stela@sictiam.fr");
        agent.setAdmin(false);
        agent = entityManager.persist(agent);
        Profile profile = new Profile(localAuthority, agent, true);
        profileRepository.save(profile);

        Optional<Profile> optionalProfile =
                profileRepository.findByLocalAuthority_SirenAndAgent_EmailIgnoreCase("siren", "STELA@sictiam.fr");
        Assert.assertTrue(optionalProfile.isPresent());

        optionalProfile =
                profileRepository.findByLocalAuthority_SirenAndAgent_EmailIgnoreCase("siren", "stela@sictiam.fr");
        Assert.assertTrue(optionalProfile.isPresent());

        optionalProfile =
                profileRepository.findByLocalAuthority_SirenAndAgent_EmailIgnoreCase("siren", "stelax@sictiam.fr");
        Assert.assertFalse(optionalProfile.isPresent());
    }
}
