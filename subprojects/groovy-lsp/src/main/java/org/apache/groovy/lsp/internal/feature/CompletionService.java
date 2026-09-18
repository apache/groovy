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

import groovy.lang.groovydoc.Groovydoc;
import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.compile.TypeIndex;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.GroovyKeywords;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Keyword, snippet, member, workspace-type and GDK completions.
 * Unimported workspace types carry {@code additionalTextEdits}.
 */
public final class CompletionService {

    static final int MAX_ITEMS = 80;
    private static final List<String> GDK_NAMES = gdkMethodNames();

    /**
     * Completes at {@code position} in {@code document}.
     *
     * @param document open document
     * @param snapshot latest compile
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return a completion list (incomplete when truncated)
     */
    public CompletionList complete(final TextDocument document, final CompilationSnapshot snapshot,
                                   final Position position, final PositionEncoding encoding) {
        List<CompletionItem> items = new ArrayList<>();
        String prefix = prefixAt(document, position, encoding);
        boolean memberAccess = isMemberAccess(document, position, encoding);

        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        ModuleNode module = compiled == null ? null : compiled.getModule();

        if (!memberAccess) {
            addMatching(items, GroovyKeywords.KEYWORDS, prefix, CompletionItemKind.Keyword);
            addSnippets(items, prefix);
        }

        if (module != null) {
            addFromAst(items, document, compiled, snapshot, position, encoding, module);
        } else if (memberAccess && prefix.length() >= 2) {
            addGdk(items, prefix);
        }

        boolean incomplete = items.size() > MAX_ITEMS;
        if (incomplete) {
            items = new ArrayList<>(items.subList(0, MAX_ITEMS));
        }
        return new CompletionList(incomplete, items);
    }

