/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.xml.dom;

import groovy.xml.QName;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.w3c.dom.*;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.util.*;

/**
 * @author sam
 * @author paulk
 */
public class DOMCategory {

    private static boolean trimWhitespace = true;

    public static Object get(Element element, String elementName) {
        return getAt(element, elementName);
    }

    public static Object get(NodeList nodeList, String elementName) {
        return getAt(nodeList, elementName);
    }

    public static Object get(NamedNodeMap nodeMap, String elementName) {
        return getAt(nodeMap, elementName);
    }

    private static Object getAt(Element element, String elementName) {
        if ("..".equals(elementName)) {
            return parent(element);
        }
        if ("**".equals(elementName)) {
            return depthFirst(element);
        }
        if (elementName.startsWith("@")) {
            return element.getAttribute(elementName.substring(1));
        }
        return getChildElements(element, elementName);
    }

    private static Object getAt(NodeList nodeList, String elementName) {
        List results = new ArrayList();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                addResult(results, get((Element)node, elementName));
            }
        }
        if (elementName.startsWith("@")) {
            return results;
        }
        return new NodeListsHolder(results);
    }

    public static NamedNodeMap attributes(Element element) {
        return element.getAttributes();
    }

    private static String getAt(NamedNodeMap namedNodeMap, String elementName) {
        Attr a = (Attr) namedNodeMap.getNamedItem(elementName);
        return a.getValue();
    }

    public static int size(NamedNodeMap namedNodeMap) {
        return namedNodeMap.getLength();
    }

    public static Node getAt(Node o, int i) {
        return nodeGetAt(o, i);
    }

    public static Node getAt(NodeListsHolder o, int i) {
        return nodeGetAt(o, i);
    }

    public static Node getAt(NodesHolder o, int i) {
        return nodeGetAt(o, i);
    }

    private static Node nodeGetAt(Object o, int i) {
        if (o instanceof Element) {
            Node n = getAt((Element)o, i);
            if (n != null) return n;
        }
        if (o instanceof NodeList) {
            return getAt((NodeList)o, i);
        }
        return null;
    }

    private static Node getAt(Element element, int i) {
        if (hasChildElements(element, "*")) {
            NodeList nodeList = getChildElements(element, "*");
            return nodeList.item(i);
        }
        return null;
    }

    private static Node getAt(NodeList nodeList, int i) {
        if (i >= 0 && i < nodeList.getLength()) {
            return nodeList.item(i);
        }
        return null;
    }

    public static String name(Element element) {
        return element.getNodeName();
    }

    public static Node parent(Node node) {
        return node.getParentNode();
    }

    public static String text(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            return node.getNodeValue();
        }
        if (node.hasChildNodes()) {
            return text(node.getChildNodes());
        }
        return "";
    }

    public static String text(NodeList nodeList) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nodeList.getLength(); i++) {
            sb.append(text(nodeList.item(i)));
        }
        return sb.toString();
    }

    public static List list(NodeList self) {
        List answer = new ArrayList();
        Iterator it = DefaultGroovyMethods.iterator(self);
        while (it.hasNext()) {
            answer.add(it.next());
        }
        return answer;
    }

    public static NodeList depthFirst(Element self) {
        List result = new ArrayList();
        result.add(createNodeList(self));
        result.add(self.getElementsByTagName("*"));
        return new NodeListsHolder(result);
    }

    public static void setValue(Element self, String value) {
        self.getFirstChild().setNodeValue(value);
    }

    public static void putAt(Element self, String property, Object value) {
        if (property.startsWith("@")) {
            String attributeName = property.substring(1);
            Document doc = self.getOwnerDocument();
            Attr newAttr = doc.createAttribute(attributeName);
            newAttr.setValue(value.toString());
            self.setAttributeNode(newAttr);
            return;
        }
        InvokerHelper.setProperty(self, property, value);
    }

    public static Element appendNode(Element self, Object name) {
        return appendNode(self, name, (String)null);
    }

    public static Element appendNode(Element self, Object name, Map attributes) {
        return appendNode(self, name, attributes, null);
    }

    public static Element appendNode(Element self, Object name, String value) {
        Document doc = self.getOwnerDocument();
        Element newChild;
        if (name instanceof QName) {
            QName qn = (QName) name;
            newChild = doc.createElementNS(qn.getNamespaceURI(), qn.getQualifiedName());
        } else {
            newChild = doc.createElement(name.toString());
        }
        if (value != null) {
            Text text = doc.createTextNode(value);
            newChild.appendChild(text);
        }
        self.appendChild(newChild);
        return newChild;
    }

    public static Element appendNode(Element self, Object name, Map attributes, String value) {
        Element result = appendNode(self, name, value);
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry e = (Map.Entry) iterator.next();
            putAt(result, "@" + e.getKey().toString(), e.getValue());
        }
        return result;
    }

    private static NodeList createNodeList(Element self) {
        List first = new ArrayList();
        first.add(self);
        return new NodesHolder(first);
    }

    public static NodeList breadthFirst(Element self) {
        List result = new ArrayList();
        NodeList thisLevel = createNodeList(self);
        while (thisLevel.getLength() > 0) {
            result.add(thisLevel);
            thisLevel = getNextLevel(thisLevel);
        }
        return new NodeListsHolder(result);
    }

    private static NodeList getNextLevel(NodeList thisLevel) {
        List result = new ArrayList();
        for (int i = 0; i < thisLevel.getLength(); i++) {
            Node n = thisLevel.item(i);
            if (n instanceof Element) {
                result.add(getChildElements((Element) n, "*"));
            }
        }
        return new NodeListsHolder(result);
    }

    public static NodeList children(Element self) {
        return getChildElements(self, "*");
    }

    private static boolean hasChildElements(Element self, String elementName) {
        return getChildElements(self, elementName).getLength() > 0;
    }

    private static NodeList getChildElements(Element self, String elementName) {
        List result = new ArrayList();
        NodeList nodeList = self.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                if ("*".equals(elementName) || child.getTagName().equals(elementName)) {
                    result.add(child);
                }
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String value = node.getNodeValue();
                if (trimWhitespace) {
                    value = value.trim();
                }
                if ("*".equals(elementName) && value.length() > 0) {
                    node.setNodeValue(value);
                    result.add(node);
                }
            }
        }
        return new NodesHolder(result);
    }

    public static String toString(Object o) {
        if (o instanceof Node) {
            if (((Node) o).getNodeType() == Node.TEXT_NODE) {
                return ((Node) o).getNodeValue();
            }
        }
        if (o instanceof NodeList) {
            return toString((NodeList) o);
        }
        return o.toString();
    }

    public static Object xpath(Node self, String expression, javax.xml.namespace.QName returnType) {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            return xpath.evaluate(expression, self, returnType);
        } catch (XPathExpressionException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    public static String xpath(Node self, String expression) {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            return xpath.evaluate(expression, self);
        } catch (XPathExpressionException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    private static String toString(NodeList self) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        Iterator it = DefaultGroovyMethods.iterator(self);
        while (it.hasNext()) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(it.next().toString());
        }
        sb.append("]");
        return sb.toString();
    }

    public static int size(NodeList self) {
        return self.getLength();
    }

    public static boolean isEmpty(NodeList self) {
        return size(self) == 0;
    }

    private static void addResult(List results, Object result) {
        if (result != null) {
            if (result instanceof Collection) {
                results.addAll((Collection) result);
            } else {
                results.add(result);
            }
        }
    }

    private static final class NodeListsHolder implements NodeList {
        private List nodeLists;

        private NodeListsHolder(List nodeLists) {
            this.nodeLists = nodeLists;
        }

        public int getLength() {
            int length = 0;
            for (int i = 0; i < nodeLists.size(); i++) {
                NodeList nl = (NodeList) nodeLists.get(i);
                length += nl.getLength();
            }
            return length;
        }

        public Node item(int index) {
            int relativeIndex = index;
            for (int i = 0; i < nodeLists.size(); i++) {
                NodeList nl = (NodeList) nodeLists.get(i);
                if (relativeIndex < nl.getLength()) {
                    return nl.item(relativeIndex);
                }
                relativeIndex -= nl.getLength();
            }
            return null;
        }

        public String toString() {
            return DOMCategory.toString(this);
        }
    }

    private static final class NodesHolder implements NodeList {
        private List nodes;

        private NodesHolder(List nodes) {
            this.nodes = nodes;
        }

        public int getLength() {
            return nodes.size();
        }

        public Node item(int index) {
            if (index < 0 || index >= getLength()) {
                return null;
            }
            return (Node) nodes.get(index);
        }
    }
}