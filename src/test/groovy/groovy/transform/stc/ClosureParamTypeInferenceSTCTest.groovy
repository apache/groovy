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
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Unit tests for static type checking : closure parameter type inference.
 */
class ClosureParamTypeInferenceSTCTest extends StaticTypeCheckingTestCase {

    @Override
    void configure() {
        config.addCompilationCustomizers(
            new ImportCustomizer().addStarImports('groovy.transform', 'groovy.transform.stc', 'java.util.function')
        )
    }

    // GROOVY-6022
    @Test
    void testSimpleType1() {
        assertScript '''
            class Item {
                int x
            }

            void m(Item item, @ClosureParams(value=SimpleType, options="Item") Closure... closures) {
                closures*.call(item)
            }

            m(new Item(), { item -> item.x }, { it.x })
        '''
    }

    // GROOVY-6602
    @Test
    void testSimpleType2() {
        shouldFailWithMessages '''
            void foo(@ClosureParams(value=SimpleType, options="java.lang.Number") Closure c) {
                c.call(4.5)
            }
            foo { Integer i -> println i }
        ''',
        'Expected type java.lang.Number for closure parameter: i'
    }

    // GROOVY-6603
    @Test
    void testSimpleType3() {
        shouldFailWithMessages '''
            void foo(@ClosureParams(value=SimpleType, options="java.lang.Number") Closure c) {
                c.call("x")
            }
        ''',
        'Cannot call closure that accepts [java.lang.Number] with [java.lang.String]'
    }

    @Test
    void testFirstParam1() {
        assertScript '''
            def <T> T foo(T t, @ClosureParams(FirstParam) Closure c) { c.call(t) }
            assert foo('a') { it.toUpperCase() } == 'A'
        '''
    }

    // GROOVY-6729
    @Test
    void testFirstParam2() {
        assertScript '''
            static <T> List<T> foo(List<T> list, @ClosureParams(FirstParam.FirstGenericType) Closure c) {
                list.each {
                    c.call(it)
                }
                return list
            }
            foo(["a","b","c"]) { s ->
                s.toUpperCase() // Cannot find matching method java.lang.Object#toUpperCase()
            }
        '''
    }

    @Test
    void testFromStringWithBasicType() {
        assertScript '''
            void foo(@ClosureParams(value=FromString, options="java.lang.String") Closure c) { c.call('foo') }
            foo { String str -> println str.toUpperCase()}
        '''

        shouldFailWithMessages '''
            void foo(@ClosureParams(value=FromString, options="java.lang.String") Closure c) { c.call('foo') }
            foo { Date str -> println str}
        ''',
        'Expected type java.lang.String for closure parameter: str'
    }

    @Test
    void testFromStringWithGenericType() {
        assertScript '''
            void foo(@ClosureParams(value=FromString, options="java.util.List<java.lang.String>") Closure c) { c.call(['foo']) }
            foo { List<String> str -> str.each { println it.toUpperCase() } }
        '''

        shouldFailWithMessages '''
            void foo(@ClosureParams(value=FromString, options="java.util.List<java.lang.String>") Closure c) { c.call(['foo']) }
            foo { List<Date> d -> d.each { println it } }
        ''',
        'Expected type java.util.List<java.lang.String> for closure parameter: d'
    }

    // GROOVY-9518
    @Test
    void testFromStringWithGenericType2() {
        assertScript '''
            class C {
                C(String s, @ClosureParams(value=FromString, options="java.util.List<java.lang.Integer>") Closure<Integer> c) {
                }
            }

            new C('blah', { list -> list.get(0) })
        '''

        assertScript '''
            class C {
                C(String s, Comparable<List<Integer>> c) {
                }
            }

            new C('blah', { list -> list.get(0) })
        '''

        assertScript '''
            class C {
                C(String s, Comparable<List<Integer>> c) {
                }
            }

            new C('blah', { it.get(0) })
        '''
    }

    // GROOVY-11090
    @Test
    void testFromStringWithGenericType3() {
        String foo = '''
            void foo(@ClosureParams(value=FromString, options="Tuple2<String,Number>") Closure c) {
                c.call( new Tuple2("",42) )
            }
        '''

        assertScript foo + '''
            foo { string, number ->
                number.doubleValue()
                string.toUpperCase()
            }
        '''

        shouldFailWithMessages foo + '''
            foo { one, two, xxx -> }
        ''',
        'Incorrect number of parameters. Expected 1 or 2 but found 3'
    }

    // GROOVY-11090
    @Test
    void testFromStringWithGenericType4() {
        assertScript '''
            void foo(@ClosureParams(value=FromString, options="List<Tuple2<String,Number>>") Closure c) {
                c.call(Collections.singletonList(Tuple.tuple("",(Number)42)))
            }
            foo {
                it.each { string, number ->
                    number.doubleValue()
                    string.toUpperCase()
                }
            }
        '''
    }

    @Test
    void testFromStringWithGenericType5() {
        assertScript '''
            void foo(@ClosureParams(value=FromString, options="Optional<Tuple2<String,? extends Number>>") Closure c) {
                c(Optional.of(Tuple.tuple("",42)))
            }
            foo { opt ->
                opt.ifPresent {
                    it.v2.doubleValue()
                }
            }
        '''
    }

    @Test
    void testFromStringWithTypeParameter1() {
        assertScript '''
            def <T> void foo(T t, @ClosureParams(value=FromString, options="T") Closure c) { c.call(t) }
            foo('bar') {
                it.toUpperCase()
            }
        '''
    }

    @Test
    void testFromStringWithTypeParameter2() {
        assertScript '''
            def <T> void foo(T t, @ClosureParams(value=FromString, options="T") Closure c) { c.call(t) }
            foo(new Date()) {
                it.time
            }
        '''
    }

    @Test
    void testFromStringWithTypeParameter3() {
        assertScript '''
            def <T> void foo(T t, @ClosureParams(value=FromString, options="java.util.List<T>") Closure c) { c.call([t,t]) }
            foo('bar') { List<String> list ->
                list*.toUpperCase()
            }
            foo('baz') { list ->
                list*.toUpperCase()
            }
        '''
    }

    // GROOVY-7789
    @Test
    void testFromStringWithTypeParameter4() {
        assertScript '''
            class Monad<T> {
                private final Closure c
                Monad(@ClosureParams(value=FromString, options="T") Closure c) {
                    this.c = c
                }
                def call(T t) {
                    c.call(t)
                }
            }
            def <U> Monad<U> wrap(@ClosureParams(value=FromString, options="U") Closure c) {
                new Monad<>(c)
            }
            def list_size = this.<List>wrap({ list -> list.size() })
            assert list_size([]) == 0
        '''
    }

    @Test
    void testFromStringWithTypeParameterFromClass() {
        assertScript '''
            class Foo<T> {
                void foo(@ClosureParams(value=FromString, options="java.util.List<T>") Closure c) { c.call(['hey','ya']) }
            }
            def foo = new Foo<String>()
            foo.foo { List<String> str -> str.each { println it.toUpperCase() } }
        '''
    }

    @Test
    void testFromStringWithTypeParameterFromClassWithTwoGenerics() {
        assertScript '''
            class Foo<T,U> {
                void foo(@ClosureParams(value=FromString, options="java.util.List<U>") Closure c) { c.call(['hey','ya']) }
            }
            def foo = new Foo<Integer,String>()
            foo.foo { List<String> str -> str.each { println it.toUpperCase() } }
        '''
    }

