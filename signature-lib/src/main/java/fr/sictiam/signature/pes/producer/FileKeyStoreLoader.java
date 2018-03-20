package fr.sictiam.signature.pes.producer;

import fr.sictiam.signature.pes.verifier.UnExpectedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class FileKeyStoreLoader extends AbstractKeyStoreLoader {
    private File keyStoreFile;
    private static Map<String, String> keyStoreType = new HashMap<>();

    static {
        keyStoreType.put(null, "PKCS12");
        keyStoreType.put("", "PKCS12");
        keyStoreType.put("p12", "PKCS12");
        keyStoreType.put("pfx", "PKCS12");
        keyStoreType.put("jks", "JKS");
        keyStoreType.put("ks", "JKS");
    }

    public FileKeyStoreLoader(File keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    @Override
    public void loadKeystore() throws UnExpectedException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, NoSuchProviderException {
        FileInputStream fis = null;
        try {
            String keyStoreFileExtension = keyStoreFile.getName().substring(keyStoreFile.getName().lastIndexOf(".") + 1)
                    .toLowerCase();
            if (keyStoreType.containsKey(keyStoreFileExtension)) {
                KeyStore ks = KeyStore.getInstance(keyStoreType.get(keyStoreFileExtension));
                fis = new FileInputStream(keyStoreFile);
                ks.load(fis, password);
                setKeyStore(ks);
            } else {
                throw new KeyStoreException();
            }
        } catch (FileNotFoundException ex) {
            throw new UnExpectedException(ex);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    public File getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(File keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.producer.FileKeyStoreLoader
 * Java Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */