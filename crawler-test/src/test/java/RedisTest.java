import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.crawler.CrawlerServerApplication;
import com.example.crawler.dao.ITaskDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CrawlerServerApplication.class)
public class RedisTest {

    @Autowired
    private ITaskDao iTaskDao;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testGetProgressTaskSize() {
        Long size = redisTemplate.opsForList().size("HEIMAOTOUSU:Detail");
        List<Object> tasks = redisTemplate.opsForList().range("HEIMAOTOUSU:Detail", 0, size);
        List<JSONObject> taskObjs = new ArrayList<>();
        for (Object taskObj : tasks) {
            String taskStr = (String) (taskObj);
            JSONObject task = JSON.parseObject(taskStr);
            taskObjs.add(task);
        }
    }

    @Test
    public void testGetKeys() {
        Set<String> keys = redisTemplate.keys("*:Detail");
        for (String key : keys) {
            System.out.println(key);
        }
    }

}
