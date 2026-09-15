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
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameter-name and inferred-type inlay hints.
 */
public final class InlayHintService {

    public List<InlayHint> inlayHints(final CompiledDocument document, final Range range,
                                      final PositionEncoding encoding) {
        return inlayHints(document, range, encoding, null);
    }

    public List<InlayHint> inlayHints(final CompiledDocument document, final Range range,
                                      final PositionEncoding encoding, final CompilationSnapshot snapshot) {
        List<InlayHint> hints = new ArrayList<>();
        if (document == null || document.getModule() == null) {
            return hints;
        }
        for (ClassNode classNode : document.getModule().getClasses()) {
            addClassHints(hints, document, range, encoding, classNode);
        }
        addCallSiteHints(hints, document, range, encoding, snapshot);
        return hints;
    }

    private static void addClassHints(final List<InlayHint> hints, final CompiledDocument document, final Range range,
                                      final PositionEncoding encoding, final ClassNode classNode) {
        for (MethodNode method : classNode.getMethods()) {
            addMethodHints(hints, document, range, encoding, method);
        }
        for (FieldNode field : classNode.getFields()) {
            if (!field.isSynthetic() && field.isDynamicTyped()) {
                addTypeHint(hints, field, TypeInference.of(field), document.getText(), encoding, range,
                        TypeInference.usedInitializer(field));
            }
        }
    }

    private static void addMethodHints(final List<InlayHint> hints, final CompiledDocument document, final Range range,
                                       final PositionEncoding encoding, final MethodNode method) {
        if (method.isSynthetic() || method.getCode() == null) {
            return;
        }
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isDynamicTyped()) {
                addTypeHint(hints, parameter, parameter.getType(), document.getText(), encoding, range, false);
            }
        }
        AstQuery.walk(method.getCode(), (node, ctx) -> {
            if (node instanceof DeclarationExpression decl && !decl.isMultipleAssignmentDeclaration()) {
                VariableExpression variable = decl.getVariableExpression();
                if (variable != null && variable.isDynamicTyped()) {
                    addTypeHint(hints, variable, TypeInference.of(variable, document.getModule()),
                            document.getText(), encoding, range,
                            TypeInference.usedInitializer(variable, document.getModule()));
                }
            }
        });
    }

    private static void addTypeHint(final List<InlayHint> hints, final AnnotatedNode node, final ClassNode type,
                                    final String text, final PositionEncoding encoding, final Range range,
                                    final boolean inferred) {
        if (!SymbolIdentity.isResolvedType(type)) {
            return;
        }
        Range nameRange = Positions.toNameRange(node, text, encoding);
        if (nameRange == null || (range != null && nameRange.getStart().getLine() < range.getStart().getLine())) {
            return;
        }
        InlayHint hint = new InlayHint(nameRange.getStart(),
                Either.forLeft(type.getNameWithoutPackage() + " "));
        hint.setKind(InlayHintKind.Type);
        hint.setPaddingRight(true);
        if (inferred) {
            hint.setTooltip("inferred");
        }
        hints.add(hint);
    }

    private static void addCallSiteHints(final List<InlayHint> hints, final CompiledDocument document,
                                         final Range range, final PositionEncoding encoding,
                                         final CompilationSnapshot snapshot) {
        if (document.getModule() == null) {
            return;
        }
        AstQuery.walk(document.getModule(), (node, ctx) -> {
            if (node instanceof MethodCallExpression call) {
                MethodNode method = MethodBinding.resolveMethod(call, snapshot);
                addParameterNameHints(hints, document, range, encoding, call.getArguments(),
                        method == null ? null : method.getParameters(), call.getMethodTarget() != null);
            } else if (node instanceof StaticMethodCallExpression call) {
                MethodNode method = MethodBinding.resolveStatic(call, snapshot);
                addParameterNameHints(hints, document, range, encoding, call.getArguments(),
                        method == null ? null : method.getParameters(), false);
            } else if (node instanceof ConstructorCallExpression ctor) {
                ConstructorNode constructor = MethodBinding.resolveConstructor(ctor);
                addParameterNameHints(hints, document, range, encoding, ctor.getArguments(),
                        constructor == null ? null : constructor.getParameters(), false);
            }
        });
    }

    private static void addParameterNameHints(final List<InlayHint> hints, final CompiledDocument document,
                                              final Range range, final PositionEncoding encoding,
                                              final Expression arguments, final Parameter[] parameters,
                                              final boolean bound) {
        if (parameters == null || CallSites.hasNamedArgs(arguments)) {
            return;
        }
        List<Expression> args = CallSites.argumentExpressions(arguments);
        int count = Math.min(parameters.length, args.size());
        for (int i = 0; i < count; i++) {
            Parameter parameter = parameters[i];
            Expression arg = args.get(i);
            Range argRange = Positions.toRange(arg, document.getText(), encoding);
            if (skipParameterHint(parameter, arg, argRange, range)) {
                continue;
            }
            InlayHint hint = new InlayHint(argRange.getStart(), Either.forLeft(parameter.getName() + ":"));
            hint.setKind(InlayHintKind.Parameter);
            hint.setPaddingRight(true);
            if (!bound) {
                hint.setTooltip("inferred");
            }
            hints.add(hint);
        }
    }

    private static boolean skipParameterHint(final Parameter parameter, final Expression arg, final Range argRange,
                                             final Range range) {
        if (parameter == null || parameter.isSynthetic() || parameter.getName() == null
                || parameter.getName().startsWith("$")) {
            return true;
        }
        if (arg instanceof VariableExpression variable && parameter.getName().equals(variable.getName())) {
            return true;
        }
        return argRange == null || (range != null && !overlapsLine(argRange, range));
    }

    private static boolean overlapsLine(final Range token, final Range range) {
        if (range == null || token == null) {
            return true;
        }
        return token.getEnd().getLine() >= range.getStart().getLine()
                && token.getStart().getLine() <= range.getEnd().getLine();
    }
}
