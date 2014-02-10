
package edu.stanford.pcl.news.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.pcl.news.NewsProperties;
import edu.stanford.pcl.news.model.entity.*;
import edu.stanford.pcl.news.task.Task;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoreNlpTask extends Task {
    private static final long serialVersionUID = 8128611873587355604L;


    private Article article;

    public CoreNlpTask(Article article) {
        this.article = article;
        // Don't initialize the pipeline here!  Tasks are created on the server.  Let the worker initialize the task.
    }

    public Article getArticle() {
        return article;
    }

    @Override
    public void initialize() {
        // The first factory call for a given set of annotators is expensive.  Get it here so that it is
        // done once outside of the scope of the timing.  The factory will reuse this instance.
        CoreNlpFactory.getPipeline("tokenize, ssplit, pos, lemma, ner, parse, sentiment");
    }

    @Override
    public void execute() {
        CoreNlpPipeline pipeline = CoreNlpFactory.getPipeline("tokenize, ssplit, pos, lemma, ner, parse, sentiment");

        // Statistics counters.
        int totalTokens = 0;
        int minTokensPerSentence = Integer.MAX_VALUE;
        int maxTokensPerSentence = 0;
        double meanTokensPerSentence = 0;

        try {
            Annotation document = pipeline.process(article.body);

            article.corenlp = new CoreNlp();
            article.corenlp.sentences = new ArrayList<Sentence>();

            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Sentence s = new Sentence();

                // Store sentence tokens.
                s.tokens = new ArrayList<Token>();
                for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    Token t = new Token();
                    t.word = token.get(CoreAnnotations.TextAnnotation.class);
                    t.lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                    t.pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    t.ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                    s.tokens.add(t);
                }

                // Store dependencies.  Don't bother if the token length is greater than the parser's token limit.
                if (s.tokens.size() <= Integer.parseInt(NewsProperties.getProperty("corenlp.parse.maxlen"))) {
                    SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                    s.dependencies = new ArrayList<String>();
                    Collections.addAll(s.dependencies, dependencies.toPOSList().split("\n"));
                }

                // Sentiment
                s.sentiments = new ArrayList<Sentiment>();

                // XXX  For now, there is just one sentiment annotation.
                {
                    Sentiment sentiment = new Sentiment();
                    sentiment.model = "corenlp.default";
                    sentiment.prediction = sentence.get(SentimentCoreAnnotations.ClassName.class);

                    // XXX  Hard code this for now.  This comes from StanfordCoreNLP.annotators[SentimentAnnotator].model.op.classNames.
                    String[] classes = new String[] { "Very negative", "Negative", "Neutral", "Positive", "Very positive" };

                    sentiment.predictions = new Predictions();
                    Label label = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class).label();
                    if (label instanceof CoreLabel) {
                        CoreLabel cl = (CoreLabel)label;
                        SimpleMatrix matrix = cl.get(RNNCoreAnnotations.Predictions.class);
                        for (int i=0; i<matrix.numRows(); i++) {
                            sentiment.predictions.put(classes[i], matrix.get(i, 0));
                        }
                    }

                    s.sentiments.add(sentiment);
                }

                // Store the sentence.
                article.corenlp.sentences.add(s);

                // Calculate token statistics.
                totalTokens += s.tokens.size();
                minTokensPerSentence = Math.min(minTokensPerSentence, s.tokens.size());
                maxTokensPerSentence = Math.max(maxTokensPerSentence, s.tokens.size());
                meanTokensPerSentence = meanTokensPerSentence + ((s.tokens.size() - meanTokensPerSentence) / article.corenlp.sentences.size());
            }

            // Store token statistics.
            article.corenlp.statistics = new Statistics();
            article.corenlp.statistics.put("document:tokens:total", totalTokens);
            article.corenlp.statistics.put("sentences:tokens:min", minTokensPerSentence);
            article.corenlp.statistics.put("sentences:tokens:max", maxTokensPerSentence);
            article.corenlp.statistics.put("sentences:tokens:mean", meanTokensPerSentence);

            // Calculate and store time statistics.
            Map<String, Long> annotatorTimes = pipeline.getAnnotatorTimes(false);
            long totalTime = 0;
            for (Map.Entry<String, Long> entry : annotatorTimes.entrySet()) {
                long time = entry.getValue();
                article.corenlp.statistics.put(String.format("time:annotator:%s", entry.getKey()), time);
                totalTime += time;
            }
            article.corenlp.statistics.put("time:total", totalTime);

            this.successful = true;
        }
        finally {
        }
    }

    @Override
    public String toString() {
        return String.format("%s\t%s", super.toString(), article.file);
    }
}
