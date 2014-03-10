package edu.stanford.pcl.news.task;

import com.mongodb.*;
import edu.stanford.pcl.news.elasticsearch.ElasticsearchIndexingTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.transformation.SchemaTransformationTask;
import edu.stanford.pcl.news.model.entity.Article;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

public class MongoCollectionToElasticSearchTaskRunner extends TaskRunner {

    private MongoClient mongodb;
    private DB db;
    private DBCollection collection;

//    private Client client;

    public MongoCollectionToElasticSearchTaskRunner(String mongohost, String mongodb, String collection)
            throws FileNotFoundException {

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

//        // Try to connect to a single local elasticsearch node.
//        try {
//            this.client = new TransportClient()
//                    .addTransportAddress(new InetSocketTransportAddress(elasticsearchhost, 9300));
//        } catch (ElasticsearchException e) {
//            e.printStackTrace();
//        }

        // Perform conversion from mongodb collection schema to elasticsearch search schema.
        // Index updated document.

//        registerResolver(SchemaTransformationTask.class, new TaskResolver<SchemaTransformationTask>() {
//            @Override
//            public void resolve(SchemaTransformationTask task) {
//                if (task.isSuccessful()) {
//                    server.getTaskQueue().putContinuationTask(new ElasticsearchIndexingTask(task.getArticle()));
//                }
//            }
//        });
            registerResolver(SchemaTransformationTask.class, new TaskResolver<SchemaTransformationTask>() {
            @Override
            public void resolve(SchemaTransformationTask task) {
                if (task.isSuccessful()) {
                    System.out.println("Inserted.");
                }
            }
        });

    }

    @Override
    public Task next() {
        BasicDBObject query = new BasicDBObject("processed", "CoreNLP");
        BasicDBObject sort = new BasicDBObject("$natural", 1);
        BasicDBObject update = new BasicDBObject();
        update.append("$set", new BasicDBObject("processed", true));

        DBObject doc = this.collection.findAndModify(query, sort, update);

        try {
            if (doc == null) {
                return null;
            }
            Article a = Serialization.toJavaObject(doc.toString(), Article.class);
            return new SchemaTransformationTask(a);

        }
        catch (Exception e) { // XXX Probably several errors to catch.
            return null;
        }

    }
}
