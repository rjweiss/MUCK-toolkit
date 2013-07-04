
package edu.stanford.pcl.news.task;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.Callable;


/**
 * Provides a base class for representing distributed tasks.  A <code>Task</code> object should fully describe
 * a distributable unit of work and, eventually, the result of the completed work.
 */
public abstract class Task implements Callable<Void>, Serializable {
    private static final long serialVersionUID = -5727217829452869247L;

    protected String workerId = "<unassigned>";
    protected boolean successful = false;
    protected boolean timedOut = false;
    private long executionMillis;
    protected int retryCount = 0;

    private final UUID id = UUID.randomUUID();

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public long getExecutionMillis() {
        return executionMillis;
    }

    public int getTryCount() {
        return retryCount;
    }

    public void setTryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void initialize() {
    }

    @Override
    public Void call() throws Exception {
        long stop, start = System.currentTimeMillis();
        log(String.format("%s\t%s", "start", this));
        try {
            execute();
        }
        catch (RuntimeException e) {
            // XXX
            System.err.println("Caught RuntimeException: " + e.getMessage());
        }
        finally {
            stop = System.currentTimeMillis();
            this.executionMillis = stop - start;
            log(String.format("%s\t%s", "stop", this));
        }
        return null;
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
        System.out.printf("%d\t%d\t%d\t%s\n", System.currentTimeMillis(), rt.totalMemory(), rt.freeMemory(), message);
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
        return String.format("%s\t%s\t%b\t%b\t%d\t%d", this.getClass().getSimpleName(), workerId, successful, timedOut, retryCount, executionMillis);
    }

}
