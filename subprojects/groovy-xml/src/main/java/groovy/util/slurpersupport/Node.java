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
package groovy.util.slurpersupport;

import groovy.lang.Buildable;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.Writable;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Represents a node.
 */
@Deprecated
public class Node implements Writable {
    private final String name;
    private final Map attributes;
    private final Map attributeNamespaces;
    private final String namespaceURI;
    private final List children = new LinkedList();
    private final Stack replacementNodeStack = new Stack();
    private final Node parent;

    /**
     * @param parent the parent node
     * @param name the name for the node
     * @param attributes the attributes for the node
     * @param attributeNamespaces the namespace mappings for attributes
     * @param namespaceURI the namespace URI if any
     */
    public Node(final Node parent, final String name, final Map attributes, final Map attributeNamespaces, final String namespaceURI) {
        this.name = name;
        this.attributes = attributes;
        this.attributeNamespaces = attributeNamespaces;
        this.namespaceURI = namespaceURI;
        this.parent = parent;
    }

    /**
     * Returns the name of this Node.
     * @return the name of this Node
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns the parent of this Node.
     * @return the parent of this Node
     */
    public Node parent() {
        return this.parent;
    }

    /**
     * Returns the URI of the namespace of this Node.
     * @return the namespace of this Node
     */
    public String namespaceURI() {
        return this.namespaceURI;
    }

    /**
     * Returns a map of the attributes of this Node.
     * @return a map of the attributes of this Node
     */
    public Map attributes() {
        return this.attributes;
    }

    /**
     * Returns a list of the children of this Node.
     * @return a list of the children of this Node
     */
    public List children() {
        return this.children;
    }

    /**
     * Adds an object as a new child to this Node.
     * @param child the object to add as a child
     */
    public void addChild(final Object child) {
        this.children.add(child);
    }

    public void replaceNode(final Closure replacementClosure, final GPathResult result) {
        this.replacementNodeStack.push(new ReplacementNode() {
            public void build(final GroovyObject builder, final Map namespaceMap, final Map<String, String> namespaceTagHints) {
                final Closure c = (Closure) replacementClosure.clone();
                Node.this.replacementNodeStack.pop(); // disable the replacement whilst the closure is being executed
                c.setDelegate(builder);
                c.call(new Object[]{result});
                Node.this.replacementNodeStack.push(this);
            }
        });
    }

    /**
     * Replaces the current body of this Node with the passed object.
     * @param newValue the new body
     */
    protected void replaceBody(final Object newValue) {
        this.children.clear();
        this.children.add(newValue);
    }

    protected void appendNode(final Object newValue, final GPathResult result) {
        if (newValue instanceof Closure) {
            this.children.add(new ReplacementNode() {
                public void build(final GroovyObject builder, final Map namespaceMap, final Map<String, String> namespaceTagHints) {
                    final Closure c = (Closure) ((Closure) newValue).clone();
                    c.setDelegate(builder);
                    c.call(new Object[]{result});
                }
            });
        } else {
            this.children.add(newValue);
        }
    }

    /**
     * Returns a string containing the text of the children of this Node.
     * @return a string containing the text of the children of this Node
     */
    public String text() {
        final StringBuilder sb = new StringBuilder();
        for (Object child : this.children) {
            if (child instanceof Node) {
                sb.append(((Node) child).text());
            } else {
                sb.append(child);
            }
        }
        return sb.toString();
    }

    /**
     * Returns the list of any direct String nodes of this node.
     *
     * @return the list of String values from this node
     * @since 2.3.0
     */
    public List<String> localText() {
        final List<String> result = new ArrayList<String>();
        for (Object child : this.children) {
            if (!(child instanceof Node)) {
                result.add(child.toString());
            }
        }
        return result;
    }

