package edu.stanford.pcl.news.aws;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.pcl.news.NewsProperties;
import edu.stanford.pcl.news.task.RemoteTaskServer;
import edu.stanford.pcl.news.task.TaskRunner;

public abstract class AwsTaskRunner extends TaskRunner {
    private String serverHost = "127.0.0.1";

    private String name;
    private String ami;
    private String instanceType;
    private String keyPairName;
    private String securityGroupName;
    private int workerCount;
    private String spotPrice;


    public AwsTaskRunner(String name, String ami, String instanceType, String keyPairName, String securityGroupName, int workerCount, String spotPrice) {
        this.name = name;
        this.ami = ami;
        this.instanceType = instanceType;
        this.keyPairName = keyPairName;
        this.securityGroupName = securityGroupName;
        this.workerCount = workerCount;
        this.spotPrice = spotPrice;
    }


    @Override
    public void start() throws RemoteException {
        String serverUserData = AwsUtil.createUserDataScript(
                "wget https://s3.amazonaws.com/MUCK/muck-bundle-0.0.2.jar",
                "java -cp muck-bundle-0.0.2.jar edu.stanford.pcl.news.aws.AwsTaskServer > /server.log 2>&1"
        );
        String instanceId = AwsUtil.startInstance(String.format("%s %s", name, "Instance"), ami, instanceType, keyPairName, securityGroupName, serverUserData);

        List<String> ids = new ArrayList<String>();
        ids.add(instanceId);
        AwsUtil.waitForInstances(ids);

        try {
            serverHost = AwsUtil.getInstancePublicIpv4Address(instanceId);
            System.out.println(String.format("server: %s %s ", instanceId, serverHost));
            Registry registry = LocateRegistry.getRegistry(serverHost, Integer.parseInt(NewsProperties.getProperty("rmi.registry.port")));
            server = (RemoteTaskServer)registry.lookup("TaskServer");
        }
        catch (NotBoundException e) {
            e.printStackTrace();
        }

        String workerUserData = AwsUtil.createUserDataScript(
                "wget https://s3.amazonaws.com/MUCK/muck-bundle-0.0.1.jar",
                String.format("java -cp muck-bundle-0.0.1.jar edu.stanford.pcl.news.task.TaskWorker --taskserver=%s > /worker.log 2>&1", serverHost)
        );
        ArrayList<String> workerInstanceIds = AwsUtil.startSpotInstances(String.format("%s %s", name, "Spot Instance"), workerCount, spotPrice, ami, instanceType, keyPairName, securityGroupName, workerUserData);

        AwsUtil.waitForInstances(workerInstanceIds);

        for (String id : workerInstanceIds) {
            String host = AwsUtil.getInstancePublicIpv4Address(id);
            System.out.println(String.format("worker: %s %s ", id, host));
        }

        super.start();
    }

}
