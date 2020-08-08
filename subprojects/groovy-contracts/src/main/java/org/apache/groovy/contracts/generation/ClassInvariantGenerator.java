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

import groovy.contracts.Invariant;
import org.apache.groovy.contracts.annotations.meta.ClassInvariant;
import org.apache.groovy.contracts.ast.visitor.BaseVisitor;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.io.ReaderSource;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.AND;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;

/**
 * <p>
 * Code generator for class invariants.
 * </p>
 */
public class ClassInvariantGenerator extends BaseGenerator {

    public ClassInvariantGenerator(final ReaderSource source) {
        super(source);
    }

    /**
     * Reads the {@link Invariant} boolean expression and generates a synthetic
     * method holding this class invariant. This is used for heir calls to find out about inherited class
     * invariants.
     *
     * @param type           the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param classInvariant the {@link org.apache.groovy.contracts.domain.ClassInvariant} the assertion statement should be generated from
     */
    public void generateInvariantAssertionStatement(final ClassNode type, final org.apache.groovy.contracts.domain.ClassInvariant classInvariant) {

        BooleanExpression classInvariantExpression = addCallsToSuperAnnotationClosure(type, ClassInvariant.class, classInvariant.booleanExpression());

        final BlockStatement blockStatement = new BlockStatement();

        // add a local protected method with the invariant closure - this is needed for invariant checks in inheritance lines
        MethodNode methodNode = type.addMethod(getInvariantMethodName(type), Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, blockStatement);
        methodNode.setSynthetic(true);

        blockStatement.addStatements(wrapAssertionBooleanExpression(type, methodNode, classInvariantExpression, "invariant").getStatements());
    }

    private BooleanExpression addCallsToSuperAnnotationClosure(final ClassNode type, final Class<? extends Annotation> annotationType, BooleanExpression booleanExpression) {

        final List<AnnotationNode> nextContractElementAnnotations = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), ClassHelper.makeWithoutCaching(annotationType));
        if (nextContractElementAnnotations.isEmpty()) return booleanExpression;

        for (AnnotationNode nextContractElementAnnotation : nextContractElementAnnotations) {
            ClassExpression classExpression = (ClassExpression) nextContractElementAnnotation.getMember(BaseVisitor.CLOSURE_ATTRIBUTE_NAME);
            if (classExpression == null) continue;

            MethodCallExpression doCall = callX(
                    ctorX(classExpression.getType(), args(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION)),
                    "doCall"
            );
            doCall.setMethodTarget(classExpression.getType().getMethods("doCall").get(0));

            final BooleanExpression rightExpression = boolX(doCall);
            booleanExpression.setSourcePosition(nextContractElementAnnotation);
            booleanExpression = boolX(binX(booleanExpression, AND, rightExpression));
        }

        return booleanExpression;
    }

    /**
     * Adds the current class-invariant to the given <tt>method</tt>.
     *
     * @param type   the {@link org.codehaus.groovy.ast.ClassNode} which declared the given {@link org.codehaus.groovy.ast.MethodNode}
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     */
    public void addInvariantAssertionStatement(final ClassNode type, final MethodNode method) {

        final String invariantMethodName = getInvariantMethodName(type);
        final MethodNode invariantMethod = type.getDeclaredMethod(invariantMethodName, Parameter.EMPTY_ARRAY);
        if (invariantMethod == null) return;

        Statement invariantMethodCall = stmt(callThisX(invariantMethod.getName()));

        final Statement statement = method.getCode();
        if (statement instanceof BlockStatement && method.getReturnType() != ClassHelper.VOID_TYPE && !(method instanceof ConstructorNode)) {
            final BlockStatement blockStatement = (BlockStatement) statement;

            final List<ReturnStatement> returnStatements = AssertStatementCreationUtility.getReturnStatements(method);
            for (ReturnStatement returnStatement : returnStatements) {
                AssertStatementCreationUtility.addAssertionCallStatementToReturnStatement(blockStatement, returnStatement, invariantMethodCall);
            }

            if (returnStatements.isEmpty()) blockStatement.addStatement(invariantMethodCall);

        } else if (statement instanceof BlockStatement) {
            final BlockStatement blockStatement = (BlockStatement) statement;
            blockStatement.addStatement(invariantMethodCall);
        } else {
            final BlockStatement assertionBlock = new BlockStatement();
            assertionBlock.addStatement(statement);
            assertionBlock.addStatement(invariantMethodCall);
            method.setCode(assertionBlock);
        }
    }
}
