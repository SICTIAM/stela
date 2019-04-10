package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.service.exceptions.SignatureException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = SignatureService.class,
        properties = { "application.pdfValidation = true" })
@ActiveProfiles("test")
public class SignatureServiceTest {

    @Autowired
    private SignatureService signatureService;

    @Test
    public void testValidSignature() throws Exception {
        boolean valid = true;
        InputStream pesFile = new ClassPathResource("data/PESALR2_25060187900027_190328_001.xml").getInputStream();

        try {
            signatureService.validatePes(pesFile);
        } catch (SignatureException e) {
            valid = false;
        }

        Assert.assertTrue(valid);
    }

    @Test
    public void testExpiredSignature() throws Exception {
        boolean valid = true;
        InputStream pesFile = new ClassPathResource("data/test-perime-04-04-2016.xml").getInputStream();

        try {
            signatureService.validatePes(pesFile);
        } catch (SignatureException e) {
            valid = false;

        }

        Assert.assertFalse(valid);
    }

    @Test
    public void testAlteredSignature() throws Exception {
        boolean valid = true;
        InputStream pesFile = new ClassPathResource("data/test-alteree.xml").getInputStream();

        try {
            signatureService.validatePes(pesFile);
        } catch (SignatureException e) {
            valid = false;
        }

        Assert.assertFalse(valid);
    }

}
