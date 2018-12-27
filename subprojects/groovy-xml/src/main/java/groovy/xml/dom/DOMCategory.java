/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.xml.dom;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.IntRange;
import groovy.xml.DOMBuilder;
import groovy.xml.QName;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.XmlGroovyMethods;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Category class which adds GPath style operations to Java's DOM classes.
 */
public class DOMCategory {
    private static boolean trimWhitespace = false;
    private static boolean keepIgnorableWhitespace = false;

    /**
     * @return true if text elements are trimmed before returning; default false
     */
    public static synchronized boolean isGlobalTrimWhitespace() {
        return trimWhitespace;
    }

    /**
     * Whether text content is trimmed (removing leading and trailing whitespace); default false.
     * WARNING: this is a global setting. Altering it will affect all DOMCategory usage within the current Java process.
     * It is not recommended that this is altered; instead call the trim() method on the returned text, but the
     * flag is available to support legacy Groovy behavior.
     *
     * @param trimWhitespace the new value
     */
    public static synchronized void setGlobalTrimWhitespace(boolean trimWhitespace) {
        DOMCategory.trimWhitespace = trimWhitespace;
    }

    /**
     * @return true if ignorable whitespace (e.g. whitespace between elements) is kept; default false
     */
    public static synchronized boolean isGlobalKeepIgnorableWhitespace() {
        return keepIgnorableWhitespace;
    }

    /**
     * Whether ignorable whitespace (e.g. whitespace between elements) is kept (default false).
     * WARNING: this is a global setting. Altering it will affect all DOMCategory usage within the current Java process.
     *
     * @param keepIgnorableWhitespace the new value
     */
    public static synchronized void setGlobalKeepIgnorableWhitespace(boolean keepIgnorableWhitespace) {
        DOMCategory.keepIgnorableWhitespace = keepIgnorableWhitespace;
    }

    public static Object get(Element element, String elementName) {
        return xgetAt(element, elementName);
    }

    public static Object get(NodeList nodeList, String elementName) {
        if (nodeList instanceof Element) {
            // things like com.sun.org.apache.xerces.internal.dom.DeferredElementNSImpl
            // do implement Element, NodeList and Node. But here we prefer element,
            // so we force the usage of Element. Without this DOMCategoryTest may fail
            // in strange ways
            return xgetAt((Element)nodeList, elementName);
        } else {
            return xgetAt(nodeList, elementName);
        }
    }

    public static Object get(NamedNodeMap nodeMap, String elementName) {
        return xgetAt(nodeMap, elementName);
    }

