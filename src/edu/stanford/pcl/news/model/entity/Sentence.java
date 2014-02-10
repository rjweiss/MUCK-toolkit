
package edu.stanford.pcl.news.model.entity;

import java.io.Serializable;
import java.util.List;

public class Sentence implements Serializable {
    private static final long serialVersionUID = -2974528517793786567L;

    public List<Token> tokens;
    public List<String> dependencies;
    public List<Sentiment> sentiments;
}
