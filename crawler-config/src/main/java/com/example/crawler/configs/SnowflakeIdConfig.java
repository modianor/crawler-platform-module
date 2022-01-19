package com.example.crawler.configs;

import com.example.crawler.utils.SnowflakeIdWorker;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeIdConfig {
    @Bean
    public SnowflakeIdWorker getIdWorker() {
        return new SnowflakeIdWorker(0, 0);
    }
}
