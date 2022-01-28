import com.example.crawler.CrawlerServerApplication;
import com.example.crawler.dao.IDataItemDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CrawlerServerApplication.class)
public class DaoTest {

    @Autowired
    private IDataItemDao iDataItemDao;

    @Test
    public void testInsert() {
        String tableName = "BDG_DATA_TEST";
        String pkName = "company";
        boolean update = true;
        Map<String, Object> maps = new HashMap<>();
        maps.put("company", "锤子科技2");
        maps.put("age", 12);
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
    }

}