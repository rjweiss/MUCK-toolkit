
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
    }

    private static NewsProperties singleton = new NewsProperties();

    public static String getProperty(String key) {
        return singleton.properties.getProperty(key);
    }


    private Properties properties;

    private NewsProperties() {
        properties = new Properties(defaults);
        try {
            properties.load(new FileInputStream("news.properties"));
        }
        catch (FileNotFoundException e) {
            System.err.println("Could not find properties file (news.properties).  Using defaults.");
        }
        catch (IOException e) {
            System.err.println("Could not read properties file (news.properties).  Using defaults.");
        }
    }
}
