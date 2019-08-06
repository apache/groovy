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
package groovy.util.logging;

import groovy.lang.GroovyClassLoader;
import groovy.transform.Undefined;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.codehaus.groovy.transform.LogASTTransformation;
import org.codehaus.groovy.transform.LogASTTransformation.LoggingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;

/**
 * This local transform adds a logging ability to your program using
 * java.util.logging. Every method call on a unbound variable named <i>log</i>
 * will be mapped to a call to the logger. For this a <i>log</i> field will be
 * inserted in the class. If the field already exists the usage of this transform
 * will cause a compilation error. The method name will be used to determine
 * what to call on the logger.
 * <pre>
 * log.name(exp)
 * </pre>is mapped to
 * <pre>
 * if (log.isLoggable(Level.NAME) {
 *    log.name(exp)
 * }</pre>
 * Here name is a place holder for info, fine, finer, finest, config, warning, severe.
 * NAME is name transformed to upper case. if anything else is used it will result in
 * an exception at runtime. If the expression exp is a constant or only a variable access
 * the method call will not be transformed. But this will still cause a call on the injected
 * logger.
 *
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.LogASTTransformation")
public @interface Log {
    String value() default "log";
    String category() default LogASTTransformation.DEFAULT_CATEGORY_NAME;

    /**
     * If specified, must match the "id" attribute in a VisibilityOptions annotation to enable a custom visibility.
     * @since 3.0.0
     */
    String visibilityId() default Undefined.STRING;

    Class<? extends LoggingStrategy> loggingStrategy() default JavaUtilLoggingStrategy.class;

    /**
     * This class contains the logic of how to weave a Java Util Logging logger into the host class.
     */
    public static class JavaUtilLoggingStrategy extends LogASTTransformation.AbstractLoggingStrategyV2 {

        private static final ClassNode LOGGER_CLASSNODE = ClassHelper.make(java.util.logging.Logger.class);
        private static final ClassNode LEVEL_CLASSNODE = ClassHelper.make(java.util.logging.Level.class);

        protected JavaUtilLoggingStrategy(final GroovyClassLoader loader) {
            super(loader);
        }

        @Override
        public FieldNode addLoggerFieldToClass(ClassNode classNode, String logFieldName, String categoryName, int fieldModifiers) {
            return classNode.addField(logFieldName,
                        fieldModifiers,
                        LOGGER_CLASSNODE,
                        new MethodCallExpression(
                                new ClassExpression(LOGGER_CLASSNODE),
                                "getLogger",
                                new ConstantExpression(getCategoryName(classNode, categoryName))));
        }

        @Override
        public boolean isLoggingMethod(String methodName) {
            return methodName.matches("severe|warning|info|fine|finer|finest");
        }

        @Override
        public Expression wrapLoggingMethodCall(Expression logVariable, String methodName, Expression originalExpression) {
            AttributeExpression logLevelExpression = new AttributeExpression(
                    new ClassExpression(LEVEL_CLASSNODE),
                    new ConstantExpression(methodName.toUpperCase(Locale.ENGLISH)));

            ArgumentListExpression args = new ArgumentListExpression();
            args.addExpression(logLevelExpression);
            MethodCallExpression condition = new MethodCallExpression(logVariable, "isLoggable", args);
            condition.setImplicitThis(false);

            return new TernaryExpression(
                    new BooleanExpression(condition),
                    originalExpression,
                    nullX());

        }
    }
}
