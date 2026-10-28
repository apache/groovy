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

    // Same pinned type written twice keeps that type (no least-upper-bound).
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

    // Two interfaces with nothing in common lub to Object, which is not a flow type.
    // An untyped write blocks the flow type; a later write must not revive it.
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

    // A closure is not a constructor, so the final-field write is rejected.
    // Type checking still walks the closure while recording initializer flow.
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
    void testSpreadReadDoesNotNeedANarrowElementType() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'ab'
            }
            @groovy.transform.CompileStatic
            class Baz {
                static List n(List<Foo> foos) { foos*.bar }
            }
            assert Baz.n([new Foo()]) == ['ab']
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
    void testInnerClassReadsOuterFinalField() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                class Inner {
                    int n() { bar.length() }
                }
            }
            assert new Foo().new Inner().n() == 3
        '''
    }

    @Test
    void testSuperPropertyAssignmentDoesNotBlockSubclassField() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Base {
                String bar = 'x'
            }
            @groovy.transform.CompileStatic
            class Sub extends Base {
                final baz = 'abc'
                Sub() { super.bar = 'y' }
                int n() { baz.length() }
            }
            def s = new Sub()
            assert s.bar == 'y'
            assert s.n() == 3
        '''
    }

    @Test
    void testOtherInstanceWriteIsNotAnOwnReceiver() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                String note = ''
                void setOther(Foo other) { other.note = 'zzzz' }
                int n() { bar.length() }
            }
            def a = new Foo()
            def b = new Foo()
            a.setOther(b)
            assert b.note == 'zzzz'
            assert a.n() == 3
        '''
    }

    @Test
    void testDynamicWriteOnAnotherInstanceDoesNotBlock() {
        // A dynamic name on another receiver is not an own-field write, so the
        // final field keeps its flow type. The write itself is not a static property.
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                String note = ''
                void setNote(Foo other) {
                    def p = 'note'
                    other."$p" = 'zzzz'
                }
                int n() { bar.length() }
            }
            new Foo().n()
        '''
        assert err.message.contains('property')
        assert !err.message.contains('length')
    }

    @Test
    void testOwnWritesOfNonFlowFieldsAreIgnored() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                final String typed = 't'
                def plain = 1
                Foo() {
                    typed = 'u'
                    plain = 2
                }
                int n() { bar.length() }
            }
            def f = new Foo()
            assert f.typed == 'u'
            assert f.plain == 2
            assert f.n() == 3
        '''
    }

    @Test
    void testSpreadOfNonFinalField() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                String name = 'a'
            }
            @groovy.transform.CompileStatic
            class Baz {
                static List n(List<Foo> foos) { foos*.name }
            }
            assert Baz.n([new Foo()]) == ['a']
        '''
    }

    @Test
    void testForeignClassPropertyWrite() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                void m() { System.err = System.err }
                int n() { bar.length() }
            }
            assert new Foo().n() == 3
        '''
    }

    @Test
    void testIntersectionCastInitializerIsPinned() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Both implements Runnable, java.io.Serializable {
                void run() {}
            }
            @groovy.transform.CompileStatic
            class Foo {
                final bar = (Runnable & java.io.Serializable) new Both()
                void n() { bar.run() }
            }
            new Foo().n()
        '''
    }

    @Test
    void testPropertyNodeAssignmentDoesNotBlockFinalField() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                private String hidden = ''
                String getProp() { hidden }
                void setProp(String s) { hidden = s }
                Foo() { prop = 'z' }
                int n() { bar.length() }
            }
            def f = new Foo()
            assert f.prop == 'z'
            assert f.n() == 3
        '''
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
    void testReaderBeforeFieldClassIsNotFlowTyped() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Baz {
                static int n(Foo f) { f.bar.length() }
            }
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
            }
        '''
        assert err.message.contains('length')
    }

    @Test
    void testMethodAssignmentBlocksFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                void bad() { bar = 'nope' }
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('length') || err.message.contains('read-only') || err.message.contains('final')
    }

    @Test
    void testCompoundAssignmentBlocksFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 1
                Foo() { bar += 2 }
                int n() { bar + 0 }
            }
        '''
        assert err.message.contains('plus') || err.message.contains('final') || err.message.contains('read-only')
    }

    @Test
    void testPrefixAndPostfixBlockFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 1
                Foo() { ++bar }
                int n() { bar + 0 }
            }
        '''
        assert err.message.contains('plus') || err.message.contains('final') || err.message.contains('read-only')

        err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 1
                Foo() { bar++ }
                int n() { bar + 0 }
            }
        '''
        assert err.message.contains('plus') || err.message.contains('final') || err.message.contains('read-only')
    }

    @Test
    void testDynamicPropertyWriteBlocksFlow() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'abc'
                final String typed = 't'
                def plain = 1
                Foo() {
                    def p = 'zzz'
                    this."$p" = 1
                    bar = 'abcd'
                }
                int n() { bar.length() }
            }
        '''
        assert err.message.contains('length') || err.message.contains('final') || err.message.contains('read-only')
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
        assert err.message.contains('length') || err.message.contains('final') || err.message.contains('read-only')
    }

    // Method-only @CompileStatic visits the anonymous class while the outer
    // class has started its constructors but has not finished the class visit.
    @Test
    void testAnonymousClassInsideMethodCheckedMember() {
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
            new Foo().make()
        '''
        assert err.message.contains('length')
    }

    @Test
    void testSpreadOnSingleReceiver() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                final bar = 'ab'
            }
            @groovy.transform.CompileStatic
            class Baz {
                static def n(Foo f) { f*.bar }
            }
            assert Baz.n(new Foo()) == ['ab']
        '''
    }

    @Test
    void testAnonymousClassReadsUnreadyForeignField() {
        def err = shouldFail '''
            class Other {
                final baz = 'xy'
                @groovy.transform.CompileStatic
                int seed() { baz.length() }
            }
            class Holder {
                @groovy.transform.CompileStatic
                int n() {
                    def o = new Object() {
                        int m(Other x) { x.baz.length() }
                    }
                    return o.m(new Other())
                }
            }
        '''
        assert err.message.contains('length')
    }
}
