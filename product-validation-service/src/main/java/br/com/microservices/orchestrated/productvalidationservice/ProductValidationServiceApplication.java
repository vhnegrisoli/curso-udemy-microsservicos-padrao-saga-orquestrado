package br.com.microservices.orchestrated.productvalidationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ProductValidationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductValidationServiceApplication.class, args);
	}

}
