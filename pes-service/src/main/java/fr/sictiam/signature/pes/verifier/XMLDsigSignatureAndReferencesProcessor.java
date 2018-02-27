package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1;
import fr.sictiam.signature.utils.DomUtils;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.ReferenceNotInitializedException;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.InvalidTransformException;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XMLDsigSignatureAndReferencesProcessor {
    public SignatureAndRefsVerificationResult1 process(XMLSignature xmlSignature, X509Certificate signingCertificate) {
        SignatureAndRefsVerificationResult1 signatureAndRefsVerificationResult = new SignatureAndRefsVerificationResult1();
        SignedInfo signedInfo = xmlSignature.getSignedInfo();
        for (int i = 0; i < signedInfo.getLength(); i++) {
            Reference reference = null;
            try {
                reference = signedInfo.item(i);
            } catch (XMLSecurityException xmlse) {
                throw new UnExpectedException(xmlse);
            }
            XMLDsigReference1 xmlDsigReference = new XMLDsigReference1(reference);
            signatureAndRefsVerificationResult.addReferenceInfo(xmlDsigReference);
        }
        processSignatureAndReferencesVerification(signatureAndRefsVerificationResult, xmlSignature, signingCertificate);

        processC14AlgorithmVerification(signatureAndRefsVerificationResult, signedInfo);

        return signatureAndRefsVerificationResult;
    }

    private void processSignatureAndReferencesVerification(
            SignatureAndRefsVerificationResult1 signatureAndRefsVerificationResult, XMLSignature xmlSignature,
            X509Certificate signingCertificate) {
        boolean signatureAndReferencesVerified = false;
        try {
            signatureAndReferencesVerified = xmlSignature.checkSignatureValue(signingCertificate);
        } catch (XMLSignatureException xmlse) {
            throw new UnExpectedException(xmlse);
        }
        XMLDsigReference1 xmlDsigReference;
        if (signatureAndReferencesVerified) {
            signatureAndRefsVerificationResult.setSignatureVerified(true);
            for (Iterator<XMLDsigReference1> xmlse = signatureAndRefsVerificationResult.getReferencesInfo()
                    .iterator(); xmlse.hasNext();) {
                xmlDsigReference = xmlse.next();
                xmlDsigReference.setVerified(true);
            }
        } else {
            boolean allReferencesVerified = true;
            for (XMLDsigReference1 xmlDsigReference1 : signatureAndRefsVerificationResult.getReferencesInfo()) {
                try {
                    if (xmlDsigReference1.getReference().verify()) {
                        xmlDsigReference1.setVerified(true);
                    } else {
                        allReferencesVerified = false;
                        xmlDsigReference1.setVerified(false);
                    }
                } catch (ReferenceNotInitializedException rnie) {
                    throw new UnExpectedException(rnie);
                } catch (XMLSecurityException xmlse) {
                    throw new UnExpectedException(xmlse);
                }
            }
            if (allReferencesVerified) {
                signatureAndRefsVerificationResult.setSignatureVerified(false);
            }
        }
    }

    private void processC14AlgorithmVerification(SignatureAndRefsVerificationResult1 signatureAndRefsVerificationResult,
            SignedInfo signedInfo) {
        String mainC14algo = signedInfo.getCanonicalizationMethodURI();
        if (XMLDsigC14Algorithm1.isAcceptedC14Algorithm(mainC14algo)) {
            signatureAndRefsVerificationResult.setMainC14Accepted(true);
        }
        for (XMLDsigReference1 xmlDsigReference : signatureAndRefsVerificationResult.getReferencesInfo()) {
            Reference reference = xmlDsigReference.getReference();
            Transforms reftransforms = null;
            try {
                reftransforms = reference.getTransforms();
            } catch (XMLSignatureException ex) {
                Logger.getLogger(XMLDsigSignatureAndReferencesProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidTransformException ex) {
                Logger.getLogger(XMLDsigSignatureAndReferencesProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformationException ex) {
                Logger.getLogger(XMLDsigSignatureAndReferencesProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XMLSecurityException ex) {
                Logger.getLogger(XMLDsigSignatureAndReferencesProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (reftransforms != null) {
                for (int i = 0; i < reftransforms.getLength(); i++) {
                    try {
                        Transform t = reftransforms.item(i);
                        String uri = t.getURI();
                        if ((XMLDsigC14Algorithm1.isC14Algorithm(uri))
                                && (!XMLDsigC14Algorithm1.isAcceptedC14Algorithm(uri))) {
                            signatureAndRefsVerificationResult.setAllrefsC14Accepted(false);
                            break;
                        }
                    } catch (TransformationException ex) {
                        Logger.getLogger(XMLDsigSignatureAndReferencesProcessor.class.getName()).log(Level.SEVERE, null,
                                ex);
                    }
                }
            }
        }
    }

    public static class SignatureAndRefsVerificationResult1 {
        private boolean signatureVerified = false;
        private List<XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1> xmlDsigReferences;

        public SignatureAndRefsVerificationResult1() {
            xmlDsigReferences = new ArrayList<XMLDsigReference1>();
        }

        private boolean mainC14Accepted = false;
        private boolean allrefsC14Accepted = true;

        public boolean isSignatureVerified() {
            return signatureVerified;
        }

        void setSignatureVerified(boolean signatureVerified) {
            this.signatureVerified = signatureVerified;
        }

        void addReferenceInfo(XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1 xmlDsigReference) {
            xmlDsigReferences.add(xmlDsigReference);
        }

        public List<XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1> getReferencesInfo() {
            return Collections.unmodifiableList(xmlDsigReferences);
        }

        public List<XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1> getSignedPropertiesReferences() {
            List<XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1> signedPropertiesReferences = new ArrayList<XMLDsigReference1>();
            for (XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1 xmlDsigReference : xmlDsigReferences) {
                if (xmlDsigReference.isSignedPropertiesReference()) {
                    signedPropertiesReferences.add(xmlDsigReference);
                }
            }
            return signedPropertiesReferences;
        }

        public boolean isMainC14Accepted() {
            return mainC14Accepted;
        }

        public void setMainC14Accepted(boolean mainC14Accepted) {
            this.mainC14Accepted = mainC14Accepted;
        }

        public boolean isAllrefsC14Accepted() {
            return allrefsC14Accepted;
        }

        public void setAllrefsC14Accepted(boolean allrefsC14Accepted) {
            this.allrefsC14Accepted = allrefsC14Accepted;
        }
    }

    public static class XMLDsigC14Algorithm1 {
        private static final String[] acceptedAlgorithms = { "http://www.w3.org/2001/10/xml-exc-c14n#",
                "http://www.w3.org/2001/10/xml-exc-c14n#WithComments" };
        private static final String[] notAcceptedAlgorithms = { "http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
                "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" };

        public static boolean isC14Algorithm(String algo) {
            for (String s : getAcceptedAlgorithms()) {
                if (s.equals(algo)) {
                    return true;
                }
            }
            for (String s : getNotAcceptedAlgorithms()) {
                if (s.equals(algo)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isAcceptedC14Algorithm(String algo) {
            for (String s : getAcceptedAlgorithms()) {
                if (s.equals(algo)) {
                    return true;
                }
            }
            return false;
        }

        public static String[] getAcceptedAlgorithms() {
            return acceptedAlgorithms;
        }

        public static String[] getNotAcceptedAlgorithms() {
            return notAcceptedAlgorithms;
        }
    }

    public static class XMLDsigReference1 {
        private static final String XADES_1_1_1_SIGNED_PROPS_TYPE_URI = "http://uri.etsi.org/01903/v1.1.1#SignedProperties";
        private static final String XADES_1_2_2_SIGNED_PROPS_TYPE_URI = "http://uri.etsi.org/01903/v1.2.2#SignedProperties";
        private static final String XADES_OTHER_VERSION_SIGNED_PROPS_TYPE_URI = "http://uri.etsi.org/01903#SignedProperties";
        private Reference reference;
        private boolean verified = false;
        private boolean c14algoAccepted = false;

        public XMLDsigReference1(Reference reference) {
            this.reference = reference;
        }

        public Reference getReference() {
            return reference;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        public boolean isVerified() {
            return verified;
        }

        public boolean isSignedPropertiesReference() {
            return ("http://uri.etsi.org/01903/v1.1.1#SignedProperties".equals(reference.getType()))
                    || ("http://uri.etsi.org/01903#SignedProperties".equals(reference.getType()));
        }

        public boolean isSignedPropertiesReferenceLookup(Document doc) {
            String id = reference.getURI();
            if ((id != null) && (!id.isEmpty())) {
                try {
                    Node n = XPathAPI.selectSingleNode(doc,
                            "//*[@Id='" + ((id != null) && (id.startsWith("#")) ? id.substring(1) : id) + "']",
                            DomUtils.createXMLDSigAndPesNamespaceNode(doc));
                    if (n != null) {
                        String reftype = n.lookupNamespaceURI(n.getPrefix()) + n.getLocalName();
                        if (("http://uri.etsi.org/01903/v1.1.1#SignedProperties".equals(reftype))
                                || ("http://uri.etsi.org/01903/v1.2.2#SignedProperties".equals(reftype))
                                || ("http://uri.etsi.org/01903#SignedProperties".equals(reftype))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }

        public boolean isC14algoAccepted() {
            return c14algoAccepted;
        }

        public void setC14algoAccepted(boolean c14algoAccepted) {
            this.c14algoAccepted = c14algoAccepted;
        }
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor Java
 * Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */