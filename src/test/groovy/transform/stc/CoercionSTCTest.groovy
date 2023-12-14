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

import groovy.test.NotYetImplemented
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Unit tests for static type checking : coercions.
 */
class CoercionSTCTest extends StaticTypeCheckingTestCase {

    @Override
    void configure() {
        config.addCompilationCustomizers(
            new ImportCustomizer().addStarImports('java.util.function')
        )
    }

    void testCoerceToArray() {
        assertScript '''
            try {
                throw new Exception()
            } catch (Throwable t) {
                def newTrace = []
                def clean = newTrace.toArray(newTrace as StackTraceElement[])
                // doing twice, because bug showed that the more you call the array coercion, the more the error gets stupid:
                // Cannot call java.util.List#toArray([Ljava.lang.Object;) with arguments [[Ljava.lang.StackTraceElement; -> [Ljava.lang.StackTraceElement;]
                // Cannot call java.util.List#toArray([[Ljava.lang.Object;) with arguments [[Ljava.lang.StackTraceElement; -> [Ljava.lang.StackTraceElement;]
                // Cannot call java.util.List#toArray([[[Ljava.lang.Object;) with arguments [[Ljava.lang.StackTraceElement; -> [Ljava.lang.StackTraceElement;]
                // ...
                clean = newTrace.toArray(newTrace as StackTraceElement[])
            }
        '''
    }

    // GROOVY-6802
    void testCoerceToBool1() {
        assertScript '''
            boolean b = [new Object()]
            assert b
        '''
        assertScript '''
            boolean b = [false]
            assert b
        '''
        assertScript '''
            boolean b = [true]
            assert b
        '''
        assertScript '''
            boolean b = ['x']
            assert b
        '''
        assertScript '''
            boolean b = [:]
            assert !b
        '''
        assertScript '''
            boolean b = []
            assert !b
        '''
    }

    void testCoerceToBool2() {
        assertScript '''
            Boolean b = [new Object()]
            assert b
        '''
        assertScript '''
            Boolean b = [false]
            assert b
        '''
        assertScript '''
            Boolean b = [true]
            assert b
        '''
        assertScript '''
            Boolean b = ['x']
            assert b
        '''
        assertScript '''
            Boolean b = [:]
            assert !b
        '''
        assertScript '''
            Boolean b = []
            assert !b
        '''
    }

    void testCoerceToClass() {
        assertScript '''
            Class c = 'java.lang.String'
            assert String.class
        '''
        shouldFailWithMessages '''
            Class c = []
        ''', 'No matching constructor found: java.lang.Class()'
        shouldFailWithMessages '''
            Class c = [:]
        ''', 'No matching constructor found: java.lang.Class(java.util.LinkedHashMap'
    }

    // GROOVY-6803
    void testCoerceToString() {
        assertScript '''
            String s = ['x']
            assert s == '[x]'
        '''
        assertScript '''
            String s = [:]
            assert s == '[:]' || s == '{}'
        '''
        assertScript '''
            String s = []
            assert s == '[]'
        '''
    }

    void testCoerceToFunctionalInterface1() {
        String sam = '@FunctionalInterface interface SAM { def foo() }'

        assertScript sam + '''
            def test(SAM a, SAM b) {
                '' + a.foo() + b.foo()
            }
            String result = test({'a'}, {'b'})
            assert result == 'ab'
        '''

        assertScript sam + '''
            def test(Object o, SAM a, SAM b) {
                '' + a.foo() + b.foo()
            }
            String result = test(new Object(), {'a'}, {'b'})
            assert result == 'ab'
        '''

        assertScript sam + '''
            def test(SAM a, SAM b) {
                '' + a.foo() + b.foo()
            }
            def test(Closure a, SAM b) {
                b.foo()
            }
            String result = test({'a'}, {'b'})
            assert result == 'b'
        '''

        assertScript sam + '''
            def test(SAM a, SAM b) {
                '' + a.foo() + b.foo()
            }
            def test(SAM a, Closure b) {
                a.foo()
            }
            String result = test({'a'}, {'b'})
            assert result == 'a'
        '''
    }

    // GROOVY-10254
    void testCoerceToFunctionalInterface2() {
        assertScript '''
            @FunctionalInterface
            interface SAM<T> { T get() }

            SAM<Integer> foo() {
                return { -> 42 }
            }

            def result = foo().get()
            assert result == 42
        '''
    }

