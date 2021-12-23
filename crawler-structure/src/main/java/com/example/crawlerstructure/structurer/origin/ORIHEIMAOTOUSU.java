package com.example.crawlerstructure.structurer.origin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Event;
import com.example.crawler.utils.ZipUtil;
import com.example.crawlerstructure.event.EventProducer;
import com.example.crawlerstructure.structurer.BasicStructurer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class ORIHEIMAOTOUSU extends BasicStructurer {
    @Autowired
    private EventProducer eventProducer;
    public String oriTopic = "";
    public String bissTopic = "";
    public ORIHEIMAOTOUSU() {
        this.policyId = "HEIMAOTOUSU";
        this.oriTopic = this.getOriginStructureTopic();
        this.bissTopic = this.getBusinessStructureTopic();
    }

    @Override
    @KafkaListener(topics = {"TP_BDG_AD_HEIMAOTOUSU_ORISTRUCT"})
    public void doStructure(ConsumerRecord record) throws IOException {
        if (record == null || record.value() == null) {
            log.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误!");
            return;
        }
        String content = event.getData();
        Map<String, String> map = ZipUtil.unzip(content);
        JSONObject obj = JSONObject.parseObject(JSON.toJSONString(map));
        log.info("Kafka处理原始结构化Event:" + event.toString().substring(0, 1000));

        event.setTopic("TP_BDG_AD_HEIMAOTOUSU_BISSTRUCT");
        event.setData("");
        event.setObj(obj);
        eventProducer.fireEvent(event);
    }
}
