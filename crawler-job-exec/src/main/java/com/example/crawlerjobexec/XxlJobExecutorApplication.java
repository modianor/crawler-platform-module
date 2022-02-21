package com.example.crawlerjobexec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author xuxueli 2018-10-28 00:38:13
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.crawler.configs", "com.example.crawler.service", "com.example.crawlerjobexec", "com.example.crawler.event"})
@MapperScan(basePackages = {"com.example.crawler.dao"})
public class XxlJobExecutorApplication {

    public static void main(String[] args) {
        SpringApplication.run(XxlJobExecutorApplication.class, args);
    }

}