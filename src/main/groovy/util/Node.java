/*
 * Copyright 2003-2012 the original author or authors.
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

import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.xml.QName;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

/**
 * Represents an arbitrary tree node which can be used for structured metadata or any arbitrary XML-like tree.
 * A node can have a name, a value and an optional Map of attributes.
 * Typically the name is a String and a value is either a String or a List of other Nodes,
 * though the types are extensible to provide a flexible structure, e.g. you could use a
 * QName as the name which includes a namespace URI and a local name. Or a JMX ObjectName etc.
 * So this class can represent metadata like <code>{foo a=1 b="abc"}</code> or nested
 * metadata like <code>{foo a=1 b="123" { bar x=12 text="hello" }}</code>
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Paul King
 * @version $Revision$
 */
public class Node implements Serializable {

    static {
        // wrap the standard MetaClass with the delegate
        setMetaClass(GroovySystem.getMetaClassRegistry().getMetaClass(Node.class), Node.class);
    }

    private static final long serialVersionUID = 4121134753270542643L;

    private Node parent;

    private Object name;

    private Map attributes;

    private Object value;

    /**
     * Creates a new Node named <code>name</code> and if a parent is supplied, adds
     * the newly created node as a child of the parent.
     *
     * @param parent the parent node or null if no parent
     * @param name   the name of the node
     */
    public Node(Node parent, Object name) {
        this(parent, name, new NodeList());
    }

    /**
     * Creates a new Node named <code>name</code> with value <code>value</code> and
     * if a parent is supplied, adds the newly created node as a child of the parent.
     *
     * @param parent the parent node or null if no parent
     * @param name   the name of the node
     * @param value  the Node value, e.g. some text but in general any Object
     */
    public Node(Node parent, Object name, Object value) {
        this(parent, name, new HashMap(), value);
    }

    /**
     * Creates a new Node named <code>name</code> with
     * attributes specified in the <code>attributes</code> Map. If a parent is supplied,
     * the newly created node is added as a child of the parent.
     *
     * @param parent     the parent node or null if no parent
     * @param name       the name of the node
     * @param attributes a Map of name-value pairs
     */
    public Node(Node parent, Object name, Map attributes) {
        this(parent, name, attributes, new NodeList());
    }

    /**
     * Creates a new Node named <code>name</code> with value <code>value</code> and
     * with attributes specified in the <code>attributes</code> Map. If a parent is supplied,
     * the newly created node is added as a child of the parent.
     *
     * @param parent     the parent node or null if no parent
     * @param name       the name of the node
     * @param attributes a Map of name-value pairs
     * @param value      the Node value, e.g. some text but in general any Object
     */
    public Node(Node parent, Object name, Map attributes, Object value) {
        this.parent = parent;
        this.name = name;
        this.attributes = attributes;
        this.value = value;

        if (parent != null) {
            getParentList(parent).add(this);
        }
    }

    private List getParentList(Node parent) {
        Object parentValue = parent.value();
        List parentList;
        if (parentValue instanceof List) {
            parentList = (List) parentValue;
        } else {
            parentList = new NodeList();
            parentList.add(parentValue);
            parent.setValue(parentList);
        }
        return parentList;
    }

    public boolean append(Node child) {
        child.setParent(this);
        return getParentList(this).add(child);
    }

    public boolean remove(Node child) {
        child.setParent(null);
        return getParentList(this).remove(child);
    }

    public Node appendNode(Object name, Map attributes) {
        return new Node(this, name, attributes);
    }

    public Node appendNode(Object name) {
        return new Node(this, name);
    }

    public Node appendNode(Object name, Object value) {
        return new Node(this, name, value);
    }

    public Node appendNode(Object name, Map attributes, Object value) {
        return new Node(this, name, attributes, value);
    }

    // TODO return replaced node rather than last appended?
    // * @return the original now replaced node
    /**
     * Replaces the current node with nodes defined using builder-style notation via a Closure.
     *
     * @param c A Closure defining the new nodes using builder-style notation.
     * @return the last appended node
     */
    public Node replaceNode(Closure c) {
        if (parent() == null) {
            throw new UnsupportedOperationException("Replacing the root node is not supported");
        }
        Node result = appendNodes(c);
        getParentList(parent()).remove(this);
//        this.setParent(null);
//        return this;
        return result;
    }

    /**
     * Adds sibling nodes (defined using builder-style notation via a Closure) after the current node.
     *
     * @param c A Closure defining the new sibling nodes to add using builder-style notation.
     */
    public void plus(Closure c) {
        if (parent() == null) {
            throw new UnsupportedOperationException("Adding sibling nodes to the root node is not supported");
        }
        appendNodes(c);
    }

