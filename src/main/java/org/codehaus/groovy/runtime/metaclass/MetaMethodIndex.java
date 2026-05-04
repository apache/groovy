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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.ClosureInvokingMethod;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.util.FastArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An index of metamethods for a class, organized by method name and signature.
 * Provides efficient lookup of methods for static, normal, and super method calls.
 * Uses caching to optimize method lookup performance.
 * <p>
 * This class is for internal use by the Groovy runtime's metaclass system.
 */
public class MetaMethodIndex {
    private static final int DEFAULT_CAPACITY = 32;

    /**
     * a map of the starter class plus its super classes to save method lists for
     * static/normal/super method calls. It also provides a simple cache of one
     * method name and call signature to method per static/normal/super call.
     */
    public final Map<Class<?>, Map<String, Cache>> indexMap = new ConcurrentHashMap<>(DEFAULT_CAPACITY);

    /**
     * The main class for which this index was created
     */
    public final Class<?> mainClass;

    /**
     * A cache entry for a metamethod with its parameter types.
     */
    public static class MetaMethodCache {
        /**
         * The parameter types used for this cached method
         */
        public final Class<?>[] params;
        /**
         * The cached metamethod
         */
        public final MetaMethod method;

        /**
         * Creates a new method entry.
         *
         * @param params in case of caching params might not be the same as {@link MetaMethod#getParameterTypes}
         * @param method the meta method
         */
        public MetaMethodCache(final Class<?>[] params, final MetaMethod method) {
            this.params = params;
            this.method = method;
        }
    }

    /**
     * A cache of metamethods indexed by name.
     */
    public static class Cache {
        /**
         * The method name
         */
        public final String name;
        /**
         * A list of methods or a single method
         */
        public Object methods;
        /**
         * Methods available for super calls
         */
        public Object methodsForSuper;
        /**
         * Static methods for this name
         */
        public Object staticMethods;
        /**
         * Cached method result for normal calls
         */
        public MetaMethodCache cachedMethod;
        /**
         * Cached method result for super calls
         */
        public MetaMethodCache cachedMethodForSuper;
        /**
         * Cached static method result
         */
        public MetaMethodCache cachedStaticMethod;

