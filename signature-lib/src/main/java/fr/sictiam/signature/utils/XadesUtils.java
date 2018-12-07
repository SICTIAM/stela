package fr.sictiam.signature.utils;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import eu.europa.esig.dss.validation.reports.Reports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;

public class XadesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(XadesUtils.class);

    private final static DataLoader dataloader = new CommonsDataLoader();

    public static DetailedReport validateXadesSignature(byte[] fileBytes) throws IOException, CertificateException {
        DSSDocument dssDocument = new InMemoryDocument(fileBytes);
        SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(dssDocument);
        documentValidator.setCertificateVerifier(CertUtils.getCertificateVerifier());

        Reports reports = documentValidator.validateDocument();
        LOGGER.error(reports.getSimpleReport().getJaxbModel().toString());
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

    public static Indication getCertificateValidationResult(DetailedReport report) {
        List<SignatureResult> signatureResults = getSignatureResults(report);
        Indication indicationResult = Indication.TOTAL_PASSED;
        for (SignatureResult signatureResult : signatureResults) {
            if (Indication.TOTAL_FAILED.equals(signatureResult.getStatus())
                    || Indication.FAILED.equals(signatureResult.getStatus())
                    || Indication.INDETERMINATE.equals(signatureResult.getStatus())) {
                return signatureResult.getStatus();
            } else if (!indicationResult.equals(signatureResult.getStatus())
                    && !Indication.TOTAL_PASSED.equals(signatureResult.getStatus())) {
                indicationResult = signatureResult.getStatus();
            }
        }
        return indicationResult;
    }
}
