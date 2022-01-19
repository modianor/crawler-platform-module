package com.example.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrawlerServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerServerApplication.class, args);
    }
}