    @Test
    void testFromStringWithTypeParameterFromClassWithTwoGenericsAndNoExplicitSignature() {
        assertScript '''
            class Foo<T,U> {
                void foo(@ClosureParams(value=FromString, options="java.util.List<U>") Closure c) { c.call(['hey','ya']) }
            }
            def foo = new Foo<Integer,String>()
            foo.foo { it.each { println it.toUpperCase() } }
        '''
    }

    @Test
    void testFromStringWithTypeParameterFromClassWithTwoGenericsAndNoExplicitSignatureAndNoFQN() {
        assertScript '''
            class Foo<T,U> {
                void foo(@ClosureParams(value=FromString, options="List<U>") Closure c) { c.call(['hey','ya']) }
            }
            def foo = new Foo<Integer,String>()
            foo.foo { it.each { println it.toUpperCase() } }
        '''
    }

    @Test
    void testFromStringWithTypeParameterFromClassWithTwoGenericsAndNoExplicitSignatureAndNoFQNAndReferenceToSameUnitClass() {
        assertScript '''
            class Foo {
                void bar() {
                    println 'Haha!'
                }
            }
            class Tor<D,U> {
                void foo(@ClosureParams(value=FromString, options="List<U>") Closure c) { c.call([new Foo(), new Foo()]) }
            }
            def tor = new Tor<Integer,Foo>()
            tor.foo { it.each { it.bar() } }
        '''
    }

    @Test
    void testFromStringWithTypeParameterFromClassWithTwoGenericsAndNoExplicitSignatureAndNoFQNAndReferenceToSameUnitClassAndTwoArgs() {
        assertScript '''
            class Foo {
                void bar() {
                    println 'Haha!'
                }
            }
            class Tor<D,U> {
                void foo(@ClosureParams(value=FromString, options=["D,List<U>"]) Closure c) { c.call(3, [new Foo(), new Foo()]) }
            }
            def tor = new Tor<Integer,Foo>()
            tor.foo { r, e -> r.times { e.each { it.bar() } } }
        '''
    }

    @Test
    void testFromStringWithTypeParameterFromClassWithTwoGenericsAndPolymorphicSignature() {
        assertScript '''
            class Foo {
                void bar() {
                    println 'Haha!'
                }
            }
            class Tor<D,U> {
                void foo(@ClosureParams(value=FromString, options=["D,List<U>","D"]) Closure c) {
                    if (c.maximumNumberOfParameters==2) {
                        c.call(3, [new Foo(), new Foo()])
                    } else {
                        c.call(3)
                    }
                }
            }
            def tor = new Tor<Integer,Foo>()
            tor.foo { r, e -> r.times { e.each { it.bar() } } }
            tor.foo { it.times { println 'polymorphic' } }
        '''
    }

    @Test
    void testFromStringWithConflictResolutionStrategy() {
        assertScript '''
            def transform(value, @ClosureParams(value=FromString, options=["Integer","String"], conflictResolutionStrategy=PickFirstResolver) Closure c) {
                if (c.parameterTypes[0].simpleName == 'String') {
                    c(value.toString())
                } else {
                    c(value instanceof Integer ? value : value.toString().size())
                }
            }

            assert transform('dog') { String s -> s * 2 } == 'dogdog'
            assert transform('dog') { Integer i -> i * 2 } == 6
            assert transform('dog') { it.class.simpleName[0..it] } == 'Inte'
            assert transform(35) { String s -> s * 2 } == '3535'
            assert transform(35) { Integer i -> i * 2 } == 70
            assert transform(35) { it * 2 } == 70
        '''
    }

    // GROOVY-6939
    @Test
    void testParamCountCheck1() {
        shouldFailWithMessages '''
            def m(o) {
                o.each { x, y -> }
            }
        ''',
        'Incorrect number of parameters. Expected 1 but found 2'
    }

    // GROOVY-6939
    @Test
    void testParamCountCheck2() {
        shouldFailWithMessages '''
            def m(o) {
                o.eachWithIndex { x, y, z -> }
            }
        ''',
        'Incorrect number of parameters. Expected 2 but found 3'
    }

    // GROOVY-6939
    @Test
    void testParamCountCheck3() {
        shouldFailWithMessages '''
            def m(o) {
                o.eachWithIndex { print it }
            }
        ''',
        'Incorrect number of parameters. Expected 2 but found 1'
    }

    // GROOVY-6939
    @Test
    void testParamCountCheck4() {
        shouldFailWithMessages '''
            def m(... array) {
                array.each { x, y -> }
            }
        ''',
        'Incorrect number of parameters. Expected 1 but found 2'
    }

    // GROOVY-6939
    @Test
    void testParamCountCheck5() {
        shouldFailWithMessages '''
            def m() {
                [:].each { -> }
            }
        ''',
        'Incorrect number of parameters. Expected 1 or 2 but found 0'
    }

    // GROOVY-8499
    @Test
    void testParamCountCheck6() {
        assertScript '''
            def result = ['ab'.chars,'12'.chars].combinations { l,n -> "$l$n" }
            assert result == ['a1','b1','a2','b2']
        '''
        // cannot know in advance how many list elements
        def err = shouldFail '''
            ['ab'.chars,'12'.chars].combinations((l,n,x) -> "$l$n")
        '''
        assert err =~ /No signature of method.* is applicable for argument types: \(ArrayList\)/
    }

    // GROOVY-8816
    @Test
    void testParamCountCheck7() {
        shouldFailWithMessages '''
            def m() {
                [].each { -> }
            }
        ''',
        'Incorrect number of parameters. Expected 1 but found 0'
    }

    // GROOVY-9854
    @Test
    void testParamCountCheck8() {
        shouldFailWithMessages '''
            switch (42) { case { -> }: break; }
        ''',
        'Incorrect number of parameters. Expected 1 but found 0'
    }

    // GROOVY-9854
    @Test
    void testParamCountCheck9() {
        shouldFailWithMessages '''
            switch (42) { case { i, j -> }: break; }
        ''',
        'Incorrect number of parameters. Expected 1 but found 2'
    }

    // GROOVY-11089
    @Test
    void testParamCountCheck10() {
        shouldFailWithMessages '''
            def array = new String[]{'a','b'}
            array.with { a,b -> }
        ''',
        'Incorrect number of parameters. Expected 1 but found 2'
    }

    // GROOVY-7141
    @Test
    void testInferenceWithSAMTypeCoercion1() {
        String sam = '''
            @FunctionalInterface
            interface I {
                String foo(String s)
            }
        '''
        assertScript sam + '''
            def impl = [foo: { it.toUpperCase() }] as I
            String result = impl.foo('bar')
            assert result == 'BAR'
        '''
        assertScript sam + '''
            def impl = [foo: { s -> s.toUpperCase() }] as I
            String result = impl.foo('bar')
            assert result == 'BAR'
        '''
        assertScript sam + '''
            def impl = [foo: { String s -> s.toUpperCase() }] as I
            String result = impl.foo('bar')
            assert result == 'BAR'
        '''
        assertScript '''
            def impl = [apply: { it.toUpperCase() }] as java.util.function.Function<String,String>
            String result = impl.apply('bar')
            assert result == 'BAR'
        '''
    }

    @Test
    void testInferenceWithSAMTypeCoercion2() {
        assertScript '''
            interface Action<T> {
                void execute(T thing)
            }

            class Wrapper<T> {
                private final T thing
                Wrapper(T thing) {
                    this.thing = thing
                }
                void contravariantTake(Action<? super T> action) {
                    action.execute(thing)
                }
                void invariantTake(Action<T> action) {
                    action.execute(thing)
                }
            }

            static <T> Wrapper<T> wrap(java.util.concurrent.Callable<T> callable) {
                new Wrapper(callable.call())
            }
            static Integer dub(Integer integer) {
                integer * 2
            }

            wrap {
                1
            } contravariantTake {
                dub(it) // fails static compile, 'it' is not known to be Integer
            }

            wrap {
                1
            } invariantTake {
                dub(it) // passes static compile, 'it' is known to be Integer
            }
        '''
    }

