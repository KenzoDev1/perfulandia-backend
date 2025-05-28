package com.perfulandia.carritoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CarritoserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarritoserviceApplication.class, args);
	}

	@Bean // Define un bean RestTemplate para inyección de dependencias
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}
}