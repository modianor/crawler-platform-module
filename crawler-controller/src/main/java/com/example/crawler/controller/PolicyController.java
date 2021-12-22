package com.example.crawler.controller;

import com.alibaba.fastjson.JSON;
import com.example.crawler.entity.Policy;
import com.example.crawler.service.IPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/policy")
public class PolicyController {
    private final Logger logger = LoggerFactory.getLogger(PolicyController.class);

    @Autowired
    private IPolicyService iPolicyService;

    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(path = "/getPolicys", method = RequestMethod.GET)
    @ResponseBody
    public String getPolicys(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        List<Policy> policys = iPolicyService.getPolicys();
        return JSON.toJSONString(policys);
    }

    @RequestMapping(path = "/getPolicyByPolicyId", method = RequestMethod.GET)
    @ResponseBody
    public String getPolicyByPolicyId(String policyId) {
        Policy policy = iPolicyService.getPolicyByPolicyId(policyId);
        return JSON.toJSONString(policy);
    }

    @RequestMapping(path = "/getPolicysByPolicyIdStr", method = RequestMethod.GET)
    @ResponseBody
    public String getPolicyByPolicyIdStr(String policyIdStr) {
        String[] policyIds = policyIdStr.split("\\|");
        List<Policy> policys = new ArrayList<>();
        for (String policyId : policyIds) {
            Policy policy = iPolicyService.getPolicyByPolicyId(policyId);
            policys.add(policy);
        }
        return JSON.toJSONString(policys);
    }

    @RequestMapping(path = "/getPolicById", method = RequestMethod.GET)
    @ResponseBody
    public String getPolicById(Integer id) {
        Policy policy = iPolicyService.getPolicById(id);
        return JSON.toJSONString(policy);
    }

    @RequestMapping(path = "/addPolicy", method = RequestMethod.POST)
    @ResponseBody
    public String addPolicy(Policy policy) {
        iPolicyService.addPolicy(policy);
        return JSON.toJSONString(policy);
    }

    @RequestMapping(path = "/updatePolicy", method = RequestMethod.POST)
    @ResponseBody
    public String updatePolicy(Policy policy) {
        iPolicyService.updatePolicy(policy);
        return JSON.toJSONString(policy);
    }

    @RequestMapping(path = "/deletePolicy", method = RequestMethod.POST)
    @ResponseBody
    public String deletePolicyByPolicyId(String policyId) {
        iPolicyService.deletePolicyByPolicyId(policyId);
        return "{\"status\":\"ok\"}";
    }
}
