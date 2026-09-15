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

import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.query.AstContext;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Position queries over a {@link ModuleNode}, built on the compiler's
 * {@link org.codehaus.groovy.ast.query.AstQuery}.
 */
public final class AstQuery {

    private AstQuery() {
    }

    /**
     * Returns the innermost node at {@code (line, column)}, or {@code null}.
     *
     * @param module module AST
     * @param line 1-based line
     * @param column 1-based column
     * @return the hit, or {@code null}
     */
    public static ASTNode nodeAt(final ModuleNode module, final int line, final int column) {
        if (module == null) {
            return null;
        }
        Hit best = new Hit();
        consider(best, module.getPackage(), null, line, column);
        for (ImportNode imp : module.getImports()) {
            consider(best, imp, null, line, column);
        }
        for (ImportNode imp : module.getStarImports()) {
            consider(best, imp, null, line, column);
        }
        for (ImportNode imp : module.getStaticImports().values()) {
            consider(best, imp, null, line, column);
        }
        for (ImportNode imp : module.getStaticStarImports().values()) {
            consider(best, imp, null, line, column);
        }
        walk(module, (node, ctx) -> consider(best, node, ctx.parent(), line, column));
        return liftNameLeaf(best.node, best.parent);
    }

    /**
     * Innermost node at the LSP caret, or {@code null}.
     *
     * @param module module AST
     * @param document open document
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return the hit, or {@code null}
     */
    public static ASTNode nodeAt(final ModuleNode module, final TextDocument document,
                                 final Position position, final PositionEncoding encoding) {
        int[] groovy = groovyAt(document, position, encoding);
        return groovy.length < 2 ? null : nodeAt(module, groovy[0], groovy[1]);
    }

    /**
     * Walks every class and the module statement block, including inner classes.
     *
     * @param module module AST
     * @param action node plus enclosing context
     */
    public static void walk(final ModuleNode module, final BiConsumer<ASTNode, AstContext> action) {
        if (module == null || action == null) {
            return;
        }
        for (ClassNode classNode : module.getClasses()) {
            if (classNode instanceof InnerClassNode) {
                continue;
            }
            org.codehaus.groovy.ast.query.AstQuery.from(classNode)
                    .andSelf()
                    .into(ClassNode.class)
                    .forEach((node, ctx) -> emit(node, ctx, action));
        }
        BlockStatement block = module.getStatementBlock();
        if (block != null) {
            walk(block, action);
        }
    }

    /**
     * Walks {@code node} and its descendants, including {@code node} itself.
     *
     * @param node any AST node
     * @param action node plus enclosing context
     */
    public static void walk(final ASTNode node, final BiConsumer<ASTNode, AstContext> action) {
        if (node == null || action == null) {
            return;
        }
        org.codehaus.groovy.ast.query.AstQuery.from(node)
                .andSelf()
                .forEach((hit, ctx) -> emit(hit, ctx, action));
    }

    /**
     * {@code ClassCodeVisitorSupport} does not treat {@link Parameter} as
     * a child node. The language server still needs caret hits on
     * parameter names, so parameters are emitted here instead of changing
     * the compiler query.
     */
    private static void emit(final ASTNode node, final AstContext ctx,
                             final BiConsumer<ASTNode, AstContext> action) {
        action.accept(node, ctx);
        if (node instanceof MethodNode method) {
            for (Parameter parameter : method.getParameters()) {
                emitParameter(parameter, ctx, action);
            }
        } else if (node instanceof ClosureExpression closure && closure.isParameterSpecified()) {
            for (Parameter parameter : closure.getParameters()) {
                emitParameter(parameter, ctx, action);
            }
        } else if (node instanceof ForStatement loop) {
            emitParameter(loop.getIndexVariable(), ctx, action);
            emitParameter(loop.getValueVariable(), ctx, action);
        } else if (node instanceof CatchStatement catchStatement) {
            emitParameter(catchStatement.getVariable(), ctx, action);
        }
    }

    private static void emitParameter(final Parameter parameter, final AstContext ctx,
                                      final BiConsumer<ASTNode, AstContext> action) {
        if (parameter == null || "forLoopDummyParameter".equals(parameter.getName())) {
            return;
        }
        action.accept(parameter, ctx);
    }

    /**
     * Nodes containing the LSP caret, innermost first.
     *
     * @param module module AST
     * @param document open document
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return containing nodes, possibly empty
     */
    public static List<ASTNode> containing(final ModuleNode module, final TextDocument document,
                                           final Position position, final PositionEncoding encoding) {
        int[] groovy = groovyAt(document, position, encoding);
        return groovy.length < 2 ? List.of() : containing(module, groovy[0], groovy[1]);
    }

    /**
     * Nodes containing the caret, innermost first.
     *
     * @param module module AST
     * @param line 1-based line
     * @param column 1-based column
     * @return containing nodes
     */
    public static List<ASTNode> containing(final ModuleNode module, final int line, final int column) {
        List<ASTNode> hits = new ArrayList<>();
        if (module == null) {
            return hits;
        }
        if (Positions.contains(module.getPackage(), line, column)) {
            hits.add(module.getPackage());
        }
        for (ImportNode imp : ImportSupport.allImports(module)) {
            if (Positions.contains(imp, line, column)) {
                hits.add(imp);
            }
        }
        walk(module, (node, ctx) -> {
            if (Positions.contains(node, line, column)) {
                hits.add(node);
            }
        });
        hits.sort((left, right) -> Long.compare(Positions.innerScore(right), Positions.innerScore(left)));
        return hits;
    }

