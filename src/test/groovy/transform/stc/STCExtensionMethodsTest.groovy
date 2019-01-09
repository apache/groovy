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
package groovy.transform.stc

import org.codehaus.groovy.runtime.m12n.ExtensionModuleHelperForTests

/**
 * Unit tests for static type checking : extension methods.
 */
class STCExtensionMethodsTest extends StaticTypeCheckingTestCase {

    void testShouldFindExtensionMethod() {
        assertScript '''
            // reverseToUpperCase is an extension method specific to unit tests
            def str = 'This is a string'
            assert str.reverseToUpperCase() == str.toUpperCase().reverse()
            assert String.answer() == 42
        '''
    }

    void testShouldFindExtensionMethodWithGrab() {
        ExtensionModuleHelperForTests.doInFork('groovy.transform.stc.StaticTypeCheckingTestCase', '''
        def impl = new MetaClassImpl(String)
        impl.initialize()
        String.metaClass = impl
        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
        // ensure that the module isn't loaded
        assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.3' }

        // find jar resource
        def jarURL = this.class.getResource("/jars")
        assert jarURL

        def resolver = "@GrabResolver(name='local',root='$jarURL')"

        assertScript resolver + """
        @Grab('module-test:module-test:1.4')
        import org.codehaus.groovy.runtime.m12n.*

        // the following methods are added by the Grab test module
        def str = 'This is a string'
        assert str.reverseToUpperCase2() == str.toUpperCase().reverse()
        // a static method added to String thanks to a @Grab extension
        assert String.answer2() == 42
        """
        ''')
    }

    void testExtensionMethodWithGenericsAndPrimitiveReceiver() {
        assertScript '''
            assert 2d.groovy6496(2d) == 2d
    '''
    }

    //GROOVY-7953
    void testExtensionPropertyWithPrimitiveReceiver() {
        assertScript '''
            assert 4.even
        '''
    }
}