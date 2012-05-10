package edu.stanford.rjweiss.newspaper;

import com.mongodb.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rebecca
 * Date: 4/28/12
 * Time: 6:12 PM
 * To change this template use File | Settings | File Templates.
 */

public class NewspaperProject {
    public static void main(String[] args) throws Exception {

        Mongo m = new Mongo();
        DB db = m.getDB("test");
        DBCollection articles = db.getCollection("articles");
        DBObject myDoc = articles.findOne();
        DBCursor cur;
        BasicDBObject query = new BasicDBObject();

        Calendar cal = new GregorianCalendar();
        cal.set(2010, 0, 1); // retrieving from January 1st, 2010
        Date fromDate = cal.getTime();
        query.put("date", new BasicDBObject("$gte", fromDate));
        System.out.println(articles.count(query)); //returns 157134 total articles

        Properties p = new Properties();
        p.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(p);

        AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");

        //Testing NER for just one article
        printNamedEntities(pipeline, classifier, (String)myDoc.get("body"));

    }

    private static void printNamedEntities(StanfordCoreNLP pipeline, AbstractSequenceClassifier classifier, String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            List<List<CoreLabel>> symbols = classifier.classify(sentence.toString());
            for (List<CoreLabel> labels : symbols) {
                for (CoreLabel label : labels) {
                    String currentLabel = label.get(CoreAnnotations.AnswerAnnotation.class);
                    String currentText = label.get(CoreAnnotations.TextAnnotation.class);
                    System.out.println(currentText + "(" + currentLabel + ")");
                }
            }
        }
    }
}
