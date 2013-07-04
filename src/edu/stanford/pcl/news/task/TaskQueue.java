
package edu.stanford.pcl.news.task;

import edu.stanford.pcl.news.NewsProperties;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.*;

// XXX  Shouldn't wrap the blocking queue like this.  Need to really handle the interruptions.
public class TaskQueue implements Serializable {
    private static final long serialVersionUID = 4399872932613957889L;

    private static class DelayedTask implements Delayed {
        private static final long DELAY_MILLISECONDS = Long.parseLong(NewsProperties.getProperty("task.retry.seconds")) * 1000;

        Task task;
        long createMillis;

        public DelayedTask(Task task) {
            this.task = task;
            this.createMillis = System.currentTimeMillis();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(DELAY_MILLISECONDS - (System.currentTimeMillis() - createMillis), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed delayed) {
            return Long.valueOf(this.createMillis).compareTo(((DelayedTask) delayed).createMillis);
        }
    }


    ArrayBlockingQueue<Task> primaryQueue;
    ConcurrentLinkedQueue<Task> continuationQueue;
    DelayQueue<DelayedTask> retryQueue;

    public TaskQueue() {
        //  XXX  Refine capacity (this caps the amount of workers in the pool (ha, no it doesn't)).
        this.primaryQueue = new ArrayBlockingQueue<Task>(Integer.parseInt(NewsProperties.getProperty("task.queue.size")));
        this.continuationQueue = new ConcurrentLinkedQueue<Task>();
        this.retryQueue = new DelayQueue<DelayedTask>();
    }

    public Task take() {
        Task task = null;
        try {
            DelayedTask delayedTask = null;
            if ((delayedTask = retryQueue.poll()) != null) {
                task = delayedTask.task;
                // XXX  Should have some sort of TTL so we don't keep retrying tasks that crash.
            }
            if (task == null) {
                task = continuationQueue.poll();
            }
            if (task == null) {
                task = primaryQueue.take();
            }
            if (task.getTryCount() < 3) {
                // XXX
                task.setTryCount(task.getTryCount() + 1);
                retryQueue.put(new DelayedTask(task));
            }
            else {
                System.out.printf("%d\t%d\t%s\t%s\n", System.currentTimeMillis(), Thread.currentThread().getId(), "rejected", task);
            }
            return task;
        }
        catch (InterruptedException e) {
            // XXX
            e.printStackTrace();
        }
        return null;
    }

    public boolean resolve(Task task) {
        // XXX  This is gross.
        for (Iterator<DelayedTask> iterator = retryQueue.iterator(); iterator.hasNext(); ) {
            DelayedTask delayedTask = iterator.next();
            if (task.equals(delayedTask.task)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public void putPrimaryTask(Task task) {
        try {
            primaryQueue.put(task);
        }
        catch (InterruptedException e) {
            // XXX
            e.printStackTrace();
        }
    }

    public void putContinuationTask(Task task) {
        continuationQueue.add(task);
    }

    public String toString() {
        return String.format("%d\t%d\t%d", primaryQueue.size(), continuationQueue.size(), retryQueue.size());
    }
}
