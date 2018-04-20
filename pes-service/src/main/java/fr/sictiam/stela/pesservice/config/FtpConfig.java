package fr.sictiam.stela.pesservice.config;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;

@Configuration
public class FtpConfig {

    @Value("${application.ftp.host}")
    private String host;

    @Value("${application.ftp.port}")
    private Integer port;

    @Value("${application.ftp.username}")
    private String username;

    @Value("${application.ftp.password}")
    private String password;

    @Bean
    @Primary
    public DefaultFtpSessionFactory sf() {
        DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
        sf.setHost(host);
        sf.setPort(port);
        sf.setUsername(username);
        sf.setPassword(password);
        sf.setClientMode(FTPClient.PASSIVE_REMOTE_DATA_CONNECTION_MODE);
        return sf;
    }
}
