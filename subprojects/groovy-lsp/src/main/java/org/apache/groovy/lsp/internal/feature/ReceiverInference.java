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
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The type on the left of a member-access dot, and the members of
 * that type. An unfinished statement is typed from declarations,
 * then from the AST.
 */
final class ReceiverInference {

    static final String LENGTH = "length";

    /**
     * Receiver of a member completion. {@code staticAccess} is true when the
     * text names a class ({@code String.}) rather than a value.
     */
    static final class Receiver {
        final ClassNode type;
        final boolean staticAccess;

        Receiver(final ClassNode type, final boolean staticAccess) {
            this.type = type == null ? ClassHelper.OBJECT_TYPE : type;
            this.staticAccess = staticAccess;
        }
    }

    static Receiver resolveReceiver(final TextDocument document, final SourceUnit unit,
                                            final ModuleNode module, final Position position,
                                            final PositionEncoding encoding) {
        int[] groovy = document.toGroovy(position, encoding);
        String line = Positions.lineText(document.getText(), groovy[0]);
        int caret = CompletionService.caretOffset(line, groovy[1]);
        List<String> parts = receiverParts(line, caret);
        ClassNode type = null;
        boolean staticAccess = false;
        if (!parts.isEmpty()) {
            type = declaredType(module, parts.get(0), groovy[0], groovy[1]);
            type = concretize(type, module, unit);
            if (!CompletionService.usable(type)) {
                type = resolveSimpleName(parts.get(0), module, unit);
                staticAccess = CompletionService.usable(type);
            }
            for (int i = 1; i < parts.size() && CompletionService.usable(type); i++) {
                type = concretize(memberType(type, parts.get(i)), module, unit);
                staticAccess = false;
            }
        }
        if (!CompletionService.usable(type)) {
            int end = receiverEndOffset(line, caret);
            int column = end <= 0 ? 1 : line.codePointCount(0, end);
            type = concretize(expressionAt(module, groovy[0], column), module, unit);
            staticAccess = false;
        }
        if (!CompletionService.usable(type)) {
            return new Receiver(ClassHelper.OBJECT_TYPE, false);
        }
        return new Receiver(type, staticAccess);
    }

    /**
     * Dotted names before the member-access dot, so {@code a.b.} is
     * {@code [a, b]}. Parentheses and index expressions return an empty list
     * and are typed from the AST instead.
     */
    static List<String> receiverParts(final String line, final int caret) {
        int end = receiverEndOffset(line, caret);
        List<String> parts = new ArrayList<>();
        int index = end;
        while (index > 0) {
            int start = CompletionService.skipIdentifierBack(line, index);
            if (start == index) {
                return parts;
            }
            parts.add(0, line.substring(start, index));
            index = CompletionService.skipWhitespaceBack(line, start);
            if (index <= 0 || line.charAt(index - 1) != '.') {
                return parts;
            }
            index -= 1;
            if (index > 0) {
                char before = line.charAt(index - 1);
                if (before == '?' || before == '*') {
                    index -= 1;
                }
            }
            index = CompletionService.skipWhitespaceBack(line, index);
        }
        return parts;
    }

    /** Exclusive UTF-16 end of the receiver expression, before the trigger dot. */
    static int receiverEndOffset(final String line, final int caret) {
        int end = CompletionService.skipWhitespaceBack(line, caret);
        end = CompletionService.skipIdentifierBack(line, end);
        end = CompletionService.skipWhitespaceBack(line, end);
        if (end <= 0 || line.charAt(end - 1) != '.') {
            return end;
        }
        end -= 1;
        if (end > 0) {
            char before = line.charAt(end - 1);
            if (before == '?' || before == '*') {
                end -= 1;
            }
        }
        return CompletionService.skipWhitespaceBack(line, end);
    }

    static ClassNode expressionAt(final ModuleNode module, final int line, final int column) {
        for (ASTNode node : AstQuery.containing(module, line, column)) {
            ClassNode type = expressionType(node, module);
            if (CompletionService.usable(type)) {
                return type;
            }
        }
        return null;
    }

    static ClassNode expressionType(final ASTNode node, final ModuleNode module) {
        if (node instanceof VariableExpression variable) {
            return TypeInference.of(variable, module);
        }
        if (node instanceof ConstructorCallExpression constructor) {
            return constructor.getType();
        }
        if (node instanceof MethodCallExpression call) {
            return call.getType();
        }
        if (node instanceof ClassExpression classExpression) {
            return classExpression.getType();
        }
        if (node instanceof ConstantExpression constant) {
            return constant.getType();
        }
        if (node instanceof PropertyExpression property) {
            ClassNode type = property.getType();
            return CompletionService.usable(type) ? type : expressionType(property.getObjectExpression(), module);
        }
        return null;
    }

