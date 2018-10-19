package fr.sictiam.signature.utils;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.pades.validation.PAdESCRLSource;
import eu.europa.esig.dss.pades.validation.PAdESOCSPSource;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidator;
import eu.europa.esig.dss.pdf.PdfDssDict;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.x509.crl.CRLSource;
import eu.europa.esig.dss.x509.ocsp.OCSPSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;

public class PadesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PadesUtils.class);

    private final static DataLoader dataloader = new CommonsDataLoader();

    public static DetailedReport validatePdf(byte[] fileBytes) throws IOException, CertificateException {
        DSSDocument dssDocument = new InMemoryDocument(fileBytes);
        PDFDocumentValidator pdfDocumentValidator = new PDFDocumentValidator(dssDocument);

        CommonTrustedCertificateSource certificateSource =
                CertUtils.loadLocaleCertificateSource("/signature/certs.zip");

        PdfDssDict pdfDssDict = PdfDssDict.extract(null);
        OCSPSource ocspSource = new PAdESOCSPSource(pdfDssDict);

        CRLSource crl = new PAdESCRLSource(pdfDssDict);
        CertificateVerifier certificateVerifier = new CommonCertificateVerifier(certificateSource, crl, ocspSource,
                dataloader);

        pdfDocumentValidator.setCertificateVerifier(certificateVerifier);

        Reports reports = pdfDocumentValidator.validateDocument();
        return reports.getDetailedReport();
    }

    public static DetailedReport validatePAdESSignature(byte[] fileBytes) throws IOException {
        PDFDocumentValidator documentValidator = new PDFDocumentValidator(new InMemoryDocument(fileBytes));
        documentValidator.setCertificateVerifier(CertUtils.getCertificateVerifier());

        documentValidator.setValidationLevel(ValidationLevel.ARCHIVAL_DATA);

        Reports reports = documentValidator.validateDocument();
        return reports.getDetailedReport();
    }

    public static boolean isSigned(DetailedReport report) {
        return !report.getSignatureIds().isEmpty();
    }

    public static List<SignatureResult> getSignatureResults(DetailedReport report) {
        return report.getSignatureIds().stream()
                .map(signatureId -> new SignatureResult(signatureId,
                        report.getBasicValidationIndication(signatureId),
                        report.getBasicValidationSubIndication(signatureId)))
                .collect(Collectors.toList());
    }
}
