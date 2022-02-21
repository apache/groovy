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
package org.codehaus.groovy.transform;

import groovy.lang.Reference;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static java.util.Collections.addAll;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;
import static org.codehaus.groovy.ast.tools.ClosureUtils.hasImplicitParameter;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;

/**
 * Handles generation of code for the @Category annotation.
 * <p>
 * Transformation logic is as follows:
 * <ul>
 * <li>all non-static methods converted to static ones with an additional 'self' parameter</li>
 * <li>references to 'this' changed to the additional 'self' parameter</li>
 * </ul>
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class CategoryASTTransformation implements ASTTransformation {

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit sourceUnit) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof ClassNode)) {
            sourceUnit.addError(new SyntaxException("@Category can only be added to a ClassNode but got: " + (nodes.length == 2 ? nodes[1] : "nothing"), nodes[0].getLineNumber(), nodes[0].getColumnNumber()));
        }

        ClassNode sourceClass = (ClassNode) nodes[1];
        ClassNode targetClass; // the declared type of "self"
        Expression value = ((AnnotationNode) nodes[0]).getMember("value");
        if (value instanceof ClassExpression) {
            targetClass = value.getType();
        } else {
            targetClass = ClassHelper.OBJECT_TYPE; // TODO: ClassHelper.make((Class<?>)groovy.lang.Category.class.getMethod("value").getDefaultValue());
            sourceUnit.addErrorAndContinue(new SyntaxException("@Category must define 'value' which is the class to apply this category to", nodes[0]));
        }

        if (ensureNoInstanceFieldOrProperty(sourceClass, sourceUnit)) {
            transformReferencesToThis(targetClass, sourceClass, sourceUnit);
            new VariableScopeVisitor(sourceUnit, true).visitClass(sourceClass);
        }
    }

    private static boolean ensureNoInstanceFieldOrProperty(final ClassNode sourceClass, final SourceUnit sourceUnit) {
        boolean valid = true;
        for (FieldNode fieldNode : sourceClass.getFields()) {
            if (!fieldNode.isStatic() && fieldNode.getLineNumber() > 0) { // if < 1, probably an AST transform or internal code (like generated metaclass field, ...)
                addUnsupportedInstanceMemberError(fieldNode.getName(), fieldNode,  sourceUnit);
                valid = false;
            }
        }
        for (PropertyNode propertyNode : sourceClass.getProperties()) {
            if (!propertyNode.isStatic() && propertyNode.getLineNumber() > 0) { // if < 1, probably an AST transform or internal code (like generated metaclass field, ...)
                addUnsupportedInstanceMemberError(propertyNode.getName(), propertyNode, sourceUnit);
                valid = false;
            }
        }
        return valid;
    }

    private static void addUnsupportedInstanceMemberError(final String name, final ASTNode node, final SourceUnit unit) {
        unit.addErrorAndContinue(new SyntaxException("The @Category transformation does not support instance " + (node instanceof FieldNode ? "fields" : "properties") + " but found [" + name + "]", node));
    }

    //--------------------------------------------------------------------------

    private void transformReferencesToThis(final ClassNode targetClass, final ClassNode sourceClass, final SourceUnit sourceUnit) {
        final Reference<Parameter> selfParameter = new Reference<>();
        final LinkedList<Set<String>> varStack = new LinkedList<>();

        Set<String> names = new HashSet<>();
        for (FieldNode fn : sourceClass.getFields()) names.add(fn.getName());
        for (PropertyNode pn : sourceClass.getProperties()) names.add(pn.getName());
        varStack.add(names);

        ClassCodeExpressionTransformer transformer = new ClassCodeExpressionTransformer() {
            private boolean inClosure; // GROOVY-6510: track closure containment

            private void addVariablesToStack(final Parameter[] parameter) {
                Set<String> names = new HashSet<>(varStack.getLast());
                for (Parameter p : parameter) names.add(p.getName());
                varStack.add(names);
            }

            private Expression createThisExpression() {
                VariableExpression ve = new VariableExpression("$this", targetClass);
                ve.setClosureSharedVariable(true);
                return ve;
            }

            @Override
            protected SourceUnit getSourceUnit() {
                return sourceUnit;
            }

            @Override
            public Expression transform(final Expression expression) {
                if (expression instanceof VariableExpression) {
                    VariableExpression ve = (VariableExpression) expression;
                    if (ve.isThisExpression()) {
                        Expression thisExpression = createThisExpression();
                        thisExpression.setSourcePosition(ve);
                        return thisExpression;
                    } else if (!inClosure && !ve.isSuperExpression() && !varStack.getLast().contains(ve.getName())) {
                        PropertyExpression pe = new PropertyExpression(createThisExpression(), ve.getName());
                        pe.setSourcePosition(ve);
                        return pe;
                    }
                } else if (expression instanceof MethodCallExpression) {
                    MethodCallExpression mce = (MethodCallExpression) expression;
                    if (inClosure && mce.isImplicitThis() && isThisExpression(mce.getObjectExpression())) {
                        // GROOVY-6510: preserve implicit-this semantics
                        mce.setArguments(transform(mce.getArguments()));
                        mce.setMethod(transform(mce.getMethod()));
                        return mce;
                    }
                } else if (expression instanceof ClosureExpression) {
                    ClosureExpression ce = (ClosureExpression) expression;
                    addVariablesToStack(hasImplicitParameter(ce) ? params(param(ClassHelper.OBJECT_TYPE, "it")) : getParametersSafe(ce));
                    ce.getVariableScope().putReferencedLocalVariable(selfParameter.get());
                    addAll(varStack.getLast(), "owner", "delegate", "thisObject");
                    boolean closure = inClosure; inClosure = true;
                    ce.getCode().visit(this);
                    varStack.removeLast();
                    inClosure = closure;
                }
                return super.transform(expression);
            }

            @Override
            public void visitBlockStatement(final BlockStatement statement) {
                Set<String> names = new HashSet<>(varStack.getLast());
                varStack.add(names);
                super.visitBlockStatement(statement);
                varStack.remove(names);
            }

            @Override
            public void visitCatchStatement(final CatchStatement statement) {
                varStack.getLast().add(statement.getVariable().getName());
                super.visitCatchStatement(statement);
                varStack.getLast().remove(statement.getVariable().getName());
            }

            @Override
            public void visitClosureExpression(final ClosureExpression expression) {
            }

            @Override
            public void visitDeclarationExpression(final DeclarationExpression expression) {
                if (expression.isMultipleAssignmentDeclaration()) {
                    for (Expression e : expression.getTupleExpression().getExpressions()) {
                        VariableExpression ve = (VariableExpression) e;
                        varStack.getLast().add(ve.getName());
                    }
                } else {
                    VariableExpression ve = expression.getVariableExpression();
                    varStack.getLast().add(ve.getName());
                }
                super.visitDeclarationExpression(expression);
            }

            @Override
            public void visitExpressionStatement(final ExpressionStatement statement) {
                // GROOVY-3543: visit the declaration expressions so that declaration variables get added on the varStack
                if (statement.getExpression() instanceof DeclarationExpression) {
                    statement.getExpression().visit(this);
                }
                super.visitExpressionStatement(statement);
            }

            @Override
            public void visitForLoop(final ForStatement statement) {
                Expression exp = statement.getCollectionExpression();
                exp.visit(this);
                Parameter loopParam = statement.getVariable();
                if (loopParam != null) {
                    varStack.getLast().add(loopParam.getName());
                }
                super.visitForLoop(statement);
            }

            @Override
            public void visitMethod(final MethodNode node) {
                addVariablesToStack(node.getParameters());
                super.visitMethod(node);
                varStack.removeLast();
            }
        };

        for (MethodNode method : sourceClass.getMethods()) {
            if (!method.isStatic()) {
                Parameter p = new Parameter(targetClass, "$this");
                p.setClosureSharedVariable(true);
                selfParameter.set(p);

                Parameter[] oldParams = method.getParameters();
                Parameter[] newParams = new Parameter[oldParams.length + 1];
                newParams[0] = p;
                System.arraycopy(oldParams, 0, newParams, 1, oldParams.length);

                method.setModifiers(method.getModifiers() | Opcodes.ACC_STATIC);
                method.setParameters(newParams);
                transformer.visitMethod(method);
            }
        }
    }
}
