package edu.stanford.pcl.newspaper;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Rebecca
 * Date: 6/18/12
 * Time: 11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class NytParser extends Parser {

    private static Map<String, String> attributeMap = new HashMap<String, String>();
    static {
        attributeMap.put("articlePageNumber", "//head/meta[@name=\"print_page_number\"]");
        attributeMap.put("_articlePublicationMonth", "//head/meta[@name=\"publication_month\"]");
        attributeMap.put("_articlePublicationDayOfMonth", "//head/meta[@name=\"publication_day_of_month\"]");
        attributeMap.put("_articlePublicationYear", "//head/meta[@name=\"publication_year\"]");
//        attributeMap.put("articleHeadline", articleHeadline);
//        attributeMap.put("articleText", "//body/body.content/block[@class=\"full_text\"]/p/text()");
//        attributeMap.put("articleFileName", articleFileName);
//        attributeMap.put("articleContentType", articleContentType);
//        attributeMap.put("articleContentSource", articleContentSource);
        // TODO:  Fill out other attributes.
    }

    // TODO:  Handle missing fields robustly.
    public Article parse(File file) {
        Article article = new Article();
        article.setMediaType("newspaper");
        article.setContentSource("New York Times");
        article.setFileName(file.getName());

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        domFactory.setValidating(false);

        try {
            domFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            domFactory.setFeature("http://xml.org/sax/features/validation", false);
            domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(file);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr;
            NodeList result;

            Map<String, String> attributes = new HashMap<String, String>();

            // Attributes
            for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
                expr = xpath.compile(entry.getValue());
                result = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
                attributes.put(entry.getKey(), result.item(0).getAttributes().getNamedItem("content").getNodeValue());
            }

            // Page Number
            article.setPageNumber(attributes.get("articlePageNumber"));

            // Publication Date
            // TODO: Use a real date object.
            article.setPublicationDate(attributes.get("_articlePublicationMonth") + "/" + attributes.get("_articlePublicationDayOfMonth") + "/" + attributes.get("_articlePublicationYear"));

            // Headline
            expr = xpath.compile("//body/body.head/hedline/hl1");
            result = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
            article.setHeadline(result.item(0).getTextContent());

            // Text
            StringBuilder sb = new StringBuilder();
            expr = xpath.compile("//body/body.content/block[@class=\"full_text\"]/p/text()");
            result = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < result.getLength(); i++) {
                sb.append(result.item(i).getTextContent()).append(" ");
            }
            article.setText(sb.toString());

        }
        // TODO:  Exception handling.
        catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (SAXException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return article;
    }

}
