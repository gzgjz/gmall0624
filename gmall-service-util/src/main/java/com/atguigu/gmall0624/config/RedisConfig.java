package com.atguigu.gmall0624.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//将当前类变为xxx.xml
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;


    @Value("${spring.redis.timeOut:10000}")
    private int timeOut;

    @Bean
    public RedistUtil getRedisUtil(){
        // 表示配置文件中没有host
        if ("disabled".equals(host)){
            return null;
        }
        RedistUtil redistUtil = new RedistUtil();
        redistUtil.initJedisPool(host,port,timeOut);
        return redistUtil;
    }
}
