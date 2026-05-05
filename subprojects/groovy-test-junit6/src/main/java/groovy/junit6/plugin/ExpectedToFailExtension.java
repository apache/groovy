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
        if (thrown != null) {
            if (!config.value().isInstance(thrown)) {
                throw new AssertionError("@ExpectedToFail expected exception of type "
                        + config.value().getName() + " but got "
                        + thrown.getClass().getName() + ": " + thrown.getMessage(), thrown);
            }
            String wanted = config.messageContains();
            if (!wanted.isEmpty()
                    && (thrown.getMessage() == null || !thrown.getMessage().contains(wanted))) {
                throw new AssertionError("@ExpectedToFail expected message containing '"
                        + wanted + "' but got: " + thrown.getMessage(), thrown);
            }
            return; // matched: swallow, treat as success
        }
        String reason = config.reason();
        throw new AssertionError("@ExpectedToFail: test was expected to fail but passed"
                + (reason.isEmpty() ? "" : " (" + reason + ")"));
    }

    private static ExpectedToFail findAnnotation(ExtensionContext context) {
        return context.getElement()
                .map(el -> el.getAnnotation(ExpectedToFail.class))
                .orElseGet(() -> context.getTestClass()
                        .map(c -> c.getAnnotation(ExpectedToFail.class))
                        .orElse(null));
    }
}
