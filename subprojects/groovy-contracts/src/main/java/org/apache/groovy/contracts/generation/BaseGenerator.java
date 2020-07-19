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
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * <pe
 * Base class for groovy-contracts code generators.
 * </p>
 */
public abstract class BaseGenerator {

    public static final String INVARIANT_CLOSURE_PREFIX = "invariant";
    public static final String META_DATA_USE_INLINE_MODE = "org.apache.groovy.contracts.USE_INLINE_MODE";

    protected final ReaderSource source;

    public BaseGenerator(final ReaderSource source) {
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
        final BooleanExpression combinedBooleanExpression = ExpressionUtils.getBooleanExpression(ExpressionUtils.getBooleanExpressionsFromAssertionStatements(blockStatement));
        return block(ifS(
                boolX(varX(BaseVisitor.GCONTRACTS_ENABLED_VAR, ClassHelper.boolean_TYPE)),
                block(ifS(notX(combinedBooleanExpression), blockStatement))));
    }

    protected BlockStatement wrapAssertionBooleanExpression(ClassNode type, MethodNode methodNode, BooleanExpression classInvariantExpression, String assertionType) {

        final ClassNode violationTrackerClassNode = ClassHelper.makeWithoutCaching(ViolationTracker.class);
        final VariableExpression $_gc_result = varX("$_gc_result", ClassHelper.boolean_TYPE);
        $_gc_result.setAccessedVariable($_gc_result);

        final BlockStatement ifBlockStatement = block(
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

        final TryCatchStatement lockTryCatchStatement = tryCatchS(
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

        final List<AnnotationNode> nextContractElementAnnotations = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), methodNode, ClassHelper.makeWithoutCaching(annotationType));
        if (nextContractElementAnnotations.isEmpty()) {
            if (methodNode.getNodeMetaData(META_DATA_USE_INLINE_MODE) == null)
                methodNode.setNodeMetaData(META_DATA_USE_INLINE_MODE, Boolean.TRUE);
            return booleanExpression;
        }

        for (AnnotationNode nextContractElementAnnotation : nextContractElementAnnotations) {
            ClassExpression classExpression = (ClassExpression) nextContractElementAnnotation.getMember(BaseVisitor.CLOSURE_ATTRIBUTE_NAME);
            if (classExpression == null) continue;

            ArgumentListExpression callArgumentList = new ArgumentListExpression();
            for (Parameter parameter : methodNode.getParameters()) {
                callArgumentList.addExpression(varX(parameter));
            }

            if (isPostcondition && methodNode.getReturnType() != ClassHelper.VOID_TYPE && !(methodNode instanceof ConstructorNode)) {
                callArgumentList.addExpression(localVarX("result", methodNode.getReturnType()));
            }

            if (isPostcondition && !(methodNode instanceof ConstructorNode)) {
                callArgumentList.addExpression(localVarX("old", new ClassNode(Map.class)));
            }

            ArgumentListExpression newInstanceArguments = args(
                    classExpression,
                    new ArrayExpression(
                            ClassHelper.DYNAMIC_TYPE,
                            Arrays.<Expression>asList(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION)
                    )
            );

            MethodCallExpression doCall = callX(
                    callX(ClassHelper.makeWithoutCaching(InvokerHelper.class), "invokeConstructorOf", newInstanceArguments),
                    "doCall",
                    callArgumentList);
            doCall.setMethodTarget(classExpression.getType().getMethods("doCall").get(0));

            booleanExpression.setSourcePosition(nextContractElementAnnotation);

            booleanExpression = boolX(
                    binX(
                            booleanExpression,
                            isPostcondition ? Token.newSymbol(Types.LOGICAL_AND, -1, -1) : Token.newSymbol(Types.LOGICAL_OR, -1, -1),
                            boolX(doCall))
            );
        }

        return booleanExpression;
    }
}
