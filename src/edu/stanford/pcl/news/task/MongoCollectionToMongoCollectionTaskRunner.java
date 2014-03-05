package edu.stanford.pcl.news.task;

import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.parser.ParserTask;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: rjweiss
 * Date: 3/2/14
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class MongoCollectionToMongoCollectionTaskRunner extends TaskRunner {

    private LinkedBlockingQueue<DBObject> documents;
    private MongoClient mongodb;
    private DB db;
    private DBCollection collection;

    public MongoCollectionToMongoCollectionTaskRunner(String host, String db, String collection, String outputPath) throws FileNotFoundException {

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
        update.append("$set", new BasicDBObject("processed", "Processing"));

        DBObject doc = this.collection.findAndModify(query, sort, update);

        try {
            if (doc == null) {
                return null;
            }

            Article a = new Article();
            a.file = doc.get("file").toString(); // XXX This changes the Mongo field name too!  Originally labeled "url".
            a.body = doc.get("body").toString(); // XXX This changes the Mongo field name too! Originally labeled "cleaned_text".
            a._id = (ObjectId) doc.get("_id");

            return new CoreNlpTask(a);
            // XXX There needs to be a step where after the CoreNLP task is resolved, the document's "processed" field is set to "true".
        }
        catch (MongoException e) {
            return null;
        }

    }
}
