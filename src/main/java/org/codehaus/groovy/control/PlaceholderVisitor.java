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
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.util.Arrays;
import java.util.stream.StreamSupport;

public class PlaceholderVisitor extends ClassCodeVisitorSupport {
    private static final String PLACEHOLDER = "Underscore_Placeholder";
    private SourceUnit source;

    public PlaceholderVisitor(CompilationUnit compilationUnit, SourceUnit source) {
        this.source = source;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Visit the declaration marking underscore-named variables in multi-assignments as placeholders
     *
     * @param expression the expression to check
     */
    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        if (expression.isMultipleAssignmentDeclaration() && expression.getTupleExpression().getExpressions().size() > 1) {
            long underscoreCount = StreamSupport.stream(expression.getTupleExpression().spliterator(), false)
                .map(e -> ((VariableExpression) e).getName())
                .filter(s -> s.equals("_"))
                .count();
            if (underscoreCount > 1) {
                expression.getTupleExpression().getExpressions().forEach(e -> {
                    if (((VariableExpression) e).getName().equals("_")) {
                        markAsPlaceholder(e);
                    }
                });
            }
        }
        super.visitDeclarationExpression(expression);
    }

    /**
     * Visit the Closure (or Lambda) marking underscore-named parameters as placeholders
     *
     * @param expression the expression to check
     */
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (expression.getParameters() != null) {
            long underscoreCount = Arrays.stream(expression.getParameters())
                .map(Parameter::getName)
                .filter(s -> s.equals("_"))
                .count();
            if (underscoreCount > 1) {
                for (Parameter param : expression.getParameters()) {
                    if (param.getName().equals("_")) {
                        markAsPlaceholder(param);
                    }
                }
            }
        }
        super.visitClosureExpression(expression);
    }
    private static void markAsPlaceholder(ASTNode node) {
        node.setNodeMetaData(PLACEHOLDER, Boolean.TRUE);
    }

    public static boolean isPlaceholder(ASTNode node) {
        return Boolean.TRUE.equals(node.getNodeMetaData(PLACEHOLDER));
    }
}
