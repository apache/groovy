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

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.namespace.QName;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Lazy evaluated representation of a node's attributes.
 */
public class Attributes extends NodeChildren {
    final String attributeName;

    /**
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param name if the GPathResult corresponds to something with a name, e.g. a node
     * @param namespacePrefix the namespace prefix if any
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public Attributes(final GPathResult parent, final String name, final String namespacePrefix, final Map<String, String> namespaceTagHints) {
        super(parent, name, namespacePrefix, namespaceTagHints);
        this.attributeName = this.name.substring(1);
    }

    /**
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param name if the GPathResult corresponds to something with a name, e.g. a node
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public Attributes(final GPathResult parent, final String name, final Map<String, String> namespaceTagHints) {
        this(parent, name, "*", namespaceTagHints);
    }

    public String name() {
        // this name contains @name we need to return name
        return this.name.substring(1);
    }

    /**
     * Throws a <code>GroovyRuntimeException</code>, because attributes can have no children.
     */
    public Iterator childNodes() {
        throw new GroovyRuntimeException("Can't get the child nodes on a GPath expression selecting attributes: ...." + this.parent.name() + "." + name() + ".childNodes()");
    }

    public Iterator iterator() {
        return new NodeIterator(nodeIterator()) {
            protected Object getNextNode(final Iterator iter) {
                while (iter.hasNext()) {
                    final Object next = iter.next();
                    if (next instanceof Attribute) {
                        return next;
                    } else {
                        String attributeKey = Attributes.this.attributeName;
                        if (Attributes.this.namespacePrefix != null &&
                                !"*".equals(Attributes.this.namespacePrefix) &&
                                Attributes.this.namespacePrefix.length() > 0) {
                            attributeKey = new QName(Attributes.this.lookupNamespace(Attributes.this.namespacePrefix), Attributes.this.attributeName).toString();
                        }
                        final String value = (String) ((Node) next).attributes().get(attributeKey);
                        if (value != null) {
                            return new Attribute(Attributes.this.name,
                                    value,
                                    new NodeChild((Node) next, Attributes.this.parent.parent, "", Attributes.this.namespaceTagHints),
                                    (Attributes.this.namespacePrefix == null || "*".equals(Attributes.this.namespacePrefix)) ? "" : Attributes.this.namespacePrefix,
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
        final StringBuilder sb = new StringBuilder();
        final Iterator iter = iterator();
        while (iter.hasNext()) {
            sb.append(iter.next());
        }
        return sb.toString();
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
