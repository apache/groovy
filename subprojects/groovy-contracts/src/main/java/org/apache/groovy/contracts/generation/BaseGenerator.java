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

import org.apache.groovy.contracts.ViolationTracker;
import org.apache.groovy.contracts.ast.visitor.BaseVisitor;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.apache.groovy.contracts.util.ExpressionUtils;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.io.ReaderSource;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.andX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.orX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Base class for groovy-contracts code generators.
 */
public abstract class BaseGenerator {

    public static final String INVARIANT_CLOSURE_PREFIX = "invariant";
    public static final String META_DATA_USE_INLINE_MODE = "org.apache.groovy.contracts.USE_INLINE_MODE";

    protected final ReaderSource source;

    protected BaseGenerator(final ReaderSource source) {
        this.source = source;
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     * @return the field name of the invariant closure field of the given <tt>classNode</tt>
     */
    public static String getInvariantMethodName(final ClassNode classNode) {
        return INVARIANT_CLOSURE_PREFIX + "_" + classNode.getName().replaceAll("\\.", "_");
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     * @return the {@link org.codehaus.groovy.ast.MethodNode} which contains the invariant of the given <tt>classNode</tt>
     */
    public static MethodNode getInvariantMethodNode(final ClassNode classNode) {
        return classNode.getDeclaredMethod(getInvariantMethodName(classNode), Parameter.EMPTY_ARRAY);
    }

    protected BlockStatement getInlineModeBlockStatement(BlockStatement blockStatement) {
        BooleanExpression combinedBooleanExpression = ExpressionUtils.getBooleanExpression(ExpressionUtils.getBooleanExpressionsFromAssertionStatements(blockStatement));
        return block(ifS(
                boolX(varX(BaseVisitor.GCONTRACTS_ENABLED_VAR, ClassHelper.boolean_TYPE)),
                block(ifS(notX(combinedBooleanExpression), blockStatement))));
    }

    protected BlockStatement wrapAssertionBooleanExpression(ClassNode type, MethodNode methodNode, BooleanExpression classInvariantExpression, String assertionType) {
        ClassNode violationTrackerClassNode = ClassHelper.makeWithoutCaching(ViolationTracker.class);
        VariableExpression $_gc_result = varX("$_gc_result", ClassHelper.boolean_TYPE);
        $_gc_result.setAccessedVariable($_gc_result);

        BlockStatement ifBlockStatement = block(
                declS($_gc_result, ConstantExpression.FALSE),
                stmt(callX(classX(violationTrackerClassNode), "init")),
                assignS($_gc_result, classInvariantExpression),
                ifS(
                        boolX(notX(callX($_gc_result, "booleanValue"))),
                        ifS(
                                boolX(callX(classX(violationTrackerClassNode), "violationsOccurred")),
                                tryCatchS(
                                        stmt(callX(classX(violationTrackerClassNode), "rethrowFirst")),
                                        block(stmt(callX(classX(violationTrackerClassNode), "deinit"))))
                        )
                )
        );

        TryCatchStatement lockTryCatchStatement = tryCatchS(
                block(ifS(
                        boolX(callX(classX(ClassHelper.make(ContractExecutionTracker.class)), "track", args(constX(type.getName()), constX(methodNode.getTypeDescriptor()), constX(assertionType), methodNode.isStatic() ? ConstantExpression.TRUE : ConstantExpression.FALSE))),
                        ifBlockStatement)),
                block(new VariableScope(), stmt(callX(
                        classX(ClassHelper.make(ContractExecutionTracker.class)),
                        "clear",
                        args(constX(type.getName()), constX(methodNode.getTypeDescriptor()), constX(assertionType), methodNode.isStatic() ? ConstantExpression.TRUE : ConstantExpression.FALSE)
                ))));

        return block(ifS(boolX(varX(BaseVisitor.GCONTRACTS_ENABLED_VAR, ClassHelper.boolean_TYPE)), lockTryCatchStatement));
    }

    // TODO: what about constructor method nodes - does it find a constructor node in the super class?
    protected BooleanExpression addCallsToSuperMethodNodeAnnotationClosure(final ClassNode type, final MethodNode methodNode, final Class<? extends Annotation> annotationType, BooleanExpression booleanExpression, boolean isPostcondition) {
        List<AnnotationNode> contractElementAnnotations = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), methodNode, ClassHelper.makeWithoutCaching(annotationType));
        if (contractElementAnnotations.isEmpty()) {
            methodNode.putNodeMetaData(META_DATA_USE_INLINE_MODE, Boolean.TRUE);
        } else {
            for (AnnotationNode contractElementAnnotation : contractElementAnnotations) {
                ArgumentListExpression argumentList = new ArgumentListExpression();
                for (Parameter parameter : methodNode.getParameters()) {
                    argumentList.addExpression(varX(parameter));
                }
                if (isPostcondition && !methodNode.isVoidMethod()) {
                    argumentList.addExpression(localVarX("result", methodNode.getReturnType()));
                }
                if (isPostcondition && !methodNode.isConstructor()) {
                    argumentList.addExpression(localVarX("old", ClassHelper.MAP_TYPE.getPlainNodeReference()));
                }

                BooleanExpression predicate = BaseVisitor.asConditionExecution(contractElementAnnotation);
                ((MethodCallExpression) predicate.getExpression()).setArguments(argumentList);

                if (isPostcondition) {
                    booleanExpression = boolX(andX(booleanExpression, predicate));
                } else {
                    booleanExpression = boolX(orX(booleanExpression, predicate));
                }
            }
        }
        return booleanExpression;
    }
}
