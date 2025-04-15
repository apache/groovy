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

import static org.codehaus.groovy.runtime.m12n.ExtensionModuleHelperForTests.doInFork

/**
 * Unit tests for static type checking : extension methods.
 */
class STCExtensionMethodsTest extends StaticTypeCheckingTestCase {

    /**
     * @see org.codehaus.groovy.runtime.m12n.TestStaticStringExtension
     */
    void testStaticExtensionMethod() {
        assertScript '''
            assert String.answer() == 42
        '''
    }

    /**
     * @see org.codehaus.groovy.runtime.m12n.TestStringExtension
     */
    void testNonStaticExtensionMethod() {
        assertScript '''
            def str = 'This is a string'
            assert str.reverseToUpperCase() == str.toUpperCase().reverse()
        '''
    }

    /**
     * @see org.codehaus.groovy.runtime.m12n.TestStringExtension
     * @see org.codehaus.groovy.runtime.m12n.TestStaticStringExtension
     */
    void testShouldFindExtensionMethodWithGrab() {
        doInFork 'groovy.transform.stc.StaticTypeCheckingTestCase', '''
            def impl = new MetaClassImpl(String)
            impl.initialize()
            String.metaClass = impl
            try {
                ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
                // ensure that the module isn't loaded
                assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.4' }

                def jarURL = this.class.getResource('/jars')
                assert jarURL

                assertScript """@GrabResolver(name='local',root='$jarURL')
                    @Grab('module-test:module-test:1.4;changing=true')
                    import org.codehaus.groovy.runtime.m12n.*

                    // the following methods are added by the Grab test module
                    def str = 'This is a string'
                    assert str.reverseToUpperCase2() == str.toUpperCase().reverse()
                    // a static method added to String thanks to a @Grab extension
                    assert String.answer2() == 42
                """
            } finally {
                String.metaClass = null
            }
        '''
    }

    /**
     * GROOVY-7953
     *
     * @see org.codehaus.groovy.runtime.m12n.TestPrimitiveWrapperExtension
     */
    void testExtensionPropertyWithPrimitiveReceiver() {
        assertScript '''
            assert 4.even
            assert 4.isEven()
            assert !5.isEven()
        '''
    }

    /**
     * GROOVY-6496
     *
     * @see org.codehaus.groovy.runtime.m12n.Groovy6496Extension
     */
    void testExtensionMethodWithGenericsAndPrimitiveReceiver() {
        assertScript '''
            assert 2d.groovy6496(2d) == 2d
        '''
    }

    //--------------------------------------------------------------------------

    static class Groovy8788 {
        static class A {
            def m1(Object o) {1}
            def m2(String s) {1}
            def m4(String s) {4}
            def m5(String s) {4}
            def m6(String s) {4}
        }
        static class B extends A {
            def m1(String s) {2}
            def m2(Object o) {2}
        }
        static class C extends B {
        }

        static m3(A self, String s) {1}
        static m3(B self, Object o) {2}
        static m3(B self, String s) {3}

        static m4(A self, String s) {1}
        static m4(B self, Object o) {2}

        static m5(A self, String s) {1}
        static m5(B self, Object o) {2}

        static m6(B self, Object o) {2}
    }

    // GROOVY-8788
    void testMethodSelection1() {
        assertScript """import ${Groovy8788.name}.*
            def a = new A()
            assert a.m1(new Object()) == 1
            assert a.m1(new String()) == 1
            def b = new B()
            assert b.m1(new Object()) == 1
            assert b.m1(new String()) == 2
        """
    }

    // GROOVY-8788
    void testMethodSelection2() {
        assertScript """import ${Groovy8788.name}.*
            def a = new A()
            assert a.m2(new String()) == 1
            def b = new B()
            assert b.m2(new Object()) == 2
            assert b.m2(new String()) == 1
        """

        shouldFailWithMessages """import ${Groovy8788.name}.*
            def a = new A()
            a.m2(new Object())
        """,
        'Cannot find matching method','A#m2(java.lang.Object)'
    }

    // GROOVY-8788
    void testMethodSelection3() {
        assertScript """import ${Groovy8788.name}.*
            def a = new A()
            assert a.m3(new String()) == 1
            def b = new B()
            assert b.m3(new Object()) == 2
            assert b.m3(new String()) == 3
        """
    }

    // GROOVY-8788
    void testMethodSelection4() {
        assertScript """import ${Groovy8788.name}.*
            def a = new A()
            assert a.m4(new String()) == 1
            def b = new B()
            assert b.m4(new Object()) == 2
            assert b.m4(new String()) == 1
        """
    }

    // GROOVY-8788
    void testMethodSelection5() {
        assertScript """import ${Groovy8788.name}.*
            def a = new A()
            assert a.m5(new String()) == 1
            def b = new B()
            assert b.m5(new Object()) == 2
            assert b.m5(new String()) == 1
            def c = new C()
            assert c.m5(new Object()) == 2
            assert c.m5(new String()) == 1
        """
    }

    // GROOVY-8788
    void testMethodSelection6() {
        assertScript """import ${Groovy8788.name}.*
            def a = new A()
            assert a.m6(new String()) == 4
            def b = new B()
            assert b.m6(new Object()) == 2
            assert b.m6(new String()) == 4
            def c = new C()
            assert c.m6(new Object()) == 2
            assert c.m6(new String()) == 4
        """
    }
}
