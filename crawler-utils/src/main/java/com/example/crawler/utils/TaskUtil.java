package com.example.crawler.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskUtil {
    public static String dump_task(Task task) {
        return JSON.toJSONString(task);
    }

    public static Task load_task(String task_json) {
        return JSON.parseObject(task_json, Task.class);
    }

    public static List<JSONObject> getTasksFromString(String taskString) {
        List<JSONObject> tasks = new ArrayList<>();
        JSONArray taskParams = (JSONArray) JSON.parse(taskString);
        for (int i = 0; i < taskParams.size(); i++) {
            JSONArray params = taskParams.getJSONArray(i);
            String urlSign = params.getString(0);
            String companyName = params.getString(1);
            String creditCode = params.getString(2);
            String taskInfo = params.getString(3);

            String[] policyIdAndTaskType = taskInfo.split("\\|");
            String policyId = policyIdAndTaskType[0];
            String taskType = policyIdAndTaskType[1];

            JSONObject task = new JSONObject();
            task.put("policyId", policyId);
            task.put("taskType", taskType);
            task.put("urlSign", urlSign);
            task.put("companyName", companyName);
            task.put("creditCode", creditCode);
            tasks.add(task);
        }
        return tasks;
    }
}