    /**
     * Fills documentation from the compiled snapshot using {@code item.data}.
     *
     * @param item unresolved item
     * @param snapshot latest compile
     * @return the same item, possibly with documentation
     */
    public CompletionItem resolve(final CompletionItem item, final CompilationSnapshot snapshot) {
        if (item == null || item.getData() == null || snapshot == null) {
            return item;
        }
        Map<String, String> data = stringMap(item.getData());
        if (data.isEmpty()) {
            return item;
        }
        String kind = data.get("k");
        String name = data.get("n");
        if ("t".equals(kind) && name != null) {
            TypeIndex.TypeHit hit = snapshot.types().byName(name);
            if (hit != null && !hit.documentation().isEmpty()) {
                item.setDocumentation(Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, hit.documentation())));
            }
        }
        if ("m".equals(kind) && name != null) {
            String owner = data.get("o");
            MethodNode method = NavigationService.findMethod(snapshot, owner, name, data.get("d"));
            if (method != null) {
                String docs = documentation(method);
                if (!docs.isEmpty()) {
                    item.setDocumentation(Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, docs)));
                }
            }
        }
        return item;
    }

    static String prefixAt(final TextDocument document, final Position position,
                           final PositionEncoding encoding) {
        if (document == null || position == null) {
            return "";
        }
        int[] groovy = document.toGroovy(position, encoding);
        String line = Positions.lineText(document.getText(), groovy[0]);
        int end = caretOffset(line, groovy[1]);
        int start = skipIdentifierBack(line, end);
        return line.substring(start, end);
    }

    static boolean isMemberAccess(final TextDocument document, final Position position,
                                  final PositionEncoding encoding) {
        if (document == null || position == null) {
            return false;
        }
        int[] groovy = document.toGroovy(position, encoding);
        String line = Positions.lineText(document.getText(), groovy[0]);
        int idx = skipWhitespaceBack(line, caretOffset(line, groovy[1]));
        idx = skipIdentifierBack(line, idx);
        return idx > 0 && line.codePointBefore(idx) == '.';
    }

    private static int caretOffset(final String line, final int groovyColumn) {
        int col = Math.max(groovyColumn - 1, 0);
        int cpCount = line.codePointCount(0, line.length());
        if (col > cpCount) {
            col = cpCount;
        }
        return line.offsetByCodePoints(0, col);
    }

    private static int skipWhitespaceBack(final String line, int idx) {
        while (idx > 0 && Character.isWhitespace(line.codePointBefore(idx))) {
            idx -= Character.charCount(line.codePointBefore(idx));
        }
        return idx;
    }

    private static int skipIdentifierBack(final String line, int idx) {
        while (idx > 0) {
            int cp = line.codePointBefore(idx);
            if (!Character.isJavaIdentifierPart(cp) && cp != '$') {
                break;
            }
            idx -= Character.charCount(cp);
        }
        return idx;
    }

    private static void addOverrideCompletions(final List<CompletionItem> items, final ModuleNode module,
                                               final TextDocument document, final Position position,
                                               final PositionEncoding encoding, final String prefix) {
        ClassNode enclosing = enclosingClass(module, document, position, encoding);
        if (enclosing == null || enclosing.isInterface()) {
            return;
        }
        for (MethodNode method : CodeActionService.missingAbstracts(enclosing)) {
            if (!prefixMatches(method.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(method.getName());
            item.setKind(CompletionItemKind.Method);
            item.setDetail("override " + signature(method));
            item.setSortText("0" + method.getName());
            item.setInsertText(CodeActionService.overrideSnippet(method));
            item.setInsertTextFormat(InsertTextFormat.Snippet);
            items.add(item);
        }
    }

    private static ClassNode enclosingClass(final ModuleNode module, final TextDocument document,
                                            final Position position, final PositionEncoding encoding) {
        ClassNode enclosing = null;
        for (ASTNode node : AstQuery.containing(module, document, position, encoding)) {
            if (node instanceof ClassNode classNode) {
                enclosing = classNode;
            }
        }
        return enclosing;
    }

    private static void addNamedArgumentCompletions(final List<CompletionItem> items, final ModuleNode module,
                                                    final int line, final int column,
                                                    final CompilationSnapshot snapshot, final String prefix) {
        ASTNode node = AstQuery.enclosingCall(module, line, column);
        MethodNode target = null;
        Expression arguments = null;
        if (node instanceof MethodCallExpression call) {
            target = NavigationService.resolveMethod(call, snapshot);
            arguments = call.getArguments();
        } else if (node instanceof StaticMethodCallExpression call) {
            target = NavigationService.resolveStatic(call, snapshot);
            arguments = call.getArguments();
        } else if (node instanceof ConstructorCallExpression ctor) {
            target = NavigationService.resolveConstructor(ctor);
            arguments = ctor.getArguments();
        }
        if (target == null || target.getParameters() == null) {
            return;
        }
        Set<String> used = usedNamedKeys(arguments);
        for (Parameter parameter : target.getParameters()) {
            if (parameter == null || parameter.getName() == null || parameter.getName().startsWith("$")
                    || used.contains(parameter.getName()) || !prefixMatches(parameter.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(parameter.getName());
            item.setKind(CompletionItemKind.Field);
            item.setDetail("named argument");
            item.setSortText("0" + parameter.getName());
            item.setInsertText(parameter.getName() + ": ");
            items.add(item);
        }
    }

    private static Set<String> usedNamedKeys(final Expression arguments) {
        Set<String> used = new LinkedHashSet<>();
        if (arguments instanceof MapExpression map) {
            addMapKeys(used, map);
        } else if (arguments instanceof TupleExpression tuple) {
            for (Expression arg : tuple.getExpressions()) {
                if (arg instanceof MapExpression map) {
                    addMapKeys(used, map);
                }
            }
        }
        return used;
    }

    private static void addMapKeys(final Set<String> used, final MapExpression map) {
        for (MapEntryExpression entry : map.getMapEntryExpressions()) {
            if (entry.getKeyExpression() instanceof ConstantExpression constant
                    && constant.getValue() != null) {
                used.add(constant.getValue().toString());
            }
        }
    }

    private static ClassNode receiverOf(final ASTNode node) {
        if (node instanceof PropertyExpression property) {
            return property.getObjectExpression().getType();
        }
        if (node instanceof VariableExpression variable) {
            return variable.getType();
        }
        if (node instanceof ClassNode classNode) {
            return classNode;
        }
        return null;
    }

    private static void addLocals(final List<CompletionItem> items, final ModuleNode module, final int line,
                                  final int column, final String prefix) {
        MethodNode enclosing = AstQuery.enclosingMethod(module, line, column);
        if (enclosing == null) {
            return;
        }
        for (Parameter parameter : enclosing.getParameters()) {
            if (!prefixMatches(parameter.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(parameter.getName());
            item.setKind(CompletionItemKind.Variable);
            item.setDetail(parameter.getType().getNameWithoutPackage());
            item.setSortText("0" + parameter.getName());
            items.add(item);
        }
    }

    private static void addWorkspaceTypes(final List<CompletionItem> items, final CompiledDocument compiled,
                                          final CompilationSnapshot snapshot, final String prefix,
                                          final PositionEncoding encoding) {
        if (snapshot == null) {
            return;
        }
        ModuleNode module = compiled.getModule();
        for (TypeIndex.TypeHit hit : snapshot.types().matchingPrefix(prefix, MAX_ITEMS)) {
            CompletionItem item = new CompletionItem(hit.simpleName());
            item.setKind(typeKind(hit));
            item.setDetail(hit.name());
            item.setSortText("1" + hit.simpleName());
            item.setData(Map.of("k", "t", "n", hit.name()));
            if (ImportSupport.needsImport(module, hit.name())) {
                item.setAdditionalTextEdits(ImportSupport.addImport(compiled, hit.name(), encoding));
            }
            items.add(item);
        }
    }

    private static void addMembers(final List<CompletionItem> items, final ClassNode receiver, final String prefix) {
        Set<String> seen = new LinkedHashSet<>();
        for (MethodNode method : receiver.getMethods()) {
            addMethod(items, seen, method, prefix);
        }
        for (FieldNode field : receiver.getFields()) {
            if (field.isSynthetic() || !prefixMatches(field.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(field.getName());
            item.setKind(CompletionItemKind.Field);
            item.setDetail(field.getType() == null ? null : field.getType().getNameWithoutPackage());
            item.setSortText("2" + field.getName());
            items.add(item);
        }
        for (PropertyNode property : receiver.getProperties()) {
            if (property.isSynthetic() || !prefixMatches(property.getName(), prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(property.getName());
            item.setKind(CompletionItemKind.Property);
            item.setSortText("2" + property.getName());
            items.add(item);
        }
    }

    private static void addGdk(final List<CompletionItem> items, final String prefix) {
        for (String name : GDK_NAMES) {
            if (!prefixMatches(name, prefix)) {
                continue;
            }
            CompletionItem item = new CompletionItem(name);
            item.setKind(CompletionItemKind.Method);
            item.setDetail("GDK");
            item.setSortText("4" + name);
            items.add(item);
        }
    }

    private static List<String> gdkMethodNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Method method : DefaultGroovyMethods.class.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())
                    && method.getParameterCount() > 0) {
                names.add(method.getName());
            }
        }
        return List.copyOf(names);
    }

    private static void addSnippets(final List<CompletionItem> items, final String prefix) {
        addSnippet(items, prefix, "class", "class ${1:Name} {\n    $0\n}\n", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "interface", "interface ${1:Name} {\n    $0\n}\n", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "trait", "trait ${1:Name} {\n    $0\n}\n", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "enum", "enum ${1:Name} {\n    $0\n}\n", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "for", "for (${1:item} in ${2:items}) {\n    $0\n}", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "if", "if (${1:condition}) {\n    $0\n}", CompletionItemKind.Snippet);
        addSnippet(items, prefix, "try", "try {\n    $0\n} catch (${1:Exception} ${2:e}) {\n    \n}", CompletionItemKind.Snippet);
    }

    private static void addSnippet(final List<CompletionItem> items, final String prefix, final String label,
                                   final String insert, final CompletionItemKind kind) {
        if (!prefixMatches(label, prefix)) {
            return;
        }
        CompletionItem item = new CompletionItem(label);
        item.setKind(kind);
        item.setInsertText(insert);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setSortText("3" + label);
        items.add(item);
    }

    private static void addMatching(final List<CompletionItem> items, final List<String> words, final String prefix,
                                    final CompletionItemKind kind) {
        for (String word : words) {
            if (prefixMatches(word, prefix)) {
                CompletionItem item = new CompletionItem(word);
                item.setKind(kind);
                item.setSortText("3" + word);
                items.add(item);
            }
        }
    }

    static boolean prefixMatches(final String label, final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        return label.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }

    private static String signature(final MethodNode method) {
        StringBuilder builder = new StringBuilder();
        ClassNode type = method.getReturnType() == null ? ClassHelper.OBJECT_TYPE : method.getReturnType();
        builder.append(type.getNameWithoutPackage()).append(' ').append(method.getName()).append('(');
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameters[i].getType().getNameWithoutPackage()).append(' ').append(parameters[i].getName());
        }
        builder.append(')');
        return builder.toString();
    }

    private static String methodSnippet(final MethodNode method) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return method.getName() + "()";
        }
        StringBuilder builder = new StringBuilder(method.getName()).append('(');
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("${").append(i + 1).append(':').append(parameters[i].getName()).append('}');
        }
        return builder.append(')').toString();
    }

    private static void addFromAst(final List<CompletionItem> items, final TextDocument document,
                                   final CompiledDocument compiled, final CompilationSnapshot snapshot,
                                   final Position position, final PositionEncoding encoding,
                                   final ModuleNode module) {
        String prefix = prefixAt(document, position, encoding);
        boolean memberAccess = isMemberAccess(document, position, encoding);
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.nodeAt(module, document, position, encoding);
        ClassNode receiver = receiverOf(node);
        if (memberAccess) {
            addMembers(items, receiver == null ? ClassHelper.OBJECT_TYPE : receiver, prefix);
            if (prefix.length() >= 2) {
                addGdk(items, prefix);
            }
            return;
        }
        addLocals(items, module, groovy[0], groovy[1], prefix);
        addNamedArgumentCompletions(items, module, groovy[0], groovy[1], snapshot, prefix);
        addOverrideCompletions(items, module, document, position, encoding, prefix);
        for (ClassNode classNode : module.getClasses()) {
            addMembers(items, classNode, prefix);
        }
        addWorkspaceTypes(items, compiled, snapshot, prefix, encoding);
    }

    private static CompletionItemKind typeKind(final TypeIndex.TypeHit hit) {
        if (hit.iface()) {
            return CompletionItemKind.Interface;
        }
        if (hit.enumeration()) {
            return CompletionItemKind.Enum;
        }
        return CompletionItemKind.Class;
    }

    private static void addMethod(final List<CompletionItem> items, final Set<String> seen, final MethodNode method,
                                  final String prefix) {
        if (method.isSynthetic() || !seen.add(method.getName() + "/" + method.getParameters().length)
                || !prefixMatches(method.getName(), prefix)) {
            return;
        }
        CompletionItem item = new CompletionItem(method.getName());
        item.setKind(method.isStatic() ? CompletionItemKind.Function : CompletionItemKind.Method);
        item.setDetail(signature(method));
        item.setSortText("2" + method.getName());
        item.setInsertText(methodSnippet(method));
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        String owner = method.getDeclaringClass() == null ? "" : method.getDeclaringClass().getName();
        String descriptor = method.getTypeDescriptor() == null ? "" : method.getTypeDescriptor();
        item.setData(Map.of("k", "m", "n", method.getName(), "o", owner, "d", descriptor));
        items.add(item);
    }

    private static String documentation(final AnnotatedNode node) {
        Groovydoc groovydoc = node.getGroovydoc();
        if (groovydoc == null || !groovydoc.isPresent()) {
            return "";
        }
        String content = groovydoc.getContent();
        return content == null ? "" : content;
    }

    static Map<String, String> stringMap(final Object data) {
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
}
