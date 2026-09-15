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
package org.apache.groovy.lsp.internal.compile;

import org.apache.groovy.lsp.internal.util.Uris;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Immutable result of one workspace compile.
 */
public final class CompilationSnapshot {

    public static final CompilationSnapshot EMPTY = new CompilationSnapshot(Map.of());

    private final Map<URI, CompiledDocument> documents;
    private final AtomicReference<TypeIndex> types = new AtomicReference<>();

    /**
     * @param documents compiled documents keyed by normalized URI
     */
    public CompilationSnapshot(final Map<URI, CompiledDocument> documents) {
        this.documents = Map.copyOf(documents);
    }

    /**
     * @return types declared in this snapshot
     */
    public TypeIndex types() {
        TypeIndex index = types.get();
        if (index != null) {
            return index;
        }
        TypeIndex created = TypeIndex.of(this);
        if (types.compareAndSet(null, created)) {
            return created;
        }
        return types.get();
    }

    /**
     * @param uri document URI
     * @return the compiled document, or {@code null}
     */
    public CompiledDocument get(final URI uri) {
        return documents.get(Uris.normalize(uri));
    }

    /**
     * @param uriString document URI string
     * @return the compiled document, or {@code null}
     */
    public CompiledDocument get(final String uriString) {
        return get(Uris.parse(uriString));
    }

    /**
     * @return every compiled document
     */
    public Collection<CompiledDocument> documents() {
        return documents.values();
    }

    /**
     * @return a mutable copy of the map, for builders
     */
    static Map<URI, CompiledDocument> newMap() {
        return new LinkedHashMap<>();
    }
}
