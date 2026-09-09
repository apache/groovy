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
import groovy.lang.GroovyRuntimeException;
import groovy.transform.Undefined;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.codehaus.groovy.transform.LogASTTransformation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;

/**
 * This local transform adds a logging ability to your program using
 * Log4j2 logging. Every method call on an unbound variable named <i>log</i>
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
 * @since 2.2.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.LogASTTransformation")
public @interface Log4j2 {
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
     * @since 3.0.0
     */
    String visibilityId() default Undefined.STRING;

    /**
     * Returns the logging strategy implementation used by this transform.
     * Defaults to {@link Log4j2LoggingStrategy}.
     *
     * @return the logging strategy type
     */
    Class<? extends LogASTTransformation.LoggingStrategy> loggingStrategy() default Log4j2LoggingStrategy.class;

    /**
     * Whether logging statements hand Log4j 2 their source location, computed at
     * compile time, instead of leaving it to be found by walking the stack at
     * run time. With {@code true}, {@code log.info(msg)} becomes
     * {@code log.atInfo().withLocation(location).log(msg)}, where {@code location}
     * is a {@code StackTraceElement} for the statement held in a synthetic static
     * field of the annotated class, naming the class, the enclosing method, the
     * source file and the line of the statement. That location is correct however
     * the call is dispatched -- through the Groovy runtime on a JVM, or inside a
     * GraalVM native image, where stack walking otherwise reports runtime frames --
     * and it spares Log4j 2 the stack walk that {@code %C}, {@code %M}, {@code %F}
     * and {@code %L} patterns need.
     * <p>
     * Requires the {@code LogBuilder} API (Log4j 2.13 or later) on the compile
     * classpath; compilation fails otherwise. Argument types are unknown when the
     * transform runs, so a {@code Marker} first argument or a {@code Throwable}
     * last argument is recognised, at run time, when it is passed as a variable
     * (the usual {@code log.error("...", e)} shape); other expressions in those
     * positions are logged as message parameters. Statements inside a closure
     * name the enclosing method rather than the closure's {@code doCall}.
     * Only statements made through the injected logger field are rewritten.
     *
     * @return {@code true} to supply locations at compile time
     * @since 6.0.0
     */
    boolean staticLocation() default false;

    //

    /**
     * Logging strategy for Log4j 2.
     */
    class Log4j2LoggingStrategy extends LogASTTransformation.AbstractLoggingStrategyV2 {

        private static final String LOG_BUILDER = "org.apache.logging.log4j.LogBuilder";
        private static final String MARKER = "org.apache.logging.log4j.Marker";

        /**
         * Creates a Log4j 2 logging strategy.
         *
         * @param loader the class loader used to resolve logger classes
         */
        protected Log4j2LoggingStrategy(final GroovyClassLoader loader) {
            super(loader);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FieldNode addLoggerFieldToClass(final ClassNode classNode, final String logFieldName, final String categoryName, final int fieldModifiers) {
            ClassNode fieldType = classNode("org.apache.logging.log4j.Logger"); // GROOVY-11798

            MethodCallExpression fieldValue = new MethodCallExpression(
                    classX(classNode("org.apache.logging.log4j.LogManager")),
                    "getLogger",
                    constX(getCategoryName(classNode, categoryName)));
            fieldValue.setImplicitThis(false);

            return classNode.addField(logFieldName, fieldModifiers, fieldType, fieldValue);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isLoggingMethod(final String methodName) {
            return methodName.matches("fatal|error|warn|info|debug|trace");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Expression wrapLoggingMethodCall(final Expression logVariable, final String methodName, final Expression originalExpression) {
            MethodCallExpression condition = new MethodCallExpression(
                    logVariable,
                    "is" + methodName.substring(0, 1).toUpperCase(Locale.ENGLISH) + methodName.substring(1) + "Enabled",
                    ArgumentListExpression.EMPTY_ARGUMENTS);
            condition.setImplicitThis(false);

            return ternaryX(condition, originalExpression, nullX());
        }

        /**
         * {@inheritDoc}
         * <p>Requires Log4j 2.13's {@code LogBuilder} to be resolvable.
         */
        @Override
        public String staticLocationUnsupportedReason() {
            try {
                classNode(LOG_BUILDER);
                return null;
            } catch (GroovyRuntimeException e) {
                return LOG_BUILDER + " (Log4j 2.13 or later) is not on the compile classpath";
            }
        }

        /**
         * {@inheritDoc}
         * <p>Emits {@code log.atLevel().withLocation(location).log(args)}. Argument
         * types are unknown at compile time, so when a call has several arguments
         * and its first or last one is a variable, that variable is tested at run
         * time: a leading {@code Marker} goes through {@code withMarker} and a
         * trailing {@code Throwable} through {@code withThrowable}, which the
         * builder's {@code log} methods do not do on their own. Non-simple
         * arguments keep the {@code isLevelEnabled} guard so they are not
         * evaluated for a disabled level.
         */
        @Override
        public Expression wrapLoggingMethodCallWithLocation(final Expression logVariable, final String methodName, final MethodCallExpression originalCall, final Expression location, final boolean guard) {
            String level = methodName.substring(0, 1).toUpperCase(Locale.ENGLISH) + methodName.substring(1);
            List<Expression> arguments = originalCall.getArguments() instanceof TupleExpression tuple
                    ? tuple.getExpressions() : List.of(originalCall.getArguments());

            Expression call = emitX(() -> builderX(logVariable, level, location), arguments);
            call.setSourcePosition(originalCall);

            if (!guard) return call;
            MethodCallExpression condition = callX(logVariable, "is" + level + "Enabled", ArgumentListExpression.EMPTY_ARGUMENTS);
            return ternaryX(condition, call, nullX());
        }

        /**
         * Builds the {@code log(...)} call, branching at run time on a leading
         * marker variable and then on a trailing throwable variable. Each branch
         * gets its own builder expression tree; AST nodes must not be shared.
         */
        private Expression emitX(final Supplier<Expression> builder, final List<Expression> arguments) {
            if (arguments.size() >= 2 && arguments.get(0) instanceof VariableExpression maybeMarker) {
                List<Expression> rest = arguments.subList(1, arguments.size());
                Expression marked = emitThrowableX(() -> callX(builder.get(), "withMarker", args(maybeMarker)), rest);
                Expression plain = emitThrowableX(builder, arguments);
                return ternaryX(isInstanceOfX(maybeMarker, classNode(MARKER)), marked, plain);
            }
            return emitThrowableX(builder, arguments);
        }

        private Expression emitThrowableX(final Supplier<Expression> builder, final List<Expression> arguments) {
            int last = arguments.size() - 1;
            if (arguments.size() >= 2 && arguments.get(last) instanceof VariableExpression maybeThrowable) {
                List<Expression> init = arguments.subList(0, last);
                Expression thrown = callX(callX(builder.get(), "withThrowable", args(maybeThrowable)), "log", args(init));
                Expression plain = callX(builder.get(), "log", args(arguments));
                return ternaryX(isInstanceOfX(maybeThrowable, ClassHelper.make(Throwable.class)), thrown, plain);
            }
            return callX(builder.get(), "log", args(arguments));
        }

        private static Expression builderX(final Expression logVariable, final String level, final Expression location) {
            return callX(callX(logVariable, "at" + level), "withLocation", args(location));
        }

        private static MethodCallExpression callX(final Expression receiver, final String name, final Expression arguments) {
            MethodCallExpression call = new MethodCallExpression(receiver, name, arguments);
            call.setImplicitThis(false);
            return call;
        }

        private static MethodCallExpression callX(final Expression receiver, final String name) {
            return callX(receiver, name, ArgumentListExpression.EMPTY_ARGUMENTS);
        }
    }
}
