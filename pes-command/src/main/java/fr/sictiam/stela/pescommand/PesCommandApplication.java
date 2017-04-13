package fr.sictiam.stela.pescommand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class PesCommandApplication {

	public static void main(String[] args) {
		SpringApplication.run(PesCommandApplication.class, args);
	}
}
