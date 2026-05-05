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

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

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

    // GROOVY-10047
    @Test // class::instanceMethod
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
        assert err.message =~ /Invalid receiver type: java.lang.Integer is not compatible with java.lang.String/
    }

    // GROOVY-9814
    @Test // class::instanceMethod
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

    // GROOVY-9813
    @Test // class::instanceMethod
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

    // GROOVY-9853
    @Test // class::instanceMethod
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

    // GROOVY-10054, GROOVY-10734
    @Test // class::instanceMethod
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

    // GROOVY-9803
    @Test // class::instanceMethod
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

    // GROOVY-11241
    @Test // class::instanceMethod
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

    // GROOVY-11259
    @Test // class::instanceMethod
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

    // GROOVY-11683
    @Test // class::instanceMethod
    void testFunctionCI12() {
        assertScript shell, '''
            import java.util.stream.Stream

            @CompileStatic
            List<Integer> test(List<String> strings) {
                Stream.of(strings).flatMap(List::stream).map(Integer::valueOf).toList()
            }

            def numbers = test(['1','2','3'])
            assert numbers.size() == 3
            assert numbers == [1,2,3]
        '''
    }

    // GROOVY-9974
    @Test // class::instanceMethod
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

    // GROOVY-11051
    @Test // class::instanceMethod
    void testPredicateCI2() {
        [['null','Empty'],['new Object()','Present']].each { value, which ->
            assertScript """import java.util.concurrent.atomic.AtomicReference
                def opt = new AtomicReference<Object>($value).stream()
                             .filter(AtomicReference::get).findFirst()
                assert opt.is${which}()
            """
        }
    }

    // GROOVY-10791
    @Test // class::instanceMethod
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

    // GROOVY-10974
    @Test // instance::instanceMethod
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

    // GROOVY-10933
    @Test // instance::instanceMethod
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

    // GROOVY-11020
    @Test // instance::instanceMethod
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

    // GROOVY-11068
    @Test // instance::instanceMethod
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

    // GROOVY-11068
    @Test // instance::instanceMethod
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

    // GROOVY-9813
    @Test // instance::instanceMethod
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

    // GROOVY-10653
    @Test // instance::instanceMethod
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

    // GROOVY-10653
    @Test // instance::instanceGroovyMethod
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

    // GROOVY-10972
    @Test // instance::instanceMethod
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

    // GROOVY-11364
    @Test // instance::instanceMethod
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

    // GROOVY-10057
    @Test // instance::instanceMethod
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

    // GROOVY-10994
    @Test // instance::instanceMethod
    void testPredicateII2() {
        assertScript shell, '''
            @CompileStatic
            def <T> void test(List<T> list) {
                Predicate<? super T> p = list::add
            }

            test([])
        '''
    }

    // GROOVY-10975
    @Test // instance::instanceMethod
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

    // GROOVY-11026
    @Test // instance::instanceMethod
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

    // GROOVY-11669
    @Test // class::classMethod
    void testFunctionCC1() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                [1,2,3].stream().map(Number::cast).collect(Collectors.toList())
            }

            test()
        '''
    }

    // GROOVY-11669
    @Test // class::classMethod
    void testFunctionCC2() {
        def err = shouldFail shell, '''
            @CompileStatic
            void test() {
                [1,2,3].stream().map(String::cast).collect(Collectors.toList())
            }

            test()
        '''
        assert err.message =~ /Cannot cast java.lang.Integer to java.lang.String/
    }

    @Test // class::new
    void testFunctionCN1() {
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

    // GROOVY-10033
    @Test // class::new
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

    // GROOVY-10033
    @Test // class::new
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

    // GROOVY-10930
    @Test // class::new
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
        assert err.message =~ /Cannot find matching constructor Foo\(java.lang.String\)/
    }

    // GROOVY-10971
    @Test // class::new
    void testFunctionCN6() {
        assertScript shell, '''
            class Foo {
                Foo(String s) {
                }
            }

            @CompileStatic
            void test() {
                Collectors.groupingBy(Foo::new) // Cannot find matching constructor Foo(Object)
            }

            test()
        '''
    }

    // GROOVY-11001
    @Test // class::new
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

    // GROOVY-11385
    @Test // class::new
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
        assert err.message =~ /Cannot instantiate the type A/

        for (op in ['::','.&']) {
            err = shouldFail shell, """
                @CompileStatic
                void test() {
                    Supplier<Number> s = Number${op}new
                }

                test()
            """
            assert err.message =~ /Cannot instantiate the type java.lang.Number/
        }
    }

    // GROOVY-11440
    @Test // arrayClass::new
    void testFunctionCN9() {
        def err = shouldFail shell, '''
            @CompileStatic
            void test() {
                Function<Object,Integer[]> f = Integer[]::new
                Integer[] array = f.apply(new Object())
            }

            test()
        '''
        assert err.message =~ /Cannot call java.lang.Integer\[\]#<init>\(int\) with arguments /
    }

    @Test // arrayClass::new
    void testIntFunctionCN() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                IntFunction<Integer[]> f = Integer[]::new;
                def result = [1, 2, 3].stream().toArray(f)
                assert result == new Integer[] { 1, 2, 3 }
            }

            test()
        '''
    }

    @Test // class::new
    void testSerializableConstructorReference() {
        assertScript shell, '''
            import java.io.ByteArrayInputStream
            import java.io.ByteArrayOutputStream
            import java.io.Serializable

            @CompileStatic
            class C {
                interface SerFunc<I, O> extends Serializable, Function<I, O> {}

                static class Box {
                    final String value

                    Box(String value) {
                        this.value = value.trim()
                    }
                }

                static SerFunc<String, Box> create() {
                    Box::new
                }

                static byte[] serialize(Serializable value) {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream { it.writeObject(value) }
                    out.toByteArray()
                }

                static <T> T deserialize(byte[] bytes) {
                    new ByteArrayInputStream(bytes).withObjectInputStream(C.classLoader) {
                        (T) it.readObject()
                    }
                }
            }

            assert C.declaredMethods.count { it.name == '$deserializeLambda$' } == 1

            C.SerFunc<String, C.Box> factory = C.deserialize(C.serialize(C.create()))
            assert factory instanceof Serializable
            assert factory.apply('  ok  ').value == 'ok'
        '''
    }

    @Test // arrayClass::new
    void testSerializableArrayConstructorReference() {
        assertScript shell, '''
            import java.io.ByteArrayInputStream
            import java.io.ByteArrayOutputStream
            import java.io.Serializable

            @CompileStatic
            class C {
                interface SerIntFunc<T> extends Serializable, IntFunction<T> {}

                static SerIntFunc<String[]> create() {
                    String[]::new
                }

                static byte[] serialize(Serializable value) {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream { it.writeObject(value) }
                    out.toByteArray()
                }

                static <T> T deserialize(byte[] bytes) {
                    new ByteArrayInputStream(bytes).withObjectInputStream(C.classLoader) {
                        (T) it.readObject()
                    }
                }
            }

            C.SerIntFunc<String[]> factory = C.deserialize(C.serialize(C.create()))
            String[] values = factory.apply(3)
            assert values.length == 3
            assert values.toList() == [null, null, null]
        '''
    }

    @Test
    void testSerializableConstructorReferencesShareDeserializeDispatcherWithLambdas() {
        assertScript shell, '''
            import java.io.ByteArrayInputStream
            import java.io.ByteArrayOutputStream
            import java.io.Serializable

            @CompileStatic
            class C {
                interface SerFunc<I, O> extends Serializable, Function<I, O> {}
                interface SerIntFunc<T> extends Serializable, IntFunction<T> {}

                static class Box {
                    final String value

                    Box(String value) {
                        this.value = value
                    }
                }

                static SerFunc<String, Box> createConstructorReference() {
                    Box::new
                }

                static SerIntFunc<Box[]> createArrayConstructorReference() {
                    Box[]::new
                }

                static SerFunc<Integer, String> createLambda() {
                    (Integer i) -> 'L' + i
                }

                static byte[] serialize(Serializable value) {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream { it.writeObject(value) }
                    out.toByteArray()
                }

                static <T> T deserialize(byte[] bytes) {
                    new ByteArrayInputStream(bytes).withObjectInputStream(C.classLoader) {
                        (T) it.readObject()
                    }
                }
            }

            assert C.declaredMethods.count { it.name == '$deserializeLambda$' } == 1
            assert C.declaredMethods.findAll { it.name.startsWith('$deserializeLambda') && it.name != '$deserializeLambda$' }
                .every { java.lang.reflect.Modifier.isPrivate(it.modifiers) && java.lang.reflect.Modifier.isStatic(it.modifiers) }

            C.SerFunc<String, C.Box> ctor = C.deserialize(C.serialize(C.createConstructorReference()))
            C.SerIntFunc<C.Box[]> arrayCtor = C.deserialize(C.serialize(C.createArrayConstructorReference()))
            C.SerFunc<Integer, String> lambda = C.deserialize(C.serialize(C.createLambda()))

            assert ctor.apply('box').value == 'box'
            assert arrayCtor.apply(2).length == 2
            assert lambda.apply(4) == 'L4'
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

    // GROOVY-9799
    @Test // class::staticMethod
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

    // GROOVY-9813
    @Test // class::staticMethod
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

    // GROOVY-10807
    @Test // class::staticMethod
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

    // GROOVY-10807
    @Test // class::staticMethod
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

    // GROOVY-11009
    @Test // class::staticMethod
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

    // GROOVY-11683
    @Test // class::instanceGroovyMethod
    void testFunctionCI_DGM2() {
        String head = '''\
            import static org.codehaus.groovy.control.CompilePhase.*
            import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*

            @CompileStatic
            void test(Iterable<String> iterable) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    Object type = node.getNodeMetaData(INFERRED_TYPE)
                    assert type.toString(false) == 'java.util.Optional<java.util.Collection<java.lang.String>>'
                })
        '''
        String tail = '''\
            }

            test()
        '''

        assertScript shell, head + '''
            def optional = Optional.ofNullable(iterable).map(Iterable::asCollection)
            assert optional.isEmpty()
        ''' + tail

        assertScript shell, head + '''
            def optional = Optional.empty().map(Iterable<String>::asCollection)
            assert optional.isEmpty()
        ''' + tail
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

    // GROOVY-9463
    @Test
    void testMethodNotFound2() {
        def err = shouldFail shell, '''
            @CompileStatic
            void test() {
                Function<String,String> reference = String::toLowerCaseX
            }
        '''
        assert err.message.contains("Failed to find class method 'toLowerCaseX(java.lang.String)' or instance method 'toLowerCaseX()' for the type: java.lang.String")
    }

    // GROOVY-10813, GROOVY-10858, GROOVY-11363
    @Test
    void testMethodSelection() {
        for (spec in ['', '<Object>', '<? super String>']) {
            assertScript shell, """
                @CompileStatic
                void test() {
                    Consumer$spec c = this::print // overloads in Script and DefaultGroovyMethods
                    c.accept('hello world!')
                }

                test()
            """
        }
        for (spec in ['', '<Object,Object>', '<Object,? super String>']) {
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
                BiConsumer<Script,Object> c = Script::print
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
        assertScript shell, '''
            @CompileStatic
            void test() {
                Supplier<String> s = Object::toString
                def result = s.get()
                assert result == 'class java.lang.Object'
            }

            test()
        '''
        def err = shouldFail shell, '''
            @CompileStatic
            void test() {
                BinaryOperator<String> s = Object::toString
            }
        '''
        assert err.message.contains("Failed to find class method 'toString(java.lang.String,java.lang.String)' or instance method 'toString(java.lang.String)' for the type: java.lang.Object")
    }

    // GROOVY-10859
    @ParameterizedTest
    @ValueSource(strings=['@CompileDynamic','@TypeChecked','@CompileStatic'])
    void testDynamicMethodSelection(String tag) {
        assertScript shell, """
            $tag
            void test() {
                def result = [[]].stream().flatMap(List::stream).toList()
                assert result.isEmpty()
            }

            test()
        """
    }

    // GROOVY-10904
    @ParameterizedTest
    @ValueSource(strings=['@CompileDynamic','@TypeChecked','@CompileStatic'])
    void testPropertyMethodLocation(String tag) {
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

    // GROOVY-10742, GROOVY-10858
    @Test
    void testIncompatibleReturnType() {
        def err = shouldFail shell, '''
            void foo(bar) {
            }
            @CompileStatic
            void test() {
                Function<Object,String> f = this::foo
            }
        '''
        assert err.message =~ /Invalid return type: void is not convertible to java.lang.String/

        err = shouldFail shell, '''
            @CompileStatic
            void test() {
                Function<Object,Number> f = Object::toString
            }
        '''
        assert err.message =~ /Invalid return type: java.lang.String is not convertible to java.lang.Number/

        err = shouldFail shell, '''
            def m(Function<Object,Number> f) {
            }
            @CompileStatic
            void test() {
                m(Object::toString)
            }
        '''
        assert err.message =~ /Invalid return type: java.lang.String is not convertible to java.lang.Number/
    }

    // GROOVY-10269
    @Test
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
        assert err.message =~ /Argument is a method reference, but parameter type 'java.lang.Object' is not a functional interface/
    }

    // GROOVY-10336
    @Test
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
        assert err.message =~ /Argument is a method reference, but parameter type 'java.lang.Object' is not a functional interface/
    }

    // GROOVY-10979
    @Test
    void testNotFunctionalInterface3() {
        def err = shouldFail shell, '''
            Integer m(String x) {
                return 1
            }
            @CompileStatic
            void test() {
                java.util.stream.Stream<Number>                                          x = null
                BiFunction<Function<String, Integer>, Number, Function<String, Integer>> y = null
                BinaryOperator<Function<String, Integer>>                                z = null
                // reduce number(s) to string-to-integer functions
                x.<Function<String, Integer>>reduce(this::m, y, z)
                x.reduce(this::m, y, z)
                x.reduce((s) -> 1, y, z)
            }
        '''
        assert err.message =~ /Argument is a method reference, but parameter type 'U' is not a functional interface/
    }

    // GROOVY-11254
    @Test
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

    @Test
    void testSerializableNonCapturingMethodReference() {
        assertScript shell, '''
            import java.io.ByteArrayInputStream
            import java.io.ByteArrayOutputStream
            import java.io.Serializable

            @CompileStatic
            class C {
                interface SerFunc<I, O> extends Serializable, Function<I, O> {}

                static SerFunc<Integer, String> create() {
                    Integer::toString
                }

                static byte[] serialize(Serializable value) {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream { it.writeObject(value) }
                    out.toByteArray()
                }

                static <T> T deserialize(byte[] bytes) {
                    new ByteArrayInputStream(bytes).withObjectInputStream(C.classLoader) {
                        (T) it.readObject()
                    }
                }
            }

            assert C.declaredMethods.count { it.name == '$deserializeLambda$' } == 1

            C.SerFunc<Integer, String> fn = C.deserialize(C.serialize(C.create()))
            assert fn instanceof Serializable
            assert fn.apply(7) == '7'
        '''
    }

    @Test
    void testSerializableCapturingMethodReference() {
        assertScript shell, '''
            import java.io.ByteArrayInputStream
            import java.io.ByteArrayOutputStream
            import java.io.Serializable

            @CompileStatic
            class C {
                interface SerSupplier<T> extends Serializable, Supplier<T> {}

                private final String text

                C(String text) {
                    this.text = text
                }

                SerSupplier<String> create() {
                    text::trim
                }

                static byte[] serialize(Serializable value) {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream { it.writeObject(value) }
                    out.toByteArray()
                }

                static <T> T deserialize(byte[] bytes) {
                    new ByteArrayInputStream(bytes).withObjectInputStream(C.classLoader) {
                        (T) it.readObject()
                    }
                }
            }

            C.SerSupplier<String> supplier = C.deserialize(C.serialize(new C('  answer  ').create()))
            assert supplier instanceof Serializable
            assert supplier.get() == 'answer'
        '''
    }

    @Test
    void testSerializableMethodReferencesShareDeserializeDispatcherWithLambdas() {
        assertScript shell, '''
            import java.io.ByteArrayInputStream
            import java.io.ByteArrayOutputStream
            import java.io.Serializable

            @CompileStatic
            class C {
                interface SerFunc<I, O> extends Serializable, Function<I, O> {}
                interface SerSupplier<T> extends Serializable, Supplier<T> {}

                private final String text

                C(String text) {
                    this.text = text
                }

                static SerFunc<Integer, String> createMethodReference() {
                    Integer::toString
                }

                static SerFunc<Integer, String> createLambda() {
                    (Integer i) -> 'L' + i
                }

                SerSupplier<String> createBoundMethodReference() {
                    text::trim
                }

                static byte[] serialize(Serializable value) {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream { it.writeObject(value) }
                    out.toByteArray()
                }

                static <T> T deserialize(byte[] bytes) {
                    new ByteArrayInputStream(bytes).withObjectInputStream(C.classLoader) {
                        (T) it.readObject()
                    }
                }
            }

            assert C.declaredMethods.count { it.name == '$deserializeLambda$' } == 1
            assert C.declaredMethods.findAll { it.name.startsWith('$deserializeLambda') && it.name != '$deserializeLambda$' }
                .every { java.lang.reflect.Modifier.isPrivate(it.modifiers) && java.lang.reflect.Modifier.isStatic(it.modifiers) }

            C.SerFunc<Integer, String> methodRef = C.deserialize(C.serialize(C.createMethodReference()))
            C.SerFunc<Integer, String> lambda = C.deserialize(C.serialize(C.createLambda()))
            C.SerSupplier<String> bound = C.deserialize(C.serialize(new C('  x  ').createBoundMethodReference()))

            assert methodRef.apply(3) == '3'
            assert lambda.apply(4) == 'L4'
            assert bound.get() == 'x'
        '''
    }

    @Test
    void testDeserializeDispatcherReturnsMatchingMethodReferenceAndLambdaBeforeFallback() {
        assertScript shell, '''
            import java.io.Serializable
            import java.lang.invoke.SerializedLambda

            @CompileStatic
            class C {
                interface SerFunc<I, O> extends Serializable, Function<I, O> {}

                static SerFunc<Integer, String> createMethodReference() {
                    Integer::toString
                }

                static SerFunc<Integer, String> createLambda() {
                    (Integer i) -> 'L' + i
                }

                @CompileDynamic
                static SerializedLambda serialized(Serializable value) {
                    def writeReplace = value.class.getDeclaredMethod('writeReplace')
                    writeReplace.accessible = true
                    (SerializedLambda) writeReplace.invoke(value)
                }
            }

            def dispatcher = C.getDeclaredMethod('$deserializeLambda$', SerializedLambda)
            dispatcher.accessible = true

            C.SerFunc<Integer, String> methodRef =
                (C.SerFunc<Integer, String>) dispatcher.invoke(null, C.serialized(C.createMethodReference()))
            C.SerFunc<Integer, String> lambda =
                (C.SerFunc<Integer, String>) dispatcher.invoke(null, C.serialized(C.createLambda()))

            assert methodRef.apply(3) == '3'
            assert lambda.apply(3) == 'L3'
        '''
    }

    @Test
    void testDeserializeDispatcherRejectsWrongCapturingClassEvenWhenOtherSerializedFieldsMatch() {
        assertScript shell, '''
            import java.io.Serializable
            import java.lang.invoke.SerializedLambda

            @CompileStatic
            class C {
                interface SerFunc<I, O> extends Serializable, Function<I, O> {}

                static SerFunc<Integer, String> createMethodReference() {
                    Integer::toString
                }

                static SerFunc<Integer, String> createLambda() {
                    (Integer i) -> 'L' + i
                }

                @CompileDynamic
                static SerializedLambda serialized(Serializable value) {
                    def writeReplace = value.class.getDeclaredMethod('writeReplace')
                    writeReplace.accessible = true
                    (SerializedLambda) writeReplace.invoke(value)
                }

                @CompileDynamic
                static SerializedLambda withCapturingClass(SerializedLambda serialized, Class capturingClass) {
                    new SerializedLambda(
                        capturingClass,
                        serialized.functionalInterfaceClass,
                        serialized.functionalInterfaceMethodName,
                        serialized.functionalInterfaceMethodSignature,
                        serialized.implMethodKind,
                        serialized.implClass,
                        serialized.implMethodName,
                        serialized.implMethodSignature,
                        serialized.instantiatedMethodType,
                        (0..<serialized.capturedArgCount).collect { serialized.getCapturedArg(it) } as Object[]
                    )
                }
            }

            def dispatcher = C.getDeclaredMethod('$deserializeLambda$', SerializedLambda)
            dispatcher.accessible = true

            def assertInvalid = { SerializedLambda serialized ->
                def err
                try {
                    dispatcher.invoke(null, serialized)
                    assert false: 'dispatcher invocation should fail'
                } catch (java.lang.reflect.InvocationTargetException e) {
                    err = e
                }
                assert err.cause instanceof IllegalArgumentException
                assert err.cause.message == 'Invalid serialized functional interface'
            }

            assertInvalid(C.withCapturingClass(C.serialized(C.createMethodReference()), String))
            assertInvalid(C.withCapturingClass(C.serialized(C.createLambda()), Integer))
        '''
    }

    @Test
    void testDeserializeDispatcherReportsClearErrorForMismatchedSerializedForm() {
        assertScript shell, '''
            import java.lang.invoke.MethodHandleInfo
            import java.lang.invoke.SerializedLambda

            @CompileStatic
            class C {
                interface SerFunc<I, O> extends Serializable, Function<I, O> {}

                static SerFunc<Integer, String> create() {
                    Integer::toString
                }
            }

            def dispatcher = C.getDeclaredMethod('$deserializeLambda$', SerializedLambda)
            dispatcher.accessible = true

            def serialized = new SerializedLambda(
                C,
                'java/util/function/Function',
                'apply',
                '(Ljava/lang/Object;)Ljava/lang/Object;',
                MethodHandleInfo.REF_invokeStatic,
                'java/lang/Integer',
                'toString',
                '(I)Ljava/lang/String;',
                '(Ljava/lang/Integer;)Ljava/lang/String;',
                [] as Object[]
            )

            def err
            try {
                dispatcher.invoke(null, serialized)
                assert false: 'dispatcher invocation should fail'
            } catch (java.lang.reflect.InvocationTargetException e) {
                err = e
            }
            assert err.cause instanceof IllegalArgumentException
            assert err.cause.message == 'Invalid serialized functional interface'
        '''
    }

    // GROOVY-11467
    @Test
    void testSuperInterfaceMethodReference() {
        assertScript shell, '''
            interface A { int m() }
            interface B extends A { }
            class C implements B { int m() { 42 } }

            @CompileStatic
            class D {
                B b = new C()
                void test() {
                    IntSupplier s = b::m
                    assert s.getAsInt() == 42
                }
            }

            new D().test()
        '''
    }

    // GROOVY-10635
    @Test
    void testRecordComponentMethodReference() {
        assertScript shell, '''
            record Bar(String name) {
            }
            def bars = [new Bar(name: 'A'), new Bar(name: 'B')]
            assert bars.stream().map(Bar::name).map(String::toLowerCase).toList() == ['a', 'b']
        '''
    }

    // GROOVY-11618
    @Test
    void testRecordComponentMethodReference2() {
        assertScript shell, '''
            class C {
                record R(String x) {
                }
                @CompileStatic m() {
                    def list = [new R('x')]
                    def stream = list.stream().map(R::x)
                    def string = stream.collect(Collectors.joining())

                    assert string == 'x'
                }
            }
            new C().m()
        '''
    }

    // GROOVY-11301
    @Test
    void testInnerClassPrivateMethodReference() {
        assertScript shell, '''
            @CompileStatic
            class C {
                static class D {
                    private static String m() { 'D' }
                }
                static main(args) {
                    Supplier<String> str = D::m
                    assert str.get() == 'D'
                }
            }
        '''
    }

    // GROOVY-11365
    @Test
    void testInnerClassProtectedMethodReference() {
        assertScript shell, '''package p
            abstract class A<E> {
                protected E op(E e) { result = e }
                protected E result
            }

            true
        '''
        assertScript shell, '''
            @CompileStatic
            class C extends p.A<Integer> {
                void test() {
                    def runnable = { ->
                        Consumer<Integer> consumer = this::op
                        consumer.accept(42) // IllegalAccessError
                    }
                    runnable.run()
                    assert result == Integer.valueOf(42)
                }
            }

            new C().test()
        '''
    }

    @Test
    void testDoubleSupplierMethodReference() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                DoubleSupplier s = Math::random
                assert s.getAsDouble() >= 0.0d && s.getAsDouble() < 1.0d
            }

            test()
        '''
    }

    @Test
    void testLongSupplierMethodReference() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                LongSupplier s = System::currentTimeMillis
                assert s.getAsLong() > 0L
            }

            test()
        '''
    }

    @Test
    void testBiPredicateFromMapPutBooleanPlaceholder() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Map<String, Boolean> m = [k: true]
                BiPredicate<String, Boolean> p = m::put
                assert p.test('k', false) == true  // old value was true
                assert p.test('k', true)  == false // old value was false
            }

            test()
        '''
    }

    @Test
    void testToDoubleFunctionFromMapGetDoublePlaceholder() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Map<String, Double> m = [pi: 3.14d, e: 2.71d]
                ToDoubleFunction<String> f = m::get
                assert Math.abs(f.applyAsDouble('pi') - 3.14d) < 1e-9d
                assert Math.abs(f.applyAsDouble('e')  - 2.71d) < 1e-9d
            }

            test()
        '''
    }

    @Test
    void testToLongFunctionFromMapGetLongPlaceholder() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Map<String, Long> m = [a: 100L, b: 200L]
                ToLongFunction<String> f = m::get
                assert f.applyAsLong('a') == 100L
                assert f.applyAsLong('b') == 200L
            }

            test()
        '''
    }

    @Test
    void testByteReturnSAMMethodReference() {
        assertScript shell, '''
            @FunctionalInterface interface ToByteFunc<T> { byte apply(T t) }

            @CompileStatic
            void test() {
                ToByteFunc<Number> f = Number::byteValue
                assert f.apply(65)  == (byte) 65
                assert f.apply(300) == (byte) 44  // 300 % 256 = 44
            }

            test()
        '''
    }

    @Test
    void testCharReturnSAMMethodReference() {
        assertScript shell, '''
            @FunctionalInterface interface ToCharFunc { char apply(String s, int i) }

            @CompileStatic
            void test() {
                ToCharFunc f = String::charAt
                assert f.apply('hello', 0) == 'h'
                assert f.apply('hello', 4) == 'o'
            }

            test()
        '''
    }

    @Test
    void testFloatReturnSAMMethodReference() {
        assertScript shell, '''
            @FunctionalInterface interface ToFloatFunc<T> { float apply(T t) }

            @CompileStatic
            void test() {
                ToFloatFunc<Number> f = Number::floatValue
                assert f.apply(3.14) == 3.14f
                assert f.apply(42)   == 42.0f
            }

            test()
        '''
    }

    @Test
    void testShortReturnSAMMethodReference() {
        assertScript shell, '''
            @FunctionalInterface interface ToShortFunc<T> { short apply(T t) }

            @CompileStatic
            void test() {
                ToShortFunc<Number> f = Number::shortValue
                assert f.apply(42)  == (short) 42
                assert f.apply(300) == (short) 300
            }

            test()
        '''
    }

    @Test
    void testVarargAdapterWhenLastSamParamNotAssignableToVarargArray() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                BiFunction<String, String, String> f = String::format
                assert f.apply('%s',  'hello') == 'hello'
                assert f.apply('[%s]', 'world') == '[world]'
            }

            test()
        '''
    }

    @Test
    void testVarargZeroArgSAMMethodReference() {
        assertScript shell, '''
            import java.util.Arrays

            @CompileStatic
            void test() {
                Supplier<List<Object>> s = Arrays::asList
                assert s.get() == []
            }

            test()
        '''
    }

    @Test
    void testVarargMultiArgSAMMethodReference() {
        assertScript shell, '''
            @FunctionalInterface
            interface StringFormatter { String format(String fmt, String a, String b) }

            @CompileStatic
            void test() {
                StringFormatter sf = String::format
                assert sf.format('%s and %s', 'foo', 'bar') == 'foo and bar'
            }

            test()
        '''
    }

    @Test
    void testBoundInstanceVarargExternalClassMethodReference() {
        assertScript shell, '''
            class StringJoiner {
                String join(String... parts) { parts.join('-') }
            }

            @CompileStatic
            void test() {
                StringJoiner joiner = new StringJoiner()
                // 0-arg SAM; join is vararg instance method on external class
                Supplier<String> s = joiner::join
                assert s.get() == ''
                assert new StringJoiner().join('a', 'b', 'c') == 'a-b-c'
            }

            test()
        '''
    }

    @Test
    void testVarargMethodReferenceInCurrentClass() {
        assertScript shell, '''
            @CompileStatic
            class C {
                String join(String... parts) { parts.join('-') }

                Supplier<String> createRef() {
                    this::join
                }
            }

            def result = new C().createRef().get()
            assert result == ''
        '''
    }

    @Test
    void testInstanceBoundToStaticMethodReference() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Integer num = 42
                ToIntFunction<String> f = num::parseInt
                assert f.applyAsInt('123') == 123
                assert f.applyAsInt('-7')  == -7
            }

            test()
        '''
    }

    @Test
    void testToByteFunctionFromMapGetBytePlaceholder() {
        assertScript shell, '''
            @FunctionalInterface interface ToByteFunc<K> { byte apply(K k) }

            @CompileStatic
            void test() {
                Map<String, Byte> m = [a: (byte) 1, b: (byte) 127]
                ToByteFunc<String> f = m::get
                assert f.apply('a') == (byte) 1
                assert f.apply('b') == (byte) 127
            }

            test()
        '''
    }

    @Test
    void testToCharFunctionFromMapGetCharPlaceholder() {
        assertScript shell, '''
            @FunctionalInterface interface ToCharFunc<K> { char apply(K k) }

            @CompileStatic
            void test() {
                Map<String, Character> m = [x: (char) 'A', y: (char) 'Z']
                ToCharFunc<String> f = m::get
                assert f.apply('x') == 'A'
                assert f.apply('y') == 'Z'
            }

            test()
        '''
    }

    @Test
    void testToFloatFunctionFromMapGetFloatPlaceholder() {
        assertScript shell, '''
            @FunctionalInterface interface ToFloatFunc<K> { float apply(K k) }

            @CompileStatic
            void test() {
                Map<String, Float> m = [pi: 3.14f, e: 2.71f]
                ToFloatFunc<String> f = m::get
                assert Math.abs(f.apply('pi') - 3.14f) < 1e-4f
                assert Math.abs(f.apply('e')  - 2.71f) < 1e-4f
            }

            test()
        '''
    }

    @Test
    void testToShortFunctionFromMapGetShortPlaceholder() {
        assertScript shell, '''
            @FunctionalInterface interface ToShortFunc<K> { short apply(K k) }

            @CompileStatic
            void test() {
                Map<String, Short> m = [lo: (short) 0, hi: (short) 100]
                ToShortFunc<String> f = m::get
                assert f.apply('lo') == (short) 0
                assert f.apply('hi') == (short) 100
            }

            test()
        '''
    }

    @Test
    void testTypeReferringInstanceVarargMethodReference() {
        assertScript shell, '''
            class Joiner {
                String join(String... parts) { parts.join('-') }
            }

            @FunctionalInterface interface JoinerBiFunc { String apply(Joiner j, String a, String b) }

            @CompileStatic
            void test() {
                JoinerBiFunc f = Joiner::join
                assert f.apply(new Joiner(), 'a', 'b') == 'a-b'
                assert f.apply(new Joiner(), 'x', 'y') == 'x-y'
            }

            test()
        '''
    }

    @Test
    void testVarargMethodRefNoAdapterWhenArrayParamCompatible() {
        assertScript shell, '''
            @FunctionalInterface interface StrFormatter { String fmt(String format, Object[] args) }

            @CompileStatic
            void test() {
                StrFormatter f = String::format
                assert f.fmt('%s=%d', ['answer', 42] as Object[]) == 'answer=42'
            }

            test()
        '''
    }

    @Test
    void testGenericTypeResolutionForMethodReference() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Function<List<String>, Integer> f = List::size
                assert f.apply(['a', 'b', 'c']) == 3
                assert f.apply([]) == 0
            }

            test()
        '''
    }

    @Test
    void testMethodRefReceiverTypeNarrowerThanSAMParam() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                ToIntFunction<String> f = String::length
                assert f.applyAsInt('hello') == 5
                assert f.applyAsInt('') == 0
            }
            test()
        '''
    }

    @Test
    void testInstanceBoundStaticVarargMethodRef() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                String fmt = "unused"
                BiFunction<String, String, String> f = fmt::format
                assert f.apply('Hello %s!', 'world') == 'Hello world!'
                assert f.apply('Hi %s', 'Groovy') == 'Hi Groovy'
            }
            test()
        '''
    }

    @Test
    void testMethodRefAssignedToClosureType() {
        assertScript shell, '''
            @CompileStatic
            void test() {
                Closure<?> fn = String::length
                assert fn.call('hello') == 5
                assert fn.call('') == 0
            }
            test()
        '''
    }

    @Nested
    class NativeMethodReferenceBytecodeTest extends AbstractBytecodeTestCase {
        @Test
        void testClassReferringInstanceMethodUsesCaptureFreeInvokeDynamic() {
            def bytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', '''
                @CompileStatic
                class C {
                    static ToIntFunction<String> create() {
                        String::length
                    }
                }
            ''')

            assertUsesInvokeDynamicFactory(
                bytecode,
                'INVOKEDYNAMIC applyAsInt()Ljava/util/function/ToIntFunction;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'java/lang/String.length()I'
            )
        }

        @Test
        void testBoundInstanceMethodCapturesReceiverInFactoryDescriptor() {
            def bytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', '''
                @CompileStatic
                class C {
                    static Supplier<String> create() {
                        String text = '  ok  '
                        text::trim
                    }
                }
            ''')

            assertUsesInvokeDynamicFactory(
                bytecode,
                'INVOKEDYNAMIC get(Ljava/lang/String;)Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'java/lang/String.trim()Ljava/lang/String;'
            )
            assert !bytecode.hasSequence(['INVOKEDYNAMIC get()Ljava/util/function/Supplier;'])
        }

        @Test
        void testInstanceBoundToStaticMethodDropsSuperfluousReceiverCapture() {
            def bytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', '''
                @CompileStatic
                class C {
                    static ToIntFunction<String> create() {
                        Integer number = 42
                        number::parseInt
                    }
                }
            ''')

            assertUsesInvokeDynamicFactory(
                bytecode,
                'INVOKEDYNAMIC applyAsInt()Ljava/util/function/ToIntFunction;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'java/lang/Integer.parseInt(Ljava/lang/String;)I'
            )
            assert !bytecode.hasSequence(['INVOKEDYNAMIC applyAsInt(Ljava/lang/Integer;)Ljava/util/function/ToIntFunction;'])
        }

        @Test
        void testClassReferringStaticMethodUsesDirectBootstrapTarget() {
            def bytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', '''
                @CompileStatic
                class C {
                    static IntUnaryOperator create() {
                        Math::abs
                    }
                }
            ''')

            assertUsesInvokeDynamicFactory(
                bytecode,
                'INVOKEDYNAMIC applyAsInt()Ljava/util/function/IntUnaryOperator;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'java/lang/Math.abs(I)I'
            )
        }

        @Test
        void testConstructorReferenceUsesSyntheticFactoryMethodAsBootstrapTarget() {
            def script = '''
                @CompileStatic
                class C {
                    static class Box {
                        final String value
                        Box(String value) {
                            this.value = value
                        }
                    }

                    static Function<String, Box> create() {
                        Box::new
                    }
                }
            '''

            def helperBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'ctorRef$create$0', script)
            assert helperBytecode.hasSequence([
                'private final static synthetic ctorRef$create$0(Ljava/lang/String;)LC$Box;',
                'LDC LC$Box;.class',
                'INVOKEDYNAMIC init(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;',
                '"<init>"',
                'INVOKEDYNAMIC cast(Ljava/lang/Object;)LC$Box;',
                'ARETURN'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assertUsesInvokeDynamicFactory(
                outerBytecode,
                'INVOKEDYNAMIC apply()Ljava/util/function/Function;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C.ctorRef$create$0(Ljava/lang/String;)LC$Box;'
            )
        }

        @Test
        void testArrayConstructorReferenceUsesSyntheticArrayFactoryMethod() {
            def script = '''
                @CompileStatic
                class C {
                    static IntFunction<String[]> create() {
                        String[]::new
                    }
                }
            '''

            def helperBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'ctorRef$create$0', script)
            assert helperBytecode.hasSequence([
                'private final static synthetic ctorRef$create$0(I)[Ljava/lang/String;',
                'ANEWARRAY java/lang/String',
                'ARETURN'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assertUsesInvokeDynamicFactory(
                outerBytecode,
                'INVOKEDYNAMIC apply()Ljava/util/function/IntFunction;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C.ctorRef$create$0(I)[Ljava/lang/String;'
            )
        }

        @Test
        void testSerializableMethodReferenceUsesAltMetafactoryAndDeserializeDispatcher() {
            def script = '''
                @CompileStatic
                class C {
                    interface SerFunc<I, O> extends Serializable, Function<I, O> {}

                    static SerFunc<String, String> create() {
                        String::trim
                    }
                }
            '''

            def createBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assertUsesInvokeDynamicFactory(
                createBytecode,
                'INVOKEDYNAMIC apply()LC$SerFunc;',
                'java/lang/invoke/LambdaMetafactory.altMetafactory',
                'java/lang/String.trim()Ljava/lang/String;'
            )
            assert createBytecode.hasSequence(['CHECKCAST java/io/Serializable'])

            def dispatcherBytecode = compileStaticBytecode(classNamePattern: 'C', method: '$deserializeLambda$', script)
            assert dispatcherBytecode.hasSequence(['INVOKESTATIC C.$deserializeLambda_methodref$'])
        }

        @Test
        void testVarargMethodReferenceUsesSyntheticAdapterInsteadOfFallback() {
            def script = '''
                @CompileStatic
                class C {
                    static Supplier<List> create() {
                        Arrays::asList
                    }
                }
            '''

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assertUsesInvokeDynamicFactory(
                outerBytecode,
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C.adapt$Arrays$asList$'
            )
        }

        private static void assertUsesInvokeDynamicFactory(final bytecode, final String invokedynamicInstruction, final String bootstrapMethod, final String implementationTarget) {
            assert bytecode.hasSequence([
                invokedynamicInstruction,
                bootstrapMethod,
                implementationTarget
            ])
            assertNoMethodPointerFallback(bytecode)
        }

        private static void assertNoMethodPointerFallback(final bytecode) {
            assert !bytecode.hasSequence(['INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.getMethodPointer'])
        }

        private compileStaticBytecode(final Map options = [:], final String script) {
            compile(options, COMMON_IMPORTS + script)
        }

        private static final String COMMON_IMPORTS = '''\
            import groovy.transform.CompileStatic
            import java.io.Serializable
            import java.util.Arrays
            import java.util.function.*
            '''
    }
}
