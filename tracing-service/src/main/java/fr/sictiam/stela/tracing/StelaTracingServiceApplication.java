package fr.sictiam.stela.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.zipkin.stream.EnableZipkinStreamServer;

@SpringBootApplication
@EnableZipkinStreamServer
public class StelaTracingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StelaTracingServiceApplication.class, args);
	}
}
