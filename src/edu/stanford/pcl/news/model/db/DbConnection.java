
package edu.stanford.pcl.news.model.db;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import edu.stanford.pcl.news.NewsProperties;

import java.net.UnknownHostException;

public class DbConnection {
    Mongo mongo;
    DB db;

    public DbConnection(String dbName) throws UnknownHostException {
        String host = (String)NewsProperties.getProperty("model.db.host");
        this.mongo = new Mongo(host);
        this.db = mongo.getDB(dbName);
    }

    public DBCollection getCollection(String collection) {
        return db.getCollection(collection);
    }
}
