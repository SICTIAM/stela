package fr.sictiam.stela;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PesApplication.class, args);
	}
}
