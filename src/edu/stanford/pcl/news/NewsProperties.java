
package edu.stanford.pcl.news;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class NewsProperties {
    private static Properties defaults = new Properties();
    static {
        defaults.setProperty("model.db.host", "localhost");
        defaults.setProperty("data.root.path", "/news/data");
        defaults.setProperty("task.queue.size", "4");           // XXX  Probably remove.
        defaults.setProperty("task.retry.seconds", "300");
        defaults.setProperty("corenlp.parse.maxlen", "150");
    }

    private static NewsProperties singleton = new NewsProperties();

    public static String getProperty(String key) {
        return singleton.properties.getProperty(key);
    }


    private Properties properties;

    private NewsProperties() {
        properties = new Properties(defaults);

        boolean found = load("/news/news.properties");
        found |= load("news.properties");

        if (!found) {
            System.err.println("Could not find news.properties file in either /news or the local directory.  Using defaults.");
        }
    }


    private boolean load(String path) {
        try {
            properties.load(new FileInputStream(path));
            return true;
        }
        catch (FileNotFoundException e) { /* Okay. */ }
        catch (IOException e) {
            System.err.printf("Could not read properties file (%s).\n", path);
        }
        return false;
    }

}
