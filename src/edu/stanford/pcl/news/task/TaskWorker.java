
package edu.stanford.pcl.news.task;

import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class TaskWorker extends Thread {
    private String id;
    private Registry registry;
    protected Map<String, Object> resources;

    public TaskWorker() {
        this.id = ManagementFactory.getRuntimeMXBean().getName();
        this.resources = new HashMap<String, Object>();
        this.setDaemon(false);
    }

    public void register(String host) throws RemoteException {
        this.registry = LocateRegistry.getRegistry(host);
    }

    public void run() {
        try {
            RemoteTaskServer server = (RemoteTaskServer)this.registry.lookup("TaskServer");

            Task task;
            while ((task = server.takeTask()) != null) {
                task.execute();
                server.returnTask(task);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            String taskServerHost = "127.0.0.1";
            for (String arg : args) {
                String[] parts = arg.split("=");
                if (parts.length == 2 && parts[0].equals("--taskserver")) {
                    taskServerHost = parts[1];
                }
            }
            TaskWorker worker = new TaskWorker();
            worker.register(taskServerHost);
            worker.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
