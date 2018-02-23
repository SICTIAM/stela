package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.pes.producer.SigningPolicies.SigningPolicy1;

import java.util.HashMap;
import java.util.Map;

public class XadesInfoProcessor$XadesInfoProcessResult {
    private String sigCertcalculatedHash;
    private String sigCertExpectedHash;
    private String sigCertHashMethod;
    private String sigCertExpectedHashMethod;
    private String sigCertSerialNumber;
    private String sigCertExpectedSerialNumber;
    private String sigCertIssuerName;
    private String sigCertIssuerNameRFC2253;
    private String sigCertExpectedIssuerName;
    private String sigCertTargetAttribute;
    private String sigCertExpectedTargetAttribute;
    private String objectFirstChild;
    private String objectExpectedFirstChild;
    private String sigSecurityPolicyId;
    private String sigExpectedSecurityPolicyId;
    private String sigExpectedSecurityPolicyIdHashMethod;
    private String sigSecurityPolicyIdHashMethod;
    private String sigExpectedSecurityPolicyIdHash;
    private String sigSecurityPolicyIdHash;
    private SigningPolicy1 signingPolicy;
    private static Map<String, String> securityAllowedProtocol = new HashMap();
    public static final String SHA1XMLProtocol = "http://www.w3.org/2000/09/xmldsig#sha1";
    public static final String SHA256XMLProtocol = "http://www.w3.org/2001/04/xmlenc#sha256";
    public static final String SHA512XMLProtocol = "http://www.w3.org/2001/04/xmlenc#sha512";
    private static final String SHA1Protocol = "SHA-1";
    private static final String SHA256Protocol = "SHA-256";
    private static final String SHA512Protocol = "SHA-512";

    static {
        securityAllowedProtocol.put("http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1");
        securityAllowedProtocol.put("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256");
        securityAllowedProtocol.put("http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512");
        securityAllowedProtocol.put(null, "SHA-1");
    }

    public String getSigCertcalculatedHash() {
        return sigCertcalculatedHash;
    }

    public void setSigCertcalculatedHash(String sigCertcalculatedHash) {
        this.sigCertcalculatedHash = sigCertcalculatedHash;
    }

    public String getSigCertExpectedHash() {
        return sigCertExpectedHash;
    }

    public void setSigCertExpectedHash(String sigCertExpectedHash) {
        this.sigCertExpectedHash = sigCertExpectedHash;
    }

    public String getSigCertHashMethod() {
        return sigCertHashMethod;
    }

    public void setSigCertHashMethod(String sigCertHashMethod) {
        this.sigCertHashMethod = sigCertHashMethod;
    }

    public String getSigCertExpectedHashMethod() {
        return sigCertExpectedHashMethod;
    }

    public void setSigCertExpectedHashMethod(String sigCertExpectedHashMethod) {
        this.sigCertExpectedHashMethod = sigCertExpectedHashMethod;
    }

    public String getSigCertSerialNumber() {
        return sigCertSerialNumber;
    }

    public void setSigCertSerialNumber(String sigCertSerialNumber) {
        this.sigCertSerialNumber = sigCertSerialNumber;
    }

    public String getSigCertExpectedSerialNumber() {
        return sigCertExpectedSerialNumber;
    }

    public void setSigCertExpectedSerialNumber(String sigCertExpectedSerialNumber) {
        this.sigCertExpectedSerialNumber = sigCertExpectedSerialNumber;
    }

    public String getSigCertIssuerName() {
        return sigCertIssuerName;
    }

    public void setSigCertIssuerName(String sigCertIssuerName) {
        this.sigCertIssuerName = sigCertIssuerName;
    }

    public String getSigCertExpectedIssuerName() {
        return sigCertExpectedIssuerName;
    }

    public void setSigCertExpectedIssuerName(String sigCertExpectedIssuerName) {
        this.sigCertExpectedIssuerName = sigCertExpectedIssuerName;
    }

    public String getSigCertTargetAttribute() {
        return sigCertTargetAttribute;
    }

    public void setSigCertTargetAttribute(String sigCertTargetAttribute) {
        this.sigCertTargetAttribute = sigCertTargetAttribute;
    }

    public String getSigCertExpectedTargetAttribute() {
        return sigCertExpectedTargetAttribute;
    }

    public void setSigCertExpectedTargetAttribute(String sigCertExpectedTargetAttribute) {
        this.sigCertExpectedTargetAttribute = sigCertExpectedTargetAttribute;
    }

    public String getObjectFirstChild() {
        return objectFirstChild;
    }

    public void setObjectFirstChild(String objectFirstChild) {
        this.objectFirstChild = objectFirstChild;
    }

    public String getObjectExpectedFirstChild() {
        return objectExpectedFirstChild;
    }

    public void setObjectExpectedFirstChild(String objectExpectedFirstChild) {
        this.objectExpectedFirstChild = objectExpectedFirstChild;
    }

    public String getSigSecurityPolicyId() {
        return sigSecurityPolicyId;
    }

    public void setSigSecurityPolicyId(String sigSecurityPolicyId) {
        this.sigSecurityPolicyId = sigSecurityPolicyId;
    }

    public String getSigExpectedSecurityPolicyId() {
        return sigExpectedSecurityPolicyId;
    }

    public void setSigExpectedSecurityPolicyId(String sigExpectedSecurityPolicyId) {
        this.sigExpectedSecurityPolicyId = sigExpectedSecurityPolicyId;
    }

    public static Map<String, String> getSecurityAllowedProtocol() {
        return securityAllowedProtocol;
    }

    public String getSigExpectedSecurityPolicyIdHashMethod() {
        return sigExpectedSecurityPolicyIdHashMethod;
    }

    public void setSigExpectedSecurityPolicyIdHashMethod(String sigExpectedSecurityPolicyIdHashMethod) {
        this.sigExpectedSecurityPolicyIdHashMethod = sigExpectedSecurityPolicyIdHashMethod;
    }

    public String getSigSecurityPolicyIdHashMethod() {
        return sigSecurityPolicyIdHashMethod;
    }

    public void setSigSecurityPolicyIdHashMethod(String sigSecurityPolicyIdHashMethod) {
        this.sigSecurityPolicyIdHashMethod = sigSecurityPolicyIdHashMethod;
    }

    public String getSigExpectedSecurityPolicyIdHash() {
        return sigExpectedSecurityPolicyIdHash;
    }

    public void setSigExpectedSecurityPolicyIdHash(String sigExpectedSecurityPolicyIdHash) {
        this.sigExpectedSecurityPolicyIdHash = sigExpectedSecurityPolicyIdHash;
    }

    public SigningPolicy1 getSigningPolicy() {
        return signingPolicy;
    }

    private void setSigSecurityPolicy(SigningPolicy1 signingPolicy) {
        this.signingPolicy = signingPolicy;
    }

    public String getSigSecurityPolicyIdHash() {
        return sigSecurityPolicyIdHash;
    }

    public void setSigSecurityPolicyIdHash(String sigSecurityPolicyIdHash) {
        this.sigSecurityPolicyIdHash = sigSecurityPolicyIdHash;
    }

    public String getSigCertIssuerNameRFC2253() {
        return sigCertIssuerNameRFC2253;
    }

    public void setSigCertIssuerNameRFC2253(String sigCertIssuerNameRFC2253) {
        this.sigCertIssuerNameRFC2253 = sigCertIssuerNameRFC2253;
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.XadesInfoProcessor.XadesInfoProcessResult
 * Java Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */