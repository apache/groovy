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
package groovy.lang


import org.junit.jupiter.api.Test

/**
 * Tests for the GroovySystem class
 */
class GroovySystemTest {
    @Test
    void testGetMetaClassRegistry() {
        def registry = GroovySystem.getMetaClassRegistry()
        assert registry != null
    }

    @Test
    void testGroovyVersion() {
        assert GroovySystem.getVersion()
    }

    // --- Merged from GroovySystemJUnit5Test ---
    @Test
    void testGetMetaClassRegistryReturnsSameInstance() {
        def registry1 = GroovySystem.getMetaClassRegistry()
        def registry2 = GroovySystem.getMetaClassRegistry()
        assert registry1.is(registry2)
    }

    @Test
    void testGetVersion() {
        def version = GroovySystem.getVersion()
        assert version != null
        assert !version.isEmpty()
    }

    @Test
    void testGetShortVersion() {
        def shortVersion = GroovySystem.getShortVersion()
        assert shortVersion != null

        // Short version should contain exactly one dot
        def dotCount = shortVersion.length() - shortVersion.replace(".", "").length()
        assert 1 == dotCount

        // Should start with a number
        assert Character.isDigit(shortVersion.charAt(0))
    }

    @Test
    void testShortVersionIsPartOfFullVersion() {
        def fullVersion = GroovySystem.getVersion()
        def shortVersion = GroovySystem.getShortVersion()

        assert fullVersion.startsWith(shortVersion)
    }

    @Test
    void testIsKeepJavaMetaClassesDefault() {
        def original = GroovySystem.isKeepJavaMetaClasses()
        try {
            assert !original
        } finally {
            GroovySystem.setKeepJavaMetaClasses(original)
        }
    }

    @Test
    void testSetKeepJavaMetaClasses() {
        def original = GroovySystem.isKeepJavaMetaClasses()
        try {
            GroovySystem.setKeepJavaMetaClasses(true)
            assert GroovySystem.isKeepJavaMetaClasses()

            GroovySystem.setKeepJavaMetaClasses(false)
            assert !GroovySystem.isKeepJavaMetaClasses()
        } finally {
            GroovySystem.setKeepJavaMetaClasses(original)
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    void testIsUseReflection() {
        assert GroovySystem.isUseReflection()
    }

    @Test
    @SuppressWarnings("deprecation")
    void testRunnerRegistry() {
        assert GroovySystem.RUNNER_REGISTRY != null
    }

    @Test
    void testVersionFormat() {
        def version = GroovySystem.getVersion()
        assert version.matches("\\d+\\.\\d+\\.\\d+.*") : "Version should match pattern X.Y.Z[suffix], got: " + version
    }

    @Test
    void testStopThreadedReferenceManager() {
        // This should not throw any exception
        GroovySystem.stopThreadedReferenceManager()
    }
}
