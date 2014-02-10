
package edu.stanford.pcl.news.task;

import edu.stanford.pcl.news.parser.ParserTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private Map<Type, List<TaskResolver>> resolvers;


    public TaskServer() {
        this.taskQueue = new TaskQueue();
        this.resolvers = new HashMap<Type, List<TaskResolver>>();
    }


    public void registerResolver(Type taskType, TaskResolver resolver) {
        List<TaskResolver> resolverList = this.resolvers.get(taskType);
        if (resolverList == null) {
            resolverList = new ArrayList<TaskResolver>();
            this.resolvers.put(taskType, resolverList);
        }
        resolverList.add(resolver);
    }

    public void start() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("TaskServer", UnicastRemoteObject.exportObject(server, 23456)); //  XXX  Some arbitrary port.
        System.out.println("Server ready.");
    }

    @Override
    public Task takeTask(String workerId) throws RemoteException {
        Task task = taskQueue.take();
        task.setWorkerId(workerId); // XXX  Really, this is just for logging.
        log("dequeued", task);
        return task;
    }

    @Override
    public void returnTask(Task task) throws RemoteException {
        log("returned", task);

        if (task.timedOut) {
            log("timedout", task);
        }

        boolean resolved = taskQueue.resolve(task);
        if (!resolved) return;  // XXX  Pretty sure this is going to prevent tasks from being completed on their last try.

        // XXX  Might want to let resolvers abort resolution.
        log("resolved", task);

        List<TaskResolver> resolverList = resolvers.get(task.getClass());
        if (resolverList != null) {
            for (TaskResolver resolver : resolverList) {
                resolver.resolve(task);
            }
        }
    }

    public TaskQueue getTaskQueue() {
        return taskQueue;
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
        System.out.printf("%d\t%d\t%s\t%s\n", System.currentTimeMillis(), Thread.currentThread().getId(), operation, task);
    }

    private static void log(String operation, String message) {
        System.out.printf("%d\t%d\t%s\t%s\n", System.currentTimeMillis(), Thread.currentThread().getId(), operation, message);
    }

    public static void main(String[] args) {
        try {
            server = new TaskServer();
            server.start();

            // Print log column headers.
            System.out.println("UNIXTIMESTAMP\tTID\tOPERATION\tTASKNAME\tTASKWORKERID\tDONE\tSUCCESS\tMS\tFILE");
            System.err.println("UNIXTIMESTAMP\tTID\tOPER\tTASKWORKERID\tT\tC\tR");


            // For now, simply enqueue a task for each article.
            File dataRootDirectory = new File("/news/data");
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
