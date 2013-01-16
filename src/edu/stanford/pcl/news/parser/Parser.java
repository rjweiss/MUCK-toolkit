
package edu.stanford.pcl.news.parser;

import edu.stanford.pcl.news.model.entity.Article;

public abstract class Parser {

    public abstract Article parse(String path) throws ParseException;

}
