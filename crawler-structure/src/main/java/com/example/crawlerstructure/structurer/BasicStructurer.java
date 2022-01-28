package com.example.crawlerstructure.structurer;

import com.example.crawler.entity.Event;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

import static com.example.crawler.entity.Constant.*;

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
        return String.format(TOPIC_BISS_TEMPLATE, this.policyId);
    }

}
