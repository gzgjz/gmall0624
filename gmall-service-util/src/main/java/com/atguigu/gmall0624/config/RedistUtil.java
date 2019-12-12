package com.atguigu.gmall0624.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedistUtil {

    //1、创建连接池
    //2、初始化连接池
    //3、获取jedis
    private JedisPool jedisPool;

    public void initJedisPool(String host,int port,int timeOut){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        //获取到连接后自检
        jedisPoolConfig.setTestOnBorrow(true);

        //核心数
        jedisPoolConfig.setMaxTotal(200);

        //等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);

        //最小剩余数
        jedisPoolConfig.setMinIdle(10);

        //达到最大连接数是，放入等待队列中
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPool = new JedisPool(jedisPoolConfig,host,port,timeOut);
    }

    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

}
