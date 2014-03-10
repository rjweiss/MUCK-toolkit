package edu.stanford.pcl.news.task;

import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.model.entity.Article;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class MongoCollectionToMongoCollectionTaskRunner extends TaskRunner {

    private LinkedBlockingQueue<DBObject> documents;
    private MongoClient mongodb;
    private DB db;
    private DBCollection collection;

    public MongoCollectionToMongoCollectionTaskRunner(String host, String db, String collection) throws FileNotFoundException {

        try {
            this.mongodb = new MongoClient(host);
            this.db = mongodb.getDB(db);
            this.collection = this.db.getCollection(collection);
        } catch (UnknownHostException e) {
            // XXX
            e.printStackTrace();
        }


/*        registerResolver(ParserTask.class, new TaskResolver<ParserTask>() {
            @Override
            public void resolve(ParserTask task) {
                Task continuationTask = new CoreNlpTask(task.getArticle());
                server.getTaskQueue().putContinuationTask(continuationTask);
            }
        });*/

        final DBCollection dbCollection = this.collection;
        registerResolver(CoreNlpTask.class, new TaskResolver<CoreNlpTask>() {
            @Override
            public void resolve(CoreNlpTask task) {
                if (task.isSuccessful()) {
                    dbCollection.save((DBObject)JSON.parse(Serialization.toMongoJson(task.getArticle())));
                }
            }
        });
    }

    @Override
    public Task next() {
        BasicDBObject query = new BasicDBObject("processed", false);
        BasicDBObject sort = new BasicDBObject("$natural", 1);
        BasicDBObject update = new BasicDBObject();
        update.append("$set", new BasicDBObject("processed", "CoreNLP"));

        DBObject doc = this.collection.findAndModify(query, sort, update);

        try {
            if (doc == null) {
                return null;
            }

            Article a = Serialization.toJavaObject(doc.toString(), Article.class);
            return new CoreNlpTask(a);
        }
        catch (MongoException e) {
            return null;
        }

    }
}
