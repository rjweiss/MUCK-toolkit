
package edu.stanford.pcl.news.model.entity;

import java.io.Serializable;

public class Descriptor implements Serializable {
    private static final long serialVersionUID = 8030378376892385941L;

    public String type;
    public String value;
    public Descriptor(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
