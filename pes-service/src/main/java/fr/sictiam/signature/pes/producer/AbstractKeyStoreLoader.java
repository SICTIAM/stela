package fr.sictiam.signature.pes.producer;

import fr.sictiam.signature.pes.verifier.UnExpectedException;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractKeyStoreLoader {
    private KeyStore keyStore;
    protected char[] password = null;
    private List<char[]> keyPasswordList = new ArrayList<char[]>();

    public List<CertAliases1> recupCertList() {
        List<CertAliases1> returnList = new ArrayList<CertAliases1>();
        if (keyStore != null) {
            try {
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    if (keyStore.isCertificateEntry(alias)) {
                        returnList.add(new CertAliases1(keyStore.getCertificate(alias), alias));
                    }
                }
            } catch (KeyStoreException ex) {
                throw new UnExpectedException(ex);
            }
        }
        return returnList;
    }

    public List<String> recupPrivateKeyAliasList() {
        List<String> returnList = new ArrayList<String>();
        if (keyStore != null) {
            try {
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    if (keyStore.isKeyEntry(alias)) {
                        returnList.add(alias);
                    }
                }
            } catch (KeyStoreException ex) {
                throw new UnExpectedException(ex);
            }
        }
        return returnList;
    }

    public KeyAliases1 recupPrivateKey(String alias, char[] password) {
        try {
            if (keyStore.isKeyEntry(alias)) {
                try {
                    return new KeyAliases1(keyStore.getKey(alias, password), alias,
                            keyStore.getCertificateChain(alias));
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(AbstractKeyStoreLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnrecoverableKeyException ex) {
                    Logger.getLogger(AbstractKeyStoreLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (KeyStoreException ex) {
            Logger.getLogger(AbstractKeyStoreLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public abstract void loadKeystore() throws UnExpectedException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, NoSuchProviderException;

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore aKeyStore) {
        keyStore = aKeyStore;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public List<char[]> getKeyPasswordList() {
        return keyPasswordList;
    }

    public void setKeyPasswordList(List<char[]> keyPasswordList) {
        this.keyPasswordList = keyPasswordList;
    }

    public class CertAliases1 {
        private Certificate certificate;
        private String alias;

        public CertAliases1(Certificate certificate, String alias) {
            this.certificate = certificate;
            this.alias = alias;
        }

        public Certificate getCertificate() {
            return certificate;
        }

        public void setCertificate(Certificate certificate) {
            this.certificate = certificate;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }
    }

    public class KeyAliases1 {
        private Key key;
        private String alias;
        private Certificate[] certChain;

        public KeyAliases1(Key key, String alias, Certificate[] certChain) {
            this.key = key;
            this.alias = alias;
            this.certChain = certChain;
        }

        public Certificate getUserCert() {
            if ((certChain != null) && (certChain.length != 0)) {
                return certChain[0];
            }
            return null;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public Key getKey() {
            return key;
        }

        public void setKey(Key key) {
            this.key = key;
        }

        public Certificate[] getCertChain() {
            return certChain;
        }

        public void setCertChain(Certificate[] certChain) {
            this.certChain = certChain;
        }
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.producer.AbstractKeyStoreLoader Java Class Version: 6
 * (50.0) JD-Core Version: 0.7.1
 */