package fr.sictiam.stela.pes.dgfip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class PesDgFiPSenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PesDgFiPSenderApplication.class, args);
	}
}
