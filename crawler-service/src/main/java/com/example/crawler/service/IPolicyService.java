package com.example.crawler.service;


import com.example.crawler.entity.Policy;

import java.util.List;

public interface IPolicyService {
    List<Policy> getPolicys();

    Policy getPolicyByPolicyId(String policyId);

    Policy getPolicById(Integer id);

    void addPolicy(Policy policy);

    void updatePolicy(Policy policy);

    void deletePolicyByPolicyId(String policyId);
}
