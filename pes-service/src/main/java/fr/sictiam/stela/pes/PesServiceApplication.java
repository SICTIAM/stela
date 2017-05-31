package fr.sictiam.stela.pes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PesServiceApplication.class, args);
	}
}
