package com.udacity.boogle.maps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class BoogleMapsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoogleMapsApplication.class, args);
	}

}
