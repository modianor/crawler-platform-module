import com.example.crawler.CrawlerServerApplication;
import com.example.crawler.dao.IDataItemDao;
import com.example.crawler.dao.IPolicyConfigDao;
import com.example.crawler.dao.IPolicyExtensionDao;
import com.example.crawler.entity.PolicyConfig;
import com.example.crawler.entity.PolicyExtension;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CrawlerServerApplication.class)
public class DaoTest {

    @Autowired
    private IDataItemDao iDataItemDao;
    @Autowired
    private IPolicyExtensionDao iPolicyExtensionDao;
    @Autowired
    private IPolicyConfigDao iPolicyConfigDao;

    @Test
    public void testInsert() {
        String tableName = "BDG_DATA_HEIMAOTOUSU_COMPANY";
        String pkName = "uid";
        boolean update = true;
        Map<String, Object> maps = new HashMap<>();
        maps.put("id", null);
        maps.put("uid", "2026014915");
        maps.put("title", "支付宝");
        maps.put("url", "https://tousu.sina.com.cn/company/view/?couid=1644100824");
        int count = iDataItemDao.getCount(maps, pkName, tableName);
        if (count > 0) {
            // 已存在
            if (update) {
                iDataItemDao.updateTableData(maps, pkName, tableName);
            }
        } else {
            // 不存在
            iDataItemDao.insertTableData(maps, pkName, tableName);
        }
        System.out.println(maps.get("id"));
    }

    @Test
    public void testFetchData() {
        String sql = "select id,\n" +
                "       CONCAT(\n" +
                "               '{\"companyName\":\"{',\n" +
                "               '\\\\\"uid\\\\\"', ':', '\\\\\"', uid, '\\\\\",',\n" +
                "               '\\\\\"title\\\\\"', ':', '\\\\\"', title, '\\\\\"',\n" +
                "               '}\",\"creditCode\":\"\",\"urlSign\":\"\"}') Params,\n" +
                "       1                                           Progress,\n" +
                "       'HEIMAOTOUSU'                               PolicyId,\n" +
                "       4                                           LoadOrder\n" +
                "from BDG_DATA_HEIMAOTOUSU_COMPANY\n" +
                "where id > 0\n" +
                "order by id\n" +
                "limit 100;";
        List<Map<String, Object>> items = iDataItemDao.fetchData(sql);
        for (Map<String, Object> item : items) {
            System.out.println(item.entrySet());
        }

    }

    @Test
    public void testGetPolicyExtension() {
        PolicyExtension extension = iPolicyExtensionDao.getPolicyExtensionByPolicyId("HEIMAOTOUSU");
        System.out.println(extension);
    }

    @Test
    public void testGetPolicyConfig() {
        PolicyConfig config = iPolicyConfigDao.getPolicyConfigByPolicyId("ENV_PUNISHMENT");
        System.out.println(config);
    }

}