
package edu.stanford.pcl.news.parser;

import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.task.Task;


public class ParserTask extends Task {
    private static final long serialVersionUID = -7395120686484734721L;


    private String filePath;
    private String xml;
    private Article article;


    public ParserTask(String filePath, String xml) {
        this.filePath = filePath;
        this.xml = xml;
    }

    public Article getArticle() {
        return article;
    }

    @Override
    public void execute() {
        System.out.printf("%d\tParserTask\t%s\n", System.currentTimeMillis(), filePath);
        long start = System.currentTimeMillis();
        try {

            ParserFactory.Outlet outlet = null;
            if (this.filePath.contains("nytimes")) {
                outlet = ParserFactory.Outlet.NEW_YORK_TIMES;
            }
            else if (this.filePath.contains("latimes")) {
                outlet = ParserFactory.Outlet.LOS_ANGELES_TIMES;
            }
            else if (this.filePath.contains("chitrib")) {
                outlet = ParserFactory.Outlet.CHICAGO_TRIBUNE;
            }

            if (outlet != null) {
                try {
                    Parser parser = ParserFactory.getParser(outlet);
                    this.article = parser.parse(this.filePath);
//                    String string = Serialization.toJson(article);
                }
                catch (ParseException e) {
                    // XXX  Do something...?
                    e.printStackTrace();
                }
            }
        }
        finally {
            long stop = System.currentTimeMillis();
            this.executionMillis = stop - start;
            this.complete = true;
        }
    }

}
