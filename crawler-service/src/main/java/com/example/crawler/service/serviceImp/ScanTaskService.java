package com.example.crawler.service.serviceImp;

import com.alibaba.fastjson.JSONObject;
import com.example.crawler.dao.ITaskDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ScanTaskService {
    @Autowired
    private ITaskDao iTaskDao;

    @Scheduled(cron = "0/5 * * * * ?")
    public void progressTaskScan() {
        log.info("扫描正在处理等待爬虫端返回结果的任务...");
        List<JSONObject> tasks = iTaskDao.getProgressTasks();
        for (JSONObject task : tasks) {
            int inProgressTime = task.getInteger("in_progress_time");
            int curTime = (int) (System.currentTimeMillis() / 1000);
            int deltaTime = curTime - inProgressTime;
            if (deltaTime > 600) {
                Boolean status = iTaskDao.removeTask("CRAWLER_IN_PROGRESS_TASKS", task);
                if (status) {
                    // 扫描到任务已经超时并且成功将任务移除
                    task.remove("in_progress_time");
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
