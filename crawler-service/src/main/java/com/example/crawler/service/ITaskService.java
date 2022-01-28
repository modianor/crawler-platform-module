package com.example.crawler.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface ITaskService {
    void pushTask(JSONObject task, Boolean duplication);

    Boolean doDeduplication(JSONObject task);

    Boolean isTurnOnDeduplication(JSONObject task);

    Boolean updateDeduplication(JSONObject task, String taskMd5);

    Boolean isDuplication(JSONObject task, String taskMd5);

    String getDeduplicationFields(JSONObject task);

    JSONArray getTaskParams(List<String> policyIds);

    Boolean acknowledgeTask(JSONObject taskObj);

    void handleUploadTask(JSONObject parentTask, String result);

    void pushCompletedTask(JSONObject taskObj);

    void handleDataTask(JSONObject task);
}
