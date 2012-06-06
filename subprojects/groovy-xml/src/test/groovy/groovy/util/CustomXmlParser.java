package groovy.util;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

class CustomXmlParser extends XmlParser {
    CustomXmlParser() throws ParserConfigurationException, SAXException {
        super();
    }

    protected Object getElementName(String s, String s1, String s2) {
        return new Integer(42);
    }

    protected Node createNode(Node parent, Object name, Map attributes) {
        return new CustomNode(parent, name, attributes, new NodeList());
    }
}