package fr.sictiam.signature.pes.verifier;

import java.util.List;

public class SignatureValidation {

    boolean valid;
    List<SignatureValidationError> signatureValidationErrors;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<SignatureValidationError> getSignatureValidationErrors() {
        return signatureValidationErrors;
    }

    public void setSignatureValidationErrors(List<SignatureValidationError> signatureValidationErrors) {
        this.signatureValidationErrors = signatureValidationErrors;
    }
}
