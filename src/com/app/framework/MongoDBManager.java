package com.app.framework;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

@SuppressWarnings("rawtypes")
public class MongoDBManager {
	
	private MongoClient mongoClient;
	public DB db;
	
	public MongoDBManager(MongoDBConfig config)
	{
		init(config);
	}
	
	public void init(MongoDBConfig config)
	{
		try {
			mongoClient = new MongoClient(config.getMongoDBHost(), config.getMongoDBPort());
			db = mongoClient.getDB(config.getMongoDbName());
		} catch (UnknownHostException e) {
			throw new RuntimeException("Unable to connect to Mongo Database", e.getCause());
		}
	}
	
	//FindOne without any query
	public DBObject findOne(String collectionName)
	{
		 DBCollection collection = db.getCollection(collectionName);
	     DBObject result = collection.findOne();
	     return result;
	}
		
	//FindOne based on the query
	public DBObject findOne(String collectionName, Map queryParams)
	{
		 DBCollection collection = db.getCollection(collectionName);
		 BasicDBObject query = new BasicDBObject(queryParams);
	     DBObject result = collection.findOne(query);
	     return result;
	}
	
	//Find with query
	public List<DBObject> find(String collectionName, Map queryParams) {
        DBCollection collection = db.getCollection(collectionName);

        BasicDBObject query = new BasicDBObject(queryParams);
        DBCursor results = collection.find(query);
        List<DBObject> resultsList = new ArrayList<DBObject>();
        while(results.hasNext()) {
            DBObject next = results.next();
            resultsList.add(next);
        }
        return resultsList;
    }
	
	//Update query for updating the complete document
	public void updateDocument(String collectionName, Map queryParams, Map newObj) {
        DBCollection collection = db.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject(queryParams);
        BasicDBObject newObject = new BasicDBObject(newObj);
        collection.update(query, newObject);
    }
	
	//Updating some field in the document.
	public boolean setFieldInDocument(String collectionName, Map<String, Object> queryParams, Map<String, Object> fieldValueMap) {
        DBCollection collection = db.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject(queryParams);
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start("$set", new BasicDBObject(fieldValueMap));
        DBObject setField = builder.get();
        WriteResult writeResult = collection.update(query, setField);
        return writeResult.getLastError().ok();
    }
	
	//Remove some document
	public boolean removeDocument(String collectionName, Map<String, Object> queryParams)
	{
		DBCollection collection = db.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject(queryParams);
        WriteResult writeResult  = collection.remove(query);
        return writeResult.getLastError().ok();
	}
	
	public void closeConnection()
	{
		mongoClient.close();
	}
}
