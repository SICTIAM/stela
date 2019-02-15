package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.model.ui.GenericAccount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = PaullService.class)
public class PaullServiceTest {

    @Autowired
    private PaullService paullService;

    @MockBean
    ExternalRestService externalRestService;

    @Test
    public void emailAuth() throws RuntimeException {
        given(externalRestService.authWithEmailPassword("test1@test.fr", "test1password")).willReturn(new GenericAccount());

        Assert.assertNotNull(paullService.emailAuth("test1@test.fr", "test1password"));
    }

    @Test
    public void emailAuthFailed() throws RuntimeException {
        GenericAccount genericAccount = null;
        given(externalRestService.authWithEmailPassword("test1@test.fr", "test1password")).willReturn(genericAccount);

        Assert.assertNull(paullService.emailAuth("test1@test.fr", "test1password"));
    }
}