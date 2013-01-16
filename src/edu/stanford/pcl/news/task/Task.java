
package edu.stanford.pcl.news.task;

import java.io.Serializable;


/**
 * Provides a base class for representing distributed tasks.  A <code>Task</code> object should fully describe
 * a distributable unit of work and, eventually, the result of the completed work.
 */
public abstract class Task implements Serializable {
    private static final long serialVersionUID = -5727217829452869247L;
    protected boolean complete = false;
    protected long executionMillis;

    private String id = "Task:0";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isComplete() {
        return complete;
    }

    public long getExecutionMillis() {
        return executionMillis;
    }

    /**
     * This method does the work of the task.
     */
    public abstract void execute();

}
