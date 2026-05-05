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
 * Optionally constrain the expected failure by exception type and/or message
 * substring:
 * <pre>
 * &#64;Test
 * &#64;ExpectedToFail(IllegalStateException.class)
 * void mustThrowIse() { throw new IllegalStateException("boom") }
 *
 * &#64;Test
 * &#64;ExpectedToFail(messageContains = "boom")
 * void mustThrowWithBoomInMessage() { throw new RuntimeException("kaboom!") }
 * </pre>
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
 * {@code @ForkedJvm}'s serialisation. When declared after (inner), the
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
     * Expected exception type. The thrown exception must be an instance of
     * this class (or a subclass) for the test to be reported as passing.
     * Defaults to {@link Throwable}, which matches anything.
     */
    Class<? extends Throwable> value() default Throwable.class;

    /**
     * Substring that must appear in the thrown exception's message.
     * Defaults to empty (no message check).
     */
    String messageContains() default "";

    /**
     * Optional human-readable explanation of why this test is expected to fail.
     * Surfaced in the failure message when the test unexpectedly passes.
     */
    String reason() default "";
}
