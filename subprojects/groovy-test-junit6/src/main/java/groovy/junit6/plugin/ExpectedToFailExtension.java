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
package groovy.junit6.plugin;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * JUnit 5 {@link InvocationInterceptor} backing the {@link ExpectedToFail}
 * annotation. Inverts a test's pass/fail outcome with optional exception-type
 * and message-substring filters.
 * <p>
 * Composes with {@link ForkedJvm} in either declaration order via explicit
 * coordination through {@link ExtensionContext.Store} and a child-JVM system
 * property &mdash; not via brittle reliance on annotation iteration order.
 *
 * @since 6.0.0
 */
public class ExpectedToFailExtension implements InvocationInterceptor {

    /**
     * Shared {@link ExtensionContext.Namespace} used to coordinate inversion
     * placement between this extension and {@link ForkedJvmExtension}.
     */
    static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create("groovy.junit6.expectedtofail");

    /**
     * Store key set by the parent's {@code @ExpectedToFail} interceptor when
     * it intends to invert the outcome itself (i.e. it's the OUTER
     * interceptor relative to {@code @ForkedJvm}). {@link ForkedJvmExtension}
     * reads this key and, if present, instructs the forked child to defer
     * inversion via {@link #DEFERRED_TO_PARENT_PROP}.
     */
    static final String STORE_KEY_PARENT_INVERTING = "parentInverting";

    /**
     * System property set on the child JVM's command line by
     * {@link ForkedJvmExtension} when the parent's {@code @ExpectedToFail}
     * has claimed the inversion. The child's {@code @ExpectedToFail}
     * interceptor checks this property and passes through honestly so the
     * parent can observe the propagated outcome.
     */
    public static final String DEFERRED_TO_PARENT_PROP = "groovy.junit6.expectedtofail.deferred";

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        ExpectedToFail config = findAnnotation(extensionContext);
        if (config == null) {
            invocation.proceed();
            return;
        }
        Method method = invocationContext.getExecutable();

        // If this is the child JVM and the parent claimed inversion, just pass
        // through; the parent's interceptor will observe the propagated outcome.
        if (Boolean.parseBoolean(System.getProperty(DEFERRED_TO_PARENT_PROP))) {
            invocation.proceed();
            return;
        }

        // Otherwise this layer handles inversion. If we're in the parent and
        // @ForkedJvm is also present, signal the fork (via the shared store)
        // so the child's interceptor knows to defer to us.
        boolean inForkedJvm = Boolean.parseBoolean(System.getProperty(ForkedJvmTestRunner.FORKED_FLAG));
        if (!inForkedJvm && annotationOnMethodOrClass(method, ForkedJvm.class)) {
            extensionContext.getStore(NAMESPACE).put(STORE_KEY_PARENT_INVERTING, Boolean.TRUE);
        }

        Throwable thrown = null;
        try {
            invocation.proceed();
        } catch (TestAbortedException tae) {
            // Assumption failures never count as the expected failure.
            throw tae;
        } catch (Throwable t) {
            thrown = t;
        }
        evaluateOutcome(thrown, config);
    }

    private static boolean annotationOnMethodOrClass(Method method, Class<? extends Annotation> a) {
        return method.isAnnotationPresent(a) || method.getDeclaringClass().isAnnotationPresent(a);
    }

    private static void evaluateOutcome(Throwable thrown, ExpectedToFail config) {
        Class<?> value = config.value();
        Class<? extends Throwable> exception = config.exception();
        boolean valueSet = value != Throwable.class;
        boolean exceptionSet = exception != Throwable.class;
        boolean valueIsClosure = Closure.class.isAssignableFrom(value);
        if (valueSet && !valueIsClosure && exceptionSet) {
            throw new AssertionError(
                    "@ExpectedToFail: 'value' (as Throwable type) and 'exception' are mutually "
                            + "exclusive — use a closure for 'value' if you also want a type guard");
        }
        if (valueSet && !valueIsClosure && !Throwable.class.isAssignableFrom(value)) {
            throw new AssertionError(
                    "@ExpectedToFail value must be a Throwable or Closure subclass, was: "
                            + value.getName());
        }
        if (thrown != null) {
            Class<? extends Throwable> typeFilter;
            if (exceptionSet) {
                typeFilter = exception;
            } else if (valueSet && !valueIsClosure) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> t = (Class<? extends Throwable>) value;
                typeFilter = t;
            } else {
                typeFilter = Throwable.class;
            }
            if (!typeFilter.isInstance(thrown)) {
                throw new AssertionError("@ExpectedToFail expected exception of type "
                        + typeFilter.getName() + " but got "
                        + thrown.getClass().getName() + ": " + thrown.getMessage(), thrown);
            }
            if (valueIsClosure && !evaluateClosure(value, thrown)) {
                throw new AssertionError("@ExpectedToFail predicate did not match thrown "
                        + thrown.getClass().getName() + ": " + thrown.getMessage(), thrown);
            }
            return; // matched: swallow, treat as success
        }
        String reason = config.reason();
        throw new AssertionError("@ExpectedToFail: test was expected to fail but passed"
                + (reason.isEmpty() ? "" : " (" + reason + ")"));
    }

    private static boolean evaluateClosure(Class<?> closureClass, Throwable thrown) {
        Closure<?> closure;
        try {
            closure = (Closure<?>) closureClass.getConstructor(Object.class, Object.class)
                    .newInstance(null, null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(
                    "@ExpectedToFail: failed to instantiate closure predicate", e);
        }
        closure.setDelegate(new ExpectedToFailContext(thrown));
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        return DefaultTypeTransformation.castToBoolean(closure.call());
    }

    private static ExpectedToFail findAnnotation(ExtensionContext context) {
        return context.getElement()
                .map(el -> el.getAnnotation(ExpectedToFail.class))
                .orElseGet(() -> context.getTestClass()
                        .map(c -> c.getAnnotation(ExpectedToFail.class))
                        .orElse(null));
    }
}
