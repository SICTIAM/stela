package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XMLDsigSignatureAndReferencesProcessor$SignatureAndRefsVerificationResult {
    private boolean signatureVerified = false;
    private List<XMLDsigReference1> xmlDsigReferences;

    public XMLDsigSignatureAndReferencesProcessor$SignatureAndRefsVerificationResult() {
        xmlDsigReferences = new ArrayList();
    }

    private boolean mainC14Accepted = false;
    private boolean allrefsC14Accepted = true;

    public boolean isSignatureVerified() {
        return signatureVerified;
    }

    void setSignatureVerified(boolean signatureVerified) {
        this.signatureVerified = signatureVerified;
    }

    void addReferenceInfo(XMLDsigReference1 xmlDsigReference) {
        xmlDsigReferences.add(xmlDsigReference);
    }

    public List<XMLDsigReference1> getReferencesInfo() {
        return Collections.unmodifiableList(xmlDsigReferences);
    }

    public List<XMLDsigReference1> getSignedPropertiesReferences() {
        List<XMLDsigReference1> signedPropertiesReferences = new ArrayList();
        for (XMLDsigReference1 xmlDsigReference : xmlDsigReferences) {
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

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.
 * SignatureAndRefsVerificationResult Java Class Version: 6 (50.0) JD-Core
 * Version: 0.7.1
 */