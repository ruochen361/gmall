package com.atguigu.gmall.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * User: Administrator
 * Date:2018/4/16 0016
 */
public class RedisUtil {

    private JedisPool jedisPool;

    public void initJedisPool(String host,int port,int database){

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setMaxTotal(200);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPool = new JedisPool(jedisPoolConfig,host,port,20*1000);

    }


    public  Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return  jedis;
    }
}
