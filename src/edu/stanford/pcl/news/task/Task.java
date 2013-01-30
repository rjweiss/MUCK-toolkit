
package edu.stanford.pcl.news.task;

import java.io.Serializable;
import java.util.UUID;


/**
 * Provides a base class for representing distributed tasks.  A <code>Task</code> object should fully describe
 * a distributable unit of work and, eventually, the result of the completed work.
 */
public abstract class Task implements Serializable {
    private static final long serialVersionUID = -5727217829452869247L;

    protected String workerId;
    protected boolean complete = false;
    protected boolean successful = false;
    protected long executionMillis;

    private final UUID id = UUID.randomUUID();

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public long getExecutionMillis() {
        return executionMillis;
    }

    /**
     * This method does the work of the task.
     */
    public abstract void execute();

    /**
     * Logs a message to System.out with a common prefix.
     * @param message the message
     */
    public void log(String message) {
        Runtime rt = Runtime.getRuntime();
        System.out.printf("%d\t%d\t%d\t%d\t%s\t%s\n", System.currentTimeMillis(), rt.maxMemory(), rt.totalMemory(), rt.freeMemory(), this.getClass().getSimpleName(), message);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
       	    return true;
       	}
       	if (obj instanceof Task) {
            Task other = (Task)obj;
            return (this.id.equals(other.id));
       	}
       	return false;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%b\t%b\t%d", this.getClass().getSimpleName(), workerId, complete, successful, executionMillis);
    }

}
