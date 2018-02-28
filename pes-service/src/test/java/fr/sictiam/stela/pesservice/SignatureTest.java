package fr.sictiam.stela.pesservice;

import fr.sictiam.signature.pes.verifier.PesAllerAnalyser.InvalidPesAllerFileException;
import fr.sictiam.signature.pes.verifier.SignatureValidation;
import fr.sictiam.signature.pes.verifier.SimplePesInformation;
import fr.sictiam.stela.pesservice.service.SesileService;
import org.apache.commons.io.IOUtils;
import org.apache.xml.security.Init;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SignatureTest {

    SesileService sesileService = new SesileService(null, null, null, null);

    @Test
    public void testsigned() throws IOException, InvalidPesAllerFileException {
        Init.init();
        InputStream file = new ClassPathResource("data/30002-2015-P-DN-16-1429552171140-sign.xml").getInputStream();

        SimplePesInformation simplePesInformation = sesileService
                .computeSimplePesInformation(IOUtils.toByteArray(file));
        assertThat(sesileService.isSigned(simplePesInformation), is(true));
        SignatureValidation validation = sesileService.isValidSignature(simplePesInformation);
        assertThat(validation.isValid(), is(true));
        InputStream fileNotSigned = new ClassPathResource("data/28000-2017-P-RN-22-1516807373820.xml").getInputStream();
        SimplePesInformation simplePesInformationNotSigned = sesileService
                .computeSimplePesInformation(IOUtils.toByteArray(fileNotSigned));

        assertThat(sesileService.isSigned(simplePesInformationNotSigned), is(false));

    }

}
