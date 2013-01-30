
package edu.stanford.pcl.news.task;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import edu.stanford.pcl.news.NewsProperties;
import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.model.Serialization;
import edu.stanford.pcl.news.model.db.DbConnection;
import edu.stanford.pcl.news.parser.ParserTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class TaskServer implements RemoteTaskServer {
    // XXX  Needed to hold a strong reference to the server in the RMI registry.
    private static TaskServer server;

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


    private TaskQueue taskQueue;
    private DbConnection dbConnection;


    public TaskServer() throws UnknownHostException {
        this.taskQueue = new TaskQueue();
        this.dbConnection = new DbConnection("news");
    }


    public void start() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("TaskServer", UnicastRemoteObject.exportObject(server, 23456)); //  XXX  Some arbitrary port.
        System.out.println("Server ready.");
    }

    @Override
    public Task takeTask() throws RemoteException {
        Task task = taskQueue.take();
        log("dequeued", task);
        return task;
    }

    @Override
    public void returnTask(Task task) throws RemoteException {
        log("returned", task);

        boolean resolved = taskQueue.resolve(task);
        if (!resolved) return;

        log("resolved", task);

        // XXX  Shouldn't do this directly from the server.  Maybe defined in the task or some sort of workload object?
        if (task instanceof ParserTask) {
            ParserTask t = (ParserTask)task;
            if (t.isSuccessful()) {
                taskQueue.putContinuationTask(new CoreNlpTask(((ParserTask)task).getArticle()));
            }
        }
        else if (task instanceof CoreNlpTask) {
            CoreNlpTask t = (CoreNlpTask)task;
            if (t.isSuccessful()) {
                // XXX  Not sure if this should really happen here, but...
                DBCollection articles = dbConnection.getCollection("articles");
                articles.save((DBObject)JSON.parse(Serialization.toJson(t.getArticle())));
            }
        }
    }


    private void traverse(File file) {
        if (file == null) return;
        File[] files = file.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".xml")) {
                try {
                    Task task = new ParserTask(f.getAbsolutePath(), readFile(f));
                    taskQueue.putPrimaryTask(task);
                    log("enqueued", task);
                }
                catch (IOException e) {
                    // XXX  Log and skip it?
                }
            }
            else if (f.isDirectory()) {
                traverse(f);
            }
        }
    }


    private static void log(String operation, Task task) {
        System.out.printf("%d\t%s\t%s\n", System.currentTimeMillis(), operation, task);
    }

    public static void main(String[] args) {
        try {
            server = new TaskServer();
            server.start();

            // Print log column headers.
            System.out.println("UNIXTIMESTAMP\tOPERATION\tTASKNAME\tVMID\tDONE\tSUCCESS\tMS\tFILE");


            // For now, simply enqueue a task for each article.
            File dataRootDirectory = new File(NewsProperties.getProperty("data.root.path"));
            if (dataRootDirectory.exists()) {
                server.traverse(dataRootDirectory);
            }

            System.out.println("Traversal completed.");
        }
        catch (Exception e) {
            // XXX ...
            e.printStackTrace();
        }
    }

}
