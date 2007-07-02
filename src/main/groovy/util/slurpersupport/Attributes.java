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
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * @author John Wilson
 */

class Attributes extends NodeChildren {
    final String attributeName;

    public Attributes(final GPathResult parent, final String name, final String namespacePrefix, final Map namespaceTagHints) {
        super(parent, name, namespacePrefix, namespaceTagHints);
        this.attributeName = this.name.substring(1);
    }

    public Attributes(final GPathResult parent, final String name, final Map namespaceTagHints) {
        this(parent, name, "*", namespaceTagHints);
    }

    public String name() {
        // this name contains @name we need to return name
        return this.name.substring(1);
    }

    public Iterator childNodes() {
        throw new GroovyRuntimeException("Can't get the child nodes on a a GPath expression selecting attributes: ...." + this.parent.name() + "." + name() + ".childNodes()");
    }

    public Iterator iterator() {
        return new NodeIterator(nodeIterator()) {
            protected Object getNextNode(final Iterator iter) {
                while (iter.hasNext()) {
                    final Object next = iter.next();
                    if (next instanceof Attribute) {
                        return next;
                    } else {
                        final String value = (String) ((Node) next).attributes().get(Attributes.this.attributeName);
                        if (value != null) {
                            return new Attribute(Attributes.this.attributeName,
                                    value,
                                    new NodeChild((Node) next, Attributes.this.parent.parent, "", Attributes.this.namespaceTagHints),
                                    "",
                                    Attributes.this.namespaceTagHints);
                        }
                    }
                }
                return null;
            }
        };
    }

    public Iterator nodeIterator() {
        return this.parent.nodeIterator();
    }

    public GPathResult parents() {
        return super.parents();
    }

    public String text() {
        final StringBuffer buf = new StringBuffer();
        final Iterator iter = iterator();
        while (iter.hasNext()) {
            buf.append(iter.next());
        }
        return buf.toString();
    }

    public List list() {
        final Iterator iter = iterator();
        final List result = new ArrayList();
        while (iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }

    public GPathResult findAll(final Closure closure) {
        return new FilteredAttributes(this, closure, this.namespaceTagHints);
    }

    public Writer writeTo(final Writer out) throws IOException {
        out.write(text());
        return out;
    }

    public void build(final GroovyObject builder) {
        builder.getProperty("mkp");
        builder.invokeMethod("yield", new Object[]{text()});
    }
}
