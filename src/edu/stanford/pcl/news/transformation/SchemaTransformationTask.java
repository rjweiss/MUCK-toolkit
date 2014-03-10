package edu.stanford.pcl.news.transformation;

import com.google.gson.Gson;
import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.model.entity.Sentence;
import edu.stanford.pcl.news.model.entity.Token;
import edu.stanford.pcl.news.task.Task;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.util.*;

public class SchemaTransformationTask  extends Task {

    private Article article;
    private transient Client client;

    public SchemaTransformationTask(Article article) {
        this.article = article;
    }

    public Article getArticle() {
        return article;
    }

    @Override
    public void initialize() {
        try {
            this.client = new TransportClient()
                    .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
        } catch (ElasticsearchException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() {
        List<Sentence> sentences = this.article.corenlp.sentences;
        ArrayList<String> tokens = new ArrayList<String>();
        HashSet<String> persons = new HashSet<String>();
        HashSet<String> organizations = new HashSet<String>();
        HashSet<String> locations = new HashSet<String>();

        Iterator<Sentence> sentenceIterator = sentences.iterator();
        while (sentenceIterator.hasNext()) {
            Sentence sentence = sentenceIterator.next();
            Iterator<Token> tokenIterator = sentence.tokens.iterator();

            while (tokenIterator.hasNext()) {
                Token token = tokenIterator.next();
                tokens.add(token.lemma.toLowerCase());

                // XXX Need to check if preceding or following word is same entity class, will need to be appended if so
                if (token.ne.equals("PERSON")) {
                    persons.add(token.word);
                }
                else if (token.ne.equals("ORGANIZATION")) {
                    organizations.add(token.word);
                }
                else if (token.ne.equals("LOCATION")) {
                    locations.add(token.word);
                }
            }

            Collections.sort(tokens);
            StringBuilder stringBuilder = new StringBuilder();

            for (String token : tokens) {
                stringBuilder.append(token);
                stringBuilder.append(" ");
            }

            String indextext = stringBuilder.toString().replaceAll("[^a-zA-Z ]", "");

            if (article.indextext == null) {
                article.indextext = indextext;
            }
            else {
                article.indextext = article.indextext.concat(indextext);
            }

            if (!persons.isEmpty()) {
                article.persons = persons.toString();
            } else {
                List<String> currentPersons = Arrays.asList(article.persons);
                currentPersons.addAll(persons);
                article.persons = currentPersons.toString();
            }

            if (!organizations.isEmpty()) {
                article.organizations = organizations.toString();
            } else {
                List<String> currentOrganizations = Arrays.asList(article.organizations);
                currentOrganizations.addAll(organizations);
                article.organizations = currentOrganizations.toString();
            }

            if (!locations.isEmpty()) {
                article.locations = locations.toString();
            } else {
                List<String> currentLocations = Arrays.asList(article.locations);
                currentLocations.addAll(locations);
                article.locations = currentLocations.toString();
            }

        }

        // Index into Elasticsearch.
        final String indexName = "gist";
        final String documentType = "article";
        final String documentId = String.valueOf(article._id);

//        client.admin().indices().prepareCreate(indexName).execute().actionGet();

        IndexRequest indexRequest = new IndexRequest(indexName, documentType, documentId);
        indexRequest.source(new Gson().toJson(this.article));
        IndexResponse indexResponse = client.index(indexRequest).actionGet();

        this.client.close();
        this.successful = true;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s", super.toString(), article.file);
    }


}
