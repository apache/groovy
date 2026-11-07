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
package org.apache.groovy.ginq.provider;

import groovy.lang.Closure;
import groovy.transform.Internal;
import org.apache.groovy.ginq.provider.collection.runtime.Queryable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Loads {@link GinqAggregate} services for the compiler. The generated query
 * instantiates the implementation itself, so a query does not look the service
 * up again at runtime.
 *
 * @since 7.0.0
 */
@Internal
public final class GinqAggregates {

    private static final Map<ClassLoader, Map<String, Class<? extends GinqAggregate>>> CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private GinqAggregates() {
    }

    /**
     * Returns the aggregate implementation registered under {@code name}, or {@code null}.
     * The loader and its parents are searched. The first registration of a name wins.
     *
     * @param name the aggregate name used in a query
     * @param loader the compilation class loader, or {@code null} to use this class's loader
     * @return the implementation class, or {@code null} when the name is not registered
     */
    public static Class<? extends GinqAggregate> find(String name, ClassLoader loader) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        ClassLoader resolved = loader == null ? GinqAggregates.class.getClassLoader() : loader;
        return aggregates(resolved).get(name);
    }

    /**
     * Creates the aggregate named by {@code type} and applies it to {@code group}.
     * Called from queries lowered by the GINQ compiler.
     *
     * @param type the implementation class recorded at compile time
     * @param group the current group
     * @param selector a closure or function of one element, or {@code null}
     * @return the aggregate value
     * @throws IllegalStateException when {@code type} cannot be constructed
     */
    public static Object apply(Class<? extends GinqAggregate> type, Queryable<?> group, Object selector) {
        try {
            GinqAggregate aggregate = type.getDeclaredConstructor().newInstance();
            return aggregate.apply(group, asFunction(selector));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create GINQ aggregate " + type.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Function<Object, Object> asFunction(Object selector) {
        if (selector == null) {
            return null;
        }
        if (selector instanceof Closure<?> closure) {
            return value -> closure.call(value);
        }
        if (selector instanceof Function<?, ?> function) {
            return (Function<Object, Object>) function;
        }
        throw new IllegalArgumentException("GINQ aggregate selector must be a closure or a function");
    }

    private static Map<String, Class<? extends GinqAggregate>> aggregates(ClassLoader loader) {
        Map<String, Class<? extends GinqAggregate>> cached = CACHE.get(loader);
        if (cached != null) {
            return cached;
        }
        Map<String, Class<? extends GinqAggregate>> loaded = load(loader);
        CACHE.put(loader, loaded);
        return loaded;
    }

    private static Map<String, Class<? extends GinqAggregate>> load(ClassLoader loader) {
        Map<String, Class<? extends GinqAggregate>> found = new LinkedHashMap<>();
        for (ClassLoader current = loader; current != null; current = current.getParent()) {
            try {
                for (GinqAggregate aggregate : ServiceLoader.load(GinqAggregate.class, current)) {
                    String name = aggregate.name();
                    if (name != null && !name.isEmpty()) {
                        found.putIfAbsent(name, aggregate.getClass());
                    }
                }
            } catch (ServiceConfigurationError ignored) {
                // A provider listed for this loader is not loadable yet. Keep looking.
            }
        }
        return found;
    }
}
