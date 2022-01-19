package com.example.crawler.dao.daoImp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.entity.Task;
import com.example.crawler.dao.ITaskDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class TaskDao implements ITaskDao {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Task pop_task(String spiderName) {
        String taskKey = spiderName + ":" + "requests";
        Task task = (Task) redisTemplate.opsForList().leftPop(taskKey);
        /*Set<Object> objs = redisTemplate.opsForZSet().range(taskKey, 0, 1);

        if (objs != null && objs.size() > 0) {
            for (Object obj : objs) {
                Task task = (Task) (obj);
                log.info("pop task: " + task);
                //redisTemplate.opsForZSet().removeRange(taskKey, 0, 1);
                return task;
            }
        }*/
        return task;
    }

    @Override
    public void pushTask(JSONObject task) {
        String policyId = task.getString("policyId");
        String taskType = task.getString("taskType");
        String redisKey = policyId + ":" + taskType;
        redisTemplate.opsForList().leftPush(redisKey, task.toJSONString());
    }

    @Override
    public JSONObject getTaskParam(String policyId) {
        String redisListKey = policyId + ":List";
        String redisDetailKey = policyId + ":Detail";
        Long detailTaskSize = redisTemplate.opsForList().size(redisDetailKey);
        if (detailTaskSize != null && detailTaskSize > 0) {
            String jsonStr = (String) redisTemplate.opsForList().leftPop(redisDetailKey);
            JSONObject task = JSON.parseObject(jsonStr);
            return task;
        } else {
            Long listTaskSize = redisTemplate.opsForList().size(redisListKey);
            if (listTaskSize != null && listTaskSize > 0) {
                String jsonStr = (String) redisTemplate.opsForList().leftPop(redisListKey);
                JSONObject task = JSON.parseObject(jsonStr);
                return task;
            }
        }
        return null;
    }

    @Override
    public void pushProgressTask(JSONObject task) {
        String redisKey = "CRAWLER_IN_PROGRESS_TASKS";
        redisTemplate.opsForList().rightPush(redisKey, task.toJSONString());
    }

    @Override
    public List<JSONObject> getProgressTasks() {
        String redisKey = "CRAWLER_IN_PROGRESS_TASKS";
        Long size = redisTemplate.opsForList().size(redisKey);
        List<JSONObject> tasks = new ArrayList<>();
        List<Object> taskObjs = redisTemplate.opsForList().range(redisKey, 0, size);
        for (Object taskObj : taskObjs) {
            String taskJsonStr = (String) taskObj;
            JSONObject task = JSON.parseObject(taskJsonStr);
            tasks.add(task);
        }
        return tasks;
    }

    @Override
    public void removeTask(String redisKey,JSONObject task) {
        redisTemplate.opsForList().remove(redisKey, 0, task.toJSONString());
    }
}
