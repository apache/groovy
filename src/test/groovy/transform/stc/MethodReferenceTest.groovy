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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail

final class MethodReferenceTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            normal 'groovy.transform.CompileStatic'
            normal 'java.util.stream.Collectors'
            star 'java.util.function'
        }
    }

    @Test // class::instanceMethod
    void testFunctionCI() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList())
                assert result == ['1', '2', '3']
            }

            p()
        '''
    }

    @Test // class::instanceMethod
    void testFunctionCI2() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(Integer::toString).collect(Collectors.toList())
                assert result == ['1', '2', '3']
            }

            p()
        '''
    }

    @Test // class::instanceMethod -- GROOVY-10047
    void testFunctionCI3() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                List<String> list = ['a','bc','def']
                Function<String,String> self = str -> str // help for toMap
                def map = list.stream().collect(Collectors.toMap(self, String::length))
                assert map == [a: 1, bc: 2, 'def': 3]
            }

            p()
        '''

        assertScript shell, '''
            @CompileStatic
            void p() {
                List<String> list = ['a','bc','def']
                // TODO: inference for T in toMap(Function<? super T,...>, Function<? super T,...>)
                def map = list.stream().collect(Collectors.toMap(Function.<String>identity(), String::length))
                assert map == [a: 1, bc: 2, 'def': 3]
            }

            p()
        '''
    }

    @Test // class::instanceMethod
    void testFunctionCI4() {
        def err = shouldFail shell, '''
            @CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(String::toString).collect(Collectors.toList())
            }

            p()
        '''
        assert err =~ /Invalid receiver type: java.lang.Integer is not compatible with java.lang.String/
    }

    @Test // class::instanceMethod -- GROOVY-9814
    void testFunctionCI5() {
        assertScript shell, '''
            @CompileStatic
            class One { String id }

            @CompileStatic
            class Two extends One { }

            @CompileStatic @groovy.transform.Immutable(knownImmutableClasses=[Function])
            class FunctionHolder<T> {
                Function<T, ?> extractor

                def apply(T t) {
                    extractor.apply(t)
                }
            }

            def fh = new FunctionHolder(One::getId)
            assert fh.apply(new One(id:'abc')) == 'abc'

            fh = new FunctionHolder(One::getId)
            assert fh.apply(new Two(id:'xyz')) == 'xyz' // sub-type argument
        '''
    }

    @Test // class::instanceMethod -- GROOVY-9813
    void testFunctionCI6() {
        String head = '''
            @CompileStatic
            class C {
                def <T> List<T> asList(T... a) {
                    return Arrays.asList(a)
                }
                static main(args) {
        '''
        String tail = '''
                }
            }
        '''

        shouldFail shell, head + '''
            Supplier<List> zero = C::asList
        ''' + tail

        assertScript shell, head + '''
            Function<C, List> one = C::asList
            def list = one.apply(new C())
            assert list.isEmpty()
        ''' + tail

        assertScript shell, head + '''
            BiFunction<C, Integer, List> two = C::asList
            def list = two.apply(new C(),1)
            assert list.size() == 1
            assert list[0] == 1
        ''' + tail
    }

    @Test // class::instanceMethod -- GROOVY-9853
    void testFunctionCI7() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                ToIntFunction<CharSequence> f = CharSequence::size
                int size = f.applyAsInt("")
                assert size == 0
            }
            test()
        '''

        assertScript shell, '''
            @CompileStatic
            void test() {
                ToIntFunction<CharSequence> f = CharSequence::length
                int length = f.applyAsInt("")
                assert length == 0
            }
            test()
        '''

        assertScript shell, '''
            @CompileStatic
            void test() {
                Function<CharSequence,Integer> f = CharSequence::length
                Integer length = f.apply("")
                assert length == 0
            }
            test()
        '''

        assertScript shell, '''
            import java.util.stream.IntStream

            @CompileStatic
            void test() {
                Function<CharSequence,IntStream> f = CharSequence::chars // default method
                IntStream chars = f.apply("")
                assert chars.count() == 0
            }
            test()
        '''

        if (!isAtLeastJdk('11.0')) return

        assertScript shell, '''
            @CompileStatic
            void test() {
                ToIntBiFunction<CharSequence,CharSequence> f = CharSequence::compare // static method
                int result = f.applyAsInt("","")
                assert result == 0
            }
            test()
        '''
    }

    @Test // class::instanceMethod -- GROOVY-10734
    void testFunctionCI8() {
        assertScript shell, '''
            class C {
                String p
            }
            @CompileStatic
            Map test(Collection<C> items) {
                items.stream().collect(
                    Collectors.groupingBy(C::getP) // Failed to find the expected method[getP(Object)] in the type[C]
                )
            }
            def map = test([new C(p:'foo'), new C(p:'bar'), new C(p:'foo')])
            assert map.foo.size() == 2
            assert map.bar.size() == 1
        '''
    }

    @Test // class::instanceMethod -- GROOVY-9974
    void testPredicateCI() {
        assertScript shell, '''
            @CompileStatic
            void test(List<String> strings = ['']) {
                strings.removeIf(String::isEmpty)
                assert strings.isEmpty()
            }
            test()
        '''
    }

    @Test // class::instanceMethod
    void testBinaryOperatorCI() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, BigDecimal::add)
                assert 6.0G == result
            }
            test()
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-9813
    void testFunctionII() {
        String asList = '''
            def <T> List<T> asList(T... a) {
                return Arrays.asList(a)
            }
        '''

        assertScript shell, asList + '''
            @CompileStatic
            void test() {
                Supplier<List> zero = this::asList
                def list = zero.get()
                assert list.isEmpty()
            }
            test()
        '''

        assertScript shell, asList + '''
            @CompileStatic
            void test() {
                Function<Integer, List> one = this::asList
                def list = one.apply(1)
                assert list.size() == 1
                assert list[0] == 1
            }
            test()
        '''

        assertScript shell, asList + '''
            @CompileStatic
            void test() {
                BiFunction<Integer, Integer, List> two = this::asList
                def list = two.apply(2,3)
                assert list.size() == 2
                assert list[0] == 2
                assert list[1] == 3
            }
            test()
        '''

        assertScript shell, asList + '''
            @CompileStatic
            void test() { def that = this
                BiFunction<Integer, Integer, List> two = that::asList
                def list = two.apply(2,3)
                assert list.size() == 2
                assert list[0] == 2
                assert list[1] == 3
            }
            test()
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-10653
    void testFunctionII2() {
        assertScript shell, '''
            class C {
                List m(... args) {
                    [this,*args]
                }
            }
            @CompileStatic
            void test(C c) {
                BiFunction<Integer, Integer, List> two = c::m
                def list = two.apply(1,2)
                assert list.size() == 3
                assert list[0] == c
                assert list[1] == 1
                assert list[2] == 2
            }
            test(new C())
        '''
    }

    @Test // instance::instanceMethod (DGM) -- GROOVY-10653
    void testFunctionII3() {
        assertScript shell, '''
            @CompileStatic
            int test(CharSequence chars) {
                IntSupplier sizing = chars::size // from StringGroovyMethods
                return sizing.getAsInt()
            }
            int size = test("foo")
            assert size == 3
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-10057
    void testPredicateII() {
        assertScript shell, '''
            Class c = Integer
            Predicate p

            p = c::isInstance
            assert p.test(null) == false
            assert p.test('xx') == false
            assert p.test(1234) == true

            p = c.&isInstance
            assert p.test(null) == false
            assert p.test('xx') == false
            assert p.test(1234) == true

            p = o -> c.isInstance(o)
            assert p.test(null) == false
            assert p.test('xx') == false
            assert p.test(1234) == true

            p = { c.isInstance(it) }
            assert p.test(null) == false
            assert p.test('xx') == false
            assert p.test(1234) == true
        '''
    }

    @Test // instance::instanceMethod
    void testBinaryOperatorII() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)
                assert 6.0G == result
            }

            p()

            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    @Test // instance::instanceMethod
    void testBinaryOperatorII_COMPATIBLE() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)
                assert 6.0G == result
            }

            p()

            class Adder {
                BigDecimal add(Number a, Number b) {
                    ((BigDecimal) a).add((BigDecimal) b)
                }
            }
        '''
    }

    @Test // expression::instanceMethod
    void testBinaryOperatorII_EXPRESSION() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder()::add)
                assert 6.0G == result
            }

            p()

            class Adder {
                public BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    @Test // expression::instanceMethod
    void testBinaryOperatorII_EXPRESSION2() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder().getThis()::add)
                assert new BigDecimal(6) == result
            }

            p()

            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }

                Adder getThis() {
                    return this
                }
            }
        '''
    }

    @Test // instance::instanceMethod
    void testBinaryOperatorII_RHS() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                Adder adder = new Adder()
                BinaryOperator<BigDecimal> b = adder::add
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, b)
                assert 6.0G == result
            }

            p()

            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    @Test // expression::instanceMethod
    void testBinaryOperatorII_RHS2() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                BinaryOperator<BigDecimal> b = new Adder()::add
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, b)
                assert 6.0G == result
            }

            p()

            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    @Test // instance::staticMethod
    void testBinaryOperatorIS() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)
                assert 6.0G == result
            }

            p()

            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    @Test // expression::staticMethod
    void testBinaryOperatorIS_EXPRESSION() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder()::add)
                assert 6.0G == result
            }

            p()

            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    @Test // expression::staticMethod
    void testBinaryOperatorIS_EXPRESSION2() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, Adder.newInstance()::add)
                assert 6.0G == result
            }

            p()

            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }

                static Adder newInstance() {
                    new Adder()
                }
            }
        '''
    }

    @Test // arrayClass::new
    void testFunctionCN() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [1, 2, 3].stream().toArray(Integer[]::new)
                assert result == new Integer[] { 1, 2, 3 }
            }

            p()
        '''
    }

    @Test // class::new
    void testFunctionCN2() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = ["1", "2", "3"].stream().map(Integer::new).collect(Collectors.toList())
                assert result == [1, 2, 3]
            }

            p()
        '''
    }

    @Test // class::new -- GROOVY-10033
    void testFunctionCN3() {
        assertScript shell, '''
            @CompileStatic
            class C {
                C(Function<String,Integer> f) {
                    def i = f.apply('42')
                    assert i == 42
                }
                static test() {
                    new C(Integer::new)
                }
            }
            C.test()
        '''
    }

    @Test // class::new -- GROOVY-10033
    void testFunctionCN4() {
        assertScript shell, '''
            class A {
                A(Function<A,B> f) {
                    B b = f.apply(this)
                    assert b instanceof X.Y
                }
            }
            class B {
                B(A a) {
                    assert a != null
                }
            }
            @CompileStatic
            class X extends A {
              public X() {
                super(Y::new)
              }
              private static class Y extends B {
                Y(A a) {
                  super(a)
                }
              }
            }

            new X()
        '''
    }

    @Test // class::new
    void testFunctionCN5() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                Function<String, Integer> f = Integer::new
                assert [1, 2, 3] == ["1", "2", "3"].stream().map(f).collect(Collectors.toList())
            }

            p()
        '''
    }

    @Test // arrayClass::new
    void testIntFunctionCN6() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                IntFunction<Integer[]> f = Integer[]::new
                assert new Integer[] { 1, 2, 3 } == [1, 2, 3].stream().toArray(f)
            }

            p()
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [1, -2, 3].stream().map(Math::abs).collect(Collectors.toList())
                assert [1, 2, 3] == result
            }

            p()
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS2() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                List<String> list = ['x','y','z']
                def map = list.stream().collect(Collectors.toMap(Function.identity(), Collections::singletonList))
                assert map == [x: ['x'], y: ['y'], z: ['z']]
            }

            p()
        '''
    }

    @Test // class::staticMethod -- GROOVY-9799
    void testFunctionCS3() {
        assertScript shell, '''
            class C {
                String x
            }

            class D {
                String x
                static D from(C c) {
                    new D(x: c.x)
                }
            }

            @CompileStatic
            def test(C c) {
                Optional.of(c).map(D::from).get()
            }

            def d = test(new C(x: 'x'))
            assert d.x == 'x'
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS4() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                Function<Integer, Integer> f = Math::abs
                def result = [1, -2, 3].stream().map(f).collect(Collectors.toList())
                assert [1, 2, 3] == result
            }
            p()
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS5() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def f = Math::abs // No explicit type defined, so it is actually a method closure. We can make it smarter in a later version.
                def result = [1, -2, 3].stream().map(f).collect(Collectors.toList())
                assert [1, 2, 3] == result
            }
            p()
        '''
    }

    @Test // class::staticMethod -- GROOVY-9813
    void testFunctionCS6() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                Supplier<List> zero = Arrays::asList
                def list = zero.get()
                assert list.isEmpty()
            }
            p()
        '''

        assertScript shell, '''
            @CompileStatic
            void p() {
                Function<Integer, List> one = Arrays::asList
                def list = one.apply(1)
                assert list.size() == 1
                assert list[0] == 1
            }
            p()
        '''

        assertScript shell, '''
            @CompileStatic
            void p() {
                BiFunction<Integer, Integer, List> two = Arrays::asList
                def list = two.apply(2,3)
                assert list.size() == 2
                assert list[0] == 2
                assert list[1] == 3
            }
            p()
        '''
    }

    @Test // class::instanceMethod, actually class::staticMethod
    void testFunctionCI_DGM() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = ['a', 'ab', 'abc'].stream().map(String::size).collect(Collectors.toList())
                assert [1, 2, 3] == result
            }
            p()
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS_DGSM() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every(e -> e instanceof Thread)
            }
            p()
        '''
    }

    @Test // class::instanceMethod
    void testFunctionCI_SHADOW_DGM() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [[a:1], [b:2], [c:3]].stream().map(Object::toString).collect(Collectors.toList())
                assert 3 == result.size()
                assert ['[a:1]', '[b:2]', '[c:3]'] == result
            }
            p()
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS_MULTI_DGSM() {
        assertScript shell, '''
            @CompileStatic
            void p() {
                def result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every(e -> e instanceof Thread)

                result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every(e -> e instanceof Thread)
            }
            p()
        '''
    }

    @Test
    void testMethodNotFound1() {
        def err = shouldFail shell, '''
            @CompileStatic
            void p() {
                [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, BigDecimal::addx)
            }
        '''
        assert err.message.contains('Failed to find the expected method[addx(java.math.BigDecimal,java.math.BigDecimal)] in the type[java.math.BigDecimal]')
    }

    @Test // GROOVY-9463
    void testMethodNotFound2() {
        def err = shouldFail shell, '''
            @CompileStatic
            void p() {
                Function<String,String> reference = String::toLowerCaseX
            }
        '''
        assert err.message.contains('Failed to find the expected method[toLowerCaseX(java.lang.String)] in the type[java.lang.String]')
    }

    @Test // GROOVY-10714
    void testMethodSelection() {
        assertScript shell, '''
            class C {
                String which
                void m(int i) { which = 'int' }
                void m(Number n) { which = 'Number' }
            }
            interface I {
                I andThen(Consumer<? super Number> c)
                I andThen(BiConsumer<? super Number, ?> bc)
            }
            @CompileStatic
            void test(I i, C c) {
                i = i.andThen(c::m) // "andThen" is ambiguous unless parameters of "m" overloads are taken into account
            }

            C x= new C()
            test(new I() {
                I andThen(Consumer<? super Number> c) {
                    c.accept(42)
                    return this
                }
                I andThen(BiConsumer<? super Number, ?> bc) {
                    bc.accept(42, null)
                    return this
                }
            }, x)
            assert x.which == 'Number'
        '''
    }

    @Test // GROOVY-10742
    void testVoidMethodSelection() {
        def err = shouldFail shell, '''
            void foo(bar) {
            }
            @CompileStatic
            void test() {
                Function<Object,String> f = this::foo
            }
        '''
        assert err =~ /Invalid return type: void is not convertible to java.lang.String/
    }

    @Test // GROOVY-10269
    void testNotFunctionalInterface() {
        def err = shouldFail shell, '''
            void foo(Integer y) {
            }
            void bar(Consumer<Integer> x) {
            }
            @CompileStatic
            void test() {
                bar(this::foo)
                def baz = { Consumer<Integer> x -> }
                baz(this::foo) // not yet supported!
            }
        '''
        assert err =~ /The argument is a method reference, but the parameter type is not a functional interface/
    }

    @Test // GROOVY-10336
    void testNotFunctionalInterface2() {
        def err = shouldFail shell, '''
            class C {
                Integer m() { 1 }
            }
            @CompileStatic
            void test() {
                Supplier<Long> outer = () -> {
                    Closure<Long> inner = (Object o, Supplier<Integer> s) -> 2L
                    inner(new Object(), new C()::m) // TODO: resolve call(Object,Supplier<Integer>)
                }
            }
        '''
        assert err =~ /The argument is a method reference, but the parameter type is not a functional interface/
    }

    @Test // GROOVY-10635
    void testRecordComponentMethodReference() {
        assertScript shell, '''
            record Bar(String name) {
            }
            def bars = [new Bar(name: 'A'), new Bar(name: 'B')]
            assert bars.stream().map(Bar::name).map(String::toLowerCase).toList() == ['a', 'b']
        '''
    }
}
