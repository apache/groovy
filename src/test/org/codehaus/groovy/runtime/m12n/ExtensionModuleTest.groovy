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

/**
 * Unit tests for extension methods loading.
 */
class ExtensionModuleTest extends GroovyTestCase {

    private static final URL moduleTest12JarURL
    private static final URL moduleTest107225JarURL

    static {
        moduleTest12JarURL = ExtensionModuleHelperForTests.createModuleTest12Jar()
        moduleTest107225JarURL = ExtensionModuleHelperForTests.createModuleTest107225Jar()
    }

    void testThatModuleHasBeenLoaded() {
        ExtensionModuleHelperForTests.doInFork '''
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

    void testThatModuleCanBeLoadedWithGrab() {
        ExtensionModuleHelperForTests.doInFork """
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

            def resolver = "@GrabResolver('$moduleTest12JarURL')"

            assertScript resolver + '''
            @Grab(value='module-test:module-test:1.2-test', changing='true')
            import org.codehaus.groovy.runtime.m12n.*

            // ensure that the module is now loaded
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

            // the following methods are added by the 'Test module for Grab' module
            def str = 'This is a string'
            assert str.reverseToUpperCase2() == str.toUpperCase().reverse()
            assert String.answer2() == 42
            '''

            // the module should still be available
            assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }
        """
    }

    void testExtensionModuleUsingGrabAndMap() {
        ExtensionModuleHelperForTests.doInFork """
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

            def resolver = "@GrabResolver('$moduleTest12JarURL')"

            assertScript resolver + '''
            @Grab(value='module-test:module-test:1.2-test', changing='true')
            import org.codehaus.groovy.runtime.m12n.*

            def map = [:]
            assert 'foo'.taille() == 3
            assert map.taille() == 0
            '''
        """
    }

    /**
     * Test case that reproduces GROOVY-7225.
     */
    void testExtensionModuleUsingGrabAndClosure() {
        ExtensionModuleHelperForTests.doInFork """
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            // ensure that the module isn't loaded
            assert !registry.modules.any { it.name == 'Groovy Test 7225' && it.version == '1.0.7225-test' }

            assertScript '''
            @GrabResolver('$moduleTest107225JarURL')
            @Grab(value='module-test:module-test:1.0.7225-test', changing='true')
            import org.codehaus.groovy.runtime.m12n.*

            assert 'test'.groovy7225() == 'test: ok'
            assert {->}.groovy7225() == '{"field":"value"}'
            '''
        """
    }
}
