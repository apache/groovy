/*
 * Copyright 2008-2010 the original author or authors.
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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

/**
 * Handles transformation for the @Field annotation.
 * This is experimental, use at your own risk.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class FieldASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = Field.class;
    private static final ClassNode MY_TYPE = new ClassNode(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private SourceUnit sourceUnit;
    private DeclarationExpression candidate;
    private boolean insideScriptBody;

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
            }
            candidate = de;
            super.visitClass(cNode);
            VariableExpression ve = de.getVariableExpression();
            // set owner null here, it will be updated by addField
            FieldNode fNode = new FieldNode(ve.getName(), ve.getModifiers(), ve.getType(), null, de.getRightExpression());
            fNode.setSourcePosition(de);
            cNode.addField(fNode);
        }
    }

    public Expression transform(Expression expr) {
        if (expr == null) return null;
        if (expr instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) expr;
            if (de.getVariableExpression() == candidate.getVariableExpression()) {
                if (insideScriptBody) {
                    // TODO make EmptyExpression work
                    // partially works but not if only thing in script
                    // return EmptyExpression.INSTANCE;
                    return new ConstantExpression(null);
                }
                addError("Error: annotation " + MY_TYPE_NAME + " can only be used within a Script body.", expr);
            }
        }
        return expr.transformExpression(this);
    }

    @Override
    public void visitMethod(MethodNode node) {
        Boolean oldInsideScriptBody = insideScriptBody;
        if (node.isScriptBody()) insideScriptBody = true;
        super.visitMethod(node);
        insideScriptBody = oldInsideScriptBody;
    }

    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
}
