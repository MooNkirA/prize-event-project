package com.moon.prize.job.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.moon.prize.job.annotation.ElasticSimpleJob;
import com.moon.prize.job.listner.ElasticJobListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class ElasticJobAutoConfiguration {

    @Value("${spring.cloud.zookeeper.connect-string}")
    private String serverList;

    @Value("${spring.application.name}")
    private String namespace;

    @Value("${elaticjob.zookeeper.session-timeout-milliseconds}")
    private int sessionTimeoutMilliseconds;

    @Autowired
    private ApplicationContext applicationContext;

    // 在配置类加载时执行，对 elastic-job 的一些配置
    @PostConstruct
    public void initElasticJob() {
        // 获取 zookeeper 中的配置
        ZookeeperConfiguration config = new ZookeeperConfiguration(serverList, namespace);
        config.setSessionTimeoutMilliseconds(sessionTimeoutMilliseconds);
        // 初始化 ZookeeperRegistryCenter，主要是用于 elastic-job 分片与调试
        ZookeeperRegistryCenter regCenter = new ZookeeperRegistryCenter(config);
        regCenter.init();
        // 获取 spring 容器中所有 SimpleJob 接口的实例（实现类）
        Map<String, SimpleJob> map = applicationContext.getBeansOfType(SimpleJob.class);
        for (Map.Entry<String, SimpleJob> entry : map.entrySet()) {
            SimpleJob simpleJob = entry.getValue();
            // 获取 @ElasticSimpleJob 注解（自定义的注解）
            ElasticSimpleJob elasticSimpleJobAnnotation = simpleJob.getClass().getAnnotation(ElasticSimpleJob.class);
            // 获取注解中的 cron 表达式、分片、任务名称、任务的参数等，用于创建 SimpleJobConfiguration 配置对象
            String cron = StringUtils.defaultIfBlank(elasticSimpleJobAnnotation.cron(), elasticSimpleJobAnnotation.value());
            SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(
                    JobCoreConfiguration.newBuilder(simpleJob.getClass().getName(), cron, elasticSimpleJobAnnotation.shardingTotalCount())
                            .shardingItemParameters(elasticSimpleJobAnnotation.shardingItemParameters())
                            .build(),
                    simpleJob.getClass().getCanonicalName()
            );
            LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(simpleJobConfiguration).overwrite(true).build();

            String dataSourceRef = elasticSimpleJobAnnotation.dataSource();
            if (StringUtils.isNotBlank(dataSourceRef)) {

                if (!applicationContext.containsBean(dataSourceRef)) {
                    throw new RuntimeException("not exist datasource [" + dataSourceRef + "] !");
                }

                DataSource dataSource = (DataSource) applicationContext.getBean(dataSourceRef);
                JobEventRdbConfiguration jobEventRdbConfiguration = new JobEventRdbConfiguration(dataSource);
                // 创建 SpringJobScheduler 任务对象，并调用初始化方法
                SpringJobScheduler jobScheduler = new SpringJobScheduler(simpleJob, regCenter, liteJobConfiguration, jobEventRdbConfiguration);
                jobScheduler.init();
            } else {
                SpringJobScheduler jobScheduler = new SpringJobScheduler(simpleJob, regCenter, liteJobConfiguration);
                jobScheduler.init();
            }
        }
    }

    /**
     * 设置活动监听，前提是已经设置好了监听，见下一个目录
     *
     * @return
     */
    @Bean
    public ElasticJobListener elasticJobListener() {
        return new ElasticJobListener(100, 100);
    }
}