    /**
     * @param module module AST
     * @param line 1-based line
     * @param column 1-based column
     * @return enclosing method, or {@code null}
     */
    public static MethodNode enclosingMethod(final ModuleNode module, final int line, final int column) {
        if (module == null) {
            return null;
        }
        MethodNode[] found = new MethodNode[1];
        walk(module, (node, ctx) -> {
            if (Positions.contains(node, line, column) && ctx.enclosingMethod() != null) {
                found[0] = ctx.enclosingMethod();
            }
        });
        return found[0];
    }

    /**
     * Returns the innermost call or constructor invocation containing the caret.
     *
     * @param module module AST
     * @param line 1-based line
     * @param column 1-based column
     * @return the call, or {@code null}
     */
    public static ASTNode enclosingCall(final ModuleNode module, final int line, final int column) {
        if (module == null) {
            return null;
        }
        Hit best = new Hit();
        walk(module, (node, ctx) -> {
            if (node instanceof MethodCallExpression
                    || node instanceof ConstructorCallExpression
                    || node instanceof StaticMethodCallExpression) {
                consider(best, node, ctx.parent(), line, column);
            }
        });
        return best.node;
    }

    /**
     * Innermost call or constructor containing the LSP caret.
     *
     * @param module module AST
     * @param document open document
     * @param position LSP position
     * @param encoding negotiated encoding
     * @return the call, or {@code null}
     */
    public static ASTNode enclosingCall(final ModuleNode module, final TextDocument document,
                                        final Position position, final PositionEncoding encoding) {
        int[] groovy = groovyAt(document, position, encoding);
        return groovy.length < 2 ? null : enclosingCall(module, groovy[0], groovy[1]);
    }

    private static int[] groovyAt(final TextDocument document, final Position position,
                                  final PositionEncoding encoding) {
        if (document == null || position == null) {
            return new int[0];
        }
        return document.toGroovy(position, encoding);
    }

    /**
     * Collects classes, methods, fields and properties in source order.
     *
     * @param module module AST
     * @return declaration nodes
     */
    public static List<ASTNode> declarations(final ModuleNode module) {
        List<ASTNode> nodes = new ArrayList<>();
        if (module == null) {
            return nodes;
        }
        if (module.getPackage() != null) {
            nodes.add(module.getPackage());
        }
        nodes.addAll(module.getImports());
        nodes.addAll(module.getStarImports());
        nodes.addAll(module.getStaticImports().values());
        nodes.addAll(module.getStaticStarImports().values());
        for (ClassNode classNode : module.getClasses()) {
            addClassDeclarations(nodes, classNode);
        }
        return nodes;
    }

    private static void addClassDeclarations(final List<ASTNode> nodes, final ClassNode classNode) {
        if (classNode.isScript() && classNode.getLineNumber() <= 0) {
            return;
        }
        nodes.add(classNode);
        for (PropertyNode property : classNode.getProperties()) {
            if (!property.isSynthetic()) {
                nodes.add(property);
            }
        }
        for (FieldNode field : classNode.getFields()) {
            if (!field.isSynthetic()) {
                nodes.add(field);
            }
        }
        for (ConstructorNode constructor : classNode.getDeclaredConstructors()) {
            if (!constructor.isSynthetic()) {
                nodes.add(constructor);
            }
        }
        for (MethodNode method : classNode.getMethods()) {
            if (!method.isSynthetic() && method.getLineNumber() > 0) {
                nodes.add(method);
            }
        }
    }

    /**
     * Call and property names are {@link ConstantExpression} children. Hover,
     * definition and rename want the enclosing call or property.
     */
    private static ASTNode liftNameLeaf(final ASTNode node, final ASTNode parent) {
        if (!(node instanceof ConstantExpression constant)) {
            return node;
        }
        if (parent instanceof MethodCallExpression call && call.getMethod() == node) {
            return call;
        }
        if (parent instanceof PropertyExpression property && property.getProperty() == node) {
            return property;
        }
        if (parent instanceof AttributeExpression attribute && attribute.getProperty() == node) {
            return attribute;
        }
        String text = constant.getText();
        if (parent instanceof MethodNode method && method.getName() != null && method.getName().equals(text)) {
            return parent;
        }
        if (parent instanceof ClassNode classNode && classNode.getNameWithoutPackage() != null
                && classNode.getNameWithoutPackage().equals(text)) {
            return parent;
        }
        return node;
    }

    private static void consider(final Hit best, final ASTNode candidate, final ASTNode parent,
                                 final int line, final int column) {
        if (candidate == null || !Positions.contains(candidate, line, column)) {
            return;
        }
        if (best.node == null || Positions.innerScore(candidate) >= Positions.innerScore(best.node)) {
            best.node = candidate;
            best.parent = parent;
        }
    }

    private static final class Hit {
        private ASTNode node;
        private ASTNode parent;
    }
}
