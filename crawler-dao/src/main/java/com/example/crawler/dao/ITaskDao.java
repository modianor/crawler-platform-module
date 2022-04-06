package com.example.crawler.dao;


import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface ITaskDao {
    void pushTask(String redisKey, JSONObject task);

    void pushBatchTask(String redisKey, String[] tasks);

    JSONObject getTaskParam(String policyId);

    void pushProgressTask(JSONObject task);

    Map<String,JSONObject> getProgressTasks(String redisKey);

    Boolean removeProgessTask(String redisKey, JSONObject task);
}
