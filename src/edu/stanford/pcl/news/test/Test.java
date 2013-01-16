
package edu.stanford.pcl.news.test;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.model.entity.CoreNlp;
import edu.stanford.pcl.news.model.entity.Sentence;
import edu.stanford.pcl.news.model.entity.Token;
import edu.stanford.pcl.news.parser.ParseException;
import edu.stanford.pcl.news.parser.Parser;
import edu.stanford.pcl.news.parser.ParserFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Test {

    public static String testNlp(StanfordCoreNLP pipeline, Article article) {
        Annotation document = new Annotation("Stanford University is located in California.  It is a great university.");
        pipeline.annotate(document);


        article.corenlp = new CoreNlp();
        article.corenlp.sentences = new ArrayList<Sentence>();

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {

            Sentence s = new Sentence();
            s.tokens = new ArrayList<Token>();

            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                Token t = new Token();
                t.word = token.get(CoreAnnotations.TextAnnotation.class);
                t.lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                t.pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                t.ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                s.tokens.add(t);
            }



          // this is the parse tree of the current sentence
//              Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);


            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

            s.dependencies = new ArrayList<String>();

            for (String d : dependencies.toPOSList().split("\n")) {
                s.dependencies.add(d);
            }

            article.corenlp.sentences.add(s);

            System.out.println();
        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
//            Map<Integer, CorefChain> graph = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);


        String s = Serialization.toJson(article);
        return s;
    }



    public static void main(String[] args) {
        try {
            Parser p = ParserFactory.getParser(ParserFactory.Outlet.LOS_ANGELES_TIMES);
            Article article = p.parse("data\\latimes\\2000\\20000120\\1101384\\txt\\48148189.xml");
//            Parser p = ParserFactory.getParser(ParserFactory.Outlet.LOS_ANGELES_TIMES);
//            Article article = p.parse("data\\latimes\\2000\\20000101\\1092183\\txt\\47589645.xml");
//            Parser p = ParserFactory.getParser(ParserFactory.Outlet.CHICAGO_TRIBUNE);
//            Article article = p.parse("data\\chitrib\\2000\\20000101\\1092154\\txt\\47587647.xml");

            try {
                for (Field field : article.getClass().getFields()) {
                    Object v = field.get(article);
                    if (v != null) {
                        System.out.printf("%8s: %s\n", field.getName(), v.toString());
                    }
                }
            }
            catch (IllegalAccessException e) {
                e.printStackTrace(System.err);
            }

            // XXX  Everything below is hideous.

            StanfordCoreNLP pipeline;
            Properties properties = new Properties();
            properties.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
            pipeline = new StanfordCoreNLP(properties);
//            Annotation document = new Annotation(article.body);

            testNlp(pipeline, article);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
