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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Script-level names a REPL host should not redeclare. A non-script class
 * contributes its simple name when that name is non-empty. A script
 * contributes methods other than the generated no-arg {@code run} and a
 * static {@code main} whose single parameter is an array, plus names
 * assigned in the {@code run} body. The walk enters {@code if} and
 * {@code try}, and a tuple on the left of an assignment. It does not
 * enter a closure or a lambda. An empty name is skipped.
 */
public final class ScriptNames {

    private ScriptNames() {
    }

    /**
     * @param module compiled module, or {@code null}
     * @return names, never {@code null}
     */
    public static Set<String> of(final ModuleNode module) {
        if (module == null) {
            return Set.of();
        }
        Set<String> names = new LinkedHashSet<>();
        for (ClassNode classNode : module.getClasses()) {
            collect(classNode, names);
        }
        return names;
    }

    private static void collect(final ClassNode classNode, final Set<String> names) {
        if (classNode.isScript()) {
            for (MethodNode method : classNode.getMethods()) {
                if (scriptLevelMethod(method)) {
                    names.add(method.getName());
                }
            }
            addScriptAssignments(classNode, names);
        } else {
            String simple = classNode.getNameWithoutPackage();
            if (simple != null && !simple.isEmpty()) {
                names.add(simple);
            }
        }
    }

    static boolean scriptLevelMethod(final MethodNode method) {
        if (method == null || method.isSynthetic()) {
            return false;
        }
        String name = method.getName();
        if (name == null || name.isEmpty() || "<init>".equals(name) || "<clinit>".equals(name)) {
            return false;
        }
        if ("run".equals(name) && method.getParameters().length == 0) {
            return false;
        }
        if ("main".equals(name) && method.isStatic()) {
            Parameter[] parameters = method.getParameters();
            if (parameters.length == 1 && parameters[0].getType() != null && parameters[0].getType().isArray()) {
                return false;
            }
        }
        return true;
    }

    static void addScriptAssignments(final ClassNode script, final Set<String> names) {
        if (script == null || names == null) {
            return;
        }
        MethodNode run = script.getMethod("run", Parameter.EMPTY_ARRAY);
        if (run == null || run.getCode() == null) {
            return;
        }
        run.getCode().visit(new CodeVisitorSupport() {
            @Override
            public void visitClosureExpression(final ClosureExpression expression) {
                // Names assigned inside a closure are not script-level bindings.
            }

            @Override
            public void visitLambdaExpression(final LambdaExpression expression) {
                // Names assigned inside a lambda are not script-level bindings.
            }

            @Override
            public void visitDeclarationExpression(final DeclarationExpression expression) {
                addLeftNames(expression.getLeftExpression(), names);
            }

            @Override
            public void visitBinaryExpression(final BinaryExpression expression) {
                if (expression.getOperation() != null && expression.getOperation().getType() == Types.ASSIGN) {
                    addLeftNames(expression.getLeftExpression(), names);
                }
                super.visitBinaryExpression(expression);
            }
        });
    }

    private static void addLeftNames(final Expression left, final Set<String> names) {
        if (left instanceof VariableExpression variable) {
            String name = variable.getName();
            if (name != null && !name.isEmpty()) {
                names.add(name);
            }
        } else if (left instanceof TupleExpression tuple) {
            for (Expression element : tuple.getExpressions()) {
                addLeftNames(element, names);
            }
        }
    }
}
