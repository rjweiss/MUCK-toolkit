
package edu.stanford.pcl.news.task;

import edu.stanford.pcl.news.corenlp.CoreNlpTask;
import edu.stanford.pcl.news.parser.ParserTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private TaskQueue returnQueue;


    private TaskServer() {
        this.taskQueue = new TaskQueue();
        this.returnQueue = new TaskQueue();
    }


    @Override
    public Task takeTask() throws RemoteException {
        return taskQueue.take();
    }

    @Override
    public void returnTask(Task task) throws RemoteException {
        if (task instanceof ParserTask) {
            ParserTask t = (ParserTask)task;
            System.out.printf("%d\tParserTask\t%d\t%d\t%s\n", System.currentTimeMillis(), t.getArticle().body.length(), task.executionMillis, t.getArticle().file);
            returnQueue.put(new CoreNlpTask(((ParserTask)task).getArticle()));
        }
        else if (task instanceof CoreNlpTask) {
            CoreNlpTask t = (CoreNlpTask)task;
            System.out.printf("%d\tCoreNlpTask\t%d\t%d\t%s\n", System.currentTimeMillis(), t.getArticle().body.length(), task.executionMillis, t.getArticle().file);
        }
    }


    private void traverse(File file) {
        if (file == null) return;
        File[] files = file.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".xml")) {
                try {
                    // Schedule a task from the return queue before scheduling any more new tasks.
                    if (returnQueue.peek() != null) {
                        taskQueue.put(returnQueue.poll());
                    }
                    Task task = new ParserTask(f.getAbsolutePath(), readFile(f));
                    taskQueue.put(task);
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



    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry();

            server = new TaskServer();
            registry.rebind("TaskServer", UnicastRemoteObject.exportObject(server, 23456)); //  XXX  Some arbitrary port.

            System.out.println("Server ready.");

            // For now, simply enqueue a task for each article.
            File dataRootDirectory = new File("/news/data/");
            if (!dataRootDirectory.exists()) {
                // XXX  Just for development...
                dataRootDirectory = new File("data");
            }
            server.traverse(dataRootDirectory);

            System.out.println("Traversal completed.");
        }
        catch (RemoteException e) {
            // XXX ...
            e.printStackTrace();
        }
    }

}
