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
package groovy.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for GroovySystem class.
 */
class GroovySystemJUnit5Test {

    @Test
    void testGetMetaClassRegistry() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        assertNotNull(registry);
    }

    @Test
    void testGetMetaClassRegistryReturnsSameInstance() {
        MetaClassRegistry registry1 = GroovySystem.getMetaClassRegistry();
        MetaClassRegistry registry2 = GroovySystem.getMetaClassRegistry();
        assertSame(registry1, registry2);
    }

    @Test
    void testGetVersion() {
        String version = GroovySystem.getVersion();
        assertNotNull(version);
        assertFalse(version.isEmpty());
    }

    @Test
    void testGetShortVersion() {
        String shortVersion = GroovySystem.getShortVersion();
        assertNotNull(shortVersion);
        
        // Short version should contain exactly one dot
        int dotCount = shortVersion.length() - shortVersion.replace(".", "").length();
        assertEquals(1, dotCount);
        
        // Should start with a number
        assertTrue(Character.isDigit(shortVersion.charAt(0)));
    }

    @Test
    void testShortVersionIsPartOfFullVersion() {
        String fullVersion = GroovySystem.getVersion();
        String shortVersion = GroovySystem.getShortVersion();
        
        assertTrue(fullVersion.startsWith(shortVersion));
    }

    @Test
    void testIsKeepJavaMetaClassesDefault() {
        // Default should be false
        boolean original = GroovySystem.isKeepJavaMetaClasses();
        try {
            // Test the default behavior
            assertFalse(original);
        } finally {
            // Restore original value
            GroovySystem.setKeepJavaMetaClasses(original);
        }
    }

    @Test
    void testSetKeepJavaMetaClasses() {
        boolean original = GroovySystem.isKeepJavaMetaClasses();
        try {
            GroovySystem.setKeepJavaMetaClasses(true);
            assertTrue(GroovySystem.isKeepJavaMetaClasses());
            
            GroovySystem.setKeepJavaMetaClasses(false);
            assertFalse(GroovySystem.isKeepJavaMetaClasses());
        } finally {
            GroovySystem.setKeepJavaMetaClasses(original);
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    void testIsUseReflection() {
        // This is deprecated but we can still test it
        assertTrue(GroovySystem.isUseReflection());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testRunnerRegistry() {
        // RUNNER_REGISTRY is deprecated but we can verify it exists
        assertNotNull(GroovySystem.RUNNER_REGISTRY);
    }

    @Test
    void testVersionFormat() {
        String version = GroovySystem.getVersion();
        // Version should match pattern like "4.0.0", "4.0.1-SNAPSHOT", "4.0.0-rc-1", etc.
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+.*"), 
            "Version should match pattern X.Y.Z[suffix], got: " + version);
    }

    @Test
    void testStopThreadedReferenceManager() {
        // This should not throw any exception
        assertDoesNotThrow(() -> GroovySystem.stopThreadedReferenceManager());
    }
}
