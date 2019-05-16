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

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * Lazy evaluated representation of a node attribute.
 */
public class Attribute extends GPathResult {
    private final String value;

    /**
     * @param name              of the attribute
     * @param value             of the attribute
     * @param parent            the GPathResult prior to the application of the expression creating this GPathResult
     * @param namespacePrefix   the namespace prefix if any
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public Attribute(final String name, final String value, final GPathResult parent, final String namespacePrefix, final Map<String, String> namespaceTagHints) {
        super(parent, name, namespacePrefix, namespaceTagHints);
        this.value = value;
    }

    public String name() {
        // this name contains @name we need to return name
        return this.name.substring(1);
    }

    /**
     * Returns the size of this Attribute, which is always <code>1</code>.
     * @return <code>1</code>
     */
    public int size() {
        return 1;
    }

    /**
     * Returns the value of this Attribute.
     * @return the value of this Attribute
     */
    public String text() {
        return this.value;
    }

    /**
     * Returns the URI of the namespace of this Attribute.
     * @return the namespace of this Attribute
     */
    public String namespaceURI() {
        if (namespacePrefix == null || namespacePrefix.isEmpty()) return "";
        String uri = namespaceTagHints.get(namespacePrefix);
        return uri == null ? "" : uri;
    }

    /**
     * Throws a <code>GroovyRuntimeException</code>, because this method is not implemented yet.
     */
    public GPathResult parents() {
        // TODO Auto-generated method stub
        throw new GroovyRuntimeException("parents() not implemented yet");
    }

    /**
     * Throws a <code>GroovyRuntimeException</code>, because an attribute can have no children.
     */
    public Iterator childNodes() {
        throw new GroovyRuntimeException("can't call childNodes() in the attribute " + this.name);
    }

    public Iterator iterator() {
        return nodeIterator();
    }

    public GPathResult find(final Closure closure) {
        if (DefaultTypeTransformation.castToBoolean(closure.call(new Object[]{this}))) {
            return this;
        } else {
            return new NoChildren(this, "", this.namespaceTagHints);
        }
    }

    public GPathResult findAll(final Closure closure) {
        return find(closure);
    }

    public Iterator nodeIterator() {
        return createIterator(this);
    }

    public Writer writeTo(final Writer out) throws IOException {
        out.write(this.value);
        return out;
    }

    public void build(final GroovyObject builder) {
        builder.getProperty("mkp");
        builder.invokeMethod("yield", new Object[]{this.value});
    }

    /**
     * NOP, because an attribute does not have any Node to replace.
     */
    protected void replaceNode(final Closure newValue) {
    }

    /**
     * NOP, because an attribute does not have a Body.
     */
    protected void replaceBody(final Object newValue) {
    }

    /**
     * NOP, because an node can not be appended to an attribute.
     */
    protected void appendNode(final Object newValue) {
    }
}
