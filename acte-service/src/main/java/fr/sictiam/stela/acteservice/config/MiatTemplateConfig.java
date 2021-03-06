package fr.sictiam.stela.acteservice.config;

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
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

@Configuration
public class MiatTemplateConfig {

    @Value("${application.miat.keystorepasswd}")
    private String keystorePassword;

    @Value("${application.miat.truststorepasswd}")
    private String truststorePassword;

    @Value("${application.miat.truststorepath}")
    private String trustStorePath;

    @Value("${application.miat.keystorepath}")
    private String keyStorePath;

    @Value("${application.miat.accessTimeout}")
    private int accessTimeout;

    @Bean
    @Qualifier("miatRestTemplate")
    public RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
            IOException, java.security.cert.CertificateException, UnrecoverableKeyException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(ResourceUtils.getFile(keyStorePath)), keystorePassword.toCharArray());
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadKeyMaterial(keyStore, keystorePassword.toCharArray())
                .loadTrustMaterial(ResourceUtils.getFile(trustStorePath), truststorePassword.toCharArray(),
                        acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectionRequestTimeout(accessTimeout);
        requestFactory.setConnectTimeout(accessTimeout);

        return new RestTemplate(requestFactory);
    }
}
