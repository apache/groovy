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
import org.codehaus.groovy.ast.MethodNode;
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
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.apache.groovy.ast.tools.VisibilityUtils.getVisibility;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;

/**
 * This class provides an AST Transformation to add a log field to a class.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class LogASTTransformation extends AbstractASTTransformation implements CompilationUnitAware, TransformWithPriority {

    /**
     * This is just a dummy value used because String annotations values can not be null.
     * It will be replaced by the fully qualified class name of the annotated class.
     */
    public static final String DEFAULT_CATEGORY_NAME = "##default-category-name##";

    /**
     * Default access modifier for logger field.
     */
    public static final String DEFAULT_ACCESS_MODIFIER = "private";

    @Override
    public int priority() {
        return 1; // GROOVY-7439
    }

    private CompilationUnit compilationUnit;

    @Override
    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit sourceUnit) {
        init(nodes, sourceUnit);
        AnnotatedNode targetClass = (AnnotatedNode) nodes[1];
        AnnotationNode logAnnotation = (AnnotationNode) nodes[0];

        final LoggingStrategy loggingStrategy = createLoggingStrategy(logAnnotation, sourceUnit.getClassLoader(), compilationUnit.getTransformLoader());
        if (loggingStrategy == null) return;

        final String logFieldName = lookupLogFieldName(logAnnotation);

        final String categoryName = lookupCategoryName(logAnnotation);

        final int logFieldModifiers = lookupLogFieldModifiers(targetClass, logAnnotation);

        if (!(targetClass instanceof ClassNode classNode))
            throw new GroovyBugError("Class annotation " + logAnnotation.getClassNode().getName() + " annotated no Class, this must not happen.");

        final boolean staticLocation = lookupStaticLocation(logAnnotation);
        if (staticLocation) {
            String unsupported = loggingStrategy.staticLocationUnsupportedReason();
            if (unsupported != null) {
                addError("staticLocation is not supported by " + loggingStrategy.getClass().getName() + ": " + unsupported, logAnnotation);
                return;
            }
        }
        final String sourceFileName = new File(sourceUnit.getName()).getName();
        final List<FieldNode> locationFields = new ArrayList<>();

        var transformer = new ClassCodeExpressionTransformer() {
            private boolean inClosure;
            private FieldNode logNode;
            private String currentMethod = "<clinit>";

            @Override
            protected SourceUnit getSourceUnit() {
                return sourceUnit;
            }

            @Override
            public Expression transform(final Expression exp) {
                if (exp instanceof MethodCallExpression call) {
                    Expression modifiedCall = addGuard(call);
                    if (modifiedCall != null) {
                        return modifiedCall;
                    }
                } else if (exp instanceof ClosureExpression closure) {
                    if (closure.getCode() instanceof BlockStatement block) {
                        boolean previousValue = inClosure; inClosure = true;
                        try {
                            super.visitBlockStatement(block);
                        } finally {
                            inClosure = previousValue;
                        }
                    }
                    return closure;
                }
                return super.transform(exp);
            }

            @Override
            protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
                String previous = currentMethod;
                currentMethod = isConstructor ? "<init>" : node.getName();
                try {
                    super.visitConstructorOrMethod(node, isConstructor);
                } finally {
                    currentMethod = previous;
                }
            }

            @Override
            public void visitField(final FieldNode node) {
                String previous = currentMethod;
                currentMethod = node.isStatic() ? "<clinit>" : "<init>";
                try {
                    super.visitField(node);
                } finally {
                    currentMethod = previous;
                }
            }

            @Override
            public void visitObjectInitializerStatements(final ClassNode node) {
                String previous = currentMethod;
                currentMethod = "<init>";
                try {
                    super.visitObjectInitializerStatements(node);
                } finally {
                    currentMethod = previous;
                }
            }

            /**
             * A compile-time {@code StackTraceElement} for a logging statement, held in a
             * synthetic static field of the annotated class (GROOVY-12378). The field is
             * registered only once the strategy accepts the rewrite, and fields are added
             * after the traversal, since a statement in a field initializer is visited
             * while the class's field list is being iterated.
             */
            private FieldNode locationFieldFor(final MethodCallExpression mce, final ClassNode owner) {
                String name = "$log$loc$" + (locationFields.size() + 1);
                Expression init = ctorX(ClassHelper.make(StackTraceElement.class), args(
                        constX(owner.getName()), constX(currentMethod), constX(sourceFileName), constX(mce.getLineNumber())));
                FieldNode field = new FieldNode(name, ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC, ClassHelper.make(StackTraceElement.class), owner, init);
                field.setSynthetic(true);
                return field;
            }

            @Override
            public void visitClass(final ClassNode node) {
                FieldNode logField = node.getField(logFieldName);
                if (logField != null && logField.getOwner().equals(node)) {
                    addError("Class annotated with Log annotation cannot have log field declared", logField);
                } else if (logField != null && !Modifier.isPrivate(logField.getModifiers())) {
                    addError("Class annotated with Log annotation cannot have log field declared because the field exists in the parent class: " + logField.getOwner().getName(), logField);
                } else {
                    if (loggingStrategy instanceof LoggingStrategyV2 loggingStrategyV2) {
                        logNode = loggingStrategyV2.addLoggerFieldToClass(node, logFieldName, categoryName, logFieldModifiers);
                    } else {
                        // support the old style but they won't be as configurable
                        logNode = loggingStrategy.addLoggerFieldToClass(node, logFieldName, categoryName);
                    }
                }
                super.visitClass(node);
            }

            private Expression addGuard(final MethodCallExpression mce) {
                // only add guard to methods of the form: logVar.logMethod(arguments)
                if (!(mce.getObjectExpression() instanceof VariableExpression variableExpression)) {
                    return null;
                }
                if (!variableExpression.getName().equals(logFieldName)
                        || !(variableExpression.getAccessedVariable() instanceof DynamicVariable)) {
                    return null;
                }
                String methodName = mce.getMethodAsString();
                if (methodName == null || !loggingStrategy.isLoggingMethod(methodName)) return null;

                Expression receiver;
                if (inClosure) {
                    receiver = propX(callThisX("getThisObject"), logFieldName); // GROOVY-11800
                    receiver.setType(logNode.getType());
                    mce.setObjectExpression(receiver);
                } else {
                    receiver = variableExpression;
                    variableExpression.setAccessedVariable(logNode);
                }

                boolean simpleArguments = usesSimpleMethodArgumentsOnly(mce);
                if (staticLocation) {
                    FieldNode location = locationFieldFor(mce, logNode.getOwner());
                    Expression withLocation = loggingStrategy.wrapLoggingMethodCallWithLocation(
                            receiver, methodName, mce, fieldX(location), !simpleArguments);
                    if (withLocation != null) {
                        locationFields.add(location);
                        return withLocation;
                    }
                }

                // do not bother with guard if we have "simple" args since there are no savings
                if (simpleArguments) return null;

                return loggingStrategy.wrapLoggingMethodCall(receiver, methodName, mce);
            }

            private boolean usesSimpleMethodArgumentsOnly(final MethodCallExpression mce) {
                Expression arguments = mce.getArguments();
                if (arguments instanceof TupleExpression tuple) {
                    for (Expression exp : tuple) {
                        if (!isSimpleExpression(exp)) return false;
                    }
                    return true;
                }
                return !isSimpleExpression(arguments);
            }

            private boolean isSimpleExpression(final Expression exp) {
                if (exp instanceof ConstantExpression) return true;
                if (exp instanceof VariableExpression) return true;
                return false;
            }

        };
        transformer.visitClass(classNode);
        for (FieldNode locationField : locationFields) {
            classNode.addField(locationField);
        }

        // GROOVY-6373: references to 'log' field are normally already FieldNodes by now, so revisit scoping
        new VariableScopeVisitor(sourceUnit, true).visitClass(classNode);
    }

    private static String lookupLogFieldName(final AnnotationNode logAnnotation) {
        Expression member = logAnnotation.getMember("value");
        if (member != null && member.getText() != null) {
            return member.getText();
        } else {
            return "log";
        }
    }

    private static boolean lookupStaticLocation(final AnnotationNode logAnnotation) {
        Expression member = logAnnotation.getMember("staticLocation");
        return member instanceof ConstantExpression constant && Boolean.TRUE.equals(constant.getValue());
    }

    private static String lookupCategoryName(final AnnotationNode logAnnotation) {
        Expression member = logAnnotation.getMember("category");
        if (member != null && member.getText() != null) {
            return member.getText();
        }
        return DEFAULT_CATEGORY_NAME;
    }

    private static int lookupLogFieldModifiers(final AnnotatedNode targetClass, final AnnotationNode logAnnotation) {
        int modifiers = getVisibility(logAnnotation, targetClass, ClassNode.class, ACC_PRIVATE);
        return ACC_FINAL | ACC_STATIC | ACC_TRANSIENT | modifiers;
    }

    private static LoggingStrategy createLoggingStrategy(final AnnotationNode logAnnotation, final ClassLoader classLoader, final ClassLoader xformLoader) {
        String annotationName = logAnnotation.getClassNode().getName();

        Class<?> annotationClass;
        try {
            annotationClass = Class.forName(annotationName, false, xformLoader);
        } catch (Throwable t) {
            throw new RuntimeException("Could not resolve class named " + annotationName);
        }

        Method annotationMethod;
        try {
            annotationMethod = annotationClass.getDeclaredMethod("loggingStrategy", (Class[]) null);
        } catch (Throwable t) {
            throw new RuntimeException("Could not find method named loggingStrategy on class named " + annotationName);
        }

        Object defaultValue;
        try {
            defaultValue = annotationMethod.getDefaultValue();
        } catch (Throwable t) {
            throw new RuntimeException("Could not find default value of method named loggingStrategy on class named " + annotationName);
        }

        if (!LoggingStrategy.class.isAssignableFrom((Class<?>) defaultValue)) {
            throw new RuntimeException("Default loggingStrategy value on class named " + annotationName + " is not a LoggingStrategy");
        }

        // try configurable logging strategy
        try {
            Class<? extends LoggingStrategyV2> strategyClass = (Class<? extends LoggingStrategyV2>) defaultValue;
            if (AbstractLoggingStrategy.class.isAssignableFrom(strategyClass)) {
                return DefaultGroovyMethods.newInstance(strategyClass, new Object[]{classLoader});
            } else {
                return strategyClass.getDeclaredConstructor().newInstance();
            }
        } catch (Exception ignore) {
        }

        // try legacy logging strategy
        try {
            Class<? extends LoggingStrategy> strategyClass = (Class<? extends LoggingStrategy>) defaultValue;
            if (AbstractLoggingStrategy.class.isAssignableFrom(strategyClass)) {
                return DefaultGroovyMethods.newInstance(strategyClass, new Object[]{classLoader});
            } else {
                return strategyClass.getDeclaredConstructor().newInstance();
            }
        } catch (Exception ignore) {
        }

        return null;
    }

    //--------------------------------------------------------------------------

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

        default String getCategoryName(final ClassNode classNode, final String categoryName) {
            return categoryName.equals(DEFAULT_CATEGORY_NAME) ? classNode.getName() : categoryName;
        }

        Expression wrapLoggingMethodCall(Expression logVariable, String methodName, Expression originalExpression);

        /**
         * Why {@link #wrapLoggingMethodCallWithLocation} cannot be used with this
         * strategy in the current compilation, for the compile error reported when
         * {@code staticLocation} is requested anyway. A strategy whose logging API
         * accepts a caller-supplied location returns {@code null} when that API is
         * resolvable, and names what is missing from the compile classpath when it
         * is not. By default compile-time locations are not implemented.
         *
         * @return the reason compile-time locations are unavailable, or {@code null} if they are supported
         * @since 6.0.0
         */
        default String staticLocationUnsupportedReason() {
            return "it does not implement compile-time locations";
        }

        /**
         * Rewrites a logging call so that the logging framework is handed the
         * statement's location, computed at compile time, instead of walking the
         * stack at run time (GROOVY-12378). Only called when
         * {@link #staticLocationUnsupportedReason()} returned {@code null}.
         *
         * @param logVariable the logger expression
         * @param methodName the logging method that was called, e.g. {@code info}
         * @param originalCall the call as written; its arguments are the call's arguments
         * @param location an expression yielding the {@code StackTraceElement} for the statement
         * @param guard whether the arguments are worth guarding with a level check
         *              (they are not all constants or variables)
         * @return the replacement expression, or {@code null} to leave the call unchanged
         * @since 6.0.0
         */
        default Expression wrapLoggingMethodCallWithLocation(Expression logVariable, String methodName, MethodCallExpression originalCall, Expression location, boolean guard) {
            return null;
        }
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

    /**
     * Base class for logging strategy implementations supporting the v2 logging API.
     */
    public abstract static class AbstractLoggingStrategyV2 extends AbstractLoggingStrategy implements LoggingStrategyV2 {

        /**
         * Creates a new logging strategy with the given class loader.
         *
         * @param loader the class loader for loading logging implementation classes
         */
        protected AbstractLoggingStrategyV2(final GroovyClassLoader loader) {
            super(loader);
        }

        /**
         * Creates a new logging strategy using the default class loader.
         */
        protected AbstractLoggingStrategyV2() {
            this(null);
        }

        @Override
        public FieldNode addLoggerFieldToClass(final ClassNode classNode, final String fieldName, final String categoryName) {
            throw new UnsupportedOperationException("This logger requires a later version of Groovy");
        }
    }

    /**
     * Base class for logging strategy implementations.
     */
    public abstract static class AbstractLoggingStrategy implements LoggingStrategy {

        /**
         * The class loader for resolving logging implementation classes.
         */
        protected final GroovyClassLoader loader;

        /**
         * Creates a new logging strategy with the given class loader.
         *
         * @param loader the class loader for loading logging implementation classes
         */
        protected AbstractLoggingStrategy(final GroovyClassLoader loader) {
            this.loader = loader;
        }

        /**
         * Creates a new logging strategy using the default class loader.
         */
        protected AbstractLoggingStrategy() {
            this(null);
        }

        /**
         * Resolves a ClassNode for the given class name.
         *
         * @param name the fully qualified class name
         * @return the ClassNode for the specified class
         */
        protected ClassNode classNode(final String name) {
            ClassLoader cl = loader != null ? loader : getClass().getClassLoader();
            try {
                Class<?> c = Class.forName(name, false, cl);
                return ClassHelper.make(c);
            } catch (ClassNotFoundException e) {
                throw new GroovyRuntimeException("Unable to load class: " + name, e);
            }
        }
    }
}
