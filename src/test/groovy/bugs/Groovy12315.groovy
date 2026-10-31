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
 * A final field with a dynamic declared type holds one value for its lifetime,
 * so a read of the field itself within the declaring class has the type of
 * that value, as a local variable does. A read through the property getter,
 * which a subclass may override, keeps the declared type.
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
    void testFinalFieldOfAnotherInstanceOfTheClass() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                int foo(Foo other) { other.bar.length() }
            }
            assert new Foo().foo(new Foo()) == 3
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
    void testStaticFieldAssignedByClassName() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                static final bar
                static { Foo.bar = 'abc' }
                static int n() { Foo.bar.length() }
            }
            assert Foo.n() == 3
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
    void testOverloadIsChosenByTheFlowType() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                static String m(Object o) { 'Object' }
                static String m(String s) { 'String' }
                String test() { m(bar) }
            }
            assert new Foo().test() == 'String'
        '''
    }

    // the same type written twice keeps that type
    @Test
    void testConstructorAssignsSameTypeAsInitializer() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'ab'
                Foo() { bar = 'abcd' }
                int n() { bar.length() }
            }
            assert new Foo().n() == 4
        '''
    }

    @Test
    void testElvisAssignmentKeepsString() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'ab'
                Foo() { bar ?= 'abcd' }
                int n() { bar.length() }
            }
            assert new Foo().n() == 2
        '''
    }

    @Test
    void testAbstractMethodDoesNotBlockFlow() {
        assertScript '''
            @groovy.transform.CompileStatic
            abstract class Foo {
                final bar = 'abc'
                abstract void m()
                int n() { bar.length() }
            }
            assert new Foo() { void m() {} }.n() == 3
        '''
    }

    @Test
    void testWritesOfOtherFieldsAndPropertiesDoNotMatter() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Base {
                String bar = 'x'
            }
            @groovy.transform.CompileStatic
            class Sub extends Base {
                final baz = 'abc'
                final String typed = 't'
                def plain = 1
                private String hidden = ''
                String getProp() { hidden }
                void setProp(String s) { hidden = s }
                Sub() {
                    super.bar = 'y'
                    typed = 'u'
                    plain = 2
                    prop = 'z'
                }
                void setOther(Sub other) { other.plain = 3 }
                int n() { baz.length() }
            }
            def s = new Sub()
            assert s.bar == 'y' && s.typed == 'u' && s.plain == 2 && s.prop == 'z'
            assert s.n() == 3
        '''
    }

    //--------------------------------------------------------------------------
    // reads that do not use the flow type

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

    // a closure or lambda reads a property through its getter, which a subclass may override
    @Test
    void testPropertyReadFromClosureIsNotFlowTyped() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                int foo() { ({ -> bar.length() })() }
            }
        '''
        assert err.message.contains('length')

        err = shouldFail '''
            import java.util.function.IntSupplier
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                int foo() { IntSupplier s = () -> bar.length(); s.getAsInt() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testOverriddenGetterIsNotBypassed() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                static String m(Object o) { 'Object:' + o }
                static String m(String s) { 'String:' + s }
                String direct()  { m(bar) }
                String closure() { ({ -> m(bar) })() }
                class Inner { String inner() { m(bar) } }
                String inner()   { new Inner().inner() }
            }
            @groovy.transform.CompileStatic
            class Sub extends Foo {
                def getBar() { 42 }
            }
            def sub = new Sub()
            assert sub.direct()  == 'String:abc'
            assert sub.closure() == 'Object:42'
            assert sub.inner()   == 'Object:42'
        '''
    }

    // no subclass can override the generated getter of a final class
    @Test
    void testPropertyReadFromClosureInFinalClassIsFlowTyped() {
        assertScript '''
            import java.util.function.IntSupplier
            @groovy.transform.CompileStatic
            final class Foo {
                final bar = 'abc'
                final sb = new StringBuilder()
                int closure() { ({ -> bar.length() })() }
                int lambda()  { IntSupplier s = () -> bar.length(); s.getAsInt() }
                Foo add(String s) { tap { sb.append(s) } }
            }
            def foo = new Foo()
            assert foo.closure() == 3
            assert foo.lambda() == 3
            assert foo.add('x').add('y').sb.toString() == 'xy'
        '''
    }

    // a hand-written getter of a final class is not the field
    @Test
    void testPropertyWithGetterReadFromClosureInFinalClassIsNotFlowTyped() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            final class Foo {
                final bar = 'abc'
                def getBar() { 42 }
                int foo() { ({ -> bar.length() })() }
            }
        '''
        assert err.message.contains('length')
    }

    // a private field is not a property: a closure reads the field itself
    @Test
    void testPrivateFieldReadFromClosureIsFlowTyped() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                private final bar = 'abc'
                int foo() { ({ -> bar.length() })() }
            }
            assert new Foo().foo() == 3
        '''
    }

    // the getter of a static property cannot be overridden
    @Test
    void testStaticPropertyReadFromClosureIsFlowTyped() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                static final bar = 'abc'
                static int foo() { ({ -> bar.length() })() }
            }
            assert Foo.foo() == 3
        '''
    }

    @Test
    void testInnerClassReadIsNotFlowTyped() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                class Inner {
                    int n() { bar.length() }
                }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testAnonymousClassReadIsNotFlowTyped() {
        def err = shouldFail '''
            class Foo {
                final bar = 'abc'
                @groovy.transform.CompileStatic
                Object make() {
                    return new Object() {
                        int n() { bar.length() }
                    }
                }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testSubclassReadIsNotFlowTyped() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
            }
            @groovy.transform.CompileStatic
            class Sub extends Foo {
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testAnotherClassReadIsNotFlowTyped() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
            }
            @groovy.transform.CompileStatic
            class Baz {
                static int n(Foo f) { f.bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testSpreadReadIsNotFlowTyped() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'ab'
            }
            @groovy.transform.CompileStatic
            class Baz {
                static List n(List<Foo> foos) { foos*.bar }
                static def one(Foo f) { f*.bar }
            }
            assert Baz.n([new Foo()]) == ['ab']
            assert Baz.one(new Foo()) == ['ab']
        '''
    }

    //--------------------------------------------------------------------------
    // values of no one type

    @Test
    void testConstructorsWithUnrelatedTypesDoNotFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                Foo(String s) { bar = s }
                Foo(int i) { bar = i }
                Comparable foo() { bar }
            }
        '''
        assert err.message.contains('Comparable')
    }

    @Test
    void testOverloadsAreNotAmbiguousForUnrelatedTypes() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                Foo(String s) { bar = s }
                Foo(Integer i) { bar = i }
                static String m(Object o) { 'Object' }
                static String m(Comparable c) { 'Comparable' }
                static String m(Serializable s) { 'Serializable' }
                String test() { m(bar) }
            }
            assert new Foo(1).test() == 'Object'
        '''
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

    // an untyped write blocks the flow type; a later write must not revive it
    @Test
    void testBlockedFlowTypeStaysBlocked() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                Foo(Object o) { bar = o }
                Foo(String s) { bar = s }
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testUnrelatedInterfacesDoNotFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            interface A { int onlyA() }
            @groovy.transform.CompileStatic
            interface B { int onlyB() }
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                Foo(A a) { bar = a }
                Foo(B b) { bar = b }
                int n() { bar.onlyA() }
            }
        '''
        assert err.message.contains('onlyA')
    }

    @Test
    void testIntersectionCastInitializerDoesNotFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Both implements Runnable, Serializable {
                void run() {}
            }
            @groovy.transform.CompileStatic
            class Foo {
                final bar = (Runnable & Serializable) new Both()
                void n() { bar.run() }
            }
        '''
        assert err.message.contains('run')
    }

    @Test
    void testDynamicReturnDoesNotFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                def d(x) { return x }
                final bar = d('abc')
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testVoidInitializerDoesNotFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                static void v() {}
                final bar = v()
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('length') || err.message.contains('void')
    }

    @Test
    void testCompoundAssignmentBlocksFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 40
                Foo() { bar += 2 }
                int n() { bar + 0 }
            }
        '''
        assert err.message.contains('plus')
    }

    @Test
    void testPrefixAndPostfixBlockFlow() {
        for (String increment : ['++bar', 'bar++']) {
            def err = shouldFail """
                @groovy.transform.CompileStatic
                class Foo {
                    final bar = 1
                    Foo() { $increment }
                    int n() { bar + 0 }
                }
            """
            assert err.message.contains('plus') : err.message
        }
    }

    @Test
    void testTupleAssignmentBlocksFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                Foo() {
                    def other = 0
                    (bar, other) = ['zzzz', 2]
                }
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('length')
    }

    // a closure is not a constructor, so the write is rejected
    @Test
    void testClosureAssignmentOutsideConstructorIsRejected() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar
                Foo() {
                    def assign = { bar = 'abc' }
                    assign()
                }
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('final')
    }

    @Test
    void testMethodAssignmentIsRejected() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                void bad() { bar = 'nope' }
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('read-only') || err.message.contains('final')
    }
}
