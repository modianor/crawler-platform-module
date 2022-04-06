package com.example.crawler.dao;

import com.example.crawler.entity.PolicyExtension;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IPolicyExtensionDao {
    PolicyExtension getPolicyExtensionByPolicyId(String policyId);
}