    // GROOVY-9991
    void testCoerceToFunctionalInterface3() {
        assertScript '''
            Consumer<Integer>  c = { it }
            Supplier<Integer>  s = { 42 }
            Predicate<Integer> p = { true }
        '''

        assertScript '''
            Consumer<Integer>  c = { n -> }
            Supplier<Integer>  s = {   -> 42 }
            Predicate<Integer> p = { n -> 42 }
        '''

        assertScript '''
            def c = (Consumer<Integer>)  { n -> }
            def s = (Supplier<Integer>)  {   -> 42 }
            def p = (Predicate<Integer>) { n -> 42 }
        '''

        assertScript '''
            def c = { n ->    } as Consumer<Integer>
            def s = {   -> 42 } as Supplier<Integer>
            def p = { n -> 42 } as Predicate<Integer>
        '''
    }

    // GROOVY-10277
    @NotYetImplemented
    void testCoerceToFunctionalInterfaceX() {
        shouldFailWithMessages '''
            def s = (Supplier<Number>) { -> false }
        ''',
        'Cannot return value of type boolean for closure expecting java.lang.Number'

        shouldFailWithMessages '''
            def s = { -> false } as Supplier<Number>
        ''',
        'Cannot return value of type boolean for closure expecting java.lang.Number'

        shouldFailWithMessages '''
            def foo(Supplier<Number> s) { s.get() }
            def n = foo { -> false }
        ''',
        'Cannot return value of type boolean for closure expecting java.lang.Number'

        shouldFailWithMessages '''
            def s = (() -> [""]) as Supplier<Number>
        ''',
        'Cannot return value of type java.util.List <java.lang.String> for lambda expecting java.lang.Number'
    }

    void testCoerceToFunctionalInterface4() {
        assertScript '''
            interface I { int m() }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE).name == 'I'
            })
            I i = { 1 }
            assert i.m() == 1
            def x = (I) { 2 }
            assert x.m() == 2
        '''

        assertScript '''
            interface I { int m() }
            abstract class A implements I { }

            I i = { 1 }
            assert i.m() == 1
            A a = { 2 }
            assert a.m() == 2
        '''

        assertScript '''
            interface I { // non-functional, but every instance extends Object
                boolean equals(Object)
                int m()
            }
            I i = { 1 }
            assert i.m() == 1
        '''

        shouldFailWithMessages '''
            interface I {
                boolean equals(Object)
                int m()
            }
            abstract class A implements I { // no abstract methods
                int m() { 1 }
            }
            A a = { 2 }
        ''',
        'Cannot assign value of type groovy.lang.Closure <java.lang.Integer> to variable of type A'

        shouldFailWithMessages '''
            interface I { // no abstract methods; every instance extends Object
                String toString()
            }
            I i = { p -> "" }
        ''',
        'Cannot assign value of type groovy.lang.Closure <java.lang.String> to variable of type I'

        shouldFailWithMessages '''
            interface I { // no abstract methods; every instance extends Object
                String toString()
            }
            abstract class A implements I { }

            A a = { "" } // implicit parameter
        ''',
        'Cannot assign value of type groovy.lang.Closure <java.lang.String> to variable of type A'
    }

    // GROOVY-7927
    void testCoerceToFunctionalInterface5() {
        assertScript '''
            interface SAM<T,R> { R accept(T t); }
            SAM<Integer,Integer> s = { Integer n -> -n }
            assert s.accept(1) == -1
        '''
    }

    void testCoerceToFunctionalInterface6() {
        String sam = '@FunctionalInterface interface SAM { def foo() }'

        assertScript sam + '''
            class X {
                public SAM s
            }
            def x = new X()
            x.s = {1}
            assert x.s.foo() == 1
            x.@s = {2}
            assert x.s.foo() == 2
        '''

        assertScript sam + '''
            class X {
                SAM s
            }
            def x = new X(s: {3})
            assert x.s.foo() == 3
        '''
    }

    // GROOVY-11085
    void testCoerceToFunctionalInterface7() {
        for (type in ['','long','Long']) {
            assertScript """
                void setStrategy(Predicate<Long> tester) {
                    assert tester.test(1L)
                }
                strategy = { $type n -> n instanceof Long }
            """
        }
    }

    // GROOVY-7003
    void testCoerceToFunctionalInterface8() {
        assertScript '''import java.beans.*
            class C {
                static PropertyChangeListener listener = { PropertyChangeEvent event ->
                    result = "${event.oldValue} -> ${event.newValue}"
                }
                public static result
            }

            def event = new PropertyChangeEvent(new Object(), 'foo', 'bar', 'baz')
            C.getListener().propertyChange(event)
            assert C.result == 'bar -> baz'
        '''
    }

    // GROOVY-8045
    void ____CoerceToFunctionalInterface9() {
        assertScript '''
            def f(Supplier<Integer>... suppliers) {
                suppliers*.get().sum()
            }
            Object result = f({->1},{->2})
            assert result == 3
        '''
    }

