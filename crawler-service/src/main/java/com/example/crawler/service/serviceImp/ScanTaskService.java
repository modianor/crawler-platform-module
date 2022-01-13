package com.example.crawler.service.serviceImp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScanTaskService {
    @Scheduled(cron = "*/15 * * * * ?")
    public void progressTaskScan() {

    }
}
