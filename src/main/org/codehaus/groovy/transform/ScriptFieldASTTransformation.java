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

import groovy.lang.Script;
import groovy.transform.ScriptField;
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
 * Handles transformation for the @ScriptField annotation.
 * This is experimental, use at your own risk.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ScriptFieldASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = ScriptField.class;
    private static final ClassNode MY_TYPE = new ClassNode(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode SCRIPT_TYPE = new ClassNode(Script.class);
    private SourceUnit sourceUnit;
    private DeclarationExpression candidate;

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
            if (!cNode.isDerivedFrom(SCRIPT_TYPE)) {
                addError("Error: annotation " + MY_TYPE_NAME + " can only be used within a Script.", parent);
            }
            VariableExpression ve = de.getVariableExpression();
            cNode.addField(ve.getName(), ve.getModifiers(), ve.getType(), de.getRightExpression());
            candidate = de;
            super.visitClass(cNode);
        }
    }

    public Expression transform(Expression expr) {
        if (expr == null) return null;
        if (expr instanceof DeclarationExpression && expr == candidate) {
            return new ConstantExpression(null);
        }
        return expr.transformExpression(this);
    }

    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
}
