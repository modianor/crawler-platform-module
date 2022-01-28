package com.example.crawler.event;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogStashUtil {
    public void sendTaskToLogstash(JSONObject task) {
        log.info(task.toString());
    }
}