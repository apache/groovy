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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.codehaus.groovy.transform.LogASTTransformation;
import org.codehaus.groovy.transform.LogASTTransformation.LoggingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;

/**
 * This local transform adds a logging ability to your program using
 * java.logging. Every method call on a unbound variable named <i>log</i>
 * will be mapped to a call to the logger. For this a <i>log</i> field will be
 * inserted in the class. If the field already exists the usage of this transform
 * will cause a compilation error. The method name will be used to determine
 * what to call on the logger.
 * <pre>
 * import groovy.util.logging.*
 * import static java.lang.System.Logger.Level.INFO
 *
 * {@code @PlatformLog}
 * class Foo {
 *     def method() {
 *         log.log INFO, 'Foobar'
 *     }
 * }
 *
 * new Foo().method()
 * </pre>
 *
 * @since 4.0.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.LogASTTransformation")
public @interface PlatformLog {
    String value() default "log";

    String category() default LogASTTransformation.DEFAULT_CATEGORY_NAME;

    /**
     * If specified, must match the "id" attribute in a VisibilityOptions annotation to enable a custom visibility.
     */
    String visibilityId() default Undefined.STRING;

    Class<? extends LoggingStrategy> loggingStrategy() default JavaUtilLoggingStrategy.class;

    /**
     * This class contains the logic of how to weave a Java platform logger into the host class.
     */
    class JavaUtilLoggingStrategy extends LogASTTransformation.AbstractLoggingStrategyV2 {

        private static final ClassNode LOGGER_CLASSNODE = ClassHelper.make(System.Logger.class);
        private static final ClassNode LOGGER_FINDER_CLASSNODE = ClassHelper.make(System.LoggerFinder.class);

        protected JavaUtilLoggingStrategy(final GroovyClassLoader loader) {
            super(loader);
        }

        @Override
        public FieldNode addLoggerFieldToClass(ClassNode classNode, String logFieldName, String categoryName, int fieldModifiers) {
            Expression module = callX(classX(classNode), "getModule");
            Expression loggerFinder = callX(classX(LOGGER_FINDER_CLASSNODE), "getLoggerFinder");
            Expression initialValue = callX(loggerFinder, "getLogger", args(constX(getCategoryName(classNode, categoryName)), module));
            return classNode.addField(logFieldName, fieldModifiers, LOGGER_CLASSNODE, initialValue);
        }

        @Override
        public boolean isLoggingMethod(String methodName) {
//            return methodName.matches("error|warn|info|debug|trace");
            return false;
        }

        @Override
        public Expression wrapLoggingMethodCall(Expression logVariable, String methodName, Expression originalExpression) {
            // no shortcut
            return originalExpression;
        }
    }
}
