package fr.sictiam.stela.acteservice.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MiatTemplateConfig {

    @Value("${application.miat.keystorepasswd}")
    private String keystorePassword;
    
    @Value("${application.miat.truststorepasswd}")
    private String truststorePassword;

    @Bean
    @Qualifier("miatRestTemplate")
    public RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
            IOException, java.security.cert.CertificateException, UnrecoverableKeyException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(
                new FileInputStream(new File(
                        MiatTemplateConfig.class.getResource("/certificates/stela.sictiam2015.fr.p12").getPath())),
                keystorePassword.toCharArray());
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadKeyMaterial(keyStore, keystorePassword.toCharArray())
                .loadTrustMaterial(
                        new File(MiatTemplateConfig.class.getResource("/certificates/truststore.jks").getPath()),
                        truststorePassword.toCharArray(), acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }
}
