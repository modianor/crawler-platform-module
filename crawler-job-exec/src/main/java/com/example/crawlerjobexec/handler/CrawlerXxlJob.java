package com.example.crawlerjobexec.handler;

import cn.hutool.http.HttpUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * XxlJob开发示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、任务开发：在Spring Bean实例中，开发Job方法；
 * 2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 * 4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
public class CrawlerXxlJob {
    private static Logger logger = LoggerFactory.getLogger(CrawlerXxlJob.class);

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        XxlJobHelper.log("XXL-JOB, Hello World.");
        String json = "[{\"id\":1,\"Params\":\"{\\\"companyName\\\":\\\"{\\\\\\\"uid\\\\\\\":\\\\\\\"1644100824\\\\\\\",\\\\\\\"title\\\\\\\":\\\\\\\"支付宝\\\\\\\"}\\\",\\\"creditCode\\\":\\\"\\\",\\\"urlSign\\\":\\\"\\\"}\",\"Progress\":1,\"TaskType\":\"HEIMAOTOUSU\",\"PolicyId\":\"List\",\"LoadOrder\":4},{\"id\":2,\"Params\":\"{\\\"companyName\\\":\\\"{\\\\\\\"uid\\\\\\\":\\\\\\\"2673619603\\\\\\\",\\\\\\\"title\\\\\\\":\\\\\\\"蜻蜓FM\\\\\\\"}\\\",\\\"creditCode\\\":\\\"\\\",\\\"urlSign\\\":\\\"\\\"}\",\"Progress\":1,\"TaskType\":\"HEIMAOTOUSU\",\"PolicyId\":\"List\",\"LoadOrder\":4}]";
        String message = HttpUtil.post("http://127.0.0.1:6048/task/generateTaskSourceParams", json);
        System.out.println(message);
    }


    /**
     * 2、分片广播任务
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

        // 业务逻辑
        for (int i = 0; i < shardTotal; i++) {
            if (i == shardIndex) {
                XxlJobHelper.log("第 {} 片, 命中分片开始处理", i);
            } else {
                XxlJobHelper.log("第 {} 片, 忽略", i);
            }
        }

    }

    public void init() {
        logger.info("init");
    }

    public void destroy() {
        logger.info("destroy");
    }

}
