package com.example.crawler.entity;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Policy {

    /*# 业务配置类
    # 业务策略名称
    # 消重服务
    # 是否启用代理
    # 每次请求间隔
    # 请求超时时间
    # 请求重试次数
    # 策略持有的任务类型List|Detail|Data
    # 策略对应插件的子线程(默认为0)
    # 策略对应插件的子线程任务队列上限(默认为1)*/

    // 主键ID
    public Integer id;
    // 策略ID
    public String policyId;
    // 策略名称
    public String policyName;
    // 策略状态
    public Boolean policyState;
    // 所属集群
    public String clusterId;
    // 消重服务ID
    public String deduplicationServerId;
    // 策略支持的任务类型
    public String taskTypes;
    // 过期时间
    public Integer periodTime = 180;
    // List任务结果抓取表达式
    public String listExpress;
    // Data任务抓取表达式
    public String dataExpress;
    // 策略任务重次数
    public Integer retryTimes;
    // 是否启用代理
    public Boolean proxy;
    // 策略任务请求等待时间
    public Float interval;
    // 策略任务请求超时时间
    public Float timeout;

    /***
     * 这下面的属性属于主机对爬虫策略的设置
     * 暂时不放入Policy表中

     // 策略对应爬虫端插件子线程数
     public Integer childThread = 0;
     // 策略对应爬虫端插件任务队列大小
     public Integer taskQueueSize = 1;*/

    /*public Policy() {
    }*/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Boolean getPolicyState() {
        return policyState;
    }

    public void setPolicyState(Boolean policyState) {
        this.policyState = policyState;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getDeduplicationServerId() {
        return deduplicationServerId;
    }

    public void setDeduplicationServerId(String deduplicationServerId) {
        this.deduplicationServerId = deduplicationServerId;
    }

    public String getTaskTypes() {
        return taskTypes;
    }

    public void setTaskTypes(String taskTypes) {
        this.taskTypes = taskTypes;
    }

    public Integer getPeriodTime() {
        return periodTime;
    }

    public void setPeriodTime(Integer periodTime) {
        this.periodTime = periodTime;
    }

    public String getListExpress() {
        return listExpress;
    }

    public void setListExpress(String listExpress) {
        this.listExpress = listExpress;
    }

    public String getDataExpress() {
        return dataExpress;
    }

    public void setDataExpress(String dataExpress) {
        this.dataExpress = dataExpress;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Boolean getProxy() {
        return proxy;
    }

    public void setProxy(Boolean proxy) {
        this.proxy = proxy;
    }

    public Float getInterval() {
        return interval;
    }

    public void setInterval(Float interval) {
        this.interval = interval;
    }

    public Float getTimeout() {
        return timeout;
    }

    public void setTimeout(Float timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
