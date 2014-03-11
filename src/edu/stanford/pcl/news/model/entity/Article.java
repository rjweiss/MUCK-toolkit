
package edu.stanford.pcl.news.model.entity;

import edu.stanford.pcl.news.model.Serialization;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Article implements Serializable {
    private static final long serialVersionUID = 5139318906731251221L;

    public ObjectId _id;

    public String file;
    public String outlet;
    public Date date;
    public String page;
    public String headline;
    public String body;
    public Set<Descriptor> descriptors;
    public CoreNlp corenlp;
    public String indextext;
    public HashSet<String> persons;
    public HashSet<String> organizations;
    public HashSet<String> locations;
    public HashSet<String> subjects;
    public HashSet<String> objects;

    // For working with mediacloud data

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
