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
package gls.generics

import gls.CompilableTestSupport
import org.codehaus.groovy.control.MultipleCompilationErrorsException

final class GenericsUsageTest extends CompilableTestSupport {

    void testInvalidParameterUsage() {
        shouldNotCompile """
            abstract class B<T> implements Map<T>{}
        """
        shouldNotCompile """
            class A<T,V> extends ArrayList<T,V>{}
        """
        shouldNotCompile """
            class A<T extends Number> {}
            class B<T> extends A<T>{}
        """
        shouldNotCompile """
            class B<T> extends ArrayList<?>{}
        """
    }

    void testCovariantReturn() {
        shouldNotCompile '''
            class A<T> {
                T foo(T t) { 1 }
            }

            class B extends A<Long> {
                String foo(Long l) { '2' }
            }
        '''

        // GROOVY-6977
        assertScript '''
            class A {
              public <R> List<R> foo() {
                List<R> list = new ArrayList<R>() {
                    // ...
                }
                // ...
                list
              }
            }

            def longList = new A().<Long>foo()
            assert longList != null
            assert longList.empty
        '''

        assertScript '''
            class A<T> {
                T foo(T t) {1}
            }

            class B extends A<Long> {
                Long foo(Long l) { 2 }
            }

            def b = new B()
            try {
                b.foo(new Object())
                assert false
            } catch (ClassCastException cce) {
                assert true
            }
            assert b.foo((Long) 1) == 2
        '''
    }

    void testCovariantReturnWithInterface() {
        assertScript """
            import java.util.concurrent.*

            class CallableTask implements Callable<String> {
                String call() { "x" }
            }

            def task = new CallableTask()
            assert task.call() == "x"
        """
    }

    void testCovariantReturnWithEmptyAbstractClassesInBetween() {
        assertScript """
        import java.util.concurrent.*;

        abstract class AbstractCallableTask<T> implements Callable<T> { }
        abstract class SubclassCallableTask<T> extends AbstractCallableTask<T> { }
        class CallableTask extends SubclassCallableTask<String> {
            String call() { return "x"; }
        }
        assert "x" == new CallableTask().call();
      """
    }

    void testGenericsDiamondShortcutSimple() {
        assertScript """
            List<List<String>> list1 = new ArrayList<>()
            assert list1.size() == 0
        """
    }

    void testGenericsDiamondShortcutComplex() {
        assertScript """
            List<List<List<List<List<String>>>>> list2 = new ArrayList<>()
            assert list2.size() == 0
        """
    }

    void testGenericsDiamondShortcutMethodCall() {
        assertScript """
            def method(List<List<String>> list3) {
              list3.size()
            }

            assert method(new ArrayList<>()) == 0
        """
    }

    void testGenericsDiamondShortcutIllegalPosition() {
        shouldFailCompilationWithAnyMessage '''
            List<> list4 = []
        ''', ['unexpected token: <', 'Unexpected input: \'List<>\'']
    }

    void testGenericsInAsType() {
        // this is to ensure no regression to GROOVY-2725 will happen
        // "as ThreadLocal<Integer>\n" did not compile because the nls
        // was swallowed and could not be used to end the expression
        assertScript """
            import java.util.concurrent.atomic.AtomicInteger

            class ThreadId {
                private static final AtomicInteger nextId = new AtomicInteger(0)
                private static final ThreadLocal<Integer> threadId = [
                    initialValue: { return nextId.getAndIncrement() }
                ] as ThreadLocal<Integer>

                static int get() {
                    println "Thread ID: " + threadId.get()
                    return threadId.get()
                }

            }
            // we do not actually want to execute something, just
            // ensure this compiles, so we do a dummy command here
            assert ThreadId != null
        """
    }

