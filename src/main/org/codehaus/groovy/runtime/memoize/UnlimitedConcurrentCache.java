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
package org.codehaus.groovy.runtime.memoize;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache backed by a ConcurrentHashMap
 *
 * @author Vaclav Pech
 */
public final class UnlimitedConcurrentCache implements MemoizeCache<Object, Object> {

    private final ConcurrentHashMap<Object, Object> cache = new ConcurrentHashMap<Object, Object>();

    public Object put(final Object key, final Object value) {
        return cache.put(key, value);
    }

    public Object get(final Object key) {
        return cache.get(key);
    }

    /**
     * Replying on the ConcurrentHashMap thread-safe iteration implementation the method will remove all entries holding
     * SoftReferences to gc-evicted objects.
     */
    public void cleanUpNullReferences() {
        final Iterator<Map.Entry<Object, Object>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Object, Object> entry = iterator.next();
            Object entryVal = entry.getValue();
            if (entryVal != null && ((SoftReference) entryVal).get() == null) cache.remove(entry.getKey(), entryVal);
        }
    }
}
