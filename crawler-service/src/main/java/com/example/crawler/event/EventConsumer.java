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

import static com.example.crawler.entity.Constant.*;

@Component
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private ITaskService iTaskService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LogStashUtil logStashUtil;

    // 消费List任务
    @KafkaListener(topics = {TOPIC_TASK_LIST})
    public void handleListMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        JSONObject task = event.getTask();
        String taskType = task.getString("taskType");
        String policyMode = task.getString("policyMode");
        if ("plugin".equals(policyMode)) {
            // 策略模式为通用插件爬虫
            iTaskService.pushTask(null, event.getTask(), true);
        } else if ("config".equals(policyMode)) {
            // 策略模式为通用配置爬虫
            String redisKey = String.format("%s:%s", "NORMAL", taskType);
            iTaskService.pushTask(redisKey, event.getTask(), true);
        }
        logger.info("Kafka处理Event:" + event.toString());
    }

    // 消费Data任务
    @KafkaListener(topics = {TOPIC_TASK_DATA})
    public void handleDataMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        iTaskService.handleDataTask(event.getTask());
        logger.info("Kafka处理Event:" + event.toString());
    }

    // 消费Fail任务
    @KafkaListener(topics = {TOPIC_TASK_FAIL})
    public void handleFailMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        // 失败任务插入到对应策略下的FAIL列表中,可以配置重试任务
        JSONObject task = event.getTask();
        String policyId = task.getString("policyId");
        String redisKey = String.format("%s:%s", policyId, REDIS_KEY_FAIL_TASK);
        iTaskService.pushTask(redisKey, event.getTask());
        logger.info("Kafka处理Event:" + event.toString());
    }

    // 消费已完成的任务数据
    @KafkaListener(topics = {TOPIC_COMPLETED_TASK})
    public void handleDetailMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        JSONObject task = event.getTask();

        // 通过消费kafka消息将任务状态和日志发送到logstash
        /*Event visualEvent = new Event()
                .setPolicyId(task.getString("policyId"))
                .setTaskId(task.getString("taskId"))
                .setEntityType(task.getString("taskType"))
                .setTopic("TP_BDG_AD_VISUAL_COMPLETED_TASK")
                .setTask(task);
        eventProducer.fireEvent(visualEvent);*/
        logStashUtil.sendTaskToLogstash(task);
        logger.info("Kafka处理Event:" + event);
    }
}