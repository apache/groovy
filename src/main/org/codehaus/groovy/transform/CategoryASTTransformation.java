/*
 * Copyright 2008 the original author or authors.
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

import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.util.Set;
import java.util.LinkedList;
import java.util.HashSet;

/**
 * Handles generation of code for the @Category annotation
 * - all non-static methods converted to static ones with additional parameter 'self'
 *
 * @author Alex Tkachman
 */
@GroovyASTTransformation(phase=CompilePhase.CANONICALIZATION)
public class CategoryASTTransformation implements ASTTransformation, Opcodes {
    private static final VariableExpression THIS_EXPRESSION = new VariableExpression("$this");

    public void visit(ASTNode[] nodes, final SourceUnit source) {
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        ClassNode parent = (ClassNode) nodes[1];

        ClassNode targetClass = getTargetClass(source, annotation);

        final LinkedList<Set<String>> varStack = new LinkedList<Set<String>> ();
        Set<String> names = new HashSet<String>();
        for (FieldNode field : parent.getFields()) {
          names.add(field.getName());
        }
        varStack.add(names);

        final ClassCodeExpressionTransformer expressionTransformer = new ClassCodeExpressionTransformer() {
            protected SourceUnit getSourceUnit() {
                return source;
            }

            public void visitMethod(MethodNode node) {
                Set<String> names = new HashSet<String>();
                names.addAll(varStack.getLast());
                final Parameter[] params = node.getParameters();
                for (int i = 0; i < params.length; i++) {
                    Parameter param = params[i];
                    names.add(param.getName());
                }
                varStack.add(names);

                super.visitMethod(node);

                varStack.removeLast();
            }

            public void visitBlockStatement(BlockStatement block) {
                Set<String> names = new HashSet<String>();
                names.addAll(varStack.getLast());
                varStack.add(names);
                super.visitBlockStatement(block);
                varStack.remove(names);
            }

            public void visitDeclarationExpression(DeclarationExpression expression) {
                varStack.getLast().add(expression.getVariableExpression().getName());
                super.visitDeclarationExpression(expression);
            }

            public Expression transform(Expression exp) {
                if (exp instanceof VariableExpression) {
                    VariableExpression ve = (VariableExpression) exp;
                    if (ve.getName().equals("this"))
                        return THIS_EXPRESSION;
                    else {
                        if (!varStack.getLast().contains(ve.getName())) {
                            return new PropertyExpression(THIS_EXPRESSION, ve.getName());
                        }
                    }
                }
                return super.transform(exp);
            }
        };

        for (MethodNode method : parent.getMethods() ) {
            if (!method.isStatic()) {
                method.setModifiers(method.getModifiers() | Opcodes.ACC_STATIC);
                final Parameter[] origParams = method.getParameters();
                final Parameter[] newParams = new Parameter [origParams.length + 1];
                newParams [0] = new Parameter(targetClass, "$this");
                System.arraycopy(origParams, 0, newParams, 1, origParams.length);
                method.setParameters(newParams);


                expressionTransformer.visitMethod(method);
            }
        }
    }

    private ClassNode getTargetClass(SourceUnit source, AnnotationNode annotation) {
        final Expression value = annotation.getMember("value");
        if (value == null || !(value instanceof ClassExpression)) {
            //noinspection ThrowableInstanceNeverThrown
            source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(
                        "@groovy.lang.Category must define 'value' which is class to apply this category",
                        annotation.getLineNumber(),
                        annotation.getColumnNumber()),
                        source));
        }

        ClassNode targetClass = ((ClassExpression)value).getType();
        return targetClass;
    }
}