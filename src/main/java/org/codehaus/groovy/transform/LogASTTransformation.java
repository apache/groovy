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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;

import static org.apache.groovy.ast.tools.VisibilityUtils.getVisibility;

/**
 * This class provides an AST Transformation to add a log field to a class.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class LogASTTransformation extends AbstractASTTransformation implements CompilationUnitAware {

    /**
     * This is just a dummy value used because String annotations values can not be null.
     * It will be replaced by the fully qualified class name of the annotated class.
     */
    public static final String DEFAULT_CATEGORY_NAME = "##default-category-name##";

    public static final String DEFAULT_ACCESS_MODIFIER = "private";

    private CompilationUnit compilationUnit;

    @Override
    public void visit(ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotatedNode targetClass = (AnnotatedNode) nodes[1];
        AnnotationNode logAnnotation = (AnnotationNode) nodes[0];

        final GroovyClassLoader classLoader = compilationUnit != null ? compilationUnit.getTransformLoader() : source.getClassLoader();
        final LoggingStrategy loggingStrategy = createLoggingStrategy(logAnnotation, classLoader);
        if (loggingStrategy == null) return;

        final String logFieldName = lookupLogFieldName(logAnnotation);

        final String categoryName = lookupCategoryName(logAnnotation);

        final int logFieldModifiers = lookupLogFieldModifiers(targetClass, logAnnotation);

        if (!(targetClass instanceof ClassNode))
            throw new GroovyBugError("Class annotation " + logAnnotation.getClassNode().getName() + " annotated no Class, this must not happen.");

        final ClassNode classNode = (ClassNode) targetClass;

        ClassCodeExpressionTransformer transformer = new ClassCodeExpressionTransformer() {
            private FieldNode logNode;

            @Override
            protected SourceUnit getSourceUnit() {
                return source;
            }

            @Override
            public Expression transform(Expression exp) {
                if (exp == null) return null;
                if (exp instanceof MethodCallExpression) {
                    return transformMethodCallExpression(exp);
                }
                if (exp instanceof ClosureExpression) {
                    return transformClosureExpression((ClosureExpression) exp);
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
                    if (loggingStrategy instanceof LoggingStrategyV2) {
                        LoggingStrategyV2 loggingStrategyV2 = (LoggingStrategyV2) loggingStrategy;
                        logNode = loggingStrategyV2.addLoggerFieldToClass(node, logFieldName, categoryName, logFieldModifiers);
                    } else {
                        // support the old style but they won't be as configurable
                        logNode = loggingStrategy.addLoggerFieldToClass(node, logFieldName, categoryName);
                    }
                }
                super.visitClass(node);
            }

            private Expression transformClosureExpression(ClosureExpression exp) {
                if (exp.getCode() instanceof BlockStatement) {
                    BlockStatement code = (BlockStatement) exp.getCode();
                    super.visitBlockStatement(code);
                }
                return exp;
            }

            private Expression transformMethodCallExpression(Expression exp) {
                Expression modifiedCall = addGuard((MethodCallExpression) exp);
                return modifiedCall == null ? super.transform(exp) : modifiedCall;
            }

            private Expression addGuard(MethodCallExpression mce) {
                // only add guard to methods of the form: logVar.logMethod(params)
                if (!(mce.getObjectExpression() instanceof VariableExpression)) {
                    return null;
                }
                VariableExpression variableExpression = (VariableExpression) mce.getObjectExpression();
                if (!variableExpression.getName().equals(logFieldName)
                        || !(variableExpression.getAccessedVariable() instanceof DynamicVariable)) {
                    return null;
                }

                String methodName = mce.getMethodAsString();
                if (methodName == null) return null;
                if (!loggingStrategy.isLoggingMethod(methodName)) return null;
                // also don't bother with guard if we have "simple" method args
                // since there is no saving
                if (usesSimpleMethodArgumentsOnly(mce)) return null;

                variableExpression.setAccessedVariable(logNode);
                return loggingStrategy.wrapLoggingMethodCall(variableExpression, methodName, mce);
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

        // GROOVY-6373: references to 'log' field are normally already FieldNodes by now, so revisit scoping
        new VariableScopeVisitor(sourceUnit, true).visitClass(classNode);
    }

    private static String lookupLogFieldName(AnnotationNode logAnnotation) {
        Expression member = logAnnotation.getMember("value");
        if (member != null && member.getText() != null) {
            return member.getText();
        } else {
            return "log";
        }
    }

    private static String lookupCategoryName(AnnotationNode logAnnotation) {
        Expression member = logAnnotation.getMember("category");
        if (member != null && member.getText() != null) {
            return member.getText();
        }
        return DEFAULT_CATEGORY_NAME;
    }

    private int lookupLogFieldModifiers(AnnotatedNode targetClass, AnnotationNode logAnnotation) {
        int modifiers = getVisibility(logAnnotation, targetClass, ClassNode.class, Opcodes.ACC_PRIVATE);
        return Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT | Opcodes.ACC_STATIC | modifiers;
    }

    private static LoggingStrategy createLoggingStrategy(AnnotationNode logAnnotation, GroovyClassLoader loader) {

        String annotationName = logAnnotation.getClassNode().getName();

        Class annotationClass;
        try {
            annotationClass = Class.forName(annotationName, false, loader);
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

        if (!LoggingStrategy.class.isAssignableFrom((Class) defaultValue)
                && !LoggingStrategyV2.class.isAssignableFrom((Class) defaultValue)) {
            throw new RuntimeException("Default loggingStrategy value on class named " + annotationName + " is not a LoggingStrategy");
        }

        // try V2 configurable logging strategy
        try {
            Class<? extends LoggingStrategyV2> strategyClass = (Class<? extends LoggingStrategyV2>) defaultValue;
            if (AbstractLoggingStrategy.class.isAssignableFrom(strategyClass)) {
                return DefaultGroovyMethods.newInstance(strategyClass, new Object[]{loader});
            } else {
                return strategyClass.newInstance();
            }
        } catch (Exception e) {
        }

        // try legacy logging strategy
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
         * In this method, you are given a ClassNode, a field name and a category name, and you must add a new Field
         * onto the class. Return the result of the ClassNode.addField operations.
         *
         * @param classNode    the class that was originally annotated with the Log transformation.
         * @param fieldName    the name of the logger field
         * @param categoryName the name of the logging category
         * @return the FieldNode instance that was created and added to the class
         */
        FieldNode addLoggerFieldToClass(ClassNode classNode, String fieldName, String categoryName);

        boolean isLoggingMethod(String methodName);

        String getCategoryName(ClassNode classNode, String categoryName);

        Expression wrapLoggingMethodCall(Expression logVariable, String methodName, Expression originalExpression);
    }

    /**
     * A LoggingStrategy defines how to wire a new logger instance into an existing class.
     * It is meant to be used with the @Log family of annotations to allow you to
     * write your own Log annotation provider.
     */
    public interface LoggingStrategyV2 extends LoggingStrategy {
        /**
         * In this method, you are given a ClassNode, a field name and a category name, and you must add a new Field
         * onto the class. Return the result of the ClassNode.addField operations.
         *
         * @param classNode      the class that was originally annotated with the Log transformation.
         * @param fieldName      the name of the logger field
         * @param categoryName   the name of the logging category
         * @param fieldModifiers the modifiers (private, final, et. al.) of the logger field
         * @return the FieldNode instance that was created and added to the class
         */
        FieldNode addLoggerFieldToClass(ClassNode classNode, String fieldName, String categoryName, int fieldModifiers);
    }

    public abstract static class AbstractLoggingStrategyV2 extends AbstractLoggingStrategy implements LoggingStrategyV2 {
        protected AbstractLoggingStrategyV2(final GroovyClassLoader loader) {
            super(loader);
        }

        protected AbstractLoggingStrategyV2() {
            this(null);
        }

        @Override
        public FieldNode addLoggerFieldToClass(ClassNode classNode, String fieldName, String categoryName) {
            throw new UnsupportedOperationException("This logger requires a later version of Groovy");
        }
    }

    public abstract static class AbstractLoggingStrategy implements LoggingStrategy {
        protected final GroovyClassLoader loader;

        protected AbstractLoggingStrategy(final GroovyClassLoader loader) {
            this.loader = loader;
        }

        protected AbstractLoggingStrategy() {
            this(null);
        }

        @Override
        public String getCategoryName(ClassNode classNode, String categoryName) {
            if (categoryName.equals(DEFAULT_CATEGORY_NAME)) {
                return classNode.getName();
            }
            return categoryName;
        }

        protected ClassNode classNode(String name) {
            ClassLoader cl = loader == null ? this.getClass().getClassLoader() : loader;
            try {
                return ClassHelper.make(Class.forName(name, false, cl));
            } catch (ClassNotFoundException e) {
                throw new GroovyRuntimeException("Unable to load logging class", e);
            }
        }
    }

    @Override
    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }
}