    @Test
    void testGroovy6735() {
        assertScript '''
            def extractInfo(String s) {
                def squareNums = s.findAll(/\\d+/) { String num -> num.toInteger() }.collect{ Integer num -> num ** 2 }
                def wordSizePlusNum = s.findAll(/\\s*(\\w+)\\s*(\\d+)/) { _, String word, String num -> word.size() + num.toInteger() }
                def numPlusWordSize = s.findAll(/\\s*(\\w+)\\s*(\\d+)/) { _, word, num -> num.toInteger() + word.size() }
                [squareNums, wordSizePlusNum, numPlusWordSize]
            }
            assert extractInfo(" ab 12 cdef 34 jhg ") == [[144, 1156], [14, 38], [14, 38]]
        '''
        assertScript '''
            assert "foobarbaz".findAll('b(a)([rz])') { full, a, b -> assert "BA"=="B" + a.toUpperCase() }.size() == 2
            assert "foobarbaz".findAll('ba') { String found -> assert "BA" == found.toUpperCase() }.size() == 2
        '''
    }

    @Test
    void testGroovy9058() {
        assertScript '''
            List<Object[]> table() {
                [ ['fee', 'fi'] as Object[], ['fo', 'fum'] as Object[] ]
            }

            List<String> result = []
            table().each { row -> result << row[0].toString().toUpperCase() }
            assert result == ['FEE', 'FO']
        '''
    }

    @Test
    void testGroovy9570() {
        assertScript '''
            class C<I extends Item> {
                Queue<I> queue

                def c = { ->
                    queue.each { I item ->
                        println item
                    }
                }

                def m() {
                    queue.each { I item ->
                        println item
                    }
                }
            }

            interface Item {}

            new C()
        '''
    }

    @Test
    void testGroovy9735() {
        assertScript '''
            class C<I extends Item> {
                Queue<I> queue

                def c = { ->
                    x(queue) { I item ->
                        println item
                    }
                }

                def m() {
                    x(queue) { I item ->
                        println item
                    }
                }

                def <T> T x(Collection<T> y, @ClosureParams(FirstParam.FirstGenericType) Closure z) {
                }
            }

            interface Item {}

            new C()
        '''
    }

    @Test
    void testGroovy9597a() {
        assertScript '''
            class A {
                def <T> void proc(Collection<T> values, @ClosureParams(FirstParam.FirstGenericType) Closure<String> block) {
                }
            }

            class B {
                List<Integer> list
                void test(A a) {
                    a.proc(this.list) { it.toBigDecimal().toString() } // works
                    a.with {
                      proc(this.list) { it.toBigDecimal().toString() } // error
                    }
                }
            }

            new B().test(new A())
        '''
    }

    @Test
    void testGroovy9597b() {
        assertScript '''
            class A {
                static A of(@DelegatesTo(A) Closure x) {
                    new A().tap {
                        x.delegate = it
                        x.call()
                    }
                }
                def <T> void proc(Collection<T> values, @ClosureParams(FirstParam.FirstGenericType) Closure<String> block) {
                }
            }

            class B {
              List<Integer> list
              A a = A.of {
                  proc(
                      this.list,
                      { it.toBigDecimal().toString() } // Cannot find matching method java.lang.Object#toBigDecimal()
                  )
              }
            }

            new B()
        '''
    }

    @Test
    void testGroovy9854() {
        assertScript '''
            def result = switch (42) {
                case { i -> i > 0 } -> 'positive'
                case { it < 0 } -> 'negative'
                default -> 'zero'
            }
            assert result == 'positive'
        '''

        shouldFailWithMessages '''
            switch (42) { case { String s -> }: break; }
        ''',
        'Expected type java.lang.Integer for closure parameter: s'
    }

    @Test
    void testGroovy9968() {
        assertScript '''
            @Canonical class Pogo { String prop }
            @Canonical class Type<T extends Pogo> implements Iterable<T> {
                Iterator<T> iterator() {
                    list.iterator()
                }
                List<T> list
            }

            def iterable = new Type([new Pogo('x'), new Pogo('y'), new Pogo('z')])
            assert iterable.collect { Pogo p -> p.prop } == ['x', 'y', 'z']
            assert iterable.collect { it.prop } == ['x', 'y', 'z']
        '''
    }

    @Test
    void testGroovy10180() {
        assertScript '''
            void test(args) {
                if (args instanceof Map) {
                    args.each { e ->
                        def k = e.key, v = e.value
                    }
                }
            }
        '''
        assertScript '''
            void test(args) {
                if (args instanceof Map) {
                    args.each { k, v ->
                    }
                }
            }
        '''
    }

    @Test
    void testGroovy10660() {
        assertScript '''
            <T> BiConsumer<String, List<T>> m(BiConsumer<String, ? super T> proc) {
                // batch processor:
                return { text, list ->
                    for (item in list) proc.accept(text, item)
                }
            }

            m { text, item -> }
        '''

        assertScript '''
            <T> BiConsumer<String, List<T>> m(BiConsumer<String, ? super T> proc) {
                // batch processor:
                return (text, list) -> {
                    for (item in list) proc.accept(text, item)
                }
            }

            m((text, item) -> { })
        '''

        assertScript '''
            <T> BiConsumer<String, List<T>> m(BiConsumer<String, ? super T> proc) {
                /*implicit return*/ (text, list) -> {
                    for (item in list) proc.accept(text, item)
                }
            }

            m((text, item) -> { })
        '''
    }

    @Test
    void testGroovy10673() {
        assertScript '''
            void proc(Consumer<Number> action) {
                action.accept(1.2345)
            }

            int i = 0
            proc { n ->
                def c = {
                    i = n.intValue()
                }
                c()
            }
            assert i == 1
        '''
    }

    @Test
    void testGroovy10756() {
        assertScript """import ${Pogo10756.name.replace('$','.')}
            Pogo10756.files.collect { it.name }
            //                        ^^ File
        """

        assertScript """import ${Pogo10756.name.replace('$','.')}
            def file = Pogo10756.files[0]
            file?.name
        """

        assertScript """import ${Pogo10756.name.replace('$','.')}
            def files = Pogo10756.files
            files*.name
        """
    }

    static class Pogo10756 {
        static <T extends File> Collection<T> getFiles() { [] }
    }

    //--------------------------------------------------------------------------

    @Test
    void testDGM_anyOnMap() {
        assertScript '''
            assert [a:10, b:1].any { k,v -> k.length() == v }
            assert [a:10, b:1].any { e -> e.key.length() == e.value }
            assert [a:10, b:1].any {it.key.length() == it.value }
        '''
    }
    @Test
    void testDGM_anyOnIterable() {
        assertScript '''
            assert ['abc','de','f'].any { it.length() == 2 }
        '''
    }
    @Test
    void testDGM_anyOnIterator() {
        assertScript '''
            assert ['abc','de','f'].iterator().any { it.length() == 2 }
        '''
    }
    @Test
    void testDGM_anyOnArray() {
        assertScript '''
            String[] strings = ['abc','de','f']
            assert strings.any { it.length() == 2 }
        '''
    }

