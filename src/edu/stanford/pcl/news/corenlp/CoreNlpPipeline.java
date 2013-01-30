
package edu.stanford.pcl.news.corenlp;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.MutableInteger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreNlpPipeline {

    // XXX  This is particularly invasive, and should be removed when a real API is available.
    private static Field annotatorsField;
    private static Field accumulatedTimeField;
    static {
        try {
            annotatorsField = AnnotationPipeline.class.getDeclaredField("annotators");
            accumulatedTimeField = AnnotationPipeline.class.getDeclaredField("accumulatedTime");
            annotatorsField.setAccessible(true);
            accumulatedTimeField.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            // XXX  This shouldn't happen until we upgrade the Core NLP version.  When it does, die so this gets noticed.
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }


    StanfordCoreNLP pipeline;
    Map<String, Long> lastAnnotatorTimes = null;

    public CoreNlpPipeline(StanfordCoreNLP pipeline) {
        this.pipeline = pipeline;
    }


    public Annotation process(String text) {
        lastAnnotatorTimes = getAnnotatorTimes(true);
        return pipeline.process(text);
    }

    public Map<String, Long> getAnnotatorTimes(boolean accumulated) {
        Map<String, Long> annotatorTimeMap = new HashMap<String, Long>();

        // Get the time counters for each annotator in the pipeline.
        // XXX  This is particularly invasive, and should be removed when a real API is available.
        try {
            @SuppressWarnings("unchecked")
            List<Annotator> annotators = (List<Annotator>)annotatorsField.get(pipeline);
            @SuppressWarnings("unchecked")
            List<MutableInteger> accumulatedTime = (List<MutableInteger>)accumulatedTimeField.get(pipeline);

            for (int i=0; i<annotators.size(); i++) {
                Annotator annotator = annotators.get(i);
                long time = accumulatedTime.get(i).longValue();
                if (!accumulated && lastAnnotatorTimes != null) {
                    time -= lastAnnotatorTimes.get(annotator.getClass().getSimpleName());
                }
                annotatorTimeMap.put(annotator.getClass().getSimpleName(), time);
            }
        }
        catch (IllegalAccessException e) {
            // XXX  This shouldn't happen, but if it does we'll have no timing information.
            e.printStackTrace(System.err);
        }

        return annotatorTimeMap;
    }

}
