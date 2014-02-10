
package edu.stanford.pcl.news.parser;

import edu.stanford.pcl.news.model.entity.Article;

public abstract class Parser {

    public abstract Article parse(String filePath, String content) throws ParseException;

}
