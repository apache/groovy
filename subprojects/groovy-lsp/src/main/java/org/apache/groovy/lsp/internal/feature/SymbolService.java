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
package org.apache.groovy.lsp.internal.feature;

import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Document and workspace symbols.
 */
public final class SymbolService {

    /**
     * Hierarchical document symbols for {@code document}.
     *
     * @param document compiled document
     * @param encoding negotiated encoding
     * @return document symbols
     */
    public List<DocumentSymbol> documentSymbols(final CompiledDocument document, final PositionEncoding encoding) {
        List<DocumentSymbol> symbols = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return symbols;
        }
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            addDocumentSymbol(symbols, document, encoding, node);
        }
        return symbols;
    }

    /**
     * Workspace symbols matching {@code query}.
     *
     * @param snapshot latest compile
     * @param query case-insensitive substring
     * @param encoding negotiated encoding
     * @return workspace symbols
     */
    public List<WorkspaceSymbol> workspaceSymbols(final CompilationSnapshot snapshot, final String query,
                                                  final PositionEncoding encoding) {
        String needle = query == null ? "" : query.toLowerCase(Locale.ROOT);
        List<WorkspaceSymbol> symbols = new ArrayList<>();
        if (snapshot == null) {
            return symbols;
        }
        for (CompiledDocument document : snapshot.documents()) {
            addWorkspaceSymbols(symbols, document, needle, encoding);
        }
        return symbols;
    }

    /**
     * Legacy symbol information for clients that do not support hierarchical symbols.
     *
     * @param document compiled document
     * @param encoding negotiated encoding
     * @return symbol information
     */
    public List<SymbolInformation> documentSymbolInformation(final CompiledDocument document,
                                                             final PositionEncoding encoding) {
        List<SymbolInformation> symbols = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return symbols;
        }
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            String name = NavigationService.nameOf(node);
            Range range = node instanceof AnnotatedNode annotated
                    ? Positions.toNameRange(annotated, document.getText(), encoding)
                    : Positions.toRange(node, document.getText(), encoding);
            if (name == null || range == null) {
                continue;
            }
            symbols.add(new SymbolInformation(name, kindOf(node),
                    new Location(document.getUri().toString(), range)));
        }
        return symbols;
    }

    private static void addDocumentSymbol(final List<DocumentSymbol> symbols, final CompiledDocument document,
                                          final PositionEncoding encoding, final ASTNode node) {
        if (!(node instanceof ClassNode classNode) || (classNode.isScript() && classNode.getLineNumber() <= 0)) {
            return;
        }
        DocumentSymbol symbol = toDocumentSymbol(classNode, document.getText(), encoding);
        if (symbol == null) {
            return;
        }
        List<DocumentSymbol> children = new ArrayList<>();
        for (ASTNode member : AstQuery.declarations(document.getModule())) {
            addChildSymbol(children, document, encoding, classNode, member);
        }
        symbol.setChildren(children);
        symbols.add(symbol);
    }

    private static void addChildSymbol(final List<DocumentSymbol> children, final CompiledDocument document,
                                       final PositionEncoding encoding, final ClassNode classNode, final ASTNode member) {
        AnnotatedNode annotated = childOf(classNode, member);
        if (annotated == null) {
            return;
        }
        DocumentSymbol child = toDocumentSymbol(annotated, document.getText(), encoding);
        if (child != null) {
            children.add(child);
        }
    }

    private static AnnotatedNode childOf(final ClassNode classNode, final ASTNode member) {
        if (member instanceof MethodNode method && method.getDeclaringClass() == classNode) {
            return method;
        }
        if (member instanceof FieldNode field && field.getDeclaringClass() == classNode) {
            return field;
        }
        if (member instanceof PropertyNode property && property.getDeclaringClass() == classNode) {
            return property;
        }
        return null;
    }

    private static void addWorkspaceSymbols(final List<WorkspaceSymbol> symbols, final CompiledDocument document,
                                            final String needle, final PositionEncoding encoding) {
        if (document.getModule() == null) {
            return;
        }
        for (ASTNode node : AstQuery.declarations(document.getModule())) {
            WorkspaceSymbol symbol = workspaceSymbol(document, node, needle, encoding);
            if (symbol != null) {
                symbols.add(symbol);
            }
        }
    }

    private static WorkspaceSymbol workspaceSymbol(final CompiledDocument document, final ASTNode node,
                                                   final String needle, final PositionEncoding encoding) {
        String name = NavigationService.nameOf(node);
        if (name == null || (!needle.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(needle))) {
            return null;
        }
        Range range = node instanceof AnnotatedNode annotated
                ? Positions.toNameRange(annotated, document.getText(), encoding)
                : Positions.toRange(node, document.getText(), encoding);
        if (range == null) {
            return null;
        }
        return new WorkspaceSymbol(name, kindOf(node),
                Either.forLeft(new Location(document.getUri().toString(), range)));
    }

    private static DocumentSymbol toDocumentSymbol(final AnnotatedNode node, final String text,
                                                   final PositionEncoding encoding) {
        String name = NavigationService.nameOf(node);
        Range range = Positions.toRange(node, text, encoding);
        Range selection = Positions.toNameRange(node, text, encoding);
        if (name == null || range == null || selection == null) {
            return null;
        }
        return new DocumentSymbol(name, kindOf(node), range, selection);
    }

    static SymbolKind kindOf(final ASTNode node) {
        if (node instanceof ClassNode classNode) {
            if (classNode.isInterface()) {
                return SymbolKind.Interface;
            }
            if (classNode.isEnum()) {
                return SymbolKind.Enum;
            }
            return SymbolKind.Class;
        }
        if (node instanceof ConstructorNode) {
            return SymbolKind.Constructor;
        }
        if (node instanceof MethodNode) {
            return SymbolKind.Method;
        }
        if (node instanceof FieldNode) {
            return SymbolKind.Field;
        }
        if (node instanceof PropertyNode) {
            return SymbolKind.Property;
        }
        return SymbolKind.Variable;
    }

    static Range emptyRange() {
        return new Range(new Position(0, 0), new Position(0, 0));
    }
}
