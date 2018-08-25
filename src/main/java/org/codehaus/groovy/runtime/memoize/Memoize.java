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

import groovy.lang.Closure;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;

/**
 * Implements memoize for Closures.
 * It is supposed to be used by the Closure class itself to implement the memoize() family of methods.
 */
public abstract class Memoize {

    /**
     * A place-holder for null values in cache
     */
    private static final MemoizeNullValue MEMOIZE_NULL = new MemoizeNullValue();

    /**
     * Creates a new closure delegating to the supplied one and memoizing all return values by the arguments.
     *
     * The supplied cache is used to store the memoized values and it is the cache's responsibility to put limits
     * on the cache size or implement cache eviction strategy.
     * The LRUCache, for example, allows to set the maximum cache size constraint and implements
     * the LRU (Last Recently Used) eviction strategy.
     *
     * @param cache A map to hold memoized return values
     * @param closure The closure to memoize
     * @param <V> The closure's return type
     * @return A new memoized closure
     */
    public static <V> Closure<V> buildMemoizeFunction(final MemoizeCache<Object, Object> cache, final Closure<V> closure) {
        return new MemoizeFunction<>(cache, closure);
    }

    /**
     * Creates a new closure delegating to the supplied one and memoizing all return values by the arguments.
     * The memoizing closure will use SoftReferences to remember the return values allowing the garbage collector
     * to reclaim the memory, if needed.
     *
     * The supplied cache is used to store the memoized values and it is the cache's responsibility to put limits
     * on the cache size or implement cache eviction strategy.
     * The LRUCache, for example, allows to set the maximum cache size constraint and implements
     * the LRU (Last Recently Used) eviction strategy.
     *
     * If the protectedCacheSize argument is greater than 0 an optional LRU (Last Recently Used) cache of hard references
     * is maintained to protect recently touched memoized values against eviction by the garbage collector.
     *
     * @param protectedCacheSize The number of hard references to keep in order to prevent some (LRU) memoized return values from eviction
     * @param cache A map to hold memoized return values
     * @param closure The closure to memoize
     * @param <V> The closure's return type
     * @return A new memoized closure
     */
    public static <V> Closure<V> buildSoftReferenceMemoizeFunction(final int protectedCacheSize, final MemoizeCache<Object, SoftReference<Object>> cache, final Closure<V> closure) {
        final ProtectionStorage lruProtectionStorage = protectedCacheSize > 0 ?
                new LRUProtectionStorage(protectedCacheSize) :
                new NullProtectionStorage(); // Nothing should be done when no elements need protection against eviction

        final ReferenceQueue queue = new ReferenceQueue();

        return new SoftReferenceMemoizeFunction<>(cache, closure, lruProtectionStorage, queue);
    }

    /**
     * Creates a key to use in the memoize cache
     *
     * @param args The arguments supplied to the closure invocation
     *
     * @return The key - a list holding all arguments
     */
    private static Object generateKey(final Object[] args) {
        if (args == null) return Collections.emptyList();
        Object[] copyOfArgs = copyOf(args, args.length);
        return asList(copyOfArgs);
    }

    /**
     * A place-holder for cached null values
     */
    private static class MemoizeNullValue {

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof MemoizeNullValue;
        }

        @Override
        public int hashCode() {
            return "MemoizeNullValue".hashCode();
        }
    }

    private static class MemoizeFunction<V> extends Closure<V> {
        private static final long serialVersionUID = -2780003153676993093L;
        final MemoizeCache<Object, Object> cache;
        final Closure<V> closure;
        
        MemoizeFunction(final MemoizeCache<Object, ?> cache, Closure<V> closure) {
            super(closure.getOwner());
            this.cache = coerce(cache);
            this.closure = closure;
            parameterTypes = closure.getParameterTypes();
            maximumNumberOfParameters = closure.getMaximumNumberOfParameters();
        }

        private static MemoizeCache coerce(MemoizeCache<Object, ?> cache) {
            return cache;
        }
        
        @Override
        public V call(final Object... args) {
            final Object key = generateKey(args);
            Object result = cache.getAndPut(key, k -> {
                Object r = closure.call(args);
                //noinspection GroovyConditionalCanBeElvis
                return r != null ? r : MEMOIZE_NULL;
            });

            return result == MEMOIZE_NULL ? null : (V) result;
        }

        public V doCall(final Object... args) {
            return call(args);
        }
    }
    
    private static class SoftReferenceMemoizeFunction<V> extends MemoizeFunction<V> {
        private static final long serialVersionUID = -1338206227167457991L;
        final ProtectionStorage lruProtectionStorage;
        final ReferenceQueue queue;
        
        SoftReferenceMemoizeFunction(final MemoizeCache<Object, SoftReference<Object>> cache, Closure<V> closure,
                ProtectionStorage lruProtectionStorage, ReferenceQueue queue) {
            super(cache, closure);
            this.lruProtectionStorage = lruProtectionStorage;
            this.queue = queue;
        }

        @Override
        public V call(final Object... args) {
            if (queue.poll() != null) cleanUpNullReferences(cache, queue);  // if something has been evicted, do a clean-up
            final Object key = generateKey(args);

            SoftReference reference = (SoftReference) cache.getAndPut(key, k -> {
                Object r = closure.call(args);

                return null != r ? new SoftReference<Object>(r, queue) : new SoftReference<Object>(MEMOIZE_NULL);
            });

            Object result = reference.get();
            lruProtectionStorage.touch(key, result);

            return result == MEMOIZE_NULL ? null : (V) result;
        }

        /**
         * After the garbage collector has done its job, we need to clean the cache from references to all the evicted memoized values.
         * @param cache The cache to prune
         * @param queue A reference queue holding references to gc-evicted memoized values
         */
        private static void cleanUpNullReferences(final MemoizeCache<Object, Object> cache, final ReferenceQueue queue) {
            while(queue.poll() != null) {}  //empty the reference queue
            cache.cleanUpNullReferences();
        }
    }
}
