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

import org.apache.groovy.lang.annotation.Incubating;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inverts the pass/fail outcome of the annotated test method (or every test
 * method on the annotated class): a test that throws is reported as passing,
 * and a test that completes normally is reported as failing.
 * <p>
 * Useful for asserting that some condition reliably fails, and for testing
 * test-infrastructure code that should propagate failures.
 * <p>
 * Optionally constrain the expected failure by exception type or by a closure
 * predicate evaluated against the thrown exception:
 * <pre>
 * &#64;Test
 * &#64;ExpectedToFail(IllegalStateException)
 * void mustThrowIse() { throw new IllegalStateException("boom") }
 *
 * &#64;Test
 * &#64;ExpectedToFail({ ex instanceof RuntimeException &amp;&amp; message.contains('boom') })
 * void mustMatchPredicate() { throw new RuntimeException("kaboom!") }
 * </pre>
 * <p>
 * Closure predicates are evaluated with three bindings: {@code ex} (the
 * thrown exception), {@code message} (its message, possibly {@code null}),
 * and {@code cause} (its cause, possibly {@code null}). The test is
 * reported as passing iff the predicate returns a Groovy-truthy value.
 * <p>
 * For callers who want compile-time enforcement that the configured class
 * is a {@link Throwable} subclass, set {@link #exception()} instead of
 * (or alongside) {@link #value()}. {@code exception} and a closure
 * {@code value} compose: the type acts as a guard that runs before the
 * predicate, so the closure body can drop the {@code ex instanceof X}
 * boilerplate. {@code exception} and a {@link Throwable} {@code value} are
 * mutually exclusive (they would specify the type twice).
 * <p>
 * {@code TestAbortedException} (the exception thrown by JUnit
 * {@code Assumptions}) is never treated as the expected failure; it's
 * always rethrown so the test is reported as aborted.
 * <p>
 * <b>Composition with {@link ForkedJvm}:</b> works in either declaration
 * order via explicit coordination between the two extensions (no reliance on
 * annotation iteration order). When {@code @ExpectedToFail} is declared
 * <em>before</em> {@code @ForkedJvm} (i.e., outer), the inversion happens in
 * the parent JVM after the failure propagates from the fork — exercising
 * {@code @ForkedJvm}'s serialization. When declared after (inner), the
 * inversion happens inside the forked child.
 *
 * @since 6.0.0
 */
@Documented
@Incubating
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(ExpectedToFailExtension.class)
public @interface ExpectedToFail {
    /**
     * Either an expected {@link Throwable} subclass or a {@code Closure}
     * predicate evaluated against the thrown exception. Defaults to
     * {@link Throwable}, which matches anything.
     * <p>
     * When set to a {@link Throwable} subclass, mutually exclusive with
     * {@link #exception()}. When set to a closure, composes with
     * {@link #exception()}: the type guard is checked first, then the
     * closure predicate.
     */
    Class<?> value() default Throwable.class;

    /**
     * Type-safe alternative to {@link #value()}: declares the expected
     * exception type with compile-time enforcement that it is a
     * {@link Throwable} subclass. Defaults to {@link Throwable}, which
     * matches anything.
     * <p>
     * Mutually exclusive with a {@link Throwable} {@link #value()};
     * composes with a closure {@link #value()} as a type guard evaluated
     * before the predicate.
     */
    Class<? extends Throwable> exception() default Throwable.class;

    /**
     * Optional human-readable explanation of why this test is expected to fail.
     * Surfaced in the failure message when the test unexpectedly passes.
     */
    String reason() default "";
}
