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
import org.apache.groovy.lsp.internal.compile.Identifiers;
import org.apache.groovy.lsp.internal.compile.JavaSymbolIndex;
import org.apache.groovy.lsp.internal.compile.TypeIndex;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Go-to definition, type definition, implementation, declaration and references.
 */
public final class NavigationService {

    /**
     * Resolves the definition of the symbol at {@code position}.
     *
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return location links
     */
    public List<LocationLink> definition(final TextDocument document, final CompilationSnapshot snapshot,
                                         final Position position, final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled != null && compiled.getModule() != null) {
            ASTNode node = AstQuery.nodeAt(compiled.getModule(), document, position, encoding);
            if (node instanceof MethodCallExpression || node instanceof StaticMethodCallExpression) {
                List<MethodNode> methods = new ArrayList<>(MethodBinding.resolveMethodCandidates(node, snapshot));
                List<LocationLink> links = linksFor(methods, node, document, snapshot, compiled, encoding);
                if (!links.isEmpty()) {
                    return links;
                }
            }
        }
        List<LocationLink> groovy = locate(document, snapshot, position, encoding, Target.DEFINITION);
        if (!groovy.isEmpty()) {
            return groovy;
        }
        return javaIdentifierLinks(document, snapshot, position, encoding);
    }

    /**
     * Resolves the type definition of the symbol at {@code position}.
     *
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return location links
     */
    public List<LocationLink> typeDefinition(final TextDocument document, final CompilationSnapshot snapshot,
                                             final Position position, final PositionEncoding encoding) {
        return locate(document, snapshot, position, encoding, Target.TYPE);
    }

    /**
     * Resolves implementations of the type or method at {@code position}.
     *
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return location links
     */
    public List<LocationLink> implementation(final TextDocument document, final CompilationSnapshot snapshot,
                                             final Position position, final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), document, position, encoding);
        ASTNode dest = resolve(node, Target.IMPLEMENTATION, snapshot);
        if (dest instanceof ClassNode classNode) {
            return linksFor(MethodBinding.implementations(classNode, snapshot), node, document, snapshot, compiled, encoding);
        }
        if (dest instanceof MethodNode method) {
            return linksFor(MethodBinding.methodImplementations(method, snapshot), node, document, snapshot, compiled, encoding);
        }
        return locate(document, snapshot, position, encoding, Target.IMPLEMENTATION);
    }

    /**
     * Nearest overridden method at {@code position}. Skips
     * {@code Object} / {@code GroovyObject}. Empty when the caret is not
     * on an override or the super has no source.
     *
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return locations, never {@code null}
     */
    public List<Location> superMethod(final TextDocument document, final CompilationSnapshot snapshot,
                                      final Position position, final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), document, position, encoding);
        MethodNode method = MethodBinding.methodAt(node, snapshot);
        MethodNode parent = MethodBinding.superOf(method);
        if (parent == null) {
            return List.of();
        }
        LocationLink link = toLink(node, parent, document, snapshot, compiled, encoding);
        if (link == null) {
            return List.of();
        }
        return List.of(new Location(link.getTargetUri(), link.getTargetSelectionRange()));
    }

    /**
     * Resolves the declaration (same as definition for Groovy).
     *
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return location links
     */
    public List<LocationLink> declaration(final TextDocument document, final CompilationSnapshot snapshot,
                                          final Position position, final PositionEncoding encoding) {
        return locate(document, snapshot, position, encoding, Target.DEFINITION);
    }

    /**
     * Finds references to the symbol at {@code position}.
     *
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @param includeDeclaration whether to include the declaration
     * @return locations
     */
    public List<Location> references(final TextDocument document, final CompilationSnapshot snapshot,
                                     final Position position, final PositionEncoding encoding,
                                     final boolean includeDeclaration) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), document, position, encoding);
        SymbolIdentity identity = SymbolIdentity.of(node, snapshot);
        if (identity == null || identity.name() == null) {
            return List.of();
        }
        List<Location> locations = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        if (includeDeclaration) {
            addLocation(locations, seen, document.getUri(), identity.declaration(), compiled.getText(), encoding);
        }
        ReferenceWalk walk = new ReferenceWalk(locations, seen, identity, snapshot, encoding, includeDeclaration);
        for (CompiledDocument other : snapshot.documents()) {
            addReferencesFrom(walk, other);
        }
        return locations;
    }

    private static void addReferencesFrom(final ReferenceWalk walk, final CompiledDocument other) {
        if (other.getModule() == null || !Identifiers.containsWord(other.getText(), walk.identity().name())) {
            return;
        }
        AstQuery.walk(other.getModule(), (hit, ctx) -> {
            if (!walk.includeDeclaration() && walk.identity().isDeclaration(hit)) {
                return;
            }
            if (walk.identity().refersTo(hit, ctx)) {
                addLocation(walk.locations(), walk.seen(), other.getUri(), hit, other.getText(), walk.encoding());
            }
        });
    }

    private List<LocationLink> locate(final TextDocument document, final CompilationSnapshot snapshot,
                                      final Position position, final PositionEncoding encoding, final Target target) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), document, position, encoding);
        ASTNode dest = resolve(node, target, snapshot);
        if (dest == null) {
            dest = node;
        }
        LocationLink link = toLink(node, dest, document, snapshot, compiled, encoding);
        return link == null ? List.of() : List.of(link);
    }

    private static ASTNode resolve(final ASTNode node, final Target target, final CompilationSnapshot snapshot) {
        if (node instanceof VariableExpression variable) {
            return resolveVariable(variable, target);
        }
        if (node instanceof ClassExpression classExpression) {
            return classExpression.getType();
        }
        if (node instanceof MethodCallExpression call) {
            return resolveCall(call, target, snapshot);
        }
        if (node instanceof StaticMethodCallExpression call) {
            return MethodBinding.resolveStatic(call, snapshot);
        }
        if (node instanceof ConstructorCallExpression ctor) {
            return ctor.getType();
        }
        if (node instanceof PropertyExpression property) {
            return resolvePropertyTarget(property, target);
        }
        if (node instanceof DeclarationExpression declaration && declaration.isMultipleAssignmentDeclaration()) {
            return declaration;
        }
        if (node instanceof ClassNode classNode && target == Target.IMPLEMENTATION) {
            return classNode;
        }
        if (node instanceof MethodNode method && target == Target.IMPLEMENTATION) {
            return method;
        }
        return resolveDeclaration(node, target);
    }

    private static ASTNode resolveVariable(final VariableExpression variable, final Target target) {
        if (target == Target.TYPE) {
            return variable.getType();
        }
        Variable accessed = variable.getAccessedVariable();
        return accessed instanceof ASTNode ast ? ast : null;
    }

    private static ASTNode resolveCall(final MethodCallExpression call, final Target target,
                                       final CompilationSnapshot snapshot) {
        if (target == Target.TYPE) {
            return call.getType();
        }
        return MethodBinding.resolveMethod(call, snapshot);
    }

    private static ASTNode resolvePropertyTarget(final PropertyExpression property, final Target target) {
        if (target == Target.TYPE) {
            return property.getType();
        }
        return MethodBinding.resolveProperty(property);
    }

    private static ASTNode resolveDeclaration(final ASTNode node, final Target target) {
        if (!(node instanceof MethodNode || node instanceof FieldNode || node instanceof PropertyNode
                || node instanceof ClassNode || node instanceof Parameter)) {
            return null;
        }
        if (target == Target.TYPE && node instanceof FieldNode field) {
            return field.getType();
        }
        if (target == Target.TYPE && node instanceof Parameter parameter) {
            return parameter.getType();
        }
        return node;
    }

    /**
     * Best-effort single target for hover and similar read-only features.
     * Prefers {@link MethodCallExpression#getMethodTarget()}; otherwise the
     * unique method on the receiver (or enclosing class for implicit
     * {@code this}) whose arity matches. Returns {@code null} when several
     * methods match — callers must not treat that as a rename target.
     *
     * @param call a method call
     * @param snapshot latest compile
     * @return one method, or {@code null}
     */


    static URI uriOf(final ASTNode dest, final URI fallback, final CompilationSnapshot snapshot,
                     final ModuleNode current) {
        ModuleNode module = moduleOf(dest);
        if (module != null && module.getContext() != null) {
            try {
                return Uris.parse(module.getContext().getName());
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        if (current != null && current.getContext() != null) {
            try {
                return Uris.parse(current.getContext().getName());
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        URI fromSnapshot = uriOfClass(dest, snapshot);
        return fromSnapshot == null ? fallback : fromSnapshot;
    }

    private static URI uriOfClass(final ASTNode dest, final CompilationSnapshot snapshot) {
        if (snapshot == null || !(dest instanceof ClassNode classNode)) {
            return null;
        }
        for (CompiledDocument document : snapshot.documents()) {
            URI uri = uriIfDeclares(document, classNode);
            if (uri != null) {
                return uri;
            }
        }
        return null;
    }

    private static URI uriIfDeclares(final CompiledDocument document, final ClassNode classNode) {
        if (document.getModule() == null) {
            return null;
        }
        for (ClassNode other : document.getModule().getClasses()) {
            if (MethodBinding.sameType(other, classNode)) {
                return document.getUri();
            }
        }
        return null;
    }

    static ModuleNode moduleOf(final ASTNode dest) {
        if (dest instanceof ClassNode classNode) {
            return classNode.getModule();
        }
        if (dest instanceof AnnotatedNode annotated && annotated.getDeclaringClass() != null) {
            return annotated.getDeclaringClass().getModule();
        }
        return null;
    }

    private static String textOf(final URI uri, final CompilationSnapshot snapshot, final String fallback) {
        CompiledDocument document = snapshot.get(uri);
        return document == null ? fallback : document.getText();
    }

    private static void addLocation(final List<Location> locations, final Set<String> seen, final URI uri,
                                    final ASTNode node, final String text, final PositionEncoding encoding) {
        Range range = node instanceof AnnotatedNode annotated
                ? Positions.toNameRange(annotated, text, encoding)
                : Positions.toIdentifierRange(node, text, encoding);
        if (range == null) {
            return;
        }
        String key = uri + "|" + range.getStart().getLine() + "|" + range.getStart().getCharacter()
                + "|" + range.getEnd().getLine() + "|" + range.getEnd().getCharacter();
        if (!seen.add(key)) {
            return;
        }
        locations.add(new Location(uri.toString(), range));
    }

    private List<LocationLink> linksFor(final List<? extends ASTNode> nodes, final ASTNode origin,
                                        final TextDocument document, final CompilationSnapshot snapshot,
                                        final CompiledDocument compiled, final PositionEncoding encoding) {
        List<LocationLink> links = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (ASTNode dest : nodes) {
            LocationLink link = toLink(origin, dest, document, snapshot, compiled, encoding);
            if (link == null) {
                continue;
            }
            String key = link.getTargetUri() + "|" + link.getTargetSelectionRange();
            if (seen.add(key)) {
                links.add(link);
            }
        }
        return links;
    }

    private static LocationLink toLink(final ASTNode origin, final ASTNode dest, final TextDocument document,
                                       final CompilationSnapshot snapshot, final CompiledDocument compiled,
                                       final PositionEncoding encoding) {
        if (dest == null) {
            return null;
        }
        if (dest.getLineNumber() <= 0 || moduleOf(dest) == null) {
            LocationLink java = javaLink(origin, dest, document, snapshot, encoding);
            if (java != null) {
                return java;
            }
        }
        if (dest.getLineNumber() <= 0) {
            return null;
        }
        URI uri = uriOf(dest, document.getUri(), snapshot, compiled.getModule());
        Range originRange = Positions.toIdentifierRange(origin, document.getText(), encoding);
        String destText = textOf(uri, snapshot, compiled.getText());
        Range targetRange = Positions.toRange(dest, destText, encoding);
        Range selection = Positions.toIdentifierRange(dest, destText, encoding);
        if (targetRange == null || selection == null) {
            return null;
        }
        LocationLink link = new LocationLink();
        link.setOriginSelectionRange(originRange);
        link.setTargetUri(uri.toString());
        link.setTargetRange(targetRange);
        link.setTargetSelectionRange(selection);
        return link;
    }

    private static List<LocationLink> javaIdentifierLinks(final TextDocument document,
                                                          final CompilationSnapshot snapshot,
                                                          final Position position, final PositionEncoding encoding) {
        if (document == null || snapshot == null || position == null) {
            return List.of();
        }
        String name = identifierAt(document, position, encoding);
        if (name.isEmpty()) {
            return List.of();
        }
        List<LocationLink> links = new ArrayList<>();
        addGroovyTypeLinks(links, document, snapshot, position, encoding, name);
        addJavaTypeLinks(links, document, snapshot, position, encoding, name);
        return links;
    }

    private static String identifierAt(final TextDocument document, final Position position,
                                       final PositionEncoding encoding) {
        int[] groovy = document.toGroovy(position, encoding);
        String line = Positions.lineText(document.getText(), groovy[0]);
        int offset = Positions.groovyColumnToCharacter(line, groovy[1], encoding);
        String name = Identifiers.wordAround(line, offset);
        if (name == null || name.isEmpty()) {
            name = CompletionService.prefixAt(document, position, encoding);
        }
        return name == null ? "" : name;
    }

    private static void addGroovyTypeLinks(final List<LocationLink> links, final TextDocument document,
                                           final CompilationSnapshot snapshot, final Position position,
                                           final PositionEncoding encoding, final String name) {
        TypeIndex.TypeHit groovyType = snapshot.types().uniqueBySimpleName(name);
        if (groovyType == null) {
            return;
        }
        CompiledDocument dest = snapshot.get(groovyType.uri());
        if (dest == null || dest.getModule() == null) {
            return;
        }
        for (ClassNode classNode : dest.getModule().getClasses()) {
            if (name.equals(classNode.getNameWithoutPackage()) && classNode.getLineNumber() > 0) {
                addLink(links, javaSourceLink(document, position, encoding, new JavaTarget(groovyType.uri(),
                        dest.getText(), classNode.getLineNumber(), classNode.getColumnNumber(),
                        classNode.getLastLineNumber(), classNode.getLastColumnNumber())));
            }
        }
    }

    private static void addJavaTypeLinks(final List<LocationLink> links, final TextDocument document,
                                         final CompilationSnapshot snapshot, final Position position,
                                         final PositionEncoding encoding, final String name) {
        for (JavaSymbolIndex.JavaSymbol symbol : snapshot.javaSymbols().typesNamed(name)) {
            addLink(links, javaSourceLink(document, position, encoding, new JavaTarget(symbol.uri(),
                    textOf(symbol.uri(), snapshot, document.getText()),
                    symbol.line(), symbol.column(), symbol.endLine(), symbol.endColumn())));
        }
    }

    private static void addLink(final List<LocationLink> links, final LocationLink link) {
        if (link != null) {
            links.add(link);
        }
    }

    private static LocationLink javaLink(final ASTNode origin, final ASTNode dest, final TextDocument document,
                                         final CompilationSnapshot snapshot, final PositionEncoding encoding) {
        JavaSymbolIndex.JavaSymbol symbol = javaSymbolOf(dest, snapshot);
        if (symbol == null) {
            return null;
        }
        Range originRange = Positions.toIdentifierRange(origin, document.getText(), encoding);
        Position caret = originRange == null ? new Position(0, 0) : originRange.getStart();
        return javaSourceLink(document, caret, encoding, new JavaTarget(symbol.uri(),
                textOf(symbol.uri(), snapshot, document.getText()),
                symbol.line(), symbol.column(), symbol.endLine(), symbol.endColumn()));
    }

    private static JavaSymbolIndex.JavaSymbol javaSymbolOf(final ASTNode dest, final CompilationSnapshot snapshot) {
        if (snapshot == null || dest == null) {
            return null;
        }
        JavaSymbolIndex index = snapshot.javaSymbols();
        if (dest instanceof ClassNode classNode) {
            return index.type(classNode.getName());
        }
        if (dest instanceof MethodNode method && method.getDeclaringClass() != null) {
            List<JavaSymbolIndex.JavaSymbol> hits = index.members(method.getDeclaringClass().getName(), method.getName());
            return hits.isEmpty() ? null : hits.get(0);
        }
        if (dest instanceof FieldNode field && field.getDeclaringClass() != null) {
            List<JavaSymbolIndex.JavaSymbol> hits = index.members(field.getDeclaringClass().getName(), field.getName());
            return hits.isEmpty() ? null : hits.get(0);
        }
        return null;
    }

    private static LocationLink javaSourceLink(final TextDocument origin, final Position position,
                                               final PositionEncoding encoding, final JavaTarget dest) {
        if (origin == null || dest == null || dest.uri() == null || dest.text() == null) {
            return null;
        }
        int endLine = dest.endLine() <= 0 ? dest.line() : dest.endLine();
        int endColumn = dest.endColumn() <= 0 ? dest.column() + 1 : dest.endColumn();
        Range target = Positions.toRange(dest.line(), dest.column(), endLine, endColumn, dest.text(), encoding);
        if (target == null) {
            return null;
        }
        LocationLink link = new LocationLink();
        link.setOriginSelectionRange(new Range(position, position));
        link.setTargetUri(dest.uri().toString());
        link.setTargetRange(target);
        link.setTargetSelectionRange(target);
        return link;
    }

    private record ReferenceWalk(List<Location> locations, Set<String> seen, SymbolIdentity identity,
                                 CompilationSnapshot snapshot, PositionEncoding encoding, boolean includeDeclaration) {
    }

    private record JavaTarget(URI uri, String text, int line, int column, int endLine, int endColumn) {
    }


    private enum Target {
        DEFINITION, TYPE, IMPLEMENTATION
    }
}
