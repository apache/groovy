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
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Lazy evaluated representation of a child node.
 */
public class NodeChild extends GPathResult {
    private final Node node;

    /**
     * @param node a node
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param namespacePrefix the namespace prefix if any
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public NodeChild(final Node node, final GPathResult parent, final String namespacePrefix, final Map<String, String> namespaceTagHints) {
        super(parent, node.name(), namespacePrefix, namespaceTagHints);
        this.node = node;
        ((NamespaceAwareHashMap)this.node.attributes()).setNamespaceTagHints(namespaceTagHints);
    }

    /**
     * @param node a node
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public NodeChild(final Node node, final GPathResult parent, final Map<String, String> namespaceTagHints) {
        this(node, parent, "*", namespaceTagHints);
    }
    
    public GPathResult parent() {
        if (node.parent() != null)
            return new NodeChild(node.parent(), this, namespaceTagHints);
        else
            return this;
    }

    public int size() {
        return 1;
    }

    public String text() {
        return this.node.text();
    }

    /**
     * Returns the list of any direct String nodes of this NodeChild.
     *
     * @return the list of String values from this node
     * @since 2.3.0
     */
    public List<String> localText() {
        return this.node.localText();
    }

    /**
     * Returns the URI of the namespace of this NodeChild.
     * @return the namespace of this NodeChild
     */
    public String namespaceURI() {
        return this.node.namespaceURI();
    }

    /**
     * Throws a <code>GroovyRuntimeException</code>, because this method is not implemented yet.
     */
    public GPathResult parents() {
        // TODO Auto-generated method stub
        throw new GroovyRuntimeException("parents() not implemented yet");
    }

    public Iterator iterator() {
        return createIterator(this);
    }

    public Iterator nodeIterator() {
        return createIterator(this.node);
    }

    public Object getAt(final int index) {
        if (index == 0) {
            return node;
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    /**
     * Returns a map containing all attributes of the Node of this NodeChild.
     * @return a map containing all attributes
     */
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
