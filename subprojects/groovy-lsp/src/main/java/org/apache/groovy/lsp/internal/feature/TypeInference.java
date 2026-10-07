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
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

/**
 * Best-effort types for hover and inlay when the AST still says
 * {@code Object} / dynamic. Prefers {@link StaticTypesMarker} metadata
 * written by the core static type-checking pass, then an initializer
 * expression. Does not run the static type checker itself.
 */
final class TypeInference {

    private TypeInference() {
    }

    static ClassNode of(final FieldNode field) {
        if (field == null) {
            return null;
        }
        ClassNode marked = markerType(field);
        if (marked != null) {
            return marked;
        }
        if (SymbolIdentity.isResolvedType(field.getType()) && !field.isDynamicTyped()) {
            return field.getType();
        }
        return firstResolved(field.getType(), typeOf(field.getInitialExpression()));
    }

    static ClassNode of(final Parameter parameter) {
        if (parameter == null) {
            return null;
        }
        ClassNode marked = markerType(parameter);
        return marked == null ? parameter.getType() : marked;
    }

    static ClassNode of(final VariableExpression variable, final ModuleNode module) {
        if (variable == null) {
            return null;
        }
        ClassNode marked = markerType(variable);
        if (marked != null) {
            return marked;
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
        ClassNode fromDecl = markerType(declared);
        if (fromDecl == null) {
            fromDecl = initializerType(declared, module);
        }
        return firstResolved(variable.getType(), fromDecl);
    }

    static boolean usedInitializer(final FieldNode field) {
        return field != null && usedInitializer(field.isDynamicTyped(), of(field), field.getType());
    }

    static boolean usedInitializer(final VariableExpression variable, final ModuleNode module) {
        return variable != null && usedInitializer(variable.isDynamicTyped(), of(variable, module), variable.getType());
    }

    private static boolean usedInitializer(final boolean dynamic, final ClassNode inferred, final ClassNode declared) {
        if (!dynamic || !SymbolIdentity.isResolvedType(inferred) || declared == null) {
            return false;
        }
        return !inferred.getName().equals(declared.getName());
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
        if (expression == null) {
            return null;
        }
        ClassNode marked = markerType(expression);
        return marked == null ? expression.getType() : marked;
    }

    private static ClassNode markerType(final ASTNode node) {
        if (node == null) {
            return null;
        }
        Object inferred = node.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (inferred instanceof ClassNode classNode && SymbolIdentity.isResolvedType(classNode)) {
            return classNode;
        }
        Object declared = node.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE);
        if (declared instanceof ClassNode classNode && SymbolIdentity.isResolvedType(classNode)) {
            return classNode;
        }
        return null;
    }

    private static ClassNode firstResolved(final ClassNode declared, final ClassNode inferred) {
        if (SymbolIdentity.isResolvedType(inferred)) {
            return inferred;
        }
        return declared;
    }
}
