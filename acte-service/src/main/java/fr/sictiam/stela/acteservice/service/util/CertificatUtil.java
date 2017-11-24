package fr.sictiam.stela.acteservice.service.util;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;

public class CertificatUtil {

    private static String CERTS_ROOT_DIR = "/home/benoit/dev/sictiam/stela-poc/acte-service/src/main/resources/certificates/";

    private static RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException, java.security.cert.CertificateException, UnrecoverableKeyException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File(CERTS_ROOT_DIR + "stela.sictiam2015.fr.p12")), "changeit".toCharArray());

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadKeyMaterial(keyStore, "changeit".toCharArray())
                .loadTrustMaterial(new File(CERTS_ROOT_DIR + "truststore.jks"), "changeit".toCharArray(), acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }

    /**
     * Référence commande curl qui marche sur la PF de pré-prod STELA :
     *     curl -L -v --cert stela.sictiam2015.fr.pem --cacert ca.pem -k \
     *          "https://217.109.5.5:6030/E60TSSIC?user=E60TSSIC&password=h_k/EWFG3f8i"
     *
     * Le p12 client contient notre certificat et sa clé, ainsi que toute la chaine de certification ChamberSign
     * (tous ne sont probablement pas nécessaires)
     *
     * Le truststore contient le certificat du serveur ainsi que sa chaine de certification
     */
    public static void main(String[] args) {
        try {

            System.setProperty("javax.net.debug", "all");

            getRestTemplate().getForObject("https://217.109.5.5:6030/E60TSSIC?user=E60TSSIC&password=h_k/EWFG3f8i", Object.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}