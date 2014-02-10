
package edu.stanford.pcl.news.model.entity;


import java.io.Serializable;

public class Sentiment implements Serializable {
    private static final long serialVersionUID = -5979857818980125264L;

    public String model;
    public String prediction;
    public Predictions predictions;
}
