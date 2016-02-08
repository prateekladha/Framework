package com.app.framework;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisManager {
	private Jedis jedis;

	public RedisManager(RedisConfig config){
	    jedis = new Jedis(config.getRedisHost(), config.getRedisPort(), 20000);
	}
	
	public String hGet(String key, String field){
		String result = "";
		try{
			result = jedis.hget(key, field);
		}
		catch(JedisConnectionException ex){
			result = jedis.hget(key, field);
		}
		return result;
	}
	
	public Long hSet(String key, String field, String value){
		Long result;
		try{
			result = jedis.hset(key, field, value);
		}
		catch(JedisConnectionException ex){
			result = jedis.hset(key, field, value);
		}
		return result;
	}
	
	public String set(String key, String value){
		String result;
		try{
			result = jedis.set(key, value);
		}
		catch(JedisConnectionException ex){
			result = jedis.set(key, value);
		}
		return result;
	}
	
	public Long sAdd(String key, String... members){
		Long result;
		try{
			result = jedis.sadd(key, members);
		}
		catch(JedisConnectionException ex){
			result = jedis.sadd(key, members);
		}
		return result;
	}
	
	public boolean sIsMember(String key, String member){
		boolean result;
		try{
			result = jedis.sismember(key, member);
		}
		catch(JedisConnectionException ex){
			result = jedis.sismember(key, member);
		}
		return result;
	}
	
	public Long sRemove(String key, String... member){
		Long result;
		try{
			result = jedis.srem(key, member);
		}
		catch(JedisConnectionException ex){
			result = jedis.srem(key, member);
		}
		return result;
	}
	
	public void quit(){
		try{
			jedis.quit();
		}
		catch(JedisConnectionException ex){
			//Do Nothing
		}
	}
	
	public String get(String key){
		String result;
		try{
			result = jedis.get(key);
		}
		catch(JedisConnectionException ex){
			result = jedis.get(key);
		}
		return result;
	}
	
	public Long del(String... keys){
		Long result;
		try{
			result = jedis.del(keys);
		}
		catch(JedisConnectionException ex){
			result = jedis.del(keys);
		}
		return result;
	}
	
	public Long hDel(String key, String... fields){
		Long result;
		try{
			result = jedis.hdel(key, fields);
		}
		catch(JedisConnectionException ex){
			result = jedis.hdel(key, fields);
		}
		return result;
	}
}