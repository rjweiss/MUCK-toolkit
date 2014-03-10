package edu.stanford.pcl.news.elasticsearch;

import com.google.gson.Gson;
import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.task.Task;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.ElasticsearchException;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticsearchIndexingTask extends Task {

    private static final long serialVersionUID = 4077518524681583409L;
    private Article article;
    private transient Client client;

    public ElasticsearchIndexingTask(Article article) { // XXX Will need to be TransportClient.
        this.article = article;
        }

    public Article getArticle() {
        return article;
    }

    public Client getClient() {
        return client;
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
    public void execute() {

        final String indexName = "gist";
        final String documentType = "article";
        final String documentId = String.valueOf(article._id);

//        client.admin().indices().prepareCreate(indexName).execute().actionGet();

        IndexRequest indexRequest = new IndexRequest(indexName, documentType, documentId);
        indexRequest.source(new Gson().toJson(article));
        IndexResponse indexResponse = client.index(indexRequest).actionGet();

        this.successful = indexResponse.isCreated();
//        client.close();


//        IndexRequestBuilder indexRequestBuilder = client.prepareIndex(indexName, documentType, documentId);
//
//        XContentBuilder contentBuilder = null;
//        try {
//            contentBuilder = jsonBuilder().startObject().prettyPrint();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (contentBuilder != null) {
//            try {
//                contentBuilder
//                        .field("indextext", article.indextext)
//                        .field("date", article.date)
//                        .field("persons", article.persons)
//                        .field("organizations", article.organizations)
//                        .field("locations", article.locations)
//                        .endObject();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        indexRequestBuilder.setSource(contentBuilder);
//        indexRequestBuilder.execute().actionGet();
//        client.close();
//        this.successful = true;
    }

}