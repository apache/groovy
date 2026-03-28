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
package groovy.concurrent;

import groovy.lang.Closure;
import org.apache.groovy.runtime.async.DefaultAsyncContext;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A lightweight, thread-scoped context map for propagating execution-scoped
 * state across asynchronous boundaries.
 *
 * <h2>Purpose</h2>
 * <p>{@code AsyncContext} addresses the common production need for carrying
 * request correlation IDs, tenant identifiers, security principals, trace
 * metadata, and other small pieces of execution-scoped state across the
 * thread hops introduced by {@code async}/{@code await}.  It is the Groovy
 * counterpart of Go's {@code context.Context}, Kotlin's
 * {@code CoroutineContext}, and C#'s {@code AsyncLocal&lt;T&gt;}.
 *
 * <h2>Propagation model</h2>
 * <p>The current context is stored per thread.  The Groovy async runtime
 * captures an immutable {@linkplain Snapshot snapshot} when a task or
 * continuation is scheduled and restores that snapshot when it executes.
 * This <em>copy-on-spawn</em> model provides two key guarantees:
 * <ul>
 *   <li><b>Inheritance</b> — child tasks inherit the parent's context as
 *       it existed at spawn time.</li>
 *   <li><b>Isolation</b> — child-side mutations remain invisible to the
 *       parent and siblings, preventing accidental cross-task leaking.</li>
 * </ul>
 *
 * <h2>Key semantics</h2>
 * <p>Keys are normalized to non-{@code null} {@link String} values.
 * Supplying {@code null} as a value is equivalent to removing the key.
 *
 * <h2>Thread safety</h2>
 * <p>Each thread owns its own {@code AsyncContext} instance, managed by
 * the runtime via {@link org.apache.groovy.runtime.async.ScopedLocal
 * ScopedLocal}.  On JDK&nbsp;25+, this leverages {@code ScopedValue} for
 * optimal virtual-thread performance; on earlier JDKs it falls back to
 * {@code ThreadLocal}.</p>
 *
 * <p>Instance methods ({@link #put}, {@link #get}, {@link #remove}) are
 * <em>not</em> synchronized — they are invoked only on the owning thread.
 * Static methods ({@link #withSnapshot}, {@link #capture}) use a
 * scope-based binding pattern, ensuring the previous context is always
 * reinstated even if the action throws.  This design prevents stale
 * context from leaking to thread-pool threads between task executions.</p>
 *
 * @see Snapshot
 * @see AsyncScope
 * @since 6.0.0
 */
public interface AsyncContext {

    // ---- Instance methods ------------------------------------------------

    /**
     * Looks up a value in this context by key.
     *
     * @param key the context key; must not be {@code null}
     * @return the stored value, or {@code null} if absent
     * @throws NullPointerException if {@code key} is {@code null}
     */
    Object get(String key);

    /**
     * Returns the value for the given key, or the specified default if
     * the key is not present.
     * <p>
     * This method mirrors {@link Map#getOrDefault(Object, Object)} and
     * is useful when a fallback is needed without a separate
     * {@link #containsKey(String)} check.
     *
     * @param key          the context key; must not be {@code null}
     * @param defaultValue the value to return if the key is absent
     * @return the stored value, or {@code defaultValue} if absent
     * @throws NullPointerException if {@code key} is {@code null}
     */
    Object getOrDefault(String key, Object defaultValue);

    /**
     * If the specified key is not already associated with a value (or is
     * mapped to {@code null}), attempts to compute its value using the
     * given mapping function and enters it into this context.
     * <p>
     * This method mirrors {@link Map#computeIfAbsent(Object, Function)}
     * and is useful for one-time, lazy initialization of context entries.
     *
     * @param key             the context key; must not be {@code null}
     * @param mappingFunction the function to compute a value; must not
     *                        be {@code null}
     * @return the current (existing or computed) value associated with
     *         the key, or {@code null} if the computed value is {@code null}
     * @throws NullPointerException if {@code key} or
     *         {@code mappingFunction} is {@code null}
     */
    Object computeIfAbsent(String key, Function<String, Object> mappingFunction);

    /**
     * Groovy subscript-read operator: {@code context['traceId']}.
     *
     * @param key the context key; must not be {@code null}
     * @return the stored value, or {@code null} if absent
     * @throws NullPointerException if {@code key} is {@code null}
     */
    Object getAt(String key);

    /**
     * Stores a value in this context.
     * <p>
     * A {@code null} value removes the key.
     *
     * @param key   the context key; must not be {@code null}
     * @param value the value to store, or {@code null} to remove the key
     * @return the previous value associated with the key, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    Object put(String key, Object value);

    /**
     * Groovy subscript-write operator: {@code context['traceId'] = 'abc-123'}.
     *
     * @param key   the context key; must not be {@code null}
     * @param value the value to store, or {@code null} to remove the key
     * @throws NullPointerException if {@code key} is {@code null}
     */
    void putAt(String key, Object value);

    /**
     * Removes a key from this context.
     *
     * @param key the context key; must not be {@code null}
     * @return the removed value, or {@code null} if absent
     * @throws NullPointerException if {@code key} is {@code null}
     */
    Object remove(String key);

    /**
     * Adds all entries from the supplied map to this context.
     * <p>
     * A {@code null} or empty map is silently ignored.
     * {@code null} values remove the corresponding keys.
     *
     * @param entries the entries to add; may be {@code null}
     */
    void putAll(Map<?, ?> entries);

    /**
     * Removes all entries from this context.
     */
    void clear();

    /**
     * Returns {@code true} if the given key is present in this context.
     *
     * @param key the context key; must not be {@code null}
     * @return {@code true} if the key is present
     * @throws NullPointerException if {@code key} is {@code null}
     */
    boolean containsKey(String key);

    /**
     * Returns the number of entries in this context.
     *
     * @return the entry count
     */
    int size();

    /**
     * Returns {@code true} if this context contains no entries.
     *
     * @return {@code true} if empty
     */
    boolean isEmpty();

    /**
     * Returns an unmodifiable copy of this context's entries as a {@link Map}.
     * <p>
     * The returned map is a point-in-time snapshot: subsequent mutations
     * to this context are not reflected in the returned map.
     *
     * @return an unmodifiable snapshot of the context entries
     */
    Map<String, Object> snapshot();

    // ---- Static factory and utility methods ------------------------------

    /**
     * Returns the live {@code AsyncContext} associated with the current
     * thread.
     * <p>
     * If no context has been explicitly bound, a fresh empty context is
     * created on first access (lazy initialization).
     *
     * @return the current async context; never {@code null}
     */
    static AsyncContext current() {
        return DefaultAsyncContext.current();
    }

    /**
     * Captures an immutable {@link Snapshot} of the current thread's
     * async context.
     * <p>
     * The snapshot is a frozen copy: mutations to the live context after
     * this call do not affect the snapshot.
     *
     * @return the captured snapshot; never {@code null}
     */
    static Snapshot capture() {
        return DefaultAsyncContext.capture();
    }

    /**
     * Executes the supplier with the given snapshot installed as the
     * current async context, restoring the previous context afterwards.
     * <p>
     * This method is the primary mechanism for context propagation across
     * thread boundaries.  The Groovy async runtime uses it internally when
     * resuming continuations and executing scheduled tasks.
     *
     * @param snapshot the snapshot to install; must not be {@code null}
     * @param supplier the action to execute; must not be {@code null}
     * @param <T>      the result type
     * @return the supplier's result
     * @throws NullPointerException if {@code snapshot} or {@code supplier}
     *                              is {@code null}
     */
    static <T> T withSnapshot(Snapshot snapshot, Supplier<T> supplier) {
        return DefaultAsyncContext.withSnapshot(snapshot, supplier);
    }

    /**
     * Executes the runnable with the given snapshot installed as the
     * current async context, restoring the previous context afterwards.
     *
     * @param snapshot the snapshot to install; must not be {@code null}
     * @param action   the action to execute; must not be {@code null}
     * @throws NullPointerException if {@code snapshot} or {@code action}
     *                              is {@code null}
     */
    static void withSnapshot(Snapshot snapshot, Runnable action) {
        DefaultAsyncContext.withSnapshot(snapshot, action);
    }

    /**
     * Temporarily overlays the current async context with the supplied
     * entries for the duration of the closure.
     * <p>
     * Any modifications performed inside the closure are scoped to that
     * dynamic extent and are discarded when the closure returns or throws.
     *
     * @param entries the entries to overlay; may be {@code null}
     * @param action  the closure to execute; must not be {@code null}
     * @param <T>     the result type
     * @return the closure's result
     * @throws NullPointerException if {@code action} is {@code null}
     */
    static <T> T with(Map<?, ?> entries, Closure<T> action) {
        return DefaultAsyncContext.with(entries, action);
    }

    /**
     * Creates a new, empty {@code AsyncContext}.
     *
     * @return a new empty context
     */
    static AsyncContext create() {
        return new DefaultAsyncContext();
    }

    /**
     * Creates a new {@code AsyncContext} seeded with the supplied entries.
     *
     * @param initialValues initial context entries; may be {@code null}
     * @return a new context with the given entries
     */
    static AsyncContext create(Map<?, ?> initialValues) {
        return new DefaultAsyncContext(initialValues);
    }

    // ---- Snapshot --------------------------------------------------------

    /**
     * An immutable, point-in-time snapshot of an {@link AsyncContext},
     * captured at task or continuation registration time and restored when
     * the task executes.
     * <p>
     * Snapshots are the unit of context propagation across async
     * boundaries.  They are lightweight — internally backed by an
     * unmodifiable {@link Map} — and safe to share across threads.
     *
     * <h2>Usage</h2>
     * <pre>{@code
     * AsyncContext.Snapshot snap = AsyncContext.capture();
     * executor.execute(() -> {
     *     AsyncContext.withSnapshot(snap, () -> {
     *         // context restored here
     *     });
     * });
     * }</pre>
     *
     * @since 6.0.0
     */
    interface Snapshot {

        /**
         * Returns {@code true} if this snapshot contains the given key.
         *
         * @param key the context key; must not be {@code null}
         * @return {@code true} if present
         * @throws NullPointerException if {@code key} is {@code null}
         */
        boolean containsKey(String key);

        /**
         * Returns the captured value for the given key.
         *
         * @param key the context key; must not be {@code null}
         * @return the captured value, or {@code null} if absent
         * @throws NullPointerException if {@code key} is {@code null}
         */
        Object get(String key);

        /**
         * Returns the captured value for the given key, or the specified
         * default if the key is not present.
         *
         * @param key          the context key; must not be {@code null}
         * @param defaultValue the value to return if the key is absent
         * @return the captured value, or {@code defaultValue} if absent
         * @throws NullPointerException if {@code key} is {@code null}
         */
        Object getOrDefault(String key, Object defaultValue);

        /**
         * Returns {@code true} if this snapshot contains no entries.
         *
         * @return {@code true} if empty
         */
        boolean isEmpty();

        /**
         * Returns the number of entries in this snapshot.
         *
         * @return the entry count
         */
        int size();

        /**
         * Returns the captured entries as an unmodifiable {@link Map}.
         *
         * @return the captured entries; never {@code null}
         */
        Map<String, Object> asMap();

        /**
         * Creates a new snapshot by overlaying the supplied entries on top
         * of this snapshot.
         * <p>
         * {@code null} values in the overlay remove the corresponding keys.
         * A {@code null} or empty overlay returns {@code this} unchanged.
         *
         * @param entries the entries to merge; may be {@code null}
         * @return the merged snapshot; never {@code null}
         */
        Snapshot with(Map<?, ?> entries);

        /**
         * Creates a snapshot from the given map.
         * <p>
         * The map is defensively copied; subsequent mutations to the
         * original map do not affect the snapshot.
         *
         * @param values the context entries; must not be {@code null}
         * @return an immutable snapshot; never {@code null}
         * @throws NullPointerException if {@code values} is {@code null}
         */
        static Snapshot of(Map<String, Object> values) {
            return DefaultAsyncContext.createSnapshot(values);
        }
    }
}
