package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.pes.verifier.CertificateProcessor.CertificatInformation1;
import fr.sictiam.signature.pes.verifier.SignatureTypeCalculator.SignatureAnexInfo1;
import fr.sictiam.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.SignatureAndRefsVerificationResult1;
import fr.sictiam.signature.pes.verifier.XadesInfoExtractor.XadesInfo1;
import fr.sictiam.signature.pes.verifier.XadesInfoProcessor.XadesInfoProcessResult1;
import org.apache.xml.security.signature.XMLSignature;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

public class SignatureVerifierResult {
    private XMLSignature xmlSignature;
    private XadesInfo1 xadesInfo;
    private Exception UnverifiableSignatureException;
    private Exception xadesExtractionException;
    private CertificatInformation1 certificatInformation;
    private Exception certificateProcessException;
    private SignatureAndRefsVerificationResult1 signatureAndRefsVerificationResult;
    private XadesInfoProcessResult1 xadesInfoProcessResult;
    private SignatureAnexInfo1 signatureAnexInfo;
    private List<String> listeBordereauxNonSignes;
    private boolean signatureGlobalePresente = false;

    public XMLSignature getXmlSignature() {
        return xmlSignature;
    }

    void setXmlSignature(XMLSignature xmlSignature) {
        this.xmlSignature = xmlSignature;
    }

    public XadesInfo1 getXadesInfo() {
        return xadesInfo;
    }

    void setXadesInfo(XadesInfo1 xadesInfo) {
        this.xadesInfo = xadesInfo;
    }

    public Date getSigningDate() {
        return (getXadesInfo1() != null) && (getXadesInfo1().getSigningTime() != null)
                ? getXadesInfo1().getSigningTime().getTime()
                : null;
    }

    public Exception getXadesExtractionException() {
        return xadesExtractionException;
    }

    void setXadesExtractionException(Exception xadesExtractionException) {
        this.xadesExtractionException = xadesExtractionException;
    }

    public CertificatInformation1 getCertificatInformation() {
        return certificatInformation;
    }

    void setCertificatInformation(CertificatInformation1 certificatInformation) {
        this.certificatInformation = certificatInformation;
    }

    public Exception getCertificateProcessException() {
        return certificateProcessException;
    }

    void setCertificateProcessException(Exception certificateProcessException) {
        this.certificateProcessException = certificateProcessException;
    }

    public SignatureAndRefsVerificationResult1 getSignatureAndRefsVerificationResult() {
        return signatureAndRefsVerificationResult;
    }

    void setSignatureAndRefsVerificationResult(SignatureAndRefsVerificationResult1 signatureAndRefsVerificationResult) {
        this.signatureAndRefsVerificationResult = signatureAndRefsVerificationResult;
    }

    public X509Certificate getSigningCertificate() {
        return certificatInformation != null ? certificatInformation.getSigningCertificate() : null;
    }

    public XadesInfoProcessResult1 getXadesInfoProcessResult() {
        return xadesInfoProcessResult;
    }

    void setXadesInfoProcessResult(XadesInfoProcessResult1 xadesInfoProcessResult) {
        this.xadesInfoProcessResult = xadesInfoProcessResult;
    }

    public SignatureAnexInfo1 getSignatureAnexInfo() {
        return signatureAnexInfo;
    }

    public void setSignatureAnexInfo(SignatureAnexInfo1 signatureAnexInfo) {
        this.signatureAnexInfo = signatureAnexInfo;
    }

    public void setListeBordereauxNonSignes(List<String> lst) {
        listeBordereauxNonSignes = lst;
    }

    public List<String> getListeBordereauxNonSignes() {
        return listeBordereauxNonSignes;
    }

    public boolean isSignatureGlobalePresente() {
        return signatureGlobalePresente;
    }

    public void setSignatureGlobalePresente(boolean signatureGlobalePresente) {
        this.signatureGlobalePresente = signatureGlobalePresente;
    }

    public Exception getUnverifiableSignatureException() {
        return UnverifiableSignatureException;
    }

    public void setUnverifiableSignatureException(Exception UnverifiableSignatureException) {
        this.UnverifiableSignatureException = UnverifiableSignatureException;
    }

    public XadesInfo1 getXadesInfo1() {
        // TODO Auto-generated method stub
        return xadesInfo;
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.SignatureVerifierResult Java Class Version:
 * 6 (50.0) JD-Core Version: 0.7.1
 */