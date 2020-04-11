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
package groovy.xml.slurpersupport;

import groovy.lang.Buildable;
import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.IntRange;
import groovy.lang.MetaClass;
import groovy.lang.Writable;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Base class for representing lazy evaluated GPath expressions.
 */
public abstract class GPathResult extends GroovyObjectSupport implements Writable, Buildable, Iterable {
    protected final GPathResult parent;
    protected final String name;
    protected final String namespacePrefix;
    protected final Map namespaceMap = new HashMap();
    protected final Map<String, String> namespaceTagHints;

    /**
     * Creates a new GPathResult named <code>name</code> with the parent <code>parent</code>,
     * the namespacePrefix <code>namespacePrefix</code> and the namespaceTagHints specified in
     * the <code>namespaceTagHints</code> Map.
     *
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param name if the GPathResult corresponds to something with a name, e.g. a node
     * @param namespacePrefix the namespace prefix if any
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public GPathResult(final GPathResult parent, final String name, final String namespacePrefix, final Map<String, String> namespaceTagHints) {
        if (parent == null) {
            // we are the top of the tree
            this.parent = this;
            this.namespaceMap.put("xml", "http://www.w3.org/XML/1998/namespace");  // The XML namespace is always defined
        } else {
            this.parent = parent;
            this.namespaceMap.putAll(parent.namespaceMap);
        }
        this.name = name;
        this.namespacePrefix = namespacePrefix;
        this.namespaceTagHints = namespaceTagHints;

        setMetaClass(getMetaClass()); // wrap the standard MetaClass with the delegate
    }

    /**
     * Replaces the MetaClass of this GPathResult.
     *
     * @param metaClass the new MetaClass
     */
    @Override
    public void setMetaClass(final MetaClass metaClass) {
        final MetaClass newMetaClass = new DelegatingMetaClass(metaClass) {
            @Override
            public Object getAttribute(final Object object, final String attribute) {
                return GPathResult.this.getProperty("@" + attribute);
            }

            @Override
            public void setAttribute(final Object object, final String attribute, final Object newValue) {
                GPathResult.this.setProperty("@" + attribute, newValue);
            }
        };
        super.setMetaClass(newMetaClass);
    }

    /**
     * Returns the specified Property of this GPathResult.
     * <p>
     * Realizes the follow shortcuts:
     * <ul>
     * <li><code>'..'</code> for <code>parent()</code>
     * <li><code>'*'</code> for <code>children()</code>
     * <li><code>'**'</code> for <code>depthFirst()</code>
     * <li><code>'@'</code> for attribute access
     * </ul>
     * @param property the Property to fetch
     */
    public Object getProperty(final String property) {
        if ("..".equals(property)) {
            return parent();
        } else if ("*".equals(property)) {
            return children();
        } else if ("**".equals(property)) {
            return depthFirst();
        } else if (property.startsWith("@")) {
            if (property.contains(":") && !this.namespaceTagHints.isEmpty()) {
                final int i = property.indexOf(':');
                return new Attributes(this, "@" + property.substring(i + 1), property.substring(1, i), this.namespaceTagHints);
            } else {
                return new Attributes(this, property, this.namespaceTagHints);
            }
        } else {
            if (property.contains(":") && !this.namespaceTagHints.isEmpty()) {
                final int i = property.indexOf(':');
                return new NodeChildren(this, property.substring(i + 1), property.substring(0, i), this.namespaceTagHints);
            } else {
                return new NodeChildren(this, property, this.namespaceTagHints);
            }
        }
    }

    /**
     * Replaces the specified property of this GPathResult with a new value.
     *
     * @param property the property of this GPathResult to replace
     * @param newValue the new value of the property
     */
    public void setProperty(final String property, final Object newValue) {
        if (property.startsWith("@")) {
            if (newValue instanceof String || newValue instanceof GString) {

                for (Object o : this) {
                    final NodeChild child = (NodeChild) o;

                    child.attributes().put(property.substring(1), newValue);
                }
            }
        } else {
            final GPathResult result = new NodeChildren(this, property, this.namespaceTagHints);

            if (newValue instanceof Map) {
                for (Object o : ((Map) newValue).entrySet()) {
                    final Map.Entry entry = (Map.Entry) o;
                    result.setProperty("@" + entry.getKey(), entry.getValue());
                }
            } else {
                if (newValue instanceof Closure) {
                    result.replaceNode((Closure) newValue);
                } else {
                    result.replaceBody(newValue);
                }
            }
        }
    }

