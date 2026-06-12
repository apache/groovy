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
import groovy.namespace.QName;
import groovy.xml.DOMBuilder;
import groovy.xml.FactorySupport;
import org.apache.groovy.xml.extensions.XmlExtensions;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Category class which adds GPath style operations to Java's DOM classes.
 * These helpers provide property access, traversal, mutation, and XPath support
 * for W3C DOM nodes in a style similar to Groovy's XML slurper APIs.
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

    /**
     * Resolves a GPath-style property lookup against a DOM element.
     * Supports child element lookup, attribute access using {@code @name},
     * parent lookup using {@code ..}, and depth-first traversal using {@code **}.
     *
     * @param element the element to query
     * @param elementName the property or selector name
     * @return the matching child nodes, attribute value, parent node, or traversal view
     */
    public static Object get(Element element, String elementName) {
        return xgetAt(element, elementName);
    }

    /**
     * Resolves a GPath-style property lookup against every element in a node list.
     *
     * @param nodeList the nodes to query
     * @param elementName the property or selector name
     * @return an aggregated result for the supplied selector
     */
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

    /**
     * Returns the value of a named attribute from a DOM attribute map.
     *
     * @param nodeMap the attributes to query
     * @param elementName the attribute name
     * @return the attribute value
     */
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

    /**
     * Returns the attributes belonging to the supplied element.
     *
     * @param element the element whose attributes should be returned
     * @return the element's attribute map
     */
    public static NamedNodeMap attributes(Element element) {
        return element.getAttributes();
    }

    private static String xgetAt(NamedNodeMap namedNodeMap, String elementName) {
        Attr a = (Attr) namedNodeMap.getNamedItem(elementName);
        return a.getValue();
    }

    /**
     * Returns the number of attributes in a DOM attribute map.
     *
     * @param namedNodeMap the attributes to inspect
     * @return the number of attributes
     */
    public static int size(NamedNodeMap namedNodeMap) {
        return namedNodeMap.getLength();
    }

    /**
     * Returns the indexed child visible through DOMCategory navigation.
     * Negative indices count backward from the end of the result.
     *
     * @param o the node or element to index
     * @param i the zero-based index
     * @return the selected node, or {@code null} if the index is out of range
     */
    public static Node getAt(Node o, int i) {
        return nodeGetAt(o, i);
    }

    /**
     * Returns the indexed node from an aggregated node-list view.
     * Negative indices count backward from the end of the result.
     *
     * @param o the aggregated node-list view
     * @param i the zero-based index
     * @return the selected node, or {@code null} if the index is out of range
     */
    public static Node getAt(NodeListsHolder o, int i) {
        return nodeGetAt(o, i);
    }

    /**
     * Returns the indexed node from a simple node-list view.
     * Negative indices count backward from the end of the result.
     *
     * @param o the node-list view
     * @param i the zero-based index
     * @return the selected node, or {@code null} if the index is out of range
     */
    public static Node getAt(NodesHolder o, int i) {
        return nodeGetAt(o, i);
    }

    /**
     * Returns a range of child nodes visible through DOMCategory navigation.
     * Negative bounds count backward from the end of the result.
     *
     * @param o the node or element to slice
     * @param r the range of indices to select
     * @return a node-list view containing the selected nodes
     */
    public static NodeList getAt(Node o, IntRange r) {
        return nodesGetAt(o, r);
    }

    /**
     * Returns a range of nodes from an aggregated node-list view.
     *
     * @param o the aggregated node-list view
     * @param r the range of indices to select
     * @return a node-list view containing the selected nodes
     */
    public static NodeList getAt(NodeListsHolder o, IntRange r) {
        return nodesGetAt(o, r);
    }

    /**
     * Returns a range of nodes from a simple node-list view.
     *
     * @param o the node-list view
     * @param r the range of indices to select
     * @return a node-list view containing the selected nodes
     */
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

    /**
     * Returns the DOM node name used for GPath name lookups.
     *
     * @param node the node to inspect
     * @return the node name
     */
    public static String name(Node node) {
        return node.getNodeName();
    }

    /**
     * Returns the parent DOM node.
     *
     * @param node the node whose parent should be returned
     * @return the parent node, or {@code null} for a root node
     */
    public static Node parent(Node node) {
        return node.getParentNode();
    }

    /**
     * Returns the text visible from a DOM node.
     * Text and CDATA nodes return their value directly; other nodes concatenate descendant text.
     *
     * @param node the node to inspect
     * @return the concatenated text for the node
     */
    public static String text(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
            return node.getNodeValue();
        }
        if (node.hasChildNodes()) {
            return text(node.getChildNodes());
        }
        return "";
    }

    /**
     * Concatenates the text visible from every node in the list.
     *
     * @param nodeList the nodes to inspect
     * @return the concatenated text for all nodes in document order
     */
    public static String text(NodeList nodeList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeList.getLength(); i++) {
            sb.append(text(nodeList.item(i)));
        }
        return sb.toString();
    }

    /**
     * Copies a {@link NodeList} into a mutable {@link List}.
     *
     * @param self the node list to copy
     * @return a list containing the nodes from the node list
     */
    public static List<Node> list(NodeList self) {
        List<Node> answer = new ArrayList<Node>();
        Iterator<Node> it = XmlExtensions.iterator(self);
        while (it.hasNext()) {
            answer.add(it.next());
        }
        return answer;
    }

    /**
     * Returns a depth-first traversal view containing the element itself followed by descendant elements.
     *
     * @param self the root element for traversal
     * @return a node-list view in depth-first order
     */
    public static NodeList depthFirst(Element self) {
        List<NodeList> result = new ArrayList<NodeList>();
        result.add(createNodeList(self));
        result.add(self.getElementsByTagName("*"));
        return new NodeListsHolder(result);
    }

    /**
     * Sets the value of the first child text node, creating one if the element has no children.
     *
     * @param self the element to update
     * @param value the text value to set
     */
    public static void setValue(Element self, String value) {
        Node firstChild = self.getFirstChild();
        if (firstChild == null) {
            firstChild = self.getOwnerDocument().createTextNode(value);
            self.appendChild(firstChild);
        }
        firstChild.setNodeValue(value);
    }

    /**
     * Performs a GPath-style property assignment on an element.
     * Attribute assignments use the {@code @name} form; all other properties are delegated to Groovy property handling.
     *
     * @param self the element to update
     * @param property the property or attribute selector
     * @param value the value to assign
     */
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

    /**
     * Appends a child element with the given name.
     *
     * @param self the parent element
     * @param name the child element name, optionally a {@link QName}
     * @return the appended child element
     */
    public static Element appendNode(Element self, Object name) {
        return appendNode(self, name, (String)null);
    }

    /**
     * Appends a child element with the given name and attributes.
     *
     * @param self the parent element
     * @param name the child element name, optionally a {@link QName}
     * @param attributes the attributes to apply to the new child
     * @return the appended child element
     */
    public static Element appendNode(Element self, Object name, Map attributes) {
        return appendNode(self, name, attributes, null);
    }

    /**
     * Appends a child element with the given name and optional text value.
     *
     * @param self the parent element
     * @param name the child element name, optionally a {@link QName}
     * @param value the text value to append inside the new child, or {@code null}
     * @return the appended child element
     */
    public static Element appendNode(Element self, Object name, String value) {
        Document doc = self.getOwnerDocument();
        Element newChild;
        if (name instanceof QName qn) {
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

    /**
     * Appends a child element with attributes and optional text content.
     *
     * @param self the parent element
     * @param name the child element name, optionally a {@link QName}
     * @param attributes the attributes to apply to the new child
     * @param value the text value to append inside the new child, or {@code null}
     * @return the appended child element
     */
    public static Element appendNode(Element self, Object name, Map attributes, String value) {
        Element result = appendNode(self, name, value);
        for (Object o : attributes.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            putAt(result, "@" + e.getKey().toString(), e.getValue());
        }
        return result;
    }

    /**
     * Replaces a single selected node with the nodes produced by the supplied builder closure.
     *
     * @param self the node selection to replace; it must contain exactly one node
     * @param c the builder closure creating replacement nodes
     * @return the removed node
     * @throws GroovyRuntimeException if the selection does not contain exactly one node
     */
    public static Node replaceNode(NodesHolder self, Closure c) {
        if (self.getLength() != 1) {
            throw new GroovyRuntimeException(
                    "replaceNode() can only be used to replace a single element, " +
                    "but was applied to " + self.getLength() + " elements."
            );
        }
        return replaceNode(self.item(0), c);
    }

    /**
     * Replaces a node with the nodes produced by the supplied builder closure.
     *
     * @param self the node to replace
     * @param c the builder closure creating replacement nodes
     * @return the removed node
     * @throws UnsupportedOperationException if {@code self} is the document root
     */
    public static Node replaceNode(Node self, Closure c) {
        if (self.getParentNode() instanceof Document) {
            throw new UnsupportedOperationException("Replacing the root node is not supported");
        }
        appendNodes(self, c);
        self.getParentNode().removeChild(self);
        return self;
    }

    /**
     * Adds sibling nodes after the supplied element using the builder closure.
     *
     * @param self the element after which new siblings should be inserted
     * @param c the builder closure creating the sibling nodes
     * @throws UnsupportedOperationException if {@code self} is the document root
     */
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
        Iterator<Node> iter = XmlExtensions.iterator(children(newNodes));
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

    /**
     * Adds sibling nodes after every element in the supplied node list.
     *
     * @param self the elements after which new siblings should be inserted
     * @param c the builder closure creating the sibling nodes
     */
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

    /**
     * Returns a breadth-first traversal view containing the element and its descendants level by level.
     *
     * @param self the root element for traversal
     * @return a node-list view in breadth-first order
     */
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

    /**
     * Returns the child nodes visible to DOMCategory navigation.
     * Child elements are always included, and retained text nodes are included when they are not discarded as ignorable whitespace.
     *
     * @param self the parent element
     * @return a node-list view of the visible children
     */
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
                if ((!isGlobalKeepIgnorableWhitespace() && value.trim().isEmpty()) || isGlobalTrimWhitespace()) {
                    value = value.trim();
                }
                if ("*".equals(elementName) && !value.isEmpty()) {
                    node.setNodeValue(value);
                    result.add(node);
                }
            }
        }
        return new NodesHolder(result);
    }

    /**
     * Renders a DOMCategory value in a GPath-friendly string form.
     *
     * @param o the value to render
     * @return text for text nodes, list-style output for node lists, or {@code o.toString()}
     */
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

    /**
     * Evaluates an XPath expression against the supplied node.
     *
     * @param self the context node
     * @param expression the XPath expression to evaluate
     * @param returnType the desired XPath return type
     * @return the XPath evaluation result
     * @throws GroovyRuntimeException if the expression cannot be evaluated
     */
    public static Object xpath(Node self, String expression, javax.xml.namespace.QName returnType) {
        final XPath xpath = FactorySupport.createXPathFactory().newXPath();
        try {
            return xpath.evaluate(expression, self, returnType);
        } catch (XPathExpressionException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    /**
     * Evaluates an XPath expression against the supplied node and returns the string result.
     *
     * @param self the context node
     * @param expression the XPath expression to evaluate
     * @return the string result of the XPath evaluation
     * @throws GroovyRuntimeException if the expression cannot be evaluated
     */
    public static String xpath(Node self, String expression) {
        final XPath xpath = FactorySupport.createXPathFactory().newXPath();
        try {
            return xpath.evaluate(expression, self);
        } catch (XPathExpressionException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    private static String toString(NodeList self) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator it = XmlExtensions.iterator(self);
        while (it.hasNext()) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(it.next().toString());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns the number of nodes in the supplied node list.
     *
     * @param self the node list to inspect
     * @return the number of nodes
     */
    public static int size(NodeList self) {
        return self.getLength();
    }

    /**
     * Determines whether the supplied node list is empty.
     *
     * @param self the node list to inspect
     * @return {@code true} if the node list contains no nodes
     */
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

        /**
         * {@inheritDoc}
         */
        @Override
        public int getLength() {
            int length = 0;
            for (NodeList nl : nodeLists) {
                length += nl.getLength();
            }
            return length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
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

        /**
         * Returns the DOMCategory list rendering of this aggregated node-list view.
         *
         * @return a list-style string representation
         */
        @Override
        public String toString() {
            return DOMCategory.toString(this);
        }
    }

    private static final class NodesHolder implements NodeList {
        private final List<Node> nodes;

        private NodesHolder(List<Node> nodes) {
            this.nodes = nodes;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getLength() {
            return nodes.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Node item(int index) {
            if (index < 0 || index >= getLength()) {
                return null;
            }
            return nodes.get(index);
        }
    }
}
