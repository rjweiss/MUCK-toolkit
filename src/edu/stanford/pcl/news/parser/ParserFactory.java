
package edu.stanford.pcl.news.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserFactory {

    public static enum Outlet {
        NEW_YORK_TIMES("New York Times"),
        LOS_ANGELES_TIMES("Los Angeles Times"),
        CHICAGO_TRIBUNE("Chicago Tribune"),
        BALTIMORE_SUN("Baltimore Sun");

        public String name;
        Outlet(String name) {
            this.name = name;
        }
    }


    public static Parser getParser(Outlet outlet) {
        if (outlet == Outlet.NEW_YORK_TIMES) {
            Map<String, String> featureExpressionMap = new HashMap<String, String>();
            featureExpressionMap.put("date", "//head/pubdata/@date.publication");
            featureExpressionMap.put("page", "//head/meta[@name=\"print_page_number\"]/@content");
            featureExpressionMap.put("headline", "//body/body.head/hedline/hl1");
            featureExpressionMap.put("body", "//body/body.content/block[@class=\"full_text\"]");
            List<String> descriptorExpressions = new ArrayList<String>();
            descriptorExpressions.add("//head/docdata/identified-content/classifier");
            Map<String, String> excludeConditionsMap = new HashMap<String, String>();
            excludeConditionsMap.put("//head/docdata/identified-content/classifier[@type=\"descriptor\"]", "Deaths (Obituaries)");
            return new XmlParser(outlet.name, featureExpressionMap, excludeConditionsMap, descriptorExpressions, "yyyyMMdd'T000000'");
        }
        else if (outlet == Outlet.LOS_ANGELES_TIMES || outlet == Outlet.CHICAGO_TRIBUNE || outlet == Outlet.BALTIMORE_SUN) {
            Map<String, String> featureExpressionMap = new HashMap<String, String>();
            featureExpressionMap.put("date", "//pcdt/pcdtn");
            featureExpressionMap.put("page", "//docdt/startpg");
            featureExpressionMap.put("headline", "//docdt/doctitle");
            featureExpressionMap.put("body", "//txtdt/text/paragraph");
            Map<String, String> excludeConditionsMap = new HashMap<String, String>();
            excludeConditionsMap.put("//docsec", "OBITUARIES");
            return new XmlParser(outlet.name, featureExpressionMap, excludeConditionsMap, "yyyyMMdd");
        }
        else {
            // XXX  Not sure this is a good default.
            return new PlainTextParser();
        }
    }


    private ParserFactory() {
    }

}
