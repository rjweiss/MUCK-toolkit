
package edu.stanford.pcl.news.model.entity;

import edu.stanford.pcl.news.model.Serialization;
import org.bson.types.ObjectId;

import java.io.Serializable;

public class Article implements Serializable {
    private static final long serialVersionUID = -6060628666990358845L;

    private ObjectId _id;

    public String file;
    public String outlet;
    public String date; // XXX  String?
    public String page;
    public String headline;
    public String body;

    public CoreNlp corenlp;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !this.getClass().equals(obj.getClass())) return false;

        // XXX  This little shortcut may bite.
        Object o1 = Serialization.toJavaObject(this.toString(), this.getClass());
        Object o2 = Serialization.toJavaObject(obj.toString(), this.getClass());
        return (o1.equals(o2));
    }
}
