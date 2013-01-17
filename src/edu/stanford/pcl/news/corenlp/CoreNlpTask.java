
package edu.stanford.pcl.news.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.model.entity.CoreNlp;
import edu.stanford.pcl.news.model.entity.Sentence;
import edu.stanford.pcl.news.model.entity.Token;
import edu.stanford.pcl.news.task.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoreNlpTask extends Task {
    private static final long serialVersionUID = 1828040719302562749L;


    private Article article;

    public CoreNlpTask(Article article) {
        this.article = article;
    }

    public Article getArticle() {
        return article;
    }

    @Override
    public void execute() {
        // Don't start timing until after getting the pipeline.
        // The first factory call for a given set of annotators is expensive.
        StanfordCoreNLP pipeline = CoreNlpFactory.getPipeline("tokenize, ssplit, pos, lemma, ner, parse");

        System.out.printf("%d\tCoreNlpTask\t%s\n", System.currentTimeMillis(), article.file);
        long start = System.currentTimeMillis();
        try {
            Annotation document = new Annotation(article.body);
            pipeline.annotate(document);

            article.corenlp = new CoreNlp();
            article.corenlp.sentences = new ArrayList<Sentence>();

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

                SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                s.dependencies = new ArrayList<String>();
                Collections.addAll(s.dependencies, dependencies.toPOSList().split("\n"));

                article.corenlp.sentences.add(s);
            }
        }
        finally {
            long stop = System.currentTimeMillis();
            this.executionMillis = stop - start;
            this.complete = true;
        }
    }
}
