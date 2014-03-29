package edu.stanford.pcl.news.indexer;

import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.model.entity.Dependency;
import edu.stanford.pcl.news.model.entity.Sentence;
import edu.stanford.pcl.news.model.entity.Token;
import edu.stanford.pcl.news.task.Task;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.*;

import static org.elasticsearch.client.Requests.createIndexRequest;

public class ElasticsearchTransformAndIndexTask extends Task {
    private static final long serialVersionUID = 4733805814140484587L;

    private Article article;
    private transient Client client;
    private String indexName = "gist";

    public ElasticsearchTransformAndIndexTask(Article article) {
        this.article = article;
    }

    @Override
    public void initialize() throws IOException {
        try {
            // 9200 is for HTTP
            // 9300 is for the Java driver
            this.client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
        }
        catch (ElasticsearchException e) {
//            e.printStackTrace();
        }

        // XXX  ?
        this.client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
        ClusterStateResponse response = this.client.admin().cluster().prepareState().execute().actionGet();
        boolean hasIndex = response.getState().metaData().hasIndex(this.indexName);

        if (hasIndex) {
            return;
        }

        this.client.admin().indices().create(createIndexRequest(this.indexName)).actionGet();

        // Sentence mapping.
        XContentBuilder sentenceBuilder = XContentFactory.jsonBuilder().
                startObject().
                startObject("sentence").
                startObject("properties").
                startObject("persons").
                field("type", "string").field("store", "yes").field("index", "not_analyzed").
                endObject().
                startObject("organizations").
                field("type", "string").field("store", "yes").field("index", "not_analyzed").
                endObject().
                startObject("locations").
                field("type", "string").field("store", "yes").field("index", "not_analyzed").
                endObject().
                startObject("subjects").
                field("type", "string").field("store", "yes").field("index", "analyzed").
                endObject().
                startObject("objects").
                field("type", "string").field("store", "yes").field("index", "analyzed").
                endObject().
                startObject("sentiment").
                field("type", "string").field("store", "yes").field("index", "not_analyzed").
                endObject().
                startObject("indextext").
                field("type", "string").field("store", "yes").field("index", "analyzed").
                endObject().
                startObject("text").
                field("type", "string").field("store", "yes").field("index", "analyzed").
                endObject().
                startObject("date").
                field("type", "date").field("store", "yes").
                endObject().
                startObject("parent_id").
                field("type", "string").field("store", "yes").
                startObject("media_id").
                field("type", "integer").field("store", "yes").
                endObject().
                endObject().
                endObject().
                endObject();
        client.admin().indices().preparePutMapping(indexName).setType("sentence").setSource(sentenceBuilder).execute().actionGet();

        // Article mapping.
        XContentBuilder articleBuilder = XContentFactory.jsonBuilder().
                startObject().
                startObject("article").
                startObject("properties").
                startObject("persons").
                field("type", "string").field("store", "yes").field("index", "not_analyzed").
                endObject().
                startObject("organizations").
                field("type", "string").field("store", "yes").field("index", "not_analyzed").
                endObject().
                startObject("locations").
                field("type", "string").field("store", "yes").field("index", "not_analyzed").
                endObject().
                startObject("subjects").
                field("type", "string").field("store", "yes").field("index", "analyzed").
                endObject().
                startObject("objects").
                field("type", "string").field("store", "yes").field("index", "analyzed").
                endObject().
                startObject("indextext").
                field("type", "string").field("store", "yes").field("index", "analyzed").
                endObject().
                startObject("date").
                field("type", "date").field("store", "yes").
                startObject("media_id").
                field("type", "integer").field("store", "yes").
                endObject().
                endObject().
                endObject().
                endObject();
        client.admin().indices().preparePutMapping(indexName).setType("article").setSource(articleBuilder).execute().actionGet();
    }

