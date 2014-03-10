
package edu.stanford.pcl.news.task;

import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.model.entity.Article;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

public class MongoCollectionToMongoCollectionTaskRunner extends TaskRunner {

    private DBCollection collection;

    public MongoCollectionToMongoCollectionTaskRunner(String host, String db, String collection, String outputPath) throws FileNotFoundException {
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
                    dbCollection.save((DBObject)JSON.parse(Serialization.toMongoJson(task.getArticle())));

                    // XXX  There needs to be a step where after the CoreNLP task is resolved, the document's "processed" field is set to "true".
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

            Article a = Serialization.toJavaObject(doc.toString(), Article.class);
            return new CoreNlpTask(a);
        }
        catch (MongoException e) {
            return null;
        }
    }

}
