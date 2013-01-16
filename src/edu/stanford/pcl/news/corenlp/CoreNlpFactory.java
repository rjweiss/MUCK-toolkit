
package edu.stanford.pcl.news.corenlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CoreNlpFactory {
    private static Map<String, StanfordCoreNLP> pipelines = new HashMap<String, StanfordCoreNLP>();

    public static StanfordCoreNLP getPipeline(String annotators) {
        StanfordCoreNLP pipeline = pipelines.get(annotators);
        if (pipeline == null) {
            Properties properties = new Properties();
            properties.put("annotators", annotators);
            pipeline = new StanfordCoreNLP(properties);
            pipelines.put(annotators, pipeline);
        }
        return pipeline;
    }


    private CoreNlpFactory() {
    }

}
