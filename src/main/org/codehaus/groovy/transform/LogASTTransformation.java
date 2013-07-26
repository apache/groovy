/*
 * Copyright 2003-2012 the original author or authors.
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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.SyntaxException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class LogASTTransformation implements ASTTransformation {

    public void visit(ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            addError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes), nodes[0], source);
        }

        AnnotatedNode targetClass = (AnnotatedNode) nodes[1];
        AnnotationNode logAnnotation = (AnnotationNode) nodes[0];

        final LoggingStrategy loggingStrategy = createLoggingStrategy(logAnnotation, source.getClassLoader());
        if (loggingStrategy == null) return;

        final String logFieldName = lookupLogFieldName(logAnnotation);

        if (!(targetClass instanceof ClassNode))
            throw new GroovyBugError("Class annotation " + logAnnotation.getClassNode().getName() + " annotated no Class, this must not happen.");

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
                if (logField != null && logField.getOwner().equals(node)) {
                    addError("Class annotated with Log annotation cannot have log field declared", logField);
                } else if (logField != null && !Modifier.isPrivate(logField.getModifiers())) {
                    addError("Class annotated with Log annotation cannot have log field declared because the field exists in the parent class: " + logField.getOwner().getName(), logField);
                } else {
                    logNode = loggingStrategy.addLoggerFieldToClass(node, logFieldName);
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

                if (!loggingStrategy.isLoggingMethod(methodName)) return exp;

                return loggingStrategy.wrapLoggingMethodCall(variableExpression, methodName, exp);
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
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(),
                        expr.getLastLineNumber(), expr.getLastColumnNumber()), source)
        );
    }

    private LoggingStrategy createLoggingStrategy(AnnotationNode logAnnotation, GroovyClassLoader loader) {

        String annotationName = logAnnotation.getClassNode().getName();

        Class annotationClass;
        try {
            annotationClass = Class.forName(annotationName);
        } catch (Throwable e) {
            throw new RuntimeException("Could not resolve class named " + annotationName);
        }

        Method annotationMethod;
        try {
            annotationMethod = annotationClass.getDeclaredMethod("loggingStrategy", (Class[]) null);
        } catch (Throwable e) {
            throw new RuntimeException("Could not find method named loggingStrategy on class named " + annotationName);
        }

        Object defaultValue;
        try {
            defaultValue = annotationMethod.getDefaultValue();
        } catch (Throwable e) {
            throw new RuntimeException("Could not find default value of method named loggingStrategy on class named " + annotationName);
        }

        if (!LoggingStrategy.class.isAssignableFrom((Class) defaultValue)) {
            throw new RuntimeException("Default loggingStrategy value on class named " + annotationName + " is not a LoggingStrategy");
        }

        try {
            Class<? extends LoggingStrategy> strategyClass = (Class<? extends LoggingStrategy>) defaultValue;
            if (AbstractLoggingStrategy.class.isAssignableFrom(strategyClass)) {
                return DefaultGroovyMethods.newInstance(strategyClass, new Object[]{loader});
            } else {
                return strategyClass.newInstance();
            }
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * A LoggingStrategy defines how to wire a new logger instance into an existing class.
     * It is meant to be used with the @Log family of annotations to allow you to
     * write your own Log annotation provider.
     */
    public interface LoggingStrategy {
        /**
         * In this method, you are given a ClassNode and a field name, and you must add a new Field
         * onto the class. Return the result of the ClassNode.addField operations.
         *
         * @param classNode the class that was originally annotated with the Log transformation.
         * @param fieldName the name of the logger field
         * @return the FieldNode instance that was created and added to the class
         */
        FieldNode addLoggerFieldToClass(ClassNode classNode, String fieldName);

        boolean isLoggingMethod(String methodName);

        Expression wrapLoggingMethodCall(Expression logVariable, String methodName, Expression originalExpression);
    }

    public static abstract class AbstractLoggingStrategy implements LoggingStrategy {
        protected final GroovyClassLoader loader;

        protected AbstractLoggingStrategy(final GroovyClassLoader loader) {
            this.loader = loader;
        }

        protected AbstractLoggingStrategy() {
            this(null);
        }

        protected ClassNode classNode(String name) {
            ClassLoader cl = loader==null?this.getClass().getClassLoader():loader;
            try {
                return ClassHelper.make(Class.forName(name, false, cl));
            } catch (ClassNotFoundException e) {
                throw new GroovyRuntimeException(e);
            }
        }
    }
}
