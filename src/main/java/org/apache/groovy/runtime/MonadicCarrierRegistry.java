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
package org.apache.groovy.runtime;

import groovy.concurrent.Awaitable;
import org.apache.groovy.lang.annotation.Incubating;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

/**
 * The standard carrier allow-list for monadic comprehensions: stdlib and
 * Groovy-core carriers whose bind/map method names diverge from the structural
 * {@code flatMap}/{@code map} convention.
 * <p>
 * Carriers known by {@code Class} (always on the classpath) are matched by
 * {@code isInstance}; carriers known only by name &mdash; third-party libraries
 * that Groovy must not depend on, such as Functional Java and Vavr &mdash; are
 * matched by walking the value's type hierarchy and comparing fully-qualified
 * names. The {@code CompletionStage} entry covers {@code CompletableFuture};
 * the {@code Awaitable} entry covers {@code DataflowVariable}; the Functional
 * Java entries use that library's {@code bind}/{@code map} convention; the
 * Vavr entries use the structural {@code flatMap}/{@code map} convention.
 *
 * @since 6.0.0
 */
@Incubating
public final class MonadicCarrierRegistry {

    /** One {@code Class}-keyed allow-list row. */
    public static final class Entry {
        private final Class<?> carrier;
        private final String bind;
        private final String map;

        Entry(Class<?> carrier, String bind, String map) {
            this.carrier = carrier;
            this.bind = bind;
            this.map = map;
        }

        public Class<?> carrier() { return carrier; }
        public String bind() { return bind; }
        public String map() { return map; }

        @Override
        public String toString() {
            return carrier.getName() + " { bind=" + bind + ", map=" + map + " }";
        }
    }

    /** One name-keyed allow-list row, for carriers Groovy must not depend on. */
    public static final class NamedEntry {
        private final String carrierName;
        private final String bind;
        private final String map;

        NamedEntry(String carrierName, String bind, String map) {
            this.carrierName = carrierName;
            this.bind = bind;
            this.map = map;
        }

        public String carrierName() { return carrierName; }
        public String bind() { return bind; }
        public String map() { return map; }

        @Override
        public String toString() {
            return carrierName + " { bind=" + bind + ", map=" + map + " }";
        }
    }

    private static final List<Entry> ENTRIES;
    private static final List<NamedEntry> NAMED_ENTRIES;
    static {
        ENTRIES = List.of(
            new Entry(Optional.class, "flatMap", "map"),
            new Entry(Stream.class, "flatMap", "map"),
            new Entry(CompletionStage.class, "thenCompose", "thenApply"), // covers CompletableFuture
            new Entry(Awaitable.class, "thenCompose", "then") // covers DataflowVariable
        );

        // Functional Java (org.functionaljava) — recognised by name; no dependency.

        NAMED_ENTRIES = List.of(
            new NamedEntry("fj.data.Option", "bind", "map"),
            new NamedEntry("fj.data.List", "bind", "map"),
            new NamedEntry("fj.data.Stream", "bind", "map"),
            new NamedEntry("fj.data.Validation", "bind", "map"),
            new NamedEntry("fj.P1", "bind", "map"),

            // Vavr (io.vavr) — recognised by name; no dependency. Vavr's control
            // carriers use the structural flatMap/map convention, so they are
            // covered by the default dispatcher even without an entry here; the
            // entries are retained so the carrier names appear explicitly in the
            // standard allow-list and pass the MonadicChecker's participation test
            // without requiring a structural match.
            new NamedEntry("io.vavr.control.Option", "flatMap", "map"),
            new NamedEntry("io.vavr.control.Try", "flatMap", "map"),
            new NamedEntry("io.vavr.control.Either", "flatMap", "map"),
            new NamedEntry("io.vavr.control.Validation", "flatMap", "map"));
    }

    private MonadicCarrierRegistry() {}

    /** The {@code Class}-keyed allow-list, exposed for the type-checking extension. */
    public static List<Entry> entries() {
        return ENTRIES;
    }

    /** The name-keyed allow-list, exposed for the type-checking extension. */
    public static List<NamedEntry> namedEntries() {
        return NAMED_ENTRIES;
    }

    /**
     * The {@code [bind, map]} method names for the given carrier value, or
     * {@code null} if it is not on either allow-list. {@code Class} entries are
     * tried first ({@code isInstance}), then name entries against the value's
     * full type hierarchy (so {@code fj.data.Some} matches {@code fj.data.Option}).
     */
    public static String[] lookupBindMap(Object carrier) {
        if (carrier == null) return null;
        for (Entry entry : ENTRIES) {
            if (entry.carrier().isInstance(carrier)) {
                return new String[]{entry.bind(), entry.map()};
            }
        }
        if (!NAMED_ENTRIES.isEmpty()) {
            for (Class<?> t : supertypes(carrier.getClass())) {
                String name = t.getName();
                for (NamedEntry entry : NAMED_ENTRIES) {
                    if (entry.carrierName().equals(name)) {
                        return new String[]{entry.bind(), entry.map()};
                    }
                }
            }
        }
        return null;
    }

    private static Set<Class<?>> supertypes(Class<?> start) {
        Set<Class<?>> seen = new LinkedHashSet<Class<?>>();
        Deque<Class<?>> queue = new ArrayDeque<Class<?>>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Class<?> c = queue.poll();
            if (c == null || !seen.add(c)) continue;
            if (c.getSuperclass() != null) queue.add(c.getSuperclass());
            Collections.addAll(queue, c.getInterfaces());
        }
        return seen;
    }
}
