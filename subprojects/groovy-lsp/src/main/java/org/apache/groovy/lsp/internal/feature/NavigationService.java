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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.query.AstContext;
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
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled != null && compiled.getModule() != null) {
            int[] groovy = document.toGroovy(position, encoding);
            ASTNode node = AstQuery.nodeAt(compiled.getModule(), groovy[0], groovy[1]);
            if (node instanceof MethodCallExpression || node instanceof StaticMethodCallExpression) {
                List<MethodNode> methods = new ArrayList<>(resolveMethodCandidates(node, snapshot));
                methods.removeIf(method -> method.getLineNumber() <= 0);
                if (!methods.isEmpty()) {
                    return linksFor(methods, node, document, snapshot, compiled, encoding);
                }
            }
        }
        return locate(document, snapshot, position, encoding, Target.DEFINITION);
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
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), groovy[0], groovy[1]);
        ASTNode dest = resolve(node, Target.IMPLEMENTATION, snapshot);
        if (dest instanceof ClassNode classNode) {
            return linksFor(implementations(classNode, snapshot), node, document, snapshot, compiled, encoding);
        }
        if (dest instanceof MethodNode method) {
            return linksFor(methodImplementations(method, snapshot), node, document, snapshot, compiled, encoding);
        }
        return locate(document, snapshot, position, encoding, Target.IMPLEMENTATION);
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
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), groovy[0], groovy[1]);
        SymbolIdentity identity = SymbolIdentity.of(node, snapshot);
        if (identity == null || identity.name() == null) {
            return List.of();
        }
        List<Location> locations = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        if (includeDeclaration) {
            addLocation(locations, seen, document.getUri(), identity.declaration(), compiled.getText(), encoding);
        }
        for (CompiledDocument other : snapshot.documents()) {
            addReferencesFrom(locations, seen, other, identity, snapshot, encoding, includeDeclaration);
        }
        return locations;
    }

    private static void addReferencesFrom(final List<Location> locations, final Set<String> seen,
                                          final CompiledDocument other, final SymbolIdentity identity,
                                          final CompilationSnapshot snapshot, final PositionEncoding encoding,
                                          final boolean includeDeclaration) {
        if (other.getModule() == null || !Identifiers.containsWord(other.getText(), identity.name())) {
            return;
        }
        AstQuery.walk(other.getModule(), (hit, ctx) -> {
            if (!includeDeclaration && identity.isDeclaration(hit)) {
                return;
            }
            if (identity.refersTo(hit, ctx, snapshot)) {
                addLocation(locations, seen, other.getUri(), hit, other.getText(), encoding);
            }
        });
    }

    private List<LocationLink> locate(final TextDocument document, final CompilationSnapshot snapshot,
                                      final Position position, final PositionEncoding encoding, final Target target) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), groovy[0], groovy[1]);
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
            return resolveStatic(call, snapshot);
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
        return resolveMethod(call, snapshot);
    }

    private static ASTNode resolvePropertyTarget(final PropertyExpression property, final Target target) {
        if (target == Target.TYPE) {
            return property.getType();
        }
        return resolveProperty(property);
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
    static MethodNode resolveMethod(final MethodCallExpression call, final CompilationSnapshot snapshot) {
        List<MethodNode> methods = resolveMethodCandidates(call, snapshot);
        return methods.size() == 1 ? methods.get(0) : null;
    }

    static MethodNode resolveStatic(final StaticMethodCallExpression call, final CompilationSnapshot snapshot) {
        List<MethodNode> methods = resolveMethodCandidates(call, snapshot);
        return methods.size() == 1 ? methods.get(0) : null;
    }

    /**
     * All arity-matching methods for definition. When {@code methodTarget}
     * is set, that is the only candidate.
     *
     * @param node a method or static call
     * @param snapshot latest compile
     * @return candidates, never {@code null}
     */
    static List<MethodNode> resolveMethodCandidates(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof MethodCallExpression call) {
            if (call.getMethodTarget() != null) {
                return List.of(call.getMethodTarget());
            }
            String name = call.getMethodAsString();
            int argc = argumentCount(call.getArguments());
            List<MethodNode> found = findMethods(typeOf(call.getObjectExpression()), name, argc);
            if (found.isEmpty() && call.isImplicitThis()) {
                found = findMethods(enclosingClass(call, snapshot), name, argc);
            }
            return found;
        }
        if (node instanceof StaticMethodCallExpression call) {
            return findMethods(call.getOwnerType(), call.getMethodAsString(), argumentCount(call.getArguments()));
        }
        return List.of();
    }

    /**
     * Whether rename/prepareRename may edit this symbol. Declarations are
     * safe. Calls and properties are safe only when the compiler bound a
     * unique target ({@code methodTarget} or a uniquely resolved field).
     * A name-and-arity heuristic is not enough: it can rewrite the wrong
     * overload in dynamically typed Groovy.
     *
     * @param node node under the caret
     * @param snapshot latest compile
     * @return {@code true} when a workspace edit is justified
     */
    static boolean isRenameSafe(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof MethodCallExpression call) {
            return call.getMethodTarget() != null;
        }
        if (node instanceof StaticMethodCallExpression call) {
            return resolveStatic(call, snapshot) != null;
        }
        if (node instanceof PropertyExpression property) {
            return resolveProperty(property) != null;
        }
        if (node instanceof VariableExpression variable) {
            Variable accessed = variable.getAccessedVariable();
            return accessed instanceof ASTNode ast && ast.getLineNumber() > 0;
        }
        return node instanceof ClassNode || node instanceof MethodNode || node instanceof FieldNode
                || node instanceof PropertyNode || node instanceof Parameter;
    }

    static ASTNode resolveProperty(final PropertyExpression property) {
        if (property == null) {
            return null;
        }
        String name = property.getPropertyAsString();
        ClassNode type = typeOf(property.getObjectExpression());
        if (name == null || !SymbolIdentity.isResolvedType(type)) {
            return null;
        }
        FieldNode field = type.getField(name);
        if (field != null && !field.isSynthetic()) {
            return field;
        }
        PropertyNode prop = type.getProperty(name);
        if (prop != null && !prop.isSynthetic()) {
            return prop;
        }
        return null;
    }

    static boolean callMatches(final MethodCallExpression call, final MethodNode method,
                               final AstContext context) {
        if (call == null || method == null || !method.getName().equals(call.getMethodAsString())) {
            return false;
        }
        MethodNode target = call.getMethodTarget();
        if (target != null) {
            return SymbolIdentity.keyOf(target).equals(SymbolIdentity.keyOf(method));
        }
        ClassNode receiver = typeOf(call.getObjectExpression());
        ClassNode owner = method.getDeclaringClass();
        if (SymbolIdentity.isResolvedType(receiver) && owner != null) {
            return sameOrDerived(receiver, owner);
        }
        if (call.isImplicitThis() && context != null && context.enclosingClass() != null && owner != null) {
            return sameOrDerived(context.enclosingClass(), owner);
        }
        return false;
    }

    static boolean staticCallMatches(final StaticMethodCallExpression call, final MethodNode method) {
        if (call == null || method == null || !method.getName().equals(call.getMethodAsString())) {
            return false;
        }
        ClassNode owner = method.getDeclaringClass();
        return owner != null && sameType(owner, call.getOwnerType());
    }

    static boolean receiverMatchesOwner(final Expression receiver, final String ownerName) {
        if (ownerName == null || ownerName.isEmpty()) {
            return false;
        }
        ClassNode type = typeOf(receiver);
        return SymbolIdentity.isResolvedType(type) && ownerName.equals(type.getName());
    }

    static List<ClassNode> implementations(final ClassNode type, final CompilationSnapshot snapshot) {
        List<ClassNode> result = new ArrayList<>();
        if (snapshot == null || type == null) {
            return result;
        }
        for (CompiledDocument document : snapshot.documents()) {
            if (document.getModule() == null) {
                continue;
            }
            for (ClassNode classNode : document.getModule().getClasses()) {
                if (classNode == type || sameType(classNode, type) || classNode.getLineNumber() <= 0) {
                    continue;
                }
                if (implementsOrExtends(classNode, type)) {
                    result.add(classNode);
                }
            }
        }
        return result;
    }

    static List<MethodNode> methodImplementations(final MethodNode method, final CompilationSnapshot snapshot) {
        List<MethodNode> result = new ArrayList<>();
        if (method == null || method.getDeclaringClass() == null) {
            return result;
        }
        for (ClassNode classNode : implementations(method.getDeclaringClass(), snapshot)) {
            MethodNode override = classNode.getDeclaredMethod(method.getName(), method.getParameters());
            if (override == null) {
                override = classNode.getMethod(method.getName(), method.getParameters());
            }
            if (override != null && override.getLineNumber() > 0 && override != method) {
                result.add(override);
            }
        }
        return result;
    }

    static String nameOf(final ASTNode node) {
        return Identifiers.nameOf(node);
    }

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
            if (sameType(other, classNode)) {
                return document.getUri();
            }
        }
        return null;
    }

    private static ModuleNode moduleOf(final ASTNode dest) {
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
        if (dest == null || dest.getLineNumber() <= 0) {
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

    private static List<MethodNode> findMethods(final ClassNode type, final String name, final int argc) {
        List<MethodNode> matches = new ArrayList<>();
        if (!SymbolIdentity.isResolvedType(type) || name == null) {
            return matches;
        }
        for (MethodNode method : type.getMethods(name)) {
            if (method.isSynthetic()) {
                continue;
            }
            int count = method.getParameters() == null ? 0 : method.getParameters().length;
            if (count == argc) {
                matches.add(method);
            }
        }
        return matches;
    }

    private static ClassNode enclosingClass(final MethodCallExpression call, final CompilationSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        ClassNode[] found = new ClassNode[1];
        for (CompiledDocument document : snapshot.documents()) {
            if (document.getModule() == null) {
                continue;
            }
            AstQuery.walk(document.getModule(), (node, ctx) -> {
                if (node == call && ctx.enclosingClass() != null) {
                    found[0] = ctx.enclosingClass();
                }
            });
            if (found[0] != null) {
                return found[0];
            }
        }
        return null;
    }

    static List<MethodNode> overloads(final ClassNode type, final String name) {
        List<MethodNode> result = new ArrayList<>();
        if (type == null || name == null) {
            return result;
        }
        for (MethodNode method : type.getMethods(name)) {
            if (!method.isSynthetic()) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * All visible overloads of a call, including implicit {@code this}.
     * Signature help lists every overload; it must not require a unique match.
     *
     * @param call a method call
     * @param snapshot latest compile
     * @return overloads, possibly empty
     */
    static List<MethodNode> overloads(final MethodCallExpression call, final CompilationSnapshot snapshot) {
        if (call == null) {
            return List.of();
        }
        List<MethodNode> found = overloads(typeOf(call.getObjectExpression()), call.getMethodAsString());
        if (found.isEmpty() && call.isImplicitThis()) {
            found = overloads(enclosingClass(call, snapshot), call.getMethodAsString());
        }
        return found;
    }

    static int argumentCount(final Expression arguments) {
        if (arguments instanceof TupleExpression tuple) {
            return tuple.getExpressions().size();
        }
        return arguments == null ? 0 : 1;
    }

    private static ClassNode typeOf(final Expression expression) {
        return expression == null ? null : expression.getType();
    }

    static boolean sameType(final ClassNode left, final ClassNode right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        return left.getName() != null && left.getName().equals(right.getName());
    }

    static boolean sameOrDerived(final ClassNode receiver, final ClassNode owner) {
        if (sameType(receiver, owner)) {
            return true;
        }
        return implementsOrExtends(receiver, owner);
    }

    private static boolean implementsOrExtends(final ClassNode candidate, final ClassNode type) {
        if (candidate == null || type == null || sameType(candidate, type)) {
            return false;
        }
        if (sameType(candidate.getSuperClass(), type)) {
            return true;
        }
        if (candidate.isDerivedFrom(type)) {
            return true;
        }
        return candidate.implementsInterface(type);
    }

    private enum Target {
        DEFINITION, TYPE, IMPLEMENTATION
    }
}
