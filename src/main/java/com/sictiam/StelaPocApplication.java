package com.sictiam;

import org.axonframework.spring.config.EnableAxon;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAxon
public class StelaPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(StelaPocApplication.class, args);
	}
}
