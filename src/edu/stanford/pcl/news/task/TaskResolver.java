package edu.stanford.pcl.news.task;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TaskResolver<T extends Task> extends Remote {
    public void resolve(T task) throws RemoteException;
}
