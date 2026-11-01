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
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

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

    @Test
    void testCastIntToShort() {
        assertScript '''
            short s = (short) 0
        '''
    }

    @Test
    void testCastIntToFloat() {
        assertScript '''
            float f = (float) 1
        '''
    }

    @Test
    void testCastCompatibleType() {
        assertScript '''
            String s = 'Hello'
            ((CharSequence) s)
        '''
    }

    @Test
    void testCastIncompatibleType() {
        shouldFailWithMessages '''
            String s = 'Hello'
            ((Set) s)
        ''',
        'Inconvertible types: cannot cast java.lang.String to java.util.Set'
    }

    @Test
    void testCastIncompatibleTypeWithFlowType() {
        shouldFailWithMessages '''
            def s = 'Hello'
            s = 1
            ((Set) s)
        ''',
        'Inconvertible types: cannot cast java.lang.Integer to java.util.Set'
    }

    @Test
    void testCastStringToChar() {
        assertScript '''
            def c = (char) 'a'
            assert c === "a".charAt(0)
        '''
    }

    @Test
    void testCastCharToByte() {
        assertScript '''
            void foo(char c) {
                byte b = (byte) c
            }
        '''
    }

    @Test
    void testCastCharToInt() {
        assertScript '''
            void foo(char c) {
                int b = (int) c
            }
        '''
    }

    @Test
    void testCastStringLongerThan1ToChar() {
        shouldFailWithMessages '''
            def c = (char) 'aa'
        ''',
        'Inconvertible types: cannot cast java.lang.String to char'
    }

    // GROOVY-6577
    @Test
    void testCastNullToBoolean() {
        assertScript '''
            boolean b = (boolean) null
            assert b === false
        '''
    }

    @Test
    void testCastNullToPrimitive() {
        // boolean tested above and void cannot appear in cast expression
        for (type in ['byte','char','double','float','int','long','short']) {
            shouldFailWithMessages """
                def v = ($type) null
            """,
            "Inconvertible types: cannot cast java.lang.Object to $type"
        }
    }

    @Test
    void testCastNullToCharacter() {
        assertScript '''
            def c = (Character) null
            assert c === null
        '''
    }

    @Test
    void testCastStringToCharacter() {
        assertScript '''
            def c = (Character) 'a'
            assert c instanceof Character
            assert c.charValue() == "a".charAt(0)
        '''
    }

    @Test
    void testCastStringLongerThan1ToCharacter() {
        shouldFailWithMessages '''
            def c = (Character) 'aa'
        ''',
        'Inconvertible types: cannot cast java.lang.String to java.lang.Character'
    }

    @Test
    void testCastArray1() {
        assertScript '''
            (String[]) ['a','b','c'].toArray(new String[0])
        '''
        assertScript '''
            (String[]) new String[0]
        '''
    }

    @Test
    void testCastArray2() {
        assertScript '''
            (Object[]) new String[0]
        '''
        assertScript '''
            (Object) new String[0]
        '''
    }

    @Test
    void testCastArray3() {
        assertScript '''
            (Object[][]) new String[0][]
        '''
        assertScript '''
            (Object[]) new String[0][]
        '''
    }

    @Test
    void testCastArrayIncompatible1() {
        shouldFailWithMessages '''
            String[] src = ['a','b','c']
            (Set[]) src
        ''',
        'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    @Test
    void testCastArrayIncompatible2() {
        shouldFailWithMessages '''
            (Set[]) ['a','b','c'].toArray(new String[3])
        ''',
        'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    @Test
    void testCastArrayIncompatible3() {
        shouldFailWithMessages '''
            (Set[]) ['a','b','c'].toArray(String[]::new)
        ''',
        'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    @Test
    void testCastArrayIncompatible4() {
        shouldFailWithMessages '''
            (String[]) new String[0][]
        ''',
        'Inconvertible types: cannot cast java.lang.String[][] to java.lang.String[]'
    }

    // GROOVY-11371
    @Test
    void testCastArrayIncompatible5() {
        for (type in ['byte','char','double','float','int','long','short']) {
            shouldFailWithMessages """
                ($type[]) new Integer[1] // null values unsafe
            """,
            "Inconvertible types: cannot cast java.lang.Integer[] to $type[]"
        }
    }

    @Test
    void testCastObjectToSubclass() {
        assertScript '''
            Object o = null
            ((Integer) o)?.intValue()
        '''
    }

    @Test
    void testCastObjectToInterface() {
        assertScript '''
            List<Object> m(object) {
                if (object == null) return new ArrayList<>()
                if (object instanceof Collection) return new ArrayList<Object>((Collection) object)
                return [object]
            }
            def item = 0
            def list = [1,2]
            assert m(null).size() == 0
            assert m(item).size() == 1
            assert m(list).size() == 2
        '''
    }

    @Test
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

    @Test
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

    @Test
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
    @Test
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

    @Test
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

    @Test
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
    @Test
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

    @Test
    void testCoerceIncompatibleType() {
        // If the user uses explicit type coercion, there's nothing we can do
        assertScript '''
            String s = 'Hello'
            s as Set
        '''
    }

    @Test
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
    @Test
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
    @Test
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

    @Test
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
    @Test
    void testCoerceToFunctionalInterface5() {
        assertScript '''
            interface SAM<T,R> { R accept(T t); }
            SAM<Integer,Integer> s = { Integer n -> -n }
            assert s.accept(1) == -1
        '''
    }

    @Test
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

    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
    void testCoerceToFunctionalInterface12() {
        assertScript '''import java.util.concurrent.Callable
            Callable<String> c = { -> return 'foo' }
            assert c() == 'foo'
        '''
    }

    // GROOVY-10128, GROOVY-10306
    @Test
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
    @Test
    void testCoerceToFunctionalInterface14() {
        assertScript '''
            @Grab('org.awaitility:awaitility-groovy:4.2.0')
            import static org.awaitility.Awaitility.await

            List<String> strings = ['x']
            await().until { -> strings }
        '''
    }

    // GROOVY-10905
    @Test
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
    @Test
    void testCoerceToFunctionalInterface16() {
        assertScript '''import java.util.concurrent.atomic.AtomicReference
            def opt = new AtomicReference<Object>(null)
                .stream().filter { it.get() }.findAny()
            assert opt.isEmpty()
        '''
    }

    // GROOVY-11079
    @Test
    void testCoerceToFunctionalInterface17() {
        assertScript '''
            @Grab('io.vavr:vavr:0.10.4')
            import io.vavr.control.Try
            import io.vavr.Tuple2
import static groovy.test.GroovyAssert.shouldFail

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
    @Test
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
    @Test
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
    @Test
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

    // GROOVY-12599
    @Test
    void testCoerceClosureArgumentToFunctionalInterface() {
        assertScript '''
            String take(Supplier<String> s) { s.get().toLowerCase() }
            static String give(Supplier<String> s) { s.get() }
            Closure<String> c = { 'X' }
            assert take(c) == 'x'
            assert give(c) == 'X'

            String x = 'Y'
            Closure<GString> g = { "$x" }
            assert take(g) == 'y'

            Integer apply(Function<Integer,Integer> f) { f.apply(1) }
            Closure<Integer> i = { int n -> n + 1 }
            assert apply(i) == 2

            def <T> T pick(Supplier<T> s) { s.get() }
            Closure<Integer> one = { 1 }
            Integer n = pick(one)
            assert n == 1

            int size(ToIntFunction<String> f) { f.applyAsInt('abc') }
            Closure<Integer> len = { String s -> s.length() }
            assert size(len) == 3

            List<String> list(Supplier<List<String>> s) { s.get() }
            Closure<ArrayList<String>> d = { ['z'] }
            assert list(d) == ['z']

            def supply(Supplier<String> s) { s.get() }
            def runIt(Runnable r) { r.run(); 'ran' }
            def fromRaw(Closure r) { supply(r) }
            def fromObject(Closure<Object> o) { supply(o) }
            assert fromRaw({ 'r' }) == 'r'
            assert fromObject({ 'o' }) == 'o'
            assert runIt(one) == 'ran'
            assert take({ 'L' }) == 'l'
            assert take({ "$x" }) == 'y'
            assert take(() -> 'M') == 'm'

            class K {
                K(Supplier<String> s) { assert s.get() == 'k' }
            }
            Closure<String> k = { 'k' }
            new K(k)

            class D extends Closure<String> {
                D() { super(null) }
                String doCall() { 'D' }
            }
            assert take(new D()) == 'd'
            assert give(new D()) == 'D'
        '''
    }

    // GROOVY-12599
    @Test
    void testCoerceClosureArgumentToFunctionalInterface2() {
        shouldFailWithMessages '''
            class C {
                def take(Supplier<String> s) { s.get() }
            }
            Closure<Integer> c = { 1 }
            new C().take(c)
        ''',
        'Cannot call C#take(java.util.function.Supplier<java.lang.String>) with arguments [groovy.lang.Closure<java.lang.Integer>]'

        shouldFailWithMessages '''
            class C {
                static take(Supplier<? extends CharSequence> s) { s.get() }
            }
            Closure<Integer> c = { 1 }
            C.take(c)
        ''',
        'Cannot call C#take(java.util.function.Supplier<? extends java.lang.CharSequence>) with arguments [groovy.lang.Closure<java.lang.Integer>]'

        shouldFailWithMessages '''
            class C {
                static take(Function<Integer,String> f) { f.apply(1) }
            }
            Closure<Integer> c = { int i -> i }
            C.take(c)
        ''',
        'Cannot call C#take(java.util.function.Function<java.lang.Integer, java.lang.String>) with arguments [groovy.lang.Closure<java.lang.Integer>]'

        shouldFailWithMessages '''
            class C {
                static take(Supplier<List<String>> s) { s.get() }
            }
            Closure<List<Integer>> c = { [1] }
            C.take(c)
        ''',
        'Cannot call C#take(java.util.function.Supplier<java.util.List<java.lang.String>>) with arguments [groovy.lang.Closure<java.util.List<java.lang.Integer>>]'

        shouldFailWithMessages '''
            class C {
                static take(ToIntFunction<String> f) { f.applyAsInt('') }
            }
            Closure<String> c = { String s -> s }
            C.take(c)
        ''',
        'Cannot call C#take(java.util.function.ToIntFunction<java.lang.String>) with arguments [groovy.lang.Closure<java.lang.String>]'

        shouldFailWithMessages '''
            class K {
                K(Supplier<String> s) { }
            }
            Closure<Integer> c = { 1 }
            new K(c)
        ''',
        'Cannot call K#<init>(java.util.function.Supplier<java.lang.String>) with arguments [groovy.lang.Closure<java.lang.Integer>]'

        shouldFailWithMessages '''
            class D extends Closure<Integer> {
                D() { super(null) }
                Integer doCall() { 1 }
            }
            class C {
                static take(Supplier<String> s) { s.get() }
            }
            C.take(new D())
        ''',
        'Cannot call C#take(java.util.function.Supplier<java.lang.String>) with arguments [D]'

        shouldFailWithMessages '''
            Optional<String> o = Optional.empty()
            Closure<Integer> c = { 1 }
            o.orElseGet(c)
        ''',
        'Cannot call java.util.Optional#orElseGet(java.util.function.Supplier<? extends java.lang.String>) with arguments [groovy.lang.Closure<java.lang.Integer>]'
    }

    // GROOVY-12599
    @Test
    void testCoerceClosureReturnToFunctionalInterface() {
        assertScript '''
            Supplier<String> one() {
                Closure<String> c = { 'x' }
                c
            }
            static Supplier<String> two() {
                String x = 'Y'
                Closure<GString> c = { "$x" }
                return c
            }
            Function<Integer,Integer> three() {
                Closure<Integer> c = { int n -> n + 1 }
                c
            }
            Supplier<String> four(Closure c) { c }
            Supplier<String> five(Closure<Object> c) { c }
            Runnable six() {
                Closure<Integer> c = { 1 }
                c
            }
            Supplier<String> seven(boolean b) {
                b ? { 'y' } : { 'n' }
            }
            Supplier<List<String>> eight() {
                Closure<ArrayList<String>> c = { ['z'] }
                c
            }
            abstract class A<T> { abstract T get() }
            A<String> nine() {
                Closure<String> c = { 'a' }
                c
            }
            Function<Integer,Integer> ten() { Math.&abs }

            assert one().get() == 'x'
            assert two().get().toLowerCase() == 'y'
            assert three().apply(1) == 2
            assert four({ 'r' }).get() == 'r'
            assert five({ 'o' }).get() == 'o'
            six().run()
            assert seven(true).get() == 'y'
            assert eight().get() == ['z']
            assert nine().get() == 'a'
            assert ten().apply(-1) == 1
        '''
    }

    // GROOVY-12599
    @Test
    void testCoerceClosureReturnToFunctionalInterface2() {
        shouldFailWithMessages '''
            Supplier<String> mk() {
                Closure<Integer> c = { 1 }
                c
            }
        ''',
        'Cannot return value of type groovy.lang.Closure<java.lang.Integer> for method returning java.util.function.Supplier<java.lang.String>'

        shouldFailWithMessages '''
            static Supplier<String> mk() {
                Closure<Integer> c = { 1 }
                return c
            }
        ''',
        'Cannot return value of type groovy.lang.Closure<java.lang.Integer> for method returning java.util.function.Supplier<java.lang.String>'

        shouldFailWithMessages '''
            Function<Integer,String> mk() {
                Closure<Integer> c = { int i -> i }
                c
            }
        ''',
        'Cannot return value of type groovy.lang.Closure<java.lang.Integer> for method returning java.util.function.Function<java.lang.Integer, java.lang.String>'

        shouldFailWithMessages '''
            Supplier<List<String>> mk() {
                Closure<List<Integer>> c = { [1] }
                c
            }
        ''',
        'Cannot return value of type groovy.lang.Closure<java.util.List<java.lang.Integer>> for method returning java.util.function.Supplier<java.util.List<java.lang.String>>'

        shouldFailWithMessages '''
            Supplier<String> mk(boolean b) {
                Closure<Integer> c = { 1 }
                b ? c : null
            }
        ''',
        'Cannot return value of type groovy.lang.Closure<java.lang.Integer> for method returning java.util.function.Supplier<java.lang.String>'

        shouldFailWithMessages '''
            abstract class A<T> { abstract T get() }
            A<String> mk() {
                Closure<Integer> c = { 1 }
                c
            }
        ''',
        'Cannot return value of type groovy.lang.Closure<java.lang.Integer> for method returning A<java.lang.String>'

        shouldFailWithMessages '''
            class D extends Closure<Integer> {
                D() { super(null) }
                Integer doCall() { 1 }
            }
            Supplier<String> mk() {
                new D()
            }
        ''',
        'Cannot return value of type D for method returning java.util.function.Supplier<java.lang.String>'
    }

    // GROOVY-11092, GROOVY-8499
    @Test
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
