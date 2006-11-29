/*
 * Copyright 2005 John G. Wilson
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
 *
 */

package groovy.util.slurpersupport;

import groovy.lang.Buildable;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * @author John Wilson
 */

class NodeChildren extends GPathResult {
    private int size = -1;

    /**
     * @param parent
     * @param name
     * @param namespacePrefix
     * @param namespaceTagHints
     */
    public NodeChildren(final GPathResult parent, final String name, final String namespacePrefix, final Map namespaceTagHints) {
        super(parent, name, namespacePrefix, namespaceTagHints);
    }

    /**
     * @param parent
     * @param name
     * @param namespaceTagHints
     */
    public NodeChildren(final GPathResult parent, final String name, final Map namespaceTagHints) {
        this(parent, name, "*", namespaceTagHints);
    }

    /**
     * @param parent
     * @param namespaceTagHints
     */
    public NodeChildren(final GPathResult parent, final Map namespaceTagHints) {
        this(parent, "*", namespaceTagHints);
    }

    public Iterator childNodes() {
        return new Iterator() {
            private final Iterator iter = NodeChildren.this.parent.childNodes();
            private Iterator childIter = nextChildIter();

            /* (non-Javadoc)
            * @see java.util.Iterator#hasNext()
            */
            public boolean hasNext() {
                return this.childIter != null;
            }

            /* (non-Javadoc)
            * @see java.util.Iterator#next()
            */
            public Object next() {
                while (this.childIter != null) {
                    try {
                        if (this.childIter.hasNext()) {
                            return this.childIter.next();
                        }
                    } finally {
                        if (!this.childIter.hasNext()) {
                            this.childIter = nextChildIter();
                        }
                    }
                }

                return null;
            }

            /* (non-Javadoc)
            * @see java.util.Iterator#remove()
            */
            public void remove() {
                throw new UnsupportedOperationException();
            }

            private Iterator nextChildIter() {
                while (this.iter.hasNext()) {
                    final Node node = (Node) this.iter.next();

                    if (NodeChildren.this.name.equals(node.name())) {
                        final Iterator result = node.childNodes();

                        if (result.hasNext()) {
                            if ("*".equals(NodeChildren.this.namespacePrefix) ||
                                    ("".equals(NodeChildren.this.namespacePrefix) && "".equals(node.namespaceURI())) ||
                                    node.namespaceURI().equals(NodeChildren.this.namespaceMap.get(NodeChildren.this.namespacePrefix))) {
                                return result;
                            }
                        }
                    }
                }

                return null;
            }
        };
    }

    public Iterator iterator() {
        return new Iterator() {
        final Iterator iter = nodeIterator();

            public boolean hasNext() {
                return this.iter.hasNext();
            }

            public Object next() {
                return new NodeChild((Node) this.iter.next(), NodeChildren.this.parent, NodeChildren.this.namespaceTagHints);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Iterator nodeIterator() {
        if ("*".equals(this.name)) {
            return this.parent.childNodes();
        } else {
            return new NodeIterator(this.parent.childNodes()) {
                /* (non-Javadoc)
                * @see org.codehaus.groovy.sandbox.util.slurpersupport.NodeIterator#getNextNode(java.util.Iterator)
                */
                protected Object getNextNode(Iterator iter) {
                    while (iter.hasNext()) {
                        final Node node = (Node) iter.next();

                        if (NodeChildren.this.name.equals(node.name())) {
                            if ("*".equals(NodeChildren.this.namespacePrefix) ||
                                    ("".equals(NodeChildren.this.namespacePrefix) && "".equals(node.namespaceURI())) ||
                                    node.namespaceURI().equals(NodeChildren.this.namespaceMap.get(NodeChildren.this.namespacePrefix))) {
                                return node;
                            }
                        }
                    }

                    return null;
                }
            };
        }
    }

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
    final StringBuffer buf = new StringBuffer();
    final Iterator iter = nodeIterator();

        while (iter.hasNext()) {
            buf.append(((Node) iter.next()).text());
        }

        return buf.toString();
    }

    public GPathResult find(final Closure closure) {
    final Iterator iter = iterator();

        while (iter.hasNext()) {
            final Object node = iter.next();

            if (DefaultTypeTransformation.castToBoolean(closure.call(new Object[]{node}))) {
                return (GPathResult) node;
            }
        }

        return new NoChildren(this, this.name, this.namespaceTagHints);
    }

    public GPathResult findAll(final Closure closure) {
        return new FilteredNodeChildren(this, closure, this.namespaceTagHints);
    }

    public void build(final GroovyObject builder) {
        final Iterator iter = nodeIterator();

        while (iter.hasNext()) {
            final Object next = iter.next();

            if (next instanceof Buildable) {
                ((Buildable) next).build(builder);
            } else {
                ((Node) next).build(builder, this.namespaceMap, this.namespaceTagHints);
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
    final Iterator iter = iterator();

        while (iter.hasNext()) {
        final NodeChild result = (NodeChild)iter.next();
            result.replaceNode(newValue);
        }
    }

    protected void replaceBody(final Object newValue) {
    final Iterator iter = iterator();

        while (iter.hasNext()) {
        final NodeChild result = (NodeChild)iter.next();
            result.replaceBody(newValue);
        }
    }
}
