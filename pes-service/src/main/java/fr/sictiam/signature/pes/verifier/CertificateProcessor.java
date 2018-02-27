package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.pes.CertificateContainer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.signature.XMLSignature;

import javax.security.auth.x500.X500Principal;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.RSAKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

public class CertificateProcessor {

    public CertificatInformation1 process(CertificateContainer trustStore, XMLSignature xmlSignature,
            SignatureVerifierResult signatureVerificationResult, CertificateContainer interTrustStore)
            throws Exception {
        List<X509Certificate> certificates = extractCertificates(xmlSignature);
        if (certificates.isEmpty()) {
            throw new Exception();
        }
        return process(trustStore, certificates, signatureVerificationResult, interTrustStore);
    }

    public CertificatInformation1 process(CertificateContainer trustStore, List<X509Certificate> certificates,
            SignatureVerifierResult signatureVerificationResult, CertificateContainer interTrustStore)
            throws Exception {
        if (certificates.isEmpty()) {
            throw new Exception();
        }
        CertificatInformation1 certificatInformation = new CertificatInformation1(certificates);

        List<X509Certificate> validatedCertPath = new ArrayList<>();
        PKIXCertPathBuilderResult builderRes = null;
        X509CertSelector certSelector = new X509CertSelector();
        certSelector.setCertificate(certificates.iterator().next());
        Date refDate = (signatureVerificationResult != null) && (signatureVerificationResult.getSigningDate() != null)
                ? signatureVerificationResult.getSigningDate()
                : new Date();
        try {
            try {
                PKIXBuilderParameters builderParams = new PKIXBuilderParameters(CertificateContainer
                        .fromCollection(trustStore.extractValidCertificates(refDate)).buildJKSKeyStore(), certSelector);
                builderParams.setRevocationEnabled(false);
                builderParams.setMaxPathLength(-1);
                builderParams.setDate(refDate);

                try {
                    builderRes = (PKIXCertPathBuilderResult) CertPathBuilder.getInstance("PKIX").build(builderParams);
                    validatedCertPath
                            .addAll((Collection<? extends X509Certificate>) builderRes.getCertPath().getCertificates());
                    validatedCertPath.add(builderRes.getTrustAnchor().getTrustedCert());
                    certificatInformation.setValidatedCertPath(validatedCertPath);
                } catch (CertPathBuilderException cpbe) {
                    certificatInformation.setCertPathBuilderException(cpbe);
                }
            } catch (IOException ex) {
                PKIXBuilderParameters builderParams;
                throw new UnExpectedException(ex);
            } catch (CertificateException ex) {
                throw new UnExpectedException(ex);
            }

        } catch (KeyStoreException kse) {
            throw new UnExpectedException(kse);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new UnExpectedException(iape);
        } catch (NoSuchAlgorithmException nsae) {
            throw new UnExpectedException(nsae);
        }
        certificatInformation.setAuthorizedCertPath(authorizedCertPath(certificatInformation.getValidatedCertPath(),
                CertificateContainer.fromCollection(interTrustStore.extractValidCertificates(refDate))));
        certificatInformation.setSignCertAuthorized(certSignAuthorized(certificatInformation.getValidatedCertPath()));
        return certificatInformation;
    }

