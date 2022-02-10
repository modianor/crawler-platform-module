package com.example.crawler.service.serviceImp;

import com.alibaba.fastjson.JSONObject;
import com.example.crawler.dao.IPolicyDao;
import com.example.crawler.entity.Policy;
import com.example.crawler.service.IPolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class PolicyService implements IPolicyService {
    @Resource
    private IPolicyDao iPolicyDao;

    @Override
    public List<Policy> getPolicys() {
        return iPolicyDao.getAllPolicy(0, 10);
    }

    @Override
    public Policy getPolicyByPolicyId(String policyId) {
        return iPolicyDao.getPolicyByPolicyId(policyId);
    }

    @Override
    public Policy getPolicById(Integer id) {
        return iPolicyDao.getPolicyById(id);
    }

    @Override
    public void addPolicy(Policy policy) {
        Policy p = this.getPolicyByPolicyId(policy.getPolicyId());
        if (p == null) {
            iPolicyDao.addPolicy(policy);
        }
    }

    @Override
    public void updatePolicy(Policy policy) {
        Policy p = this.getPolicyByPolicyId(policy.getPolicyId());
        if (p != null) {
            iPolicyDao.updatePolicy(policy);
        }
    }

    @Override
    public void deletePolicyByPolicyId(String policyId) {
        iPolicyDao.deletePolicyByPolicyId(policyId);
    }

    @Override
    public JSONObject getDataMappingByPolicyId(String policyId) {
        JSONObject dataMapping = new JSONObject();
        dataMapping.put("columnNames", "[\"code\"]");
        dataMapping.put("pkName", "uid");
        dataMapping.put("tableName", "BDG_DATA_HEIMAOTOUSU_COMPANY");
        dataMapping.put("update", true);
        return dataMapping;
    }
}
