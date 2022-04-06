package com.example.crawler.dao;

import com.example.crawler.entity.PolicyConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IPolicyConfigDao {
    PolicyConfig getPolicyConfigByPolicyId(String policyId);
}
