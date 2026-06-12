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
package org.codehaus.groovy.tools.groovydoc.testfiles;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JavaImplementsMapInheritDoc implements Map<String, Object> {
    private final Map<String, Object> delegate = new LinkedHashMap<String, Object>();

    /** {@inheritDoc} */
    @Override
    public int size() {
        return delegate.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        delegate.putAll(m);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        delegate.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Object> values() {
        return delegate.values();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
