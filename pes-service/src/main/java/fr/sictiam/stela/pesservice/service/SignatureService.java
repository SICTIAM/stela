package fr.sictiam.stela.pesservice.service;

import com.axyus.signature.pes.CertificateContainer;
import com.axyus.signature.pes.verifier.SignatureVerifier;
import com.axyus.signature.pes.verifier.SignatureVerifierResult;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import fr.sictiam.signature.utils.PadesUtils;
import fr.sictiam.signature.utils.SignatureResult;
import fr.sictiam.stela.pesservice.service.exceptions.MissingSignatureException;
import fr.sictiam.stela.pesservice.service.exceptions.SignatureException;
import org.apache.xml.security.utils.IdResolver;
import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
public class SignatureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureService.class);
    private static final String NAMESPACE_SPEC_NS = "http://www.w3.org/2000/xmlns/";
    private static final String SIGNATURE_SPEC_NS = "http://www.w3.org/2000/09/xmldsig#";
    private static final String QUALIFIED_NAME = "xmlns:ds";

    private Document loadXml(InputStream pesStream) throws ParserConfigurationException, SAXException, IOException {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setAttribute("http://xml.org/sax/features/namespaces", Boolean.TRUE);
        return dbf.newDocumentBuilder().parse(new InputSource(pesStream));
    }

    private List<SignatureVerifierResult> processPesVerification(Document document)
            throws IOException, TransformerException, CertificateException, MissingSignatureException {

        NodeList ids = XPathAPI.selectNodeList(document, "//*[@Id]", createNamespaceNode(document));
        for (int i = 0; i < ids.getLength(); i++) {
            Element e = (Element) ids.item(i);
            IdResolver.registerElementById(e, e.getAttributeNode("Id"));
        }

        NodeList sigs = XPathAPI.selectNodeList(document, "//ds:Signature", createNamespaceNode(document));
        if (sigs.getLength() == 0) {
            LOGGER.error("Signature not found in PES");
            throw new MissingSignatureException();
        }
        SignatureVerifier signatureVerificationProcessor =
                new SignatureVerifier(
                        CertificateContainer.fromZipURL(getClass().getResource("/signature/CA_RGS3.zip")),
                        CertificateContainer.fromZipURL(getClass().getResource("/signature/CA_RGS3.zip")));
        List<SignatureVerifierResult> results = new ArrayList<>();
        for (int i = 0; i < sigs.getLength(); i++) {
            Element element = (Element) sigs.item(i);
            results.add(signatureVerificationProcessor.process(element, null));
        }

        return results;
    }


    private static Element createNamespaceNode(Document document) {
        Element nscontext = document.createElementNS(null, "namespaceContext");
        nscontext.setAttributeNS(NAMESPACE_SPEC_NS, QUALIFIED_NAME, SIGNATURE_SPEC_NS);
        return nscontext;
    }


    private List<String> validatePdfAttachment(byte[] pdf) throws CertificateException, IOException {
        List<String> messages = new ArrayList<>();
        DetailedReport report = PadesUtils.validatePAdESSignature(pdf);
        if (PadesUtils.isSigned(report)) {
            List<SignatureResult> signatureResults = PadesUtils.getSignatureResults(report);
            signatureResults.forEach(signatureResult -> {
                if (!(Indication.TOTAL_PASSED.equals(signatureResult.getStatus())
                        || Indication.PASSED.equals(signatureResult.getStatus()))) {
                    LOGGER.info("DSS validation response : {}", signatureResult.getReason());
                    messages.add(signatureResult.getReason().toString());
                }
            });
        }

        return messages;
    }

    public void validatePes(byte[] pesFile) throws SignatureException {
        validatePes(new ByteArrayInputStream(pesFile));
    }

    public void validatePes(InputStream pesStream) throws SignatureException, MissingSignatureException {
        try {
            Document document = loadXml(pesStream);
            List<SignatureVerifierResult> results = processPesVerification(document);

            // Verify PES signature
            for (SignatureVerifierResult result : results) {
                // check signature
                if (!result.getSignatureAndRefsVerificationResult().isSignatureVerified()) {
                    LOGGER.error("Signature cannot be verified");
                    throw new SignatureException("INVALID_SIGNATURE");
                }

                // check certificate
                try {
                    result.getSigningCertificate().checkValidity();
                } catch (CertificateExpiredException e) {
                    LOGGER.error("Expired certificate : {}", e.getMessage());
                    throw new SignatureException("EXPIRED_CERTIFICATE", e);
                } catch (CertificateNotYetValidException e) {
                    LOGGER.error("Certificate not yet valid : {}", e.getMessage());
                    throw new SignatureException("NOT_YET_VALID_CERTIFICATE", e);
                }
            }

            // Verify embedded pdf files
            NodeList pdfs = XPathAPI.selectNodeList(document, "//Fichier[@MIMEType='application/pdf']",
                    createNamespaceNode(document));
            for (int i = 0; i < pdfs.getLength(); i++) {
                Node pdf = pdfs.item(i);
                InputStream bais =
                        new ByteArrayInputStream(Base64.getMimeDecoder().decode(pdf.getTextContent()));
                GZIPInputStream gis = new GZIPInputStream(bais);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = gis.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    List<String> errors = validatePdfAttachment(out.toByteArray());
                    if (!errors.isEmpty()) {
                        LOGGER.error("Error while validating pdf attachment in pes : {}",
                                errors.stream().collect(Collectors.joining(", ")));
                        throw new SignatureException("PDF_ATTACHMENT");
                    }
                } catch (IOException | CertificateException e) {
                    LOGGER.error("Generic error while validating pdf attachment in pes : {}", e.getMessage());
                    throw new SignatureException("PDF_ATTACHMENT", e);
                } catch (Exception e) {
                    LOGGER.error("Unknown error while validating pdf attachment in pes : {} : {})",
                            e.getClass().getSimpleName(), e.getMessage());
                    throw new SignatureException("PDF_ATTACHMENT", e);
                } finally {
                    bais.close();
                    gis.close();
                    out.close();
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException |
                CertificateException e) {
            LOGGER.error("Error while checking signature : {}", e.getMessage());
            throw new SignatureException("Error while parsing signed file : " + e.getMessage(), e);
        }
    }
}
