package edu.stanford.pcl.newspaper;

/**
 * Created by IntelliJ IDEA.
 * User: Rebecca
 * Date: 6/19/12
 * Time: 1:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class QueryTool {
    public static void main(String[] args) {

//        // 2. query
//        String query = "headline:Mirrors";
//
//        // the "title" arg specifies the default field to use
//        // when no field is explicitly specified in the query.
//        Query q = new QueryParser(Version.LUCENE_35, "title", analyzer).parse(query);
//
//        // 3. search
//        int hitsPerPage = 10;
//        IndexReader reader = IndexReader.open(index);
//        IndexSearcher searcher = new IndexSearcher(reader);
//        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
//        searcher.search(q, collector);
//        ScoreDoc[] hits = collector.topDocs().scoreDocs;
//
//        // 4. display results
//        System.out.println("Found " + hits.length + " hits.");
//        for (int i = 0; i < hits.length; ++i) {
//            int docId = hits[i].doc;
//            Document d = searcher.doc(docId);
//            System.out.println((i + 1) + ". " + d.get("headline"));
//        }
//
//        // searcher can only be closed when there
//        // is no need to access the documents any more.
//        searcher.close();
    }
}
