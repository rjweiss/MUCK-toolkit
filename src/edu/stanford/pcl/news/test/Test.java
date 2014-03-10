
package edu.stanford.pcl.news.test;

import edu.stanford.pcl.news.task.MongoCollectionToElasticSearchTaskRunner;
import edu.stanford.pcl.news.task.MongoCollectionToMongoCollectionTaskRunner;
import edu.stanford.pcl.news.task.TaskRunner;

public class Test {

    public static void main(String[] args) throws Exception {

//        TaskRunner runner = new LocalDirectoryToJsonTaskRunner("data/plain", "output.json");
//        runner.start();

//        TaskRunner runner = new LocalDirectoryToMongoCollectionTaskRunner("data/plain", "localhost", "news", "articles");
//        runner.start();

//        TaskRunner runner = new LocalDirectoryToSolrIndexTaskRunner("data/plain", "test");
//        runner.start();

//        TaskRunner NLPrunner = new MongoCollectionToMongoCollectionTaskRunner("localhost", "election2012_test", "dev");
//        NLPrunner.start();

        TaskRunner ESrunner = new MongoCollectionToElasticSearchTaskRunner(
                "localhost", "election2012_test", "dev");
        ESrunner.start();

    }

}
