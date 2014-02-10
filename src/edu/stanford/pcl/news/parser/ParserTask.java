
package edu.stanford.pcl.news.parser;

import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.task.Task;


public class ParserTask extends Task {
    private static final long serialVersionUID = -8416082213031535994L;


    private String filePath;
    private String content;
    private Article article;


    public ParserTask(String filePath, String content) {
        this.filePath = filePath;
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public Article getArticle() {
        return article;
    }

    @Override
    public void execute() {
        try {

            // XXX  This is weak.  Should probably use some sort of signature.
            ParserFactory.Outlet outlet = null;
            if (this.filePath.contains("new-york-times")) {
                outlet = ParserFactory.Outlet.NEW_YORK_TIMES;
            }
            else if (this.filePath.contains("los-angeles-times")) {
                outlet = ParserFactory.Outlet.LOS_ANGELES_TIMES;
            }
            else if (this.filePath.contains("chicago-tribune")) {
                outlet = ParserFactory.Outlet.CHICAGO_TRIBUNE;
            }
            else if (this.filePath.contains("baltimore-sun")) {
                outlet = ParserFactory.Outlet.BALTIMORE_SUN;
            }

            try {
                Parser parser = ParserFactory.getParser(outlet);
                this.article = parser.parse(this.filePath, this.content);
                if (this.article != null) {
                    this.successful = true;
                }
            }
            catch (ParseException e) {
                // XXX  Do something...?
                e.printStackTrace();
            }
        }
        finally {
        }
    }

    @Override
    public String toString() {
        return String.format("%s\t%s", super.toString(), filePath);
    }
}
