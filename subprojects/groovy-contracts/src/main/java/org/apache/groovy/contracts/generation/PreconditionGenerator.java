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
package org.apache.groovy.contracts.generation;

import groovy.contracts.Requires;
import org.apache.groovy.contracts.annotations.meta.Precondition;
import org.apache.groovy.contracts.ast.visitor.AnnotationClosureVisitor;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.io.ReaderSource;

import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;

/**
 * Code generator for preconditions.
 */
public class PreconditionGenerator extends BaseGenerator {

    public PreconditionGenerator(final ReaderSource source) {
        super(source);
    }

    /**
     * Injects a precondition assertion statement in the given <tt>method</tt>, based on the given <tt>annotation</tt> of
     * type {@link Requires}.
     *
     * @param method       the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param precondition the {@link org.apache.groovy.contracts.domain.Precondition} the assertion statement should be generated from
     */
    public void generatePreconditionAssertionStatement(final MethodNode method, final org.apache.groovy.contracts.domain.Precondition precondition) {
        final BooleanExpression preconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(method.getDeclaringClass(), method, Precondition.class, precondition.booleanExpression(), false);

        BlockStatement blockStatement;

        final BlockStatement originalBlockStatement = precondition.originalBlockStatement();
        // if use execution tracker flag is found in the meta-data the annotation closure visitor discovered
        // method calls which might be subject to cycling boolean expressions -> no inline mode possible
        final boolean useExecutionTracker = originalBlockStatement == null || Boolean.TRUE.equals(originalBlockStatement.getNodeMetaData(AnnotationClosureVisitor.META_DATA_USE_EXECUTION_TRACKER));

        if (!useExecutionTracker && Boolean.TRUE.equals(method.getNodeMetaData(META_DATA_USE_INLINE_MODE))) {
            blockStatement = getInlineModeBlockStatement(precondition.originalBlockStatement());
        } else {
            blockStatement = wrapAssertionBooleanExpression(method.getDeclaringClass(), method, preconditionBooleanExpression, "precondition");
        }

        addPrecondition(method, blockStatement);
    }

    /**
     * Generates the default precondition statement for {@link org.codehaus.groovy.ast.MethodNode} instances with
     * the {@link org.apache.groovy.contracts.annotations.meta.Precondition} annotation.
     *
     * @param type       the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param methodNode the {@link org.codehaus.groovy.ast.MethodNode} with a {@link org.apache.groovy.contracts.annotations.meta.Precondition} annotation
     */
    public void generateDefaultPreconditionStatement(final ClassNode type, final MethodNode methodNode) {

        // if another precondition is available we'll evaluate to false
        boolean isAnotherPreconditionAvailable = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), methodNode, ClassHelper.makeWithoutCaching(Precondition.class)).size() > 0;
        if (!isAnotherPreconditionAvailable) return;

        // if there is another preconditio up the inheritance path, we need a default precondition with FALSE
        // e.g. C1 <no precondition> : C2 <item != null> == false || item != null
        BooleanExpression preconditionBooleanExpression = boolX(ConstantExpression.FALSE);
        preconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(type, methodNode, Precondition.class, preconditionBooleanExpression, false);
        // if precondition could not be found in parent class, let's return
        if (preconditionBooleanExpression.getExpression() == ConstantExpression.FALSE)
            return;

        final BlockStatement blockStatement = wrapAssertionBooleanExpression(type, methodNode, preconditionBooleanExpression, "precondition");

        addPrecondition(methodNode, blockStatement);
    }

    private void addPrecondition(MethodNode method, BlockStatement blockStatement) {
        final BlockStatement modifiedMethodCode = new BlockStatement();
        modifiedMethodCode.addStatements(blockStatement.getStatements());

        if (method.getCode() instanceof BlockStatement) {

            BlockStatement methodBlock = (BlockStatement) method.getCode();
            for (Statement statement : methodBlock.getStatements()) {
                if (method instanceof ConstructorNode && statement instanceof ExpressionStatement && ((ExpressionStatement) statement).getExpression() instanceof ConstructorCallExpression) {
                    modifiedMethodCode.getStatements().add(0, statement);
                } else {
                    modifiedMethodCode.getStatements().add(statement);
                }
            }
        } else {
            modifiedMethodCode.addStatement(method.getCode());
        }

        method.setCode(modifiedMethodCode);
    }
}
