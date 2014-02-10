package edu.stanford.pcl.news.task;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.parser.ParserTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.LinkedBlockingQueue;


public class LocalDirectoryToMongoCollectionTaskRunner extends TaskRunner {

    private static String readFile(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        }
        finally {
            stream.close();
        }
    }


    private LinkedBlockingQueue<Path> paths;
    private SimpleFileVisitor<Path> fileVisitor;

    private MongoClient mongodb;
    private DB db;
    private DBCollection collection;


    public LocalDirectoryToMongoCollectionTaskRunner(String directory, String host, String db, String collection) {
        paths = new LinkedBlockingQueue<Path>(1);

        fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                if (file.toString().endsWith(".txt")) {
                    try {
                        paths.put(file);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            this.mongodb = new MongoClient(host);
            this.db = mongodb.getDB(db);
            this.collection = this.db.getCollection(collection);
        }
        catch (UnknownHostException e) {
            // XXX
            e.printStackTrace();
        }

        // XXX  Maybe move this to a TaskRunner init() method.
        final String directoryPath = directory;
        new Thread() {
            @Override
            public void run() {
                try {
                    Files.walkFileTree(new File(directoryPath).toPath(), fileVisitor);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        registerResolver(ParserTask.class, new TaskResolver<ParserTask>() {
            @Override
            public void resolve(ParserTask task) {
                Task continuationTask = new CoreNlpTask(task.getArticle());
                server.getTaskQueue().putContinuationTask(continuationTask);
            }
        });

        final DBCollection dbCollection = this.collection;
        registerResolver(CoreNlpTask.class, new TaskResolver<CoreNlpTask>() {
            @Override
            public void resolve(CoreNlpTask task) {
                if (task.isSuccessful()) {
                    dbCollection.save((DBObject)JSON.parse(Serialization.toMongoJson(task.getArticle())));
                }
            }
        });
    }


    @Override
    public Task next() {
        try {
            Path path = paths.take();
            if (path == null) {
                return null;
            }

            File file = path.toFile();
            String content = readFile(file);
            return new ParserTask(file.getName(), content);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        catch (IOException e) {
            return null;
        }
    }
}
