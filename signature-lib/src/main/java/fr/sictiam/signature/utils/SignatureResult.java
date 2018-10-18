package fr.sictiam.signature.utils;

import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.policy.rules.SubIndication;

public class SignatureResult {

    private String signatureId;
    private Indication status;
    private SubIndication reason;

    public String getSignatureId() {
        return signatureId;
    }

    public SignatureResult(String signatureId, Indication status, SubIndication reason) {
        this.signatureId = signatureId;
        this.status = status;
        this.reason = reason;
    }

    public void setSignatureId(String signatureId) {
        this.signatureId = signatureId;
    }

    public Indication getStatus() {
        return status;
    }

    public void setStatus(Indication status) {
        this.status = status;
    }

    public SubIndication getReason() {
        return reason;
    }

    public void setReason(SubIndication reason) {
        this.reason = reason;
    }
}
