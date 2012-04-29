package edu.stanford.rjweiss.newspaper;

import com.mongodb.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: Rebecca
 * Date: 4/28/12
 * Time: 6:12 PM
 * To change this template use File | Settings | File Templates.
 */

public class NewspaperProject {
    public static void main(String[] args) throws Exception {
        Mongo m = new Mongo();
        DB db = m.getDB("test");
        DBCollection articles = db.getCollection("articles");
        DBObject myDoc = articles.findOne();
        DBCursor cur;
        BasicDBObject query = new BasicDBObject();

//      some tutorial exploration
//        int n = 0;

//        while (cur.hasNext() && n++ < 100) {
//            System.out.println(cur.next());
//        }
//        System.out.println(myDoc);
//        System.out.println(articles.getCount());


//        retrieve a single document
//        query.put("file", "66276940.xml"); //returns a single document, the string is the name of the .xml file
//        cur = articles.find(query); // will retrieve the document in the articles collection that matches the .xml query

//        this section will print out the document retrieved
//        while (cur.hasNext()) {
//            DBObject o = cur.next();
//            System.out.println(o);
//        }

        Calendar cal = new GregorianCalendar();
        cal.set(2010, 0, 1); // retrieving from January 1st, 2010
        Date fromDate = cal.getTime();
        query.put("date", new BasicDBObject("$gte", fromDate));
        System.out.println(articles.count(query)); //returns 157134 total articles

    }
}
