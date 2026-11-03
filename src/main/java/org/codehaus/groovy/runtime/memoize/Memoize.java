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

import java.io.ObjectStreamException;
import java.io.Serial;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.function.Supplier;

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
     * A copy made by {@code dehydrate} or {@code rehydrate} calls the dehydrated or rehydrated closure and shares
     * the supplied cache.
     *
     * @param cache A map to hold memoized return values
     * @param closure The closure to memoize
     * @param <V> The closure's return type
     * @return A new memoized closure
     */
    public static <V> Closure<V> buildMemoizeFunction(final MemoizeCache<Object, Object> cache, final Closure<V> closure) {
        return new MemoizeFunction<V>(cache, null, closure);
    }

    /**
     * Creates a new closure delegating to the supplied one and memoizing all return values by the arguments.
     *
     * The supplied factory creates the cache that stores the memoized values, and it is the cache's responsibility
     * to put limits on the cache size or implement cache eviction strategy.
     * A copy made by {@code dehydrate} or {@code rehydrate} calls the dehydrated or rehydrated closure and starts
     * with an empty cache created by the same factory, as the cached values were computed against the old owner,
     * delegate and this object. The factory has to be serializable for the memoized closure to be serializable.
     *
     * @param cacheFactory Creates the map to hold memoized return values
     * @param closure The closure to memoize
     * @param <V> The closure's return type
     * @return A new memoized closure
     */
    public static <V> Closure<V> buildMemoizeFunction(final Supplier<? extends MemoizeCache<Object, Object>> cacheFactory, final Closure<V> closure) {
        return new MemoizeFunction<V>(cacheFactory.get(), cacheFactory, closure);
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
     * A copy made by {@code dehydrate} or {@code rehydrate} calls the dehydrated or rehydrated closure and shares
     * the supplied cache.
     *
     * @param protectedCacheSize The number of hard references to keep in order to prevent some (LRU) memoized return values from eviction
     * @param cache A map to hold memoized return values
     * @param closure The closure to memoize
     * @param <V> The closure's return type
     * @return A new memoized closure
     */
    public static <V> Closure<V> buildSoftReferenceMemoizeFunction(final int protectedCacheSize, final MemoizeCache<Object, SoftReference<Object>> cache, final Closure<V> closure) {
        return new SoftReferenceMemoizeFunction<V>(cache, null, closure, protectedCacheSize);
    }

    /**
     * Creates a new closure delegating to the supplied one and memoizing all return values by the arguments.
     * The memoizing closure will use SoftReferences to remember the return values allowing the garbage collector
     * to reclaim the memory, if needed.
     *
     * The supplied factory creates the cache that stores the memoized values, and it is the cache's responsibility
     * to put limits on the cache size or implement cache eviction strategy.
     * A copy made by {@code dehydrate} or {@code rehydrate} calls the dehydrated or rehydrated closure and starts
     * with an empty cache created by the same factory and the same protected cache size, as the cached values were
     * computed against the old owner, delegate and this object.
     *
     * If the protectedCacheSize argument is greater than 0 an optional LRU (Last Recently Used) cache of hard references
     * is maintained to protect recently touched memoized values against eviction by the garbage collector.
     *
     * @param protectedCacheSize The number of hard references to keep in order to prevent some (LRU) memoized return values from eviction
     * @param cacheFactory Creates the map to hold memoized return values
     * @param closure The closure to memoize
     * @param <V> The closure's return type
     * @return A new memoized closure
     */
    public static <V> Closure<V> buildSoftReferenceMemoizeFunction(final int protectedCacheSize, final Supplier<? extends MemoizeCache<Object, SoftReference<Object>>> cacheFactory, final Closure<V> closure) {
        return new SoftReferenceMemoizeFunction<V>(cacheFactory.get(), cacheFactory, closure, protectedCacheSize);
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
        @Serial
        private static final long serialVersionUID = -2780003153676993093L;
        final MemoizeCache<Object, Object> cache;
        /**
         * Creates the cache of a dehydrated or rehydrated copy; {@code null} when the memoized closure was
         * built from a cache instance, in which case the copy shares that cache.
         */
        final Supplier<? extends MemoizeCache<Object, ?>> cacheFactory;
        Closure<V> closure;

        MemoizeFunction(final MemoizeCache<Object, ?> cache, final Supplier<? extends MemoizeCache<Object, ?>> cacheFactory, Closure<V> closure) {
            super(closure.getOwner(), closure.getThisObject());
            this.cache = coerce(cache);
            this.cacheFactory = cacheFactory;
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

        @Override
        public Object getDelegate() {
            return closure.getDelegate();
        }

        @Override
        public void setDelegate(final Object delegate) {
            closure.setDelegate(delegate);
        }

        @Override
        public int getResolveStrategy() {
            return closure.getResolveStrategy();
        }

        @Override
        public void setResolveStrategy(final int resolveStrategy) {
            closure.setResolveStrategy(resolveStrategy);
        }

        /**
         * Clones the memoized closure too, so that setting the delegate or resolve strategy of the
         * copy leaves this function alone. The copy shares the cache.
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object clone() {
            MemoizeFunction<V> result = (MemoizeFunction<V>) super.clone();
            result.closure = (Closure<V>) closure.clone();
            return result;
        }

        /**
         * Dehydrates the memoized closure and memoizes the copy, since calls go through the memoized closure.
         * The copy starts with an empty cache of the same kind and limits.
         */
        @Override
        public Closure<V> dehydrate() {
            return memoizeCopy(closure.dehydrate());
        }

        /**
         * Rehydrates the memoized closure and memoizes the copy, since calls go through the memoized closure.
         * The copy starts with an empty cache of the same kind and limits, as the cached values were computed
         * against the old owner, delegate and this object.
         */
        @Override
        public Closure<V> rehydrate(final Object delegate, final Object owner, final Object thisObject) {
            return memoizeCopy(closure.rehydrate(delegate, owner, thisObject));
        }

        Closure<V> memoizeCopy(final Closure<V> copy) {
            return new MemoizeFunction<V>(newCache(), cacheFactory, copy);
        }

        MemoizeCache<Object, ?> newCache() {
            return cacheFactory != null ? cacheFactory.get() : cache;
        }

        /**
         * Exposes the memoized closure, which {@code call} dispatches to but which is held outside the
         * {@code owner}/{@code delegate}/{@code thisObject} references the cycle check walks by default.
         */
        @Override
        protected Object[] additionalReferences() {
            return new Object[]{closure};
        }

        /**
         * Rejects a deserialized closure whose references form a cycle, which would otherwise recurse
         * indefinitely on invocation. See {@link Closure#checkForReferenceCycle}.
         */
        @Serial
        private Object readResolve() throws ObjectStreamException {
            Closure.checkForReferenceCycle(this);
            return this;
        }
    }

    private static class SoftReferenceMemoizeFunction<V> extends MemoizeFunction<V> {
        @Serial
        private static final long serialVersionUID = -1338206227167457991L;
        final ProtectionStorage lruProtectionStorage;
        final ReferenceQueue queue;
        final int protectedCacheSize;

        SoftReferenceMemoizeFunction(final MemoizeCache<Object, ?> cache, final Supplier<? extends MemoizeCache<Object, ?>> cacheFactory,
                Closure<V> closure, final int protectedCacheSize) {
            this(cache, cacheFactory, closure, protectedCacheSize,
                    protectedCacheSize > 0 ?
                            new LRUProtectionStorage(protectedCacheSize) :
                            new NullProtectionStorage(), // Nothing should be done when no elements need protection against eviction
                    new ReferenceQueue());
        }

        private SoftReferenceMemoizeFunction(final MemoizeCache<Object, ?> cache, final Supplier<? extends MemoizeCache<Object, ?>> cacheFactory,
                Closure<V> closure, final int protectedCacheSize, ProtectionStorage lruProtectionStorage, ReferenceQueue queue) {
            super(cache, cacheFactory, closure);
            this.protectedCacheSize = protectedCacheSize;
            this.lruProtectionStorage = lruProtectionStorage;
            this.queue = queue;
        }

        /**
         * Memoizes the copy with an empty cache, protection storage and reference queue, or, when the memoized
         * closure was built from a cache instance, with the same ones.
         */
        @Override
        Closure<V> memoizeCopy(final Closure<V> copy) {
            return cacheFactory != null
                    ? new SoftReferenceMemoizeFunction<V>(newCache(), cacheFactory, copy, protectedCacheSize)
                    : new SoftReferenceMemoizeFunction<V>(cache, null, copy, protectedCacheSize, lruProtectionStorage, queue);
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

        /**
         * Declared again rather than inherited: {@code readResolve} is private, so it is not inherited
         * and a subclass that does not declare its own is deserialized unchecked.
         * See {@link Closure#checkForReferenceCycle}.
         */
        @Serial
        private Object readResolve() throws ObjectStreamException {
            Closure.checkForReferenceCycle(this);
            return this;
        }
    }
}
