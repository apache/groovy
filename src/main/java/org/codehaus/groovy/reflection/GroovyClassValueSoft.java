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
import java.util.concurrent.atomic.AtomicReference;

/**
 * GROOVY-12281 investigation prototype ({@code -Dgroovy.use.classvalue=soft}):
 * keeps {@code java.lang.ClassValue}'s per-{@code Class} fast path but stores
 * each value behind a mutable <em>slot</em> holding either a
 * {@link SoftReference} to the value (the reclaimable state) or the value
 * itself ({@linkplain #pin pinned}), so an association on an immortal
 * platform class (for example {@code String}) no longer holds a strong chain
 * to the value's class loader unless the value was deliberately pinned
 * (JDK-8136353 / GROOVY-12142). The slot is a bootstrap-loaded
 * {@link AtomicReference} and the wrapper a bootstrap-loaded
 * {@code java.lang.ref.SoftReference}; the per-Class entry references the
 * defining {@code ClassValue} only weakly (via its {@code Version}; the map
 * key is a bootstrap {@code Identity} object). So the only path from an
 * immortal key to the Groovy-loaded value is softly reachable — cleared under
 * memory pressure once nothing else keeps the Groovy island alive.
 * <p>
 * Correctness relies on <em>resurrection</em>: a weak-key/weak-value side map
 * is the identity authority. {@code computeValue} consults it first, so when
 * the slot's soft reference has been cleared but the value is still reachable
 * anywhere — for example captured by a linked call site — the same instance
 * is re-associated rather than a fresh one created. A fresh instance can only
 * be created once the old one is weakly unreachable, at which point no guard,
 * cache or call site can still observe the old instance, so "two live
 * generations of one association" cannot arise.
 * <p>
 * {@linkplain #pin Pinning} flips a slot to hold its value strongly, giving
 * the value exactly a plain {@code ClassValue} association's lifetime: as
 * long as its key class, and no longer. Because the strong hold lives inside
 * the association (an ephemeron), a pinned value on a collectible key — for
 * example an EMC-carrying script class — is released together with its class
 * and loader; a global strong root would instead extend it to the runtime's
 * lifetime (the "reverse" leak raised in review of PR #2820).
 * <p>
 * {@link #remove(Class)} stays a <em>hard detach</em> (the identity entry and
 * any pin go too), preserving the documented undeploy semantics of
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

    /**
     * Per-Class slot: contains either a {@code SoftReference<T>} (reclaimable)
     * or the value itself (pinned). Both the slot and the wrapper are
     * bootstrap-loaded, so an unpinned association on an immortal key keeps
     * nothing Groovy-loaded strongly reachable.
     */
    private final ClassValue<AtomicReference<Object>> store = new ClassValue<AtomicReference<Object>>() {
        @Override
        protected AtomicReference<Object> computeValue(final Class<?> type) {
            return new AtomicReference<>(new SoftReference<>(canonical(type)));
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
        T value = deref(store.get(type).get());
        if (value != null) return value;
        // The soft reference was cleared: drop the memoized slot and
        // recompute once — resurrection returns the canonical instance when
        // it is still alive (GROOVY-12280 remove-and-recompute pattern).
        store.remove(type);
        value = deref(store.get(type).get());
        if (value != null) return value;
        // Pathological memory pressure cleared the fresh wrapper before we
        // could dereference it: serve the canonical instance uncached; a
        // later get() re-tries the cache. Bounded, and identity-safe because
        // the side map, not the ClassValue, is the identity authority.
        return canonical(type);
    }

    /** The value a slot's content designates: through the soft wrapper, or the pinned value itself. */
    @SuppressWarnings("unchecked")
    private T deref(final Object slotContent) {
        return slotContent instanceof SoftReference ? ((SoftReference<T>) slotContent).get() : (T) slotContent;
    }

    @Override
    public void remove(final Class<?> type) {
        canonical.remove(type);
        store.remove(type);
    }

    @Override
    public boolean valuesReclaimable() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The retry handles the race with {@link #get}'s remove-and-recompute: if
     * the slot we wrote was concurrently discarded (its soft reference had
     * been cleared), the write is repeated on the successor slot — whose
     * recompute returned the same canonical instance, because {@code value}
     * is strongly reachable in our hands throughout.
     */
    @Override
    public void pin(final Class<?> type, final T value) {
        while (true) {
            AtomicReference<Object> slot = store.get(type);
            slot.set(value);
            if (store.get(type) == slot) return; // still current: the strong hold is visible
        }
    }

    @Override
    public void unpin(final Class<?> type, final T value) {
        // CAS: only downgrade the exact pinned value; a slot already holding a
        // soft wrapper (or a successor value) is left untouched.
        store.get(type).compareAndSet(value, new SoftReference<>(value));
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
