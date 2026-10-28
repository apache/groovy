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

/**
 * Marks repeated underscore placeholders used in tuple and closure parameters.
 * <p>
 * A repeated {@code _} is unnamed: it occupies a tuple slot or parameter
 * position but does not declare a variable of that name. Parameter placeholders
 * are given a distinct bytecode name so they cannot hide a real {@code _}.
 */
public class PlaceholderVisitor extends ClassCodeVisitorSupport {
    private static final String PLACEHOLDER = "Underscore_Placeholder";
    private static final String SLOT = "Underscore_Placeholder_Slot";
    private SourceUnit source;
    private int nextSlot;

    /**
     * Creates a visitor for placeholder analysis.
     *
     * @param compilationUnit the owning compilation unit
     * @param source the source unit being visited
     */
    public PlaceholderVisitor(CompilationUnit compilationUnit, SourceUnit source) {
        this.source = source;
    }

    /**
     * Returns the source unit currently being visited.
     *
     * @return the active source unit
     */
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
                .filter(s -> "_".equals(s))
                .count();
            if (underscoreCount > 1) {
                expression.getTupleExpression().getExpressions().forEach(e -> {
                    if ("_".equals(((VariableExpression) e).getName())) {
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
                .filter(s -> "_".equals(s))
                .count();
            if (underscoreCount > 1) {
                for (Parameter param : expression.getParameters()) {
                    if ("_".equals(param.getName())) {
                        markAsPlaceholder(param);
                        if (param.getNodeMetaData(SLOT) == null) {
                            param.setNodeMetaData(SLOT, nextSlot++);
                        }
                    }
                }
            }
        }
        super.visitClosureExpression(expression);
    }

    private static void markAsPlaceholder(ASTNode node) {
        node.setNodeMetaData(PLACEHOLDER, Boolean.TRUE);
    }

    /**
     * Checks whether the supplied node has been marked as a placeholder.
     *
     * @param node the node to inspect
     * @return {@code true} if the node is a placeholder
     */
    public static boolean isPlaceholder(ASTNode node) {
        return Boolean.TRUE.equals(node.getNodeMetaData(PLACEHOLDER));
    }

    /**
     * Bytecode name of a parameter. Unnamed parameters use a slot-specific
     * name so a real variable called {@code _} is not hidden.
     *
     * @param parameter the parameter to name
     * @return the name to use in the local-variable map
     */
    public static String bytecodeName(final Parameter parameter) {
        if (!isPlaceholder(parameter)) {
            return parameter.getName();
        }
        Object slot = parameter.getNodeMetaData(SLOT);
        if (slot == null) {
            slot = System.identityHashCode(parameter);
        }
        return "$_unnamed_" + slot;
    }
}