    static ClassNode declaredType(final ModuleNode module, final String name, final int line, final int column) {
        if (module == null || name == null || name.isEmpty()) {
            return null;
        }
        ClassNode[] best = new ClassNode[1];
        int[] rank = {Integer.MIN_VALUE};
        int caretRank = line * 1_000_000 + Math.max(column, 0);
        AstQuery.walk(module, (node, ctx) -> {
            if (node instanceof DeclarationExpression declaration) {
                offerDeclaration(declaration, module, name, caretRank, best, rank);
            } else if (node instanceof BinaryExpression binary) {
                offerAssignment(binary, name, caretRank, best, rank);
            } else if (node instanceof Parameter parameter && name.equals(parameter.getName())) {
                offer(parameter.getLineNumber(), parameter.getColumnNumber(), TypeInference.of(parameter),
                        caretRank, best, rank);
            } else if (node instanceof FieldNode field && name.equals(field.getName())) {
                offer(field.getLineNumber(), field.getColumnNumber(), TypeInference.of(field), caretRank, best, rank);
            }
        });
        return best[0];
    }

    static void offerDeclaration(final DeclarationExpression declaration, final ModuleNode module,
                                         final String name, final int caretRank,
                                         final ClassNode[] best, final int[] rank) {
        if (declaration.isMultipleAssignmentDeclaration()) {
            return;
        }
        VariableExpression left = declaration.getVariableExpression();
        if (left == null || !name.equals(left.getName())) {
            return;
        }
        offer(left.getLineNumber(), left.getColumnNumber(), TypeInference.of(left, module), caretRank, best, rank);
    }

    static void offerAssignment(final BinaryExpression binary, final String name, final int caretRank,
                                        final ClassNode[] best, final int[] rank) {
        if (binary instanceof DeclarationExpression || binary.getOperation() == null
                || binary.getOperation().getType() != Types.ASSIGN
                || !(binary.getLeftExpression() instanceof VariableExpression variable)
                || !name.equals(variable.getName())) {
            return;
        }
        ClassNode type = binary.getRightExpression() == null ? null : binary.getRightExpression().getType();
        offer(variable.getLineNumber(), variable.getColumnNumber(), type, caretRank, best, rank);
    }

    static void offer(final int nodeLine, final int nodeColumn, final ClassNode type,
                              final int caretRank, final ClassNode[] best, final int[] rank) {
        if (!CompletionService.usable(type) || nodeLine <= 0) {
            return;
        }
        int score = nodeLine * 1_000_000 + Math.max(nodeColumn, 0);
        if (score > caretRank || score < rank[0]) {
            return;
        }
        rank[0] = score;
        best[0] = type;
    }

    static ClassNode memberType(final ClassNode owner, final String name) {
        if (owner == null || name == null) {
            return null;
        }
        ClassNode[] found = new ClassNode[1];
        eachType(owner, current -> {
            if (found[0] != null) {
                return;
            }
            found[0] = fieldOrPropertyType(current, name);
            if (found[0] == null) {
                found[0] = zeroArgReturn(current, name);
            }
        });
        return found[0];
    }

    static ClassNode fieldOrPropertyType(final ClassNode owner, final String name) {
        for (FieldNode field : owner.getFields()) {
            if (name.equals(field.getName()) && field.isPublic()) {
                return field.getType();
            }
        }
        for (PropertyNode property : owner.getProperties()) {
            if (name.equals(property.getName()) && !Modifier.isPrivate(property.getModifiers())) {
                return property.getType();
            }
        }
        return null;
    }

    static ClassNode zeroArgReturn(final ClassNode owner, final String name) {
        String suffix = name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (MethodNode method : owner.getMethods()) {
            if (method.getParameters().length != 0 || !method.isPublic() || method.getName() == null) {
                continue;
            }
            String methodName = method.getName();
            if (name.equals(methodName) || ("get" + suffix).equals(methodName) || ("is" + suffix).equals(methodName)) {
                return method.getReturnType();
            }
        }
        return null;
    }

    static void addVisibleMembers(final List<CompletionItem> items, final ClassNode receiver,
                                          final String prefix, final boolean staticAccess) {
        ClassNode type = receiver == null ? ClassHelper.OBJECT_TYPE : receiver;
        if (!staticAccess && ClassHelper.isPrimitiveType(type)) {
            type = ClassHelper.getWrapper(type);
        }
        Set<String> seen = new LinkedHashSet<>();
        if (type.isArray() && !staticAccess && CompletionService.prefixMatches(LENGTH, prefix)) {
            CompletionItem length = new CompletionItem(LENGTH);
            length.setKind(CompletionItemKind.Field);
            length.setDetail("int");
            length.setSortText("2" + LENGTH);
            items.add(length);
            seen.add(LENGTH);
        }
        eachType(type, current -> {
            for (MethodNode method : current.getMethods()) {
                if (method.isSynthetic() || !method.isPublic() || method.getName() == null
                        || method.getName().startsWith("<") || method.getName().indexOf('$') >= 0) {
                    continue;
                }
                CompletionService.addMethod(items, seen, method, prefix);
            }
            addFieldsAndProperties(items, seen, current, prefix);
        });
    }

