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
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles transformation for the @Field annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class FieldASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation {

    private static final ClassNode MY_TYPE = ClassHelper.make(Field.class);
    private static final ClassNode LAZY_TYPE = ClassHelper.make(Lazy.class);
    private static final ClassNode OPTION_TYPE = ClassHelper.make(Option.class);
    private static final ClassNode ASTTRANSFORMCLASS_TYPE = ClassHelper.make(GroovyASTTransformationClass.class);

    private DeclarationExpression candidate;
    private FieldNode fieldNode;
    private String variableName;

    private ClosureExpression currentClosure;
    private boolean insideScriptBody;
    private SourceUnit sourceUnit;

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.toString(nodes));
        }

        if (!MY_TYPE.equals(((AnnotationNode) nodes[0]).getClassNode())) return;

        sourceUnit = source; // support for addError

        if (nodes[1] instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) nodes[1];
            ClassNode declaringClass = de.getDeclaringClass();
            if (!declaringClass.isScript()) {
                addError("Annotation @" + MY_TYPE.getNameWithoutPackage() + " can only be used within a Script.", de);
                return;
            }
            // GROOVY-4548: temp fix to stop CCE until proper support is added
            if (de.isMultipleAssignmentDeclaration()) {
                addError("Annotation @" + MY_TYPE.getNameWithoutPackage() + " not supported with multiple assignment notation.", de);
                return;
            }

            VariableExpression ve = de.getVariableExpression();
            variableName = ve.getName();
            candidate = de;

            // set owner null here, it will be updated by addField
            fieldNode = new FieldNode(variableName, ve.getModifiers(), ve.getType(), null, de.getRightExpression());
            fieldNode.setSourcePosition(de);
            declaringClass.addField(fieldNode);

            if (fieldNode.isFinal()) {
                if (!de.getAnnotations(OPTION_TYPE).isEmpty()) {
                    addError("Can't have a final field also annotated with @" + OPTION_TYPE.getNameWithoutPackage(), de);
                }
            } else { // provide setter for CLI Builder purposes
                String setterName = getSetterName(variableName);
                declaringClass.addMethod(setterName, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, ClassHelper.VOID_TYPE, params(param(ve.getType(), variableName)), ClassNode.EMPTY_ARRAY, block(
                        stmt(assignX(propX(varX("this"), variableName), varX(variableName)))
                ));
            }

            for (AnnotationNode annotation : de.getAnnotations()) {
                // GROOVY-6337: in case newly created field is @Lazy
                if (annotation.getClassNode().equals(LAZY_TYPE)) {
                    LazyASTTransformation.visitField(this, annotation, fieldNode);
                }
                // GROOVY-4833: copy annotations that are not Groovy transforms; GROOVY-6112: also copy acceptable Groovy transforms
                if (notTransform(annotation.getClassNode()) || acceptableTransform(annotation)) {
                    fieldNode.addAnnotation(annotation);
                }
            }

            super.visitClass(declaringClass);
            // GROOVY-5207: So that Closures can see newly added fields
            // (not super efficient for a very large class with many @Fields but we chose simplicity
            // and understandability of this solution over more complex but efficient alternatives)
            new VariableScopeVisitor(source).visitClass(declaringClass);
        }
    }

    private static boolean notTransform(final ClassNode annotationType) {
        return annotationType.getAnnotations(ASTTRANSFORMCLASS_TYPE).isEmpty();
    }

    private static boolean acceptableTransform(final AnnotationNode annotation) {
        // TODO also check for phase after sourceUnit.getPhase()? but will be ignored anyway?
        // TODO we should only copy those annotations with FIELD_TARGET but haven't visited annotations
        // and gathered target info at this phase, so we can't do this:
        // return annotation.isTargetAllowed(AnnotationNode.FIELD_TARGET);
        // instead just don't copy ourselves for now
        return !annotation.getClassNode().equals(MY_TYPE);
    }

    //

    @Override
    public void visitMethod(final MethodNode node) {
        boolean old = insideScriptBody;
        if (node.isScriptBody()) insideScriptBody = true;
        super.visitMethod(node);
        insideScriptBody = old;
    }

    @Override
    public Expression transform(final Expression expr) {
        if (expr == null) return null;
        if (expr instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) expr;
            if (de.getLeftExpression() == candidate.getVariableExpression()) {
                if (insideScriptBody) {
                    // TODO: make EmptyExpression work
                    // partially works but not if only thing in script
                    //return EmptyExpression.INSTANCE;
                    return nullX();
                }
                addError("Annotation @" + MY_TYPE.getNameWithoutPackage() + " can only be used within a Script body.", expr);
                return expr;
            }
        } else if (expr instanceof ClosureExpression) {
            var old = currentClosure; currentClosure = (ClosureExpression) expr;
            // GROOVY-4700, GROOVY-5207, GROOVY-9554
            visitClosureExpression(currentClosure);
            currentClosure = old;
        } else if (expr instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) expr;
            // only need to check the variable name because the Groovy compiler already fails if a variable
            // with the same name exists in the scope; this means a closure cannot shadow a class variable
            if (insideScriptBody && currentClosure != null && ve.getName().equals(variableName)) {
                adjustToClassVar(ve);
            }
        } else if (expr instanceof ConstructorCallExpression) {
            ConstructorCallExpression cce = (ConstructorCallExpression) expr;
            if (insideScriptBody && cce.isUsingAnonymousInnerClass()) {
                // if a match is found, the compiler will have already set up AIC constructor to have
                // an argument which isn't needed since we'll be accessing the field; we must undo it
                List<Expression> arguments = ((ArgumentListExpression) cce.getArguments()).getExpressions();
                for (int i = 0, n = arguments.size(); i < n; i += 1) { Expression argument = arguments.get(i);
                    if (matchesCandidate(argument)) { // GROOVY-8112
                        adjustConstructorAndFields(i, cce.getType());

                        var copy = new ConstructorCallExpression(cce.getType(), adjustedArgList(argument, arguments));
                        copy.setUsingAnonymousInnerClass(true);
                        copy.setSourcePosition(cce);
                        copy.copyNodeMetaData(cce);
                        return copy;
                    }
                }
            }
        }
        return expr.transformExpression(this);
    }

    private boolean matchesCandidate(final Expression expr) {
        return expr instanceof VariableExpression && ((VariableExpression) expr).getAccessedVariable() == candidate.getVariableExpression().getAccessedVariable();
    }

    private void adjustToClassVar(final VariableExpression expr) {
        expr.setAccessedVariable(fieldNode);
        VariableScope variableScope = currentClosure.getVariableScope();
        Iterator<Variable> iterator = variableScope.getReferencedLocalVariablesIterator();
        while (iterator.hasNext()) {
            Variable next = iterator.next();
            if (next.getName().equals(variableName)) iterator.remove();
        }
        variableScope.putReferencedClassVariable(fieldNode);
    }

    private void adjustConstructorAndFields(final int skipIndex, final ClassNode type) {
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

    private Expression adjustedArgList(final Expression skip, final List<Expression> origArgs) {
        List<Expression> newArgs = new ArrayList<>(origArgs.size() - 1);
        for (Expression origArg : origArgs) {
            if (skip != origArg) {
                newArgs.add(transform(origArg));
            }
        }
        return new ArgumentListExpression(newArgs);
    }
}
