package com.example.crawler.dao;


import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Task;

import java.util.List;

public interface ITaskDao {
    Task pop_task(String spiderName);

    void pushTask(JSONObject task);

    JSONObject getTaskParam(String policyId);

    void pushProgressTask(JSONObject task);

    List<JSONObject> getProgressTasks();

    Boolean removeTask(String redisKey, JSONObject task);
}