        /**
         * Constructs a new Cache for the given method name.
         *
         * @param name the method name
         */
        public Cache(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "[name=" + name + "]";
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Constructs a new MetaMethodIndex for the given class.
     *
     * @param theCachedClass the cached class for which to build the index
     */
    public MetaMethodIndex(final CachedClass theCachedClass) {
        this.mainClass = theCachedClass.getTheClass();
        if (!theCachedClass.isInterface()) {
            for (CachedClass c = theCachedClass; c != null; c = c.getCachedSuperClass()) {
                indexMap.put(c.getTheClass(), new ConcurrentHashMap<>());
            }
        } else {
            indexMap.put(Object.class, new ConcurrentHashMap<>());
            indexMap.put(mainClass, new ConcurrentHashMap<>());
        }
    }

    /**
     * Gets the cached methods for a given class and method name.
     *
     * @param cls the class to look up
     * @param name the method name
     * @return the cache entry or null if not found
     */
    public final Cache getMethods(final Class<?> cls, final String name) {
        var map = indexMap.get(cls);
        return map == null ? null : map.get(name);
    }

    /**
     * Adds a metamethod to the index.
     *
     * @param method the metamethod to add
     * @param map the cache map to add the method to
     */
    public void addMetaMethod(MetaMethod method, Map<String, Cache> map) {
        var cache = map.computeIfAbsent(method.getName(), Cache::new);

        if (method.isStatic()) {
            cache.staticMethods = addMethodToList(cache.staticMethods, method);
        }
        cache.methods = addMethodToList(cache.methods, method);
    }

    private Cache getOrPutMethods(final String name, final Map<String, Cache> cacheIndex) {
        return cacheIndex.computeIfAbsent(name, Cache::new);
    }

    /**
     * Gets the method cache header for the given class.
     *
     * @param cls the class
     * @return the method cache map or null if not found
     */
    public Map<String, Cache> getHeader(final Class<?> cls) {
        return indexMap.get(cls);
    }

    /**
     * Copies all non-private methods from one method cache map to another.
     *
     * @param from the source method cache map
     * @param to the destination method cache map
     */
    public  void copyNonPrivateMethods(final Map<String, Cache> from, final Map<String, Cache> to) {
        for (Cache e : from.values()) {
            copyNonPrivateMethods(e, to);
        }
    }

    private void copyNonPrivateMethods(final Cache from, final Map<String, Cache> to) {
        var fastArrayOrMetaMethod = from.methods;
        if (fastArrayOrMetaMethod instanceof FastArray fastArray) {
            Cache e = null;
            final int n = fastArray.size();
            Object[] array = fastArray.getArray();
            for (int i = 0; i != n; i += 1) {
                MetaMethod method = (MetaMethod) array[i];
                if (isNonPrivate(method)) {
                    if (e == null)
                        e = getOrPutMethods(from.name, to);
                    e.methods = addMethodToList(e.methods, method);
                }
            }
        } else {
            MetaMethod method = (MetaMethod) fastArrayOrMetaMethod;
            if (isNonPrivate(method)) {
                Cache e = getOrPutMethods(from.name, to);
                e.methods = addMethodToList(e.methods, method);
            }
        }
    }

    /**
     * Copies all non-private, non-new metamethods from one method cache map to another.
     *
     * @param from the source method cache map
     * @param to the destination method cache map
     */
    public  void copyNonPrivateNonNewMetaMethods(final Map<String, Cache> from, final Map<String, Cache> to) {
        for (Cache e : from.values()) {
            copyNonPrivateNonNewMetaMethods(e, to);
        }
    }

    private void copyNonPrivateNonNewMetaMethods(final Cache from, final Map<String, Cache> to) {
        var fastArrayOrMetaMethod = from.methods;
        if (fastArrayOrMetaMethod instanceof FastArray fastArray) {
            Cache e = null;
            final int n = fastArray.size();
            Object[] array = fastArray.getArray();
            for (int i = 0; i != n; i += 1) {
                MetaMethod method = (MetaMethod) array[i];
                if (isNonPrivate(method) && !(method instanceof NewMetaMethod)) {
                    if (e == null)
                        e = getOrPutMethods(from.name, to);
                    e.methods = addMethodToList(e.methods, method);
                }
            }
        } else if (fastArrayOrMetaMethod != null) {
            MetaMethod method = (MetaMethod) fastArrayOrMetaMethod;
            if (isNonPrivate(method) && !(method instanceof NewMetaMethod)) {
                Cache e = getOrPutMethods(from.name, to);
                e.methods = addMethodToList(e.methods, method);
            }
        }
    }

    private boolean isNonPrivate(final MetaMethod method) {
        return !method.isPrivate() && (!method.isPackagePrivate() || // GROOVY-11357
                GeneralUtils.inSamePackage(method.getDeclaringClass().getTheClass(), mainClass));
    }

    /**
     * Adds a metamethod to a method list, handling overrides and duplicates.
     * Returns either a single MetaMethod, a FastArray of methods, or the original object.
     *
     * @param o the existing method list (can be null, a MetaMethod, or a FastArray)
     * @param toIndex the metamethod to add
     * @return the updated method list
     */
    public Object addMethodToList(final Object o, final MetaMethod toIndex) {
        if (o == null) {
            return toIndex;
        }

        if (o instanceof MetaMethod inIndex) {
            if (!isMatchingMethod(inIndex, toIndex)) {
                return new FastArray(new Object[]{inIndex, toIndex});
            }
            return !isOverridden(inIndex, toIndex) ? inIndex : toIndex;
        }

        if (o instanceof FastArray array) {
            int found = findMatchingMethod(array, toIndex);
            if (found == -1) {
                array.add(toIndex);
            } else {
                final MetaMethod inIndex = (MetaMethod) array.get(found);
                if (inIndex != toIndex && isOverridden(inIndex, toIndex)) {
                    array.set(found, toIndex);
                }
            }
        }

        return o;
    }

    /**
     * Note: private methods from parent classes are not handled here, but when
     * doing the multi-method connection step, methods of the parent class will
     * be overwritten with methods of a subclass and in that case private methods
     * should be kept.
     */
    private static boolean isOverridden(final MetaMethod inIndex, final MetaMethod toIndex) {
        // don't overwrite private method
        if (inIndex.isPrivate()) {
            return false;
        }

        CachedClass inIndexDC = inIndex.getDeclaringClass();
        CachedClass toIndexDC = toIndex.getDeclaringClass();
        if (inIndexDC == toIndexDC) {
            return inIndex.isSynthetic() || isNonRealMethod(toIndex); // GROOVY-10136, GROOVY-10594
        }

        // interface vs instance method; be careful...
        if (!inIndex.isStatic() && !toIndex.isStatic() // GROOVY-9815
                && inIndexDC.isInterface() != toIndexDC.isInterface()
                && !(toIndex instanceof ClosureMetaMethod || toIndex instanceof ClosureStaticMetaMethod)) { // GROOVY-3493
            // this is the old logic created for GROOVY-2391 and GROOVY-7879, which was labeled as "do not overwrite interface methods with instance methods"
            return (toIndexDC.isInterface() || !inIndexDC.isInterface() || isNonRealMethod(inIndex)) && !toIndexDC.isAssignableFrom(inIndexDC.getTheClass());
        }

        // prefer most-specific or most-recent for type disjunction
        return inIndexDC.isAssignableFrom(toIndexDC.getTheClass());
    }

    private static boolean isNonRealMethod(final MetaMethod method) {
        return method instanceof NewMetaMethod
            || method instanceof GeneratedMetaMethod
            || method instanceof ClosureInvokingMethod
            || method instanceof MixinInstanceMetaMethod;
    }

    private static boolean isMatchingMethod(final MetaMethod method1, final MetaMethod method2) {
        if (method1 == method2) return true;
        CachedClass[] params1 = method1.getParameterTypes();
        CachedClass[] params2 = method2.getParameterTypes();
        if (params1.length != params2.length) return false;

        for (int i = 0, n = params1.length; i < n; i += 1) {
            if (params1[i] != params2[i]) {
                return false;
            }
        }
        return true;
    }

    private static int findMatchingMethod(final FastArray list, final MetaMethod method) {
        final int n = list.size();
        Object[] data = list.getArray();
        for (int i = 0; i != n; i += 1) {
            MetaMethod aMethod = (MetaMethod) data[i];
            if (isMatchingMethod(aMethod, method)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Copies all methods to their super method counterparts in the cache index.
     */
    public void copyMethodsToSuper() {
        allEntries().forEach(cacheEntry -> {
            if (cacheEntry.methods instanceof FastArray) {
                cacheEntry.methodsForSuper = ((FastArray) cacheEntry.methods).copy();
            } else {
                cacheEntry.methodsForSuper = cacheEntry.methods;
            }
        });
    }

    private java.util.stream.Stream<Cache> allEntries() {
        return indexMap.values().stream().flatMap(map -> map.values().stream());
    }

    /**
     * Clears all cached metamethods across all cache entries.
     */
    public void clearCaches() {
        allEntries().forEach(e ->
            e.cachedMethod = e.cachedMethodForSuper = e.cachedStaticMethod = null
        );
    }

    /**
     * Clears all cached metamethods for methods with the given name.
     *
     * @param name the method name
     */
    public void clearCaches(String name) {
        allEntries().filter(cache -> cache.name.equals(name)).forEach(e ->
            e.cachedMethod = e.cachedMethodForSuper = e.cachedStaticMethod = null
        );
    }
}
