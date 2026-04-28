package com.shopzone.searchservice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.shopzone.searchservice", "com.shopzone.common"})
@EnableAsync
public class SearchServiceApplication {
    public static void main(String[] args) { SpringApplication.run(SearchServiceApplication.class, args); }
}
