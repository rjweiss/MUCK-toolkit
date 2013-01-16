
package edu.stanford.pcl.news.model.entity;

import java.io.Serializable;

public class Token implements Serializable {
    private static final long serialVersionUID = -6864113291099757485L;

    public String word;
    public String lemma;
    public String pos;
    public String ne;
}
