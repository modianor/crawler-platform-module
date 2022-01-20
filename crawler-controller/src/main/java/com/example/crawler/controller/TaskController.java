package com.example.crawler.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Event;
import com.example.crawler.entity.Status;
import com.example.crawler.entity.Task;
import com.example.crawler.event.EventProducer;
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
import java.util.Objects;

import static com.example.crawler.utils.TaskUtil.getTasksFromString;

@Controller
@RequestMapping("/task")
public class TaskController {
    private final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private ITaskService iTaskService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/uploadTaskParams", method = RequestMethod.POST)
    @ResponseBody
    public String uploadTask(String task, Integer status, String result, @RequestParam("kibana_log") String kibanaLog) {
        JSONObject taskObj = JSON.parseObject(task);
        // 确认任务返回结果
        Boolean timeoutStatus = iTaskService.acknowledgeTask(taskObj);
        if (timeoutStatus) {
            // 确认任务没有超时，正常处理任务结果
            taskObj.put("status", status);
            if (Objects.equals(status, Status.SUCCESS)) {
                // 任务处理成功且包含有效数据
                List<JSONObject> childTasks = getTasksFromString(taskObj, result);
                for (JSONObject childTask : childTasks) {
                    Event event = new Event()
                            .setPolicyId(taskObj.getString("policyId"))
                            .setTaskId(taskObj.getString("taskId"))
                            .setEntityType("List")
                            .setTopic("TP_BDG_AD_Task_List")
                            .setTask(childTask);
                    eventProducer.fireEvent(event);
                }
            } else if (Objects.equals(status, Status.FAIL)) {
                // 任务处理失败
                // 可以考虑将策略的失败任务存储起来，设置自动重试任务
            } else if (Objects.equals(status, Status.None_State)) {
                // 任务处理完成但不包含有效数据
            }
        } else {
            // 修改任务状态为超时，丢弃任务结果
            taskObj.put("status", Status.TIMEOUT);
        }


        // 处理完任务状态和任务结果以后
        taskObj.put("kibana_log", kibanaLog);
        // 1、任务结果和日志推送到kafka
        Event event = new Event()
                .setPolicyId(taskObj.getString("policyId"))
                .setTaskId(taskObj.getString("taskId"))
                .setEntityType(taskObj.getString("taskType"))
                .setTopic("TP_BDG_AD_COMPLETED_TASK")
                .setTask(taskObj);
        eventProducer.fireEvent(event);
        // 2、通过消费kafka消息将任务状态和日志发送到elasticsearch
        // 3、设计实时任务状态监控，根据统计结果对策略进行报警
        return "{\"status\":\"ok\"}";
    }

    @RequestMapping(path = "/uploadTaskData", method = RequestMethod.POST)
    @ResponseBody
    public String uploadTaskData(String task, Integer status, String data, @RequestParam("kibana_log") String kibanaLog) {
        JSONObject taskObj = JSON.parseObject(task);
        // 确认任务没有超时，正常处理任务结果
        Boolean timeoutStatus = iTaskService.acknowledgeTask(taskObj);
        if (timeoutStatus) {
            Event event = new Event()
                    .setPolicyId(taskObj.getString("policyId"))
                    .setTaskId(taskObj.getString("taskId"))
                    .setEntityType("Detail")
                    .setTopic("TP_BDG_AD_HEIMAOTOUSU_ORISTRUCT")
                    .setTask(taskObj)
                    .setData(data);
            eventProducer.fireEvent(event);
        } else {
            // 修改任务状态为超时，丢弃任务结果
            taskObj.put("status", status);
        }
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


    @RequestMapping(path = "/getTask", method = RequestMethod.GET)
    @ResponseBody
    public Task getTask(@RequestParam("spider_name") String spiderName) {
        return iTaskService.pop_task(spiderName);
    }
}