    /**
     * Returns an iterator over the child nodes of this Node.
     * @return an iterator over the child nodes of this Node
     */
    public Iterator childNodes() {
        return new Iterator() {
            private final Iterator iter = Node.this.children.iterator();
            private Object nextElementNodes = getNextElementNodes();

            public boolean hasNext() {
                return this.nextElementNodes != null;
            }

            public Object next() {
                try {
                    return this.nextElementNodes;
                } finally {
                    this.nextElementNodes = getNextElementNodes();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private Object getNextElementNodes() {
                while (iter.hasNext()) {
                    final Object node = iter.next();
                    if (node instanceof Node) {
                        return node;
                    }
                }
                return null;
            }
        };
    }

    public Writer writeTo(final Writer out) throws IOException {
        if (this.replacementNodeStack.empty()) {
            for (Object child : this.children) {
                if (child instanceof Writable) {
                    ((Writable) child).writeTo(out);
                } else {
                    out.write(child.toString());
                }
            }
            return out;
        } else {
            return ((Writable) this.replacementNodeStack.peek()).writeTo(out);
        }
    }

    public void build(final GroovyObject builder, final Map namespaceMap, final Map<String, String> namespaceTagHints) {
        if (this.replacementNodeStack.empty()) {
            final Closure rest = new Closure(null) {
                public Object doCall(final Object o) {
                    buildChildren(builder, namespaceMap, namespaceTagHints);
                    return null;
                }
            };

            if (this.namespaceURI.length() == 0 && this.attributeNamespaces.isEmpty()) {
                builder.invokeMethod(this.name, new Object[]{this.attributes, rest});
            } else {
                final List newTags = new LinkedList();
                builder.getProperty("mkp");
                final List namespaces = (List) builder.invokeMethod("getNamespaces", new Object[]{});

                final Map current = (Map) namespaces.get(0);
                final Map pending = (Map) namespaces.get(1);

                if (this.attributeNamespaces.isEmpty()) {
                    builder.getProperty(getTagFor(this.namespaceURI, current, pending, namespaceMap, namespaceTagHints, newTags, builder));
                    builder.invokeMethod(this.name, new Object[]{this.attributes, rest});
                } else {
                    final Map attributesWithNamespaces = new HashMap(this.attributes);
                    for (Object key : this.attributes.keySet()) {
                        final Object attributeNamespaceURI = this.attributeNamespaces.get(key);
                        if (attributeNamespaceURI != null) {
                            attributesWithNamespaces.put(getTagFor(attributeNamespaceURI, current, pending, namespaceMap, namespaceTagHints, newTags, builder) +
                                    "$" + key, attributesWithNamespaces.remove(key));
                        }
                    }
                    builder.getProperty(getTagFor(this.namespaceURI, current, pending, namespaceMap, namespaceTagHints, newTags, builder));
                    builder.invokeMethod(this.name, new Object[]{attributesWithNamespaces, rest});
                }

                // remove the new tags we had to define for this element
                if (!newTags.isEmpty()) {
                    final Iterator iter = newTags.iterator();
                    do {
                        pending.remove(iter.next());
                    } while (iter.hasNext());
                }
            }
        } else {
            ((ReplacementNode) this.replacementNodeStack.peek()).build(builder, namespaceMap, namespaceTagHints);
        }
    }

    private static String getTagFor(final Object namespaceURI, final Map current,
                                    final Map pending, final Map local, final Map tagHints,
                                    final List newTags, final GroovyObject builder) {
        String tag = findNamespaceTag(pending, namespaceURI); // look in the namespaces whose declaration has already been emitted
        if (tag == null) {
            tag = findNamespaceTag(current, namespaceURI);  // look in the namespaces who will be declared at the next element

            if (tag == null) {
                // we have to declare the namespace - choose a tag
                tag = findNamespaceTag(local, namespaceURI);  // If the namespace has been declared in the GPath expression use that tag

                if (tag == null || tag.length() == 0) {
                    tag = findNamespaceTag(tagHints, namespaceURI);  // If the namespace has been used in the parse document use that tag
                }

                if (tag == null || tag.length() == 0) { // otherwise make up a new tag and check it has not been used before
                    int suffix = 0;
                    do {
                        final String possibleTag = "tag" + suffix++;

                        if (!pending.containsKey(possibleTag) && !current.containsKey(possibleTag) && !local.containsKey(possibleTag)) {
                            tag = possibleTag;
                        }
                    } while (tag == null);
                }

                final Map newNamespace = new HashMap();
                newNamespace.put(tag, namespaceURI);
                builder.getProperty("mkp");
                builder.invokeMethod("declareNamespace", new Object[]{newNamespace});
                newTags.add(tag);
            }
        }
        return tag;
    }

    private static String findNamespaceTag(final Map tagMap, final Object namespaceURI) {
        if (tagMap.containsValue(namespaceURI)) {
            for (Object o : tagMap.entrySet()) {
                final Map.Entry entry = (Map.Entry) o;
                if (namespaceURI.equals(entry.getValue())) {
                    return (String) entry.getKey();
                }
            }
        }
        return null;
    }

    private void buildChildren(final GroovyObject builder, final Map namespaceMap, final Map<String, String> namespaceTagHints) {
        for (Object child : this.children) {
            if (child instanceof Node) {
                ((Node) child).build(builder, namespaceMap, namespaceTagHints);
            } else if (child instanceof Buildable) {
                ((Buildable) child).build(builder);
            } else {
                builder.getProperty("mkp");
                builder.invokeMethod("yield", new Object[]{child});
            }
        }
    }
}
