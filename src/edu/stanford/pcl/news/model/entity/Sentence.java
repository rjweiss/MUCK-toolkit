
package edu.stanford.pcl.news.model.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class Sentence implements Serializable {
    private static final long serialVersionUID = -1462494338131348394L;
    
    public List<Token> tokens;
    public List<Dependency> dependencies;
    public List<Sentiment> sentiments;

    // Elasticsearch
    public Set<String> persons;
    public Set<String> organizations;
    public Set<String> locations;
    public Set<String> subjects;
    public Set<String> objects;
    public String sentiment;
    public String indextext;
    public String text;
}
