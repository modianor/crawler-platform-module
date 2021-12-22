package com.example.crawler.service.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.dao.ITaskDao;
import com.example.crawler.entity.Task;
import com.example.crawler.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TaskService implements ITaskService {

    @Autowired
    private ITaskDao iTaskDao;

    @Override
    public Task pop_task(String spiderName) {
        return iTaskDao.pop_task(spiderName);
    }

    @Override
    public void pushTasks(List<JSONObject> tasks) {
        for (JSONObject task : tasks) {
            iTaskDao.pushTask(task);
        }
    }

    @Override
    public void pushTask(JSONObject task) {
        iTaskDao.pushTask(task);
    }

    @Override
    public JSONArray getTaskParams(List<String> policyIds) {
        JSONArray tasks = new JSONArray();
        for (String policyId : policyIds) {
            JSONObject task = iTaskDao.getTaskParam(policyId);
            tasks.add(task);
        }
        return tasks;
    }
}
