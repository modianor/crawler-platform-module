package com.example.crawler.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Status;
import com.example.crawler.service.ITaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.example.crawler.utils.TaskUtil.getTasksFromString;

@Controller
@RequestMapping("/task")
public class TaskController {
    private final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private ITaskService iTaskService;

    @RequestMapping(path = "/uploadTaskParams", method = RequestMethod.POST)
    @ResponseBody
    public String uploadTask(String task, Integer status, String result, @RequestParam("kibana_log") String kibanaLog) {
        JSONObject taskObj = JSON.parseObject(task);
        // 确认任务返回结果
        Boolean timeoutStatus = iTaskService.acknowledgeTask(taskObj);

        if (timeoutStatus) {
            taskObj.put("status", status);
            // 确认任务没有超时，根据任务类型，正常处理任务结果
            if (Status.SUCCESS.equals(status)) {
                // 逻辑层面上任务成功，处理返回的任务数据包
                iTaskService.handleUploadTask(taskObj, result);
            } else if (Status.None_State.equals(status)) {
                // 逻辑层面上任务为空包
            } else if (Status.FAIL.equals(status)) {
                // 逻辑层面上任务失败
                iTaskService.pushFailTask(taskObj);
            }
        } else {
            // 确认任务超时，修改任务状态，丢弃任务结果
            taskObj.put("status", Status.TIMEOUT);
            kibanaLog = "任务处理超时";
        }

        // 处理完任务状态和任务结果以后
        taskObj.put("kibana_log", kibanaLog);
        // 任务结果和日志推送到kafka
        iTaskService.pushCompletedTask(taskObj);
        // 2、设计实时任务状态监控，根据统计结果对策略进行报警
        // 3、通过消费kafka消息将任务状态和日志发送到elasticsearch
        return "{\"status\":\"ok\"}";
    }

    @RequestMapping(path = "/getTaskParams", method = RequestMethod.POST)
    @ResponseBody
    public String getTaskParams(@RequestParam(value = "policyIds") List<String> policyIds) {
        JSONArray tasks = iTaskService.getTaskParams(policyIds);
        return tasks.toJSONString();
    }

    @RequestMapping(path = "/generateTaskParam", method = RequestMethod.POST)
    @ResponseBody
    public String generateTaskParam(String taskParam) {
        JSONObject task = JSON.parseObject(taskParam);
        iTaskService.pushTask(task, true);
        return "{\"status\":\"ok\"}";
    }

    @RequestMapping(path = "/generateTaskSourceParam", method = RequestMethod.POST)
    @ResponseBody
    public String generateTaskSourceParam(String taskParam) {
        JSONObject task = JSON.parseObject(taskParam);
        iTaskService.pushTask(task, false);
        return "{\"status\":\"ok\"}";
    }

    @RequestMapping(path = "/generateTaskSourceParams", method = RequestMethod.POST)
    @ResponseBody
    public String generateTaskSourceParams(String taskParams) {
        List<JSONObject> tasks = getTasksFromString(taskParams);
        iTaskService.pushBatchTasks(tasks);
        return "{\"status\":\"ok\"}";
    }
}
