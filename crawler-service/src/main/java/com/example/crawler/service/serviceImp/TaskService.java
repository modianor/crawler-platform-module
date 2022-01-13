package com.example.crawler.service.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.configs.ElasticSearchConfig;
import com.example.crawler.dao.ITaskDao;
import com.example.crawler.entity.Task;
import com.example.crawler.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class TaskService implements ITaskService {

    @Autowired
    private ITaskDao iTaskDao;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

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
    public String getDeduplicationFields(JSONObject task) {
        return "urlSign";
    }

    @Override
    public Boolean isTurnOnDeduplication(JSONObject task) {
        // 检查任务对应的爬虫策略是否开启消重
        String policyId = task.getString("policyId");
        return true;
    }

    @Override
    public Boolean isDuplication(JSONObject task, String taskMd5) {
        String policyId = task.getString("policyId");
        //1.创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices(policyId.toLowerCase(Locale.ROOT));
        //指定DSL，检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.1 构造检索条件
        searchSourceBuilder.query(QueryBuilders.termQuery("taskSignature", taskMd5));
        searchRequest.source(searchSourceBuilder);
        //2.执行检索
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            return searchHits.length > 0;
        } catch (IOException e) {
            log.error("爬虫任务消重服务查询失败, 错误原因:{}", e.toString());
            return false;
        }

    }


    @Override
    public Boolean doDeduplication(JSONObject task) {
        Boolean isTurnOn = isTurnOnDeduplication(task);
        if (isTurnOn) {
            String fields = getDeduplicationFields(task);
            if ("None".equals(fields)) {
                // 开启消重服务但是消重字段为None，并不打算消重
                return false;
            } else {
                String[] fieldArr = fields.split("\\|");
                // 根据策略消重规则进行消重
                StringBuilder stringBuffer = new StringBuilder();
                for (String field : fieldArr) {
                    String value = task.getString(field);
                    stringBuffer.append(value);
                }
                String taskMd5 = DigestUtils.md5DigestAsHex(stringBuffer.toString().getBytes());
                return isDuplication(task, taskMd5);
            }
        } else {
            // 不开启消重或者消重字段为None
            return false;
        }

    }

    @Override
    public void pushTask(JSONObject task) {
        // 根据消重结果判断是否进入爬虫任务队列
        Boolean exist = doDeduplication(task);
        if (!exist) {
            iTaskDao.pushTask(task);
        }
    }

    @Override
    public JSONArray getTaskParams(List<String> policyIds) {
        JSONArray tasks = new JSONArray();
        for (String policyId : policyIds) {
            JSONObject task = iTaskDao.getTaskParam(policyId);
            if (task != null) {
                tasks.add(task);
                task.put("in_progress_time", (int) (System.currentTimeMillis() / 1000));
                iTaskDao.pushProgressTask(task);
            }
        }
        return tasks;
    }
}
