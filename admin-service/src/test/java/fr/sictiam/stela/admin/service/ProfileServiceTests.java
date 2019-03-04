package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ProfileService.class)
public class ProfileServiceTests {

    @Autowired
    private ProfileService profileService;

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private WorkGroupService workGroupService;

    @MockBean
    private LocalAuthorityService localAuthorityService;

    @Test
    public void getByLocalAuthoritySirenAndEmail() {
        LocalAuthority localAuthority = new LocalAuthority("SICTIAM", "siren", "sictiam)");
        Agent agent = new Agent("stela@sictiam.fr");
        given(profileRepository.findByLocalAuthority_SirenAndAgent_EmailIgnoreCase("siren", "stela@sictiam.fr"))
                .willReturn(Optional.of(new Profile(localAuthority, agent, true)));

        Profile profile = profileService.getByLocalAuthoritySirenAndEmail("siren", "stela@sictiam.fr");
        Assert.assertEquals("SICTIAM", profile.getLocalAuthority().getName());
        Assert.assertEquals("stela@sictiam.fr", profile.getAgent().getEmail());
        Assert.assertTrue(profile.getAdmin());

        verify(profileRepository).findByLocalAuthority_SirenAndAgent_EmailIgnoreCase("siren", "stela@sictiam.fr");
    }

    @Test(expected = NotFoundException.class)
    public void getByLocalAuthoritySirenAndEmailNotFound() {
        given(profileRepository.findByLocalAuthority_SirenAndAgent_EmailIgnoreCase("siren", "stela@sictiam.fr"))
                .willReturn(Optional.empty());

        profileService.getByLocalAuthoritySirenAndEmail("siren", "stela@sictiam.fr");

        verify(profileRepository).findByLocalAuthority_SirenAndAgent_EmailIgnoreCase("siren", "stela@sictiam.fr");
    }
}
