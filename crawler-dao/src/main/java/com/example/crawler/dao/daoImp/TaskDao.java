package com.example.crawler.dao.daoImp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.dao.ITaskDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String taskId = task.getString("taskId");
        String redisKey = String.format("%s:%s", policyId, REDIS_KEY_IN_PROGRESS_TASK);
        redisTemplate.opsForHash().put(redisKey, taskId, task.toJSONString());
    }

    @Override
    public Map<String, JSONObject> getProgressTasks(String redisKey) {
        Long size = redisTemplate.opsForHash().size(redisKey);
        Map<String, JSONObject> tasks = new HashMap<>();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
        for (Object key : entries.keySet()) {
            String value = (String) entries.get(key);
            JSONObject task = JSONObject.parseObject(value);
            tasks.put((String) key, task);
        }
        return tasks;
    }

    @Override
    public Boolean removeProgessTask(String redisKey, JSONObject task) {
        if (redisKey == null) {
            String policyId = task.getString("policyId");
            String taskType = task.getString("taskType");
            redisKey = String.format("%s:%s", policyId, taskType);
        }
        String taskId = task.getString("taskId");
        Long num = redisTemplate.opsForHash().delete(redisKey, taskId);
        return num > 0;
    }
}
