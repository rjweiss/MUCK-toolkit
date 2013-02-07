
package edu.stanford.pcl.news.parser;

import edu.stanford.pcl.news.model.entity.Article;
import edu.stanford.pcl.news.model.entity.Descriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * An <code>XmlParser</code> object is not thread-safe.
 */
class XmlParser extends Parser {
    private DocumentBuilder documentBuilder;
    private XPath xpath;

    private Map<String, String> featureExpressionMap;
    private List<String> descriptorExpressions;
    private Map<String, String> excludeConditionsMap;
    private String outlet;

    private XmlParser() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);

        try {
            documentBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        xpath = XPathFactory.newInstance().newXPath();
    }

    XmlParser(String outlet, Map<String, String> featureExpressionMap) {
        this(outlet, featureExpressionMap, null, null);
    }

    XmlParser(String outlet, Map<String, String> featureExpressionMap, Map<String, String> excludeConditionsMap) {
        this(outlet, featureExpressionMap, excludeConditionsMap, null);
    }

    XmlParser(String outlet, Map<String, String> featureExpressionMap, Map<String, String> excludeConditionsMap, List<String> descriptorExpressions) {
        this();
        this.outlet = outlet;
        this.featureExpressionMap = featureExpressionMap;
        this.excludeConditionsMap = excludeConditionsMap;
        this.descriptorExpressions = descriptorExpressions;
    }


    @Override
    public Article parse(String filePath, String xml) throws ParseException {
        Article article = new Article();

        try {
            InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document document = documentBuilder.parse(in);

            if (excludeConditionsMap != null) {
                for (Map.Entry<String, String> exclude : excludeConditionsMap.entrySet()) {
                    XPathExpression xPathExpression = xpath.compile(exclude.getKey());
                    String value = xPathExpression.evaluate(document);
                    if (value != null && value.equals(exclude.getValue())) {
                        return null;
                    }
                }
            }

            article.outlet = outlet;
            article.file = filePath;  // XXX  Relative or absolute?

            for (Field field : article.getClass().getFields()) {
                String fieldName = field.getName();
                String expression = featureExpressionMap.get(fieldName);
                if (expression != null) {
                    XPathExpression xPathExpression = xpath.compile(expression);
                    NodeList nodes = (NodeList)xPathExpression.evaluate(document, XPathConstants.NODESET);
                    if (nodes == null) {
                        System.err.println(filePath + ": " + fieldName + " missing");
                    }
                    else {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < nodes.getLength(); i++) {
                            builder.append(nodes.item(i).getTextContent());
                        }
                        // XXX  Only works for strings!
                        field.set(article, builder.toString());
                    }
                }
            }

            if (descriptorExpressions != null) {
                for (String expression : descriptorExpressions) {
                    XPathExpression xPathExpression = xpath.compile(expression);
                    NodeList nodes = (NodeList)xPathExpression.evaluate(document, XPathConstants.NODESET);
                    if (nodes != null) {
                        article.descriptors = new HashSet<Descriptor>();
                        for (int i=0; i < nodes.getLength(); i++) {
                            Node node = nodes.item(i);
                            // XXX  This is not properly decoupled from the New York Times.
                            article.descriptors.add(new Descriptor(node.getAttributes().getNamedItem("type").getTextContent(), node.getTextContent()));
                        }
                    }
                }
            }
        }
        catch (Throwable t) {
            throw new ParseException(t);
        }

        return article;
    }

}
