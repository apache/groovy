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
package groovy.xml;

import groovy.namespace.QName;
import groovy.util.BuilderSupport;
import groovy.util.NodeBuilder;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A helper class for creating namespaced GroovyMarkup
 */
public class NamespaceBuilderSupport extends BuilderSupport {
    private boolean autoPrefix;
    private Map<String, String> nsMap = new HashMap<String, String>();
    private BuilderSupport builder;

    /**
     * Creates a namespace-aware wrapper around the supplied builder.
     *
     * @param builder the builder to wrap
     */
    public NamespaceBuilderSupport(BuilderSupport builder) {
        super(builder);
        this.builder = builder;
    }

    /**
     * Creates a namespace-aware wrapper and binds the default prefix to the supplied namespace URI.
     *
     * @param builder the builder to wrap
     * @param uri the namespace URI to bind to the default prefix
     */
    public NamespaceBuilderSupport(BuilderSupport builder, String uri) {
        this(builder, uri, "");
    }

    /**
     * Creates a namespace-aware wrapper and binds the supplied prefix to the supplied namespace URI.
     *
     * @param builder the builder to wrap
     * @param uri the namespace URI to bind
     * @param prefix the prefix to associate with {@code uri}
     */
    public NamespaceBuilderSupport(BuilderSupport builder, String uri, String prefix) {
        this(builder, uri, prefix, true);
    }

    /**
     * Creates a namespace-aware wrapper with optional automatic prefixing for unqualified method names.
     *
     * @param builder the builder to wrap
     * @param uri the namespace URI to bind
     * @param prefix the prefix to associate with {@code uri}
     * @param autoPrefix whether the configured prefix should be applied automatically
     */
    public NamespaceBuilderSupport(BuilderSupport builder, String uri, String prefix, boolean autoPrefix) {
        this(builder);
        nsMap.put(prefix, uri);
        this.autoPrefix = autoPrefix;
    }

    /**
     * Creates a namespace-aware wrapper using the supplied prefix-to-URI mappings.
     *
     * @param builder the builder to wrap
     * @param nsMap the namespace mappings to use
     */
    public NamespaceBuilderSupport(BuilderSupport builder, Map nsMap) {
        this(builder);
        this.nsMap = nsMap;
    }

    /**
     * Associates the default prefix with the supplied namespace URI.
     *
     * @param namespaceURI the namespace URI to bind
     * @return this wrapper
     */
    public NamespaceBuilderSupport namespace(String namespaceURI) {
        nsMap.put("", namespaceURI);
        return this;
    }

    /**
     * Associates the supplied prefix with the supplied namespace URI.
     *
     * @param namespaceURI the namespace URI to bind
     * @param prefix the prefix to associate with {@code namespaceURI}
     * @return this wrapper
     */
    public NamespaceBuilderSupport namespace(String namespaceURI, String prefix) {
        nsMap.put(prefix, namespaceURI);
        return this;
    }

    /**
     * Replaces the current namespace mappings used to resolve subsequent builder calls.
     *
     * @param nsMap the prefix-to-URI mappings to use
     * @return this wrapper
     */
    public NamespaceBuilderSupport declareNamespace(Map nsMap) {
        this.nsMap = nsMap;
        return this;
    }

    /**
     * Builder lifecycle callback that exposes the wrapped builder's current node.
     * Subclasses may override to customize how builder state is shared.
     *
     * @return the wrapped builder's current node marker
     */
    @Override
    protected Object getCurrent() {
        // TODO a better way to do this?
        if (builder instanceof NodeBuilder)
            return InvokerHelper.invokeMethod(builder, "getCurrent", null);
        else
            return super.getCurrent();
    }

    /**
     * Builder lifecycle callback that updates the wrapped builder's current node marker.
     * Subclasses may override to customize how builder state is shared.
     *
     * @param current the new current node marker
     */
    @Override
    protected void setCurrent(Object current) {
        // TODO a better way to do this?
        if (builder instanceof NodeBuilder)
            InvokerHelper.invokeMethod(builder, "setCurrent", current);
        else
            super.setCurrent(current);
    }

    /**
     * Builder lifecycle callback invoked after a child node has been created.
     * This implementation is a no-op because parent handling is delegated to the wrapped builder.
     *
     * @param parent the parent node marker
     * @param child the child node marker
     */
    @Override
    protected void setParent(Object parent, Object child) {
    }

    /**
     * Resolves a builder method name into a namespace-aware node name.
     * Subclasses may override to customize method-to-{@link QName} resolution.
     *
     * @param methodName the method name invoked on the wrapper
     * @return a {@link QName} when a namespace mapping is available, otherwise the original method name
     */
    @Override
    protected Object getName(String methodName) {
        String prefix = autoPrefix ? nsMap.keySet().iterator().next() : "";
        String localPart = methodName;
        int idx = methodName.indexOf(':');
        if (idx > 0 ) {
            prefix = methodName.substring(0, idx);
            localPart = methodName.substring(idx + 1);
        }
        String namespaceURI = nsMap.get(prefix);
        if (namespaceURI == null) {
        	return methodName;
        }
        return new QName(namespaceURI, localPart, prefix);
    }

    /**
     * Allow automatic detection of namespace declared in the attributes
     *
     * @param methodName the builder method being invoked
     * @param args the builder arguments, including any attribute map
     * @return the result from the wrapped builder invocation
     */
    @Override
    public Object invokeMethod(String methodName, Object args) {
        // detect namespace declared on the added node like xmlns:foo="http:/foo"
    	Map attributes = findAttributes(args);
    	for (Iterator<Map.Entry> iter = attributes.entrySet().iterator(); iter.hasNext();) {
    		Map.Entry entry = iter.next();
    		String key = String.valueOf(entry.getKey());
            if (key.startsWith("xmlns:")) {
                String prefix = key.substring(6);
                String uri = String.valueOf(entry.getValue());
                namespace(uri, prefix);
                iter.remove();
            }
        }

        return super.invokeMethod(methodName, args);
    }

    private static Map findAttributes(Object args) {
        List list = InvokerHelper.asList(args);
        for (Object o : list) {
        	if (o instanceof Map) {
        		return (Map) o;
        	}
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * Builder lifecycle callback that keeps the resolved node name for the wrapped builder.
     *
     * @param name the resolved node name
     * @return the same node name
     */
    @Override
    protected Object createNode(Object name) {
        return name;
    }

    /**
     * Builder lifecycle callback that keeps the resolved node name for the wrapped builder.
     *
     * @param name the resolved node name
     * @param value the node value supplied by the builder call
     * @return the same node name
     */
    @Override
    protected Object createNode(Object name, Object value) {
        return name;
    }

    /**
     * Builder lifecycle callback that keeps the resolved node name for the wrapped builder.
     *
     * @param name the resolved node name
     * @param attributes the attributes supplied by the builder call
     * @return the same node name
     */
    @Override
    protected Object createNode(Object name, Map attributes) {
        return name;
    }

    /**
     * Builder lifecycle callback that keeps the resolved node name for the wrapped builder.
     *
     * @param name the resolved node name
     * @param attributes the attributes supplied by the builder call
     * @param value the node value supplied by the builder call
     * @return the same node name
     */
    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        return name;
    }
}
