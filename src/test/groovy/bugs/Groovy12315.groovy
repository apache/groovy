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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-12315: a final field with a dynamic declared type still has one
 * value for its lifetime, so flow typing should know that value's type the
 * way it already does for a local variable.
 */
final class Groovy12315 {

    @Test
    void testFinalFieldInitializer() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                def foo() {
                    println(bar.length())
                    return bar.length()
                }
            }
            assert new Foo().foo() == 3
        '''
    }

    @Test
    void testFinalFieldInitializerWithTypeChecked() {
        assertScript '''
            @groovy.transform.TypeChecked
            class Foo {
                final bar = 'abc'
                int foo() { bar.length() }
            }
            assert new Foo().foo() == 3
        '''
    }

    @Test
    void testStaticFinalFieldInitializer() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                static final bar = 'abc'
                static int foo() { bar.length() }
            }
            assert Foo.foo() == 3
        '''
    }

    @Test
    void testPrivateFinalFieldAndExplicitThis() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                private final bar = 'abc'
                int foo() { this.bar.length() }
            }
            assert new Foo().foo() == 3
        '''
    }

    @Test
    void testFinalFieldUsedFromClosure() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                int foo() { ({ -> bar.length() })() }
            }
            assert new Foo().foo() == 3
        '''
    }

    @Test
    void testFinalFieldFlowsToLaterField() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                final n = bar.length()
            }
            assert new Foo().n == 3
        '''
    }

    @Test
    void testFinalFieldAssignedInConstructor() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                Foo() { bar = 'abc' }
                int foo() { bar.length() }
            }
            assert new Foo().foo() == 3
        '''
    }

    @Test
    void testFinalFieldAssignedInEveryConstructor() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                Foo(String s) { bar = s }
                Foo(CharSequence s) { this.bar = s }
                int foo() { bar.length() }
            }
            assert new Foo('abc').foo() == 3
            assert new Foo(new StringBuilder('abcd')).foo() == 4
        '''
    }

    @Test
    void testFinalFieldAssignedInInstanceInitializer() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                { bar = 'abc' }
                int foo() { bar.length() }
            }
            assert new Foo().foo() == 3
        '''
    }

    @Test
    void testStaticFinalFieldAssignedInStaticInitializer() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                static final bar
                static { bar = 'abc' }
                static int foo() { bar.length() }
            }
            assert Foo.foo() == 3
        '''
    }

    @Test
    void testFinalListFieldKeepsComponentType() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = ['ab', 'c']
                String foo() { bar[0].toUpperCase() }
            }
            assert new Foo().foo() == 'AB'
        '''
    }

    @Test
    void testFinalNumericField() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 40
                int foo() { bar + 2 }
            }
            assert new Foo().foo() == 42
        '''
    }

    @Test
    void testFinalFieldReadFromAnotherClass() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
            }
            @groovy.transform.CompileStatic
            class Baz {
                static int n(Foo f) { f.bar.length() }
            }
            assert Baz.n(new Foo()) == 3
        '''
    }

    @Test
    void testSubclassReadsInheritedFinalField() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
            }
            @groovy.transform.CompileStatic
            class Sub extends Foo {
                int n() { bar.length() }
            }
            assert new Sub().n() == 3
        '''
    }

    @Test
    void testNonFinalFieldIsNotFlowTyped() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                def bar = 'abc'
                int foo() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testExplicitObjectTypeIsNotFlowTyped() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final Object bar = 'abc'
                int foo() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testExplicitTypedFinalFieldStillWorks() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final String bar = 'abc'
                int foo() { bar.length() }
            }
            assert new Foo().foo() == 3
        '''
    }

    @Test
    void testConstructorsWithUnrelatedTypesDoNotFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                Foo(String s) { bar = s }
                Foo(int i) { bar = i }
                int foo() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testInitializerWidenedByConstructorDoesNotFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                Foo() { bar = 1 }
                int foo() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testStaticInitializerWidensAwayFromString() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                static final bar = 'abc'
                static { bar = 1 }
                static int foo() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testInstanceInitializerWidensAwayFromString() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                { bar = 1 }
                int foo() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testNullInitializerDoesNotInventAType() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = null
                int foo() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }
}
