import eu.europa.esig.dss.validation.reports.DetailedReport;
import fr.sictiam.signature.utils.PadesUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.cert.CertificateException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PdfValidation {

    @Test
    public void test() throws IOException, CertificateException {
        ClassPathResource classPathResource = new ClassPathResource("SampleSignedPDFDocument.pdf");
        DetailedReport signedReport = PadesUtils.validatePdf(IOUtils.toByteArray(classPathResource.getInputStream()));

        assertThat(PadesUtils.isSigned(signedReport), is(true));
        assertThat(PadesUtils.getSignatureResults(signedReport), notNullValue());
    }

}
