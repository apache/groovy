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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import org.apache.groovy.lsp.internal.util.Uris;

import javax.lang.model.element.Name;
import javax.tools.JavaFileObject;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Types and members compiled from workspace {@code .java} sources.
 * Coordinates are Groovy 1-based carets.
 */
public final class JavaSymbolIndex {

    public static final JavaSymbolIndex EMPTY = new JavaSymbolIndex(Map.of(), Map.of(), Map.of());

    private final Map<String, JavaSymbol> byName;
    private final Map<String, List<JavaSymbol>> bySimpleName;
    private final Map<String, List<JavaSymbol>> members;

    private JavaSymbolIndex(final Map<String, JavaSymbol> byName, final Map<String, List<JavaSymbol>> bySimpleName,
                            final Map<String, List<JavaSymbol>> members) {
        this.byName = byName;
        this.bySimpleName = bySimpleName;
        this.members = members;
    }

    /**
     * Walks {@code units} using {@code trees} captured from the javac task
     * before {@code generate()}.
     *
     * @param trees javac tree API
     * @param units parsed compilation units
     * @return an index, never {@code null}
     */
    public static JavaSymbolIndex of(final Trees trees, final Iterable<? extends CompilationUnitTree> units) {
        if (trees == null || units == null) {
            return EMPTY;
        }
        Map<String, JavaSymbol> names = new LinkedHashMap<>();
        Map<String, List<JavaSymbol>> simple = new LinkedHashMap<>();
        Map<String, List<JavaSymbol>> members = new LinkedHashMap<>();
        SourcePositions positions = trees.getSourcePositions();
        for (CompilationUnitTree unit : units) {
            if (unit == null || isStub(unit.getSourceFile())) {
                continue;
            }
            URI uri = Uris.normalize(unit.getSourceFile().toUri());
            LineMap lineMap = unit.getLineMap();
            new IndexingScanner(uri, unit, positions, lineMap, new IndexMaps(names, simple, members)).scan(unit, null);
        }
        return new JavaSymbolIndex(Map.copyOf(names), copyLists(simple), copyLists(members));
    }

    /**
     * @param qualifiedName FQCN
     * @return the type, or {@code null}
     */
    public JavaSymbol type(final String qualifiedName) {
        return qualifiedName == null ? null : byName.get(qualifiedName);
    }

    /**
     * @return every indexed type
     */
    public List<JavaSymbol> types() {
        return List.copyOf(byName.values());
    }

    /**
     * @param simpleName unqualified name
     * @return hits, possibly empty
     */
    public List<JavaSymbol> typesNamed(final String simpleName) {
        if (simpleName == null) {
            return List.of();
        }
        List<JavaSymbol> hits = bySimpleName.get(simpleName);
        return hits == null ? List.of() : hits;
    }

    /**
     * Members of {@code owner} with {@code simpleName}.
     *
     * @param owner FQCN
     * @param simpleName member name
     * @return hits, possibly empty
     */
    public List<JavaSymbol> members(final String owner, final String simpleName) {
        if (owner == null || simpleName == null) {
            return List.of();
        }
        List<JavaSymbol> hits = members.get(owner + "#" + simpleName);
        return hits == null ? List.of() : hits;
    }

    private static boolean isStub(final JavaFileObject file) {
        if (file == null || file.toUri() == null) {
            return true;
        }
        String scheme = file.toUri().getScheme();
        return scheme != null && "string".equalsIgnoreCase(scheme);
    }

    private static Map<String, List<JavaSymbol>> copyLists(final Map<String, List<JavaSymbol>> source) {
        Map<String, List<JavaSymbol>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<JavaSymbol>> entry : source.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }

    /**
     * One Java type or member.
     *
     * @param qualifiedName FQCN for types; {@code owner#name} for members
     * @param simpleName identifier
     * @param owner FQCN of the enclosing type, or {@code null} for a top-level type
     * @param uri source file
     * @param line 1-based start line
     * @param column 1-based start column
     * @param endLine 1-based end line
     * @param endColumn exclusive 1-based end column
     */
    public record JavaSymbol(String qualifiedName, String simpleName, String owner, URI uri,
                             int line, int column, int endLine, int endColumn) {
    }

