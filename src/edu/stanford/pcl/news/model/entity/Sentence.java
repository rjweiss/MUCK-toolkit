
package edu.stanford.pcl.news.model.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

public class Sentence implements Serializable {
    private static final long serialVersionUID = -2974528517793786567L;

    public List<Token> tokens;
    public List<Dependency> dependencies;
    public List<Sentiment> sentiments;

    public HashSet<String> persons;
    public HashSet<String> organizations;
    public HashSet<String> locations;
    public HashSet<String> subjects;
    public HashSet<String> objects;
    public String sentiment;
    public String indextext;



}
