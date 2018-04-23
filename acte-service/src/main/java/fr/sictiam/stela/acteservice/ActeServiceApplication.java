package fr.sictiam.stela.acteservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

import java.util.TimeZone;

@EntityScan(basePackageClasses = { ActeServiceApplication.class })
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableAsync
@EnableRabbit
public class ActeServiceApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ActeServiceApplication.class, args);
    }
}
