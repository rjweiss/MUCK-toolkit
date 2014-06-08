package edu.stanford.pcl.news.task;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import com.mongodb.*;
import edu.stanford.pcl.news.indexer.ElasticsearchTransformAndIndexTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.model.entity.Article;

public class MongoCollectionToElasticSearchTaskRunner extends TaskRunner {

    private MongoClient mongodb;
    private DB db;
    private DBCollection collection;

    public MongoCollectionToElasticSearchTaskRunner(String mongohost, String mongodb, String collection) throws FileNotFoundException {
        // XXX  Need at least one worker.
        registerWorker(new TaskWorker());

        // Try to connect to mongodb.
        try {
            this.mongodb = new MongoClient(mongohost);
            this.db = this.mongodb.getDB(mongodb);
            this.collection = this.db.getCollection(collection);
        } catch (UnknownHostException e) {
            // XXX
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }

        final DBCollection dbCollection = this.collection;
        registerResolver(ElasticsearchTransformAndIndexTask.class, new TaskResolver<ElasticsearchTransformAndIndexTask>() {
            @Override
            public void resolve(ElasticsearchTransformAndIndexTask task) {
                if (task.isSuccessful()) {
                    BasicDBObject query = new BasicDBObject("_id", task.getArticle()._id);
                    BasicDBObject update = new BasicDBObject().append("$set", new BasicDBObject("processed", "indexed"));
                    dbCollection.update(query, update);
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
        BasicDBObject query = new BasicDBObject("processed", "annotated");
        BasicDBObject update = new BasicDBObject().append("$set", new BasicDBObject("processed", "indexing"));

        DBObject doc = this.collection.findAndModify(query, update);

        try {
            if (doc == null) {
                return null;
            }
            Article a = Serialization.toJavaObject(doc.toString(), Article.class);
            return new ElasticsearchTransformAndIndexTask(a);

        }
        catch (Exception e) { // XXX Probably several errors to catch.
            return null;
        }

    }
}
