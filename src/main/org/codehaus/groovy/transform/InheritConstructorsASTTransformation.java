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

import groovy.lang.InheritConstructors;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.Parameter;
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
    private static final ClassNode MY_TYPE = new ClassNode(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (cNode.isInterface()) {
                addError("Error processing interface '" + cNode.getName() +
                        "'. " + MY_TYPE_NAME + " only allowed for classes.", cNode, source);
                return;
            }
            ClassNode sNode = cNode.getSuperClass();
            for (ConstructorNode cn : sNode.getDeclaredConstructors()) {
                Parameter[] params = cn.getParameters();
                if (cn.isPrivate()) continue;
                Parameter[] pcopy = new Parameter[params.length];
                List<Expression> args = new ArrayList<Expression>();
                for (int i = 0; i < params.length; i++) {
                    Parameter p = params[i];
                    pcopy[i] = p.hasInitialExpression() ?
                            new Parameter(p.getType(), p.getName(), p.getInitialExpression()) :
                            new Parameter(p.getType(), p.getName());
                    args.add(new VariableExpression(p.getName(), p.getType()));
                }
                if (isClashing(cNode, pcopy)) continue;
                BlockStatement body = new BlockStatement();
                body.addStatement(new ExpressionStatement(new ConstructorCallExpression(ClassNode.SUPER, new ArgumentListExpression(args))));
                cNode.addConstructor(cn.getModifiers(), pcopy, cn.getExceptions(), body);
            }
        }
    }

    private boolean isClashing(ClassNode cNode, Parameter[] pcopy) {
        for (ConstructorNode cn : cNode.getDeclaredConstructors()) {
            if (conflictingTypes(pcopy, cn.getParameters())) {
                return true;
            }
        }
        return false;
    }

    private boolean conflictingTypes(Parameter[] pcopy, Parameter[] parameters) {
        if (pcopy.length != parameters.length) return false;
        for (int i = 0; i < pcopy.length; i++) {
            if (!pcopy[i].getType().equals(parameters[i].getType())) {
                return false;
            }
        }
        return true;
    }

    private void addError(String msg, ASTNode expr, SourceUnit source) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
        );
    }

}
