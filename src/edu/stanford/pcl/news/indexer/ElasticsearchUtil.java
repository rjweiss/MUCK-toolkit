package edu.stanford.pcl.news.indexer;


import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;


public class ElasticsearchUtil {
    private static Client client;

    public static Client getClient() throws ElasticsearchException {
        if (client == null) {
            // 9200 is for HTTP
            // 9300 is for the Java driver
            client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
        }
        return client;
    }

    public static void closeClient(Client client) {
        if (client != null) {
            client.close();
        }
        if (client == ElasticsearchUtil.client) {
            ElasticsearchUtil.client = null;
        }
    }


    private ElasticsearchUtil() {
    }
}
