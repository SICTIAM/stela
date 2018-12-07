package fr.sictiam.stela.pesservice;

import fr.sictiam.stela.pesservice.service.SesileService;
import fr.sictiam.stela.pesservice.service.SignatureService;
import fr.sictiam.stela.pesservice.service.exceptions.SignatureException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class SignatureTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureTest.class);

    private SesileService sesileService = new SesileService(null, null, null, null, null, null, null);
    private SignatureService signatureService = new SignatureService();

    @Test
    public void testValidSignature() throws Exception {
        boolean valid = true;
        InputStream pesFile = new ClassPathResource("data/30000-depenses-2018-BO-227.xml").getInputStream();

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