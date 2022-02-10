package com.example.crawler.service.serviceImp;

import com.alibaba.fastjson.JSONObject;
import com.example.crawler.dao.ITaskDao;
import com.example.crawler.service.ITaskService;
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
    private ITaskService iTaskService;

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
                if (deltaTime > 60 * 60) {
                    // IN_PROGRESS_TASKS队列里面的任务已经等待超过60分钟 被判定为爬虫端任务丢失
                    // 尝试去移除已经超时60分钟的任务
                    Boolean status = iTaskService.acknowledgeTask(task);
                    if (status) {
                        // 扫描到任务已经超时并且成功将任务移除
                        task.remove(TASK_KEY_IN_PROGRESS_TIME);
                        // 想了想,超时任务只汇报状态,不加入任务队列做重试
                        // 过年期间有仔细想了想,任务超时包括两种情况:
                        // 情况一、爬虫端正常处理了爬虫任务并且返回了爬虫任务结果,只是处理过程有点缓慢,被爬虫服务端判定为处理超时
                        // 情况二、爬虫端因为异常导致爬虫端并未向爬虫管理平台返回爬虫任务状态以及结果,爬虫任务一直在IN_PROGRESS_TASKS等待
                        // 情况一一般发生在情况二之前，所以若发生情况一，则爬虫任务被标记为超时，不再重新进入任务队列重试(爬虫任务超时机制)
                        // 若发生情况二,则爬虫任务被标记为丢失，需要重新加入到任务队列等待重新被处理(爬虫任务防丢机制)
                        iTaskDao.pushTask(null, task);
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