    private static Object xgetAt(Element element, String elementName) {
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

    private static Object xgetAt(NodeList nodeList, String elementName) {
        List<NodeList> results = new ArrayList<NodeList>();
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

    private static String xgetAt(NamedNodeMap namedNodeMap, String elementName) {
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

    public static NodeList getAt(Node o, IntRange r) {
        return nodesGetAt(o, r);
    }

    public static NodeList getAt(NodeListsHolder o, IntRange r) {
        return nodesGetAt(o, r);
    }

    public static NodeList getAt(NodesHolder o, IntRange r) {
        return nodesGetAt(o, r);
    }

    private static Node nodeGetAt(Object o, int i) {
        if (o instanceof Element) {
            Node n = xgetAt((Element)o, i);
            if (n != null) return n;
        }
        if (o instanceof NodeList) {
            return xgetAt((NodeList)o, i);
        }
        return null;
    }

    private static NodeList nodesGetAt(Object o, IntRange r) {
        if (o instanceof Element) {
            NodeList n = xgetAt((Element)o, r);
            if (n != null) return n;
        }
        if (o instanceof NodeList) {
            return xgetAt((NodeList)o, r);
        }
        return null;
    }

    private static Node xgetAt(Element element, int i) {
        if (hasChildElements(element, "*")) {
            NodeList nodeList = getChildElements(element, "*");
            return xgetAt(nodeList, i);
        }
        return null;
    }

    private static Node xgetAt(NodeList nodeList, int i) {
        if (i < 0) {
            i += nodeList.getLength();
        }

        if (i >= 0 && i < nodeList.getLength()) {
            return nodeList.item(i);
        }
        return null;
    }

    private static NodeList xgetAt(Element element, IntRange r) {
        if (hasChildElements(element, "*")) {
            NodeList nodeList = getChildElements(element, "*");
            return xgetAt(nodeList, r);
        }
        return null;
    }

    private static NodeList xgetAt(NodeList nodeList, IntRange r) {
        int from = r.getFromInt();
        int to = r.getToInt();

        // If the range is of size 1, then we can use the existing
        // xgetAt() that takes an integer index.
        if (from == to) return new NodesHolder(Collections.singletonList(xgetAt(nodeList, from)));

        // Normalise negative indices.
        if (from < 0) from = from + nodeList.getLength();
        if (to < 0) to = to + nodeList.getLength();

        // After normalisation, 'from' may be greater than 'to'. In that
        // case, we need to reverse them and make sure the range's 'reverse'
        // property is correct.
        // TODO We should probably use DefaultGroovyMethodsSupport.subListBorders(),
        // but that's protected and unavailable to us.
        if (from > to) {
            r = r.isReverse() ? new IntRange(to, from) : new IntRange(from, to);
            from = r.getFromInt();
            to = r.getToInt();
        }

        // Copy the required nodes into a new list.
        List<Node> nodes = new ArrayList<Node>(to - from + 1);
        if (r.isReverse()) {
            for (int i = to; i >= from; i--) nodes.add(nodeList.item(i));
        }
        else {
            for (int i = from; i <= to; i++) nodes.add(nodeList.item(i));
        }
        return new NodesHolder(nodes);
    }

    public static String name(Node node) {
        return node.getNodeName();
    }

    public static Node parent(Node node) {
        return node.getParentNode();
    }

    public static String text(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
            return node.getNodeValue();
        }
        if (node.hasChildNodes()) {
            return text(node.getChildNodes());
        }
        return "";
    }

    public static String text(NodeList nodeList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeList.getLength(); i++) {
            sb.append(text(nodeList.item(i)));
        }
        return sb.toString();
    }

    public static List<Node> list(NodeList self) {
        List<Node> answer = new ArrayList<Node>();
        Iterator<Node> it = XmlGroovyMethods.iterator(self);
        while (it.hasNext()) {
            answer.add(it.next());
        }
        return answer;
    }

    public static NodeList depthFirst(Element self) {
        List<NodeList> result = new ArrayList<NodeList>();
        result.add(createNodeList(self));
        result.add(self.getElementsByTagName("*"));
        return new NodeListsHolder(result);
    }

    public static void setValue(Element self, String value) {
        Node firstChild = self.getFirstChild();
        if (firstChild == null) {
            firstChild = self.getOwnerDocument().createTextNode(value);
            self.appendChild(firstChild);
        }
        firstChild.setNodeValue(value);
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
        for (Object o : attributes.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            putAt(result, "@" + e.getKey().toString(), e.getValue());
        }
        return result;
    }

    public static Node replaceNode(NodesHolder self, Closure c) {
        if (self.getLength() <= 0 || self.getLength() > 1) {
            throw new GroovyRuntimeException(
                    "replaceNode() can only be used to replace a single element, " +
                    "but was applied to " + self.getLength() + " elements."
            );
        }
        return replaceNode(self.item(0), c);
    }

    public static Node replaceNode(Node self, Closure c) {
        if (self.getParentNode() instanceof Document) {
            throw new UnsupportedOperationException("Replacing the root node is not supported");
        }
        appendNodes(self, c);
        self.getParentNode().removeChild(self);
        return self;
    }

    public static void plus(Element self, Closure c) {
        if (self.getParentNode() instanceof Document) {
            throw new UnsupportedOperationException("Adding sibling nodes to the root node is not supported");
        }
        appendNodes(self, c);
    }

    private static void appendNodes(Node self, Closure c) {
        Node parent = self.getParentNode();
        Node beforeNode = self.getNextSibling();
        DOMBuilder b = new DOMBuilder(self.getOwnerDocument());
        Element newNodes = (Element) b.invokeMethod("rootNode", c);
        Iterator<Node> iter = XmlGroovyMethods.iterator(children(newNodes));
        while (iter.hasNext()) {
            parent.insertBefore(iter.next(), beforeNode);
        }
    }

    /**
     * Returns the list of any direct String nodes of this node.
     *
     * @return the list of String values from this node
     * @since 2.3.0
     */
    public static List<String> localText(Element self) {
        List<String> result = new ArrayList<String>();
        if (self.getNodeType() == Node.TEXT_NODE || self.getNodeType() == Node.CDATA_SECTION_NODE) {
            result.add(self.getNodeValue());
        } else if (self.hasChildNodes()) {
            NodeList nodeList = self.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                if (item.getNodeType() == Node.TEXT_NODE || item.getNodeType() == Node.CDATA_SECTION_NODE) {
                    result.add(item.getNodeValue());
                }
            }
        }
        return result;
    }

    public static void plus(NodeList self, Closure c) {
        for (int i = 0; i < self.getLength(); i++) {
            plus((Element) self.item(i), c);
        }
    }

    private static NodeList createNodeList(Element self) {
        List<Node> first = new ArrayList<Node>();
        first.add(self);
        return new NodesHolder(first);
    }

    public static NodeList breadthFirst(Element self) {
        List<NodeList> result = new ArrayList<NodeList>();
        NodeList thisLevel = createNodeList(self);
        while (thisLevel.getLength() > 0) {
            result.add(thisLevel);
            thisLevel = getNextLevel(thisLevel);
        }
        return new NodeListsHolder(result);
    }

    private static NodeList getNextLevel(NodeList thisLevel) {
        List<NodeList> result = new ArrayList<NodeList>();
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
        List<Node> result = new ArrayList<Node>();
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
                if ((!isGlobalKeepIgnorableWhitespace() && value.trim().length() == 0) || isGlobalTrimWhitespace()) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator it = XmlGroovyMethods.iterator(self);
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

    @SuppressWarnings("unchecked")
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
        private final List<NodeList> nodeLists;

        private NodeListsHolder(List<NodeList> nodeLists) {
            this.nodeLists = nodeLists;
        }

        public int getLength() {
            int length = 0;
            for (NodeList nl : nodeLists) {
                length += nl.getLength();
            }
            return length;
        }

        public Node item(int index) {
            int relativeIndex = index;
            for (NodeList nl : nodeLists) {
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
        private final List<Node> nodes;

        private NodesHolder(List<Node> nodes) {
            this.nodes = nodes;
        }

        public int getLength() {
            return nodes.size();
        }

        public Node item(int index) {
            if (index < 0 || index >= getLength()) {
                return null;
            }
            return nodes.get(index);
        }
    }
}
