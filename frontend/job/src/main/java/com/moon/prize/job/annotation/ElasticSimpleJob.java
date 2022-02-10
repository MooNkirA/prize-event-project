package com.moon.prize.job.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSimpleJob {

    // cron 表达式
    @AliasFor("cron")
    String value() default "";

    @AliasFor("value")
    String cron() default "";
    // 定时任务的名称
    String jobName() default "";
    // 分片的数量，即在分布式环境中分成多份同时执行
    int shardingTotalCount() default 1;
    // 指定不同分片所获取到的传递参数
    String shardingItemParameters() default "";
    // 执行任务所传递的参数
    String jobParameter() default "";
    // 定义数据源，值是数据源实例在 spring 容器中的名称
    String dataSource() default "";

    String description() default "";

    boolean disabled() default false;

    boolean overwrite() default true;
}
