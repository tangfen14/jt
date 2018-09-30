package com.jt.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

//是操作redis工具API,运用了装饰模式
@Service
public class RedisService {
	//想要得到redis的链接,首先要注入在xml中配置的连接池对象
	@Autowired(required=false)  /*程序启动时暂时不注入,但是如果调用时自动注入;
	由于这里是工具类,是被其他业务类所依赖的,jt-manage中是由appContext-redis的配置的,
	而如果有别的业务类,也依赖了此工具类,但是并没有用到redis,那么如果不加false属性,就无法注入*/
	//private ShardedJedisPool jedisPool;
	private JedisSentinelPool sentinelPool;
	
	//包装哨兵
	public void set(String key,String value){
		Jedis jedis = sentinelPool.getResource();
		jedis.set(key, value);
		sentinelPool.returnResource(jedis);
	}
	
	public String get(String key){
		Jedis jedis = sentinelPool.getResource();
		String result = jedis.get(key);
		sentinelPool.returnResource(jedis);
		return result;
	}
	
/*	分片学习
 * 	public void set(String key,String value){
		//从池中拿出一个链接(如6379,6980等)
		ShardedJedis shardedJedis = jedisPool.getResource();
		shardedJedis.set(key, value);
		jedisPool.returnResource(shardedJedis);//将链接还回池中
	}
	
	public String get(String key){
		ShardedJedis shardedJedis = jedisPool.getResource();
		String result = shardedJedis.get(key);
		jedisPool.returnResource(shardedJedis);
		return result;
	}
	
	//为key添加超时时间
	public void set(String key,String value,int seconds){
		ShardedJedis shardedJedis = jedisPool.getResource();
		shardedJedis.setex(key, seconds, value);
		jedisPool.returnResource(shardedJedis);//将链接还回池中
	}*/
}
