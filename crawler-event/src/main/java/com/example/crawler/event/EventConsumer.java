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

    @Autowired
    private EventProducer eventProducer;

    // 消费List Data任务
    @KafkaListener(topics = {TOPIC_LIST})
    public void handleListDataMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        iTaskService.pushTask(event.getTask(), true);
        logger.info("Kafka处理Event:" + event.toString());
    }

    // 消费已完成的任务数据
    @KafkaListener(topics = {"TP_BDG_AD_COMPLETED_TASK"})
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
        Event visualEvent = new Event()
                .setPolicyId(task.getString("policyId"))
                .setTaskId(task.getString("taskId"))
                .setEntityType(task.getString("taskType"))
                .setTopic("TP_BDG_AD_VISUAL_COMPLETED_TASK")
                .setTask(task);
        eventProducer.fireEvent(visualEvent);
        logger.info("Kafka处理Event:" + event);
    }
}
