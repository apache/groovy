/*
 * $Id$version Apr 25, 2004 5:18:30 PM $user Exp $
 * 
 * Copyright 2003 (C) Sam Pullara. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy of
 * this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. 3. The
 * name "groovy" must not be used to endorse or promote products derived from
 * this Software without prior written permission of The Codehaus. For written
 * permission, please contact info@codehaus.org. 4. Products derived from this
 * Software may not be called "groovy" nor may "groovy" appear in their names
 * without prior written permission of The Codehaus. "groovy" is a registered
 * trademark of The Codehaus. 5. Due credit should be given to The Codehaus -
 * http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package groovy.xml.dom;

import org.w3c.dom.*;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author sam
 * @author paulk
 */
public class DOMCategory {

    private static boolean trimWhitespace = true;

    public static Object get(Object o, String elementName) {
        if (o instanceof Element) {
            return get((Element) o, elementName);
        }
        if (o instanceof NodeList) {
            return get((NodeList) o, elementName);
        }
        if (o instanceof NamedNodeMap) {
            return get((NamedNodeMap) o, elementName);
        }
        return null;
    }

    private static Object get(Element element, String elementName) {
        return getAt(element, elementName);
    }

    private static Object get(NodeList nodeList, String elementName) {
        return getAt(nodeList, elementName);
    }

    private static Object get(NamedNodeMap nodeMap, String elementName) {
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
                addResult(results, get(node, elementName));
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

    public static Node getAt(Element element, int i) {
        if (hasChildElements(element, "*")) {
            NodeList nodeList = getChildElements(element, "*");
            return nodeList.item(i);
        }
        return null;
    }

    public static Node getAt(NodeList nodeList, int i) {
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

    public static Object text(Object o) {
        if (o instanceof Element) {
            return text((Element) o);
        }
        if (o instanceof Node) {
            Node n = (Node) o;
            if (n.getNodeType() == Node.TEXT_NODE) {
                return n.getNodeValue();
            }
        }
        if (o instanceof NodeList) {
            return text((NodeList) o);
        }
        return null;
    }

    private static String text(Element element) {
        if (!element.hasChildNodes()) {
            return null;
        }
        if (element.getFirstChild().getNodeType() != Node.TEXT_NODE) {
            return null;
        }
        return element.getFirstChild().getNodeValue();
    }

    private static List text(NodeList nodeList) {
        List results = new ArrayList();
        for (int i = 0; i < nodeList.getLength(); i++) {
            addResult(results, text(nodeList.item(i)));
        }
        return results;
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
            if (((Node)o).getNodeType() == Node.TEXT_NODE) {
                return ((Node)o).getNodeValue();
            }
        }
        if (o instanceof NodeList) {
            return toString((NodeList)o);
        }
        return o.toString();
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

    private static void addResult(List results, Object result) {
        if (result != null) {
            if (result instanceof Collection) {
                results.addAll((Collection) result);
            } else {
                results.add(result);
            }
        }
    }

    private static class NodeListsHolder implements NodeList {
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

    private static class NodesHolder implements NodeList {
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