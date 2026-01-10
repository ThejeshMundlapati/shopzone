package com.shopzone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
// Removed @EnableMongoRepositories from here as it is now in the Main class
public class MongoConfig {
  // Enables @CreatedDate and @LastModifiedDate for MongoDB documents
}