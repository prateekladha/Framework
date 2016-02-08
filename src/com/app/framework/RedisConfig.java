package com.app.framework;

public class RedisConfig {
	// redis properties
  private String redisHost;
  private int redisPort;
  
  public RedisConfig(String host, int port){
	  this.redisHost = host;
	  this.redisPort = port;
  }

  public String getRedisHost() {
      return redisHost;
  }
  
  public int getRedisPort() {
      return redisPort;
  }
}
