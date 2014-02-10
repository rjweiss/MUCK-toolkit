package edu.stanford.pcl.news.parser;


import edu.stanford.pcl.news.model.entity.Article;

public class PlainTextParser extends Parser {

    @Override
    public Article parse(String filePath, String content) throws ParseException {
        Article article = new Article();

        article.file = filePath;
        article.body = content;

        return article;
    }

}
