
package edu.stanford.pcl.news.corenlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.pcl.news.NewsProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CoreNlpFactory {
    private static Map<String, CoreNlpPipeline> pipelines = new HashMap<String, CoreNlpPipeline>();

    public static CoreNlpPipeline getPipeline(String annotators) {
        CoreNlpPipeline pipeline = pipelines.get(annotators);
        if (pipeline == null) {
            Properties properties = new Properties();
            properties.setProperty("annotators", annotators);
            properties.setProperty("parse.maxlen", NewsProperties.getProperty("corenlp.parse.maxlen"));
            pipeline = new CoreNlpPipeline(new StanfordCoreNLP(properties));
            pipelines.put(annotators, pipeline);
        }
        return pipeline;
    }


    private CoreNlpFactory() {
    }

}
