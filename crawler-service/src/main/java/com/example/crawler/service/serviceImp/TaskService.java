package com.example.crawler.service.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.configs.ElasticSearchConfig;
import com.example.crawler.dao.IDataItemDao;
import com.example.crawler.dao.ITaskDao;
import com.example.crawler.entity.Constant;
import com.example.crawler.entity.Event;
import com.example.crawler.entity.Task;
import com.example.crawler.event.EventProducer;
import com.example.crawler.service.IPolicyService;
import com.example.crawler.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
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
import java.util.Map;

import static com.example.crawler.entity.Constant.*;
import static com.example.crawler.utils.TaskUtil.getTasksFromString;

@Slf4j
@Service
public class TaskService implements ITaskService {

    @Autowired
    private ITaskDao iTaskDao;

    @Autowired
    private IDataItemDao iDataItemDao;

    @Autowired
    private IPolicyService iPolicyService;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private EventProducer eventProducer;

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
    public Boolean updateDeduplication(JSONObject task, String taskMd5) {
        String policyId = task.getString("policyId");
        //指定索引和id
        JSONObject obj = new JSONObject();
        obj.put(TASK_SIGNATURE, taskMd5);
        IndexRequest request = new IndexRequest(policyId.toLowerCase(Locale.ROOT));
        request.source(obj.toJSONString(), XContentType.JSON);
        //执行保存操作
        IndexResponse indexResponse = null;
        try {
            indexResponse = restHighLevelClient.index(request, ElasticSearchConfig.COMMON_OPTIONS);
            log.info("重复任务进入消重服务, {}", indexResponse.toString());
        } catch (IOException e) {
            log.error("重复任务进入消重服务错误, {}", e.toString());
            return false;
        }
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
        searchSourceBuilder.query(QueryBuilders.termQuery(TASK_SIGNATURE, taskMd5));
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
                Boolean exsit = isDuplication(task, taskMd5);
                if (!exsit) {
                    updateDeduplication(task, taskMd5);
                }
                return exsit;
            }
        } else {
            // 不开启消重或者消重字段为None
            return false;
        }

    }

    @Override
    public void pushTask(JSONObject task, Boolean duplication) {
        // 根据消重结果判断是否进入爬虫任务队列
        Boolean exist = doDeduplication(task);
        if (duplication) {
            // 爬虫端生成子任务，需要进入消重流程
            if (!exist) {
                iTaskDao.pushTask(task);
                log.info(String.format("爬虫子任务不需要消重:[%s]", task.toJSONString()));
            } else {
                log.warn(String.format("爬虫子任务已被消重:[%s]", task.toJSONString()));
            }
        } else {
            // 任务来源程序生成任务，不需要进行消重
            iTaskDao.pushTask(task);
            log.info(String.format("任务来源程序生成任务，不需要消重:[%s]", task.toJSONString()));
        }

    }

    @Override
    public Boolean acknowledgeTask(JSONObject task) {
        String policyId = task.getString("policyId");
        String redisKey = String.format("%s:%s", policyId, REDIS_KEY_IN_PROGRESS_TASK);
        return iTaskDao.removeTask(redisKey, task);
    }

    @Override
    public JSONArray getTaskParams(List<String> policyIds) {
        JSONArray tasks = new JSONArray();
        for (String policyId : policyIds) {
            JSONObject task = iTaskDao.getTaskParam(policyId);
            if (task != null) {
                task.put(TASK_KEY_IN_PROGRESS_TIME, (int) (System.currentTimeMillis() / 1000));
                tasks.add(task);
                iTaskDao.pushProgressTask(task);
            }
        }
        return tasks;
    }

    @Override
    public void handleUploadTask(JSONObject parentTask, String result) {
        String policyId = parentTask.getString("policyId");
        String taskType = parentTask.getString("taskType");
        String taskId = parentTask.getString("taskId");
        if ("List".equals(taskType)) {
            // List任务可以生成子任务参数，任务类型包括三种：List、Detail、Data
            List<JSONObject> childTasks = getTasksFromString(parentTask, result);
            for (JSONObject childTask : childTasks) {
                Event event = new Event()
                        .setPolicyId(policyId)
                        .setTaskId(taskId)
                        .setEntityType(taskType)
                        .setTopic(TOPIC_LIST)
                        .setTask(childTask);
                eventProducer.fireEvent(event);
            }
        } else if ("Detail".equals(taskType)) {
            // Detail任务用于下载页面，并打包成zip文件，Base64编码过后传递到服务端
            Event event = new Event()
                    .setPolicyId(policyId)
                    .setTaskId(taskId)
                    .setEntityType(taskType)
                    .setTopic(String.format(TOPIC_ORI_TEMPLATE, policyId.toUpperCase(Locale.ROOT)))
                    .setTask(parentTask)
                    .setData(result);
            eventProducer.fireEvent(event);
        } else if ("Data".equals(taskType)) {
            // Data任务用于更新爬虫任务辅助表
            List<JSONObject> tasks = getTasksFromString(parentTask, result);
            JSONObject dataMapping = iPolicyService.getDataMappingByPolicyId(policyId);

            for (JSONObject task : tasks) {
                task.putAll(dataMapping);
                Event event = new Event()
                        .setPolicyId(policyId)
                        .setTaskId(taskId)
                        .setEntityType(taskType)
                        .setTopic(TOPIC_DATA)
                        .setTask(task);
                eventProducer.fireEvent(event);
            }
        }
    }

    @Override
    public void handleDataTask(JSONObject task) {
        String tableName = task.getString("tableName");
        String pkName = task.getString("pkName");
        boolean update = task.getBoolean("update");

        // 移除不必要的字段
        task.remove("tableName");
        task.remove("pkName");
        task.remove("update");
        task.remove("columnNames");

        Map<String, Object> maps = task.getInnerMap();

        int count = iDataItemDao.getCount(maps, pkName, tableName);
        if (count > 0) {
            // 当前这条数据已存在
            if (update) {
                iDataItemDao.updateTableData(maps, pkName, tableName);
                log.info(String.format("Data Mapping数据已存在,更新数据状态:%s", task.toJSONString()));
            } else {
                log.warn(String.format("Data Mapping数据已存在,不更新数据状态:%s", task.toJSONString()));
            }
        } else {
            // 当前这条数据不存在,直接插入
            iDataItemDao.insertTableData(maps, pkName, tableName);
            log.info(String.format("Data Mapping数据不存在,直接更新或者插入数据状态:%s", task.toJSONString()));
        }
    }

    @Override
    public void pushCompletedTask(JSONObject taskObj) {
        String policyId = taskObj.getString("policyId");
        String taskType = taskObj.getString("taskType");
        String taskId = taskObj.getString("taskId");
        Event event = new Event()
                .setPolicyId(policyId)
                .setTaskId(taskId)
                .setEntityType(taskType)
                .setTopic(TOPIC_COMPLETED_TASK)
                .setTask(taskObj);
        eventProducer.fireEvent(event);
    }
}
