package fr.sictiam.stela.apigateway;

import fr.sictiam.stela.apigateway.filter.PreLoggingFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
public class StelaApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(StelaApiGatewayApplication.class, args);
	}

	@Bean
    public PreLoggingFilter preLoggingFilter() { return new PreLoggingFilter(); }
}
