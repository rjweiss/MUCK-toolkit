package edu.stanford.pcl.news.task;


public interface TaskResolver<T extends Task> {
    public void resolve(T task);
}
