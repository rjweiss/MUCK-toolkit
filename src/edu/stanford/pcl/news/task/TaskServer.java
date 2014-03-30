
package edu.stanford.pcl.news.task;

import edu.stanford.pcl.news.NewsProperties;

import java.lang.reflect.Type;
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

    public String getHostname() {
        return "127.0.0.1";
    }

    public void start() throws RemoteException {
        System.setProperty("java.rmi.server.hostname", getHostname());
        LocateRegistry.createRegistry(Integer.valueOf(NewsProperties.getProperty("rmi.registry.port")));
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("TaskServer", UnicastRemoteObject.exportObject(server, 23456)); //  XXX  Some arbitrary port.
        System.out.println("Server ready.");
    }

    @Override
    public void putTask(Task task) throws RemoteException {
        taskQueue.putPrimaryTask(task);
        log("enqueued", task);
    }

    @Override
    public void putContinuationTask(Task task) throws RemoteException {
        taskQueue.putContinuationTask(task);
        log("enqueued", task);
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
        }
        catch (Exception e) {
            // XXX ...
            e.printStackTrace();
        }
    }

}
