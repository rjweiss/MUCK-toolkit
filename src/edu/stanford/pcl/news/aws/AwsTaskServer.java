package edu.stanford.pcl.news.aws;

import edu.stanford.pcl.news.NewsProperties;
import edu.stanford.pcl.news.task.TaskServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class AwsTaskServer extends TaskServer {
    // XXX  Needed to hold a strong reference to the server in the RMI registry.
    private static TaskServer server;

    @Override
    public String getHostname() {
        return AwsUtil.getPublicIpv4Address();
    }

    public void start() throws RemoteException {
        System.setProperty("java.rmi.server.hostname", getHostname());
        LocateRegistry.createRegistry(Integer.valueOf(NewsProperties.getProperty("rmi.registry.port")));
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("TaskServer", UnicastRemoteObject.exportObject(server, 23456)); //  XXX  Some arbitrary port.
        System.out.println("AwsTaskServer ready.");
    }


    public static void main(String[] args) {
        try {
            server = new AwsTaskServer();
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
