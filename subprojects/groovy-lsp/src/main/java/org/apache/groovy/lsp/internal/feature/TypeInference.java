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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;

/**
 * Best-effort types for hover and inlay when the AST still says
 * {@code Object} / dynamic. Uses an initializer expression when present;
 * does not run the static type checker.
 */
final class TypeInference {

    private TypeInference() {
    }

    static ClassNode of(final FieldNode field) {
        if (field == null) {
            return null;
        }
        if (SymbolIdentity.isResolvedType(field.getType()) && !field.isDynamicTyped()) {
            return field.getType();
        }
        return firstResolved(field.getType(), typeOf(field.getInitialExpression()));
    }

    static ClassNode of(final Parameter parameter) {
        return parameter == null ? null : parameter.getType();
    }

    static ClassNode of(final VariableExpression variable, final ModuleNode module) {
        if (variable == null) {
            return null;
        }
        if (SymbolIdentity.isResolvedType(variable.getType()) && !variable.isDynamicTyped()) {
            return variable.getType();
        }
        Variable accessed = variable.getAccessedVariable();
        if (accessed instanceof FieldNode field) {
            return of(field);
        }
        VariableExpression declared = accessed instanceof VariableExpression variableExpression
                ? variableExpression : variable;
        ClassNode fromDecl = initializerType(declared, module);
        return firstResolved(variable.getType(), fromDecl);
    }

    static boolean usedInitializer(final VariableExpression variable, final ModuleNode module) {
        if (variable == null || !variable.isDynamicTyped()) {
            return false;
        }
        ClassNode inferred = of(variable, module);
        if (!SymbolIdentity.isResolvedType(inferred) || variable.getType() == null) {
            return false;
        }
        return !inferred.getName().equals(variable.getType().getName());
    }

    private static ClassNode initializerType(final VariableExpression declared, final ModuleNode module) {
        if (declared == null || module == null) {
            return null;
        }
        ClassNode[] found = new ClassNode[1];
        AstQuery.walk(module, (node, ctx) -> {
            if (found[0] != null || !(node instanceof DeclarationExpression decl) || decl.isMultipleAssignmentDeclaration()) {
                return;
            }
            VariableExpression left = decl.getVariableExpression();
            if (left == declared || left == declared.getAccessedVariable()) {
                found[0] = typeOf(decl.getRightExpression());
            }
        });
        return found[0];
    }

    private static ClassNode typeOf(final Expression expression) {
        return expression == null ? null : expression.getType();
    }

    private static ClassNode firstResolved(final ClassNode declared, final ClassNode inferred) {
        if (SymbolIdentity.isResolvedType(inferred)) {
            return inferred;
        }
        return declared;
    }
}
