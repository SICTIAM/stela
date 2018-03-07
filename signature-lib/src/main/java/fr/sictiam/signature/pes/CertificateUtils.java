package fr.sictiam.signature.pes;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CertificateUtils {
    public static boolean isSelfSigned(X509Certificate certificate) {
        return certificate.getIssuerDN().equals(certificate.getSubjectDN());
    }

    public static KeyStore createEmptyJKSKeyStore()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        return createEmptyKeyStore("JKS");
    }

    public static KeyStore createEmptyKeyStore(String type)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        return ks;
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.CertificateUtils Java Class
 * Version: 6 (50.0) JD-Core Version: 0.7.1
 */