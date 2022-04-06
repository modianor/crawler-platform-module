package com.example.crawler.entity;

public class PolicyExtension {
    public Integer id;
    public String policyId;
    public String processName;
    public String policyMode;

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

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getPolicyMode() {
        return policyMode;
    }

    public void setPolicyMode(String policyMode) {
        this.policyMode = policyMode;
    }

    @Override
    public String toString() {
        return "PolicyExtension{" +
                "id=" + id +
                ", policyId='" + policyId + '\'' +
                ", processName='" + processName + '\'' +
                ", policyMode='" + policyMode + '\'' +
                '}';
    }
}