    /**
     * Overloads the left shift operator to provide an easy way to
     * lazily append Objects to this GPathResult.
     *
     * @param newValue the Object to append
     * @return <code>this</code>
     */
    public Object leftShift(final Object newValue) {
        appendNode(newValue);
        return this;
    }

    /**
     * Lazily adds the specified Object to this GPathResult.
     *
     * @param newValue the Object to add
     * @return <code>this</code>
     */
    public Object plus(final Object newValue) {
        this.replaceNode(new Closure(this) {
            public void doCall(Object[] args) {
                final GroovyObject delegate = (GroovyObject) getDelegate();
                delegate.getProperty("mkp");
                delegate.invokeMethod("yield", args);
                delegate.getProperty("mkp");
                delegate.invokeMethod("yield", new Object[]{newValue});
            }
        });

        return this;
    }

    protected abstract void replaceNode(Closure newValue);

    protected abstract void replaceBody(Object newValue);
    
    protected abstract void appendNode(Object newValue);

    /**
     * Returns the name of this GPathResult.
     *
     * @return the name of this GPathResult
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns the parent of this GPathResult. If this GPathResult has no parent the GPathResult itself is returned.
     * This is no navigation in the XML tree. It is backtracking on the GPath expression chain.
     * It is the behavior of parent() prior to 2.2.0.
     * Backtracking on '..' actually goes down one level in the tree again.
     * find() and findAll() are popped along with the level they have been applied to.
     *
     * @return the parent or <code>this</code>
     */
    public GPathResult pop() {
        return this.parent;
    }

    /**
     * Returns as GPathResult with the parent nodes of the current GPathResult
     *
     * @return the parents GPathResult or <code>this</code> for the root
     */
    public GPathResult parent() {
        return new NodeParents(this, this.namespaceTagHints);
    }

    /**
     * Returns the children of this GPathResult as a GPathResult object.
     *
     * @return the children of this GPathResult
     */
    public GPathResult children() {
        return new NodeChildren(this, this.namespaceTagHints);
    }

    /**
     * Returns the namespace mapped to the specified prefix.
     *
     * @param prefix the prefix lookup
     * @return the namespace of the prefix
     */
    public String lookupNamespace(final String prefix) {
        Object namespace = namespaceMap.get(prefix);
        if (namespace != null) {
            return namespace.toString();
        }
        return this.namespaceTagHints.isEmpty() ? prefix : this.namespaceTagHints.get(prefix);
    }

    /**
     * Returns the text of this GPathResult.
     *
     * @return the GPathResult, converted to a <code>String</code>
     */
    public String toString() {
        return text();
    }

    /**
     * Converts the text of this GPathResult to a Integer object.
     *
     * @return the GPathResult, converted to a <code>Integer</code>
     */
    public Integer toInteger() {
        if(textIsEmptyOrNull()){
            return null;
        }
        return StringGroovyMethods.toInteger((CharSequence)text());
    }

    /**
     * Converts the text of this GPathResult to a Long object.
     *
     * @return the GPathResult, converted to a <code>Long</code>
     */
    public Long toLong() {
        if(textIsEmptyOrNull()){
            return null;
        }
        return StringGroovyMethods.toLong((CharSequence)text());
    }

    /**
     * Converts the text of this GPathResult to a Float object.
     *
     * @return the GPathResult, converted to a <code>Float</code>
     */
    public Float toFloat() {
        if(textIsEmptyOrNull()){
            return null;
        }
        return StringGroovyMethods.toFloat((CharSequence)text());
    }

    /**
     * Converts the text of this GPathResult to a Double object.
     *
     * @return the GPathResult, converted to a <code>Double</code>
     */
    public Double toDouble() {
        if(textIsEmptyOrNull()){
            return null;
        }
        return StringGroovyMethods.toDouble((CharSequence)text());
    }

    /**
     * Converts the text of this GPathResult to a BigDecimal object.
     *
     * @return the GPathResult, converted to a <code>BigDecimal</code>
     */
    public BigDecimal toBigDecimal() {
        if(textIsEmptyOrNull()){
            return null;
        }
        return StringGroovyMethods.toBigDecimal((CharSequence)text());
    }

    /**
     * Converts the text of this GPathResult to a BigInteger object.
     *
     * @return the GPathResult, converted to a <code>BigInteger</code>
     */
    public BigInteger toBigInteger() {
        if(textIsEmptyOrNull()){
            return null;
        }
        return StringGroovyMethods.toBigInteger((CharSequence)text());
    }

    private boolean textIsEmptyOrNull() {
        String t = text();
        return null == t || 0 == t.length();
    }

