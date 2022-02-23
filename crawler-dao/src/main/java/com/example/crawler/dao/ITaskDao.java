package com.example.crawler.dao;


import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface ITaskDao {
    void pushTask(String redisKey, JSONObject task);

    void pushBatchTask(String redisKey, String[] tasks);

    JSONObject getTaskParam(String policyId);

    void pushProgressTask(JSONObject task);

    List<JSONObject> getProgressTasks(String redisKey);

    Boolean removeTask(String redisKey, JSONObject task);
}
