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
package org.apache.groovy.contracts.ast.visitor;

import groovy.contracts.Ensures;
import groovy.contracts.Requires;
import org.apache.groovy.contracts.ClassInvariantViolation;
import org.apache.groovy.contracts.PostconditionViolation;
import org.apache.groovy.contracts.PreconditionViolation;
import org.apache.groovy.contracts.annotations.meta.ContractElement;
import org.apache.groovy.contracts.annotations.meta.Postcondition;
import org.apache.groovy.contracts.classgen.asm.ContractClosureWriter;
import org.apache.groovy.contracts.generation.AssertStatementCreationUtility;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.apache.groovy.contracts.generation.TryCatchBlockGenerator;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.apache.groovy.contracts.util.ExpressionUtils;
import org.apache.groovy.contracts.util.FieldValues;
import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.TransformingCodeVisitor;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;

/**
 * Visits interfaces &amp; classes and looks for <tt>@Requires</tt> or <tt>@Ensures</tt> and creates {@link groovy.lang.Closure}
 * classes for the annotation closures.<p/>
 * <p>
 * The annotation closure classes are used later on to check interface contract pre- and post-conditions in
 * implementation classes.
 *
 * @see Requires
 * @see Ensures
 * @see org.apache.groovy.contracts.ast.visitor.BaseVisitor
 */
public class AnnotationClosureVisitor extends BaseVisitor implements ASTNodeMetaData {

    public static final String META_DATA_USE_EXECUTION_TRACKER = "org.apache.groovy.contracts.META_DATA.USE_EXECUTION_TRACKER";
    public static final String META_DATA_ORIGINAL_TRY_CATCH_BLOCK = "org.apache.groovy.contracts.META_DATA.ORIGINAL_TRY_CATCH_BLOCK";

    private static final String POSTCONDITION_TYPE_NAME = Postcondition.class.getName();
    private static final ClassNode FIELD_VALUES = ClassHelper.makeCached(FieldValues.class);

    private ClassNode classNode;
    private final ContractClosureWriter contractClosureWriter = new ContractClosureWriter();

    public AnnotationClosureVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (node == null) return;
        if (!(CandidateChecks.isInterfaceContractsCandidate(node) || CandidateChecks.isContractsCandidate(node)))
            return;

        classNode = node;

