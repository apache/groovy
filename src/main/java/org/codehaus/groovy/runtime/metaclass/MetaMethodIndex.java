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

import groovy.lang.MetaMethod;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.util.FastArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaMethodIndex {
    private static final int DEFAULT_CAPACITY = 32;

    /**
     * a map of the starter class plus its super classes to save method lists for
     * static/normal/super method calls. It also provides a simple cache of one
     * method name and call signature to method per static/normal/super call.
     */
    public final Map<Class<?>, Map<String, Cache>> indexMap = new ConcurrentHashMap<>(DEFAULT_CAPACITY);

    public final Class<?> mainClass;

    public static class MetaMethodCache {
        public final Class<?>[] params;
        public final MetaMethod method;

        /**
         * create a new method entry
         * @param params in case of caching params might not be the same as {@link MetaMethod#getParameterTypes}
         * @param method the meta method
         */
        public MetaMethodCache(final Class<?>[] params, final MetaMethod method) {
            this.params = params;
            this.method = method;
        }
    }

    public static class Cache {
        public final String name;
        public Object methods;
        public Object methodsForSuper;
        public Object staticMethods;
        public MetaMethodCache cachedMethod;
        public MetaMethodCache cachedMethodForSuper;
        public MetaMethodCache cachedStaticMethod;

        public Cache(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "[name=" + name + "]";
        }
    }

    //--------------------------------------------------------------------------

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

    public final Cache getMethods(final Class<?> cls, final String name) {
        var map = indexMap.get(cls);
        return map == null ? null : map.get(name);
    }

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

    public Map<String, Cache> getHeader(final Class<?> cls) {
        return indexMap.get(cls);
    }

    public  void copyNonPrivateMethods(final Map<String, Cache> from, final Map<String, Cache> to) {
        for (Cache e : from.values()) {
            copyNonPrivateMethods(e, to);
        }
    }

    private void copyNonPrivateMethods(final Cache from, final Map<String, Cache> to) {
        var fastArrayOrMetaMethod = from.methods;
        if (fastArrayOrMetaMethod instanceof FastArray) {
            FastArray fastArray = (FastArray) fastArrayOrMetaMethod;
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

    public  void copyNonPrivateNonNewMetaMethods(final Map<String, Cache> from, final Map<String, Cache> to) {
        for (Cache e : from.values()) {
            copyNonPrivateNonNewMetaMethods(e, to);
        }
    }

    private void copyNonPrivateNonNewMetaMethods(final Cache from, final Map<String, Cache> to) {
        var fastArrayOrMetaMethod = from.methods;
        if (fastArrayOrMetaMethod instanceof FastArray) {
            FastArray fastArray = (FastArray) fastArrayOrMetaMethod;
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

    public Object addMethodToList(final Object o, final MetaMethod toIndex) {
        if (o == null) {
            return toIndex;
        }

        if (o instanceof MetaMethod) {
            final MetaMethod inIndex = (MetaMethod) o;
            if (!isMatchingMethod(inIndex, toIndex)) {
                return new FastArray(new Object[]{inIndex, toIndex});
            }
            return !isOverridden(inIndex, toIndex) ? inIndex : toIndex;
        }

        if (o instanceof FastArray) {
            final FastArray array = (FastArray) o;
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
        // do not overwrite private methods
        if (inIndex.isPrivate()) return false;

        CachedClass inIndexDC = inIndex.getDeclaringClass();
        CachedClass toIndexDC = toIndex.getDeclaringClass();
        if (inIndexDC == toIndexDC) {
            return isNonRealMethod(toIndex) || inIndex.isSynthetic(); // GROOVY-10136, GROOVY-10594
        }

        // interface vs instance method; be careful...
        if (!inIndex.isStatic() && !toIndex.isStatic() // GROOVY-9815
                && inIndexDC.isInterface() != toIndexDC.isInterface()
                && !(toIndex instanceof ClosureMetaMethod || toIndex instanceof ClosureStaticMetaMethod)) { // GROOVY-3493
            // this is the old logic created for GROOVY-2391 and GROOVY-7879, which was labeled as "do not overwrite interface methods with instance methods"
            return (isNonRealMethod(inIndex) || !inIndexDC.isInterface() || toIndexDC.isInterface()) && !toIndexDC.isAssignableFrom(inIndexDC.getTheClass());
        }

        // prefer most-specific or most-recent for type disjunction
        return inIndexDC.isAssignableFrom(toIndexDC.getTheClass());
    }

    private static boolean isNonRealMethod(final MetaMethod method) {
        return method instanceof NewMetaMethod
            || method instanceof ClosureMetaMethod
            || method instanceof GeneratedMetaMethod
            || method instanceof ClosureStaticMetaMethod
            || method instanceof MixinInstanceMetaMethod
            || method instanceof ClosureMetaMethod.AnonymousMetaMethod;
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

    public void clearCaches() {
        allEntries().forEach(e ->
            e.cachedMethod = e.cachedMethodForSuper = e.cachedStaticMethod = null
        );
    }

    public void clearCaches(String name) {
        allEntries().filter(cache -> cache.name.equals(name)).forEach(e ->
            e.cachedMethod = e.cachedMethodForSuper = e.cachedStaticMethod = null
        );
    }
}
