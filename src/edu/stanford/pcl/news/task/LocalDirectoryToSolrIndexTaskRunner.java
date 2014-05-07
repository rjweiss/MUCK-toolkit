
package edu.stanford.pcl.news.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.parser.ParserTask;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;


public class LocalDirectoryToSolrIndexTaskRunner extends TaskRunner {

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

    private SolrServer solr;


    public LocalDirectoryToSolrIndexTaskRunner(String directory, String core) {
        // XXX  Need at least one worker.
        registerWorker(new TaskWorker());

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

        String url = String.format("http://localhost:8983/solr/%s", core);
        solr = new HttpSolrServer(url);

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
            public void resolve(ParserTask task) throws RemoteException {
                Task continuationTask = new CoreNlpTask(task.getArticle());
                server.putContinuationTask(continuationTask);
            }
        });

        registerResolver(CoreNlpTask.class, new TaskResolver<CoreNlpTask>() {
            @Override
            public void resolve(CoreNlpTask task) {
                if (task.isSuccessful()) {
                    Article a = task.getArticle();

//                    solr.addBean(a);

                    SolrInputDocument doc = new SolrInputDocument();
                    doc.addField("id", UUID.randomUUID().toString());
                    doc.addField("file", a.file);
                    doc.addField("outlet", a.outlet);
                    doc.addField("date", a.date);
                    doc.addField("page", a.page);
                    doc.addField("headline", a.headline);
                    doc.addField("body", a.body);

                    try {
                        solr.add(doc);
                        solr.commit();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
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

