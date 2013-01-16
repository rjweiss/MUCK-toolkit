
package edu.stanford.pcl.news.model.entity;

import java.io.Serializable;
import java.util.List;

public class Sentence implements Serializable {
    private static final long serialVersionUID = 851901945464225311L;

    public List<Token> tokens;
    public List<String> dependencies;
}
