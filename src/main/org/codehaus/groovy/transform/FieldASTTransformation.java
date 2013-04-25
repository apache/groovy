/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform;

import groovy.transform.Field;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Handles transformation for the @Field annotation.
 *
 * @author Paul King
 * @author Cedric Champeau
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class FieldASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = Field.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode ASTTRANSFORMCLASS_TYPE = ClassHelper.make(GroovyASTTransformationClass.class);
    private SourceUnit sourceUnit;
    private DeclarationExpression candidate;
    private boolean insideScriptBody;
    private String variableName;
    private FieldNode fieldNode;
    private ClosureExpression currentClosure;

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
                addError("Error: annotation " + MY_TYPE_NAME + " can only be used within a Script.", parent);
                return;
            }
            candidate = de;
            // GROOVY-4548: temp fix to stop CCE until proper support is added
            if (de.isMultipleAssignmentDeclaration()) {
                addError("Error: annotation " + MY_TYPE_NAME + " not supported with multiple assignment notation.", parent);
                return;
            }
            VariableExpression ve = de.getVariableExpression();
            variableName = ve.getName();
            // set owner null here, it will be updated by addField
            fieldNode = new FieldNode(variableName, ve.getModifiers(), ve.getType(), null, de.getRightExpression());
            fieldNode.setSourcePosition(de);
            cNode.addField(fieldNode);

            // GROOVY-4833 : annotations that are not Groovy transforms should be transferred to the generated field
            // GROOVY-6112 : also copy acceptable Groovy transforms
            final List<AnnotationNode> annotations = de.getAnnotations();
            for (AnnotationNode annotation : annotations) {
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

    private boolean acceptableTransform(AnnotationNode annotation) {
        // TODO also check for phase after sourceUnit.getPhase()? but will be ignored anyway?
        // TODO we should only copy those annotations with FIELD_TARGET but haven't visited annotations
        // and gathered target info at this phase, so we can't do this:
        // return annotation.isTargetAllowed(AnnotationNode.FIELD_TARGET);
        // instead just don't copy ourselves for now
        return !annotation.getClassNode().equals(MY_TYPE);
    }

    private boolean notTransform(ClassNode annotationClassNode) {
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
                addError("Error: annotation " + MY_TYPE_NAME + " can only be used within a Script body.", expr);
                return expr;
            }
        } else if (insideScriptBody && expr instanceof VariableExpression && currentClosure != null) {
            VariableExpression ve = (VariableExpression) expr;
            if (ve.getName().equals(variableName)) {
                // we may only check the variable name because the Groovy compiler
                // already fails if a variable with the same name already exists in the scope.
                // this means that a closure cannot shadow a class variable
                ve.setAccessedVariable(fieldNode);
                final VariableScope variableScope = currentClosure.getVariableScope();
                final Iterator<Variable> iterator = variableScope.getReferencedLocalVariablesIterator();
                while (iterator.hasNext()) {
                    Variable next = iterator.next();
                    if (next.getName().equals(variableName)) iterator.remove();
                }
                variableScope.putReferencedClassVariable(fieldNode);
                return ve;
            }
        }
        return expr.transformExpression(this);
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        ClosureExpression old = currentClosure;
        currentClosure = expression;
        super.visitClosureExpression(expression);
        currentClosure = old;
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
        if (exp instanceof BinaryExpression) {
            exp.visit(this);
        }
        super.visitExpressionStatement(es);
    }

    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
}
