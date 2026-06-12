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

import groovy.junit6.plugin.ExpectedToFail
import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.junit.platform.launcher.listeners.TestExecutionSummary

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class ExpectedToFailTest {

    // ---------------- happy paths (the body throws as expected) ----------------

    @Test
    @ExpectedToFail
    void throwingBodyIsTreatedAsSuccess() {
        throw new AssertionError('expected boom')
    }

    @Test
    @ExpectedToFail(IllegalStateException)
    void typeFilterMatchesExactType() {
        throw new IllegalStateException('boom')
    }

    @Test
    @ExpectedToFail(RuntimeException)
    void typeFilterMatchesSubtype() {
        throw new IllegalStateException('boom')
    }

    @Test
    @ExpectedToFail({ message.contains('cosmic ray') })
    void closurePredicateOnMessageBinding() {
        throw new RuntimeException('hit by a cosmic ray')
    }

    @Test
    @ExpectedToFail({ ex instanceof IllegalArgumentException && message.contains('bad') })
    void closurePredicateCombinesTypeAndMessage() {
        throw new IllegalArgumentException('this is bad input')
    }

    @Test
    @ExpectedToFail({ cause?.message == 'root' })
    void closurePredicateOnCauseBinding() {
        throw new RuntimeException('wrapper', new IllegalStateException('root'))
    }

    @Test
    @ExpectedToFail(exception = RuntimeException)
    void typeSafeExceptionAttribute() {
        throw new IllegalStateException('boom')
    }

    @Test
    @ExpectedToFail(exception = IllegalArgumentException, value = { message.contains('bad') })
    void exceptionGuardComposesWithClosurePredicate() {
        throw new IllegalArgumentException('this is bad input')
    }

    // ---------------- composition with @ForkedJvm ----------------

    @Test
    @ExpectedToFail({ ex instanceof AssertionError && message.contains('forked failure') })
    @ForkedJvm
    void outerOrdering_failurePropagatesFromForkAndIsInverted() {
        // ExpectedToFail OUTER: parent does the inversion AFTER @ForkedJvm
        // serializes the failure across the JVM boundary, exercising the
        // round-trip and verifying type+message fidelity.
        throw new AssertionError('forked failure round-trips')
    }

    @Test
    @ForkedJvm
    @ExpectedToFail({ ex instanceof AssertionError && message.contains('forked failure') })
    void innerOrdering_inversionHappensInsideForkedChild() {
        // ExpectedToFail INNER: child JVM swallows the failure; parent sees
        // success. Doesn't exercise the parent's view of the propagation.
        throw new AssertionError('forked failure handled inside child')
    }

    // ---------------- negative paths (verified via Launcher) ----------------

    @Test
    void unexpectedlyPassingTestIsReportedAsFailure() {
        TestExecutionSummary summary = runFixture(BadFixtures, 'passingMethod')
        assertEquals(1, summary.totalFailureCount)
        assertTrue(summary.failures[0].exception.message.contains('expected to fail but passed'))
    }

    @Test
    void typeFilterMismatchIsReportedAsFailure() {
        TestExecutionSummary summary = runFixture(BadFixtures, 'wrongTypeMethod')
        assertEquals(1, summary.totalFailureCount)
        def msg = summary.failures[0].exception.message
        assertTrue(msg.contains('expected exception of type'),
                "actual: ${msg}")
        assertTrue(msg.contains('IllegalStateException'),
                "actual: ${msg}")
    }

    @Test
    void closurePredicateMismatchIsReportedAsFailure() {
        TestExecutionSummary summary = runFixture(BadFixtures, 'wrongMessageMethod')
        assertEquals(1, summary.totalFailureCount)
        def msg = summary.failures[0].exception.message
        assertTrue(msg.contains('predicate did not match'),
                "actual: ${msg}")
    }

    @Test
    void valueAndExceptionTogetherIsReportedAsFailure() {
        TestExecutionSummary summary = runFixture(BadFixtures, 'bothValueAndExceptionMethod')
        assertEquals(1, summary.totalFailureCount)
        def msg = summary.failures[0].exception.message
        assertTrue(msg.contains('mutually exclusive'),
                "actual: ${msg}")
    }

    @Test
    void assumptionAbortIsNotSwallowed() {
        TestExecutionSummary summary = runFixture(BadFixtures, 'assumptionMethod')
        // Aborts count as skipped, not failed; but they're definitely NOT
        // counted as passes — that's the property we care about.
        assertEquals(0, summary.totalFailureCount)
        assertTrue(summary.testsAbortedCount >= 1,
                "expected at least one aborted test, summary: ${summary}")
    }

    @Test
    void reasonAttributeAppearsInUnexpectedlyPassingFailure() {
        TestExecutionSummary summary = runFixture(BadFixtures, 'reasonedPassingMethod')
        assertEquals(1, summary.totalFailureCount)
        assertTrue(summary.failures[0].exception.message.contains('GROOVY-9999 placeholder'),
                "actual: ${summary.failures[0].exception.message}")
    }

    // ---------------- helper ----------------

    private static TestExecutionSummary runFixture(Class<?> fixtureClass, String methodName) {
        def listener = new SummaryGeneratingListener()
        def launcher = LauncherFactory.create()
        launcher.registerTestExecutionListeners(listener)
        launcher.execute(LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectMethod(fixtureClass, methodName))
                .build())
        listener.summary
    }

    /**
     * Fixtures used as inputs to the negative-path tests; tagged
     * {@code manual} so the suite-level config excludes them from normal
     * discovery — we invoke them explicitly via the Launcher.
     */
    @Tag('manual')
    static class BadFixtures {
        @Test
        @ExpectedToFail
        void passingMethod() {
            // returns normally — should be reported as failure
        }

        @Test
        @ExpectedToFail(IllegalStateException)
        void wrongTypeMethod() {
            throw new IllegalArgumentException('wrong type')
        }

        @Test
        @ExpectedToFail({ message.contains('foo') })
        void wrongMessageMethod() {
            throw new RuntimeException('bar')
        }

        @Test
        @ExpectedToFail(value = RuntimeException, exception = RuntimeException)
        void bothValueAndExceptionMethod() {
            throw new RuntimeException('boom')
        }

        @Test
        @ExpectedToFail
        void assumptionMethod() {
            Assumptions.assumeTrue(false, 'unconditional abort')
        }

        @Test
        @ExpectedToFail(reason = 'GROOVY-9999 placeholder')
        void reasonedPassingMethod() {
            // returns normally
        }
    }
}