    // GROOVY-8168
    @NotYetImplemented
    void testCoerceToFunctionalInterface10() {
        String sam = '''
            @FunctionalInterface
            interface Operation {
                double calculate(int i)
            }
        '''

        assertScript sam + '''
            Operation operation = { return 1.0d }
            def result = operation.calculate(2)
            assert result == 1.0d
        '''

        shouldFailWithMessages sam + '''
            Operation operation = { return 1.0; }
        ''',
        'Cannot return value of type java.math.BigDecimal for closure expecting double'
    }

    // GROOVY-8427
    void testCoerceToFunctionalInterface11() {
        assertScript '''
            def <T> void m(T a, Consumer<T> c) {
                c.accept(a)
            }

            def c = { ->
                int x = 0
                m('') {
                    print 'void return'
                }
            }
            c.call()
        '''
    }

    // GROOVY-8499, GROOVY-10963
    void testCoerceToFunctionalInterface1x() {
        shouldFailWithMessages '''
            ['ab'.chars,'12'.chars].combinations().stream().map((l,n) -> "$l$n")
        ''',
        'Wrong number of parameters for method target: apply(java.lang.Object)'

        shouldFailWithMessages '''
            void test(Consumer c) {}
            test(() -> {})
        ''',
        'Wrong number of parameters for method target: accept(java.lang.Object)'

        shouldFailWithMessages '''import java.util.concurrent.Callable
            void test(Callable c) {}
            test(x -> { x })
        ''',
        'Wrong number of parameters for method target: call()'

        shouldFailWithMessages '''
            void test(Runnable r) {}
            test(x -> { x })
        ''',
        'Wrong number of parameters for method target: run()'
    }

    // GROOVY-9079
    void testCoerceToFunctionalInterface12() {
        assertScript '''import java.util.concurrent.Callable
            Callable<String> c = { -> return 'foo' }
            assert c() == 'foo'
        '''
    }

    // GROOVY-10128, GROOVY-10306
    @NotYetImplemented
    void testCoerceToFunctionalInterface13() {
        assertScript '''
            Function<String, Number> x = { s ->
                long n = 1
                return n
            }
            assert x.apply('') == 1L
        '''

        assertScript '''
            class C {
                byte p = 1
                void m() {
                    byte v = 2
                    Supplier<Number> one = { -> p }
                    Supplier<Number> two = { -> v }
                    assert one.get() == (byte)1
                    assert two.get() == (byte)2
                }
            }
            new C().m()
        '''
    }

    // GROOVY-10792
    void testCoerceToFunctionalInterface14() {
        assertScript '''
            @Grab('org.awaitility:awaitility-groovy:4.2.0')
            import static org.awaitility.Awaitility.await

            List<String> strings = ['x']
            await().until { -> strings }
        '''
    }

    // GROOVY-10905
    @NotYetImplemented
    void testCoerceToFunctionalInterface15() {
        assertScript '''
            def method(IntUnaryOperator unary) { '1a' }
            def method(IntBinaryOperator binary) { '1b' }

            assert method{ x -> } == '1a'
            assert method{ x, y -> } == '1b'
            assert method{ } == '1a'
        '''

        assertScript '''
            def method(IntSupplier supplier) { '2a' }
            def method(IntBinaryOperator binary) { '2b' }

            assert method{ -> } == '2a'
            assert method{ x, y -> } == '2b'
            assert method{ } == '2a'
        '''
    }

    // GROOVY-11051
    @NotYetImplemented
    void testCoerceToFunctionalInterface16() {
        assertScript '''import java.util.concurrent.atomic.AtomicReference
            def opt = new AtomicReference<Object>(null)
                .stream().filter { it.get() }.findAny()
            assert opt.isEmpty()
        '''
    }

    // GROOVY-11079
    void testCoerceToFunctionalInterface17() {
        assertScript '''
            @Grab('io.vavr:vavr:0.10.4')
            import io.vavr.control.Try
            import io.vavr.Tuple2
            Map<String,Object> getSpec() { [:] }
            Try<Tuple2<String,Object>> tuple() {
                Try.success(new Tuple2("",null))
            }
            void test(List list) {
                list.forEach { item ->
                    def map = getSpec()
                    tuple().onSuccess {
                        map.foo = it._1
                    }
                }
            }
            test( [null] )
        '''
    }

    // GROOVY-11083
    void testCoerceToFunctionalInterface18() {
        shouldFailWithMessages '''
            void setFoo(Consumer<Number> c) {}
            void test(Date d) {
                foo = { n = d -> }
            }
        ''',
        'Cannot assign value of type java.util.Date to variable of type java.lang.Number'
    }

    // GROOVY-11085
    void testCoerceToFunctionalInterface19() {
        for (type in ['','long','Long']) {
            assertScript """
                void setFoo(Predicate<Long> p) {
                    assert p.test(1L)
                }
                foo = { $type n -> n instanceof Long }
            """
        }
    }
}
