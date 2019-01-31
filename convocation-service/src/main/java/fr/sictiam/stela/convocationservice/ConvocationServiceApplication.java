package fr.sictiam.stela.convocationservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;

import java.util.TimeZone;

@EntityScan(basePackageClasses = { ConvocationServiceApplication.class })
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableRabbit
public class ConvocationServiceApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ConvocationServiceApplication.class, args);
    }
}
