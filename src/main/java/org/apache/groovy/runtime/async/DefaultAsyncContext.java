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
package org.apache.groovy.runtime.async;

import groovy.concurrent.AsyncContext;
import groovy.lang.Closure;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of {@link AsyncContext} backed by a
 * {@link LinkedHashMap} for insertion-ordered key-value storage.
 *
 * <h2>Thread-scoped storage</h2>
 * <p>A per-thread {@code DefaultAsyncContext} instance is managed via a
 * {@link ScopedLocal} with a lazy initial-value supplier.  On JDK&nbsp;25+
 * this leverages {@code ScopedValue} for optimal virtual-thread
 * performance; on earlier JDKs it falls back to {@code ThreadLocal}.</p>
 *
 * <p>This class is an internal implementation detail of the Groovy async
 * runtime.  User code should program against the {@link AsyncContext}
 * interface and use its static factory and utility methods.</p>
 *
 * @see AsyncContext
 * @since 6.0.0
 */
public final class DefaultAsyncContext implements AsyncContext {

    /**
     * Per-thread binding of the current async context.
     * Lazy-initialized with an empty {@code DefaultAsyncContext}.
     */
    private static final ScopedLocal<DefaultAsyncContext> CURRENT =
            ScopedLocal.withInitial(DefaultAsyncContext::new);

    private final Map<String, Object> values;

    /**
     * Creates an empty async context.
     */
    public DefaultAsyncContext() {
        this.values = new LinkedHashMap<>();
    }

    /**
     * Creates a context seeded from the supplied entries.
     *
     * @param initialValues initial context entries; may be {@code null}
     */
    public DefaultAsyncContext(Map<?, ?> initialValues) {
        this();
        putAll(initialValues);
    }

    /**
     * Internal constructor that directly adopts a pre-copied map.
     * The caller must guarantee the map will not be mutated externally.
     */
    DefaultAsyncContext(LinkedHashMap<String, Object> values, boolean ignored) {
        this.values = values;
    }

    // ---- Static operations (delegated from AsyncContext interface) -------

    /**
     * Returns the live async context associated with the current thread.
     *
     * @return the current async context; never {@code null}
     */
    public static DefaultAsyncContext current() {
        return CURRENT.get();
    }

    /**
     * Captures an immutable snapshot of the current async context.
     *
     * @return the captured snapshot; never {@code null}
     */
    public static AsyncContext.Snapshot capture() {
        return DefaultSnapshot.of(current().copyValues());
    }

