package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.pes.CertificateContainer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.w3c.dom.Element;

public class SignatureVerifier {
    private CertificateContainer trustStore;
    private CertificateContainer interTrustStore;
    private XadesInfoExtractor xadesInfoExtractor;
    private CertificateProcessor certificateProcessor;
    private XMLDsigSignatureAndReferencesProcessor xmlDsigSignatureAndReferencesProcessor;
    private XadesInfoProcessor xadesInfoProcessor;

    public SignatureVerifier(CertificateContainer trustStore, CertificateContainer interTrustStore) {
        this.trustStore = trustStore;
        this.interTrustStore = interTrustStore;
        xadesInfoExtractor = new XadesInfoExtractor();
        certificateProcessor = new CertificateProcessor();
        xmlDsigSignatureAndReferencesProcessor = new XMLDsigSignatureAndReferencesProcessor();
        xadesInfoProcessor = new XadesInfoProcessor();
    }

    public SignatureVerifierResult process(Element element, String baseUri) {
        SignatureVerifierResult signatureVerificationResult = new SignatureVerifierResult();
        XMLSignature xmlSignature = null;
        try {
            xmlSignature = extractXmlSignature(element, baseUri);
        } catch (Exception e) {
            signatureVerificationResult.setUnverifiableSignatureException(e);
            return signatureVerificationResult;
        }
        signatureVerificationResult.setXmlSignature(xmlSignature);
        try {
            signatureVerificationResult.setXadesInfo(xadesInfoExtractor.process(xmlSignature));
        } catch (Exception xiee) {
            signatureVerificationResult.setXadesExtractionException(xiee);
        }
        try {
            signatureVerificationResult.setCertificatInformation(certificateProcessor.process(trustStore, xmlSignature,
                    signatureVerificationResult, interTrustStore));
        } catch (Exception cpe) {
            signatureVerificationResult.setCertificateProcessException(cpe);
        }
        signatureVerificationResult.setSignatureAndRefsVerificationResult(xmlDsigSignatureAndReferencesProcessor
                .process(xmlSignature, signatureVerificationResult.getSigningCertificate()));
        signatureVerificationResult
                .setXadesInfoProcessResult(xadesInfoProcessor.process(xmlSignature, signatureVerificationResult));
        return signatureVerificationResult;
    }

    private XMLSignature extractXmlSignature(Element signatureElement, String baseUri) {
        XMLSignature xmlSignature = null;
        try {
            xmlSignature = new XMLSignature(signatureElement, baseUri);
        } catch (XMLSignatureException xmlse) {
        } catch (XMLSecurityException xmlse) {
        }
        if (xmlSignature.getId() == null) {
        }
        return xmlSignature;
    }

    public CertificateProcessor getCertificateProcessor() {
        return certificateProcessor;
    }

    public CertificateContainer getTrustStore() {
        return trustStore;
    }

    public CertificateContainer getInterTrustStore() {
        return interTrustStore;
    }

    static {
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.verifier.SignatureVerifier
 * Java Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */