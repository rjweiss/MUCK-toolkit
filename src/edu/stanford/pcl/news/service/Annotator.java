
package edu.stanford.pcl.news.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.pcl.news.corenlp.CoreNlpFactory;
import edu.stanford.pcl.news.corenlp.CoreNlpPipeline;
import edu.stanford.pcl.news.model.entity.*;
import org.ejml.simple.SimpleMatrix;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;


@Path("annotator")
public class Annotator {

    private static int PARSE_MAXLEN = 150;

    private static Gson gson;
    private static CoreNlpPipeline corenlp;
    static {
        gson = new GsonBuilder().setPrettyPrinting().create();
        corenlp = CoreNlpFactory.getPipeline("tokenize, ssplit, pos, lemma, ner, parse, sentiment");
    }

    private static final AtomicInteger nextId = new AtomicInteger(0);
    private static final ThreadLocal<Integer> threadId = new ThreadLocal<Integer>() {
        @Override protected Integer initialValue() {
            return nextId.getAndIncrement();
        }
    };
    private static ConcurrentMap<Integer, Map<Integer, Long>> incidencesByThread = new ConcurrentSkipListMap<Integer, Map<Integer, Long>>();
    private static ConcurrentMap<Integer, Map<Integer, Double>> timesByThread = new ConcurrentSkipListMap<Integer, Map<Integer, Double>>();


    @Context
    private UriInfo context;

    public Annotator() {
    }

    @GET
    @Produces("text/plain")
    public String status() {
//        Set<Thread> threads = Thread.getAllStackTraces().keySet();
//        return String.format("Thread Count: %d", threads.size());

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Thread Count: %d\n\n", nextId.get()));

        Map<Integer, Long> totalIncidences = new TreeMap<Integer, Long>();
        Map<Integer, Double> totalTimes = new TreeMap<Integer, Double>();

        for (Integer threadId : incidencesByThread.keySet()) {
            Map<Integer, Long> incidences = incidencesByThread.get(threadId);
            Map<Integer, Double> times = timesByThread.get(threadId);

            for (Integer tokenCount : incidences.keySet()) {
                Long incidence = incidences.get(tokenCount);
                Double time = times.get(tokenCount);

                Long totalIncidence = totalIncidences.get(tokenCount);
                if (totalIncidence == null) totalIncidence = 0L;
                totalIncidences.put(tokenCount, totalIncidence + incidence);

                Double totalTime = totalTimes.get(tokenCount);
                if (totalTime == null) totalTime = 0.0;
                totalTimes.put(tokenCount, (totalTime + time) / (totalIncidence + incidence));

//                sb.append(String.format("%6d\t%6d\t%8d\t%8.0f\n", threadId, tokenCount, incidence, time));
            }
        }

        sb.append("TOKENS\t   COUNT\t    TIME\n");
        for (Integer tokenCount : totalIncidences.keySet()) {
            Long incidence = totalIncidences.get(tokenCount);
            Double time = totalTimes.get(tokenCount);
            sb.append(String.format("%6d\t%8d\t%8.0f\n", tokenCount, incidence, time));
        }
        return sb.toString();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject annotate(JsonObject request) {

        if (request == null || request.get("text") == null) {
            throw new ServiceException("Request entity missing required attribute: text", Response.Status.BAD_REQUEST);
        }

        // Statistics counters.
        int totalTokens = 0;
        int minTokensPerSentence = Integer.MAX_VALUE;
        int maxTokensPerSentence = 0;
        double meanTokensPerSentence = 0;

        try {
            long start = System.currentTimeMillis();

            String text = request.get("text").getAsString();
            Annotation document = corenlp.process(text);

            Article article = new Article();

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
//                if (s.tokens.size() <= PARSE_MAXLEN) {
//                    SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//                    s.dependencies = new ArrayList<String>();
//                    Collections.addAll(s.dependencies, dependencies.toPOSList().split("\n"));
//                }

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
            Map<String, Long> annotatorTimes = corenlp.getAnnotatorTimes(false);
            long totalTime = 0;
            for (Map.Entry<String, Long> entry : annotatorTimes.entrySet()) {
                long time = entry.getValue();
                article.corenlp.statistics.put(String.format("time:annotator:%s", entry.getKey()), time);
                totalTime += time;
            }
            article.corenlp.statistics.put("time:total", totalTime);

            // Update average web service timings.
            long stop = System.currentTimeMillis();

            Map<Integer, Long> incidences = incidencesByThread.get(threadId.get());
            if (incidences == null) {
                incidences = new TreeMap<Integer, Long>();
                incidencesByThread.put(threadId.get(), incidences);
            }
            Long incidence = incidences.get(totalTokens);
            if (incidence == null) incidence = 0L;
            incidences.put(totalTokens, incidence + 1);

            Map<Integer, Double> times = timesByThread.get(threadId.get());
            if (times == null) {
                times = new TreeMap<Integer, Double>();
                timesByThread.put(threadId.get(), times);
            }
            Double time = times.get(totalTokens);
            if (time == null) time = 0.0;
            times.put(totalTokens, (time + (stop - start)) / (incidence + 1));


            return (JsonObject)gson.toJsonTree(article);
        }
        catch (Throwable t) {
            throw new ServiceException(t.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
