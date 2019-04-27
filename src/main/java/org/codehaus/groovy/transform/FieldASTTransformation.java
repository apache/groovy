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

import groovy.cli.Option;
import groovy.lang.Lazy;
import groovy.transform.Field;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles transformation for the @Field annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class FieldASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = Field.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final ClassNode LAZY_TYPE = make(Lazy.class);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode ASTTRANSFORMCLASS_TYPE = make(GroovyASTTransformationClass.class);
    private static final ClassNode OPTION_TYPE = make(Option.class);
    private SourceUnit sourceUnit;
    private DeclarationExpression candidate;
    private boolean insideScriptBody;
    private String variableName;
    private FieldNode fieldNode;
    private ClosureExpression currentClosure;
    private ConstructorCallExpression currentAIC;

    public void visit(ASTNode[] nodes, SourceUnit source) {
        sourceUnit = source;
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) parent;
            ClassNode cNode = de.getDeclaringClass();
            if (!cNode.isScript()) {
                addError("Annotation " + MY_TYPE_NAME + " can only be used within a Script.", parent);
                return;
            }
            candidate = de;
            // GROOVY-4548: temp fix to stop CCE until proper support is added
            if (de.isMultipleAssignmentDeclaration()) {
                addError("Annotation " + MY_TYPE_NAME + " not supported with multiple assignment notation.", parent);
                return;
            }
            VariableExpression ve = de.getVariableExpression();
            variableName = ve.getName();
            // set owner null here, it will be updated by addField
            fieldNode = new FieldNode(variableName, ve.getModifiers(), ve.getType(), null, de.getRightExpression());
            fieldNode.setSourcePosition(de);
            cNode.addField(fieldNode);
            // provide setter for CLI Builder purposes unless final
            if (fieldNode.isFinal()) {
                if (!de.getAnnotations(OPTION_TYPE).isEmpty()) {
                    addError("Can't have a final field also annotated with @" + OPTION_TYPE.getNameWithoutPackage(), de);
                }
            } else {
                String setterName = "set" + capitalize(variableName);
                cNode.addMethod(setterName, ACC_PUBLIC | ACC_SYNTHETIC, ClassHelper.VOID_TYPE, params(param(ve.getType(), variableName)), ClassNode.EMPTY_ARRAY, block(
                        stmt(assignX(propX(varX("this"), variableName), varX(variableName)))
                ));
            }

            // GROOVY-4833 : annotations that are not Groovy transforms should be transferred to the generated field
            // GROOVY-6112 : also copy acceptable Groovy transforms
            final List<AnnotationNode> annotations = de.getAnnotations();
            for (AnnotationNode annotation : annotations) {
                // GROOVY-6337 HACK: in case newly created field is @Lazy
                if (annotation.getClassNode().equals(LAZY_TYPE)) {
                    LazyASTTransformation.visitField(this, annotation, fieldNode);
                }
                final ClassNode annotationClassNode = annotation.getClassNode();
                if (notTransform(annotationClassNode) || acceptableTransform(annotation)) {
                    fieldNode.addAnnotation(annotation);
                }
            }

            super.visitClass(cNode);
            // GROOVY-5207 So that Closures can see newly added fields
            // (not super efficient for a very large class with many @Fields but we chose simplicity
            // and understandability of this solution over more complex but efficient alternatives)
            VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(source);
            scopeVisitor.visitClass(cNode);
        }
    }

    private static boolean acceptableTransform(AnnotationNode annotation) {
        // TODO also check for phase after sourceUnit.getPhase()? but will be ignored anyway?
        // TODO we should only copy those annotations with FIELD_TARGET but haven't visited annotations
        // and gathered target info at this phase, so we can't do this:
        // return annotation.isTargetAllowed(AnnotationNode.FIELD_TARGET);
        // instead just don't copy ourselves for now
        return !annotation.getClassNode().equals(MY_TYPE);
    }

    private static boolean notTransform(ClassNode annotationClassNode) {
        return annotationClassNode.getAnnotations(ASTTRANSFORMCLASS_TYPE).isEmpty();
    }

    @Override
    public Expression transform(Expression expr) {
        if (expr == null) return null;
        if (expr instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) expr;
            if (de.getLeftExpression() == candidate.getLeftExpression()) {
                if (insideScriptBody) {
                    // TODO make EmptyExpression work
                    // partially works but not if only thing in script
                    // return EmptyExpression.INSTANCE;
                    return new ConstantExpression(null);
                }
                addError("Annotation " + MY_TYPE_NAME + " can only be used within a Script body.", expr);
                return expr;
            }
        } else if (insideScriptBody && expr instanceof VariableExpression && currentClosure != null) {
            VariableExpression ve = (VariableExpression) expr;
            if (ve.getName().equals(variableName)) {
                adjustToClassVar(ve);
                return ve;
            }
        } else if (currentAIC != null && expr instanceof ArgumentListExpression) {
            // if a match is found, the compiler will have already set up aic constructor to hav
            // an argument which isn't needed since we'll be accessing the field; we must undo it
            Expression skip = null;
            List<Expression> origArgList = ((ArgumentListExpression) expr).getExpressions();
            for (int i = 0; i < origArgList.size(); i++) {
                Expression arg = origArgList.get(i);
                if (matchesCandidate(arg)) {
                    skip = arg;
                    adjustConstructorAndFields(i, currentAIC.getType());
                    break;
                }
            }
            if (skip != null) {
                return adjustedArgList(skip, origArgList);
            }
        }
        return expr.transformExpression(this);
    }

    private boolean matchesCandidate(Expression arg) {
        return arg instanceof VariableExpression && ((VariableExpression) arg).getAccessedVariable() == candidate.getVariableExpression().getAccessedVariable();
    }

    private Expression adjustedArgList(Expression skip, List<Expression> origArgs) {
        List<Expression> newArgs = new ArrayList<Expression>(origArgs.size() - 1);
        for (Expression origArg : origArgs) {
            if (skip != origArg) {
                newArgs.add(origArg);
            }
        }
        return new ArgumentListExpression(newArgs);
    }

    private void adjustConstructorAndFields(int skipIndex, ClassNode type) {
        List<ConstructorNode> constructors = type.getDeclaredConstructors();
        if (constructors.size() == 1) {
            ConstructorNode constructor = constructors.get(0);
            Parameter[] params = constructor.getParameters();
            Parameter[] newParams = new Parameter[params.length - 1];
            int to = 0;
            for (int from = 0; from < params.length; from++) {
                if (from != skipIndex) {
                    newParams[to++] = params[from];
                }
            }
            type.removeConstructor(constructor);
            // code doesn't mention the removed param at this point, okay to leave as is
            addGeneratedConstructor(type, constructor.getModifiers(), newParams, constructor.getExceptions(), constructor.getCode());
            type.removeField(variableName);
        }
    }

    private void adjustToClassVar(VariableExpression expr) {
        // we only need to check the variable name because the Groovy compiler
        // already fails if a variable with the same name already exists in the scope.
        // this means that a closure cannot shadow a class variable
        expr.setAccessedVariable(fieldNode);
        final VariableScope variableScope = currentClosure.getVariableScope();
        final Iterator<Variable> iterator = variableScope.getReferencedLocalVariablesIterator();
        while (iterator.hasNext()) {
            Variable next = iterator.next();
            if (next.getName().equals(variableName)) iterator.remove();
        }
        variableScope.putReferencedClassVariable(fieldNode);
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        ClosureExpression old = currentClosure;
        currentClosure = expression;
        super.visitClosureExpression(expression);
        currentClosure = old;
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression cce) {
        if (!insideScriptBody || !cce.isUsingAnonymousInnerClass()) return;
        ConstructorCallExpression old = currentAIC;
        currentAIC = cce;
        Expression newArgs = transform(cce.getArguments());
        if (cce.getArguments() instanceof TupleExpression && newArgs instanceof TupleExpression) {
            List<Expression> argList = ((TupleExpression) cce.getArguments()).getExpressions();
            argList.clear();
            argList.addAll(((TupleExpression) newArgs).getExpressions());
        }
        currentAIC = old;
    }

    @Override
    public void visitMethod(MethodNode node) {
        Boolean oldInsideScriptBody = insideScriptBody;
        if (node.isScriptBody()) insideScriptBody = true;
        super.visitMethod(node);
        insideScriptBody = oldInsideScriptBody;
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement es) {
        Expression exp = es.getExpression();
        exp.visit(this);
        super.visitExpressionStatement(es);
    }

    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
}
