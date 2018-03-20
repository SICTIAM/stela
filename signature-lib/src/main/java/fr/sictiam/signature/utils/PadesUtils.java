package fr.sictiam.signature.utils;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.pades.validation.PAdESCRLSource;
import eu.europa.esig.dss.pades.validation.PAdESOCSPSource;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidator;
import eu.europa.esig.dss.pdf.PdfDssDict;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.x509.crl.CRLSource;
import eu.europa.esig.dss.x509.ocsp.OCSPSource;
import fr.sictiam.signature.pes.CertificateContainer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;

public class PadesUtils {

    public static DetailedReport validatePdf(byte[] fileBytes) throws IOException, CertificateException {
        DSSDocument dssDocument = new InMemoryDocument(fileBytes);
        PDFDocumentValidator pdfDocumentValidator = new PDFDocumentValidator(dssDocument);

        CommonTrustedCertificateSource certificateSource = new CommonTrustedCertificateSource();

        ClassPathResource stream = new ClassPathResource("/signature/certs.zip");

        CertificateContainer.fromZipURL(stream.getInputStream()).getAllCertificates()
                .forEach(certif -> certificateSource.addCertificate(new CertificateToken(certif)));

        PdfDssDict pdfDssDict = PdfDssDict.extract(null);
        OCSPSource ocspSource = new PAdESOCSPSource(pdfDssDict);

        CRLSource crl = new PAdESCRLSource(pdfDssDict);
        CertificateVerifier certificateVerifier = new CommonCertificateVerifier(certificateSource, crl, ocspSource,
                null);

        pdfDocumentValidator.setCertificateVerifier(certificateVerifier);

        Reports reports = pdfDocumentValidator.validateDocument();
        return reports.getDetailedReport();
    }

    public static boolean isSigned(DetailedReport report) {
        return !report.getSignatureIds().isEmpty();
    }

    public static List<PdfSignatureResult> getSignatureResults(DetailedReport report) {
        return report.getSignatureIds().stream()
                .map(signatureId -> new PdfSignatureResult(signatureId,
                        report.getBasicValidationIndication(signatureId),
                        report.getBasicValidationSubIndication(signatureId)))
                .collect(Collectors.toList());
    }
}
