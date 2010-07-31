/*
 * Copyright 2003-2010 the original author or authors.
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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

/**
 * This class provides an AST Transformation to add a log field to a class.
 *
 * @author Guillaume Laforge
 * @author Jochen Theodorou
 * @author Dinko Srkoc
 * @author Hamlet D'Arcy
 * @author Raffaele Cigni
 * @author Alberto Vilches Raton
 * @author Tomasz Bujok
 * @author Martin Ghados
 * @author Matthias Cullmann
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class LogASTTransformation implements ASTTransformation {

    public void visit(ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            addError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes), nodes[0], source);
        }

        AnnotatedNode targetClass = (AnnotatedNode) nodes[1];
        AnnotationNode logAnnotation = (AnnotationNode) nodes[0];

        final boolean isJUL = "groovy.util.logging.Log".equals(logAnnotation.getClassNode().getName());
        final boolean isLogBack = "groovy.util.logging.Slf4j".equals(logAnnotation.getClassNode().getName());
        final boolean isLog4j = "groovy.util.logging.Log4j".equals(logAnnotation.getClassNode().getName());
        final boolean isCommonsLog = "groovy.util.logging.Commons".equals(logAnnotation.getClassNode().getName());

        final String logFieldName = lookupLogFieldName(logAnnotation);

        if (!(targetClass instanceof ClassNode))
            throw new GroovyBugError("Class annotation @Log annotated no Class, this must not happen.");

        final ClassNode classNode = (ClassNode) targetClass;

        ClassCodeExpressionTransformer transformer = new ClassCodeExpressionTransformer() {
            private FieldNode logNode;

            @Override
            protected SourceUnit getSourceUnit() {
                return source;
            }

            public Expression transform(Expression exp) {
                if (exp == null) return null;
                if (exp instanceof MethodCallExpression) {
                    return transformMethodCallExpression(exp);
                }
                return super.transform(exp);
            }

            @Override
            public void visitClass(ClassNode node) {
                FieldNode logField = node.getField(logFieldName);
                if (logField != null) {
                    addError("Class annotated with Log annotation cannot have log field declared",
                            logField);
                } else {

                    if (isJUL) {
                        logNode = node.addField(logFieldName,
                                Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE,
                                new ClassNode("java.util.logging.Logger", Opcodes.ACC_PUBLIC, new ClassNode(Object.class)),
                                new MethodCallExpression(
                                        new ClassExpression(new ClassNode("java.util.logging.Logger", Opcodes.ACC_PUBLIC, new ClassNode(Object.class))),
                                        "getLogger",
                                        new ConstantExpression(node.getName())));
                    } else if (isLogBack) {
                        logNode = node.addField(logFieldName,
                                Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE,
                                new ClassNode("org.slf4j.Logger", Opcodes.ACC_PUBLIC, new ClassNode(Object.class)),
                                new MethodCallExpression(
                                        new ClassExpression(new ClassNode("org.slf4j.LoggerFactory", Opcodes.ACC_PUBLIC, new ClassNode(Object.class))),
                                        "getLogger",
                                        new ClassExpression(node)));
                    } else if (isLog4j) {
                        logNode = node.addField(logFieldName,
                                Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE,
                                new ClassNode("org.apache.log4j.Logger", Opcodes.ACC_PUBLIC, new ClassNode(Object.class)),
                                new MethodCallExpression(
                                        new ClassExpression(new ClassNode("org.apache.log4j.Logger", Opcodes.ACC_PUBLIC, new ClassNode(Object.class))),
                                        "getLogger",
                                        new ClassExpression(node)));
                    } else if (isCommonsLog) {
                        logNode = node.addField(logFieldName,
                                Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE,
                                new ClassNode("org.apache.commons.logging.Log", Opcodes.ACC_PUBLIC, new ClassNode(Object.class)),
                                new MethodCallExpression(
                                        new ClassExpression(new ClassNode("org.apache.commons.logging.LogFactory", Opcodes.ACC_PUBLIC, new ClassNode(Object.class))),
                                        "getLog",
                                        new ClassExpression(node)));
                    }
                }
                super.visitClass(node);
            }

            private Expression transformMethodCallExpression(Expression exp) {
                MethodCallExpression mce = (MethodCallExpression) exp;
                if (!(mce.getObjectExpression() instanceof VariableExpression)) {
                    return exp;
                }
                VariableExpression variableExpression = (VariableExpression) mce.getObjectExpression();
                if (!variableExpression.getName().equals(logFieldName)
                        || !(variableExpression.getAccessedVariable() instanceof DynamicVariable)) {
                    return exp;
                }
                String methodName = mce.getMethodAsString();
                if (methodName == null) return exp;
                if (usesSimpleMethodArgumentsOnly(mce)) return exp;

                variableExpression.setAccessedVariable(logNode);

                ArgumentListExpression args = new ArgumentListExpression();

                if (isCommonsLog) {
                    if (methodName.matches("fatal|error|warn|info|debug|trace")) {
                        return commonMethodCallExpression(variableExpression, methodName, exp);
                    }
                }else if (isLogBack) {
                    if (methodName.matches("error|warn|info|debug|trace")) {
                        return commonMethodCallExpression(variableExpression, methodName, exp);
                    }
                } else if (isLog4j) {
                    if (!methodName.matches("fatal|error|warn|info|debug|trace")) {
                        return exp;
                    }

                    final MethodCallExpression condition;
                    if (!"trace".equals(methodName)) {
                        ClassNode levelClass = new ClassNode("org.apache.log4j.Priority", 0, ClassHelper.OBJECT_TYPE);
                        AttributeExpression logLevelExpression = new AttributeExpression(
                                new ClassExpression(levelClass),
                                new ConstantExpression(methodName.toUpperCase()));
                        args.addExpression(logLevelExpression);
                        condition = new MethodCallExpression(variableExpression, "isEnabledFor", args);
                    } else {
                        // log4j api is inconsistent, so trace requires special handling
                        condition = new MethodCallExpression(
                                variableExpression,
                                "is" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1, methodName.length()) + "Enabled",
                                ArgumentListExpression.EMPTY_ARGUMENTS);
                    }

                    return new TernaryExpression(
                            new BooleanExpression(condition),
                            exp,
                            ConstantExpression.NULL);
                } else if (isJUL) {
                    if (!methodName.matches("severe|warning|info|fine|finer|finest")) {
                        return exp;
                    }

                    ClassNode levelClass = new ClassNode("java.util.logging.Level", 0, ClassHelper.OBJECT_TYPE);
                    AttributeExpression logLevelExpression = new AttributeExpression(
                            new ClassExpression(levelClass),
                            new ConstantExpression(methodName.toUpperCase()));
                    args.addExpression(logLevelExpression);
                    MethodCallExpression condition = new MethodCallExpression(variableExpression, "isLoggable", args);

                    return new TernaryExpression(
                            new BooleanExpression(condition),
                            exp,
                            ConstantExpression.NULL);
                }
                return exp;
            }

            private Expression commonMethodCallExpression(Expression variableExpression, String methodName, Expression exp) {

                MethodCallExpression condition = new MethodCallExpression(
                        variableExpression,
                        "is" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1, methodName.length()) + "Enabled",
                        ArgumentListExpression.EMPTY_ARGUMENTS);

                return new TernaryExpression(
                        new BooleanExpression(condition),
                        exp,
                        ConstantExpression.NULL);
            }

            private boolean usesSimpleMethodArgumentsOnly(MethodCallExpression mce) {
                Expression arguments = mce.getArguments();
                if (arguments instanceof TupleExpression) {
                    TupleExpression tuple = (TupleExpression) arguments;
                    for (Expression exp : tuple.getExpressions()) {
                        if (!isSimpleExpression(exp)) return false;
                    }
                    return true;
                }
                return !isSimpleExpression(arguments);
            }

            private boolean isSimpleExpression(Expression exp) {
                if (exp instanceof ConstantExpression) return true;
                if (exp instanceof VariableExpression) return true;
                return false;
            }

        };
        transformer.visitClass(classNode);

    }

    private String lookupLogFieldName(AnnotationNode logAnnotation) {
        Expression member = logAnnotation.getMember("value");
        if (member != null && member.getText() != null) {
            return member.getText();
        } else {
            return "log";
        }
    }

    public void addError(String msg, ASTNode expr, SourceUnit source) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
        );
    }


}
