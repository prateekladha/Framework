package com.app.framework;

public class MongoDBConfig {
	
	private String mongoDbHost;
	private int mongoDbPort;
	private String mongoDbName;
	
	public String getMongoDBHost(){
		return this.mongoDbHost;
	}
	
	public void setMongoDBHost(String host)
	{
		this.mongoDbHost = host;
	}
	
	public int getMongoDBPort()
	{
		return this.mongoDbPort;
	}
	
	public void setMongoDBPort(int port)
	{
		this.mongoDbPort = port;
	}
	
	public String getMongoDbName()
	{
		return this.mongoDbName;
	}
	
	public void setMongoDbName(String dbName)
	{
		this.mongoDbName = dbName;
	}
}
