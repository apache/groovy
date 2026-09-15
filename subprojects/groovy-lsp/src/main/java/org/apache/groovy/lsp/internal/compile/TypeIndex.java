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

import groovy.lang.groovydoc.Groovydoc;
import org.codehaus.groovy.ast.ClassNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Simple-name and FQCN index of types in a {@link CompilationSnapshot}.
 */
public final class TypeIndex {

    public static final TypeIndex EMPTY = new TypeIndex(Map.of(), Map.of());

    private final Map<String, List<TypeHit>> bySimpleName;
    private final Map<String, TypeHit> byName;

    private TypeIndex(final Map<String, List<TypeHit>> bySimpleName, final Map<String, TypeHit> byName) {
        this.bySimpleName = bySimpleName;
        this.byName = byName;
    }

    /**
     * Builds an index over every non-synthetic class in {@code snapshot}.
     *
     * @param snapshot compiled workspace
     * @return the index
     */
    public static TypeIndex of(final CompilationSnapshot snapshot) {
        if (snapshot == null) {
            return EMPTY;
        }
        Map<String, List<TypeHit>> simple = new LinkedHashMap<>();
        Map<String, TypeHit> names = new LinkedHashMap<>();
        for (CompiledDocument document : snapshot.documents()) {
            if (document.getModule() == null) {
                continue;
            }
            for (ClassNode classNode : document.getModule().getClasses()) {
                if (classNode.isScript() && classNode.getLineNumber() <= 0) {
                    continue;
                }
                TypeHit hit = TypeHit.of(classNode, document.getUri());
                names.put(hit.name(), hit);
                simple.computeIfAbsent(hit.simpleName(), key -> new ArrayList<>()).add(hit);
            }
        }
        return new TypeIndex(Map.copyOf(simple), Map.copyOf(names));
    }

    /**
     * @param simpleName unqualified name
     * @return hits, possibly empty
     */
    public List<TypeHit> bySimpleName(final String simpleName) {
        if (simpleName == null) {
            return List.of();
        }
        List<TypeHit> hits = bySimpleName.get(simpleName);
        return hits == null ? List.of() : hits;
    }

    /**
     * @param simpleName unqualified name
     * @return the only hit, or {@code null} when missing or ambiguous
     */
    public TypeHit uniqueBySimpleName(final String simpleName) {
        List<TypeHit> hits = bySimpleName(simpleName);
        return hits.size() == 1 ? hits.get(0) : null;
    }

    /**
     * @param name FQCN
     * @return the hit, or {@code null}
     */
    public TypeHit byName(final String name) {
        return name == null ? null : byName.get(name);
    }

    /**
     * Types whose simple name starts with {@code prefix}, in document order.
     *
     * @param prefix case-insensitive prefix, possibly empty
     * @param limit maximum hits
     * @return matching types
     */
    public List<TypeHit> matchingPrefix(final String prefix, final int limit) {
        String needle = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<TypeHit> hits = new ArrayList<>();
        for (TypeHit hit : byName.values()) {
            if (needle.isEmpty() || hit.simpleName().toLowerCase(Locale.ROOT).startsWith(needle)) {
                hits.add(hit);
                if (hits.size() >= limit) {
                    break;
                }
            }
        }
        return hits;
    }

    /**
     * One indexed type.
     *
     * @param name FQCN
     * @param simpleName unqualified name
     * @param uri declaring file
     * @param iface whether the type is an interface
     * @param enumeration whether the type is an enum
     * @param documentation Groovydoc markdown, possibly empty
     */
    public record TypeHit(String name, String simpleName, URI uri, boolean iface, boolean enumeration,
                          String documentation) {

        static TypeHit of(final ClassNode classNode, final URI uri) {
            return new TypeHit(classNode.getName(), classNode.getNameWithoutPackage(), uri,
                    classNode.isInterface(), classNode.isEnum(), documentation(classNode));
        }

        private static String documentation(final ClassNode classNode) {
            Groovydoc groovydoc = classNode.getGroovydoc();
            if (groovydoc == null || !groovydoc.isPresent()) {
                return "";
            }
            String content = groovydoc.getContent();
            return content == null ? "" : content;
        }
    }
}
