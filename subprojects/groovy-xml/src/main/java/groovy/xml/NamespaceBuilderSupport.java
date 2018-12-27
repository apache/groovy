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

    public NamespaceBuilderSupport(BuilderSupport builder) {
        super(builder);
        this.builder = builder;
    }

    public NamespaceBuilderSupport(BuilderSupport builder, String uri) {
        this(builder, uri, "");
    }

    public NamespaceBuilderSupport(BuilderSupport builder, String uri, String prefix) {
        this(builder, uri, prefix, true);
    }

    public NamespaceBuilderSupport(BuilderSupport builder, String uri, String prefix, boolean autoPrefix) {
        this(builder);
        nsMap.put(prefix, uri);
        this.autoPrefix = autoPrefix;
    }

    public NamespaceBuilderSupport(BuilderSupport builder, Map nsMap) {
        this(builder);
        this.nsMap = nsMap;
    }

    public NamespaceBuilderSupport namespace(String namespaceURI) {
        nsMap.put("", namespaceURI);
        return this;
    }

    public NamespaceBuilderSupport namespace(String namespaceURI, String prefix) {
        nsMap.put(prefix, namespaceURI);
        return this;
    }

    public NamespaceBuilderSupport declareNamespace(Map nsMap) {
        this.nsMap = nsMap;
        return this;
    }

    @Override
    protected Object getCurrent() {
        // TODO a better way to do this?
        if (builder instanceof NodeBuilder)
            return InvokerHelper.invokeMethod(builder, "getCurrent", null);
        else
            return super.getCurrent();
    }

    @Override
    protected void setCurrent(Object current) {
        // TODO a better way to do this?
        if (builder instanceof NodeBuilder)
            InvokerHelper.invokeMethod(builder, "setCurrent", current);
        else
            super.setCurrent(current);
    }

    @Override
    protected void setParent(Object parent, Object child) {
    }

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

    @Override
    protected Object createNode(Object name) {
        return name;
    }

    @Override
    protected Object createNode(Object name, Object value) {
        return name;
    }

    @Override
    protected Object createNode(Object name, Map attributes) {
        return name;
    }

    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        return name;
    }
}
