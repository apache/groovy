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

import groovy.namespace.QName;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute map that resolves namespace-prefixed keys into QName-style names on demand.
 */
public class NamespaceAwareHashMap extends HashMap<String, String> {
    /**
     * Sets the prefix-to-namespace hints used to normalize prefixed keys.
     *
     * @param namespaceTagHints known namespace prefix mappings
     */
    public void setNamespaceTagHints(Map namespaceTagHints) {
        this.namespaceTagHints = namespaceTagHints;
    }

    private Map namespaceTagHints = null;

    /**
     * Returns the prefix hints used for key normalization.
     *
     * @return namespace prefix hints
     */
    public Map getNamespaceTagHints() {
        return namespaceTagHints;
    }

    /** {@inheritDoc} */
    @Override
    public String get(Object key) {
        key = adjustForNamespaceIfNeeded(key);
        return super.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public String remove(Object key) {
        key = adjustForNamespaceIfNeeded(key).toString();
        return super.remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(Object key) {
        key = adjustForNamespaceIfNeeded(key).toString();
        return super.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public String put(String key, String value) {
        key = adjustForNamespaceIfNeeded(key).toString();
        return super.put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Object o : m.entrySet())
            if (o instanceof Map.Entry) {
                Map.Entry<String, String> e = (Map.Entry) o;
                put(e.getKey(), e.getValue());
            }
    }

    private Object adjustForNamespaceIfNeeded(Object key) {
        String keyString = key.toString();
        if (keyString.contains("{") || namespaceTagHints == null || namespaceTagHints.isEmpty() || !keyString.contains(":")) {
            return key;
        }
        final int i = keyString.indexOf(':');
        return new QName(namespaceTagHints.get(keyString.substring(0, i)).toString(), keyString.substring(i + 1)).toString();
    }
}
