package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.ArchiveSettings;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.model.StampPosition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest(
        excludeAutoConfiguration = { FlywayAutoConfiguration.class }
)
@ActiveProfiles("test")
public class LocalAuthorityRepositoryTests {

    @Autowired
    private LocalAuthorityRepository localAuthorityRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void shouldReturnMaterialCodes() {

        LocalAuthority localAuthority = new LocalAuthority("uuid", "SICTIAM", "siren", true);
        localAuthority.setMaterialCodes(Collections.singletonList(new MaterialCode("1-1", "Acte budgétaire", localAuthority)));
        localAuthority.setArchiveSettings(new ArchiveSettings());
        localAuthority.setStampPosition(new StampPosition(10, 10));
        entityManager.persist(localAuthority);

        Optional<LocalAuthority> optLocalAuthority = localAuthorityRepository.findWithMaterialCodesBySiren("siren");

        /*
         * FIXME it seems material codes are also loaded if we call localAuthorityRepository.findBySiren(...)
         *  a manual inspection showed the resulting SQL effectively joins on the material_codes table
         *  look later at how the test could be more valuable
         */
        Assert.assertTrue(optLocalAuthority.isPresent());
        LocalAuthority loadedLocalAuthority = optLocalAuthority.get();
        Assert.assertEquals(1, loadedLocalAuthority.getMaterialCodes().size());
        Assert.assertEquals("1-1", loadedLocalAuthority.getMaterialCodes().get(0).getCode());
    }
}