    @Override
    public void execute() {
        List<Sentence> sentences = new ArrayList<Sentence>();

        try {
            sentences = this.article.corenlp.sentences;
        }
        catch (Exception e) {
//            e.printStackTrace();
        }

        ArrayList<String> tokens = new ArrayList<String>();
        Set<String> persons = new HashSet<String>();
        Set<String> organizations = new HashSet<String>();
        Set<String> locations = new HashSet<String>();
        Set<String> subjects = new HashSet<String>();
        Set<String> objects = new HashSet<String>();

        Iterator<Sentence> sentenceIterator = null;
        try {
            sentenceIterator = sentences.iterator();
        }
        catch (Exception e) {
//            e.printStackTrace();
        }

        if (sentenceIterator != null) {
            // Iterate through sentences, extract the NLP annotations, transform and add to sentence object for indexing.
            // N-grams for persons, organizations, locations, subjects, objects, and sentiment.

            while (sentenceIterator.hasNext()) {
                Sentence sentence = sentenceIterator.next();
                sentence.persons = new HashSet<String>();
                sentence.organizations = new HashSet<String>();
                sentence.locations = new HashSet<String>();
                sentence.text = "";

                StringBuilder sentencesb = new StringBuilder();

                Iterator<Token> tokenIterator = sentence.tokens.iterator();

                StringBuilder currentPerson = new StringBuilder();
                StringBuilder currentOrganization = new StringBuilder();
                StringBuilder currentLocation = new StringBuilder();
                List<String> sentencetext = new ArrayList<String>();

                while (tokenIterator.hasNext()) {
                    Token token = tokenIterator.next();
                    tokens.add(token.lemma.toLowerCase());

                    sentencetext.add(token.lemma.toLowerCase());
                    sentencesb.append(token.word).append(" ");

                    if (token.ne.equals("PERSON")) {
                        currentPerson.append(token.word).append(" ");
                    }
                    else {
                        if (currentPerson.length() > 0) {
                            persons.add(currentPerson.toString().trim());
                            sentence.persons.add(currentPerson.toString().trim());
                            currentPerson = new StringBuilder();
                        }
                    }

                    if (token.ne.equals("ORGANIZATION")) {
                        currentOrganization.append(token.word).append(" ");
                    }
                    else {
                        if (currentOrganization.length() > 0) {
                            organizations.add(currentOrganization.toString().trim());
                            sentence.organizations.add(currentOrganization.toString().trim());
                            currentOrganization = new StringBuilder();
                        }
                    }
                    if (token.ne.equals("LOCATION")) {
                        currentLocation.append(token.word).append(" ");
                    }
                    else {
                        if (currentLocation.length() > 0) {
                            locations.add(currentLocation.toString().trim());
                            sentence.locations.add(currentLocation.toString().trim());
                            currentLocation = new StringBuilder();
                        }
                    }
                }

                List<Dependency> dependencies = sentence.dependencies;

                for (Dependency dependency : dependencies) {
                    if (dependency.rel.equals("subj")
                            || dependency.rel.equals("nsubj")
                            || dependency.rel.equals("nsubjpass")
                            || dependency.rel.equals("csubj")
                            || dependency.rel.equals("csubjpass")
                            ) {
                        subjects.add(dependency.dep.word);
                    }

                    if (dependency.rel.equals("obj")
                            || dependency.rel.equals("dobj")
                            || dependency.rel.equals("pobj")
                            || dependency.rel.equals("xobj")
                            || dependency.rel.equals("iobj")
                            ) {
                        objects.add(dependency.dep.word);
                    }

                }

                // XXX  Needs to pull by model name.
                sentence.sentiment = sentence.sentiments.get(0).prediction;
                sentence.subjects = subjects;
                sentence.objects = objects;

                Collections.sort(sentencetext);
                sentence.text = sentencesb.toString().trim();
                sentencesb = new StringBuilder();

                for (String t : sentencetext) {
                    sentencesb.append(t).append(" ");
                }
                sentence.indextext = sentencesb.toString().replaceAll("[^a-zA-Z ]", "").trim();


                try {
                    indexSentence(sentence, article);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Collections.sort(tokens);
        StringBuilder stringBuilder = new StringBuilder();

        for (String token : tokens) {
            stringBuilder.append(token).append(" ");
        }

        String indextext = stringBuilder.toString().replaceAll("[^a-zA-Z ]", "");

        if (article.indextext == null) {
            article.indextext = indextext;
        }
        else {
            article.indextext = article.indextext.concat(indextext);
        }

        if (!persons.isEmpty()) {
            article.persons = persons;
        }
        else {
            Set<String> currentPersons = article.persons;
            if (currentPersons == null) {
                currentPersons = new HashSet<String>();
            }
            currentPersons.addAll(persons);
            article.persons = currentPersons;
        }

        if (!organizations.isEmpty()) {
            article.organizations = organizations;
        }
        else {
            Set<String> currentOrganizations = article.organizations;
            if (currentOrganizations == null) {
                currentOrganizations = new HashSet<String>();
            }
            currentOrganizations.addAll(organizations);
            article.organizations = currentOrganizations;
        }

        if (!locations.isEmpty()) {
            article.locations = locations;
        }
        else {
            Set<String> currentLocations = article.locations;
            if (currentLocations == null) {
                currentLocations = new HashSet<String>();
            }
            currentLocations.addAll(locations);
            article.locations = currentLocations;
        }

        if (!subjects.isEmpty()) {
            article.subjects = subjects;
        }
        else {
            Set<String> currentSubjects = article.subjects;
            if (currentSubjects == null) {
                currentSubjects = new HashSet<String>();
            }
            currentSubjects.addAll(subjects);
            article.subjects = currentSubjects;
        }

        if (!objects.isEmpty()) {
            article.objects = objects;
        }
        else {
            Set<String> currentObjects = article.objects;
            if (currentObjects == null) {
                currentObjects = new HashSet<String>();
            }
            currentObjects.addAll(objects);
            article.objects = currentObjects;
        }

        IndexResponse indexResponse = null;
        try {
            indexResponse = indexArticle(article);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        this.client.close();
        if (indexResponse != null) {
            this.successful = indexResponse.isCreated();
        }
    }

    public IndexResponse indexArticle(Article a) throws IOException {

        XContentBuilder sourceBuilder = XContentFactory.jsonBuilder().startObject()
                .field("persons", a.persons)
                .field("organizations", a.organizations)
                .field("date", a.publish_date.toString())
                .field("locations", a.locations)
                .field("media_id", a.media_id)
                .field("subjects", a.subjects)
                .field("objects", a.objects)
                .field("indextext", a.indextext);
        IndexRequest request = new IndexRequest(indexName, "article").id(String.valueOf(a._id)).source(sourceBuilder);

        return client.index(request).actionGet();
    }

    public IndexResponse indexSentence(Sentence s, Article a) throws IOException {
        XContentBuilder sourceBuilder = XContentFactory.jsonBuilder().startObject()
                .field("persons", s.persons)
                .field("organizations", s.organizations)
                .field("date", a.publish_date.toString())
                .field("locations", s.locations)
                .field("sentiment", s.sentiment)
                .field("subjects", s.subjects)
                .field("objects", s.objects)
                .field("parent_id", a._id)
                .field("media_id", a.media_id)
                .field("text", s.text)
                .field("indextext", s.indextext);
        IndexRequest request = new IndexRequest(indexName, "sentence").source(sourceBuilder);

        return client.index(request).actionGet();

    }

    @Override
    public String toString() {
        return String.format("%s\t%s", super.toString(), article.file);
    }


}
