package com.moon.prize.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebSecurityConfig extends WebMvcConfigurerAdapter {
    // 创建自定义拦截器并加入容器
    @Bean
    public RedisSessionInterceptor getSessionInterceptor() {
        return new RedisSessionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /*
         * 所有已api开头的访问都要进入 RedisSessionInterceptor 拦截器进行登录验证，并排除login接口(全路径)。必须写成链式，分别设置的话会创建多个拦截器。
         * 必须写成 getSessionInterceptor()，否则 SessionInterceptor 中的 @Autowired 会无效
         */
        registry.addInterceptor(getSessionInterceptor())
                .addPathPatterns("/api/act/**")
                .addPathPatterns("/api/user/**");
        super.addInterceptors(registry);
    }
}
