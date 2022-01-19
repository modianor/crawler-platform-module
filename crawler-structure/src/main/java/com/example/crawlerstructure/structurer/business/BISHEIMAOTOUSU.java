package com.example.crawlerstructure.structurer.business;

import cn.wanghaomiao.xpath.model.JXDocument;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Event;
import com.example.crawlerstructure.structurer.BasicStructurer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jsoup.nodes.Element;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class BISHEIMAOTOUSU extends BasicStructurer {
    @Override
    @KafkaListener(topics = {"TP_BDG_AD_HEIMAOTOUSU_BISSTRUCT"})
    public void doStructure(ConsumerRecord<String, String> record) throws IOException {
        if (record == null || record.value() == null) {
            log.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误!");
            return;
        }
        JSONObject obj = event.getObj();
        JSONObject data = this.parse(obj);
        log.info("Kafka处理业务结构化Event:" + data);
    }

    public JSONObject parse(JSONObject obj) {
        JSONObject data = new JSONObject();
        String content = obj.getString("index.html");
        String titleXpath = "//h1[@class=\"article\"]/text()";
        String nameXpath = "//div[@class='ts-q-user clearfix']/span[@class='u-name']/text()";
        String idXpath = "//li/label[contains(text(),\"投诉编号\")]/parent::li/allText()";
        String targetXpath = "//li/label[contains(text(),\"投诉对象\")]/parent::li/a/allText()";
        String problemXpath = "//li/label[contains(text(),\"投诉问题\")]/parent::li/allText()";
        String appealXpath = "//li/label[contains(text(),\"投诉要求\")]/parent::li/allText()";
        String amountXpath = "//li/label[contains(text(),\"涉诉金额\")]/parent::li/allText()";
        String statusXpath = "//li/label[contains(text(),\"投诉进度\")]/parent::li/b/allText()";
        JXDocument jxDocument = new JXDocument(content);

        Object rs = jxDocument.selOne(titleXpath);
        data.put("title", ((Element) rs).text());

        rs = jxDocument.selOne(nameXpath);
        data.put("username", ((Element) rs).text());

        rs = jxDocument.selOne(idXpath);
        if (rs != null)
            data.put("complaint_id", rs);

        rs = jxDocument.selOne(targetXpath);
        if (rs != null)
            data.put("complaint_target", rs);

        rs = jxDocument.selOne(problemXpath);
        if (rs != null)
            data.put("complaint_problem", rs);

        rs = jxDocument.selOne(appealXpath);
        if (rs != null)
            data.put("complaint_appeal", rs);

        rs = jxDocument.selOne(amountXpath);
        if (rs != null)
            data.put("amount_involved", rs);

        rs = jxDocument.selOne(statusXpath);
        if (rs != null)
            data.put("status", rs);

        return data;
    }
}
