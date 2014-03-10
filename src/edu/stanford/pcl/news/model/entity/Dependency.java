
package edu.stanford.pcl.news.model.entity;

import java.io.Serializable;

public class Dependency implements Serializable {
    private static final long serialVersionUID = -5206024730083511778L;

    public String rel;   // relationship type
    public Relation gov; // relationship governor
    public Relation dep; // relationship dependent
}
