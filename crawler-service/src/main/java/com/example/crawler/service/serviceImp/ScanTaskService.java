package com.example.crawler.service.serviceImp;

import com.alibaba.fastjson.JSONObject;
import com.example.crawler.dao.ITaskDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.example.crawler.entity.Constant.REDIS_KEY_IN_PROGRESS_TASK;
import static com.example.crawler.entity.Constant.TASK_KEY_IN_PROGRESS_TIME;

@Slf4j
@Component
public class ScanTaskService {
    @Autowired
    private ITaskDao iTaskDao;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0/5 * * * * ?")
    public void progressTaskScan() {
        log.info("扫描正在处理等待爬虫端返回结果的任务...");
        Set<String> keys = redisTemplate.keys(String.format("*:%s", REDIS_KEY_IN_PROGRESS_TASK));
        for (String redisKey : keys) {
            List<JSONObject> tasks = iTaskDao.getProgressTasks(redisKey);
            for (JSONObject task : tasks) {
                int inProgressTime = task.getInteger(TASK_KEY_IN_PROGRESS_TIME);
                int curTime = (int) (System.currentTimeMillis() / 1000);
                int deltaTime = curTime - inProgressTime;
                if (deltaTime > 600) {

                    Boolean status = iTaskDao.removeTask(null, task);
                    if (status) {
                        // 扫描到任务已经超时并且成功将任务移除
                        task.remove(TASK_KEY_IN_PROGRESS_TIME);
                        iTaskDao.pushTask(task);
                        log.info(String.format("任务超时[%s]秒,重新进入任务队列 [%S]", deltaTime, task.toJSONString()));
                    } else {
                        // 扫描到任务已经超时但是任务结果已经抢先确认任务结果 不做处理
                    }
                } else {
                    log.info(String.format("任务没有超时[%s]秒,继续等待任务结果", deltaTime));
                }
            }
        }

    }
}
