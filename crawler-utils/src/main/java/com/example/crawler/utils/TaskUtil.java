package com.example.crawler.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskUtil {

    private static final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(0, 0);

    public static String dump_task(Task task) {
        return JSON.toJSONString(task);
    }

    public static Task load_task(String task_json) {
        return JSON.parseObject(task_json, Task.class);
    }

    /***
     * 根据爬虫任务返回的结果生成子任务
     * @param parentTask 已被处理完的任务
     * @param taskString 已被处理完的任务的结果
     * @return 已被处理完的任务生成的子任务
     */
    public static List<JSONObject> getTasksFromString(JSONObject parentTask, String taskString) {
        List<JSONObject> tasks = new ArrayList<>();
        String parentTaskType = parentTask.getString("taskType");
        String parentTaskId = parentTask.getString("taskId");

        JSONArray taskParams = (JSONArray) JSON.parse(taskString);

        if ("List".equals(parentTaskType)) {
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
                task.put("taskId", Long.toString(snowflakeIdWorker.nextId()));
                task.put("parentTaskId", parentTaskId);
                task.put("policyId", policyId);
                task.put("taskType", taskType);
                task.put("urlSign", urlSign);
                task.put("companyName", companyName);
                task.put("creditCode", creditCode);
                tasks.add(task);
            }
            return tasks;
        } else if ("Data".equals(parentTaskType)) {
            List<JSONObject> maps = new ArrayList<>();
            for (int i = 0; i < taskParams.size(); i++) {
                JSONObject params = (JSONObject) taskParams.get(i);
                maps.add(params);
            }
            return maps;
        }
        return null;


    }

    public static List<JSONObject> getTasksFromString(String taskString) {
        List<JSONObject> taskObjs = new ArrayList<>();
        JSONArray taskOriginParams = (JSONArray) JSONArray.parse(taskString);
        for (int i = 0; i < taskOriginParams.size(); i++) {
            JSONObject taskParams = (JSONObject) taskOriginParams.get(i);
            String params = taskParams.getString("Params");
            JSONObject obj = JSONObject.parseObject(params);
            String progress = taskParams.getString("Progress");
            String policyId = taskParams.getString("PolicyId");
            String loadOrder = taskParams.getString("LoadOrder");
            String taskType = taskParams.getString("TaskType");
            obj.put("taskId", Long.toString(snowflakeIdWorker.nextId()));
            obj.put("parentTaskId", null);
            obj.put("policyId", policyId);
            obj.put("taskType", taskType);
            taskObjs.add(obj);
        }
        return taskObjs;
    }
}