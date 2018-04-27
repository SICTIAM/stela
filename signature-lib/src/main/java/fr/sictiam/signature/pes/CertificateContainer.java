package fr.sictiam.signature.pes;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public abstract class CertificateContainer {
    public abstract Collection<X509Certificate> getAllCertificates();

    public static CertificateContainer fromZipURL(InputStream zipStream) throws IOException, CertificateException {
        List<X509Certificate> allCertificates = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        JarInputStream jarIS = new JarInputStream(zipStream);
        try {
            JarEntry entry = null;
            while ((entry = jarIS.getNextJarEntry()) != null) {
                if ((!entry.isDirectory()) && ((entry.getName().toUpperCase().endsWith(".CER"))
                        || (entry.getName().toUpperCase().endsWith(".CRT"))
                        || (entry.getName().toUpperCase().endsWith(".DER")))) {
                    X509Certificate certificate = (X509Certificate) cf.generateCertificate(jarIS);
                    allCertificates.add(certificate);
                }
            }
        } finally {
            jarIS.close();
        }
        return new SimpleCertificateContainer1(allCertificates);
    }

    public static CertificateContainer fromCollection(Collection<X509Certificate> certificates) {
        return new SimpleCertificateContainer1(certificates);
    }

    public KeyStore buildJKSKeyStore()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = CertificateUtils.createEmptyJKSKeyStore();
        for (X509Certificate certificate : getAllCertificates()) {
            keyStore.setCertificateEntry(certificate.getSubjectDN().getName(), certificate);
        }
        return keyStore;
    }

    public CertStore buildCertStore() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        return CertStore.getInstance("Collection", new CollectionCertStoreParameters(getAllCertificates()));
    }

    public Collection<X509Certificate> extractRootCertificates() {
        List<X509Certificate> toReturn = new ArrayList<>();
        for (X509Certificate certificate : getAllCertificates()) {
            if (CertificateUtils.isSelfSigned(certificate)) {
                toReturn.add(certificate);
            }
        }
        return toReturn;
    }

    public Collection<X509Certificate> extractNotRootCertificates() {
        List<X509Certificate> toReturn = new ArrayList<>();
        for (X509Certificate certificate : getAllCertificates()) {
            if (!CertificateUtils.isSelfSigned(certificate)) {
                toReturn.add(certificate);
            }
        }
        return toReturn;
    }

    public Collection<X509Certificate> extractValidCertificates(Date date) {
        List<X509Certificate> toReturn = new ArrayList<>();
        for (X509Certificate certificate : getAllCertificates()) {
            try {
                certificate.checkValidity(date);
                toReturn.add(certificate);
            } catch (CertificateExpiredException localCertificateExpiredException) {
            } catch (CertificateNotYetValidException localCertificateNotYetValidException) {
            }
        }
        return toReturn;
    }

    public boolean contains(X509Certificate certificate) throws CertificateEncodingException {
        for (X509Certificate certificateItem : getAllCertificates()) {
            if (certificate.equals(certificateItem)) {
                return true;
            }
        }
        return false;
    }

    public static class SimpleCertificateContainer1 extends CertificateContainer {
        private Collection<X509Certificate> allCertificates;

        public SimpleCertificateContainer1(Collection<X509Certificate> allCertificates) {
            this.allCertificates = allCertificates;
        }

        @Override
        public Collection<X509Certificate> getAllCertificates() {
            return allCertificates;
        }
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.CertificateContainer Java
 * Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */