package com.example.crawler.dao;


import com.example.crawler.entity.Policy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IPolicyDao {
    void addPolicy(Policy policy);

    void updatePolicy(Policy policy);

    void deletePolicyById(Integer id);

    void deletePolicyByPolicyId(String policyId);

    Policy getPolicyById(Integer id);

    Policy getPolicyByPolicyId(String policyId);

    List<Policy> getAllPolicy(int offset, int limit);
}
