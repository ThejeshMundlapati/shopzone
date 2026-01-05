package com.shopzone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.shopzone.repository")
public class MongoConfig {
  // Enables @CreatedDate and @LastModifiedDate for MongoDB documents
}