    void testCompilationWithMissingClosingBracketsInGenerics() {
        shouldFailCompilationWithMessage """
            def list1 = new ArrayList<Integer()
        """, "Unexpected input: '('"

        shouldFailCompilationWithMessage """
            List<Integer list2 = new ArrayList<Integer>()
        """, "Unexpected input: 'List<Integer'"

        shouldFailCompilationWithMessage """
            def c = []
            for (Iterator<String i = c.iterator(); i.hasNext(); ) { }
        """, "Unexpected input: 'Iterator<String i'"

        shouldFailCompilationWithMessage """
            def m(Class<Integer someParam) {}
        """, "Unexpected input: 'Class<Integer someParam'"

        shouldFailCompilationWithMessage """
            abstract class ArrayList1<E extends AbstractList<E> implements List<E> {}
        """, "Unexpected input: 'implements'"

        shouldFailCompilationWithMessage """
            abstract class ArrayList2<E> extends AbstractList<E implements List<E> {}
        """, "Unexpected input: '<'"

        shouldFailCompilationWithMessage """
            abstract class ArrayList3<E> extends AbstractList<E> implements List<E {}
        """, "Unexpected input: '<'"

        shouldFailCompilationWithMessage """
            def List<List<Integer> history = new ArrayList<List<Integer>>()
        """, "Unexpected input: 'def List<List<Integer> history'"

        shouldFailCompilationWithMessage """
            def List<List<Integer>> history = new ArrayList<List<Integer>()
        """, "Unexpected input: '('"
    }

    // GROOVY-3975
    void testGenericsForClosureParameters() {
        def cl = { List<String> s -> }

        String type = cl.getClass().getMethod('call', List).genericParameterTypes[0]
        assert type.contains('java.util.List<java.lang.String>')

        type = cl.getClass().getMethod('doCall', List).genericParameterTypes[0]
        assert type.contains('java.util.List<java.lang.String>')
    }

    // GROOVY-4974
    void testBoundedGenericsWithInheritance() {
        assertScript '''
            class TestGenerics {
                static interface Z {}
                static class V implements Z {}
                static class W extends V {}
                static interface X extends Z {}
                static class Y implements X {}

                static class A <T extends Z> { def a(T t) { this } }
                static class B extends A<W> {}
                static class C extends A<V> {}
                static class D extends A<Y> {}

                static void main(String[] args) {
                    assert new B().a(new W()) instanceof B
                    assert new C().a(new W()) instanceof C
                    assert new D().a(new Y()) instanceof D
                }
            }
        '''
    }

    // GROOVY-3731, GROOVY-7865, GROOVY-10033
    void testFriendlyErrorMessageForGenericsErrors() {
        // superclass and interfaces
        shouldFailCompilationWithMessages '''
            class C extends ArrayList<> { }
        ''', ["Unexpected input: '<'"]
        shouldFailCompilationWithMessages '''
            class C extends ArrayList<? extends Number> { }
        ''', ['A supertype may not specify a wildcard type']
        shouldFailCompilationWithMessages '''
            class C extends ArrayList<String, String> { }
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            class C extends HashMap<String> { }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class C implements Map<> { }
        ''', ["Unexpected input: '<'"]
        shouldFailCompilationWithMessages '''
            class MyMap implements Map<String> { }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class C implements List<String, String> { }
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']

        // constructor call
        assertScript '''
            List<String> list = new LinkedList<>()
        '''
        shouldFailCompilationWithMessage '''
            new LinkedList<>() { }
        ''', 'Cannot use diamond <> with anonymous inner classes'
        shouldFailCompilationWithMessages '''
            new LinkedList<Integer, String>()
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            new LinkedList<String, String>()
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            new LinkedList<String, String>() { }
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            new Date<Calendar>()
        ''', ['(supplied with 1 type parameter)', 'which takes no parameters']

        // constructor declaration
        shouldFailCompilationWithMessages '''
            class C { C(Map<String> m) { } }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class C { C(Closure<String,Number> c) { } }
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']

        // method declaration
        shouldFailCompilationWithMessages '''
            def method(Map<String> map) { }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            Map<String> method() { }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            def method(Map<String, Map<String>> map) { }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            def method(Map<String, Map<String>> map) { }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']

        // field declaration
        shouldFailCompilationWithMessages '''
            class C { Map<String> map }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class C { Map<String, Map<String>> map }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']

        // variable declaration
        shouldFailCompilationWithMessages '''
            def method() { Map<String> map }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            def method() { Map<String, Map<String>> map }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            def (Map<String,String> one, Map<String> two) = [ [:], [:] ]
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            Map<String>[][] array = new Map[0][0]
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            Map<String,String>[][] array = new Map<String>[0][]
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class C { { Map<String> m = null } }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            class C { static { Map<String> m = null } }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            @groovy.transform.ASTTest(value={
                Map<String> m = null
            })
            class C { }
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']

        // casting and coercion
        shouldFailCompilationWithMessages '''
            def map = (Map<String>) null
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            def map = null as Map<String>
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
    }

