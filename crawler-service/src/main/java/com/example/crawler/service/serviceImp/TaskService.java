package com.example.crawler.service.serviceImp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.configs.ElasticSearchConfig;
import com.example.crawler.dao.IDataItemDao;
import com.example.crawler.dao.IPolicyConfigDao;
import com.example.crawler.dao.IPolicyExtensionDao;
import com.example.crawler.dao.ITaskDao;
import com.example.crawler.entity.Event;
import com.example.crawler.entity.Policy;
import com.example.crawler.entity.PolicyExtension;
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
    private IPolicyExtensionDao iPolicyExtensionDao;

    @Autowired
    private IPolicyConfigDao iPolicyConfigDao;

    @Autowired
    private IPolicyService iPolicyService;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private EventProducer eventProducer;

    @Override
    public String getDeduplicationFields(JSONObject task) {
        // return "urlSign";
        return "None";
    }

    @Override
    public Boolean isTurnOnDeduplication(JSONObject task) {
        // ???????????????????????????????????????????????????
        String policyId = task.getString("policyId");
        return true;
    }

    @Override
    public Boolean updateDeduplication(JSONObject task, String taskMd5) {
        String policyId = task.getString("policyId");
        //???????????????id
        JSONObject obj = new JSONObject();
        obj.put(TASK_SIGNATURE, taskMd5);
        IndexRequest request = new IndexRequest(policyId.toLowerCase(Locale.ROOT));
        request.source(obj.toJSONString(), XContentType.JSON);
        //??????????????????
        IndexResponse indexResponse = null;
        try {
            indexResponse = restHighLevelClient.index(request, ElasticSearchConfig.COMMON_OPTIONS);
            log.info("??????????????????????????????, {}", indexResponse.toString());
        } catch (IOException e) {
            log.error("????????????????????????????????????, {}", e.toString());
            return false;
        }
        return true;
    }

    @Override
    public Boolean isDuplication(JSONObject task, String taskMd5) {
        String policyId = task.getString("policyId");
        //1.??????????????????
        SearchRequest searchRequest = new SearchRequest();
        //????????????
        searchRequest.indices(policyId.toLowerCase(Locale.ROOT));
        //??????DSL???????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.1 ??????????????????
        searchSourceBuilder.query(QueryBuilders.termQuery(TASK_SIGNATURE, taskMd5));
        searchRequest.source(searchSourceBuilder);
        //2.????????????
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            return searchHits.length > 0;
        } catch (IOException e) {
            log.error("????????????????????????????????????, ????????????:{}", e.toString());
            return false;
        }

    }


    @Override
    public Boolean doDeduplication(JSONObject task) {
        Boolean isTurnOn = isTurnOnDeduplication(task);
        if (isTurnOn) {
            String fields = getDeduplicationFields(task);
            if ("None".equals(fields)) {
                // ???????????????????????????????????????None?????????????????????
                return false;
            } else {
                String[] fieldArr = fields.split("\\|");
                // ????????????????????????????????????
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
            // ????????????????????????????????????None
            return false;
        }

    }

    @Override
    public void pushTask(String redisKey, JSONObject task, Boolean duplication) {
        if (duplication) {
            // ??????????????????????????????????????????????????????
            Boolean exist = doDeduplication(task);
            // ???????????????????????????????????????????????????
            if (!exist) {
                iTaskDao.pushTask(redisKey, task);
                log.info(String.format("??????????????????????????????:[%s]", task.toJSONString()));
            } else {
                log.warn(String.format("???????????????????????????:[%s]", task.toJSONString()));
            }
        } else {
            // ??????????????????????????????????????????????????????
            iTaskDao.pushTask(redisKey, task);
            log.info(String.format("????????????????????????????????????????????????:[%s]", task.toJSONString()));
        }
    }

    @Override
    public void pushTask(String redisKey, JSONObject task) {
        iTaskDao.pushTask(redisKey, task);
    }

    @Override
    public void pushBatchTasks(List<JSONObject> tasks) {
        String redisKey = "";
        for (int i = 0; i < tasks.size(); i++) {
            JSONObject task = tasks.get(i);
            String policyId = task.getString("policyId");
            String taskType = task.getString("taskType");
            redisKey = policyId + ":" + taskType;
            break;
        }

        String[] objs = new String[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            objs[i] = tasks.get(i).toJSONString();
        }

        iTaskDao.pushBatchTask(redisKey, objs);
    }

    @Override
    public Boolean acknowledgeTask(JSONObject task) {
        String policyId = task.getString("policyId");
        String policyMode = task.getString("policyMode");
        String redisKey = String.format("%s:%s", policyId, REDIS_KEY_IN_PROGRESS_TASK);
        if ("config".equals(policyMode)) {
            redisKey = String.format("%s:%s", "NORMAL", REDIS_KEY_IN_PROGRESS_TASK);
        }
        return iTaskDao.removeProgessTask(redisKey, task);
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

        // ?????????????????????????????????????????????
        Policy policy = iPolicyService.getPolicyByPolicyId(policyId);
        // ???????????????plugin????????????????????????
        PolicyExtension extension = iPolicyExtensionDao.getPolicyExtensionByPolicyId(policyId);
        String policyMode = extension.getPolicyMode();

        if ("List".equals(taskType)) {
            // List???????????????????????????????????????????????????????????????List???Detail???Data
            List<JSONObject> childTasks = getTasksFromString(parentTask, result);
            for (JSONObject childTask : childTasks) {
                if ("config".equals(policyMode)) {
                    // ???????????????????????????????????????????????????companyName?????????????????????????????????
                    childTask.put("companyName", parentTask.getString("companyName"));
                    childTask.put("policyMode", "config");
                } else {
                    childTask.put("policyMode", "plugin");
                }
                Event event = new Event()
                        .setPolicyId(policyId)
                        .setTaskId(taskId)
                        .setEntityType(taskType)
                        .setTopic(TOPIC_TASK_LIST)
                        .setTask(childTask);
                eventProducer.fireEvent(event);
            }
        } else if ("Detail".equals(taskType)) {
            // Detail???????????????????????????????????????zip?????????Base64??????????????????????????????
            Event event = new Event()
                    .setPolicyId(policyId)
                    .setTaskId(taskId)
                    .setEntityType(taskType)
                    .setTopic(String.format(TOPIC_ORI_TEMPLATE, policyId.toUpperCase(Locale.ROOT)))
                    .setTask(parentTask)
                    .setData(result);
            eventProducer.fireEvent(event);
        } else if ("Data".equals(taskType)) {
            // Data???????????????????????????????????????
            List<JSONObject> tasks = getTasksFromString(parentTask, result);
            JSONObject dataMapping = iPolicyService.getDataMappingByPolicyId(policyId);

            for (JSONObject task : tasks) {
                task.putAll(dataMapping);
                Event event = new Event()
                        .setPolicyId(policyId)
                        .setTaskId(taskId)
                        .setEntityType(taskType)
                        .setTopic(TOPIC_TASK_DATA)
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

        // ????????????????????????
        task.remove("tableName");
        task.remove("pkName");
        task.remove("update");
        task.remove("columnNames");

        Map<String, Object> maps = task.getInnerMap();

        int count = iDataItemDao.getCount(maps, pkName, tableName);
        if (count > 0) {
            // ???????????????????????????
            if (update) {
                try {
                    iDataItemDao.updateTableData(maps, pkName, tableName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                log.info(String.format("Data Mapping???????????????,??????????????????:%s", task.toJSONString()));
            } else {
                log.warn(String.format("Data Mapping???????????????,?????????????????????:%s", task.toJSONString()));
            }
        } else {
            // ???????????????????????????,????????????
            try {
                iDataItemDao.insertTableData(maps, pkName, tableName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info(String.format("Data Mapping???????????????,????????????????????????????????????:%s", task.toJSONString()));
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

    @Override
    public void pushFailTask(JSONObject taskObj) {
        String policyId = taskObj.getString("policyId");
        String taskType = taskObj.getString("taskType");
        String taskId = taskObj.getString("taskId");
        Event event = new Event()
                .setPolicyId(policyId)
                .setTaskId(taskId)
                .setEntityType(taskType)
                .setTopic(TOPIC_TASK_FAIL)
                .setTask(taskObj);
        eventProducer.fireEvent(event);
    }
}
