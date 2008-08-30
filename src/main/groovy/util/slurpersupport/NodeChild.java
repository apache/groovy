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

package groovy.util.slurpersupport;

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

public class NodeChild extends GPathResult {
    private final Node node;

    public NodeChild(final Node node, final GPathResult parent, final String namespacePrefix, final Map namespaceTagHints) {
        super(parent, node.name(), namespacePrefix, namespaceTagHints);
        this.node = node;
    }

    public NodeChild(final Node node, final GPathResult parent, final Map namespaceTagHints) {
        this(node, parent, "*", namespaceTagHints);
    }

    public int size() {
        return 1;
    }

    public String text() {
        return this.node.text();
    }

    public String namespaceURI() {
        return this.node.namespaceURI();
    }

    public GPathResult parents() {
        // TODO Auto-generated method stub
        throw new GroovyRuntimeException("parents() not implemented yet");
    }

    public Iterator iterator() {
        return new Iterator() {
            private boolean hasNext = true;

            public boolean hasNext() {
                return this.hasNext;
            }

            public Object next() {
                try {
                    return (this.hasNext) ? NodeChild.this : null;
                } finally {
                    this.hasNext = false;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Iterator nodeIterator() {
        return new Iterator() {
            private boolean hasNext = true;

            public boolean hasNext() {
                return this.hasNext;
            }

            public Object next() {
                try {
                    return (this.hasNext) ? NodeChild.this.node : null;
                } finally {
                    this.hasNext = false;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Object getAt(final int index) {
        if (index == 0) {
            return node;
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public Map attributes() {
        return this.node.attributes();
    }

    public Iterator childNodes() {
        return this.node.childNodes();
    }

    public GPathResult find(final Closure closure) {
        if (DefaultTypeTransformation.castToBoolean(closure.call(new Object[]{this.node}))) {
            return this;
        } else {
            return new NoChildren(this, "", this.namespaceTagHints);
        }
    }

    public GPathResult findAll(final Closure closure) {
        return find(closure);
    }

    public void build(final GroovyObject builder) {
        this.node.build(builder, this.namespaceMap, this.namespaceTagHints);
    }

    public Writer writeTo(final Writer out) throws IOException {
        return this.node.writeTo(out);
    }

    protected void replaceNode(final Closure newValue) {
        this.node.replaceNode(newValue, this);
    }

    protected void replaceBody(final Object newValue) {
        this.node.replaceBody(newValue);
    }

    protected void appendNode(final Object newValue) {
        this.node.appendNode(newValue, this);
    }
}