    private boolean authorizedCertPath(List<X509Certificate> validatedCertPath, CertificateContainer interTrustStore) {
        if ((validatedCertPath != null) && (interTrustStore != null)) {
            try {
                Enumeration<String> enumAliases = interTrustStore.buildJKSKeyStore().aliases();
                while (enumAliases.hasMoreElements()) {
                    if (validatedCertPath
                            .contains(interTrustStore.buildJKSKeyStore().getCertificate(enumAliases.nextElement()))) {
                        return true;
                    }
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(CertificateProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                java.util.logging.Logger.getLogger(CertificateProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateException ex) {
                java.util.logging.Logger.getLogger(CertificateProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyStoreException ex) {
                java.util.logging.Logger.getLogger(CertificateProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    private boolean certSignAuthorized(List<X509Certificate> validatedCertPath) {
        if (validatedCertPath != null) {
            for (int i = 1; i < validatedCertPath.size(); i++) {
                X509Certificate certificate = validatedCertPath.get(i);
                if ((certificate.getKeyUsage() == null) || (certificate.getKeyUsage().length <= 5)) {
                    return false;
                }
                if (Boolean.FALSE.equals(certificate.getKeyUsage()[5])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private List<X509Certificate> extractCertificates(XMLSignature xmlSignature) throws UnExpectedException {
        List<X509Certificate> certificates = new ArrayList<>();
        if (xmlSignature.getKeyInfo() != null) {
            try {
                X509Data x509Data = xmlSignature.getKeyInfo().itemX509Data(0);
                if (x509Data != null) {
                    for (int i = 0; i < x509Data.lengthCertificate(); i++) {
                        certificates.add(x509Data.itemCertificate(i).getX509Certificate());
                    }
                }
            } catch (XMLSecurityException xmlse) {
                throw new UnExpectedException(xmlse);
            }
        }
        return certificates;
    }

    public static class CertificatInformation1 {
        private List<X509Certificate> signingCertificates;
        private List<X509Certificate> validatedCertPath;
        private CertPathBuilderException certPathBuilderException;
        private boolean authorizedCertPath;
        private boolean signCertAuthorized;

        public boolean isSignCertAuthorized() {
            return signCertAuthorized;
        }

        public void setSignCertAuthorized(boolean signCertAuthorized) {
            this.signCertAuthorized = signCertAuthorized;
        }

        public boolean isAuthorizedCertPath() {
            return authorizedCertPath;
        }

        public void setAuthorizedCertPath(boolean authorizedCertPath) {
            this.authorizedCertPath = authorizedCertPath;
        }

        public CertificatInformation1(List<X509Certificate> signingCertificates) {
            this.signingCertificates = signingCertificates;
        }

        public X509Certificate getSigningCertificate() {
            return signingCertificates.iterator().next();
        }

        public String getSignataireCN() {
            X500Principal principal = getSigningCertificate().getSubjectX500Principal();
            String rfc2253PrincipalName = principal.getName("RFC2253");
            StringTokenizer st = new StringTokenizer(rfc2253PrincipalName, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int idx = token.indexOf("CN=");
                if (idx >= 0) {
                    return token.substring(idx + 3);
                }
            }
            return null;
        }

        public String getSigningPublicKeyDescription() {
            return getSigningCertificate().getPublicKey().getAlgorithm() + " (" + getSigningPublicKeyStrength()
                    + " bits)";
        }

        public Integer getSigningPublicKeyStrength() {
            PublicKey publicKey = getSigningCertificate().getPublicKey();
            if ((publicKey instanceof RSAKey)) {
                return Integer.valueOf(((RSAKey) publicKey).getModulus().bitLength());
            }
            if ((publicKey instanceof DSAKey)) {
                return Integer.valueOf(((DSAKey) publicKey).getParams().getP().bitLength());
            }
            throw new IllegalArgumentException("PublicKey type not supported : " + publicKey.getClass().getName());
        }

        public CertPathBuilderException getCertPathBuilderException() {
            return certPathBuilderException;
        }

        public void setCertPathBuilderException(CertPathBuilderException certPathBuilderException) {
            this.certPathBuilderException = certPathBuilderException;
        }

        public List<X509Certificate> getValidatedCertPath() {
            return validatedCertPath;
        }

        public void setValidatedCertPath(List<X509Certificate> validatedCertPath) {
            this.validatedCertPath = validatedCertPath;
        }

        public boolean isBasicConstraintCritical() {
            boolean isCritical = true;
            if (getSigningCertificate().getBasicConstraints() == -1) {
                isCritical = false;
            }
            String BasicConstraintsExtensionOID = "2.5.29.19";
            Set<?> nonCritSet = getSigningCertificate().getNonCriticalExtensionOIDs();
            Iterator<?> i;
            if (nonCritSet != null) {
                for (i = nonCritSet.iterator(); i.hasNext();) {
                    String oid = (String) i.next();
                    if (BasicConstraintsExtensionOID.equals(oid)) {
                        isCritical = false;
                    }
                }
            }
            return isCritical;
        }
    }

}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.CertificateProcessor Java Class Version: 6
 * (50.0) JD-Core Version: 0.7.1
 */