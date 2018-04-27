package fr.sictiam.stela.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

import java.util.TimeZone;

@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
@EnableCircuitBreaker
@ComponentScan(basePackages = { "org.oasis_eu.spring", "fr.sictiam.stela.apigateway" })
public class StelaApiGatewayApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    public static void main(String[] args) {
        SpringApplication.run(StelaApiGatewayApplication.class, args);
    }
}
