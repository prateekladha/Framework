package com.app.framework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.framework.Driver.HashMapNew;

public class DBActivities
{
  private HashMapNew Environment;
  private HashMapNew Dictionary;
  @SuppressWarnings("unused")
  private String driverType;
  static Logger log = LoggerFactory.getLogger(DBActivities.class);
  
  public DBActivities(String DT, HashMapNew Dict, HashMapNew Env){
	  Dictionary = Dict;
	  Environment = Env; 
	  this.driverType = DT;
  }
  
  public MongoCollection GetMongoCollection(String dbType, String collection){
	  MongoDBConfig config = ConnectToMongoDB(dbType);
	  MongoDBManager dbManager = new MongoDBManager(config);
	  Jongo jongo = new Jongo(dbManager.db);
	  return jongo.getCollection(collection);
  }
  
  public MongoDBConfig ConnectToMongoDB(String dbType){
	  MongoDBConfig config = new MongoDBConfig();
	  config.setMongoDBHost(Environment.get(dbType.toUpperCase() + "_DB_HOSTNAME"));
	  config.setMongoDBPort(Integer.parseInt(Environment.get(dbType.toUpperCase() + "_DB_PORT")));
	  config.setMongoDbName(Environment.get(dbType.toUpperCase() + "_DB_SERVICENAME"));
	  
	  return config;
  }
  
  public Connection fConnectToSqlite(String sqlitefilePath)
  {
    try
    {      
    	Class.forName("org.sqlite.JDBC");
    	return DriverManager.getConnection("jdbc:sqlite:" + sqlitefilePath);
    }
    catch (Exception e)
    {
    	log.info("Threw a Exception in DBActivities::fConnectToSqlite, full stack trace follows:", e);
    }
    return null;
  }
  
  public ResultSet fExecuteQuery(String sSQL, Connection conn)
  {
    try
    {      
      Statement stmnt = null;
      stmnt = conn.createStatement();
      ResultSet rs = null;
      if(sSQL.toUpperCase().indexOf("INSERT") > -1 || sSQL.toUpperCase().indexOf("UPDATE") > -1 || sSQL.toUpperCase().indexOf("CREATE") > -1 || sSQL.toUpperCase().indexOf("DROP") > -1){
    	  int count = stmnt.executeUpdate(sSQL);
    	  Dictionary.put("UPDATE_COUNT", String.valueOf(count));
    	  Dictionary.put("STMT_TYPE", sSQL.toUpperCase().split(" ")[0].trim());
      }
      else{
    	  rs = stmnt.executeQuery(sSQL);
      }
      stmnt.closeOnCompletion();
	  return rs; 
    }
    catch (SQLException eSQL)
    {
    	log.info("Threw a Exception in DBActivities::fExecuteQuery, full stack trace follows:", eSQL);
      return null;
    }
    catch (Exception e)
    {
    	log.info("Threw a Exception in DBActivities::fExecuteQuery, full stack trace follows:", e);
    }
    return null;
  }
  
  public ArrayList<String> fGetResultSetColumnName(String sql, Connection conn) throws SQLException{
	  ResultSet rs = fExecuteQuery(sql, conn);
	  ArrayList<String> arrColumns = new ArrayList<String>();
	  
	  if(rs == null){
		  log.info("DB Validation query isn't available for " + Dictionary.get("ACTION") + ". Result set is null");		  
		  return arrColumns;
	  }
	  
	  rs.next();
	  int rowCount = rs.getRow();
	  
	  if (rowCount == 0)
      {
		  log.info(sql + " : No Rows Found");
        rs.close();        
        rs = null;
        return arrColumns;
      }
	  
	  ResultSetMetaData rsmd = rs.getMetaData(); 
	  int colCount = rsmd.getColumnCount();
	  	
	  for(int intLoop = 1; intLoop <= colCount; intLoop++){
		  arrColumns.add(rsmd.getColumnName(intLoop));
	  }
	  
	  return arrColumns;
  }
}