    /**
     * Executes the supplier with the given snapshot installed as the
     * current async context, restoring the previous context afterwards.
     *
     * @param snapshot the snapshot to install; must not be {@code null}
     * @param supplier the action to execute; must not be {@code null}
     * @param <T>      the result type
     * @return the supplier's result
     */
    public static <T> T withSnapshot(AsyncContext.Snapshot snapshot, Supplier<T> supplier) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");
        DefaultAsyncContext restored = new DefaultAsyncContext(
                new LinkedHashMap<>(snapshot.asMap()), true);
        return CURRENT.where(restored, supplier);
    }

    /**
     * Executes the runnable with the given snapshot installed as the
     * current async context, restoring the previous context afterwards.
     *
     * @param snapshot the snapshot to install; must not be {@code null}
     * @param action   the action to execute; must not be {@code null}
     */
    public static void withSnapshot(AsyncContext.Snapshot snapshot, Runnable action) {
        withSnapshot(snapshot, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Temporarily overlays the current async context with the supplied
     * entries for the duration of the closure.
     *
     * @param entries the entries to overlay; may be {@code null}
     * @param action  the closure to execute; must not be {@code null}
     * @param <T>     the result type
     * @return the closure's result
     */
    public static <T> T with(Map<?, ?> entries, Closure<T> action) {
        Objects.requireNonNull(action, "action must not be null");
        AsyncContext.Snapshot merged = capture().with(entries);
        return withSnapshot(merged, () -> action.call());
    }

    /**
     * Creates a {@link AsyncContext.Snapshot} from a pre-copied map.
     * Called by the {@link AsyncContext.Snapshot#of(Map)} static factory.
     *
     * @param values the context entries (defensively copied by the caller)
     * @return an immutable snapshot
     */
    public static AsyncContext.Snapshot createSnapshot(Map<String, Object> values) {
        return DefaultSnapshot.of(new LinkedHashMap<>(values));
    }

    // ---- Instance methods (AsyncContext interface) -----------------------

    @Override
    public Object get(String key) {
        return values.get(normalizeKey(key));
    }

    @Override
    public Object getOrDefault(String key, Object defaultValue) {
        String normalized = normalizeKey(key);
        Object val = values.get(normalized);
        return val != null || values.containsKey(normalized) ? val : defaultValue;
    }

    @Override
    public Object computeIfAbsent(String key, Function<String, Object> mappingFunction) {
        Objects.requireNonNull(mappingFunction, "mappingFunction must not be null");
        String normalized = normalizeKey(key);
        Object val = values.get(normalized);
        if (val == null && !values.containsKey(normalized)) {
            val = mappingFunction.apply(normalized);
            if (val != null) {
                values.put(normalized, val);
            }
        }
        return val;
    }

    @Override
    public Object getAt(String key) {
        return get(key);
    }

    @Override
    public Object put(String key, Object value) {
        String normalized = normalizeKey(key);
        if (value == null) {
            return values.remove(normalized);
        }
        return values.put(normalized, value);
    }

    @Override
    public void putAt(String key, Object value) {
        put(key, value);
    }

    @Override
    public Object remove(String key) {
        return values.remove(normalizeKey(key));
    }

    @Override
    public void putAll(Map<?, ?> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        for (Map.Entry<?, ?> entry : entries.entrySet()) {
            put(normalizeKey(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return values.containsKey(normalizeKey(key));
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(copyValues());
    }

    @Override
    public String toString() {
        return "AsyncContext" + values;
    }

    // ---- Internal -------------------------------------------------------

    Map<String, Object> copyValues() {
        return new LinkedHashMap<>(values);
    }

    static String normalizeKey(Object key) {
        return Objects.requireNonNull(key, "context key must not be null").toString();
    }

    // ---- DefaultSnapshot ------------------------------------------------

    /**
     * Default implementation of {@link AsyncContext.Snapshot} backed by an
     * unmodifiable {@link Map}.
     * <p>
     * Instances are created via the package-private {@link #of(Map)} factory.
     * The caller is responsible for ensuring that the supplied map has
     * already been defensively copied.
     *
     * @since 6.0.0
     */
    static final class DefaultSnapshot implements AsyncContext.Snapshot {

        private final Map<String, Object> values;

        private DefaultSnapshot(Map<String, Object> values) {
            this.values = values;
        }

        /**
         * Wraps a pre-copied map into an immutable snapshot.
         */
        static DefaultSnapshot of(Map<String, Object> preCopied) {
            return new DefaultSnapshot(Collections.unmodifiableMap(preCopied));
        }

        @Override
        public boolean containsKey(String key) {
            return values.containsKey(normalizeKey(key));
        }

        @Override
        public Object get(String key) {
            return values.get(normalizeKey(key));
        }

        @Override
        public Object getOrDefault(String key, Object defaultValue) {
            String normalized = normalizeKey(key);
            Object val = values.get(normalized);
            return val != null || values.containsKey(normalized) ? val : defaultValue;
        }

        @Override
        public boolean isEmpty() {
            return values.isEmpty();
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public Map<String, Object> asMap() {
            return values;
        }

        @Override
        public AsyncContext.Snapshot with(Map<?, ?> entries) {
            if (entries == null || entries.isEmpty()) {
                return this;
            }
            Map<String, Object> merged = new LinkedHashMap<>(values);
            for (Map.Entry<?, ?> entry : entries.entrySet()) {
                String key = normalizeKey(entry.getKey());
                Object value = entry.getValue();
                if (value == null) {
                    merged.remove(key);
                } else {
                    merged.put(key, value);
                }
            }
            return DefaultSnapshot.of(merged);
        }

        @Override
        public String toString() {
            return "AsyncContext.Snapshot" + values;
        }
    }
}
