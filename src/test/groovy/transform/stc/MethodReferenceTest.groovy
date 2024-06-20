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
import static groovy.test.GroovyAssert.shouldFail

final class MethodReferenceTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            star 'java.util.function'
            normal 'java.util.stream.Collectors'
        }
    }

    @Test // class::instanceMethod
    void testFunctionCI1() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList())
                assert result == ['1', '2', '3']
            }

            test()
        '''
    }

    @Test // class::instanceMethod
    void testFunctionCI2() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = [1, 2, 3].stream().map(Integer::toString).collect(Collectors.toList())
                assert result == ['1', '2', '3']
            }

            test()
        '''
    }

    @Test // class::instanceMethod -- GROOVY-10047
    void testFunctionCI3() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                List<String> list = ['a','bc','def']
                Function<String,String> self = str -> str // help for toMap
                def result = list.stream().collect(Collectors.toMap(self, String::length))
                assert result == [a: 1, bc: 2, 'def': 3]
            }

            test()
        '''

        assertScript shell, '''
            @CompileStatic
            void test() {
                List<String> list = ['a','bc','def']
                // TODO: inference for T in toMap(Function<? super T,...>, Function<? super T,...>)
                def result = list.stream().collect(Collectors.toMap(Function.<String>identity(), String::length))
                assert result == [a: 1, bc: 2, 'def': 3]
            }

            test()
        '''
    }

    @Test // class::instanceMethod
    void testFunctionCI4() {
        def err = shouldFail shell, '''
            @CompileStatic
            void test() {
                [1, 2, 3].stream().map(String::toString).collect(Collectors.toList())
            }
        '''
        assert err =~ /Invalid receiver type: java.lang.Integer is not compatible with java.lang.String/
    }

    @Test // class::instanceMethod -- GROOVY-9814
    void testFunctionCI5() {
        assertScript shell, '''
            class One { String id }

            class Two extends One { }

            @CompileStatic @Immutable(knownImmutableClasses=[Function])
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

    @Test // class::instanceMethod -- GROOVY-10054, GROOVY-10734
    void testFunctionCI8() {
        assertScript shell, '''
            class C {
                String p
            }

            @CompileStatic
            Map test(Collection<C> items) {
                items.stream().collect(Collectors.groupingBy(C::getP))
            }

            def map = test([new C(p:'foo'), new C(p:'bar'), new C(p:'foo')])
            assert map.foo.size() == 2
            assert map.bar.size() == 1
        '''
    }

    @Test // class::instanceMethod -- GROOVY-9803
    void testFunctionCI9() {
        assertScript shell, '''
            class Try<X> { X x
                static <Y> Try<Y> success(Y y) {
                    new Try<Y>(x: y)
                }
                def <Z> Try<Z> map(Function<? super X, ? extends Z> f) {
                    new Try<Z>(x: f.apply(x))
                }
            }

            static <E> Set<E> asSet(E element) {
                Collections.singleton(element)
            }

            @CompileStatic
            Try<String> test() {
                def try_of_str = Try.success('WORKS')
                def try_of_opt = try_of_str.map(this::asSet)
                    try_of_str = try_of_opt.map{it.first().toLowerCase()}
            }

            assert test().x == 'works'
        '''
    }

    @Test // class::instanceMethod -- GROOVY-11241
    void testFunctionCI10() {
        assertScript shell, '''
            @Grab('io.vavr:vavr:0.10.4')
            import io.vavr.control.*

            Option<Integer> option() { Option.of(42) }

            @CompileStatic
            Try<Integer> test() {
                Try.of{ option() }.<Integer>mapTry(Option::get)
                //                 ^^^^^^^^^
            }

            assert test().get() == 42
        '''

        assertScript shell, '''
            class Try<X> { X x
                static <Y> Try<Y> of(Supplier<? extends Y> s) {
                    new Try<Y>(x: s.get())
                }
                def <Z> Try<Z> mapTry(Function<? super X, ? extends Z> f) {
                    new Try<Z>(x: f.apply(x))
                }
            }

            @CompileStatic
            Try<String> test() {
                def try_of = Try.of{Optional.of('works')}
                def result = try_of.mapTry(Optional.&get) // Function<T,_> and Optional<T>
                return result
            }

            assert test().x == 'works'
        '''

        assertScript shell, '''
            @Grab('io.vavr:vavr:0.10.4')
            import io.vavr.control.Try

            class Option<X> {
                private X x
                def X get() { x }
                static <Y> Option<Y> of(Y y) {
                    new Option(x: y)
                }
            }

            Option<Integer> option() { Option.of(42) }

            @CompileStatic
            Try<Integer> test() {
                def try_of = Try.of { option() }
                def result = try_of.mapTry(Option::get)
                result // cannot assign Try<Object> to: Try<Integer>
            }

            assert test().get() == 42
        '''
    }

    @Test // class::instanceMethod -- GROOVY-11259
    void testFunctionCI11() {
        assertScript shell, '''
            def consume(Set<String> keys){keys}
            @CompileStatic
            def test(Map<String, String> map) {
                def keys = map.entrySet().stream()
                    .map(Map.Entry::getKey).toSet()
                consume(keys) // cannot call consume(Set<String>) with arguments [Set<Object>]
            }

            def set = test(foo:'bar', fizz:'buzz')
            assert set.size() == 2
            assert 'fizz' in set
            assert 'foo' in set
        '''
    }

    @Test // class::instanceMethod -- GROOVY-9974
    void testPredicateCI1() {
        assertScript shell, '''
            @CompileStatic
            void test(List<String> strings = ['']) {
                strings.removeIf(String::isEmpty)
                assert strings.isEmpty()
            }

            test()
        '''
    }

    @Test // class::instanceMethod -- GROOVY-11051
    void testPredicateCI2() {
        [['null','Empty'],['new Object()','Present']].each { value, which ->
            assertScript """import java.util.concurrent.atomic.AtomicReference
                def opt = new AtomicReference<Object>($value).stream()
                             .filter(AtomicReference::get).findFirst()
                assert opt.is${which}()
            """
        }
    }

    @Test // class::instanceMethod -- GROOVY-10791
    void testBiConsumerCI() {
        assertScript shell, '''
            @CompileStatic
            def <T> void test(List<T> list, Consumer<? super T> todo) {
                BiConsumer<List<T>, Consumer<? super T>> binder = List::forEach // default method of Iterator
                binder.accept(list, todo)
            }

            test(['works']) { assert it == 'works' }
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-10974
    void testBiConsumerII() {
        assertScript shell, '''import java.util.stream.*
            @CompileStatic
            def test(DoubleStream x, ObjDoubleConsumer<Boolean> y, BiConsumer<Boolean, Boolean> z) {
                def b = x.collect(() -> true, y::accept, z::accept) // b should infer as Boolean
                // <R>  R collect(Supplier<R>,ObjDoubleConsumer<R>,BiConsumer<R,R>)
                Spliterator.OfDouble s_of_d = Arrays.spliterator(new double[0])
                StreamSupport.doubleStream(s_of_d, b)
            }

            test(DoubleStream.of(0d), (Boolean b, double d) -> {}, (Boolean e, Boolean f) -> {})
        '''
    }

    @Test // class::instanceMethod
    void testBinaryOperatorCI() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, BigDecimal::add)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-10933
    void testConsumerII() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                List<String> strings = []
                Optional.of('string')
                    .ifPresent(strings::add)
                assert strings.contains('string')
            }

            test()
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-11020
    void testConsumerII2() {
        assertScript shell, '''
            def <C extends Consumer<String>> void m(C c) {
                c.accept('string')
            }

            @CompileStatic
            void test(ArrayDeque<String> strings) {
                m(strings::addFirst) // NPE in STC
                assert strings.contains('string')
            }

            test(new ArrayDeque<>())
        '''

        assertScript shell, '''
            @Grab('org.apache.commons:commons-collections4:4.4')
            import org.apache.commons.collections4.*

            @CompileStatic
            def test(Iterable<String> x, ArrayDeque<String> y) {
                CollectionUtils.forAllButLastDo(x,y::addFirst)
                IterableUtils.forEachButLast(x,y::addFirst)
                Iterator<String> z = x.iterator()
                IteratorUtils.forEachButLast(z,y::addFirst)
            }

            Iterable  <String> x = ['foo','bar','baz']
            ArrayDeque<String> y = []
            def z = test(x,y)

            assert y.join('') == 'barfoo'*3 && z == 'baz'
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-11068
    void testConsumerII3() {
        assertScript shell, '''
            @Grab('org.apache.pdfbox:pdfbox:2.0.28')
            import org.apache.pdfbox.pdmodel.*
            @Grab('io.vavr:vavr:0.10.4')
            import io.vavr.control.Try

            @CompileStatic
            def test(PDDocument doc) {
                extraPages().forEach {
                    it.forEach(doc::addPage) // expect operand PDDocument
                }
            }

            Try<Iterable<PDPage>> extraPages() {
                Try.success([new PDPage()])
            }

            test(new PDDocument())
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-11068
    void testConsumerII4() {
        assertScript shell, '''
            @Grab('org.apache.pdfbox:pdfbox:2.0.28')
            import org.apache.pdfbox.pdmodel.*
            @Grab('io.vavr:vavr:0.10.4')
            import io.vavr.control.Try

            @CompileStatic
            def test(List<PDDocument> docs) {
                docs.each { doc ->
                    extraPages().forEach {
                        it.forEach(doc::addPage) // expect operand PDDocument
                    }
                }
            }

            Try<Iterable<PDPage>> extraPages() {
                Try.success([new PDPage()])
            }

            test([new PDDocument()])
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

    @Test // instance::instanceGroovyMethod -- GROOVY-10653
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

    @Test // instance::instanceMethod -- GROOVY-10972
    void testFunctionII4() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                LinkedList<String> list = new LinkedList<>()
                list.add('works')
                Function<Integer,String> func = list::remove
                assert func.apply(0) == 'works'
            }

            test()
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-11364
    void testFunctionII5() {
        assertScript shell, '''
            abstract class A<N extends Number> {
                protected N process(N n) { n }
            }

            @CompileStatic
            class C extends A<Integer> {
                static void consume(Optional<Integer> option) {
                    def result = option.orElse(null)
                    assert result instanceof Integer
                    assert result == 42
                }
                void test() {
                    consume(Optional.of(42).map(this::process))
                }
            }

            new C().test()
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

    @Test // instance::instanceMethod -- GROOVY-10994
    void testPredicateII2() {
        assertScript shell, '''
            @CompileStatic
            def <T> void test(List<T> list) {
                Predicate<? super T> p = list::add
            }

            test([])
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-10975
    void testComparatorII() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Collection<Integer> c = [1]
                Map<Integer,Integer> m = [1:1]
                new Hashtable(Collections.min(c, m::put))
            }

            test()
        '''
    }

    @Test // instance::instanceMethod -- GROOVY-11026
    void testBiFunctionII() {
        assertScript shell, '''
            @CompileDynamic
            def <In,InOut> InOut m(BiFunction<In,InOut,InOut> beef) {
                beef.apply(0,'boo')
            }

            @CompileStatic
            String test(List<String> x) {
                m(x::set) // NPE
            }

            String result = test(['foo','bar'])
            assert result == 'foo'
        '''
    }

    @Test // instance::instanceMethod
    void testBinaryOperatorII() {
        assertScript shell, '''
            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }

            @CompileStatic
            void test() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // instance::instanceMethod
    void testBinaryOperatorII2() {
        assertScript shell, '''
            class Adder {
                BigDecimal add(Number a, Number b) {
                    ((BigDecimal) a).add((BigDecimal) b)
                }
            }

            @CompileStatic
            void test() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // expression::instanceMethod
    void testBinaryOperatorII_EXPRESSION() {
        assertScript shell, '''
            class Adder {
                public BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }

            @CompileStatic
            void test() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder()::add)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // expression::instanceMethod
    void testBinaryOperatorII_EXPRESSION2() {
        assertScript shell, '''
            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }

                Adder getThis() {
                    return this
                }
            }

            @CompileStatic
            void test() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder().getThis()::add)
                assert result == new BigDecimal(6)
            }

            test()
        '''
    }

    @Test // instance::instanceMethod
    void testBinaryOperatorII_RHS() {
        assertScript shell, '''
            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }

            @CompileStatic
            void test() {
                Adder adder = new Adder()
                BinaryOperator<BigDecimal> b = adder::add
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, b)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // expression::instanceMethod
    void testBinaryOperatorII_RHS2() {
        assertScript shell, '''
            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }

            @CompileStatic
            void test() {
                BinaryOperator<BigDecimal> b = new Adder()::add
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, b)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // instance::staticMethod
    void testBinaryOperatorIS() {
        assertScript shell, '''
            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }

            @CompileStatic
            void test() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // expression::staticMethod
    void testBinaryOperatorIS_EXPRESSION() {
        assertScript shell, '''
            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }

            @CompileStatic
            void test() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder()::add)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // expression::staticMethod
    void testBinaryOperatorIS_EXPRESSION2() {
        assertScript shell, '''
            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }

                static Adder newInstance() {
                    new Adder()
                }
            }

            @CompileStatic
            void test() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, Adder.newInstance()::add)
                assert result == 6.0G
            }

            test()
        '''
    }

    @Test // class::new
    void testFunctionCN() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = ["1", "2", "3"].stream().map(Integer::new).collect(Collectors.toList())
                assert result == [1, 2, 3]
            }

            test()
        '''
    }

    @Test // class::new
    void testFunctionCN2() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Function<String, Integer> f = Integer::new // deprcated; parseInt/valueOf
                def result = ["1", "2", "3"].stream().map(f).collect(Collectors.toList())
                assert result == [1, 2, 3]
            }

            test()
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

    @Test // class::new -- GROOVY-10930
    void testFunctionCN5() {
        def err = shouldFail shell, '''
            class Foo { Foo() { } }
            def <T> void m(Function<String,T> fn) { }

            @CompileStatic
            void test() {
                m(Foo::new) // ctor does not accept String
            }

            test()
        '''
        assert err =~ /Cannot find matching constructor Foo\(java.lang.String\)/
    }

    @Test // class::new -- GROOVY-10971
    void testFunctionCN6() {
        assertScript shell, '''
            class Foo { Foo(String s) { } }

            @CompileStatic
            void test() {
                Collectors.groupingBy(Foo::new) // Cannot find matching constructor Foo(Object)
            }

            test()
        '''
    }

    @Test // class::new -- GROOVY-11001
    void testFunctionCN7() {
        assertScript shell, '''
            @Grab('io.vavr:vavr:0.10.4')
            import io.vavr.control.Try

            class StringInputStream {
                StringInputStream(String s) {
                }
            }

            @CompileStatic
            void test() {
                Try.success('string').flatMap { // <U> Try<U> flatMap(Function<? super T,? extends Try<? extends U>> mapper)
                    Try.success(it).map(StringInputStream::new)
                }
            }

            test()
        '''
    }

    @Test // class::new -- GROOVY-11385
    void testFunctionCN8() {
        def err = shouldFail shell, '''
            abstract class A {
                A(String s) {}
            }
            @CompileStatic
            void test() {
                Function<String, A> f = A::new
                f.apply("") // InstantiationException
            }

            test()
        '''
        assert err =~ /Cannot instantiate the type A/

        for (op in ['::','.&']) {
            err = shouldFail shell, """
                @CompileStatic
                void test() {
                    Supplier<Number> s = Number${op}new
                }

                test()
            """
            assert err =~ /Cannot instantiate the type java.lang.Number/
        }
    }

    @Test // arrayClass::new
    void testIntFunctionCN() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                IntFunction<Integer[]> f = Integer[]::new;
                def result = [1, 2, 3].stream().toArray(f)
                assert result == new Integer[] {1, 2, 3}
            }

            test()
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = [1, -2, 3].stream().map(Math::abs).collect(Collectors.toList())
                assert result == [1, 2, 3]
            }

            test()
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS2() {
        for (makeList in ['Collections::singletonList','List::of']) {
            assertScript shell, """
                @CompileStatic
                def test(List<String> list) {
                    list.stream().collect(Collectors.toMap(Function.identity(), $makeList))
                }

                assert test(['x','y','z']) == [x: ['x'], y: ['y'], z: ['z']]
            """
        }
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
            void test() {
                Function<Integer, Integer> f = Math::abs
                def result = [1, -2, 3].stream().map(f).collect(Collectors.toList())
                assert [1, 2, 3] == result
            }

            test()
        '''
    }

    @Test // class::staticMethod
    void testFunctionCS5() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def f = Math::abs // No explicit type defined, so it is actually a method closure
                def result = [1, -2, 3].stream().map(f).collect(Collectors.toList())
                assert [1, 2, 3] == result
            }

            test()
        '''
    }

    @Test // class::staticMethod -- GROOVY-9813
    void testFunctionCS6() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Supplier<List> zero = Arrays::asList
                def list = zero.get()
                assert list.isEmpty()
            }

            test()
        '''

        assertScript shell, '''
            @CompileStatic
            void test() {
                Function<Integer, List> one = Arrays::asList
                def list = one.apply(1)
                assert list.size() == 1
                assert list[0] == 1
            }

            test()
        '''

        assertScript shell, '''
            @CompileStatic
            void test() {
                BiFunction<Integer, Integer, List> two = Arrays::asList
                def list = two.apply(2,3)
                assert list.size() == 2
                assert list[0] == 2
                assert list[1] == 3
            }

            test()
        '''
    }

    @Test // class::staticMethod -- GROOVY-10807
    void testFunctionCS7() {
        assertScript shell, '''
            @CompileStatic
            class C {
                public static Comparator<String> c = Comparator.<String,String>comparing(C::m)
                static String m(String string) {
                    return string
                }
            }

            List<String> list = ['foo','bar','baz']
            list.sort(C.c)

            assert list == ['bar','baz','foo']
        '''
    }

    @Test // class::staticMethod -- GROOVY-10807
    void testFunctionCS8() {
        assertScript shell, '''
            @CompileStatic
            class C {
                public static Comparator<String> c = Comparator.comparing(C::m)
                static String m(String string) {
                    return string
                }
            }

            List<String> list = ['foo','bar','baz']
            list.sort(C.c)

            assert list == ['bar','baz','foo']
        '''
    }

    @Test // class::staticMethod -- GROOVY-11009
    void testFunctionCS9() {
        assertScript shell, '''
            class C {
                static <T> T clone(T t) { t }
                // see also Object#clone()
            }

            @CompileStatic
            Double test() {
                Function<Double,Double> fn = C::clone
                fn.apply(1.234d)
            }

            def n = test()
            assert n == 1.234
        '''
    }

    @Test // class::instanceGroovyMethod
    void testFunctionCI_DGM() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = ['a', 'ab', 'abc'].stream().map(String::size).collect(Collectors.toList())
                assert result == [1, 2, 3]
            }

            test()
        '''
    }

    @Test // class::staticGroovyMethod
    void testFunctionCS_DGSM() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every { it instanceof Thread }
            }

            test()
        '''
    }

    @Test // class::instanceGroovyMethod
    void testFunctionCI_SHADOW_DGM() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = [[a:1], [b:2], [c:3]].stream().map(Object::toString).collect(Collectors.toList())
                assert result == ['[a:1]', '[b:2]', '[c:3]']
            }

            test()
        '''
    }

    @Test // class::staticGroovyMethod
    void testFunctionCS_MULTI_DGSM() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                def result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every { it instanceof Thread }

                result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every { it instanceof Thread }
            }

            test()
        '''
    }

    @Test
    void testMethodNotFound1() {
        def err = shouldFail shell, '''
            @CompileStatic
            void test() {
                [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, BigDecimal::addx)
            }
        '''
        assert err.message.contains("Failed to find class method 'addx(java.math.BigDecimal,java.math.BigDecimal)' or instance method 'addx(java.math.BigDecimal)' for the type: java.math.BigDecimal")
    }

    @Test // GROOVY-9463
    void testMethodNotFound2() {
        def err = shouldFail shell, '''
            @CompileStatic
            void test() {
                Function<String,String> reference = String::toLowerCaseX
            }
        '''
        assert err.message.contains("Failed to find class method 'toLowerCaseX(java.lang.String)' or instance method 'toLowerCaseX()' for the type: java.lang.String")
    }

    @Test // GROOVY-10813, GROOVY-10858, GROOVY-11363
    void testMethodSelection() {
        for (spec in ['', '<?>', '<Object>', '<? extends Object>', '<? super String>']) {
            assertScript shell, """
                @CompileStatic
                void test() {
                    Consumer$spec c = this::print // overloads in Script and DefaultGroovyMethods
                    c.accept('hello world!')
                }

                test()
            """
        }
        for (spec in ['', '<?,?>', '<?,Object>', '<?,? extends Object>', '<?,? super String>']) {
            assertScript shell, """
                @CompileStatic
                void test() {
                    BiConsumer$spec c = Object::print
                    c.accept(this, 'hello world!')
                }

                test()
            """
        }
        assertScript shell, '''
            @CompileStatic
            void test() {
                BiConsumer<Script,?> c = Script::print
                c.accept(this, 'hello world!')
            }

            test()
        '''
        assertScript shell, '''
            @CompileStatic
            void test() {
                Supplier<String> s = 'x'::toString
                def result = s.get()
                assert result == 'x'
            }

            test()
        '''
        assertScript shell, '''
            @CompileStatic
            void test() {
                Supplier<String> s = 0::toString
                def result = s.get()
                assert result == '0'
            }

            test()
        '''
        def err = shouldFail shell, '''
            @CompileStatic
            void test() {
                Supplier<String> s = Object::toString // all options require an object
            }
        '''
        assert err.message.contains("Failed to find class method 'toString()' for the type: java.lang.Object")
    }

    @Test // GROOVY-10859
    void testDynamicMethodSelection() {
        for (tag in ['@TypeChecked', '@CompileStatic', '@CompileDynamic']) {
            assertScript shell, """
                $tag
                void test() {
                    def result = [[]].stream().flatMap(List::stream).toList()
                    assert result.isEmpty()
                }

                test()
            """
        }
    }

    @Test // GROOVY-10904
    void testPropertyMethodLocation() {
        for (tag in ['@TypeChecked', '@CompileStatic', '@CompileDynamic']) {
            assertScript shell, """
                $tag
                class Test {
                    static class Profile {
                        String foo, bar
                    }

                    Map<String, Profile> profiles = [new Profile()].stream()
                        .collect(Collectors.toMap(Profile::getFoo, Function.identity()))

                    static main(args) {
                        assert this.newInstance().getProfiles().size() == 1
                    }
                }
            """
        }
    }

    @Test // GROOVY-10742, GROOVY-10858
    void testIncompatibleReturnType() {
        def err = shouldFail shell, '''
            void foo(bar) {
            }
            @CompileStatic
            void test() {
                Function<Object,String> f = this::foo
            }
        '''
        assert err =~ /Invalid return type: void is not convertible to java.lang.String/

        err = shouldFail shell, '''
            @CompileStatic
            void test() {
                Function<Object,Number> f = Object::toString
            }
        '''
        assert err =~ /Invalid return type: java.lang.String is not convertible to java.lang.Number/

        err = shouldFail shell, '''
            def m(Function<Object,Number> f) {
            }
            @CompileStatic
            void test() {
                m(Object::toString)
            }
        '''
        assert err =~ /Invalid return type: java.lang.String is not convertible to java.lang.Number/
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

    @Test // GROOVY-11254
    void testLocalFunctionalInterface() {
        assertScript shell, '''
            class C { String s }
            @FunctionalInterface
            interface I<T> { T m(C c) }

            @CompileStatic
            def test(I<String> i_string) {
                I<String> i = i_string::m
                i.m(new C(s:'something'))
            }

            String result = test(c -> c.s)
            assert result == 'something'
        '''
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

    @Test // GROOVY-11301
    void testInnerClassPrivateMethodReference() {
        def script = '''
            class C {
                static class D {
                    private static String m() { 'D' }
                }
                @CompileStatic
                static main(args) {
                    Supplier<String> str = D::m
                    assert str.get() == 'D'
                }
            }
        '''
        if (Runtime.version().feature() < 15) {
            shouldFail(shell, IllegalAccessError, script)
        } else {
            assertScript(shell, script)
        }
    }
}
