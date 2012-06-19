package edu.stanford.pcl.newspaper;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Rebecca
 * Date: 6/18/12
 * Time: 11:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Importer {
    private DBCollection collection;
    private IndexWriter indexWriter;

    private static final String MONGO_DB_NAME = "test";
    private static final String MONGO_DB_ARTICLES_COLLECTION = "articles";
    private static final String LUCENE_INDEX_DIRECTORY = "C:\\Users\\Rebecca\\Documents\\research\\newspaper-project\\lucene";
    private static final String ARTICLE_IMPORT_ROOT_DIRECTORY = "C:\\Users\\Rebecca\\Dropbox\\stanfordBigData\\sampleData\\02";

    public Importer(DBCollection collection, IndexWriter indexWriter) {
        this.collection = collection;
        this.indexWriter = indexWriter;
    }

    public void importAll(File path) {

        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                // Recursively import sub-directories.
                importAll(file);
            }
            else {
                // TODO:  Only import XML files, and probably do some sanity checking.

                System.out.println("Parsing " + file.getAbsolutePath() + "...");

                Article article;

                try {
                    // TODO:  Instantiate the correct Parser subclass based on some hint.
                    article = new NytParser().parse(file);
                }
                catch (Exception e) {
                    // Parse failed, complain and skip.
                    e.printStackTrace();
                    return;
                }

                if (article != null) {
                    // Parse failed, skip.
                    return;
                }

                // Redundancy party party.
                try {
                    BasicDBObject mongoObject = new BasicDBObject();
                    mongoObject.put("pageNumber", article.getPageNumber());
                    mongoObject.put("publicationDate", article.getPublicationDate());
                    mongoObject.put("headline", article.getHeadline());
                    mongoObject.put("text", article.getText());
                    mongoObject.put("fileName", article.getFileName());
                    mongoObject.put("mediaType", article.getMediaType());
                    mongoObject.put("mediaSource", article.getMediaSource());
                    collection.insert(mongoObject);
                }
                catch (Exception e) {
                    // TODO:  Abort on insert failure (catch reasonable exceptions).
                }

                try {
                    Document doc = new Document();
                    doc.add(new Field("pageNumber", article.getPageNumber(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                    doc.add(new Field("publicationDate", article.getPublicationDate(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                    doc.add(new Field("headline", article.getHeadline(), Field.Store.YES, Field.Index.ANALYZED));
                    doc.add(new Field("text", article.getText(), Field.Store.YES, Field.Index.ANALYZED));
                    doc.add(new Field("fileName", article.getFileName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                    doc.add(new Field("mediaType", article.getMediaType(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                    doc.add(new Field("mediaSource", article.getMediaSource(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                    indexWriter.addDocument(doc);
                }
                catch (IOException e) {
                    // TODO:  Roll back insert failure (even if unlikely).
                    e.printStackTrace();
                }
            }

        }
    }


    public static void main(String[] args) throws IOException {
        // Connect to MongoDB.
        Mongo mongo = new Mongo();
        DB db = mongo.getDB(MONGO_DB_NAME);

        // Create/Open Lucene index.
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
        Directory index = new SimpleFSDirectory(new File(LUCENE_INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        IndexWriter indexWriter = new IndexWriter(index, config);

        // Recursively parse and import XML files...
        Importer importer = new Importer(db.getCollection(MONGO_DB_ARTICLES_COLLECTION), indexWriter);
        importer.importAll(new File(ARTICLE_IMPORT_ROOT_DIRECTORY));

        // Clean up.
        indexWriter.close();
        mongo.close();
    }

}
