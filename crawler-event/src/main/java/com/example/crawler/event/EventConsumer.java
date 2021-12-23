package com.example.crawler.event;

import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Event;
import com.example.crawler.service.ITaskService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {
    private static final String TOPIC_LIST = "TP_BDG_AD_Task_List";
    private static final String TOPIC_Detail = "TP_BDG_AD_HEIMAOTOUSU_ORISTRUCT";
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private ITaskService iTaskService;

    // 消费List Data任务
    @KafkaListener(topics = {TOPIC_LIST})
    public void handleListDataMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        iTaskService.pushTask(event.getTask());
        logger.info("Kafka处理Event:" + event.toString());
    }

    // 消费Detail任务数据
    /*@KafkaListener(topics = {TOPIC_Detail})
    public void handleDetailMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        logger.info("Kafka处理Event:" + event.toString().substring(0, 1000));
    }*/
}
