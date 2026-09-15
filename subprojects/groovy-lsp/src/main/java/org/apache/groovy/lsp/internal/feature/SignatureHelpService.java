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
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Signature help for calls and constructors.
 */
public final class SignatureHelpService {

    public SignatureHelp signatureHelp(final TextDocument document, final CompilationSnapshot snapshot,
                                       final Position position, final PositionEncoding encoding) {
        CompiledDocument compiled = snapshot == null ? null : snapshot.get(document);
        if (compiled == null || compiled.getModule() == null) {
            return new SignatureHelp(List.of(), 0, 0);
        }
        int[] groovy = document.toGroovy(position, encoding);
        ASTNode node = AstQuery.enclosingCall(compiled.getModule(), groovy[0], groovy[1]);
        List<MethodNode> overloads = new ArrayList<>();
        int active = 0;
        int argc = 0;
        Expression arguments = argumentsOf(node);
        if (arguments != null) {
            argc = MethodBinding.argumentCount(arguments);
            active = activeParameter(arguments, groovy[0], groovy[1]);
        }
        if (node instanceof MethodCallExpression call) {
            overloads.addAll(MethodBinding.overloads(call, snapshot));
        } else if (node instanceof StaticMethodCallExpression call) {
            overloads.addAll(MethodBinding.overloads(call.getOwnerType(), call.getMethodAsString()));
        } else if (node instanceof ConstructorCallExpression ctor && ctor.getType() != null) {
            overloads.addAll(List.copyOf(ctor.getType().getDeclaredConstructors()));
        }
        if (overloads.isEmpty()) {
            return new SignatureHelp(List.of(), 0, 0);
        }
        List<SignatureInformation> informations = new ArrayList<>();
        int activeSignature = 0;
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < overloads.size(); i++) {
            MethodNode method = overloads.get(i);
            informations.add(signatureInformation(method));
            int distance = Math.abs(method.getParameters().length - argc);
            if (distance < best) {
                best = distance;
                activeSignature = i;
            }
        }
        int paramCount = overloads.get(activeSignature).getParameters().length;
        if (active >= paramCount) {
            active = Math.max(paramCount - 1, 0);
        }
        return new SignatureHelp(informations, activeSignature, active);
    }

    private static int activeParameter(final Expression arguments, final int line, final int column) {
        if (!(arguments instanceof TupleExpression tuple) || tuple.getExpressions().isEmpty()) {
            return 0;
        }
        List<Expression> exprs = tuple.getExpressions();
        int active = 0;
        for (int i = 0; i < exprs.size(); i++) {
            if (caretAfter(exprs.get(i), line, column)) {
                active = i + 1;
            }
        }
        if (active >= exprs.size()) {
            active = exprs.size() - 1;
        }
        return Math.max(active, 0);
    }

    private static boolean caretAfter(final Expression arg, final int line, final int column) {
        if (arg == null || arg.getLineNumber() <= 0) {
            return false;
        }
        int lastLine = arg.getLastLineNumber() > 0 ? arg.getLastLineNumber() : arg.getLineNumber();
        int lastColumn = arg.getLastColumnNumber() > 0 ? arg.getLastColumnNumber() : arg.getColumnNumber();
        return line > lastLine || (line == lastLine && column > lastColumn);
    }

    private static SignatureInformation signatureInformation(final MethodNode method) {
        StringBuilder label = new StringBuilder(method.getName()).append('(');
        List<ParameterInformation> infos = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                label.append(", ");
            }
            String part = parameters[i].getType().getNameWithoutPackage() + " " + parameters[i].getName();
            label.append(part);
            infos.add(new ParameterInformation(part));
        }
        label.append(')');
        SignatureInformation information = new SignatureInformation(label.toString());
        information.setParameters(infos);
        return information;
    }

    private static Expression argumentsOf(final ASTNode node) {
        if (node instanceof MethodCallExpression call) {
            return call.getArguments();
        }
        if (node instanceof StaticMethodCallExpression call) {
            return call.getArguments();
        }
        if (node instanceof ConstructorCallExpression ctor) {
            return ctor.getArguments();
        }
        return null;
    }
}
