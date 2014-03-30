package edu.stanford.pcl.news.task;


import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.stanford.pcl.news.aws.AwsTaskRunner;
import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.model.entity.Article;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

public class MongoCollectionToMongoCollectionAwsTaskRunner extends AwsTaskRunner {

    private static class Resolver implements TaskResolver<CoreNlpTask>, Serializable {
        private static final long serialVersionUID = -6506359826139487858L;

        private String mongoServerHost;
        private String mongoDbName;
        private String mongoCollectionName;

        private transient DBCollection collection;

        Resolver(String mongoServerHost, String mongoDbName, String mongoCollectionName) {
            this.mongoServerHost = mongoServerHost;
            this.mongoDbName = mongoDbName;
            this.mongoCollectionName = mongoCollectionName;

            try {
                MongoClient mongodb = new MongoClient(mongoServerHost);
                DB db = mongodb.getDB(mongoDbName);
                this.collection = db.getCollection(mongoCollectionName);
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void resolve(CoreNlpTask task) throws RemoteException {
            if (collection == null) {
                try {
                    MongoClient mongodb = new MongoClient(mongoServerHost);
                    DB db = mongodb.getDB(mongoDbName);
                    this.collection = db.getCollection(mongoCollectionName);
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

            if (task.isSuccessful()) {
                collection.save((DBObject) JSON.parse(Serialization.toMongoJson(task.getArticle())));
            }
            else {
                // XXX
                System.out.println("Task not successful.");
            }
        }
    }


    DBCollection collection;

    public MongoCollectionToMongoCollectionAwsTaskRunner(String mongoServerHost, String mongoDbName, String mongoCollectionName, String ami, String instanceType, String keyPairName, String securityGroupName, int workerCount, String spotPrice) throws UnknownHostException {
        super(ami, instanceType, keyPairName, securityGroupName, workerCount, spotPrice);

        MongoClient mongodb = new MongoClient(mongoServerHost);
        DB db = mongodb.getDB(mongoDbName);
        collection = db.getCollection(mongoCollectionName);

        this.registerResolver(CoreNlpTask.class, new Resolver(mongoServerHost, mongoDbName, mongoCollectionName));
    }


    @Override
    public Task next() {
        BasicDBObject query = new BasicDBObject("processed", "false");
        BasicDBObject sort = new BasicDBObject("$natural", 1);
        BasicDBObject update = new BasicDBObject();
        update.append("$set", new BasicDBObject("processed", "CoreNLP"));

        DBObject doc = this.collection.findAndModify(query, sort, update);

        Article a = Serialization.toJavaObject(doc.toString(), Article.class);
        Object link = doc.get("link");
        if (link instanceof String) {
            a.file = (String)doc.get("link");
        }
        else if (link instanceof BasicDBList) {
            a.file = (String)((BasicDBList)doc.get("link")).get(0);
        }
        a.body = (String)doc.get("cleaned_text");
        System.out.println("Adding task: " + a.file);
        return new CoreNlpTask(a);
    }
}
