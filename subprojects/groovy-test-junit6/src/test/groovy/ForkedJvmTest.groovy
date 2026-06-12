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
import groovy.junit6.plugin.ForkedJvmExtension
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import java.util.regex.Pattern
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class ForkedJvmTest {

    private static final String INHERIT_EXACT = 'groovy.junit6.test.inherit.exact'
    private static final String INHERIT_GLOB_A = 'groovy.junit6.test.inherit.glob.a'
    private static final String INHERIT_GLOB_B = 'groovy.junit6.test.inherit.glob.b'

    @BeforeAll
    static void setParentProperties() {
        // Lifecycle hooks fire in BOTH parent and forked-child JVMs (the child
        // re-runs the class lifecycle for the single targeted method). Only
        // seed these properties in the parent — in the child they must arrive
        // exclusively via @ForkedJvm propagation, otherwise the negative
        // assertions below would be self-defeating.
        if (Boolean.parseBoolean(System.getProperty('groovy.junit6.forked'))) return
        System.setProperty(INHERIT_EXACT, 'exact-value')
        System.setProperty(INHERIT_GLOB_A, 'a-value')
        System.setProperty(INHERIT_GLOB_B, 'b-value')
    }

    @AfterAll
    static void clearParentProperties() {
        if (Boolean.parseBoolean(System.getProperty('groovy.junit6.forked'))) return
        System.clearProperty(INHERIT_EXACT)
        System.clearProperty(INHERIT_GLOB_A)
        System.clearProperty(INHERIT_GLOB_B)
    }

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
    @ForkedJvm(inheritProperties = ['groovy.junit6.test.inherit.exact'])
    void inheritsExactProperty() {
        assertEquals('exact-value', System.getProperty(INHERIT_EXACT))
        // Glob-prefixed siblings are NOT pulled in by an exact-match entry.
        assertNull(System.getProperty(INHERIT_GLOB_A))
        assertNull(System.getProperty(INHERIT_GLOB_B))
    }

    @Test
    @ForkedJvm(inheritProperties = ['groovy.junit6.test.inherit.glob.*'])
    void inheritsByPrefixPattern() {
        assertEquals('a-value', System.getProperty(INHERIT_GLOB_A))
        assertEquals('b-value', System.getProperty(INHERIT_GLOB_B))
        // The exact-only sibling must not be matched by the glob prefix.
        assertNull(System.getProperty(INHERIT_EXACT))
    }

    @Test
    @ForkedJvm(
            inheritProperties = ['groovy.junit6.test.inherit.exact'],
            systemProperties = ['groovy.junit6.test.inherit.exact=overridden'])
    void explicitSystemPropertyOverridesInherited() {
        // Both supplied; explicit value wins because it is emitted last on the
        // command line, and the JVM honours the last -D for a given key.
        assertEquals('overridden', System.getProperty(INHERIT_EXACT))
    }

    @Test
    @ForkedJvm(inheritProperties = ['does.not.exist.in.parent'])
    void unmatchedInheritPatternIsSilentNoOp() {
        // An inheritProperties entry that matches nothing in the parent JVM
        // must not throw or pollute the child — it is a quiet no-op.
        assertNull(System.getProperty('does.not.exist.in.parent'))
    }

    // ---------------- excludeFromClasspath ----------------

    @Test
    void filterClasspath_dropsMatchingEntries() {
        def sep = File.pathSeparator
        def cp = "/a/foo-1.0.jar${sep}/b/junit-platform-instrumentation-1.9.0.jar${sep}/c/bar-2.0.jar"
        def filtered = ForkedJvmExtension.filterClasspath(cp, [Pattern.compile('junit-platform-instrumentation')])
        assertEquals("/a/foo-1.0.jar${sep}/c/bar-2.0.jar".toString(), filtered)
    }

    @Test
    void filterClasspath_supportsMultiplePatterns() {
        def sep = File.pathSeparator
        def cp = "/a/foo.jar${sep}/b/bar.jar${sep}/c/baz.jar"
        def filtered = ForkedJvmExtension.filterClasspath(cp,
                [Pattern.compile('foo'), Pattern.compile('baz')])
        assertEquals("/b/bar.jar", filtered)
    }

    @Test
    void filterClasspath_unchangedWhenNoMatches() {
        def sep = File.pathSeparator
        def cp = "/a/foo-1.0.jar${sep}/b/bar-2.0.jar".toString()
        def filtered = ForkedJvmExtension.filterClasspath(cp, [Pattern.compile('nothing-matches-this')])
        assertEquals(cp, filtered)
    }

    @Test
    void filterClasspath_unchangedWithEmptyPatterns() {
        def cp = "/a/foo.jar${File.pathSeparator}/b/bar.jar".toString()
        def filtered = ForkedJvmExtension.filterClasspath(cp, [])
        assertEquals(cp, filtered)
    }

    @Test
    void filterClasspath_handlesNullAndEmpty() {
        assertNull(ForkedJvmExtension.filterClasspath(null, [Pattern.compile('x')]))
        assertEquals('', ForkedJvmExtension.filterClasspath('', [Pattern.compile('x')]))
    }

    @Test
    @ForkedJvm(excludeFromClasspath = ['this-pattern-matches-no-real-jar-xyz'])
    void excludeWithNoMatchPreservesChildClasspath() {
        // End-to-end: the wiring runs the parent's classpath through the
        // filter and lands on the child's -cp. With a pattern that matches
        // nothing real, the child must still boot, find its classes, and
        // run this assertion.
        def cp = System.getProperty('java.class.path')
        assertTrue(cp != null && !cp.isEmpty(), "child classpath was empty")
        assertTrue(!cp.contains('this-pattern-matches-no-real-jar-xyz'),
                "the test pattern should not appear in any real classpath entry")
    }

    @Test
    @ExpectedToFail({ ex instanceof AssertionError && message.contains('expected failure from forked child') })
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
