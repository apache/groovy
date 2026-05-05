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

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class ForkedJvmTest {

    @Test
    @ForkedJvm(systemProperties = ['groovy.junit6.test.example=hello'])
    void runsInChildJvmWithSystemProperty() {
        assertEquals('hello', System.getProperty('groovy.junit6.test.example'))
    }

    @Test
    void unannotatedTestRunsInParentJvm() {
        // No @ForkedJvm, no fork: the property set only in the forked test above
        // should be invisible here.
        assertNull(System.getProperty('groovy.junit6.test.example'))
    }

    @Test
    @ForkedJvm(systemProperties = ['groovy.junit6.test.a=1', 'groovy.junit6.test.b=2'])
    void multipleSystemPropertiesArePassed() {
        assertEquals('1', System.getProperty('groovy.junit6.test.a'))
        assertEquals('2', System.getProperty('groovy.junit6.test.b'))
    }

    @Test
    @ForkedJvm(jvmArgs = ['-Xmx64m'])
    void jvmArgIsApplied() {
        // -Xmx64m is innocuous; just assert we're in a fresh JVM by checking
        // the heap max is at most ~64m (with some headroom for JVM overhead).
        long max = Runtime.runtime.maxMemory()
        assertTrue(max < 128L * 1024 * 1024,
                "expected max heap < 128m under -Xmx64m, was ${max}")
    }

    @Test
    @ForkedJvm(systemProperties = ['groovy.junit6.test.recursion=outer'])
    void doesNotRecursivelyFork() {
        // If recursion guard worked, the FORKED_FLAG is true here in the child.
        assertEquals('true', System.getProperty('groovy.junit6.forked'))
        assertEquals('outer', System.getProperty('groovy.junit6.test.recursion'))
    }

    @Test
    @ForkedJvm
    void emptyAnnotationStillForks() {
        // No properties or args, but the FORKED_FLAG should still be set.
        assertEquals('true', System.getProperty('groovy.junit6.forked'))
    }

    @Test
    @ExpectedToFail(value = AssertionError, messageContains = 'expected failure from forked child')
    @ForkedJvm
    void failureInChildJvmPropagatesToParent() {
        // @ExpectedToFail is OUTER (declared before @ForkedJvm) so the
        // inversion happens in the parent JVM AFTER @ForkedJvm propagates the
        // failure across the fork boundary. The type-and-message filters
        // verify the failure round-tripped without losing fidelity.
        throw new AssertionError('expected failure from forked child')
    }

    @Test
    void abortInChildJvmPropagatesAsAbortNotPass() {
        // Run the AbortFixture's @ForkedJvm test (which calls
        // Assumptions.assumeFalse(true)) via the Launcher and assert that the
        // outcome is reported as ABORTED, not PASSED. Regression guard for
        // Copilot review comment #2.
        def listener = new SummaryGeneratingListener()
        def launcher = LauncherFactory.create()
        launcher.registerTestExecutionListeners(listener)
        launcher.execute(LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectMethod(AbortFixture, 'aborts'))
                .build())

        def summary = listener.summary
        assertEquals(0, summary.totalFailureCount,
                "expected no failures, got: ${summary.failures*.exception}")
        assertEquals(1, summary.testsAbortedCount,
                "expected exactly one aborted test, summary: tests=${summary.testsFoundCount} aborted=${summary.testsAbortedCount} succeeded=${summary.testsSucceededCount}")
    }

    /**
     * Fixture for {@link #abortInChildJvmPropagatesAsAbortNotPass}. Tagged
     * {@code manual} so the suite-level config excludes it from normal runs.
     */
    @Tag('manual')
    static class AbortFixture {
        @Test
        @ForkedJvm
        void aborts() {
            Assumptions.assumeFalse(true, 'unconditional abort from forked child')
        }
    }
}
