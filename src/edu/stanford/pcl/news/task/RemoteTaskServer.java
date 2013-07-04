
package edu.stanford.pcl.news.task;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTaskServer extends Remote {
    public Task takeTask(String workerId) throws RemoteException;
    public void returnTask(Task task) throws RemoteException;
}
