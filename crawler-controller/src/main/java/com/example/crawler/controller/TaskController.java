package com.example.crawler.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Event;
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
    public String uploadTask(String task, String result) {
        JSONObject taskObj = JSON.parseObject(task);
        // 确认任务返回结果
        iTaskService.acknowledgeTask(taskObj);
        // 处理任务结果
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
        return "{\"status\":\"ok\"}";
    }

    @RequestMapping(path = "/uploadTaskData", method = RequestMethod.POST)
    @ResponseBody
    public String uploadTaskData(String task, String data) {
        JSONObject taskObj = JSON.parseObject(task);
        // 确认任务返回结果
        iTaskService.acknowledgeTask(taskObj);
        Event event = new Event()
                .setPolicyId(taskObj.getString("policyId"))
                .setTaskId(taskObj.getString("taskId"))
                .setEntityType("Detail")
                .setTopic("TP_BDG_AD_HEIMAOTOUSU_ORISTRUCT")
                .setTask(taskObj)
                .setData(data);
        eventProducer.fireEvent(event);
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
