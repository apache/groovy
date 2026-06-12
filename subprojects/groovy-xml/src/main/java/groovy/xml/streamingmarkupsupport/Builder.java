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
package groovy.xml.streamingmarkupsupport;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base support for streaming markup builders that dispatch tags through namespace-specific closures.
 */
public abstract class Builder extends GroovyObjectSupport {
    /**
     * Normalized namespace metadata used to resolve tag handlers during binding.
     */
    protected final Map namespaceMethodMap = new HashMap();

    /**
     * Creates a builder from the precomputed namespace method metadata.
     *
     * @param namespaceMethodMap namespace-specific tag handler metadata
     */
    public Builder(final Map namespaceMethodMap) {
        for (Object e : namespaceMethodMap.entrySet()) {
            Map.Entry entry = (Map.Entry) e;
            final Object key = entry.getKey();
            final List value = (List) entry.getValue();
            final Closure dg = ((Closure) value.get(1)).asWritable();

            this.namespaceMethodMap.put(key, new Object[] { value.get(0), dg, fettleMethodMap(dg, (Map) value.get(2)) });
        }
    }

    private static Map fettleMethodMap(final Closure defaultGenerator, final Map methodMap) {
    final Map newMethodMap = new HashMap();

        for (Object o : methodMap.keySet()) {
            final Object key = o;
            final Object value = methodMap.get(key);

            if ((value instanceof Closure)) {
                newMethodMap.put(key, value);
            } else {
                newMethodMap.put(key, defaultGenerator.curry((Object[]) value));
            }
        }

        return newMethodMap;
    }

    /**
     * Binds a root markup closure into a lazily executable document object.
     *
     * @param root root markup closure
     * @return bound document representation
     */
    public abstract Object bind(Closure root);

    /**
     * Base class for bound markup documents produced by {@link #bind(Closure)}.
     */
    protected abstract static class Built extends GroovyObjectSupport {
        /**
         * Root markup closure cloned for this bound document.
         */
        protected final Closure root;
        /**
         * Namespace URI to tag handler metadata for this bound document.
         */
        protected final Map namespaceSpecificTags = new HashMap();

        /**
         * Creates a bound document with namespace-specific tag metadata.
         *
         * @param root root markup closure
         * @param namespaceTagMap namespace URI to tag handler metadata
         */
        public Built(final Closure root, final Map namespaceTagMap) {
            this.namespaceSpecificTags.putAll(namespaceTagMap);

            this.root = (Closure)root.clone();

            this.root.setDelegate(this);
        }
    }
}
