package fr.sictiam.signature.utils;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.pades.validation.PAdESCRLSource;
import eu.europa.esig.dss.pades.validation.PAdESOCSPSource;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidator;
import eu.europa.esig.dss.pdf.PdfDssDict;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.x509.crl.CRLSource;
import eu.europa.esig.dss.x509.ocsp.OCSPSource;
import fr.sictiam.signature.pes.CertificateContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PadesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PadesUtils.class);

    private final static DataLoader dataloader = new CommonsDataLoader();

    public static DetailedReport validatePdf(byte[] fileBytes) throws IOException, CertificateException {
        DSSDocument dssDocument = new InMemoryDocument(fileBytes);
        PDFDocumentValidator pdfDocumentValidator = new PDFDocumentValidator(dssDocument);

        CommonTrustedCertificateSource certificateSource = loadLocaleCertificateSource("/signature/certs.zip");

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
        documentValidator.setCertificateVerifier(getCertificateVerifier());

        documentValidator.setValidationLevel(ValidationLevel.ARCHIVAL_DATA);

        Reports reports = documentValidator.validateDocument();
        return reports.getDetailedReport();
    }

    private static TrustedListsCertificateSource getTrustedListsCertificateSource() throws IOException {
        TrustedListsCertificateSource certificateSource = new TrustedListsCertificateSource();

        KeyStoreCertificateSource keyStoreCertificateSource = new KeyStoreCertificateSource(
                new ClassPathResource("signature/keystore.p12").getInputStream(), "PKCS12", "dss-password");

        TSLRepository tslRepository = new TSLRepository();
        tslRepository.setTrustedListsCertificateSource(certificateSource);

        TSLValidationJob job = new TSLValidationJob();
        job.setDataLoader(dataloader);
        job.setRepository(tslRepository);
        job.setLotlUrl("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml");
        job.setLotlRootSchemeInfoUri("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl.html");
        job.setLotlCode("FR");
        job.setFilterTerritories(Collections.singletonList("FR"));
        job.setOjUrl("http://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2016.233.01.0001.01.ENG");
        job.setOjContentKeyStore(keyStoreCertificateSource);
        job.setCheckLOTLSignature(true);
        job.setCheckTSLSignatures(true);

        job.refresh();

        return certificateSource;
    }

    public static CertificateVerifier getCertificateVerifier() throws IOException {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setTrustedCertSource(getTrustedListsCertificateSource());

        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(dataloader);
        certificateVerifier.setCrlSource(onlineCRLSource);


        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(new OCSPDataLoader());
        certificateVerifier.setOcspSource(onlineOCSPSource);

        certificateVerifier.setDataLoader(dataloader);
        return certificateVerifier;
    }

    private static CommonTrustedCertificateSource loadLocaleCertificateSource(String fileSrc)
            throws IOException, CertificateException {
        CommonTrustedCertificateSource certificateSource = new CommonTrustedCertificateSource();

        ClassPathResource stream = new ClassPathResource(fileSrc);
        CertificateContainer.fromZipURL(stream.getInputStream()).getAllCertificates()
                .forEach(certif -> certificateSource.addCertificate(new CertificateToken(certif)));
        return certificateSource;
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
