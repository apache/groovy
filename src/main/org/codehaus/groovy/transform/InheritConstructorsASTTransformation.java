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

import groovy.transform.InheritConstructors;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles generation of code for the {@code @}InheritConstructors annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class InheritConstructorsASTTransformation implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = InheritConstructors.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof ClassNode) {
            processClass((ClassNode) parent, source);
        }
    }

    private void processClass(ClassNode cNode, SourceUnit source) {
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cNode.getName() +
                    "'. " + MY_TYPE_NAME + " only allowed for classes.", cNode, source);
            return;
        }
        ClassNode sNode = cNode.getSuperClass();
        List<AnnotationNode> superAnnotations = sNode.getAnnotations(MY_TYPE);
        if (superAnnotations.size() == 1) {
            // We need @InheritConstructors from parent classes processed first
            // so force that order here. The transformation is benign on an already
            // processed node so processing twice in any order won't matter bar
            // a very small time penalty.
            processClass(sNode, source);
        }
        for (ConstructorNode cn : sNode.getDeclaredConstructors()) {
            addConstructorUnlessAlreadyExisting(cNode, cn);
        }
    }

    private void addConstructorUnlessAlreadyExisting(ClassNode classNode, ConstructorNode consNode) {
        Parameter[] origParams = consNode.getParameters();
        if (consNode.isPrivate()) return;
        Parameter[] params = new Parameter[origParams.length];
        List<Expression> args = new ArrayList<Expression>();
        for (int i = 0; i < origParams.length; i++) {
            Parameter p = origParams[i];
            params[i] = p.hasInitialExpression() ?
                    new Parameter(p.getType(), p.getName(), p.getInitialExpression()) :
                    new Parameter(p.getType(), p.getName());
            args.add(new VariableExpression(p.getName(), p.getType()));
        }
        if (isExisting(classNode, params)) return;
        BlockStatement body = new BlockStatement();
        body.addStatement(new ExpressionStatement(
                new ConstructorCallExpression(ClassNode.SUPER, new ArgumentListExpression(args))));
        classNode.addConstructor(consNode.getModifiers(), params, consNode.getExceptions(), body);
    }

    private boolean isExisting(ClassNode classNode, Parameter[] params) {
        for (ConstructorNode consNode : classNode.getDeclaredConstructors()) {
            if (matchingTypes(params, consNode.getParameters())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchingTypes(Parameter[] params, Parameter[] existingParams) {
        if (params.length != existingParams.length) return false;
        for (int i = 0; i < params.length; i++) {
            if (!params[i].getType().equals(existingParams[i].getType())) {
                return false;
            }
        }
        return true;
    }

    private void addError(String msg, ASTNode expr, SourceUnit source) {
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(),
                        expr.getLastLineNumber(), expr.getLastColumnNumber()), source)
        );
    }

}