    // GROOVY-5441
    void testCompilationErrorForMismatchedGenericsWithQualifiedTypes() {
        shouldFailCompilationWithMessages '''
            groovy.lang.Tuple2<Object> tuple
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            java.util.List<Object,Object> list
        ''', ['(supplied with 2 type parameters)', 'which takes 1 parameter']
        shouldFailCompilationWithMessages '''
            java.util.Map<Object,Object,Object> map
        ''', ['(supplied with 3 type parameters)', 'which takes 2 parameters']
        shouldFailCompilationWithMessages '''
            def (java.util.Map<Object> x, java.util.List<Object,Object> y) = [null,null]
        ''', ['(supplied with 1 type parameter)', 'which takes 2 parameters',
              '(supplied with 2 type parameters)', 'which takes 1 parameter']
    }

    // GROOVY-8990
    void testCompilationErrorForMismatchedGenericsWithMultipleBounds() {
        shouldFailCompilationWithMessages '''
            class C1<T> {}

            interface I1 {}
            class C2 implements I1 {}

            class C3<T extends I1> {}

            class C4 extends C1<String> {} // String matches T

            class C5 extends C3<String> {} // String not an I1

            class C6 extends C3<C2> {} // C2 is an I1

            class C7<T extends Number & I1> {}

            class C8 extends C7<C2> {} // C2 not a Number
            class C9 extends C7<Integer> {} // Integer not an I1

            class C10 extends Number implements I1 {}
            class C11 extends C7<C10> {} // C10 is a Number and implements I1

            interface I2<T extends Number> {}
            class C12 implements I2<String> {} // String not a Number
            class C13 implements I2<C10> {} // C10 is a Number
        ''', [
                'The type String is not a valid substitute for the bounded parameter <T extends I1>',
                'The type C2 is not a valid substitute for the bounded parameter <T extends java.lang.Number & I1>',
                'The type Integer is not a valid substitute for the bounded parameter <T extends java.lang.Number & I1>',
                'The type String is not a valid substitute for the bounded parameter <T extends java.lang.Number>'
        ]
    }

    //--------------------------------------------------------------------------

    private void shouldFailCompilationWithDefaultMessage(scriptText) {
        shouldFailCompilationWithMessage scriptText, "Missing closing bracket '>' for generics types"
    }

    private void shouldFailCompilationWithMessage(scriptText, String errorMessage) {
        shouldFailCompilationWithMessages(scriptText, [errorMessage])
    }

    private void shouldFailCompilationWithMessages(scriptText, List<String> errorMessages) {
        try {
            assertScript scriptText
            fail("The script compilation should have failed as it contains generics errors, e.g. mis-matching generic brackets")
        } catch (MultipleCompilationErrorsException mcee) {
            def text = mcee.toString()
            errorMessages.each {
                assert text.contains(it)
            }
        }
    }

    private void shouldFailCompilationWithAnyMessage(scriptText, List<String> errorMessages) {
        try {
            assertScript scriptText
            fail("The script compilation should have failed as it contains generics errors, e.g. mis-matching generic brackets")
        } catch (MultipleCompilationErrorsException mcee) {
            def text = mcee.toString()

            for (errorMessage in errorMessages) {
                if (text.contains(errorMessage)) {
                    return
                }
            }

            assert false, text + " can not match any expected error message: " + errorMessages
        }
    }
}
