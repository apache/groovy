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
package org.codehaus.groovy.reflection;

import org.apache.groovy.util.concurrent.ConcurrentReferenceHashMap;
import org.apache.groovy.util.concurrent.ConcurrentReferenceHashMap.Option;
import org.apache.groovy.util.concurrent.ConcurrentReferenceHashMap.ReferenceType;

import java.lang.ref.SoftReference;
import java.util.EnumSet;

/**
 * GROOVY-12281 investigation prototype ({@code -Dgroovy.use.classvalue=soft}):
 * keeps {@code java.lang.ClassValue}'s per-{@code Class} fast path but stores
 * each value behind a {@link SoftReference}, so an association on an immortal
 * platform class (for example {@code String}) no longer holds a strong chain
 * to the value's class loader (JDK-8136353 / GROOVY-12142). The stored wrapper
 * is a bootstrap-loaded {@code java.lang.ref.SoftReference}, and the per-Class
 * entry references the defining {@code ClassValue} only weakly (via its
 * {@code Version}; the map key is a bootstrap {@code Identity} object), so the
 * only path from an immortal key to the Groovy-loaded value is softly
 * reachable and is cleared under memory pressure once nothing else keeps the
 * Groovy island alive.
 * <p>
 * Correctness relies on <em>resurrection</em>: a weak-key/weak-value side map
 * is the identity authority. {@code computeValue} consults it first, so when
 * the {@code ClassValue}'s soft reference has been cleared but the value is
 * still reachable anywhere — for example captured by a linked call site — the
 * same instance is re-associated rather than a fresh one created. A fresh
 * instance can only be created once the old one is weakly unreachable, at
 * which point no guard, cache or call site can still observe the old
 * instance, so "two live generations of one association" cannot arise.
 * <p>
 * {@link #remove(Class)} stays a <em>hard detach</em> (the identity entry is
 * purged too), preserving the documented undeploy semantics of
 * {@link ClassInfo#remove(Class)}: the next {@code get} creates a fresh value
 * even if the old one is still reachable somewhere.
 *
 * @param <T> the value type
 */
class GroovyClassValueSoft<T> implements GroovyClassValue<T> {

    private final ComputeValue<T> computeValue;

    /**
     * Identity authority: weak identity keys (no key class is pinned by this
     * map) and weak values (the map keeps nothing alive by itself; it only
     * remembers a value for as long as something else does).
     */
    private final ConcurrentReferenceHashMap<Class<?>, T> canonical =
            new ConcurrentReferenceHashMap<>(ReferenceType.WEAK, ReferenceType.WEAK,
                    EnumSet.of(Option.IDENTITY_COMPARISONS));

    /**
     * Striped locks for the canonical-miss path. Creation must be mutually
     * exclusive per key: without it two racing threads could each create and
     * leak a distinct instance through the uncached fallback in
     * {@link #get(Class)}, breaking the identity guarantee the side map
     * exists to provide. (The map's own {@code putIfAbsent} cannot be used
     * for this: on an entry whose collected value has not yet been purged it
     * reports success without storing the replacement.)
     */
    private final Object[] creationLocks = new Object[64];

    private final ClassValue<SoftReference<T>> store = new ClassValue<SoftReference<T>>() {
        @Override
        protected SoftReference<T> computeValue(final Class<?> type) {
            return new SoftReference<>(canonical(type));
        }
    };

    GroovyClassValueSoft(final ComputeValue<T> computeValue) {
        this.computeValue = computeValue;
        for (int i = 0; i < creationLocks.length; i++) {
            creationLocks[i] = new Object();
        }
    }

    @Override
    public T get(final Class<?> type) {
        T value = store.get(type).get();
        if (value != null) return value;
        // The soft reference was cleared: drop the memoized wrapper and
        // recompute once — resurrection returns the canonical instance when
        // it is still alive (GROOVY-12280 remove-and-recompute pattern).
        store.remove(type);
        value = store.get(type).get();
        if (value != null) return value;
        // Pathological memory pressure cleared the fresh wrapper before we
        // could dereference it: serve the canonical instance uncached; a
        // later get() re-tries the cache. Bounded, and identity-safe because
        // the side map, not the ClassValue, is the identity authority.
        return canonical(type);
    }

    @Override
    public void remove(final Class<?> type) {
        canonical.remove(type);
        store.remove(type);
    }

    /**
     * Returns the current canonical value without creating one — used by
     * detach paths that must act on the existing instance (for example
     * un-rooting it) but must not resurrect or create anything as a side
     * effect.
     *
     * @param type the key class
     * @return the live canonical value, or {@code null} if none
     */
    T getIfPresent(final Class<?> type) {
        return canonical.get(type);
    }

    /**
     * Returns the canonical value for {@code type}: the still-live existing
     * instance when there is one, otherwise a freshly computed instance
     * published under the key's creation lock.
     */
    private T canonical(final Class<?> type) {
        T existing = canonical.get(type);
        if (existing != null) return existing;
        Object lock = creationLocks[System.identityHashCode(type) & (creationLocks.length - 1)];
        synchronized (lock) {
            existing = canonical.get(type);
            if (existing != null) return existing;
            T fresh = computeValue.computeValue(type);
            // Unconditional put: replaces a stale (value-collected) entry.
            canonical.put(type, fresh);
            return fresh;
        }
    }
}
