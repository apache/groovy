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
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


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
public class CategoryASTTransformation implements ASTTransformation, Opcodes {
    // should not use a static variable because of possible changes to node metadata
    // which would be visible to other compilation units
    private final VariableExpression thisExpression = createThisExpression();

    private static VariableExpression createThisExpression() {
        VariableExpression expr = new VariableExpression("$this");
        expr.setClosureSharedVariable(true);
        return expr;
    }

    /**
     * Property invocations done on 'this' reference are transformed so that the invocations at runtime are
     * done on the additional parameter 'self'
     */
    public void visit(ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof ClassNode)) {
            source.getErrorCollector().addError(
                    new SyntaxErrorMessage(new SyntaxException("@Category can only be added to a ClassNode but got: " + (nodes.length==2?nodes[1]:"nothing"),
                        nodes[0].getLineNumber(), nodes[0].getColumnNumber()), source));
        }

        AnnotationNode annotation = (AnnotationNode) nodes[0];
        ClassNode parent = (ClassNode) nodes[1];

        ClassNode targetClass = getTargetClass(source, annotation);
        thisExpression.setType(targetClass);

        final LinkedList<Set<String>> varStack = new LinkedList<Set<String>>();
        if (!ensureNoInstanceFieldOrProperty(source, parent)) return;

        Set<String> names = new HashSet<String>();
        for (FieldNode field : parent.getFields()) {
            names.add(field.getName());
        }
        for (PropertyNode field : parent.getProperties()) {
            names.add(field.getName());
        }
        varStack.add(names);

        final Reference parameter = new Reference();
        final ClassCodeExpressionTransformer expressionTransformer = new ClassCodeExpressionTransformer() {
            protected SourceUnit getSourceUnit() {
                return source;
            }

            private void addVariablesToStack(Parameter[] params) {
                Set<String> names = new HashSet<String>(varStack.getLast());
                for (Parameter param : params) {
                    names.add(param.getName());
                }
                varStack.add(names);
            }

            @Override
            public void visitCatchStatement(CatchStatement statement) {
                varStack.getLast().add(statement.getVariable().getName());
                super.visitCatchStatement(statement);
                varStack.getLast().remove(statement.getVariable().getName());
            }

            @Override
            public void visitMethod(MethodNode node) {
                addVariablesToStack(node.getParameters());
                super.visitMethod(node);
                varStack.removeLast();
            }

            @Override
            public void visitBlockStatement(BlockStatement block) {
                Set<String> names = new HashSet<String>(varStack.getLast());
                varStack.add(names);
                super.visitBlockStatement(block);
                varStack.remove(names);
            }

            @Override
            public void visitClosureExpression(ClosureExpression ce) {
                addVariablesToStack(ce.getParameters());
                super.visitClosureExpression(ce);
                varStack.removeLast();
            }

            @Override
            public void visitDeclarationExpression(DeclarationExpression expression) {
                if (expression.isMultipleAssignmentDeclaration()) {
                    TupleExpression te = expression.getTupleExpression();
                    List<Expression> list = te.getExpressions();
                    for (Expression arg : list) {
                        VariableExpression ve = (VariableExpression) arg;
                        varStack.getLast().add(ve.getName());
                    }
                } else {
                    VariableExpression ve = expression.getVariableExpression();
                    varStack.getLast().add(ve.getName());
                }
                super.visitDeclarationExpression(expression);
            }

            @Override
            public void visitForLoop(ForStatement forLoop) {
                Expression exp = forLoop.getCollectionExpression();
                exp.accept(this);
                Parameter loopParam = forLoop.getVariable();
                if (loopParam != null) {
                    varStack.getLast().add(loopParam.getName());
                }
                super.visitForLoop(forLoop);
            }

            @Override
            public void visitExpressionStatement(ExpressionStatement es) {
                // GROOVY-3543: visit the declaration expressions so that declaration variables get added on the varStack
                Expression exp = es.getExpression();
                if (exp instanceof DeclarationExpression) {
                    exp.accept(this);
                }
                super.visitExpressionStatement(es);
            }

            @Override
            public Expression transform(Expression exp) {
                if (exp instanceof VariableExpression) {
                    VariableExpression ve = (VariableExpression) exp;
                    if (ve.getName().equals("this"))
                        return thisExpression;
                    else {
                        if (!varStack.getLast().contains(ve.getName())) {
                            return new PropertyExpression(thisExpression, ve.getName());
                        }
                    }
                } else if (exp instanceof PropertyExpression) {
                    PropertyExpression pe = (PropertyExpression) exp;
                    if (pe.getObjectExpression() instanceof VariableExpression) {
                        VariableExpression vex = (VariableExpression) pe.getObjectExpression();
                        if (vex.isThisExpression()) {
                            pe.setObjectExpression(thisExpression);
                            return pe;
                        }
                    }
                } else if (exp instanceof ClosureExpression) {
                    ClosureExpression ce = (ClosureExpression) exp;
                    ce.getVariableScope().putReferencedLocalVariable((Parameter) parameter.get());
                    Parameter[] params = ce.getParameters();
                    if (params == null) {
                        params = Parameter.EMPTY_ARRAY;
                    } else if (params.length == 0) {
                        params = new Parameter[]{
                                new Parameter(ClassHelper.OBJECT_TYPE, "it")
                        };
                    }
                    addVariablesToStack(params);
                    ce.getCode().accept(this);
                    varStack.removeLast();
                }
                return super.transform(exp);
            }
        };

        for (MethodNode method : parent.getMethods()) {
            if (!method.isStatic()) {
                method.setModifiers(method.getModifiers() | Opcodes.ACC_STATIC);
                final Parameter[] origParams = method.getParameters();
                final Parameter[] newParams = new Parameter[origParams.length + 1];
                Parameter p = new Parameter(targetClass, "$this");
                p.setClosureSharedVariable(true);
                newParams[0] = p;
                parameter.set(p);
                System.arraycopy(origParams, 0, newParams, 1, origParams.length);
                method.setParameters(newParams);

                expressionTransformer.visitMethod(method);
            }
        }
        new VariableScopeVisitor(source, true).visitClass(parent);
    }

    private static boolean ensureNoInstanceFieldOrProperty(final SourceUnit source, final ClassNode parent) {
        boolean valid = true;
        for (FieldNode fieldNode : parent.getFields()) {
            if (!fieldNode.isStatic() && fieldNode.getLineNumber()>0) {
                // if <0, probably an AST transform or internal code (like generated metaclass field, ...)
                addUnsupportedError(fieldNode,  source);
                valid = false;
            }
        }
        for (PropertyNode propertyNode : parent.getProperties()) {
            if (!propertyNode.isStatic() && propertyNode.getLineNumber()>0) {
                // if <0, probably an AST transform or internal code (like generated metaclass field, ...)
                addUnsupportedError(propertyNode, source);
                valid = false;
            }
        }
        return valid;
    }

    private static void addUnsupportedError(ASTNode node, SourceUnit unit) {
        unit.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(
                        new SyntaxException("The @Category transformation does not support instance "+
                                (node instanceof FieldNode?"fields":"properties")
                                + " but found ["+getName(node)+"]",
                                node.getLineNumber(),
                                node.getColumnNumber()

                        ), unit
                ));
    }

    private static String getName(ASTNode node) {
        if (node instanceof FieldNode) return ((FieldNode) node).getName();
        if (node instanceof PropertyNode) return ((PropertyNode) node).getName();
        return node.getText();
    }

    private static ClassNode getTargetClass(SourceUnit source, AnnotationNode annotation) {
        Expression value = annotation.getMember("value");
        if (!(value instanceof ClassExpression)) {
            //noinspection ThrowableInstanceNeverThrown
            source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
                    new SyntaxException("@groovy.lang.Category must define 'value' which is the class to apply this category to",
                            annotation.getLineNumber(), annotation.getColumnNumber(), annotation.getLastLineNumber(), annotation.getLastColumnNumber()),
                    source));
            return null;
        } else {
            ClassExpression ce = (ClassExpression) value;
            return ce.getType();
        }
    }
}