    private record IndexMaps(Map<String, JavaSymbol> names, Map<String, List<JavaSymbol>> simple,
                             Map<String, List<JavaSymbol>> members) {
    }

    private static final class IndexingScanner extends TreePathScanner<Void, Void> {
        private final URI uri;
        private final CompilationUnitTree unit;
        private final SourcePositions positions;
        private final LineMap lineMap;
        private final IndexMaps maps;
        private final List<String> enclosing = new ArrayList<>();

        private IndexingScanner(final URI uri, final CompilationUnitTree unit, final SourcePositions positions,
                                final LineMap lineMap, final IndexMaps maps) {
            this.uri = uri;
            this.unit = unit;
            this.positions = positions;
            this.lineMap = lineMap;
            this.maps = maps;
        }

        @Override
        public Void visitClass(final ClassTree node, final Void unused) {
            String simpleName = text(node.getSimpleName());
            if (simpleName.isEmpty()) {
                return super.visitClass(node, unused);
            }
            String qualified = qualified(simpleName);
            JavaSymbol symbol = symbol(qualified, simpleName, enclosing.isEmpty() ? null : enclosing.get(enclosing.size() - 1),
                    node);
            maps.names().put(qualified, symbol);
            maps.simple().computeIfAbsent(simpleName, key -> new ArrayList<>()).add(symbol);
            enclosing.add(qualified);
            try {
                return super.visitClass(node, unused);
            } finally {
                enclosing.remove(enclosing.size() - 1);
            }
        }

        @Override
        public Void visitMethod(final MethodTree node, final Void unused) {
            addMember(text(node.getName()), node);
            return super.visitMethod(node, unused);
        }

        @Override
        public Void visitVariable(final VariableTree node, final Void unused) {
            Tree.Kind parent = getCurrentPath() == null || getCurrentPath().getParentPath() == null
                    ? null : getCurrentPath().getParentPath().getLeaf().getKind();
            if (parent == Tree.Kind.CLASS || parent == Tree.Kind.INTERFACE || parent == Tree.Kind.ENUM
                    || parent == Tree.Kind.RECORD) {
                addMember(text(node.getName()), node);
            }
            return super.visitVariable(node, unused);
        }

        private void addMember(final String simpleName, final Tree node) {
            if (simpleName.isEmpty() || "<init>".equals(simpleName) || enclosing.isEmpty()) {
                return;
            }
            String owner = enclosing.get(enclosing.size() - 1);
            JavaSymbol symbol = symbol(owner + "#" + simpleName, simpleName, owner, node);
            maps.members().computeIfAbsent(owner + "#" + simpleName, key -> new ArrayList<>()).add(symbol);
        }

        private String qualified(final String simpleName) {
            if (enclosing.isEmpty()) {
                String pkg = packageName();
                return pkg.isEmpty() ? simpleName : pkg + "." + simpleName;
            }
            return enclosing.get(enclosing.size() - 1) + "$" + simpleName;
        }

        private String packageName() {
            if (unit.getPackageName() == null) {
                return "";
            }
            return unit.getPackageName().toString();
        }

        private JavaSymbol symbol(final String qualified, final String simpleName, final String owner, final Tree node) {
            long start = positions.getStartPosition(unit, node);
            long end = positions.getEndPosition(unit, node);
            if (start < 0) {
                start = 0;
            }
            if (end < start) {
                end = start;
            }
            int line = (int) lineMap.getLineNumber(start);
            int column = (int) lineMap.getColumnNumber(start);
            int endLine = (int) lineMap.getLineNumber(end);
            int endColumn = (int) lineMap.getColumnNumber(end);
            return new JavaSymbol(qualified, simpleName, owner, uri, line, column, endLine, endColumn);
        }

        private static String text(final Name name) {
            return name == null ? "" : name.toString();
        }
    }
}
