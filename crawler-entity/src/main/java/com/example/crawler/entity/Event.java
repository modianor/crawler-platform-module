package com.example.crawler.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.ToString;

/**
 * 封装kafka事件类
 */
@ToString
public class Event {

    // 策略ID：policyId
    private String policyId;
    // 事件对应任务ID
    private String taskId;
    // 主题
    private String topic;
    // 事件类型 List Detail Data
    private String entityType;
    // 事件包含的Task
    private JSONObject task;
    // 事件包含的data
    private String data;

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    public Event setEntityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    public JSONObject getTask() {
        return task;
    }

    public Event setTask(JSONObject task) {
        this.task = task;
        return this;
    }

    public String getPolicyId() {
        return policyId;
    }

    public Event setPolicyId(String policyId) {
        this.policyId = policyId;
        return this;
    }

    public String getData() {
        return data;
    }

    public Event setData(String data) {
        this.data = data;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public Event setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }
}
