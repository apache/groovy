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
package org.codehaus.groovy.runtime.m12n

import org.junit.Test

import static org.codehaus.groovy.runtime.m12n.ExtensionModuleHelperForTests.doInFork

/**
 * Unit tests for extension methods loading.
 */
final class ExtensionModuleTest {

    @Test
    void testThatModuleHasBeenLoaded() {
        doInFork '''
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            assert registry.modules
            // look for the 'Test module' module; it should always be available
            assert registry.modules.any { it.name == 'Test module' && it.version == '1.0-test' }

            // the following methods are added by the test module
            def str = 'This is a string'
            assert str.reverseToUpperCase() == str.toUpperCase().reverse()
            assert String.answer() == 42
        '''
    }

    @Test
    void testThatModuleCanBeLoadedWithGrab() {
        doInFork '''
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.4' }

            // find jar resource
            def jarURL = this.class.getResource("/jars")
            assert jarURL

            def resolver = "@GrabResolver('$jarURL')"

            assertScript resolver + """
                @Grab(value='module-test:module-test:1.4', changing='true')
                import org.codehaus.groovy.runtime.m12n.*

                // ensure that the module is now loaded
                ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
                assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.4' }

                // the following methods are added by the 'Test module for Grab' module
                def str = 'This is a string'
                assert str.reverseToUpperCase2() == str.toUpperCase().reverse()
                assert String.answer2() == 42
            """

            // the module should still be available
            assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.4' }
        '''
    }

    @Test
    void testExtensionModuleUsingGrabAndMap() {
        doInFork '''
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.4' }

            // find jar resource
            def jarURL = this.class.getResource("/jars")
            assert jarURL

            def resolver = "@GrabResolver('$jarURL')"

            assertScript resolver + """
                @Grab(value='module-test:module-test:1.4', changing='true')
                import org.codehaus.groovy.runtime.m12n.*

                def map = [:]
                assert 'foo'.taille() == 3
                assert map.taille() == 0
            """
        '''
    }

    @Test // GROOVY-7225
    void testExtensionModuleUsingGrabAndClosure() {
        doInFork '''
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.4' }

            // find jar resource
            def jarURL = this.class.getResource("/jars")
            assert jarURL

            assertScript """
                @GrabResolver('$jarURL')
                @Grab(value='module-test:module-test:1.4', changing='true')
                import org.codehaus.groovy.runtime.m12n.*

                assert 'test'.groovy7225() == 'test: ok'
                assert {->}.groovy7225() == '{"field":"value"}'
            """
        '''
    }

    /**
     * Just to make sure the custom override of {@code #compareTo} is possible and works.
     * @see TestLocalDateTimeExtension
     */
    @Test
    void testOverrideLocalDateTimeCompareTo() {
        doInFork '''
            def d1 = java.time.LocalDateTime.now()
            def d2 = java.time.LocalDate.now().plusDays(42)
            def d3 = java.time.LocalDate.now().minusDays(42)

            assert d1 < d2
            assert d1 > d3
        '''
    }
}
