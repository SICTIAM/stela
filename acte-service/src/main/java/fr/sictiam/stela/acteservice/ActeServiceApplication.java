package fr.sictiam.stela.acteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EntityScan(basePackageClasses = { ActeServiceApplication.class })
@SpringBootApplication
@EnableDiscoveryClient
public class ActeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActeServiceApplication.class, args);
    }
}
