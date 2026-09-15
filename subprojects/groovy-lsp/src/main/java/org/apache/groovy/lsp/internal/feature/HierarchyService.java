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
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.query.AstContext;
import org.eclipse.lsp4j.CallHierarchyIncomingCall;
import org.eclipse.lsp4j.CallHierarchyItem;
import org.eclipse.lsp4j.CallHierarchyOutgoingCall;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TypeHierarchyItem;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Call hierarchy and type hierarchy over the compiled snapshot.
 */
public final class HierarchyService {

    public List<CallHierarchyItem> prepareCallHierarchy(final TextDocument document,
                                                        final CompilationSnapshot snapshot,
                                                        final Position position,
                                                        final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), groovy[0], groovy[1]);
        MethodNode method = methodOf(node, snapshot);
        if (method == null) {
            return List.of();
        }
        CallHierarchyItem item = callItem(method, document.getUri(), compiled.getText(), encoding);
        return item == null ? List.of() : List.of(item);
    }

    public List<CallHierarchyIncomingCall> incomingCalls(final CallHierarchyItem item,
                                                         final CompilationSnapshot snapshot,
                                                         final PositionEncoding encoding) {
        MethodNode method = methodFromItem(item, snapshot);
        if (method == null) {
            return List.of();
        }
        Map<String, Incoming> grouped = new LinkedHashMap<>();
        for (CompiledDocument document : snapshot.documents()) {
            collectIncomingCalls(grouped, document, method, snapshot, encoding);
        }
        List<CallHierarchyIncomingCall> result = new ArrayList<>();
        for (Incoming incoming : grouped.values()) {
            if (incoming.item != null) {
                result.add(new CallHierarchyIncomingCall(incoming.item, incoming.ranges));
            }
        }
        return result;
    }

    public List<CallHierarchyOutgoingCall> outgoingCalls(final CallHierarchyItem item,
                                                         final CompilationSnapshot snapshot,
                                                         final PositionEncoding encoding) {
        MethodNode method = methodFromItem(item, snapshot);
        if (method == null || method.getCode() == null) {
            return List.of();
        }
        Map<String, Outgoing> grouped = new LinkedHashMap<>();
        String bodyText = textOf(method, snapshot);
        AstQuery.walk(method.getCode(), (node, ctx) -> {
                    MethodNode target = targetOf(node, snapshot);
                    if (target == null || target.getLineNumber() <= 0) {
                        return;
                    }
                    Range range = Positions.toIdentifierRange(node, bodyText, encoding);
                    if (range == null) {
                        return;
                    }
                    URI uri = NavigationService.uriOf(target, uriOf(item), snapshot, target.getDeclaringClass() == null
                            ? null : target.getDeclaringClass().getModule());
                    String key = SymbolIdentity.keyOf(target);
                    Outgoing outgoing = grouped.computeIfAbsent(key, ignored -> {
                        Outgoing created = new Outgoing();
                        created.item = callItem(target, uri, textOf(target, snapshot), encoding);
                        created.ranges = new ArrayList<>();
                        return created;
                    });
                    outgoing.ranges.add(range);
                });
        List<CallHierarchyOutgoingCall> result = new ArrayList<>();
        for (Outgoing outgoing : grouped.values()) {
            if (outgoing.item != null) {
                result.add(new CallHierarchyOutgoingCall(outgoing.item, outgoing.ranges));
            }
        }
        return result;
    }

    public List<TypeHierarchyItem> prepareTypeHierarchy(final TextDocument document,
                                                        final CompilationSnapshot snapshot,
                                                        final Position position,
                                                        final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document.getUri());
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.nodeAt(compiled.getModule(), groovy[0], groovy[1]);
        ClassNode type = typeOf(node);
        if (type == null || type.getLineNumber() <= 0) {
            return List.of();
        }
        TypeHierarchyItem item = typeItem(type, document.getUri(), compiled.getText(), encoding);
        return item == null ? List.of() : List.of(item);
    }

    public List<TypeHierarchyItem> supertypes(final TypeHierarchyItem item, final CompilationSnapshot snapshot,
                                              final PositionEncoding encoding) {
        ClassNode type = typeFromItem(item, snapshot);
        if (type == null) {
            return List.of();
        }
        List<TypeHierarchyItem> result = new ArrayList<>();
        ClassNode superClass = type.getSuperClass();
        if (superClass != null && !ClassHelper.isObjectType(superClass)) {
            addType(result, superClass, snapshot, encoding);
        }
        if (type.getInterfaces() != null) {
            for (ClassNode iface : type.getInterfaces()) {
                addType(result, iface, snapshot, encoding);
            }
        }
        return result;
    }

    public List<TypeHierarchyItem> subtypes(final TypeHierarchyItem item, final CompilationSnapshot snapshot,
                                            final PositionEncoding encoding) {
        ClassNode type = typeFromItem(item, snapshot);
        if (type == null) {
            return List.of();
        }
        List<TypeHierarchyItem> result = new ArrayList<>();
        for (ClassNode impl : NavigationService.implementations(type, snapshot)) {
            addType(result, impl, snapshot, encoding);
        }
        return result;
    }

    private static void addType(final List<TypeHierarchyItem> result, final ClassNode type,
                                final CompilationSnapshot snapshot, final PositionEncoding encoding) {
        if (type == null || type.getLineNumber() <= 0) {
            return;
        }
        URI uri = NavigationService.uriOf(type, null, snapshot, type.getModule());
        if (uri == null) {
            return;
        }
        String text = textOf(type, snapshot);
        TypeHierarchyItem item = typeItem(type, uri, text, encoding);
        if (item != null) {
            result.add(item);
        }
    }

    private static void collectIncomingCalls(final Map<String, Incoming> grouped, final CompiledDocument document,
                                             final MethodNode method, final CompilationSnapshot snapshot,
                                             final PositionEncoding encoding) {
        if (document.getModule() == null) {
            return;
        }
        AstQuery.walk(document.getModule(), (node, ctx) ->
                recordIncoming(grouped, document, method, snapshot, encoding, node, ctx));
    }

    private static void recordIncoming(final Map<String, Incoming> grouped, final CompiledDocument document,
                                       final MethodNode method, final CompilationSnapshot snapshot,
                                       final PositionEncoding encoding, final ASTNode node, final AstContext ctx) {
        if (!isCallTo(node, method, ctx, snapshot)) {
            return;
        }
        MethodNode enclosing = ctx.enclosingMethod();
        if (enclosing == null || enclosing.getLineNumber() <= 0) {
            return;
        }
        Range range = Positions.toIdentifierRange(node, document.getText(), encoding);
        if (range == null) {
            return;
        }
        Incoming incoming = grouped.computeIfAbsent(SymbolIdentity.keyOf(enclosing), ignored -> {
            Incoming created = new Incoming();
            created.item = callItem(enclosing, document.getUri(), document.getText(), encoding);
            created.ranges = new ArrayList<>();
            return created;
        });
        incoming.ranges.add(range);
    }

    private static MethodNode methodOf(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof MethodNode method) {
            return method;
        }
        if (node instanceof MethodCallExpression call) {
            return NavigationService.resolveMethod(call, snapshot);
        }
        if (node instanceof StaticMethodCallExpression call) {
            return NavigationService.resolveStatic(call, snapshot);
        }
        return null;
    }

    private static ClassNode typeOf(final ASTNode node) {
        if (node instanceof ClassNode classNode) {
            return classNode;
        }
        return null;
    }

    private static MethodNode targetOf(final ASTNode node, final CompilationSnapshot snapshot) {
        if (node instanceof MethodCallExpression call) {
            return NavigationService.resolveMethod(call, snapshot);
        }
        if (node instanceof StaticMethodCallExpression call) {
            return NavigationService.resolveStatic(call, snapshot);
        }
        if (node instanceof ConstructorCallExpression ctor && ctor.getType() != null) {
            List<MethodNode> ctors = List.copyOf(ctor.getType().getDeclaredConstructors());
            return ctors.isEmpty() ? null : ctors.get(0);
        }
        return null;
    }

    private static boolean isCallTo(final ASTNode node, final MethodNode method, final AstContext ctx,
                                    final CompilationSnapshot snapshot) {
        if (node instanceof MethodCallExpression call) {
            return NavigationService.callMatches(call, method, ctx);
        }
        if (node instanceof StaticMethodCallExpression call) {
            return NavigationService.staticCallMatches(call, method);
        }
        return false;
    }

    private static CallHierarchyItem callItem(final MethodNode method, final URI uri, final String text,
                                              final PositionEncoding encoding) {
        if (method == null || uri == null) {
            return null;
        }
        Range range = Positions.toRange(method, text, encoding);
        Range selection = Positions.toIdentifierRange(method, text, encoding);
        if (range == null || selection == null) {
            return null;
        }
        CallHierarchyItem item = new CallHierarchyItem(method.getName(), SymbolService.kindOf(method),
                uri.toString(), range, selection);
        item.setData(payload(method));
        return item;
    }

    private static TypeHierarchyItem typeItem(final ClassNode type, final URI uri, final String text,
                                              final PositionEncoding encoding) {
        Range range = Positions.toRange(type, text, encoding);
        Range selection = Positions.toIdentifierRange(type, text, encoding);
        if (range == null || selection == null) {
            return null;
        }
        SymbolKind kind = SymbolService.kindOf(type);
        TypeHierarchyItem item = new TypeHierarchyItem(type.getNameWithoutPackage(), kind, uri.toString(), range, selection);
        item.setData(Map.of("kind", "type", "name", type.getName()));
        return item;
    }

    private static Map<String, String> payload(final MethodNode method) {
        String owner = method.getDeclaringClass() == null ? "" : method.getDeclaringClass().getName();
        String descriptor = method.getTypeDescriptor() == null ? "" : method.getTypeDescriptor();
        return Map.of("kind", "method", "name", method.getName(), "owner", owner, "desc", descriptor);
    }

    private static MethodNode methodFromItem(final CallHierarchyItem item, final CompilationSnapshot snapshot) {
        if (item == null || snapshot == null) {
            return null;
        }
        Map<String, String> data = stringMap(item.getData());
        if (data.isEmpty()) {
            return findMethod(snapshot, item.getName(), null, null);
        }
        return findMethod(snapshot, data.get("name"), data.get("owner"), data.get("desc"));
    }

    private static ClassNode typeFromItem(final TypeHierarchyItem item, final CompilationSnapshot snapshot) {
        if (item == null || snapshot == null) {
            return null;
        }
        Map<String, String> data = stringMap(item.getData());
        String name = data.getOrDefault("name", item.getName());
        for (CompiledDocument document : snapshot.documents()) {
            if (document.getModule() == null) {
                continue;
            }
            for (ClassNode classNode : document.getModule().getClasses()) {
                if (name.equals(classNode.getName()) || name.equals(classNode.getNameWithoutPackage())) {
                    return classNode;
                }
            }
        }
        return null;
    }

    private static MethodNode findMethod(final CompilationSnapshot snapshot, final String name,
                                         final String owner, final String descriptor) {
        if (name == null) {
            return null;
        }
        MethodNode fallback = null;
        for (CompiledDocument document : snapshot.documents()) {
            MethodNode match = findMethodIn(document, name, owner, descriptor);
            if (match != null && descriptor != null && !descriptor.isEmpty()
                    && descriptor.equals(match.getTypeDescriptor())) {
                return match;
            }
            if (match != null) {
                fallback = match;
            }
        }
        return fallback;
    }

    private static MethodNode findMethodIn(final CompiledDocument document, final String name,
                                           final String owner, final String descriptor) {
        if (document.getModule() == null) {
            return null;
        }
        MethodNode fallback = null;
        for (ClassNode classNode : document.getModule().getClasses()) {
            if (owner != null && !owner.isEmpty() && !owner.equals(classNode.getName())) {
                continue;
            }
            for (MethodNode method : classNode.getMethods()) {
                if (name.equals(method.getName()) && method.getLineNumber() > 0) {
                    if (descriptor != null && !descriptor.isEmpty()
                            && descriptor.equals(method.getTypeDescriptor())) {
                        return method;
                    }
                    fallback = method;
                }
            }
        }
        return fallback;
    }

    private static String textOf(final ASTNode node, final CompilationSnapshot snapshot) {
        URI uri = NavigationService.uriOf(node, null, snapshot, moduleOf(node));
        if (uri == null) {
            return "";
        }
        CompiledDocument document = snapshot.get(uri);
        return document == null ? "" : document.getText();
    }

    private static URI uriOf(final CallHierarchyItem item) {
        try {
            return item.getUri() == null ? null : Uris.parse(item.getUri());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static ModuleNode moduleOf(final ASTNode node) {
        if (node instanceof MethodNode method && method.getDeclaringClass() != null) {
            return method.getDeclaringClass().getModule();
        }
        if (node instanceof ClassNode classNode) {
            return classNode.getModule();
        }
        return null;
    }

    private static Map<String, String> stringMap(final Object data) {
        if (!(data instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return result;
    }

    private static final class Incoming {
        private CallHierarchyItem item;
        private List<Range> ranges;
    }

    private static final class Outgoing {
        private CallHierarchyItem item;
        private List<Range> ranges;
    }
}