    /**
     * Converts the text of this GPathResult to a URL object.
     *
     * @return the GPathResult, converted to a <code>URL</code>
     */
    public URL toURL() throws MalformedURLException {
        return ResourceGroovyMethods.toURL((CharSequence)text());
    }

    /**
     * Converts the text of this GPathResult to a URI object.
     *
     * @return the GPathResult, converted to a <code>URI</code>
     */
    public URI toURI() throws URISyntaxException {
        return ResourceGroovyMethods.toURI((CharSequence)text());
    }

    /**
     * Converts the text of this GPathResult to a Boolean object.
     *
     * @return the GPathResult, converted to a <code>Boolean</code>
     */
    public Boolean toBoolean() {
        return StringGroovyMethods.toBoolean(text());
    }

    /**
     * Adds the specified map of prefix to namespace mappings to this GPathResult.
     * Already existing prefixes are overwritten.
     *
     * @param newNamespaceMapping the mappings to add
     * @return <code>this</code>
     */
    public GPathResult declareNamespace(final Map newNamespaceMapping) {
        this.namespaceMap.putAll(newNamespaceMapping);
        return this;
    }

    @Override
    public int hashCode() {
        return text().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        return text().equals(obj.toString());
    }

    /**
     * Supports the subscript operator for a GPathResult.
     * <pre class="groovyTestCase">
     * import groovy.xml.slurpersupport.*
     * import groovy.xml.XmlSlurper
     * def text = """
     * &lt;characterList&gt;
     *   &lt;character/&gt;
     *   &lt;character&gt;
     *     &lt;name&gt;Gromit&lt;/name&gt;
     *   &lt;/character&gt;
     * &lt;/characterList&gt;"""
     *
     * GPathResult characterList = new XmlSlurper().parseText(text)
     *
     * assert characterList.character[1].name == 'Gromit'
     * </pre>
     * @param index an index
     * @return the value at the given index
     */
    public Object getAt(final int index) {
        if (index < 0) {
            // calculate whole list in this case
            // recommend avoiding -ve's as this is obviously not as efficient
            List list = list();
            int adjustedIndex = index + list.size();
            if (adjustedIndex >= 0 && adjustedIndex < list.size())
                return list.get(adjustedIndex);
        } else {
            final Iterator iter = iterator();
            int count = 0;

            while (iter.hasNext()) {
                if (count++ == index) {
                    return iter.next();
                } else {
                    iter.next();
                }
            }
        }

        return new NoChildren(this, this.name, this.namespaceTagHints);
    }

    /**
     * Supports the range subscript operator for a GPathResult.
     * <pre class="groovyTestCase">
     * import groovy.xml.slurpersupport.*
     * import groovy.xml.XmlSlurper
     * def text = """
     * &lt;characterList&gt;
     *   &lt;character&gt;Wallace&lt;/character&gt;
     *   &lt;character&gt;Gromit&lt;/character&gt;
     *   &lt;character&gt;Shaun&lt;/character&gt;
     * &lt;/characterList&gt;"""
     *
     * GPathResult characterList = new XmlSlurper().parseText(text)
     *
     * assert characterList.character[1..2].join(',') == 'Gromit,Shaun'
     * </pre>
     * @param range a Range indicating the items to get
     * @return a new list based on range borders
     */
    public Object getAt(final IntRange range) {
        return DefaultGroovyMethods.getAt(list(), range);
    }

    /**
     * A helper method to allow GPathResults to work with subscript operators
     * @param index an index
     * @param newValue the value to put at the given index
     */
    public void putAt(final int index, final Object newValue) {
        final GPathResult result = (GPathResult)getAt(index);
        if (newValue instanceof Closure) {
            result.replaceNode((Closure)newValue);
        } else {
            result.replaceBody(newValue);
        }
    }

