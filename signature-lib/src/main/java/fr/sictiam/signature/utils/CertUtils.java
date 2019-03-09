package fr.sictiam.signature.utils;

import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonTrustedCertificateSource;
import fr.sictiam.signature.pes.CertificateContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

public class CertUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertUtils.class);

    private final static DataLoader dataloader = new CommonsDataLoader();

    public static CertificateReports validateCertificate(byte[] file) throws IOException, CertificateException {
        CertificateToken certificate = getCertificate(file);
        CertificateValidator certificateValidator = CertificateValidator.fromCertificate(certificate);
        certificateValidator.setCertificateVerifier(getCertificateVerifier());
        certificateValidator.setValidationTime(new Date());
        return certificateValidator.validate();
    }

    public static Indication getCertificateValidationResult(CertificateReports report) {
        List<String> certificateIds = report.getSimpleReport().getCertificateIds();
        Indication indicationResult = Indication.TOTAL_PASSED;
        for (String certificateId : certificateIds) {
            Indication indication = report.getDetailedReport().getCertificateXCVIndication(certificateId);
            if (Indication.TOTAL_FAILED.equals(indication) || Indication.FAILED.equals(indication)
                    || Indication.INDETERMINATE.equals(indication)) {
                return indication;
            } else if (!indicationResult.equals(indication) && !Indication.TOTAL_PASSED.equals(indication)) {
                indicationResult = indication;
            }
        }
        return indicationResult;
    }

    public static CertificateVerifier getCertificateVerifier() throws IOException, CertificateException {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setTrustedCertSource(loadLocaleCertificateSource());

        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(dataloader);
        certificateVerifier.setCrlSource(onlineCRLSource);


        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(new OCSPDataLoader());
        certificateVerifier.setOcspSource(onlineOCSPSource);

        certificateVerifier.setDataLoader(dataloader);
        return certificateVerifier;
    }

    public static CommonTrustedCertificateSource loadLocaleCertificateSource()
            throws IOException, CertificateException {
        String zipSource = "/signature/CA_RGS3.zip";
        LOGGER.info("Loading zip source: {}", zipSource);
        CommonTrustedCertificateSource certificateSource = new CommonTrustedCertificateSource();
        ClassPathResource stream = new ClassPathResource(zipSource);
        CertificateContainer.fromZipURL(stream.getInputStream()).getAllCertificates()
                .forEach(certif -> certificateSource.addCertificate(new CertificateToken(certif)));
        return certificateSource;
    }

    private static CertificateToken getCertificate(byte[] file) throws DSSException {
        try {
            if (file != null) {
                return DSSUtils.loadCertificate(file);
            }
        } catch (DSSException e) {
            LOGGER.warn("Cannot convert file to X509 Certificate", e);
            throw e;
        }
        return null;
    }

    public static X509Certificate getCertificateFromBytes(byte[] bytes) throws CertificateException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(bytes);
        return (X509Certificate) certFactory.generateCertificate(in);
    }
}
