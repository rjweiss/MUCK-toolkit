
package edu.stanford.pcl.news.task;

import java.lang.reflect.Type;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTaskServer extends Remote {
    void putTask(Task task) throws RemoteException;
    void putContinuationTask(Task task) throws RemoteException;
    Task takeTask(String workerId) throws RemoteException;
    void returnTask(Task task) throws RemoteException;
    void registerResolver(Type taskType, TaskResolver resolver) throws RemoteException;
}
