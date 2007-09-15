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
package groovy.util;

import groovy.xml.QName;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.PrintWriter;
import java.util.*;

/**
 * Represents an arbitrary tree node which can be used for structured metadata or any arbitrary XML-like tree.
 * A node can have a name, a value and an optional Map of attributes.
 * Typically the name is a String and a value is either a String or a List of other Nodes,
 * though the types are extensible to provide a flexible structure, e.g. you could use a
 * QName as the name which includes a namespace URI and a local name. Or a JMX ObjectName etc.
 * So this class can represent metadata like {foo a=1 b="abc"} or nested metadata like {foo a=1 b="123" { bar x=12 text="hello" }}
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Node implements java.io.Serializable {

    private static final long serialVersionUID = 4121134753270542643L;
    private Node parent;
    private Object name;
    private Map attributes;
    private Object value;

    public Node(Node parent, Object name) {
        this(parent, name, new ArrayList());
    }

    public Node(Node parent, Object name, Object value) {
        this(parent, name, new HashMap(), value);
    }

    public Node(Node parent, Object name, Map attributes) {
        this(parent, name, attributes, new ArrayList());
    }

    public Node(Node parent, Object name, Map attributes, Object value) {
        this.parent = parent;
        this.name = name;
        this.attributes = attributes;
        this.value = value;
        
        if (parent != null) {
            Object parentValue = parent.value();
            List parentList;
            if (parentValue instanceof List) {
                parentList = (List) parentValue;
            } else {
                parentList = new NodeList();
                parentList.add(parentValue);
                parent.setValue(parentList);
            }
            parentList.add(this);
        }
    }

    public String text() {
        if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof Collection) {
            Collection coll = (Collection) value;
            String previousText = null;
            StringBuffer buffer = null;
            for (Iterator iter = coll.iterator(); iter.hasNext();) {
                Object child = iter.next();
                if (child instanceof String) {
                    String childText = (String) child;
                    if (previousText == null) {
                        previousText = childText;
                    }
                    else {
                        if (buffer == null) {
                            buffer = new StringBuffer();
                            buffer.append(previousText);
                        }
                        buffer.append(childText);
                    }
                }
            }
            if (buffer != null) {
                return buffer.toString();
            }
            else {
                if (previousText != null) {
                    return previousText;
                }
            }
        }
        return "";
    }

    
    public Iterator iterator() {
        return children().iterator();
    }
    
    public List children() {
        if (value == null) {
            return new ArrayList();
        }
        else if (value instanceof List) {
            return (List) value;
        }
        else {
            // we're probably just a String
            return java.util.Collections.singletonList(value);
        }
    }

    public Map attributes() {
        return attributes;
    }

    public Object attribute(Object key) {
        return (attributes != null) ? attributes.get(key) : null;
    }
    
    public Object name() {
        return name;
    }

    public Object value() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Node parent() {
        return parent;
    }

    /**
     * Provides lookup of elements by non-namespaced name
     * @param key the name (or shortcut key) of the node(s) of interest
     * @return the nodes which match key
     */
    public Object get(String key) {
        if (key != null && key.charAt(0) == '@') {
            String attributeName = key.substring(1);
            return attributes().get(attributeName);
        }
        if ("..".equals(key)) {
            return parent();
        }
        if ("*".equals(key)) {
            return children();
        }
        if ("**".equals(key)) {
            return depthFirst();
        }
        // iterate through list looking for node with name 'key'
        List answer = new NodeList();
        for (Iterator iter = children().iterator(); iter.hasNext();) {
            Object child = iter.next();
            if (child instanceof Node) {
                Node childNode = (Node) child;
                Object childNodeName = childNode.name();
                if (childNodeName != null && childNodeName.equals(key)) {
                    answer.add(childNode);
                }
            }
        }
        return answer;
    }
    
    /**
     * Provides lookup of elements by QName.
     *
     * @param name the QName of interest
     * @return the nodes matching name
     */
    public NodeList getAt(QName name) {
        NodeList answer = new NodeList();
        for (Iterator iter = children().iterator(); iter.hasNext();) {
            Object child = iter.next();
            if (child instanceof Node) {
                Node childNode = (Node) child;
                Object childNodeName = childNode.name();
                if (childNodeName != null && childNodeName.equals(name)) {
                    answer.add(childNode);
                }
            }
        }
        return answer;
    }

    /**
     * Provide a collection of all the nodes in the tree
     * using a depth first traversal.
     *
     * @return the list of (depth-first) ordered nodes
     */
    public List depthFirst() {
        List answer = new NodeList();
        answer.add(this);
        answer.addAll(depthFirstRest());
        return answer;
    }
    
    private List depthFirstRest() {
        List answer = new NodeList();
        for (Iterator iter = InvokerHelper.asIterator(value); iter.hasNext(); ) {
            Object child = iter.next();
            if (child instanceof Node) {
                Node childNode = (Node) child;
                List children = childNode.depthFirstRest();
                answer.add(childNode);
                answer.addAll(children);
            }
        }
        return answer;
    }

    /**
     * Provide a collection of all the nodes in the tree
     * using a breadth-first traversal.
     *
     * @return the list of (breadth-first) ordered nodes
     */
    public List breadthFirst() {
        List answer = new NodeList();
        answer.add(this);
        answer.addAll(breadthFirstRest());
        return answer;
    }
    
    private List breadthFirstRest() {
        List answer = new NodeList();
        List nextLevelChildren = getDirectChildren();
        while (!nextLevelChildren.isEmpty()) {
            List working = new NodeList(nextLevelChildren);
            nextLevelChildren = new NodeList();
            for (Iterator iter = working.iterator(); iter.hasNext(); ) {
                Node childNode = (Node) iter.next();
                answer.add(childNode);
                List children = childNode.getDirectChildren();
                nextLevelChildren.addAll(children);
            }
        }
        return answer;
    }

    private List getDirectChildren() {
        List answer = new NodeList();
        for (Iterator iter = InvokerHelper.asIterator(value); iter.hasNext(); ) {
            Object child = iter.next();
            if (child instanceof Node) {
                Node childNode = (Node) child;
                answer.add(childNode);
            }
        }
        return answer;
    }

    public String toString() {
        return name + "[attributes=" + attributes + "; value=" + value + "]";
    }

    public void print(PrintWriter out) {
        new NodePrinter(out).print(this);
    }
}
