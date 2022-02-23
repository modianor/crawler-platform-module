package com.example.crawler.dao.daoImp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.dao.ITaskDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.example.crawler.entity.Constant.*;

@Slf4j
@Repository
public class TaskDao implements ITaskDao {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void pushTask(String redisKey, JSONObject task) {
        if (redisKey == null) {
            String policyId = task.getString("policyId");
            String taskType = task.getString("taskType");
            redisKey = policyId + ":" + taskType;
        }
        redisTemplate.opsForList().leftPush(redisKey, task.toJSONString());
    }

    @Override
    public void pushBatchTask(String redisKey, String[] tasks) {
        redisTemplate.opsForList().leftPushAll(redisKey, tasks);
    }

    @Override
    public JSONObject getTaskParam(String policyId) {
        String redisListKey = policyId + ":" + REDIS_KEY_LIST_TASK;
        String redisDetailKey = policyId + ":" + REDIS_KEY_DETAIL_TASK;
        String redisDataKey = policyId + ":" + REDIS_KEY_DATA_TASK;
        Long dataTaskSize = redisTemplate.opsForList().size(redisDataKey);
        if (dataTaskSize != null && dataTaskSize > 0) {
            String jsonStr = (String) redisTemplate.opsForList().leftPop(redisDataKey);
            JSONObject task = JSON.parseObject(jsonStr);
            return task;
        } else {
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
        }
        return null;
    }

    @Override
    public void pushProgressTask(JSONObject task) {
        String policyId = task.getString("policyId");
        String redisKey = String.format("%s:%s", policyId, REDIS_KEY_IN_PROGRESS_TASK);
        redisTemplate.opsForList().rightPush(redisKey, task.toJSONString());
    }

    @Override
    public List<JSONObject> getProgressTasks(String redisKey) {
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
    public Boolean removeTask(String redisKey, JSONObject task) {
        if (redisKey == null) {
            String policyId = task.getString("policyId");
            String taskType = task.getString("taskType");
            redisKey = String.format("%s:%s", policyId, taskType);
        }

        Long num = redisTemplate.opsForList().remove(redisKey, 0, task.toJSONString());
        if (num != null) {
            return num > 0;
        } else {
            return false;
        }
    }
}
