package com.shopzone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.StandardCharsets;

/**
 * Configuration for email functionality.
 * Enables async processing and configures Thymeleaf for email templates.
 */
@Configuration
@EnableAsync
public class MailConfig {

  /**
   * Template resolver specifically for email templates.
   * Looks in classpath:/templates/email/ directory.
   */
  @Bean
  public SpringResourceTemplateResolver emailTemplateResolver() {
    SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
    resolver.setPrefix("classpath:/templates/email/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resolver.setCacheable(false);  // Set to true in production
    resolver.setOrder(1);
    resolver.setCheckExistence(true);
    return resolver;
  }

  /**
   * Template engine for processing email templates.
   */
  @Bean
  public SpringTemplateEngine emailTemplateEngine() {
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(emailTemplateResolver());
    return engine;
  }
}