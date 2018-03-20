package fr.sictiam.signature.pes.producer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class WindowsKeyStoreLoader extends AbstractKeyStoreLoader {
    @Override
    public void loadKeystore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            NoSuchProviderException {
        KeyStore ks = KeyStore.getInstance("Windows-MY", "SunMSCAPI");

        ks.load(null, null);
        _fixAliases(ks);
        setKeyStore(ks);
    }

    private static void _fixAliases(KeyStore keyStore) {
        try {
            Field field = keyStore.getClass().getDeclaredField("keyStoreSpi");
            field.setAccessible(true);
            KeyStoreSpi keyStoreVeritable = (KeyStoreSpi) field.get(keyStore);
            if ("sun.security.mscapi.KeyStore$MY".equals(keyStoreVeritable.getClass().getName())) {
                field = keyStoreVeritable.getClass().getEnclosingClass().getDeclaredField("entries");
                field.setAccessible(true);
                Collection<?> entries = (Collection<?>) field.get(keyStoreVeritable);
                for (Object entry : entries) {
                    field = entry.getClass().getDeclaredField("certChain");
                    field.setAccessible(true);
                    X509Certificate[] certificates = (X509Certificate[]) field.get(entry);

                    String hashCode = Integer.toString(certificates[0].hashCode());

                    field = entry.getClass().getDeclaredField("alias");
                    field.setAccessible(true);
                    String alias = (String) field.get(entry);
                    if (!alias.equals(hashCode)) {
                        field.set(entry, alias.concat(" - ").concat(hashCode));
                    }
                }
            }
        } catch (Exception exception) {
            System.err.println(exception);
            exception.printStackTrace();
        }
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.producer.WindowsKeyStoreLoader Java Class Version: 6
 * (50.0) JD-Core Version: 0.7.1
 */