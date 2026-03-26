package com.shopzone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = "com.shopzone.repository.jpa")
@EnableMongoRepositories(basePackages = "com.shopzone.repository.mongo")
public class ShopzoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopzoneApplication.class, args);
	}

}