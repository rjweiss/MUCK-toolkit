package edu.stanford.pcl.news;


import edu.stanford.pcl.news.task.MongoCollectionToElasticSearchTaskRunner;
import edu.stanford.pcl.news.task.MongoCollectionToMongoCollectionTaskRunner;
import edu.stanford.pcl.news.task.TaskRunner;


public class Muck {
    public static void main(String[] args) throws Exception {
        // Processing goes from: downloaded -> annotating -> annotated -> indexing -> indexed

        String mode = args[args.length-1];

        if (mode.equals("annotate")) {
            TaskRunner runner = new MongoCollectionToMongoCollectionTaskRunner("localhost", "news", "articles");
            runner.start();
        }
        else if (mode.equals("index")) {
            TaskRunner runner = new MongoCollectionToElasticSearchTaskRunner("localhost", "news", "articles");
            runner.start();
        }
        else {
            System.out.println("No mode specified.");
        }

    }
}