    static void addFieldsAndProperties(final List<CompletionItem> items, final Set<String> seen,
                                               final ClassNode owner, final String prefix) {
        for (FieldNode field : owner.getFields()) {
            if (field.isSynthetic() || !field.isPublic() || field.getName() == null
                    || !CompletionService.prefixMatches(field.getName(), prefix) || !seen.add(field.getName())) {
                continue;
            }
            CompletionItem item = new CompletionItem(field.getName());
            item.setKind(CompletionItemKind.Field);
            item.setDetail(CompletionService.typeName(field.getType()));
            item.setSortText("2" + field.getName());
            items.add(item);
        }
        for (PropertyNode property : owner.getProperties()) {
            if (property.isSynthetic() || property.getName() == null || Modifier.isPrivate(property.getModifiers())
                    || !CompletionService.prefixMatches(property.getName(), prefix) || !seen.add(property.getName())) {
                continue;
            }
            CompletionItem item = new CompletionItem(property.getName());
            item.setKind(CompletionItemKind.Property);
            item.setSortText("2" + property.getName());
            items.add(item);
        }
    }

    static void eachType(final ClassNode start, final Consumer<ClassNode> action) {
        ArrayDeque<ClassNode> pending = new ArrayDeque<>();
        pending.add(start);
        Set<String> seen = new LinkedHashSet<>();
        while (!pending.isEmpty()) {
            ClassNode current = pending.removeFirst();
            if (current != null) {
                ClassNode redirected = current.redirect();
                String key = redirected.getName();
                if (key == null || seen.add(key)) {
                    action.accept(redirected);
                    enqueueParents(pending, redirected);
                }
            }
        }
    }

    static void enqueueParents(final ArrayDeque<ClassNode> pending, final ClassNode type) {
        ClassNode superClass = type.getSuperClass();
        if (superClass != null) {
            pending.add(superClass);
        }
        ClassNode[] interfaces = type.getInterfaces();
        if (interfaces != null) {
            for (ClassNode iface : interfaces) {
                if (iface != null) {
                    pending.add(iface);
                }
            }
        }
    }

    static ClassNode concretize(final ClassNode type, final ModuleNode module, final SourceUnit unit) {
        if (type == null) {
            return null;
        }
        ClassNode current = type.redirect();
        if (current.isResolved() || ClassHelper.isPrimitiveType(current)) {
            return current;
        }
        String name = current.getName();
        if (name == null || name.isEmpty()) {
            return current;
        }
        if (name.indexOf('.') >= 0) {
            ClassNode loaded = loadClass(name, unit);
            if (loaded != null) {
                return loaded;
            }
            ClassNode source = sourceClass(module, name);
            return source == null ? current : source;
        }
        ClassNode simple = resolveSimpleName(name, module, unit);
        return simple == null ? current : simple;
    }

    static ClassNode resolveSimpleName(final String name, final ModuleNode module, final SourceUnit unit) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if ("BigDecimal".equals(name)) {
            return ClassHelper.BigDecimal_TYPE;
        }
        if ("BigInteger".equals(name)) {
            return ClassHelper.BigInteger_TYPE;
        }
        ClassNode source = sourceClass(module, name);
        if (source != null) {
            return source;
        }
        ClassNode imported = importedSimpleName(name, module, unit);
        if (imported != null) {
            return imported;
        }
        return defaultImport(name, unit);
    }

    static ClassNode importedSimpleName(final String name, final ModuleNode module, final SourceUnit unit) {
        if (module == null) {
            return null;
        }
        ClassNode imported = module.getImportType(name);
        if (imported != null) {
            ClassNode loaded = loadClass(imported.getName(), unit);
            return loaded == null ? imported : loaded;
        }
        for (ImportNode star : module.getStarImports()) {
            ClassNode loaded = loadClass(qualify(star.getPackageName(), name), unit);
            if (loaded != null) {
                return loaded;
            }
        }
        return null;
    }

    static ClassNode defaultImport(final String name, final SourceUnit unit) {
        for (String prefix : ResolveVisitor.DEFAULT_IMPORTS) {
            ClassNode loaded = loadClass(prefix + name, unit);
            if (loaded != null) {
                return loaded;
            }
        }
        return null;
    }

    static ClassNode sourceClass(final ModuleNode module, final String name) {
        if (module == null || name == null) {
            return null;
        }
        for (ClassNode classNode : module.getClasses()) {
            if (classNode.isScript()) {
                continue;
            }
            if (name.equals(classNode.getName()) || name.equals(classNode.getNameWithoutPackage())) {
                return classNode;
            }
        }
        return null;
    }

    static String qualify(final String packageName, final String simple) {
        if (packageName == null || packageName.isEmpty() || simple == null) {
            return null;
        }
        return packageName.endsWith(".") ? packageName + simple : packageName + "." + simple;
    }

    static ClassNode loadClass(final String name, final SourceUnit unit) {
        if (name == null || name.isEmpty() || name.endsWith(".")) {
            return null;
        }
        ClassLoader loader = unit == null ? null : unit.getClassLoader();
        if (loader == null) {
            loader = CompletionService.class.getClassLoader();
        }
        try {
            return ClassHelper.make(Class.forName(name, false, loader));
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }
}
