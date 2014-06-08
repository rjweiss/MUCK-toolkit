
package edu.stanford.pcl.news.task;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import edu.stanford.pcl.news.NewsProperties;

public class TaskWorker extends Thread {
    private String id;
    private Registry registry;
    protected Map<String, Object> resources;

    public TaskWorker() {
        try {
            this.id = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
            this.id = UUID.randomUUID().toString();
        }
        this.resources = new HashMap<String, Object>();
        this.setDaemon(false);
    }

    public void register(String host) throws RemoteException {
        this.registry = LocateRegistry.getRegistry(host, Integer.parseInt(NewsProperties.getProperty("rmi.registry.port")));
    }

    public void run() {
        try {
            RemoteTaskServer server = (RemoteTaskServer)this.registry.lookup("TaskServer");
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Task task;

            while ((task = server.takeTask(this.id)) != null) {
                task.initialize();
                task.setWorkerId(this.id);
                Future future = executor.submit(task);
                try {
                    future.get(Integer.parseInt(NewsProperties.getProperty("task.abort.seconds")), TimeUnit.SECONDS);
                    server.returnTask(task);

                    if (task instanceof TerminateTask) {
                        break;
                    }
                }
                catch (TimeoutException e) {
                    task.successful = false;
                    task.timedOut = true;
                    server.returnTask(task);

                    // XXX  This would be great, but the pipeline swallows InterruptedException!
//                    future.cancel(true);

                    // All bets are off!
                    System.exit(1);
                }
                catch (CancellationException e) {
                    System.out.println("TaskWorker.run(): cancelled");
                }
                catch (ExecutionException e) {
                    System.out.println("TaskWorker.run(): threw an exception");
                    e.printStackTrace();
                }
                catch (InterruptedException e) {
                    System.out.println("TaskWorker.run(): interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        }
        catch (Exception e) {
            System.out.println("TaskWorker.run(): unexpected exception");
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

            // Print log column headers.
            System.out.println("UNIXTIMESTAMP\tTOTALMEM\tFREEMEM\tOPERATION\tTASKNAME\tTASKWORKERID\tDONE\tSUCCESS\tTIMEOUT\tMS\tFILE");

            TaskWorker worker = new TaskWorker();
            worker.register(taskServerHost);
            worker.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