    /**
     * Provides an Iterator over all the nodes of this GPathResult using a depth-first traversal.
     *
     * @return the <code>Iterator</code> of (depth-first) ordered GPathResults
     */
    public Iterator depthFirst() {
        return new Iterator() {
            private final List list = new LinkedList();
            private final Stack stack = new Stack();
            private Iterator iter = iterator();
            private GPathResult next = getNextByDepth();

            public boolean hasNext() {
                return this.next != null;
            }

            public Object next() {
                try {
                    return this.next;
                } finally {
                    this.next = getNextByDepth();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private GPathResult getNextByDepth() {
                while (this.iter.hasNext()) {
                    final GPathResult node = (GPathResult) this.iter.next();
                    this.list.add(node);
                    this.stack.push(this.iter);
                    this.iter = node.children().iterator();
                }

                if (this.list.isEmpty()) {
                    return null;
                } else {
                    GPathResult result = (GPathResult) this.list.get(0);
                    this.list.remove(0);
                    this.iter = (Iterator) this.stack.pop();
                    return result;
                }
            }
        };
    }

    /**
     * Provides an Iterator over all the nodes of this GPathResult using a breadth-first traversal.
     *
     * @return the <code>Iterator</code> of (breadth-first) ordered GPathResults
     */
    public Iterator breadthFirst() {
        return new Iterator() {
            private final List list = new LinkedList();
            private Iterator iter = iterator();
            private GPathResult next = getNextByBreadth();

            public boolean hasNext() {
                return this.next != null;
            }

            public Object next() {
                try {
                    return this.next;
                } finally {
                    this.next = getNextByBreadth();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private GPathResult getNextByBreadth() {
                List children = new ArrayList();
                while (this.iter.hasNext() || !children.isEmpty()) {
                    if (this.iter.hasNext()) {
                        final GPathResult node = (GPathResult) this.iter.next();
                        this.list.add(node);
                        this.list.add(this.iter);
                        children.add(node.children());
                    } else {
                        List nextLevel = new ArrayList();
                        for (Object child : children) {
                            GPathResult next = (GPathResult) child;
                            for (Object o : next) {
                                nextLevel.add(o);
                            }
                        }
                        this.iter = nextLevel.iterator();
                        children = new ArrayList();
                    }
                }
                if (this.list.isEmpty()) {
                    return null;
                } else {
                    GPathResult result = (GPathResult) this.list.get(0);
                    this.list.remove(0);
                    this.iter = (Iterator) this.list.get(0);
                    this.list.remove(0);
                    return result;
                }
            }
        };
    }

    /**
     * Creates a list of objects representing this GPathResult.
     *
     * @return a list representing of this GPathResult
     */
    public List list() {
        final Iterator iter = nodeIterator();
        final List result = new LinkedList();
        while (iter.hasNext()) {
            result.add(new NodeChild((Node) iter.next(), this.parent, this.namespacePrefix, this.namespaceTagHints));
        }
        return result;
    }

    /**
     * Returns true if the GPathResult is empty, i.e. if, and only if, <code>size()</code> is 0.
     *
     * @return true if the GPathResult is empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Creates a Closure representing the body of this GPathResult.
     *
     * @return the body of this GPathResult, converted to a <code>Closure</code>
     */
    public Closure getBody() {
        return new Closure(this.parent,this) {
            public void doCall(Object[] args) {
                final GroovyObject delegate = (GroovyObject)getDelegate();
                final GPathResult thisObject = (GPathResult)getThisObject();

                Node node = (Node)thisObject.getAt(0);
                List children = node.children();

                for (Object child : children) {
                    delegate.getProperty("mkp");
                    if (child instanceof Node) {
                        delegate.invokeMethod("yield", new Object[]{new NodeChild((Node) child, thisObject, "*", null)});
                    } else {
                        delegate.invokeMethod("yield", new Object[]{child});
                    }
                }                
            }
        };
    }

    /**
     * Returns the size of this GPathResult.
     * @return the size of this GPathResult
     */
    public abstract int size();

    /**
     * Returns the text of this GPathResult as a <code>String</code>.
     * @return the text of this GPathResult
     */
    public abstract String text();

    /**
     * Returns the parents of this GPathResult as a <code>GPathResult</code>.
     * Warning: The subclasses of this package do not implement this method yet.
     * @return the parents of this GPathResult
     */
    public abstract GPathResult parents();

    /**
     * Returns an iterator over the child nodes of this GPathResult.
     * @return an iterator over the child nodes of this GPathResult
     */
    public abstract Iterator childNodes();

    public abstract Iterator iterator();

    /**
     * Returns the first child of this GPathResult matching the condition(s)
     * specified in the passed closure.
     * @param closure a closure to filters the children of this GPathResult
     * @return the first child matching the closure
     */
    public abstract GPathResult find(Closure closure);

    /**
     * Returns the children of this GPathResult matching the condition(s)
     * specified in the passed closure.
     * @param closure a closure to filters the children of this GPathResult
     * @return the children matching the closure
     */
    public abstract GPathResult findAll(Closure closure);

    public abstract Iterator nodeIterator();

    protected Iterator createIterator(final Object obj) {
        return new Iterator() {
            private boolean hasNext = true;

            public boolean hasNext() {
                return this.hasNext;
            }

            public Object next() {
                try {
                    return (this.hasNext) ? obj : null;
                } finally {
                    this.hasNext = false;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
