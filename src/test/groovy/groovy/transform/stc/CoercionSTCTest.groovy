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

    void testCastIntToShort() {
        assertScript '''
            short s = (short) 0
        '''
    }

    void testCastIntToFloat() {
        assertScript '''
            float f = (float) 1
        '''
    }

    void testCastCompatibleType() {
        assertScript '''
            String s = 'Hello'
            ((CharSequence) s)
        '''
    }

    void testCastIncompatibleType() {
        shouldFailWithMessages '''
            String s = 'Hello'
            ((Set) s)
        ''',
        'Inconvertible types: cannot cast java.lang.String to java.util.Set'
    }

    void testCastIncompatibleTypeWithFlowType() {
        shouldFailWithMessages '''
            def s = 'Hello'
            s = 1
            ((Set) s)
        ''',
        'Inconvertible types: cannot cast java.lang.Integer to java.util.Set'
    }

    void testCastStringToChar() {
        assertScript '''
            def c = (char) 'a'
            assert c === "a".charAt(0)
        '''
    }

    void testCastCharToByte() {
        assertScript '''
            void foo(char c) {
                byte b = (byte) c
            }
        '''
    }

    void testCastCharToInt() {
        assertScript '''
            void foo(char c) {
                int b = (int) c
            }
        '''
    }

    void testCastStringLongerThan1ToChar() {
        shouldFailWithMessages '''
            def c = (char) 'aa'
        ''',
        'Inconvertible types: cannot cast java.lang.String to char'
    }

    // GROOVY-6577
    void testCastNullToBoolean() {
        assertScript '''
            boolean b = (boolean) null
            assert b === false
        '''
    }

    void testCastNullToPrimitive() {
        // boolean tested above and void cannot appear in cast expression
        for (type in ['byte','char','double','float','int','long','short']) {
            shouldFailWithMessages """
                def v = ($type) null
            """,
            "Inconvertible types: cannot cast java.lang.Object to $type"
        }
    }

    void testCastNullToCharacter() {
        assertScript '''
            def c = (Character) null
            assert c === null
        '''
    }

    void testCastStringToCharacter() {
        assertScript '''
            def c = (Character) 'a'
            assert c instanceof Character
            assert c.charValue() == "a".charAt(0)
        '''
    }

    void testCastStringLongerThan1ToCharacter() {
        shouldFailWithMessages '''
            def c = (Character) 'aa'
        ''',
        'Inconvertible types: cannot cast java.lang.String to java.lang.Character'
    }

    void testCastArray1() {
        assertScript '''
            (String[]) ['a','b','c'].toArray(new String[0])
        '''
        assertScript '''
            (String[]) new String[0]
        '''
    }

    void testCastArray2() {
        assertScript '''
            (Object[]) new String[0]
        '''
        assertScript '''
            (Object) new String[0]
        '''
    }

    void testCastArray3() {
        assertScript '''
            (Object[][]) new String[0][]
        '''
        assertScript '''
            (Object[]) new String[0][]
        '''
    }

    void testCastArrayIncompatible1() {
        shouldFailWithMessages '''
            String[] src = ['a','b','c']
            (Set[]) src
        ''',
        'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    void testCastArrayIncompatible2() {
        shouldFailWithMessages '''
            (Set[]) ['a','b','c'].toArray(new String[3])
        ''',
        'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    void testCastArrayIncompatible3() {
        shouldFailWithMessages '''
            (Set[]) ['a','b','c'].toArray(String[]::new)
        ''',
        'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    void testCastArrayIncompatible4() {
        shouldFailWithMessages '''
            (String[]) new String[0][]
        ''',
        'Inconvertible types: cannot cast java.lang.String[][] to java.lang.String[]'
    }

    // GROOVY-11371
    void testCastArrayIncompatible5() {
        for (type in ['byte','char','double','float','int','long','short']) {
            shouldFailWithMessages """
                ($type[]) new Integer[1] // null values unsafe
            """,
            "Inconvertible types: cannot cast java.lang.Integer[] to $type[]"
        }
    }

    void testCastObjectToSubclass() {
        assertScript '''
            Object o = null
            ((Integer) o)?.intValue()
        '''
    }

    void testCastInterfaceToSubclass() {
        assertScript '''
            interface A {
            }
            interface B extends A {
            }
            class C implements B {
            }
            def m(B b) {
                C c = (C) b
            }
        '''
    }

    void testCastInterfaceToSubclass2() {
        shouldFailWithMessages '''
            interface I {
            }
            interface B extends I {
            }
            final class C implements I {
            }
            def m(B b) {
                C c = (C) b
            }
        ''',
        'Inconvertible types: cannot cast B to C'
    }

    //--------------------------------------------------------------------------

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
        ''',
        'Cannot find matching constructor java.lang.Class()'

        shouldFailWithMessages '''
            Class c = [:]
        ''',
        'Cannot find matching constructor java.lang.Class(', 'Map', ')'
    }

    // GROOVY-6803
    void testCoerceToString() {
        assertScript '''
            String s = ['x']
            assert s == '[x]'
        '''
        assertScript '''
            String s = [:]
            assert s == '[:]'
        '''
        assertScript '''
            String s = []
            assert s == '[]'
        '''
    }

    void testCoerceIncompatibleType() {
        // If the user uses explicit type coercion, there's nothing we can do
        assertScript '''
            String s = 'Hello'
            s as Set
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

    // GROOVY-9991, GROOVY-10277
    void testCoerceToFunctionalInterface3() {
        assertScript '''
            Consumer<Number>  c = { it }
            Supplier<Number>  s = { 42 }
            Predicate<Number> p = { true }
        '''

        assertScript '''
            Consumer<Number>  c = { n -> }
            Supplier<Number>  s = {   -> 42 }
            Predicate<Number> p = { n -> 42 }
        '''

        assertScript '''
            def c = (Consumer<Number>)  { n -> }
            def s = (Supplier<Number>)  {   -> 42 }
            def p = (Predicate<Number>) { n -> 42 }
        '''

        assertScript '''
            def c = { n ->    } as Consumer<Number>
            def s = {   -> 42 } as Supplier<Number>
            def p = { n -> 42 } as Predicate<Number>
        '''

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
        'Cannot return value of type java.util.ArrayList<java.lang.String> for lambda expecting java.lang.Number'
    }

    void testCoerceToFunctionalInterface4() {
        assertScript '''
            interface I { def m() }

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

        shouldFailWithMessages '''
            interface I {
                String toString()
            }
            I i = { p -> "" }
        ''',
        'Cannot assign'

        shouldFailWithMessages '''
            interface I {
                String toString()
            }
            abstract class A implements I { }

            A a = { "" } // implicit parameter
        ''',
        'Cannot assign'

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
        'Cannot assign'
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
        assertScript '''
            interface SAM { def foo() }
            class X {
                public SAM s
            }
            def x = new X()
            x.s = {1}
            assert x.s.foo() == 1
            x.@s = {2}
            assert x.s.foo() == 2
        '''
    }

    void testCoerceToFunctionalInterface7() {
        assertScript '''
            interface SAM { def foo() }
            class X {
                SAM s
            }
            def x = new X(s: {3})
            assert x.s.foo() == 3
        '''
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
    void testCoerceToFunctionalInterface9() {
        assertScript '''
            def f(Supplier<Integer>... suppliers) {
                suppliers*.get().sum()
            }
            Object result = f({->1},{->2})
            assert result == 3
        '''
    }

    // GROOVY-8168
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

    // GROOVY-9079
    void testCoerceToFunctionalInterface12() {
        assertScript '''import java.util.concurrent.Callable
            Callable<String> c = { -> return 'foo' }
            assert c() == 'foo'
        '''
    }

    // GROOVY-10128, GROOVY-10306
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
                    //.map(Tuple2::_2)
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

    // GROOVY-11092
    void testCoerceToFunctionalInterface20() {
        for (spec in ['one, two','String one, String two','CharSequence one, Object two']) {
            assertScript """
                Function<List<String>,String> f = { $spec -> one + two }
                assert f.apply(['foo','bar']) == 'foobar'
            """
        }
        for (spec in ['s, n','String s, Integer n','String s, Number n']) {
            assertScript """
                ToIntFunction<Tuple2<String,Integer>> f = { $spec -> n.intValue() }
                assert f.applyAsInt(Tuple.tuple("", 42)) == 42
            """
        }

        shouldFailWithMessages '''
            Consumer<List<String>> c = { Number not_list_or_string -> }
        ''',
        'Expected type java.lang.String for closure parameter'

        shouldFailWithMessages '''
            Consumer<List<String>> c = (Number xxx) -> { }
        ''',
        'Expected type java.lang.String for lambda parameter'

        shouldFailWithMessages '''
            Consumer<Tuple2> c = { -> }
        ''',
        'Wrong number of parameters for method target'
    }

    // GROOVY-11092, GROOVY-8499
    void testCoerceToFunctionalInterface21() {
        assertScript '''
            def result = ['ab'.chars,'12'.chars].combinations().stream().map((l,n) -> "$l$n").toList()
            assert result == ['a1','b1','a2','b2']
        '''
        // cannot know in advance how many list elements
        def err = shouldFail '''
            ['ab'.chars,'12'.chars].combinations().stream().map((l,n,x) -> "").toList()
        '''
        assert err =~ /No signature of method.* is applicable for argument types: \(ArrayList\)/
    }
}
