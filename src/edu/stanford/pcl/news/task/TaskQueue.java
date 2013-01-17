
package edu.stanford.pcl.news.task;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;

// XXX  Shouldn't wrap the blocking queue like this.  Need to really handle the interruptions.
public class TaskQueue implements Serializable {
    private static final long serialVersionUID = 4399872932613957889L;

    ArrayBlockingQueue<Task> queue;

    public TaskQueue() {
        //  XXX  Refine capacity (this caps the amount of workers in the pool).
        this.queue = new ArrayBlockingQueue<Task>(3);
    }

    public Task take() {
        Task task = null;
        try {
            task = queue.take();
            return task;
        }
        catch (InterruptedException e) {
            // XXX
            e.printStackTrace();
        }
        // XXX  If something goes wrong, need to re-enqueue this file.
        return null;
    }

    public Task peek() {
        return queue.peek();
    }

    public Task poll() {
        return queue.poll();
    }

    public void put(Task task) {
        try {
            queue.put(task);
        }
        catch (InterruptedException e) {
            // XXX
            e.printStackTrace();
        }
    }

}
