package com.moon.prize.job.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源配置类
 *
 * @Package: com.moon.prize.job.config
 * @ClassName: DataSourceConfig
 * @Version: 1.0
 */
@Configuration
public class DataSourceConfig {

    @Bean("datasource")
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSourceTow() {
        return DruidDataSourceBuilder.create().build();
    }
}