package edu.stanford.pcl.news.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import edu.stanford.pcl.news.model.Serialization;
import org.bson.types.ObjectId;

public class Article implements Serializable {
    private static final long serialVersionUID = -8317131662743944911L;

    public ObjectId _id;

    // For working with Gistraker
    public String url;
    public String download_date;
    public String published_date;
    public String title;
    public String source_url;
    public String processed;

    public String file;
    public String outlet;
    public Date date;
    public String page;
    public String headline;
    public String body;
    public Set<Descriptor> descriptors;
    public CoreNlp corenlp;

    // Elasticsearch
    public String indextext;
    public Set<String> persons;
    public Set<String> organizations;
    public Set<String> locations;
    public Set<String> subjects;
    public Set<String> objects;

    // For working with MediaCloud data
    public Object publish_date;
    public Integer media_id;
    public String id;
    public String sentence;


    @Override
    public boolean equals(Object obj) {
        if (obj == null || !this.getClass().equals(obj.getClass())) return false;

        // XXX  This little shortcut may bite.
        Object o1 = Serialization.toJavaObject(this.toString(), this.getClass());
        Object o2 = Serialization.toJavaObject(obj.toString(), this.getClass());
        return (o1.equals(o2));
    }
}
