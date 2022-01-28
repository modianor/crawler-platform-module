package com.example.crawlerstructure.structurer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

import static com.example.crawler.entity.Constant.TOPIC_BIS_TEMPLATE;
import static com.example.crawler.entity.Constant.TOPIC_ORI_TEMPLATE;

public abstract class BasicStructurer {
    public String policyId = "";

    public BasicStructurer() {
    }

    public void verify() {

    }

    public void doStructure(ConsumerRecord<String, String> record) throws IOException {

    }

    public String getOriginStructureTopic() {
        return String.format(TOPIC_ORI_TEMPLATE, this.policyId);
    }

    public String getBusinessStructureTopic() {
        return String.format(TOPIC_BIS_TEMPLATE, this.policyId);
    }

}
