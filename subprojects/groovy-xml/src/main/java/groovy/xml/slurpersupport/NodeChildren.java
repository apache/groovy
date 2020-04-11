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
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * Lazy evaluated representation of child nodes.
 */
public class NodeChildren extends GPathResult {
    private int size = -1;

    /**
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param name if the GPathResult corresponds to something with a name, e.g. a node
     * @param namespacePrefix the namespace prefix if any
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public NodeChildren(final GPathResult parent, final String name, final String namespacePrefix, final Map<String, String> namespaceTagHints) {
        super(parent, name, namespacePrefix, namespaceTagHints);
    }

    /**
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param name if the GPathResult corresponds to something with a name, e.g. a node
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public NodeChildren(final GPathResult parent, final String name, final Map<String, String> namespaceTagHints) {
        this(parent, name, "*", namespaceTagHints);
    }

    /**
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public NodeChildren(final GPathResult parent, final Map<String, String> namespaceTagHints) {
        this(parent, "*", namespaceTagHints);
    }

    public Iterator childNodes() {
        return new Iterator() {
            private final Iterator iter = nodeIterator();
            private Iterator childIter = nextChildIter();

            public boolean hasNext() {
                return childIter != null;
            }

            public Object next() {
                while (childIter != null) {
                    try {
                        if (childIter.hasNext()) {
                            return childIter.next();
                        }
                    } finally {
                        if (!childIter.hasNext()) {
                            childIter = nextChildIter();
                        }
                    }
                }
                return null;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private Iterator nextChildIter() {
                while (iter.hasNext()) {
                    final Node node = (Node)iter.next();
                    final Iterator result = node.childNodes();
                    if (result.hasNext()) return result;
                }
                return null;
            }
        };
    }

    public Iterator iterator() {
        return new Iterator() {
            final Iterator iter = nodeIterator();

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Object next() {
                return new NodeChild((Node) iter.next(), pop(), namespaceTagHints);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Iterator nodeIterator() {
        if ("*".equals(name)) {
            return parent.childNodes();
        } else {
            return new NodeIterator(parent.childNodes()) {
                protected Object getNextNode(Iterator iter) {
                    while (iter.hasNext()) {
                        final Node node = (Node) iter.next();
                        if (name.equals(node.name())) {
                            if ("*".equals(namespacePrefix) ||
                                    ("".equals(namespacePrefix) && "".equals(node.namespaceURI())) ||
                                    node.namespaceURI().equals(namespaceMap.get(namespacePrefix))) {
                                return node;
                            }
                        }
                    }
                    return null;
                }
            };
        }
    }

    /**
     * Throws a <code>GroovyRuntimeException</code>, because it is not implemented yet.
     */
    public GPathResult parents() {
        // TODO Auto-generated method stub
        throw new GroovyRuntimeException("parents() not implemented yet");
    }

    public synchronized int size() {
        if (this.size == -1) {
            final Iterator iter = iterator();
            this.size = 0;
            while (iter.hasNext()) {
                iter.next();
                this.size++;
            }
        }
        return this.size;
    }

    public String text() {
        final StringBuilder buf = new StringBuilder();
        final Iterator iter = nodeIterator();
        while (iter.hasNext()) {
            buf.append(((Node) iter.next()).text());
        }
        return buf.toString();
    }

    public GPathResult find(final Closure closure) {
        for (Object node : this) {
            if (DefaultTypeTransformation.castToBoolean(closure.call(new Object[]{node}))) {
                return (GPathResult) node;
            }
        }
        return new NoChildren(this, this.name, namespaceTagHints);
    }

    public GPathResult findAll(final Closure closure) {
        return new FilteredNodeChildren(this, closure, namespaceTagHints);
    }

    public void build(final GroovyObject builder) {
        final Iterator iter = nodeIterator();
        while (iter.hasNext()) {
            final Object next = iter.next();
            if (next instanceof Buildable) {
                ((Buildable) next).build(builder);
            } else {
                ((Node) next).build(builder, namespaceMap, namespaceTagHints);
            }
        }
    }

    /* (non-Javadoc)
    * @see groovy.lang.Writable#writeTo(java.io.Writer)
    */
    public Writer writeTo(final Writer out) throws IOException {
        final Iterator iter = nodeIterator();
        while (iter.hasNext()) {
            ((Node) iter.next()).writeTo(out);
        }
        return out;
    }

    protected void replaceNode(final Closure newValue) {
        for (Object o : this) {
            final NodeChild result = (NodeChild) o;
            result.replaceNode(newValue);
        }
    }

    protected void replaceBody(final Object newValue) {
        for (Object o : this) {
            final NodeChild result = (NodeChild) o;
            result.replaceBody(newValue);
        }
    }

    protected void appendNode(final Object newValue) {
        for (Object o : this) {
            final NodeChild result = (NodeChild) o;
            result.appendNode(newValue);
        }
    }
}
