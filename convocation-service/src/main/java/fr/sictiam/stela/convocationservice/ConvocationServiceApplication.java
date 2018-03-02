package fr.sictiam.stela.convocationservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EntityScan(basePackageClasses = { ConvocationServiceApplication.class })
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableAsync
@EnableRabbit
public class ConvocationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConvocationServiceApplication.class, args);
    }
}
