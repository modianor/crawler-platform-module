package com.example.crawlerstructure.structurer;

import com.example.crawler.entity.Event;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

public abstract class BasicStructurer {
    public String policyId = "";

    public BasicStructurer() {
    }

    public void verify() {

    }

    public void doStructure(ConsumerRecord record) throws IOException {

    }

    public String getOriginStructureTopic() {
        return String.format("TP_BDG_AD_%s_ORISTRUCT", this.policyId);
    }

    public String getBusinessStructureTopic() {
        return String.format("TP_BDG_AD_%s_BISSTRUCT", this.policyId);
    }

}