    @Test
    void testDGM_collectExplicitIt() {
        assertScript '''
            ['a','b'].collect { it -> it.toUpperCase() }
        '''
        shouldFailWithMessages '''
            ['a','b'].collect { Date it -> it.toUpperCase() }
        ''',
        'Expected type java.lang.String for closure parameter: it'
    }
    @Test
    void testDGM_collectImplicitIt() {
        assertScript '''
            ['a','b'].collect { it.toUpperCase() }
        '''
        assertScript '''
            def items = []
            ['a','b','c'].collect(items) { it.toUpperCase() }
        '''
        assertScript '''
            String[] array = ['foo', 'bar', 'baz']
            assert array.collect { it.startsWith('ba') } == [false, true, true]
        '''
        assertScript '''
            List<Boolean> answer = [true]
            String[] array = ['foo', 'bar', 'baz']
            array.collect(answer){it.startsWith('ba')}
            assert answer == [true, false, true, true]
        '''
        assertScript '''
            Iterator<String> iter = ['foo', 'bar', 'baz'].iterator()
            assert iter.collect { it.startsWith('ba') } == [false, true, true]
        '''
        assertScript '''
            assert [1234, 3.14].collect { it.intValue() } == [1234,3]
        '''
    }
    @Test
    void testDGM_collectOnList() { // GROOVY-11090
        assertScript '''
            def list_of_tuple2 = ['a','b'].withIndex()
            def list_of_string = list_of_tuple2.collect { it.v1 + it.v2 }

            assert list_of_string == ['a0','b1']
        '''

        for (spec in ['s,i','String s,int i']) {
            assertScript """
                def list_of_tuple2 = ['a','b'].withIndex()
                def list_of_string = list_of_tuple2.collect { $spec -> s + i }

                assert list_of_string == ['a0','b1']
            """
        }
    }
    @Test
    void testDGM_collectOnMap() {
        assertScript '''
            assert [a: 'foo',b:'bar'].collect { k,v -> k+v } == ['afoo','bbar']
            assert [a: 'foo',b:'bar'].collect { e -> e.key+e.value } == ['afoo','bbar']
            assert [a: 'foo',b:'bar'].collect { it.key+it.value } == ['afoo','bbar']
        '''

        assertScript '''
            assert [a: 'foo',b:'bar'].collect([]) { k,v -> k+v } == ['afoo','bbar']
            assert [a: 'foo',b:'bar'].collect([]) { e -> e.key+e.value } == ['afoo','bbar']
            assert [a: 'foo',b:'bar'].collect([]) { it.key+it.value } == ['afoo','bbar']
        '''
    }

    @Test
    void testDGM_collectEntries() {
        assertScript '''
            assert ['a','b','c'].collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
        '''
    }
    @Test
    void testDGM_collectEntriesWithCollector() {
        assertScript '''
            assert ['a','b','c'].collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
        '''
    }
    @Test
    void testDGM_collectEntriesIterator() {
        assertScript '''
            assert ['a','b','c'].iterator().collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
        '''
    }
    @Test
    void testDGM_collectEntriesIteratorWithCollector() {
        assertScript '''
            assert ['a','b','c'].iterator().collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
        '''
    }
    @Test
    void testDGM_collectEntriesOnMap() {
        assertScript '''
            assert [a:'a',b:'b',c:'c'].collectEntries { k,v -> [k+k, v.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries { e -> [e.key+e.key, e.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries { [it.key+it.key, it.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
        '''
    }
    @Test
    void testDGM_collectEntriesOnMapWithCollector() {
        assertScript '''
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { k,v -> [k+k, v.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { e -> [e.key+e.key, e.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { [it.key+it.key, it.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
        '''
    }
    @Test
    void testDGM_collectEntriesOnArray() {
        assertScript '''
            String[] array = ['a','b','c']
            assert array.collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
        '''
    }
    @Test
    void testDGM_collectEntriesOnArrayWithCollector() {
        assertScript '''
            String[] array = ['a','b','c']
            assert array.collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
        '''
    }

    @Test
    void testDGM_collectManyUsingFirstSignature() {
        assertScript '''
            def map = [bread:3, milk:5, butter:2]
            def result = map.collectMany{ k, v -> k.startsWith('b') ? k.toList() : [] }
            assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
        '''
    }

    @Test
    void testDGM_collectManyUsingSecondSignature() {
        assertScript '''
            def map = [bread:3, milk:5, butter:2]
            def result = map.collectMany{ e -> e.key.startsWith('b') ? e.key.toList() : [] }
            assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
        '''
    }

    @Test
    void testDGM_collectManyUsingSecondSignatureAndImplicitIt() {
        assertScript '''
            def map = [bread:3, milk:5, butter:2]
            def result = map.collectMany{ it.key.startsWith('b') ? it.key.toList() : [] }
            assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
        '''
    }

    @Test
    void testDGM_collectManyOnIterable() {
        assertScript '''
            assert (0..5).collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
        '''
    }

    @Test
    void testDGM_collectManyOnIterator() {
        assertScript '''
            assert (0..5).iterator().collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
        '''
    }

    @Test
    void testDGM_collectManyOnIterableWithCollector() {
        assertScript '''
            assert (0..5).collectMany([]) { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
        '''
    }

    @Test
    void testDGM_collectManyOnMap() {
        assertScript '''
            assert [a:0,b:1,c:2].collectMany { k,v -> [v, 2*v ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany { e -> [e.value, 2*e.value ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany { [it.value, 2*it.value ]} == [0,0,1,2,2,4]
        '''
    }

    @Test
    void testDGM_collectManyOnMapWithCollector() {
        assertScript '''
            assert [a:0,b:1,c:2].collectMany([]) { k,v -> [v, 2*v ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany([]) { e -> [e.value, 2*e.value ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany([]) { [it.value, 2*it.value ]} == [0,0,1,2,2,4]
        '''
    }

    @Test
    void testDGM_collectManyOnArray() {
        assertScript '''
            Integer[] arr = (0..5) as Integer[]
            assert arr.collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
        '''
    }

