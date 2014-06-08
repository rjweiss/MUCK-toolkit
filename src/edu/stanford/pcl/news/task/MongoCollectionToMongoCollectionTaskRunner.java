
package edu.stanford.pcl.news.task;

import java.net.UnknownHostException;

import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.model.entity.Article;

public class MongoCollectionToMongoCollectionTaskRunner extends TaskRunner {

    private DBCollection collection;

    public MongoCollectionToMongoCollectionTaskRunner(String host, String db, String collection) {
        // XXX  Need at least one worker.
        registerWorker(new TaskWorker());

        try {
            MongoClient mongodb = new MongoClient(host);
            DB db1 = mongodb.getDB(db);
            this.collection = db1.getCollection(collection);
        }
        catch (UnknownHostException e) {
            // XXX
            e.printStackTrace();
        }

        final DBCollection dbCollection = this.collection;
        registerResolver(CoreNlpTask.class, new TaskResolver<CoreNlpTask>() {
            @Override
            public void resolve(CoreNlpTask task) {
                if (task.isSuccessful()) {
                    DBObject dbObject = (DBObject)JSON.parse(Serialization.toMongoJson(task.getArticle()));
                    // XXX  Need to put the processed flag in the entity, and think about entity state more generically.
                    dbObject.put("processed", "annotated");
                    dbCollection.save(dbObject);
                }
            }
        });

        registerResolver(TerminateTask.class, new TaskResolver<TerminateTask>() {
            @Override
            public void resolve(TerminateTask task) {
                System.exit(0);
            }
        });
    }

    @Override
    public Task next() {
        BasicDBObject query = new BasicDBObject("processed", "downloaded");
        BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("processed", "annotating"));

        DBObject doc = this.collection.findAndModify(query, update);

        try {
            if (doc == null) {
                return null;
            }

            Article a = Serialization.toJavaObject(doc.toString(), Article.class);

            // XXX  This piece knows too much about the MongoDB collection being used for testing purposes...
            if (a.file == null) {
                Object link = doc.get("url");
                if (link instanceof String) {
                    a.file = (String)doc.get("url");
                }
                else if (link instanceof BasicDBList) {
                    a.file = (String)((BasicDBList)doc.get("url")).get(0);
                }
            }
            if (a.body == null) {
                a.body = (String)doc.get("text");
            }

            return new CoreNlpTask(a);
        }
        catch (MongoException e) {
            return null;
        }
    }

}