        if (classNode.getNodeMetaData(PROCESSED) == null && CandidateChecks.isContractsCandidate(node)) {
            final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(node, ContractElement.class.getName());
            for (AnnotationNode annotationNode : annotationNodes) {
                Expression expression = annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME);
                if (expression == null || expression instanceof ClassExpression) continue;

                ClosureExpression closureExpression = (ClosureExpression) expression;

                ClosureExpressionValidator validator = new ClosureExpressionValidator(classNode, null, annotationNode, sourceUnit);
                validator.visitClosureExpression(closureExpression);
                validator.secondPass(closureExpression);

                List<Parameter> parameters = new ArrayList<>(Arrays.asList(closureExpression.getParameters()));

                final List<BooleanExpression> booleanExpressions = ExpressionUtils.getBooleanExpression(closureExpression);
                if (booleanExpressions == null || booleanExpressions.isEmpty()) continue;

                BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();

                BlockStatement newClosureBlockStatement = TryCatchBlockGenerator.generateTryCatchBlock(
                        ClassHelper.makeWithoutCaching(ClassInvariantViolation.class),
                        "<" + annotationNode.getClassNode().getName() + "> " + classNode.getName() + " \n\n",
                        AssertStatementCreationUtility.getAssertionStatements(booleanExpressions)
                );

                newClosureBlockStatement.setSourcePosition(closureBlockStatement);

                ClosureExpression rewrittenClosureExpression = new ClosureExpression(parameters.toArray(Parameter.EMPTY_ARRAY), newClosureBlockStatement);
                rewrittenClosureExpression.setSourcePosition(closureExpression);
                rewrittenClosureExpression.setDeclaringClass(closureExpression.getDeclaringClass());
                rewrittenClosureExpression.setSynthetic(true);
                rewrittenClosureExpression.setVariableScope(closureExpression.getVariableScope());
                rewrittenClosureExpression.setType(closureExpression.getType());

                ClassNode closureClassNode = contractClosureWriter.createClosureClass(classNode, null, rewrittenClosureExpression, false, false, Opcodes.ACC_PUBLIC);
                classNode.getModule().addClass(closureClassNode);

                final ClassExpression value = new ClassExpression(closureClassNode);
                value.setSourcePosition(annotationNode);

                BlockStatement value1 = TryCatchBlockGenerator.generateTryCatchBlockForInlineMode(
                        ClassHelper.makeWithoutCaching(ClassInvariantViolation.class),
                        "<" + annotationNode.getClassNode().getName() + "> " + classNode.getName() + " \n\n",
                        AssertStatementCreationUtility.getAssertionStatements(booleanExpressions)
                );
                value1.setNodeMetaData(META_DATA_USE_EXECUTION_TRACKER, validator.isMethodCalls());

                value.setNodeMetaData(META_DATA_ORIGINAL_TRY_CATCH_BLOCK, value1);

                annotationNode.setMember(CLOSURE_ATTRIBUTE_NAME, value);

                markClosureReplaced(classNode);
            }
        }

        super.visitClass(node);

        // generate closure classes for the super class and all implemented interfaces
        visitClass(node.getSuperClass());
        for (ClassNode i : node.getInterfaces()) {
            visitClass(i);
        }

        markProcessed(classNode);
    }

    @Override
    public void visitConstructorOrMethod(MethodNode methodNode, boolean isConstructor) {
        if (!CandidateChecks.couldBeContractElementMethodNode(classNode, methodNode) && !(CandidateChecks.isPreconditionCandidate(classNode, methodNode)))
            return;
        if (methodNode.getNodeMetaData(PROCESSED) != null) return;

        final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(methodNode, ContractElement.class.getName());
        for (AnnotationNode annotationNode : annotationNodes) {
            replaceWithClosureClassReference(annotationNode, methodNode);
        }

        markProcessed(methodNode);

        super.visitConstructorOrMethod(methodNode, isConstructor);
    }

    private void replaceWithClosureClassReference(AnnotationNode annotationNode, MethodNode methodNode) {
        Validate.notNull(annotationNode);
        Validate.notNull(methodNode);

        // check whether this is a pre- or postcondition
        boolean isPostcondition = AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), org.apache.groovy.contracts.annotations.meta.Postcondition.class.getName());

        Expression expression = annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME);
        if (expression == null || expression instanceof ClassExpression) return;

        ClosureExpression closureExpression = (ClosureExpression) expression;
        ClassCodeExpressionTransformer transformer = new OldPropertyExpressionTransformer(methodNode);
        TransformingCodeVisitor visitor = new TransformingCodeVisitor(transformer);
        visitor.visitClosureExpression(closureExpression);

        ClosureExpressionValidator validator = new ClosureExpressionValidator(classNode, methodNode, annotationNode, sourceUnit);
        validator.visitClosureExpression(closureExpression);
        validator.secondPass(closureExpression);

        List<Parameter> parameters = new ArrayList<>(Arrays.asList(closureExpression.getParameters()));

        parameters.addAll(new ArrayList<>(Arrays.asList(methodNode.getParameters())));

        final List<BooleanExpression> booleanExpressions = ExpressionUtils.getBooleanExpression(closureExpression);
        if (booleanExpressions == null || booleanExpressions.isEmpty()) return;

        BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();

        BlockStatement newClosureBlockStatement = TryCatchBlockGenerator.generateTryCatchBlock(
                isPostcondition ? ClassHelper.makeWithoutCaching(PostconditionViolation.class) : ClassHelper.makeWithoutCaching(PreconditionViolation.class),
                "<" + annotationNode.getClassNode().getName() + "> " + classNode.getName() + "." + methodNode.getTypeDescriptor() + " \n\n",
                AssertStatementCreationUtility.getAssertionStatements(booleanExpressions)
        );

        newClosureBlockStatement.setSourcePosition(closureBlockStatement);

        ClosureExpression rewrittenClosureExpression = new ClosureExpression(parameters.toArray(Parameter.EMPTY_ARRAY), newClosureBlockStatement);
        rewrittenClosureExpression.setSourcePosition(closureExpression);
        rewrittenClosureExpression.setDeclaringClass(closureExpression.getDeclaringClass());
        rewrittenClosureExpression.setSynthetic(true);
        rewrittenClosureExpression.setVariableScope(correctVariableScope(closureExpression.getVariableScope(), methodNode));
        rewrittenClosureExpression.setType(closureExpression.getType());

        boolean isConstructor = methodNode instanceof ConstructorNode;
        ClassNode closureClassNode = contractClosureWriter.createClosureClass(classNode, methodNode, rewrittenClosureExpression, isPostcondition && !isConstructor, isPostcondition && !isConstructor, Opcodes.ACC_PUBLIC);
        classNode.getModule().addClass(closureClassNode);

        final ClassExpression value = new ClassExpression(closureClassNode);
        value.setSourcePosition(annotationNode);

        BlockStatement value1 = TryCatchBlockGenerator.generateTryCatchBlockForInlineMode(
                isPostcondition ? ClassHelper.makeWithoutCaching(PostconditionViolation.class) : ClassHelper.makeWithoutCaching(PreconditionViolation.class),
                "<" + annotationNode.getClassNode().getName() + "> " + classNode.getName() + "." + methodNode.getTypeDescriptor() + " \n\n",
                AssertStatementCreationUtility.getAssertionStatements(booleanExpressions)
        );
        value1.setNodeMetaData(META_DATA_USE_EXECUTION_TRACKER, validator.isMethodCalls());

        value.setNodeMetaData(META_DATA_ORIGINAL_TRY_CATCH_BLOCK, value1);
        annotationNode.setMember(CLOSURE_ATTRIBUTE_NAME, value);

        markClosureReplaced(methodNode);
    }

    private VariableScope correctVariableScope(VariableScope variableScope, MethodNode methodNode) {
        if (variableScope == null) return null;
        if (methodNode == null || methodNode.getParameters() == null || methodNode.getParameters().length == 0)
            return variableScope;

        VariableScope copy = copy(variableScope);

        for (Iterator<Variable> iterator = variableScope.getReferencedClassVariablesIterator(); iterator.hasNext(); ) {
            Variable variable = iterator.next();
            String name = variable.getName();

            for (Parameter parameter : methodNode.getParameters()) {
                if (parameter.getName().equals(name)) {
                    copy.putReferencedLocalVariable(parameter);
                    break;
                }
            }

            if (!copy.isReferencedLocalVariable(name)) {
                copy.putReferencedClassVariable(variable);
            }
        }

        return copy;
    }

    private VariableScope copy(VariableScope original) {
        VariableScope copy = new VariableScope(original.getParent());
        copy.setClassScope(original.getClassScope());
        copy.setInStaticContext(original.isInStaticContext());
        return copy;
    }

    private void markProcessed(ASTNode someNode) {
        if (someNode.getNodeMetaData(PROCESSED) == null)
            someNode.setNodeMetaData(PROCESSED, Boolean.TRUE);
    }

    private void markClosureReplaced(ASTNode someNode) {
        if (someNode.getNodeMetaData(CLOSURE_REPLACED) == null)
            someNode.setNodeMetaData(CLOSURE_REPLACED, Boolean.TRUE);
    }

    static class ClosureExpressionValidator extends ClassCodeVisitorSupport implements Opcodes {

        private final ClassNode classNode;
        private final MethodNode methodNode;
        private final AnnotationNode annotationNode;
        private final SourceUnit sourceUnit;

        private final Map<VariableExpression, StaticMethodCallExpression> variableExpressions;

        private boolean secondPass = false;
        private boolean methodCalls = false;

        public ClosureExpressionValidator(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode, SourceUnit sourceUnit) {
            this.classNode = classNode;
            this.methodNode = methodNode;
            this.annotationNode = annotationNode;
            this.sourceUnit = sourceUnit;
            this.variableExpressions = new HashMap<>();
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            secondPass = false;

            if (expression.getCode() == null || expression.getCode() instanceof EmptyStatement) {
                addError("[groovy-contracts] Annotation does not contain any expressions (e.g. use '@Requires({ argument1 })').", expression);
            }

            if (expression.getCode() instanceof BlockStatement &&
                    ((BlockStatement) expression.getCode()).getStatements().isEmpty()) {
                addError("[groovy-contracts] Annotation does not contain any expressions (e.g. use '@Requires({ argument1 })').", expression);
            }

            if (expression.isParameterSpecified() && !AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), POSTCONDITION_TYPE_NAME)) {
                addError("[groovy-contracts] Annotation does not support parameters (the only exception are postconditions).", expression);
            }

            if (expression.isParameterSpecified()) {
                for (Parameter param : expression.getParameters()) {
                    if (!("result".equals(param.getName()) || "old".equals(param.getName()))) {
                        addError("[groovy-contracts] Postconditions only allow 'old' and 'result' closure parameters.", expression);
                    }

                    if (!param.isDynamicTyped()) {
                        addError("[groovy-contracts] Postconditions do not support explicit types.", expression);
                    }
                }
            }

            super.visitClosureExpression(expression);
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {

            // in case of a FieldNode, checks whether the FieldNode can be replaced with a Parameter
            Variable accessedVariable = getParameterCandidate(expression.getAccessedVariable());
            if (accessedVariable instanceof FieldNode) {
                FieldNode fieldNode = (FieldNode) accessedVariable;

                if (fieldNode.isPrivate() && !classNode.hasProperty(fieldNode.getName())) {
                    // if this is a class invariant we'll change the field node access
                    StaticMethodCallExpression field = callX(FIELD_VALUES, "fieldValue", args(VariableExpression.THIS_EXPRESSION, constX(fieldNode.getName()), classX(fieldNode.getType())));
                    variableExpressions.put(expression, field);
                }
            }

            if (accessedVariable instanceof Parameter) {
                Parameter parameter = (Parameter) accessedVariable;
                if ("it".equals(parameter.getName())) {
                    addError("[groovy-contracts] Access to 'it' is not supported.", expression);
                }
            }

            expression.setAccessedVariable(accessedVariable);

            super.visitVariableExpression(expression);
        }

        @Override
        public void visitPostfixExpression(PostfixExpression expression) {
            checkOperation(expression, expression.getOperation());

            if (secondPass) {
                if (expression.getExpression() instanceof VariableExpression) {
                    VariableExpression variableExpression = (VariableExpression) expression.getExpression();
                    if (variableExpressions.containsKey(variableExpression)) {
                        expression.setExpression(variableExpressions.get(variableExpression));
                    }
                }
            }

            super.visitPostfixExpression(expression);
        }

        @Override
        public void visitPrefixExpression(PrefixExpression expression) {
            checkOperation(expression, expression.getOperation());

            if (secondPass) {
                if (expression.getExpression() instanceof VariableExpression) {
                    VariableExpression variableExpression = (VariableExpression) expression.getExpression();
                    if (variableExpressions.containsKey(variableExpression)) {
                        expression.setExpression(variableExpressions.get(variableExpression));
                    }
                }
            }

            super.visitPrefixExpression(expression);
        }

        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            checkOperation(expression, expression.getOperation());

            if (secondPass) {
                if (expression.getLeftExpression() instanceof VariableExpression) {
                    VariableExpression variableExpression = (VariableExpression) expression.getLeftExpression();
                    if (variableExpressions.containsKey(variableExpression)) {
                        expression.setLeftExpression(variableExpressions.get(variableExpression));
                    }
                }
                if (expression.getRightExpression() instanceof VariableExpression) {
                    VariableExpression variableExpression = (VariableExpression) expression.getRightExpression();
                    if (variableExpressions.containsKey(variableExpression)) {
                        expression.setRightExpression(variableExpressions.get(variableExpression));
                    }
                }
            }

            super.visitBinaryExpression(expression);
        }

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
            methodCalls = true;
            super.visitStaticMethodCallExpression(call);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            methodCalls = true;
            super.visitMethodCallExpression(call);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression call) {
            methodCalls = true;
            super.visitConstructorCallExpression(call);
        }

        private void checkOperation(Expression expression, Token operation) {
            if (Types.ofType(operation.getType(), Types.ASSIGNMENT_OPERATOR)) {
                addError("[groovy-contracts] Assignment operators are not supported.", expression);
            }
            if (Types.ofType(operation.getType(), Types.POSTFIX_OPERATOR)) {
                addError("[groovy-contracts] State changing postfix & prefix operators are not supported.", expression);
            }
        }

        private Variable getParameterCandidate(Variable variable) {
            if (variable == null || methodNode == null) return variable;
            if (variable instanceof Parameter) return variable;

            String name = variable.getName();
            for (Parameter param : methodNode.getParameters()) {
                if (name.equals(param.getName())) return param;
            }

            return variable;
        }

        public void secondPass(ClosureExpression closureExpression) {
            secondPass = true;
            super.visitClosureExpression(closureExpression);
        }

        public boolean isMethodCalls() {
            return methodCalls;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }
    }

    private static class OldPropertyExpressionTransformer extends ClassCodeExpressionTransformer {
        private final MethodNode methodNode;
        private CastExpression currentCast = null;

        public OldPropertyExpressionTransformer(MethodNode methodNode) {
            this.methodNode = methodNode;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public Expression transform(Expression expr) {
            if (expr instanceof CastExpression) {
                CastExpression saved = currentCast;
                currentCast = (CastExpression) expr;
                Expression result = expr.transformExpression(this);
                currentCast = saved;
                return result;
            }
            if (expr instanceof PropertyExpression && expr.getNodeMetaData(PROCESSED) == null && (currentCast == null || expr != currentCast.getExpression())) {
                // add a cast but only if an explicit cast is not already there and we haven't been here before
                PropertyExpression propExpr = (PropertyExpression) super.transform(expr);
                Expression objExpr = propExpr.getObjectExpression();
                if (objExpr instanceof VariableExpression) {
                    VariableExpression varExpr = (VariableExpression) objExpr;
                    if ("old".equals(varExpr.getName())) {
                        String propName = propExpr.getPropertyAsString();
                        ClassNode declaringClass = methodNode.getDeclaringClass();
                        if (declaringClass != null && declaringClass.getField(propName) != null) {
                            CastExpression adjusted = new CastExpression(declaringClass.getField(propName).getType(), expr);
                            adjusted.setSourcePosition(expr);
                            expr.setNodeMetaData(PROCESSED, Boolean.TRUE);
                            return adjusted;
                        }
                    }
                }
            }
            return expr.transformExpression(this);
        }
    }
}