    @Test
    void testDGM_countUsingFirstSignature() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { k,v -> v>1 } == 2
        '''
    }

    @Test
    void testDGM_countUsingSecondSignature() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { e -> e.value>1 } == 2
        '''
    }

    @Test
    void testDGM_countUsingSecondSignatureAndImplicitIt() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { it.value>1 } == 2
        '''
    }

    @Test
    void testDGM_countIterableOrIterator() {
        assertScript '''
            assert ['Groovy','Java'].count { it.length() > 4 } == 1
        '''
        assertScript '''
            assert ['Groovy','Java'].iterator().count { it.length() > 4 } == 1
        '''
    }

    @Test
    void testDGM_countMap() {
        assertScript '''
            assert [G:'Groovy',J:'Java'].count { k,v -> v.length() > 4 } == 1
            assert [G:'Groovy',J:'Java'].count { e -> e.value.length() > 4 } == 1
            assert [G:'Groovy',J:'Java'].count { it.value.length() > 4 } == 1
        '''
    }

    @Test
    void testDGM_countArray() {
        assertScript '''
            String[] array = ['Groovy','Java']
            assert array.count { it.length() > 4 } == 1
        '''
    }

    @Test
    void testDGM_countByCollection() {
        assertScript '''
            assert ['Groovy','yvoorG'].countBy { it.length() } == [6:2]
        '''
        assertScript '''
            assert ['Groovy','yvoorG'].iterator().countBy { it.length() } == [6:2]
        '''
    }
    @Test
    void testDGM_countByArray() {
        assertScript '''
            String[] array = ['Groovy','yvoorG']
            assert array.countBy { it.length() } == [6:2]
        '''
    }
    @Test
    void testDGM_countByMap() {
        assertScript '''
            assert [langs:['Groovy','Java']].countBy { k,v -> k.length() } == [5:1]
            assert [langs:['Groovy','Java']].countBy { e -> e.key.length() } == [5:1]
            assert [langs:['Groovy','Java']].countBy { it.key.length() } == [5:1]
        '''
    }

    @Test
    void testDGM_downto() {
        assertScript '''
            BigDecimal sum = 0
            10.0.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            BigInteger sum = 0
            10G.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            double sum = 0
            10d.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Double sum = 0
            new Double(10).downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            float sum = 0
            10f.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Float sum = 0
            new Float(10).downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            long sum = 0
            10L.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Long sum = 0
            new Long(10).downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            def sum = 0
            new Byte((byte)10).downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
    }

    @Test
    void testDGM_upto() {
        assertScript '''
            BigDecimal sum = 0
            0.0.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            BigInteger sum = 0
            0G.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            double sum = 0
            0d.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Double sum = 0
            new Double(0).upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            float sum = 0
            0f.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Float sum = 0
            new Float(0).upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            long sum = 0
            0L.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Long sum = 0
            new Long(0).upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            def sum = 0
            new Byte((byte)0).upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
    }

    @Test
    void testDGM_dropWhileOnIterable() {
        assertScript '''
            assert (0..10).dropWhile { it<5 } == (5..10)
            assert (0..10).dropWhile { int i -> i<5 } == (5..10)
        '''
    }

    @Test
    void testDGM_dropWhileOnList() {
        assertScript '''
            assert [0,1,2,3,4,5,6,7,8,9,10].dropWhile { it<5 } == [5,6,7,8,9,10]
            assert [0,1,2,3,4,5,6,7,8,9,10].dropWhile { int i -> i<5 } == [5,6,7,8,9,10]
        '''
    }

    @Test
    void testDGM_dropWhileOnIterator() {
        assertScript '''
            assert [0,1,2,3,4,5,6,7,8,9,10].iterator().dropWhile { it<5 } as List == [5,6,7,8,9,10]
            assert [0,1,2,3,4,5,6,7,8,9,10].iterator().dropWhile { int i -> i<5 } as List == [5,6,7,8,9,10]
        '''
    }

    @Test
    void testDGM_dropWhileOnArray() {
        assertScript '''
            Integer[] array = [0,1,2,3,4,5,6,7,8,9,10]
            assert array.iterator().dropWhile { it<5 } as List == [5,6,7,8,9,10]
            assert array.iterator().dropWhile { int i -> i<5 } as List == [5,6,7,8,9,10]
        '''
    }

    @Test
    void testDGM_each() {
        assertScript '''
            ['a','b'].each { it -> it.toUpperCase() }
        '''
        assertScript '''
            ['a','b'].each { it.toUpperCase() }
        '''
    }

    @Test
    void testDGM_eachByte() {
        assertScript '''
            byte[] array = new byte[0]
            array.eachByte { byte b -> b.intValue() }
            array.eachByte { it.intValue() }
        '''
        assertScript '''
            Byte[] array = new Byte[0]
            array.eachByte { Byte b -> b.intValue() }
            array.eachByte { it.intValue() }
        '''
    }

    @Test
    void testDGM_eachMatch() {
        assertScript '''
            'foo bar baz'.eachMatch(~/(?m)^(\s*).*\n$/) { all, ws ->
                all.trim(); ws.length()
            }
        '''
    }

    @Test
    void testDGM_eachWithIndexOnMap() {
        assertScript '''
            [a:'A',bb:'B',ccc:'C'].eachWithIndex { e,i -> assert e.key.toUpperCase() == e.value*(1+i) }
            [a:'A',bb:'B',ccc:'C'].eachWithIndex { k,v,i -> assert k.toUpperCase() == v*(1+i) }
        '''
    }
    @Test
    void testDGM_eachWithIndexOnObject() {
        assertScript '''
            def foo(object) {
                object.eachWithIndex { Map<String,String> map, i -> map.ccc.toLowerCase() + (1+i) }
                //                     ^^^^^^^^^^^^^^^^^^ each/eachWithIndex are flexible
            }
            foo([ [a:'A',bb:'B',ccc:'C'] ])
        '''
    }
    @Test
    void testDGM_eachWithIndexOnIterable() {
        assertScript '''
            ['1','2','3'].eachWithIndex { e,i -> assert e.toUpperCase() == String.valueOf(1+i) }
        '''
    }
    @Test
    void testDGM_eachWithIndexOnIterator() {
        assertScript '''
            ['1','2','3'].iterator().eachWithIndex { e,i -> assert e.toUpperCase() == String.valueOf(1+i) }
        '''
    }
    @Test
    void testDGM_eachWithIndexOnRecursiveIterable() { // GROOVY-10651
        for (args in ['','<?>']) {
            assertScript """
                void proc(groovy.transform.stc.TreeNode$args node) {
                    node.eachWithIndex { child, index ->
                        proc(child) // recurse
                    }
                }
            """
        }
    }

    @Test
    void testDGM_everyOnIterable() {
        assertScript '''
            assert ['foo','bar','baz'].every { String it -> it.length() == 3 }
            assert ['foo','bar','baz'].every { it -> it.length() == 3 }
            assert ['foo','bar','baz'].every { it.length() == 3 }
        '''
    }
    @Test
    void testDGM_everyOnIterator() {
        assertScript '''
            assert ['foo','bar','baz'].iterator().every { String it -> it.length() == 3 }
            assert ['foo','bar','baz'].iterator().every { it -> it.length() == 3 }
            assert ['foo','bar','baz'].iterator().every { it.length() == 3 }
        '''
    }
    @Test
    void testDGM_everyOnArray() {
        assertScript '''
            String[] items = ['foo','bar','baz']
            assert items.every { it.length() == 3 }
            assert items.every { String s -> s.length() == 3 }
        '''
    }
    @Test
    void testDGM_everyOnMap() {
        assertScript '''
            assert [a:'A',b:'B',cc:'CC'].every { String k, String v -> k == v.toLowerCase() }
            assert [a:'A',b:'B',cc:'CC'].every { k, v -> k == v.toLowerCase() }
            assert [a:'A',b:'B',cc:'CC'].every { e -> e.key == e.value.toLowerCase() }
            assert [a:'A',b:'B',cc:'CC'].every { it.key == it.value.toLowerCase() }
        '''
    }

    @Test
    void testDGM_findIndexOf() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findIndexOf { it.startsWith('ba') == 1 }
            assert items1.findIndexOf { String s -> s.startsWith('ba') == 1 }
            def items2 = ['foo','bar','baz']
            assert items2.findIndexOf { it.startsWith('ba') == 1 }
            assert items2.iterator().findIndexOf { it.startsWith('ba') == 1 }
        '''
    }

    @Test
    void testDGM_findLastIndexOf() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findLastIndexOf { it.startsWith('ba') == 2 }
            assert items1.findLastIndexOf { String s -> s.startsWith('ba') == 2 }
            def items2 = ['foo','bar','baz']
            assert items2.findLastIndexOf { it.startsWith('ba') == 2 }
            assert items2.iterator().findLastIndexOf { it.startsWith('ba') == 2 }
        '''
    }

    @Test
    void testDGM_findIndexValues() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findIndexValues { it.startsWith('ba') } == [1, 2]
            assert items1.findIndexValues { String s -> s.startsWith('ba') } == [1, 2]
            def items2 = ['foo','bar','baz']
            assert items2.findIndexValues { it.startsWith('ba') } == [1, 2]
            assert items2.iterator().findIndexValues { it.startsWith('ba') }.toList() == [1, 2]
        '''
    }

    @Test
    void testDGM_findOnCollection() {
        assertScript '''
            assert ['a','bbb','ccc'].find { String it -> it.length() == 3 } == 'bbb'
            assert ['a','bbb','ccc'].find { it -> it.length() == 3 } == 'bbb'
            assert ['a','bbb','ccc'].find { it.length() == 3 } == 'bbb'
        '''
    }
    @Test
    void testDGM_findOnArray() {
        assertScript '''
            String[] arraylistOfStrings = ['a','bbb','ccc']
            assert arraylistOfStrings.find { String it -> it.length() == 3 } == 'bbb'
            assert arraylistOfStrings.find { it -> it.length() == 3 } == 'bbb'
            assert arraylistOfStrings.find { it.length() == 3 } == 'bbb'
        '''
    }
    @Test
    void testDGM_findOnMap() {
        assertScript '''
            assert [a:2,b:4,c:6].find { String k, int v -> k.toUpperCase()=='C' && 2*v==12 } instanceof Map.Entry
            assert [a:2,b:4,c:6].find { k, v -> k.toUpperCase()=='C' && 2*v==12 } instanceof Map.Entry
            assert [a:2,b:4,c:6].find { e -> e.key.toUpperCase()=='C' && 2*e.value==12 } instanceof Map.Entry
            assert [a:2,b:4,c:6].find { it.key.toUpperCase()=='C' && 2*it.value==12 } instanceof Map.Entry
        '''
    }
    @Test
    void testDGM_findOnStr() { // GROOVY-11076, GROOVY-11089
        assertScript '''
            "75001 Paris".find(/(\\d{5})\\s(\\w+)/) { List<String> all_zip_city -> all_zip_city*.toUpperCase() }
        '''
        assertScript '''
            "75001 Paris".find(/(\\d{5})\\s(\\w+)/) { String[] all_zip_city -> all_zip_city*.toUpperCase() }
        '''
        assertScript '''
            "75001 Paris".find(/(\\d{5})\\s(\\w+)/) { Object[] all_zip_city -> all_zip_city*.toString() }
        '''
        assertScript '''
            "75001 Paris".find(/(\\d{5})\\s(\\w+)/) { all, zip, city -> all.size() + zip.size() + city.size() }
        '''
        assertScript '''
            "75001 Paris".find(/(\\d{5})\\s(\\w+)/) { String all, String zip, String city -> city + " " + zip }
        '''
        assertScript '''
            "75001 Paris".find(~/\\d{5}/) { String zip -> zip }
        '''

        shouldFailWithMessages '''
            "75001 Paris".find(/(\\d{5})\\s(\\w+)/) { String all, Date zip, String city -> }
        ''',
        'Expected type java.lang.String for closure parameter: zip'

        shouldFailWithMessages '''
            "75001 Paris".find(~/\\d{5}/) { Number zip -> zip }
        ''',
        'Expected (java.util.List<java.lang.String>) or (java.lang.String) or (java.lang.String[]) but found (java.lang.Number)'
    }

    @Test
    void testDGM_findAllOnCollection() {
        assertScript '''
            assert ['a','bbb','ccc'].findAll { String it -> it.length() == 3 } == ['bbb','ccc']
            assert ['a','bbb','ccc'].findAll { it -> it.length() == 3 } == ['bbb','ccc']
            assert ['a','bbb','ccc'].findAll { it.length() == 3 } == ['bbb','ccc']
        '''
    }
    @Test
    void testDGM_findAllOnArray() {
        assertScript '''
            String[] arraylistOfStrings = ['a','bbb','ccc']
            assert arraylistOfStrings.findAll { String it -> it.length() == 3 } == ['bbb','ccc']
            assert arraylistOfStrings.findAll { it -> it.length() == 3 } == ['bbb','ccc']
            assert arraylistOfStrings.findAll { it.length() == 3 } == ['bbb','ccc']
        '''
    }
    @Test
    void testDGM_findAllOnMap() {
        assertScript '''
            assert [a:2,b:4,c:6].findAll { String k, int v -> k.toUpperCase()=='C' && 2*v==12 } == [c:6]
            assert [a:2,b:4,c:6].findAll { k, v -> k.toUpperCase()=='C' && 2*v==12 } == [c:6]
            assert [a:2,b:4,c:6].findAll { e -> e.key.toUpperCase()=='C' && 2*e.value==12 } == [c:6]
            assert [a:2,b:4,c:6].findAll { it.key.toUpperCase()=='C' && 2*it.value==12 } == [c:6]
        '''
    }

    @Test
    void testDGM_findResultOnCollection() {
        assertScript '''
            assert ['barbar','barbaz','foo'].findResult { it.length() == 3?it.toUpperCase():null } == 'FOO'
            assert ['barbar','barbaz','foo'].findResult { String it -> it.length() == 3?it.toUpperCase():null } == 'FOO'
            assert ['barbar','barbaz','foo'].findResult { it -> it.length() == 3?it.toUpperCase():null } == 'FOO'
            assert ['barbar','barbaz','foo'].findResult(-1) { it.length() == 4?it.toUpperCase():null } == -1
            assert ['barbar','barbaz','foo'].findResult(-1) { String it -> it.length() == 4?it.toUpperCase():null } == -1
            assert ['barbar','barbaz','foo'].findResult(-1) { it -> it.length() == 4?it.toUpperCase():null } == -1
        '''
    }
    @Test
    void testDGM_findResultOnIterable() {
        assertScript '''
            assert (0..10).findResult { it== 3?2*it:null } == 6
            assert (0..10).findResult { int it -> it==3?2*it:null } == 6
            assert (0..10).findResult { it -> it==3?2*it:null } == 6
        '''
    }
    @Test
    void testDGM_findResultOnMap() {
        assertScript '''
            assert [a:1, b:2, c:3].findResult { String k, int v -> "${k.toUpperCase()}$v"=='C3'?2*v:null } == 6
            assert [a:1, b:2, c:3].findResult { k, v -> "${k.toUpperCase()}$v"=='C3'?2*v:null } == 6
            assert [a:1, b:2, c:3].findResult { e -> "${e.key.toUpperCase()}$e.value"=='C3'?2*e.value:null } == 6
            assert [a:1, b:2, c:3].findResult { "${it.key.toUpperCase()}$it.value"=='C3'?2*it.value:null } == 6

            assert [a:1, b:2, c:3].findResult('a') { String k, int v -> "${k.toUpperCase()}$v"=='C4'?2*v:null } == 'a'
            assert [a:1, b:2, c:3].findResult('a') { k, v -> "${k.toUpperCase()}$v"=='C4'?2*v:null } == 'a'
            assert [a:1, b:2, c:3].findResult('a') { e -> "${e.key.toUpperCase()}$e.value"=='C4'?2*e.value:null } == 'a'
            assert [a:1, b:2, c:3].findResult('a') { "${it.key.toUpperCase()}$it.value"=='C4'?2*it.value:null } == 'a'
        '''
    }
    @Test
    void testDGM_findResult() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findResult { it.startsWith('ba') ? it : null } == 'bar'
            def items2 = ['foo','bar','baz']
            assert items2.findResult { it.startsWith('ba') ? it : null } == 'bar'
            assert items2.iterator().findResult { it.startsWith('ba') ? it : null } == 'bar'
        '''
    }

    @Test
    void testDGM_findResultsOnIterable() {
        assertScript '''
            assert (0..10).findResults { it<3?2*it:null } == [0,2,4]
            assert (0..10).findResults { int it -> it<3?2*it:null } == [0,2,4]
            assert (0..10).findResults { it -> it<3?2*it:null } == [0,2,4]
        '''
    }
    @Test
    void testDGM_findResultsOnMap() {
        assertScript '''
            assert [a:1, b:2, c:3].findResults { String k, int v -> "${k.toUpperCase()}$v"=='C3'?2*v:null } == [6]
            assert [a:1, b:2, c:3].findResults { k, v -> "${k.toUpperCase()}$v"=='C3'?2*v:null } == [6]
            assert [a:1, b:2, c:3].findResults { e -> "${e.key.toUpperCase()}$e.value"=='C3'?2*e.value:null } == [6]
            assert [a:1, b:2, c:3].findResults { "${it.key.toUpperCase()}$it.value"=='C3'?2*it.value:null } == [6]
        '''
    }
    @Test
    void testDGM_findResults() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findResults { it.startsWith('ba') ? it : null } == ['bar', 'baz']
            def items2 = ['foo','bar','baz']
            assert items2.findResults { it.startsWith('ba') ? it : null } == ['bar', 'baz']
            assert items2.iterator().findResults { it.startsWith('ba') ? it : null } == ['bar', 'baz']
        '''
    }

    @Test
    void testDGM_groupByIterable() {
        assertScript '''
            assert ['a','bb','cc','d','eee'].groupBy { it.length() } == [1:['a','d'],2:['bb','cc'],3:['eee']]
        '''
    }
    @Test
    void testDGM_groupByArray() {
        assertScript '''
            String[] array = ['a','bb','cc','d','eee']
            assert array.groupBy { it.length() } == [1:['a','d'],2:['bb','cc'],3:['eee']]
        '''
    }
    @Test
    void testDGM_groupByMap() {
        assertScript '''
            assert [a:'1',b:'2',c:'C'].groupBy { e -> e.key.toUpperCase()==e.value?1:0 } == [0:[a:'1',b:'2'], 1:[c:'C']]
            assert [a:'1',b:'2',c:'C'].groupBy { k, v -> k.toUpperCase()==v?1:0 } == [0:[a:'1',b:'2'], 1:[c:'C']]
            assert [a:'1',b:'2',c:'C'].groupBy { it.key.toUpperCase()==it.value?1:0 } == [0:[a:'1',b:'2'], 1:[c:'C']]
        '''
    }

    @Test
    void testDGM_groupEntriesBy() {
        assertScript '''
            def result = [a:1,b:2,c:3,d:4,e:5,f:6].groupEntriesBy { k,v -> v % 2 }
            result = [a:1,b:2,c:3,d:4,e:5,f:6].groupEntriesBy { it.value % 2 }
            result = [a:1,b:2,c:3,d:4,e:5,f:6].groupEntriesBy { e -> e.value % 2 }
            assert result[0]*.key == ["b", "d", "f"]
            assert result[1]*.value == [1, 3, 5]
        '''
    }

    @Test
    void testDGM_injectOnCollection() {
        assertScript '''
            def items = ['a','bb','ccc']
            def value = items.inject { acc, str -> acc += str.toUpperCase(); acc }
            assert value == 'aBBCCC'
        '''
        assertScript '''import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM
            def items = ['a','bb','ccc']
            def value = DGM.inject(items, { acc, str -> acc += str.toUpperCase(); acc })
            assert value == 'aBBCCC'
        '''
    }
    @NotYetImplemented
    @Test
    void testDGM_injectOnIterator() {
        assertScript '''
            def items = ['a','bb','ccc'].iterator()
            def value = items.inject { acc, str -> acc += str.toUpperCase(); acc }
            //                ^^^^^^ inject(Object,Closure) without metadata
            assert value == 'aBBCCC'
        '''
    }
    @Test
    void testDGM_injectOnArray() {
        assertScript '''
            def items = new String[]{'a','bb','ccc'}
            def value = items.inject { acc, str -> acc += str.toUpperCase(); acc }
            assert value == 'aBBCCC'
        '''
    }

    @Test
    void testDGM_injectOnCollectionWithInitialValue() {
        assertScript '''
            def items = ['a','bb','ccc']
            def value = items.inject(0) { acc, str -> acc += str.length(); acc }
            assert value == 6
        '''
        assertScript '''import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM
            def items = ['a','bb','ccc']
            def value = DGM.inject(items, 0, { acc, str -> acc += str.length(); acc })
            assert value == 6
        '''
    }
    @Test
    void testDGM_injectOnIteratorWithInitialValue() {
        assertScript '''
            def items = ['a','bb','ccc'].iterator()
            def value = items.inject(0) { acc, str -> acc += str.length(); acc }
            assert value == 6
        '''
    }
    @Test
    void testDGM_injectOnArrayWithInitialValue() {
        assertScript '''
            def items = new String[]{'a','bb','ccc'}
            def value = items.inject(0) { acc, str -> acc += str.length(); acc }
            assert value == 6
        '''
    }
    @Test
    void testDGM_injectOnMapWithInitialValue() {
        assertScript '''
            def items = [a:1,b:2]
            def value = items.inject(0) { acc, entry -> acc += entry.value; acc}
            assert value == 3
            value = items.inject(0) { acc, k, v -> acc += v; acc}
            assert value == 3
        '''
    }

    @Test
    void testDGM_max() {
        assertScript '''
            assert ['a','abc', 'defg','hi'].max { it.length() } == 'defg'
            assert ['a','abc', 'defg','hi'].iterator().max { it.length() } == 'defg'
            assert (['a','abc', 'defg','hi'] as String[]).max { it.length() } == 'defg'
        '''
    }

    @Test
    void testDGM_maxOnMap() {
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].max { a,b -> a.value.length() <=> b.value.length() }
            assert result.key == 'c'
            assert result.value == 'defg'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].max { Map.Entry<String,String> a, Map.Entry<String,String> b -> a.value.length() <=> b.value.length() }
            assert result.key == 'c'
            assert result.value == 'defg'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].max { e -> e.value.length() }
            assert result.key == 'c'
            assert result.value == 'defg'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].max { it.value.length() }
            assert result.key == 'c'
            assert result.value == 'defg'
        '''
    }

    @Test
    void testDGM_min() {
        assertScript '''
            assert ['a','abc', 'defg','hi'].min { it.length() } == 'a'
            assert ['a','abc', 'defg','hi'].iterator().min { it.length() } == 'a'
            assert (['a','abc', 'defg','hi'] as String[]).min { it.length() } == 'a'
        '''
    }

    @Test
    void testDGM_maxOnMin() {
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].min { a,b -> a.value.length() <=> b.value.length() }
            assert result.key == 'a'
            assert result.value == 'a'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].min { Map.Entry<String,String> a, Map.Entry<String,String> b -> a.value.length() <=> b.value.length() }
            assert result.key == 'a'
            assert result.value == 'a'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].min { e -> e.value.length() }
            assert result.key == 'a'
            assert result.value == 'a'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].min { it.value.length() }
            assert result.key == 'a'
            assert result.value == 'a'
        '''
    }

    @Test
    void testDGM_removeAllOnCollection() {
        assertScript '''
            def list = ['abc','a','groovy','java']
            list.removeAll { it.length() <4 }
            assert list == ['groovy','java']
        '''
    }
    @Test
    void testDGM_retainAllOnCollection() {
        assertScript '''
            def list = ['abc','a','groovy','java']
            list.retainAll { it.length()>3 }
            assert list == ['groovy','java']
        '''
    }

    @Test
    void testReverseEachOnList() {
        assertScript '''
            ['a','b'].reverseEach { println it.toUpperCase() }
        '''
    }
    @Test
    void testReverseEachOnArray() {
        assertScript '''
            (['a','b'] as String[]).reverseEach { println it.toUpperCase() }
        '''
    }
    @Test
    void testReverseEachOnMap() {
        assertScript '''
            [a:1,b:2].reverseEach { k,v -> println ((k.toUpperCase())*v) }
            [a:1,b:2].reverseEach { e -> println ((e.key.toUpperCase())*e.value) }
            [a:1,b:2].reverseEach { println ((it.key.toUpperCase())*it.value) }
        '''
    }

    @Test
    void testDGM_sortOnCollection() {
        assertScript '''
            assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { a,b -> a.length() <=> b.length() }
            assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { str -> str.length() }
            assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { it.length() }
        '''
    }
    @Test
    void testDGM_sortOnArray() {
        assertScript '''
            String[] array = ["hello","hi","hey"]
            assert ["hi","hey","hello"] == array.sort { a,b -> a.length() <=> b.length() }
            assert ["hi","hey","hello"] == array.sort { str -> str.length() }
            assert ["hi","hey","hello"] == array.sort { it.length() }
        '''
    }
    @Test
    void testDGM_sortOnIterator() {
        assertScript '''
            assert ["hi","hey","hello"] == ["hello","hi","hey"].iterator().sort { a,b -> a.length() <=> b.length() }.collect()
            assert ["hi","hey","hello"] == ["hello","hi","hey"].iterator().sort { str -> str.length() }.collect()
            assert ["hi","hey","hello"] == ["hello","hi","hey"].iterator().sort { it.length() }.collect()
        '''
    }
    @Test
    void testDGM_sortOnIterable() {
        assertScript '''
            def foo(Iterable<String> iterable) {
                assert ["hi","hey","hello"] == iterable.sort { a,b -> a.length() <=> b.length() }
                assert ["hi","hey","hello"] == iterable.sort { str -> str.length() }
                assert ["hi","hey","hello"] == iterable.sort { it.length() }
            }
            foo(["hello","hi","hey"])
        '''
    }

    @Test
    void testDGM_sortOnMap() {
        assertScript '''
            def map = [a:5, b:3, c:6, d:4].sort { a, b -> a.value <=> b.value }
            assert map == [b:3, d:4, a:5, c:6]
        '''
        assertScript '''
            def map = [a:5, b:3, c:6, d:4].sort { a -> a.value }
            assert map == [b:3, d:4, a:5, c:6]
        '''
        assertScript '''
            def map = [a:5, b:3, c:6, d:4].sort { it.value }
            assert map == [b:3, d:4, a:5, c:6]
        '''
    }

    @Test
    void testDGM_splitOnCollection() {
        assertScript '''
            assert [1,2,3,4].split { it % 2 == 0 } == [[2,4],[1,3]]
        '''
        assertScript '''
            Collection items = ['foo','bar','baz']
            assert items.split { it.startsWith('ba') } == [['bar', 'baz'], ['foo']]
        '''
    }
    @Test
    void testDGM_splitOnArray() {
        assertScript '''
            String[] items = ['foo','bar','baz']
            assert items.split { it.startsWith('ba') } == [['bar', 'baz'], ['foo']]
        '''
    }

    @Test
    void testDGM_sum() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.sum { it.toUpperCase() } == 'FOOBARBAZ'
            def items2 = ['fi','fo','fum']
            assert items2.sum('FEE') { it.toUpperCase() } == 'FEEFIFOFUM'
        '''
    }

    @Test
    void testDGM_takeWhileOnIterable() {
        assertScript '''
            class AbcIterable implements Iterable<String>  {
                Iterator<String>  iterator() { "abc".iterator() }
            }
            def abc = new AbcIterable()
            assert abc.takeWhile{ it < 'b' } == ['a']
            assert abc.takeWhile{ it <= 'b' } == ['a', 'b']
        '''
    }
    @Test
    void testDGM_takeWhileOnIterator() {
        assertScript '''
            class AbcIterable implements Iterable<String>  {
                Iterator<String>  iterator() { "abc".iterator() }
            }
            def abc = new AbcIterable()
            assert abc.iterator().takeWhile{ it < 'b' }.collect() == ['a']
            assert abc.iterator().takeWhile{ it <= 'b' }.collect() == ['a', 'b']
        '''
    }
    @Test
    void testDGM_takeWhileOnList() {
        assertScript '''
            def abc = ['a','b','c']
            assert abc.iterator().takeWhile{ it < 'b' }.collect() == ['a']
            assert abc.iterator().takeWhile{ it <= 'b' }.collect() == ['a', 'b']
        '''
    }
    @Test
    void testDGM_takeWhileOnArray() {
        assertScript '''
            String[] abc = ['a','b','c']
            assert abc.iterator().takeWhile{ it < 'b' }.collect() == ['a']
            assert abc.iterator().takeWhile{ it <= 'b' }.collect() == ['a', 'b']
        '''
    }
    @Test
    void testDGM_takeWhileOnMap() {
        assertScript '''
            def shopping = [milk:1, bread:2, chocolate:3]
            assert shopping.takeWhile{ it.key.size() < 6 } == [milk:1, bread:2]
            assert shopping.takeWhile{ it.value % 2 } == [milk:1]
            assert shopping.takeWhile{ k, v -> k.size() + v <= 7 } == [milk:1, bread:2]
        '''
    }

    @Test
    void testDGM_times() {
        assertScript '''
            String foo(int x) { "x"*x }
            10.times {
                println foo(it)
            }
        '''
    }

    @Test
    void testDGM_unique() {
        assertScript '''
            def orig = [1, 3, 4, 5]
            def uniq = orig.unique(false) { it % 2 }
            assert orig == [1, 3, 4, 5]
            assert uniq == [1, 4]
        '''
        assertScript '''
            def orig = [2, 3, 3, 4]
            def uniq = orig.unique(false) { a, b -> a <=> b }
            assert orig == [2, 3, 3, 4]
            assert uniq == [2, 3, 4]
        '''
    }
    @Test
    void testDGM_uniqueOnCollection() {
        assertScript '''
            def orig = [1, 3, 4, 5]
            def uniq = orig.unique { it % 2 }
            assert uniq == [1, 4]
        '''
        assertScript '''
            def orig = [2, 3, 3, 4]
            def uniq = orig.unique { a, b -> a <=> b }
            assert uniq == [2, 3, 4]
        '''
    }
    @Test
    void testDGM_uniqueOnIterator() {
        assertScript '''
            def orig = [1, 3, 4, 5].iterator()
            def uniq = orig.unique { it % 2 }.collect()
            assert uniq == [1, 4]
        '''
        assertScript '''
            def orig = [2, 3, 3, 4].iterator()
            def uniq = orig.unique { a, b -> a <=> b }.collect()
            assert uniq == [2, 3, 4]
        '''
    }

    @Test
    void testDGM_with0() { // GROOVY-11090: edge case
        assertScript '''
            Tuple0.INSTANCE.with { -> }
        '''
        assertScript '''
            Tuple0.INSTANCE.with {
                assert it instanceof List
                assert it instanceof Tuple
                assert it === Tuple0.INSTANCE
            }
        '''
    }
    @Test
    void testDGM_with1() {
        assertScript '''
            "string".with { it.toUpperCase() }
        '''
        assertScript '''
            "string".with { str -> str.toUpperCase() }
        '''
        shouldFailWithMessages '''
            12345678.with { xxx -> xxx.toUpperCase() }
        ''',
        'Cannot find matching method java.lang.Integer#toUpperCase()'
    }

    @Test
    void testDGM_withDefaultOnMap() {
        assertScript '''
            def map = [a:'A'].withDefault { it.toUpperCase() }
            assert map.b=='B'
        '''
    }
}
