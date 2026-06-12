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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.codehaus.groovy.transform.LogASTTransformation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;

/**
 * This local transform adds a logging ability to your program using
 * Apache Commons logging. Every method call on an unbound variable named <i>log</i>
 * will be mapped to a call to the logger. For this a <i>log</i> field will be
 * inserted in the class. If the field already exists the usage of this transform
 * will cause a compilation error. The method name will be used to determine
 * what to call on the logger.
 * <pre>
 * log.name(exp)
 * </pre>is mapped to
 * <pre>
 * if (log.isNameEnabled() {
 *    log.name(exp)
 * }</pre>
 * Here name is a placeholder for info, debug, warning, error, etc.
 * If the expression exp is a constant or only a variable access the method call will
 * not be transformed. But this will still cause a call on the injected logger.
 *
 * @since 1.8.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.LogASTTransformation")
public @interface Commons {
    /**
     * Returns the injected logger field name.
     * Defaults to {@code "log"}.
     *
     * @return the logger field name
     */
    String value() default "log";

    /**
     * Returns the logger category name.
     * Defaults to {@link LogASTTransformation#DEFAULT_CATEGORY_NAME}, which uses the host class name.
     *
     * @return the logger category name
     */
    String category() default LogASTTransformation.DEFAULT_CATEGORY_NAME;

    /**
     * If specified, must match the "id" attribute in a VisibilityOptions annotation to enable a custom visibility.
     */
    String visibilityId() default Undefined.STRING;

    /**
     * Returns the logging strategy implementation used by this transform.
     * Defaults to {@link CommonsLoggingStrategy}.
     *
     * @return the logging strategy type
     */
    Class<? extends LogASTTransformation.LoggingStrategy> loggingStrategy() default CommonsLoggingStrategy.class;

    /**
     * Logging strategy for Apache Commons Logging.
     */
    class CommonsLoggingStrategy extends LogASTTransformation.AbstractLoggingStrategyV2 {

        private static final String LOGGER_NAME = "org.apache.commons.logging.Log";
        private static final String LOGGERFACTORY_NAME = "org.apache.commons.logging.LogFactory";

        /**
         * Creates a Commons Logging strategy.
         *
         * @param loader the class loader used to resolve logger classes
         */
        protected CommonsLoggingStrategy(final GroovyClassLoader loader) {
            super(loader);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FieldNode addLoggerFieldToClass(ClassNode classNode, String logFieldName, String categoryName, int fieldModifiers) {
            return classNode.addField(logFieldName,
                    fieldModifiers,
                    classNode(LOGGER_NAME),
                    new MethodCallExpression(
                            new ClassExpression(classNode(LOGGERFACTORY_NAME)),
                            "getLog",
                            new ConstantExpression(getCategoryName(classNode, categoryName))));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isLoggingMethod(String methodName) {
            return methodName.matches("fatal|error|warn|info|debug|trace");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Expression wrapLoggingMethodCall(Expression logVariable, String methodName, Expression originalExpression) {
            MethodCallExpression condition = new MethodCallExpression(
                    logVariable,
                    "is" + methodName.substring(0, 1).toUpperCase(Locale.ENGLISH) + methodName.substring(1) + "Enabled",
                    ArgumentListExpression.EMPTY_ARGUMENTS);
            condition.setImplicitThis(false);

            return ternaryX(condition, originalExpression, nullX());
        }
   }
}