    private Node appendNodes(Closure c) {
        List list = parent().children();
        int afterIndex = list.indexOf(this);
        List leftOvers = new ArrayList(list.subList(afterIndex + 1, list.size()));
        list.subList(afterIndex + 1, list.size()).clear();
        Node lastAppended = null;
        for (Node child : buildChildrenFromClosure(c)) {
            lastAppended = parent().appendNode(child.name(), child.attributes(), child.value());
        }
        parent().children().addAll(leftOvers);
        return lastAppended;
    }

    private List<Node> buildChildrenFromClosure(Closure c) {
        NodeBuilder b = new NodeBuilder();
        Node newNode = (Node) b.invokeMethod("dummyNode", c);
        return newNode.children();
    }

    protected static void setMetaClass(final MetaClass metaClass, Class nodeClass) {
        final MetaClass newMetaClass = new DelegatingMetaClass(metaClass) {
            @Override
            public Object getAttribute(final Object object, final String attribute) {
                Node n = (Node) object;
                return n.get("@" + attribute);
            }

            @Override
            public void setAttribute(final Object object, final String attribute, final Object newValue) {
                Node n = (Node) object;
                n.attributes().put(attribute, newValue);
            }

            @Override
            public Object getProperty(Object object, String property) {
                if (object instanceof Node) {
                    Node n = (Node) object;
                    return n.get(property);
                }
                return super.getProperty(object, property);
            }

            @Override
            public void setProperty(Object object, String property, Object newValue) {
                if (property.startsWith("@")) {
                    String attribute = property.substring(1);
                    Node n = (Node) object;
                    n.attributes().put(attribute, newValue);
                    return;
                }
                delegate.setProperty(object, property, newValue);
            }

        };
        GroovySystem.getMetaClassRegistry().setMetaClass(nodeClass, newMetaClass);
    }

    public String text() {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Collection) {
            Collection coll = (Collection) value;
            String previousText = null;
            StringBuilder sb = null;
            for (Object child : coll) {
                if (child instanceof String) {
                    String childText = (String) child;
                    if (previousText == null) {
                        previousText = childText;
                    } else {
                        if (sb == null) {
                            sb = new StringBuilder();
                            sb.append(previousText);
                        }
                        sb.append(childText);
                    }
                }
            }
            if (sb != null) {
                return sb.toString();
            } else {
                if (previousText != null) {
                    return previousText;
                }
                return "";
            }
        }
        return "" + value;
    }

    public Iterator iterator() {
        return children().iterator();
    }

    public List children() {
        if (value == null) {
            return new NodeList();
        }
        if (value instanceof List) {
            return (List) value;
        }
        // we're probably just a String
        List result = new NodeList();
        result.add(value);
        return result;
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

    protected void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Provides lookup of elements by non-namespaced name
     *
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
        return getByName(key);
    }

    /**
     * Provides lookup of elements by QName.
     *
     * @param name the QName of interest
     * @return the nodes matching name
     */
    public NodeList getAt(QName name) {
        NodeList answer = new NodeList();
        for (Object child : children()) {
            if (child instanceof Node) {
                Node childNode = (Node) child;
                Object childNodeName = childNode.name();
                if (name.matches(childNodeName)) {
                    answer.add(childNode);
                }
            }
        }
        return answer;
    }

    /**
     * Provides lookup of elements by name.
     *
     * @param name the name of interest
     * @return the nodes matching name
     */
    private NodeList getByName(String name) {
        NodeList answer = new NodeList();
        for (Object child : children()) {
            if (child instanceof Node) {
                Node childNode = (Node) child;
                Object childNodeName = childNode.name();
                if (childNodeName instanceof QName) {
                    QName qn = (QName) childNodeName;
                    if (qn.matches(name)) {
                        answer.add(childNode);
                    }
                } else if (name.equals(childNodeName)) {
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
                if (children.size() > 1 || (children.size() == 1 && !(children.get(0) instanceof String))) answer.addAll(children);
            } else if (child instanceof String) {
                answer.add(child);
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
            for (Object child : working) {
                answer.add(child);
                if (child instanceof Node) {
                    Node childNode = (Node) child;
                    List children = childNode.getDirectChildren();
                    if (children.size() > 1 || (children.size() == 1 && !(children.get(0) instanceof String))) nextLevelChildren.addAll(children);
                }
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
            } else if (child instanceof String) {
                answer.add(child);
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
