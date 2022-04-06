package com.example.crawler.entity;

public class PolicyConfig {
    public Integer id;
    public String policyId;
    public String config;

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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "PolicyConfig{" +
                "id=" + id +
                ", policyId='" + policyId + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
