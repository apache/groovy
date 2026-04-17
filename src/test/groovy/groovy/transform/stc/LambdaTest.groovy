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

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class LambdaTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        ast(groovy.transform.CompileStatic)
        imports {
            normal 'java.util.stream.Collectors'
            star 'java.util.function'
            star 'java.util.stream'
        }
    }

    @Test
    void testFunction() {
        assertScript shell, '''
            def f() {
                [1, 2, 3].stream().map(i -> i + 1).toList()
            }
            assert f() == [2, 3, 4]
        '''
    }

    @Test
    void testFunction2() {
        assertScript shell, '''
            def f() {
                [1, 2, 3].stream().map(i -> i.plus(1)).toList()
            }
            assert f() == [2, 3, 4]
        '''
    }

    @Test
    void testFunction3() {
        assertScript shell, '''
            def f() {
                [1, 2, 3].stream().<String>map(i -> null).toList()
            }
            assert f() == [null, null, null]
        '''
    }

    // GROOVY-11364
    @Test
    void testFunction4() {
        assertScript shell, '''
            abstract class A<N extends Number> {
                protected N process(N n) { n }
            }

            class C extends A<Integer> {
                static void consume(Optional<Integer> option) {
                    def result = option.orElse(null)
                    assert result instanceof Integer
                    assert result == 42
                }
                void test() {
                    consume(Optional.of(42).map(i -> process(i)))
                }
            }

            new C().test()
        '''
    }

    // GROOVY-11414
    @Test
    void testFunction5() {
        def err = shouldFail shell, '''
            def map = (Map<String, Long>) [:]
            map.computeIfAbsent('key', (k) -> 1)
        '''
        assert err.message =~ /Cannot return value of type int for lambda expecting java.lang.Long/
    }

    @Test
    void testBinaryOperator() {
        assertScript shell, '''
            def f() {
                [1, 2, 3].stream().reduce(7, (Integer r, Integer i) -> r + i)
            }
            assert f() == 13
        '''
    }

    // GROOVY-8917
    @Test
    void testBinaryOperatorWithoutExplicitTypes() {
        assertScript shell, '''
            def f() {
                [1, 2, 3].stream().reduce(7, (r, i) -> r + i)
            }
            assert f() == 13
        '''
    }

    @Test
    void testBinaryOperatorWithoutExplicitTypes2() {
        assertScript shell, '''
            def f() {
                BinaryOperator<Integer> accumulator = (r, i) -> r + i
                return [1, 2, 3].stream().reduce(7, accumulator)
            }
            assert f() == 13
        '''
    }

    // GROOVY-10282
    @Test
    void testBiFunctionAndBinaryOperatorWithSharedTypeParameter() {
        assertScript shell, '''
            def f() {
                IntStream.range(0, 10).boxed().reduce('', (s, i) -> s + '-', String::concat)
            }
            assert f() == '----------'
        '''
    }

    @Test
    void testBiFunctionAndVariadicMethod() {
        assertScript shell, '''
            class C {
                List m(... args) {
                    [this,*args]
                }
            }

            void test(C c) {
                BiFunction<Integer, Integer, List> f = (i, j) -> c.m(i, j)
                def list = f.apply(1,2)
                assert list.size() == 3
                assert list[0] == c
                assert list[1] == 1
                assert list[2] == 2
            }
            test(new C())
        '''
    }

    @Test
    void testPredicate1() {
        assertScript shell, '''
            def f() {
                def list = ['ab', 'bc', 'de']
                list.removeIf(e -> e.startsWith('a'))
                list
            }
            assert f() == ['bc', 'de']
        '''
    }

    @Test
    void testPredicate2() {
        assertScript shell, '''
            def f() {
                Predicate<String> tester = s -> s.startsWith('b')
                Function<String, String> mapper = s -> s.toUpperCase()
                List<String> myList = Arrays.asList('a1', 'a2', 'b2', 'b1', 'c2', 'c1')
                List<String> result = myList.stream().filter(tester).map(mapper).sorted().collect(Collectors.toList())
            }
            assert f() == ['B1', 'B2']
        '''
    }

    @Test
    void testUnaryOperator() {
        assertScript shell, '''
            def f() {
                def list = [1, 2, 3]
                list.replaceAll(e -> e + 10)
                list
            }
            assert f() == [11, 12, 13]
        '''
    }

    // GROOVY-11490
    @Test
    void testUnaryOperator2() {
        def err = shouldFail shell, '''
            IntUnaryOperator f = (i) -> null
            int i = f.applyAsInt(42)
        '''
        assert err.message =~ /Cannot return null for lambda expecting int/
    }

    @Test
    void testBiConsumer() {
        assertScript shell, '''
            def f() {
                def map = [a: 1, b: 2, c: 3], list = []
                map.forEach((k, v) -> list.add(k + v))
                list
            }
            assert f() == ['a1', 'b2', 'c3']
        '''
    }

    @Test
    void testComparator() {
        assertScript shell, '''
            class T {
                Comparator<Integer> c = (Integer a, Integer b) -> Integer.compare(a, b)
            }
            def t = new T()
            assert t.c.compare(0,0) == 0
        '''
    }

    // GROOVY-10372
    @Test
    void testComparator2() {
        def err = shouldFail shell, '''
            class T {
                Comparator<Integer> c = (int a, String b) -> 42
            }
        '''
        assert err.message =~ /Expected type java.lang.Integer for lambda parameter: b/
    }

    // GROOVY-9977
    @Test
    void testComparator3() {
        assertScript shell, '''
            class T {
                Comparator<Integer> c = (a, b) -> Integer.compare(a, b)

                void m1() {
                    Comparator<Integer> x = (a, b) -> Integer.compare(a, b)
                }
                static void m2() {
                    Comparator<Integer> y = (a, b) -> Integer.compare(a, b)
                }
            }
            def t = new T()
            assert t.c.compare(0,0) == 0
        '''
    }

    // GROOVY-9997
    @Test
    void testComparator4() {
        assertScript '''
            @groovy.transform.TypeChecked
            void test() {
                def cast = (Comparator<Integer>) (a, b) -> Integer.compare(a, b)
                assert cast.compare(0,0) == 0

                def coerce = ((a, b) -> Integer.compare(a, b)) as Comparator<Integer>
                assert coerce.compare(0,0) == 0
            }
            test()
        '''
    }

    @Test
    void testCollectors1() {
        for (spec in ['', '<String,String,String>']) {
            assertScript shell, """
                def set = ['a', 'b', 'c'] as Set<String>
                def map = set.stream().collect(Collectors.${spec}toMap(e -> e, e -> e))

                assert map == [a: 'a', b: 'b', c: 'c']
            """
        }
    }

    @Test
    void testCollectors2() {
        for (spec in ['', '<String,String,String>']) {
            assertScript shell, """
                def set = ['a', 'b', 'c'] as Set<String>
                def map = set.stream().collect(Collectors.${spec}toMap(e -> e, e -> e, (v1,v2) -> v2))

                assert map == [a: 'a', b: 'b', c: 'c']
            """
        }
    }

    // GROOVY-11304
    @Test
    void testCollectors3() {
        assertScript shell, '''
            List<String> list = ['a', 'b', 'c']
            Map<String, Integer> map = list.stream().collect(
                Collectors.toMap(Function.identity(), (k) -> 1, (v, w) -> v + w)
            )

            assert map == [a: 1, b: 1, c: 1]
        '''
    }

    @Test
    void testFunctionWithLocalVariables() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    String x = '#'
                    assert ['#1', '#2', '#3'] == [1, 2, 3].stream().map(e -> x + e).collect(Collectors.toList());
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables2() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    new Test1().p()
                }

                void p() {
                    String x = '#'
                    Integer y = 23
                    assert ['23#1', '23#2', '23#3'] == [1, 2, 3].stream().map(e -> '' + y + x + e).collect(Collectors.toList())
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables3() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    new Test1().p()
                }

                void p() {
                    String x = 'x'
                    StringBuilder y = new StringBuilder('y')
                    assert ['yx1', 'yx2', 'yx3'] == [1, 2, 3].stream().map(e -> y + x + e).collect(Collectors.toList())
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables4() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    Function<Integer, String> f = p()
                    assert '#1' == f(1)
                }

                static Function<Integer, String> p() {
                    String x = '#'
                    Function<Integer, String> f = (Integer e) -> x + e
                    return f
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables5() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    Function<Integer, String> f = new Test1().p();
                    assert '#1' == f(1)
                }

                Function<Integer, String> p() {
                    String x = '#'
                    Function<Integer, String> f = (Integer e) -> x + e
                    return f
                }
            }
        '''
    }

    @Test
    void testFunctionWithStaticMethodCall() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    String x = 'x'
                    StringBuilder y = new StringBuilder('y')
                    assert ['Hello yx1', 'Hello yx2', 'Hello yx3'] == [1, 2, 3].stream().map(e -> hello() + y + x + e).collect(Collectors.toList())
                }

                static String hello() {
                    return 'Hello '
                }
            }
        '''
    }

    @Test
    void testFunctionWithStaticMethodCall2() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    String x = 'x'
                    StringBuilder y = new StringBuilder('y')
                    assert ['Hello yx1', 'Hello yx2', 'Hello yx3'] == [1, 2, 3].stream().map(e -> Test1.hello() + y + x + e).collect(Collectors.toList())
                }

                static String hello() {
                    return 'Hello '
                }
            }
        '''
    }

    @Test
    void testFunctionWithInstanceMethodCall() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    new Test1().p()
                }

                void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ['Jochen', 'Daniel'].stream().map(e -> hello() + e).collect(Collectors.toList())
                }

                String hello() {
                    return 'Hello '
                }
            }
        '''
    }

    @Test
    void testFunctionInConstructor() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    new Test1()
                }

                Test1() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ['Jochen', 'Daniel'].stream().map(e -> hello() + e).collect(Collectors.toList())
                }

                String hello() {
                    return 'Hello '
                }
            }
        '''
    }

    @Test
    void testFunctionWithInstanceMethodCall2() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    new Test1().p()
                }

                void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ['Jochen', 'Daniel'].stream().map(e -> this.hello() + e).collect(Collectors.toList())
                }

                String hello() {
                    return 'Hello '
                }
            }
        '''
    }

    @Test
    void testFunctionWithInstanceMethodCall3() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    new Test1().p()
                }

                void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ['Jochen', 'Daniel'].stream().map(e -> hello(e)).collect(Collectors.toList())
                }

                String hello(String name) {
                    return "Hello $name"
                }
            }
        '''
    }

    @Test
    void testFunctionCall() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    Function<Integer, Integer> f = (Integer e) -> (Integer) (e + 1)
                    assert 2 == f(1)
                }
            }
        '''
    }

    @Test
    void testFunctionCallWithoutExplicitTypeDef() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    Function<Integer, Integer> f = e -> e + 1
                    assert 2 == f(1)
                }
            }
        '''
    }

    @Test
    void testFunctionCall2() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    new Test1().p()
                }

                void p() {
                    Function<Integer, Integer> f = (Integer e) -> (Integer) (e + 1)
                    assert 2 == f(1)
                }
            }
        '''
    }

    @Test
    void testFunctionCall3() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    Function<Integer, Integer> f = (Integer e) -> (Integer) (e + 1)
                    assert 2 == f.apply(1)
                }
            }
        '''
    }

    @Test
    void testConsumer1() {
        assertScript shell, '''
            int a = 1
            Consumer<Integer> c = i -> { a += i }
            c.accept(2)
            assert a == 3
        '''
    }

    @Test
    void testConsumer2() {
        assertScript shell, '''
            int a = 1
            Consumer<Integer> c = (i) -> { a += i }
            c.accept(2)
            assert a == 3
        '''
    }

    @Test
    void testConsumer3() {
        assertScript shell, '''
            int a = 1
            Consumer<Integer> c = (Integer i) -> { a += i }
            c.accept(2)
            assert a == 3
        '''
    }

    @Test
    void testConsumer4() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    int a = 1
                    Consumer<Integer> c = e -> { a += e }
                    c.accept(2)
                    assert a == 3
                }
            }
        '''
    }

    @Test
    void testConsumer5() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    new Test1().p()
                }

                void p() {
                    int a = 1
                    Consumer<Integer> c = (Integer e) -> { a += e }
                    c.accept(2)
                    assert a == 3
                }
            }
        '''
    }

    @Test
    void testConsumer6() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    int a = 1
                    Consumer<Integer> c = (Integer e) -> { a += e }
                    c(2)
                    assert a == 3
                }
            }
        '''
    }

    // GROOVY-9347
    @Test
    void testConsumer7() {
        assertScript shell, '''
            void test() {
                int sum = 0
                Consumer<? super Integer> add = i -> sum += i

                [1, 2, 3].forEach(add)
                assert sum == 6
            }
            test()
        '''
    }

    // GROOVY-9340
    @Test
    void testConsumer8() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    Consumer<Test1> c = t -> null
                    c.accept(this.newInstance())
                }
            }
        '''
    }

    @Test
    void testConsumer9() {
        assertScript shell, '''
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    [1, 2, 3].stream().forEach(e -> { System.out.println(e + 1); })
                }
            }
        '''
    }

    @Test // GROOVY-10056
    void testConsumer10() {
        ['CompileStatic', 'TypeChecked'].each { xform ->
            assertScript """
                @groovy.transform.${xform}
                void test() {
                    String[][] arrayArray = new String[][] {
                        new String[] {'a','b','c'},
                        new String[] {'d','e','f'}
                    }
                    Arrays.stream(arrayArray).limit(1).forEach(array -> {
                        assert Arrays.asList(array) == ['a','b','c']
                    })
                }
                test()
            """
        }
    }

    @Test // GROOVY-10813
    void testConsumer11() {
        ['CompileStatic', 'TypeChecked'].each { xform ->
            assertScript """
                @groovy.transform.${xform}
                void test() {
                    java.util.function.Consumer c = x -> print(x)
                    c.accept('works')
                }
                test()
            """
            assertScript """
                interface I<T extends CharSequence> {
                    void accept(T t)
                }
                @groovy.transform.${xform}
                void test() {
                    I i = x -> print(x)
                    i.accept('works')
                }
                test()
            """
        }
    }

    @Test
    void testFunctionalInterface1() {
        assertScript shell, '''
            interface SamCallable {
                int call(int i)
            }

            void p() {
                SamCallable c = (int x) -> x
                assert c(1) == 1
            }

            p()
        '''
    }

    @Test
    void testFunctionalInterface2() {
        assertScript shell, '''
            interface SamCallable {
                int call(int i)
            }

            void p() {
                SamCallable c = x -> x
                assert c(1) == 1
            }

            p()
        '''
    }

    @Test
    void testFunctionalInterface3() {
        assertScript shell, '''
            abstract class SamCallable {
                abstract int call(int i)
            }

            void p() {
                SamCallable c = (int x) -> x // this is a closure, not a native lambda
                assert c(1) == 1
            }

            p()
        '''
    }

    // GROOVY-9881
    @Test
    void testFunctionalInterface4() {
        assertScript shell, '''
            class Value<V> {
                final V val;
                Value(V v) {
                    this.val = v
                }
                String toString() {
                    val as String
                }
                def <Out> Value<Out> replace(Supplier<Out> supplier) {
                    new Value<>(supplier.get())
                }
                def <Out> Value<Out> replace(Function<? super V, ? extends Out> function) {
                    new Value<>(function.apply(val))
                }
            }

            assert new Value<>(123).replace(() -> 'foo').toString() == 'foo'
            assert new Value<>(123).replace((Integer v) -> 'bar').toString() == 'bar'
        '''
    }

    // GROOVY-11121
    @Test
    void testFunctionalInterface5() {
        String clazz = '''
            class C<T> {
                public String which
                static <T> C<T> of(T value) { new C<>(which: 'T') }
                static <T> C<T> of(Iterable<T> values) { new C<>(which: 'Iterable<T>') }
            }
        '''
        assertScript shell, clazz + '''
            assert C.<IntUnaryOperator>of( (int i) -> i + 1 ).which == 'T'
        '''
        assertScript shell, clazz + '''
            @groovy.transform.CompileDynamic
            void p() {
                assert C.of( (int i) -> i + 1 ).which == 'T'
            }

            p()
        '''
    }

    // GROOVY-10372
    @Test
    void testFunctionalInterface6() {
        def err = shouldFail shell, '''
            interface I {
                def m(List<String> strings)
            }

            I face = (List<Object> list) -> null
        '''
        assert err.message =~ /Expected type java.util.List<java.lang.String> for lambda parameter: list/
    }

    // GROOVY-11013
    @Test
    void testFunctionalInterface7() {
        assertScript shell, '''
            interface I<T> {
                def m(List<T> list_of_t)
            }

            I<String> face = (List<String> list) -> null
        '''
    }

    // GROOVY-11072
    @Test
    void testFunctionalInterface8() {
        assertScript shell, '''
            class Model {
            }
            class Table<T extends Model> {
                interface ChunkReader<T> {
                    void call(List<T> row)
                }
                void getAll(ChunkReader<T> reader) {
                    List<T> chunk = []
                    reader.call(chunk)
                }
            }
            class TestModel extends Model {
                int id = 0
            }
            class TestTable extends Table<TestModel> {
            }

            TestTable table = new TestTable()
            table.getAll((List<TestModel> list) ->
                list.each { TestModel tm -> println(tm.id) }
            )
        '''
    }

    // GROOVY-11092
    @Test
    void testFunctionalInterface9() {
        assertScript shell, '''
            Function<List<String>,String> f = (one,two) -> { one + two }
            assert f.apply(['foo','bar']) == 'foobar'
        '''
        assertScript shell, '''
            void setFun(Function<List<String>,String> f) {
                assert f.apply(['foo','bar']) == 'foobar'
            }
            fun = (one,two) -> { one + two }
        '''
        assertScript shell, '''
            void test(Function<List<String>,String> f) {
                assert f.apply(['foo','bar']) == 'foobar'
            }
            test((one, two) -> { one + two })
        '''
    }

    // GROOVY-11681
    @Test
    void testFunctionalInterface10() {
        assertScript shell, '''
            def executor = java.util.concurrent.Executors.newSingleThreadExecutor()
            try {
                def future = executor.submit(() -> {
                    return 'works'
                })
                assert future.get() == 'works'
            } finally {
                executor.shutdown()
            }
        '''
    }

    @Test
    void testFunctionWithUpdatingLocalVariable() {
        for (mode in ['','static']) {
            assertScript shell, """
                $mode void p() {
                    int i = 1
                    Object result =  [1, 2, 3].stream().map(e -> i += e).collect(Collectors.toList())
                    assert result == [2, 4, 7]
                    assert i == 7
                }

                p()
            """
        }
    }

    @Test
    void testFunctionWithVariableDeclaration() {
        assertScript shell, '''
            Function<Integer, String> f = (Integer e) -> 'a' + e
            assert ['a1', 'a2', 'a3'] == [1, 2, 3].stream().map(f).collect(Collectors.toList())
        '''
    }

    @Test
    void testFunctionWithMixingVariableDeclarationAndMethodInvocation() {
        assertScript shell, '''
            String x = '#'
            Integer y = 23
            assert [1, 2, 3].stream().map(e -> '' + y + x + e).collect(Collectors.toList()) == ['23#1', '23#2', '23#3']

            Function<Integer, String> f = (Integer e) -> 'a' + e
            assert [1, 2, 3].stream().map(f).collect(Collectors.toList()) == ['a1', 'a2', 'a3']
            assert [1, 2, 3].stream().map(e -> e.plus(1)).collect(Collectors.toList()) == [2, 3, 4]
        '''
    }

    @Test
    void testFunctionWithNestedLambda() {
        assertScript shell, '''
            [1, 2].stream().forEach(e -> {
                def list = ['a', 'b'].stream().map(f -> f + e).toList()
                assert list == (e == 1 ? ['a1', 'b1'] : ['a2', 'b2'])
            })
        '''
    }

    @Test
    void testFunctionWithNestedLambda2() {
        assertScript shell, '''
            def list = ['a', 'b'].stream()
            .map(e -> {
                [1, 2].stream().map(f -> e + f).toList()
            }).toList()

            assert list[0] == ['a1', 'a2']
            assert list[1] == ['b1', 'b2']
        '''
    }

    @Test
    void testFunctionWithNestedLambda3() {
        assertScript shell, '''
            def list = ['a', 'b'].stream()
            .map(e -> {
                Function<Integer, String> x = (Integer f) -> e + f
                [1, 2].stream().map(x).toList()
            }).toList()

            assert list[0] == ['a1', 'a2']
            assert list[1] == ['b1', 'b2']
        '''
    }

    @Test
    void testMixingLambdaAndMethodReference() {
        assertScript shell, '''
            assert [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList()) == ['1', '2', '3']
            assert [1, 2, 3].stream().map( e -> e.plus(1) ).collect(Collectors.toList()) == [2, 3, 4]
            assert [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList()) == ['1', '2', '3']
        '''
    }

    @Test
    void testNestedLambdaAccessingInstanceFields() {
        assertScript shell, '''
            class Test1 {
                private List<String> strList = ['a', 'e', 'f']
                private Map<String, List<String>> strListHolder = ['strList': strList]
                private String b = 'b'
                def f() {
                    ['abc', 'def', 'ghi'].stream().filter(e -> strList.stream().anyMatch(c -> e.contains(c + b))).toList()
                }
                def g() {
                    ['abc', 'def', 'ghi'].stream().filter(e -> strListHolder.strList.stream().anyMatch(c -> e.contains(c + b))).toList()
                }
            }

            assert new Test1().f() == ['abc']
            assert new Test1().g() == ['abc']
        '''
    }

    @Test
    void testNestedLambdaAccessingInstanceFields2() {
        for (qual in ['','this.','thisObject.','getThisObject().']) {
            assertScript shell, """
                class Test1 {
                    def m() {
                        Optional.empty().orElseGet(() -> Optional.empty().orElseGet(() -> ${qual}n))
                    }
                    private n = 123
                }

                assert new Test1().m() == 123
            """
        }
    }

    // GROOVY-11480
    @Test
    void testNestedLambdaAccessingInstanceFields3() {
        for (qual in ['','Test1.this.']) {
            assertScript shell, """
                class Test1 {
                    class Test2 {
                        def m() {
                            Optional.empty().orElseGet(() -> Optional.empty().orElseGet(() -> ${qual}n))
                        }
                    }
                    private n = 123
                }

                assert new Test1.Test2(new Test1()).m() == 123
            """
        }
    }

    @Test
    void testInitializeBlocks() {
        assertScript shell, '''
            class Test1 {
                static sl
                def il
                static { sl = [1, 2, 3].stream().map(e -> e + 1).toList() }

                {
                    il = [1, 2, 3].stream().map(e -> e + 2).toList()
                }
            }

            assert [2, 3, 4] == Test1.sl
            assert [3, 4, 5] == new Test1().il
        '''
    }

    // GROOVY-9332
    @Test
    void testStaticInitializeBlocks1() {
        assertScript shell, '''
            class Test1 {
                static list
                static final int one = 1
                static { list = [1, 2, 3].stream().map(e -> e + one).toList() }
            }

            assert [2, 3, 4] == Test1.list
        '''
    }

    // GROOVY-9347
    @Test
    void testStaticInitializeBlocks2() {
        assertScript shell, '''
            class Test1 {
                static int acc = 1
                static { [1, 2, 3].forEach(e -> acc += e) }
            }

            assert Test1.acc == 7
        '''
    }

    // GROOVY-9342
    @Test
    void testStaticInitializeBlocks3() {
        assertScript shell, '''
            class Test1 {
                static int acc = 1
                static { [1, 2, 3].forEach((Integer i) -> acc += i) }
            }

            assert Test1.acc == 7
        '''
    }

    // GROOVY-10943
    @Test
    void testUnderscorePlaceholder() {
        assertScript '''
            def e = (a, b, _, _) -> a + b
            def f = (a, _, b, _) -> a + b
            def g = (_, a, _, b) -> a + b

            assert e(1000, 100, 10, 1) == 1100
            assert f(1000, 100, 10, 1) == 1010
            assert g(1000, 100, 10, 1) == 101
        '''
    }

    @Test
    void testAccessingThis1() {
        assertScript shell, '''
            class ThisTest {
                private final ThisTest that = this

                void m() {
                    Predicate<ThisTest> p = (ThisTest t) -> {
                        assert this === t
                    }
                    p.test(that)
                    p.test(this)
                }
            }

            new ThisTest().m()
        '''
    }

    @Test
    void testAccessingThis2() {
        assertScript shell, '''
            class ThisTest {
                private final ThisTest that = this

                void m() {
                    Predicate<ThisTest> p1 = (ThisTest t1) -> {
                        Predicate<ThisTest> p2 = (ThisTest t2) -> {
                            assert this === t1 && this === t2
                        }
                        p2.test(t1)
                    }
                    p1.test(that)
                    p1.test(this)
                }
            }

            new ThisTest().m()
        '''
    }

    @Test
    void testAccessingThis3() {
        assertScript shell, '''
            class ThisTest {
                String p = 'a'

                void m() {
                    def list = [1, 2].stream().map(e -> this.p + e).toList()
                    assert list == ['a1', 'a2']
                }
            }

            new ThisTest().m()
        '''
    }

    @Test
    void testAccessingThis4() {
        assertScript shell, '''
            class ThisTest {
                String getP() { 'a' }

                void m() {
                    def list = [1, 2].stream().map(e -> this.p + e).toList()
                    assert list == ['a1', 'a2']
                }
            }

            new ThisTest().m()
        '''
    }

    @Test
    void testSerialize1() {
        assertScript shell, '''
            interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
            }

            byte[] test() {
                try (def out = new ByteArrayOutputStream()) {
                    out.withObjectOutputStream {
                        SerializableFunction<Integer, String> f = ((Integer i) -> 'a' + i)
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }
            }

            assert test().length > 0
        '''
    }

    @Test
    void testSerialize2() {
        def err = shouldFail shell, NotSerializableException, '''
            byte[] test() {
                try (def out = new ByteArrayOutputStream()) {
                    out.withObjectOutputStream {
                        Function<Integer, String> f = ((Integer i) -> 'a' + i)
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }
            }

            test()
        '''
        assert err.message.contains('$Lambda')
    }

    @Test
    void testDeserialize1() {
        assertScript shell, '''
            package tests.lambda

            class C {
                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream { it ->
                        SerializableFunction<Integer, String> f = (Integer i) -> 'a' + i
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.newInstance().test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize2() {
        assertScript shell, '''
            package tests.lambda

            class C implements Serializable {
                private static final long serialVersionUID = -1L
                String s = 'a'
                transient SerializableFunction<Integer, String> f = (Integer i) -> s + i

                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.newInstance().test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize3() {
        def err = shouldFail shell, NotSerializableException, '''
            package tests.lambda

            class C {
                String s = 'a'
                SerializableFunction<Integer, String> f = (Integer i) -> s + i

                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    this.newInstance().test()
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
        assert err.message.contains('tests.lambda.C')
    }

    @Test
    void testDeserialize4() {
        assertScript shell, '''
            class C {
                static byte[] test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream { it ->
                        SerializableFunction<Integer, String> f = (Integer i) -> 'a' + i
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize5() {
        assertScript shell, '''
            package tests.lambda

            class C {
                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        String s = 'a'
                        SerializableFunction<Integer, String> f = (Integer i) -> s + i
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.newInstance().test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize6() {
        assertScript shell, '''
            package tests.lambda

            class C {
                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    String s = 'a'
                    SerializableFunction<Integer, String> f = (Integer i) -> s + i
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.newInstance().test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize7() {
        assertScript shell, '''
            package tests.lambda

            class C {
                static byte[] test() {
                    def out = new ByteArrayOutputStream()
                    String s = 'a'
                    SerializableFunction<Integer, String> f = (Integer i) -> s + i
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize8() {
        assertScript shell, '''
            package tests.lambda

            class C implements Serializable {
                private static final long serialVersionUID = -1L
                private String s = 'a'

                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f = (Integer i) -> s + i
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.newInstance().test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize9() {
        def err = shouldFail shell, NotSerializableException, '''
            package tests.lambda

            class C {
                private String s = 'a'

                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f = (Integer i) -> s + i
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.newInstance().test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
        assert err.message.contains('tests.lambda.C')
    }

    @Test
    void testDeserialize10() {
        assertScript shell, '''
            package tests.lambda

            class C implements Serializable {
                private static final long serialVersionUID = -1L
                private String getS() { 'a' }

                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f = (Integer i) -> s + i
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.newInstance().test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize11() {
        def err = shouldFail shell, NotSerializableException, '''
            package tests.lambda

            class C {
                private String getS() { 'a' }

                byte[] test() {
                    def out = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f = (Integer i) -> s + i
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.newInstance().test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
        assert err.message.contains('tests.lambda.C')
    }

    @Test
    void testDeserialize12() {
        assertScript shell, '''
            package tests.lambda

            class C {
                private static final String s = 'a'
                static byte[] test() {
                    def out = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f = (Integer i) -> s + i
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize13() {
        assertScript shell, '''
            package tests.lambda

            class C {
                private static String getS() { 'a' }
                static byte[] test() {
                    def out = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f = (Integer i) -> s + i
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }
                    out.toByteArray()
                }

                static main(args) {
                    new ByteArrayInputStream(this.test()).withObjectInputStream(this.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert f.apply(1) == 'a1'
                    }
                }

                interface SerializableFunction<I,O> extends Serializable, Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserializeNestedLambda1() {
        assertScript '''
            interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
            }

            @groovy.transform.CompileStatic
            class C {
                def test() {
                    def out1 = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f1 = (Integer i) -> 'a' + i
                    out1.withObjectOutputStream {
                        it.writeObject(f1)
                    }

                    def out2 = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f2 = (Integer i) -> 'b' + i
                    out2.withObjectOutputStream {
                        it.writeObject(f2)
                    }

                    // nested lambda expression
                    def out3 = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f3 = (Integer i) -> {
                        SerializableFunction<Integer, String> nf = (Integer j) -> 'c' + j
                        nf(i) + 'c'
                    }
                    out3.withObjectOutputStream {
                        it.writeObject(f3)
                    }

                    [out1.toByteArray(), out2.toByteArray(), out3.toByteArray()]
                }
            }

            def (serializedLambdaBytes1, serializedLambdaBytes2, serializedLambdaBytes3) = new C().test()

            new ByteArrayInputStream(serializedLambdaBytes1).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'a1'
            }

            new ByteArrayInputStream(serializedLambdaBytes2).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'b1'
            }

            new ByteArrayInputStream(serializedLambdaBytes3).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'c1c'
            }
        '''
    }

    @Test
    void testDeserializeNestedLambda2() {
        assertScript '''
            interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
            }

            @groovy.transform.CompileStatic
            class C {
                def test() {
                    def out1 = new ByteArrayOutputStream()
                    out1.withObjectOutputStream {
                        SerializableFunction<Integer, String> f = ((Integer i) -> 'a' + i)
                        it.writeObject(f)
                    }

                    def out2 = new ByteArrayOutputStream()
                    out2.withObjectOutputStream {
                        SerializableFunction<Integer, String> f = ((Integer i) -> 'b' + i)
                        it.writeObject(f)
                    }

                    // nested lambda expression
                    def out3 = new ByteArrayOutputStream()
                    out3.withObjectOutputStream { it ->
                        SerializableFunction<Integer, String> f = (Integer i) -> {
                            SerializableFunction<Integer, String> nf = (Integer j) -> 'c' + j
                            nf(i) + 'c'
                        }
                        it.writeObject(f)
                    }

                    [out1.toByteArray(), out2.toByteArray(), out3.toByteArray()]
                }
            }

            def (serializedLambdaBytes1, serializedLambdaBytes2, serializedLambdaBytes3) = new C().test()

            new ByteArrayInputStream(serializedLambdaBytes1).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'a1'
            }

            new ByteArrayInputStream(serializedLambdaBytes2).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'b1'
            }

            new ByteArrayInputStream(serializedLambdaBytes3).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'c1c'
            }
        '''
    }

    @Test
    void testDeserializeNestedLambda3() {
        assertScript '''
            interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
            }

            @groovy.transform.CompileStatic
            class C {
                static test() {
                    def out1 = new ByteArrayOutputStream()
                    out1.withObjectOutputStream {
                        SerializableFunction<Integer, String> f = ((Integer i) -> 'a' + i)
                        it.writeObject(f)
                    }

                    def out2 = new ByteArrayOutputStream()
                    out2.withObjectOutputStream {
                        SerializableFunction<Integer, String> f = ((Integer i) -> 'b' + i)
                        it.writeObject(f)
                    }

                    // nested lambda expression
                    def out3 = new ByteArrayOutputStream()
                    out3.withObjectOutputStream { it ->
                        SerializableFunction<Integer, String> f = (Integer i) -> {
                            SerializableFunction<Integer, String> nf = (Integer j) -> 'c' + j
                            nf(i) + 'c'
                        }
                        it.writeObject(f)
                    }

                    [out1.toByteArray(), out2.toByteArray(), out3.toByteArray()]
                }
            }

            def (serializedLambdaBytes1, serializedLambdaBytes2, serializedLambdaBytes3) = C.test()

            new ByteArrayInputStream(serializedLambdaBytes1).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'a1'
            }

            new ByteArrayInputStream(serializedLambdaBytes2).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'b1'
            }

            new ByteArrayInputStream(serializedLambdaBytes3).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'c1c'
            }
        '''
    }

    @Test
    void testDeserializeNestedLambda4() {
        assertScript '''
            interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
            }

            @groovy.transform.CompileStatic
            class C {
                static test() {
                    def out1 = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f1 = (Integer i) -> 'a' + i
                    out1.withObjectOutputStream {
                        it.writeObject(f1)
                    }

                    def out2 = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f2 = (Integer i) -> 'b' + i
                    out2.withObjectOutputStream {
                        it.writeObject(f2)
                    }

                    // nested lambda expression
                    def out3 = new ByteArrayOutputStream()
                    SerializableFunction<Integer, String> f3 = (Integer i) -> {
                        SerializableFunction<Integer, String> nf = (Integer j) -> 'c' + j
                        nf(i) + 'c'
                    }
                    out3.withObjectOutputStream {
                        it.writeObject(f3)
                    }

                    [out1.toByteArray(), out2.toByteArray(), out3.toByteArray()]
                }
            }

            def (serializedLambdaBytes1, serializedLambdaBytes2, serializedLambdaBytes3) = C.test()

            new ByteArrayInputStream(serializedLambdaBytes1).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'a1'
            }

            new ByteArrayInputStream(serializedLambdaBytes2).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'b1'
            }

            new ByteArrayInputStream(serializedLambdaBytes3).withObjectInputStream(this.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert f.apply(1) == 'c1c'
            }
        '''
    }

    // GROOVY-9146
    @Test
    void testScriptWithExistingMainCS() {
        assertScript shell, '''
            static void main(args) {
                Function<String, String> lower = String::toLowerCase
                assert lower.toString().contains('$$Lambda')
            }
        '''
    }

    // GROOVY-9770
    @Test
    void testLambdaClassIsntSynthetic() {
        assertScript shell, '''
            class Foo {
                def bar(String arg) {
                    Function<String, String> fun = (String s) -> s.toUpperCase()
                    fun.apply(arg)
                }
            }

            assert new Foo().bar('hello') == 'HELLO'
            assert this.class.classLoader.loadClass('Foo$_bar_lambda1').modifiers == 25 // public(1) + static(8) + final(16)
        '''
    }

    // GROOVY-11905
    @Nested
    class NonCapturingLambdaOptimizationTest extends AbstractBytecodeTestCase {
        @Test
        void testNonCapturingLambdaWithFunctionInStaticMethod() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e + 1).toList()
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaWithFunctionInInstanceMethodWithoutThisAccess() {
            assertScript shell,  '''
                class C {
                    void test() {
                        assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e + 1).toList()
                    }
                }
                new C().test()
            '''
        }

        @Test
        void testNonCapturingLambdaWithPredicate() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        assert [2, 4] == [1, 2, 3, 4].stream().filter(e -> e % 2 == 0).toList()
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaWithSupplier() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        Supplier<String> s = () -> 'constant'
                        assert s.get() == 'constant'
                        assert 'hello' == Optional.<String>empty().orElseGet(() -> 'hello')
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaWithBiFunction() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        BiFunction<Integer, Integer, Integer> f = (a, b) -> a + b
                        assert f.apply(3, 4) == 7
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaWithComparator() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        assert [3, 2, 1] == [1, 2, 3].stream().sorted((a, b) -> b.compareTo(a)).toList()
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaWithPrimitiveParameterType() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        IntUnaryOperator op = (int i) -> i * 2
                        assert op.applyAsInt(5) == 10
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaWithCustomFunctionalInterface() {
            assertScript shell,  '''
                interface Transformer<I, O> {
                    O transform(I input)
                }
                class C {
                    static void test() {
                        Transformer<String, Integer> t = (String s) -> s.length()
                        assert t.transform('hello') == 5
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaCallingStaticMethodOnly() {
            assertScript shell,  '''
                class C {
                    static String prefix() { 'Hi ' }
                    static void test() {
                        assert ['Hi 1', 'Hi 2'] == [1, 2].stream().map(e -> C.prefix() + e).toList()
                    }
                }
                C.test()
            '''
        }

        @Test
        void testMultipleNonCapturingLambdasInSameMethod() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        Function<Integer, Integer> f = (Integer x) -> x + 1
                        Function<Integer, String>  g = (Integer x) -> 'v' + x
                        Predicate<Integer>         p = (Integer x) -> x > 2
                        assert f.apply(1) == 2
                        assert g.apply(1) == 'v1'
                        assert p.test(3) && !p.test(1)
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaInStaticInitializerBlock() {
            assertScript shell,  '''
                class C {
                    static List<Integer> result
                    static { result = [1, 2, 3].stream().map(e -> e * 2).toList() }
                }
                assert C.result == [2, 4, 6]
            '''

            def script = '''
                @CompileStatic
                class C {
                    static IntUnaryOperator op
                    static {
                        op = (int i) -> i + 1
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$__clinit__lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall(I)I'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: '<clinit>', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC applyAsInt()Ljava/util/function/IntUnaryOperator;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C$__clinit__lambda1.doCall(I)I'
            ])
            assert !outerBytecode.hasSequence(['NEW C$__clinit__lambda1'])
        }

        @Test
        void testNonCapturingLambdaInFieldInitializer() {
            assertScript shell,  '''
                class C {
                    IntUnaryOperator op = (int i) -> i + 1
                    void test() { assert op.applyAsInt(5) == 6 }
                }
                new C().test()
            '''

            def script = '''
                @CompileStatic
                class C {
                    IntUnaryOperator op = (int i) -> i + 1
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall(I)I'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: '<init>', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC applyAsInt()Ljava/util/function/IntUnaryOperator;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C$_lambda1.doCall(I)I'
            ])
            assert !outerBytecode.hasSequence(['NEW C$_lambda1'])
        }

        @Test
        void testNonCapturingLambdaInInterfaceDefaultMethod() {
            assertScript shell,  '''
                interface Processor {
                    default List<Integer> process(List<Integer> input) {
                        input.stream().map(e -> e + 1).toList()
                    }
                }
                class C implements Processor {}
                assert new C().process([1, 2, 3]) == [2, 3, 4]
            '''

            def script = '''
                @CompileStatic
                interface Processor {
                    default IntUnaryOperator process() {
                        (int i) -> i + 1
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Processor\\$_process_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall(I)I'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Processor', method: 'process', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC applyAsInt()Ljava/util/function/IntUnaryOperator;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Processor$_process_lambda1.doCall(I)I'
            ])
            assert !outerBytecode.hasSequence(['NEW Processor$_process_lambda1'])
        }

        @Test
        void testNonCapturingLambdaSingletonIdentity() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        def identities = new HashSet()
                        for (int i = 0; i < 5; i++) {
                            Function<Integer, Integer> f = (Integer x) -> x + 1
                            identities.add(System.identityHashCode(f))
                        }
                        assert identities.size() == 1 : 'non-capturing lambda should be a singleton'
                    }
                }
                C.test()
            '''
        }

        @Test
        void testCapturingLambdaCreatesDistinctInstances() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        def identities = new HashSet()
                        for (int i = 0; i < 3; i++) {
                            int captured = i
                            Function<Integer, Integer> f = (Integer x) -> x + captured
                            identities.add(System.identityHashCode(f))
                            assert f.apply(10) == 10 + i
                        }
                        assert identities.size() == 3 : 'capturing lambda should create different instances'
                    }
                }
                C.test()
            '''
        }

        @Test
        void testCapturingLocalVariable() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        String x = '#'
                        assert ['#1', '#2'] == [1, 2].stream().map(e -> x + e).toList()
                    }
                }
                C.test()
            '''
        }

        @Test
        void testAccessingThis() {
            assertScript shell,  '''
                class C {
                    String prefix = 'Hi '
                    void test() {
                        assert ['Hi 1', 'Hi 2'] == [1, 2].stream().map(e -> this.prefix + e).toList()
                    }
                }
                new C().test()
            '''
        }

        @Test
        void testCallingInstanceMethod() {
            assertScript shell,  '''
                class C {
                    String greet(int i) { "Hello $i" }
                    void test() {
                        assert ['Hello 1', 'Hello 2'] == [1, 2].stream().map(e -> greet(e)).toList()
                    }
                }
                new C().test()
            '''
        }

        @Test
        void testCallingSuperMethod() {
            assertScript shell,  '''
                class Base {
                    String greet(int i) { "Hello $i" }
                }
                class C extends Base {
                    void test() {
                        assert ['Hello 1', 'Hello 2'] == [1, 2].stream().map(e -> super.greet(e)).toList()
                    }
                }
                new C().test()
            '''
        }

        @Test
        void testNonCapturingLambdaWithThisStringLiteralRemainsSingleton() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        def identities = new HashSet()
                        for (int i = 0; i < 5; i++) {
                            Supplier<String> supplier = () -> 'this'
                            identities.add(System.identityHashCode(supplier))
                            assert supplier.get() == 'this'
                        }
                        assert identities.size() == 1 : 'non-capturing lambda with string literal this should still be a singleton'
                    }
                }
                C.test()
            '''

            def script = '''
                @CompileStatic
                class C {
                    static Supplier<String> create() {
                        () -> 'this'
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasStrictSequence([
                'public static doCall()Ljava/lang/Object;',
                'L0'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW C$_create_lambda1'])
        }

        @Test
        void testNonCapturingSerializableLambdaCanBeSerialized() {
            assertScript shell,  '''
                import java.io.*
                interface SerFunc<I,O> extends Serializable, Function<I,O> {}
                byte[] test() {
                    try (def out = new ByteArrayOutputStream()) {
                        out.withObjectOutputStream {
                            SerFunc<Integer, String> f = ((Integer i) -> 'a' + i)
                            it.writeObject(f)
                        }
                        out.toByteArray()
                    }
                }
                assert test().length > 0
            '''
        }

        @Test
        void testNonCapturingSerializableLambdaRoundTrips() {
            assertScript shell,  '''
                package tests.lambda
                class C {
                    static byte[] test() {
                        def out = new ByteArrayOutputStream()
                        out.withObjectOutputStream { it ->
                            SerFunc<Integer, String> f = (Integer i) -> 'a' + i
                            it.writeObject(f)
                        }
                        out.toByteArray()
                    }
                    static main(args) {
                        new ByteArrayInputStream(C.test()).withObjectInputStream(C.classLoader) {
                            SerFunc<Integer, String> f = (SerFunc<Integer, String>) it.readObject()
                            assert f.apply(1) == 'a1'
                        }
                    }
                    interface SerFunc<I,O> extends Serializable, Function<I,O> {}
                }
            '''
        }

        @Test
        void testNonCapturingSerializableLambdaSingletonIdentity() {
            assertScript shell,  '''
                interface SerFunc<I,O> extends Serializable, Function<I,O> {}
                class C {
                    static void test() {
                        def identities = new HashSet()
                        for (int i = 0; i < 5; i++) {
                            SerFunc<Integer, Integer> f = (Integer x) -> x + 1
                            identities.add(System.identityHashCode(f))
                        }
                        assert identities.size() == 1 : 'non-capturing serializable lambda should be a singleton'
                    }
                }
                C.test()
            '''
        }

        @Test
        void testCapturingSerializableLambdaStillRoundTrips() {
            assertScript shell,  '''
                package tests.lambda
                class C {
                    byte[] test() {
                        def out = new ByteArrayOutputStream()
                        out.withObjectOutputStream {
                            String s = 'a'
                            SerFunc<Integer, String> f = (Integer i) -> s + i
                            it.writeObject(f)
                        }
                        out.toByteArray()
                    }
                    static main(args) {
                        new ByteArrayInputStream(C.newInstance().test()).withObjectInputStream(C.classLoader) {
                            SerFunc<Integer, String> f = (SerFunc<Integer, String>) it.readObject()
                            assert f.apply(1) == 'a1'
                        }
                    }
                    interface SerFunc<I,O> extends Serializable, Function<I,O> {}
                }
            '''
        }

        @Test
        void testCapturingLambdaWithRunnable() {
            assertScript shell,  '''
                import java.util.concurrent.atomic.AtomicBoolean
                class C {
                    static void test() {
                        AtomicBoolean ran = new AtomicBoolean(false)
                        Runnable r = () -> ran.set(true)
                        r.run()
                        assert ran.get()
                    }
                }
                C.test()
            '''
        }

        @Test
        void testCapturingLambdaWithConsumer() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        def result = []
                        Consumer<Integer> c = (Integer x) -> result.add(x * 2)
                        c.accept(3)
                        c.accept(5)
                        assert result == [6, 10]
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaAccessingStaticField() {
            assertScript shell,  '''
                class C {
                    static final int OFFSET = 100
                    static void test() {
                        assert [101, 102, 103] == [1, 2, 3].stream().map(e -> e + OFFSET).toList()
                    }
                }
                C.test()
            '''

            def script = '''
                @CompileStatic
                class C {
                    static final int OFFSET = 100
                    static IntUnaryOperator create() {
                        (int i) -> i + OFFSET
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasStrictSequence([
                'public static doCall(I)I',
                'L0'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC applyAsInt()Ljava/util/function/IntUnaryOperator;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C$_create_lambda1.doCall(I)I'
            ])
            assert !outerBytecode.hasSequence(['NEW C$_create_lambda1'])
        }

        @Test
        void testQualifiedOuterThisRemainsCapturing() {
            assertScript shell,  '''
                class Outer {
                    String name = 'outer'
                    class Inner {
                        void test() {
                            Function<Integer, String> f = (Integer x) -> Outer.this.name + x
                            assert f.apply(1) == 'outer1'
                        }
                    }
                    void test() { new Inner().test() }
                }
                new Outer().test()
            '''
        }

        @Test
        void testNestedNonCapturingLambdas() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        Function<List<Integer>, List<Integer>> f = (List<Integer> list) ->
                            list.stream().map(e -> e * 2).toList()
                        assert f.apply([1, 2, 3]) == [2, 4, 6]
                    }
                }
                C.test()
            '''
        }

        @Test
        void testNonCapturingLambdaInStaticMethodUsesStaticDoCall() {
            def bytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', '''
                @CompileStatic
                class C {
                    static IntUnaryOperator create() {
                        (int i) -> i * 2
                    }
                }
            ''')
            assert bytecode.hasStrictSequence([
                'public static doCall(I)I',
                'L0'
            ])
        }

        @Test
        void testNonCapturingLambdaInInstanceMethodWithoutThisAccessUsesCaptureFreeInvokeDynamic() {
            def bytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', '''
                @CompileStatic
                class C {
                    IntUnaryOperator create() {
                        (int i) -> i + 1
                    }
                }
            ''')
            assert bytecode.hasSequence([
                'INVOKEDYNAMIC applyAsInt()Ljava/util/function/IntUnaryOperator;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C$_create_lambda1.doCall(I)I'
            ])
            assert !bytecode.hasSequence(['NEW C$_create_lambda1'])
        }

        @Test
        void testCapturingLambdaRetainsInstanceDoCallAndCapturedReceiver() {
            def script = '''
                @CompileStatic
                class C {
                    static IntUnaryOperator create() {
                        int captured = 1
                        IntUnaryOperator op = (int i) -> i + captured
                        op
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public doCall(I)I'])
            assert !lambdaBytecode.hasSequence(['public static doCall(I)I'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assert outerBytecode.hasSequence([
                'NEW C$_create_lambda1',
                'INVOKEDYNAMIC applyAsInt(LC$_create_lambda1;)Ljava/util/function/IntUnaryOperator;',
                'C$_create_lambda1.doCall(I)I'
            ])
        }

        @Test
        void testSuperMethodCallRetainsInstanceDoCallAndCapturedReceiver() {
            def script = '''
                @CompileStatic
                class Base {
                    String greet(int i) { "Hello $i" }
                }
                @CompileStatic
                class C extends Base {
                    Function<Integer, String> create() {
                        (Integer i) -> super.greet(i)
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public doCall(Ljava/lang/Integer;)Ljava/lang/Object;'])
            assert !lambdaBytecode.hasSequence(['public static doCall(Ljava/lang/Integer;)Ljava/lang/Object;'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assert outerBytecode.hasSequence([
                'NEW C$_create_lambda1',
                'INVOKEDYNAMIC apply(LC$_create_lambda1;)Ljava/util/function/Function;',
                'C$_create_lambda1.doCall(Ljava/lang/Integer;)Ljava/lang/Object;'
            ])
        }

        @Test
        void testNonCapturingSerializableLambdaDeserializeHelperSkipsCapturedArgLookup() {
            def bytecode = compileStaticBytecode(classNamePattern: 'C', method: '$deserializeLambda_C$_create_lambda1$', '''
                @CompileStatic
                class C {
                    static SerFunc<Integer, String> create() {
                        (Integer i) -> 'a' + i
                    }
                    interface SerFunc<I,O> extends Serializable, Function<I,O> {}
                }
            ''')
            assert !bytecode.hasSequence([SERIALIZED_LAMBDA_GET_CAPTURED_ARG])
        }

        @Test
        void testCapturingSerializableLambdaDeserializeHelperReadsCapturedArg() {
            def bytecode = compileStaticBytecode(classNamePattern: 'C', method: '$deserializeLambda_C$_create_lambda1$', '''
                @CompileStatic
                class C {
                    static SerFunc<Integer, String> create() {
                        String prefix = 'a'
                        SerFunc<Integer, String> f = (Integer i) -> prefix + i
                        f
                    }
                    interface SerFunc<I,O> extends Serializable, Function<I,O> {}
                }
            ''')
            assert bytecode.hasSequence([SERIALIZED_LAMBDA_GET_CAPTURED_ARG])
        }

        @Test
        void testNonCapturingLambdaCallingQualifiedStaticMethodOnlyUsesCaptureFreeInvokeDynamic() {
            def script = '''
                @CompileStatic
                class C {
                    static String prefix() { 'Hi ' }
                    static Function<Integer, String> create() {
                        (Integer i) -> C.prefix() + i
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall(Ljava/lang/Integer;)Ljava/lang/Object;'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC apply()Ljava/util/function/Function;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C$_create_lambda1.doCall(Ljava/lang/Integer;)Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW C$_create_lambda1'])
        }

        @Test
        void testNonCapturingComparatorLambdaUsesCaptureFreeInvokeDynamic() {
            def script = '''
                @CompileStatic
                class C {
                    static java.util.Comparator<Integer> create() {
                        (Integer left, Integer right) -> right.compareTo(left)
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall(Ljava/lang/Integer;Ljava/lang/Integer;)I'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC compare()Ljava/util/Comparator;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C$_create_lambda1.doCall(Ljava/lang/Integer;Ljava/lang/Integer;)I'
            ])
            assert !outerBytecode.hasSequence(['NEW C$_create_lambda1'])
        }

        @Test
        void testNonCapturingLambdaWithCustomFunctionalInterfaceUsesCaptureFreeInvokeDynamic() {
            def script = '''
                interface Transformer<I, O> {
                    O transform(I input)
                }
                @CompileStatic
                class C {
                    static Transformer<String, Integer> create() {
                        (String s) -> s.length()
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall(Ljava/lang/String;)Ljava/lang/Object;'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC transform()LTransformer;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'C$_create_lambda1.doCall(Ljava/lang/String;)Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW C$_create_lambda1'])
        }

        @Test
        void testNonCapturingSerializableLambdaUsesCaptureFreeAltMetafactory() {
            def script = '''
                @CompileStatic
                class C {
                    static SerFunc<Integer, String> create() {
                        (Integer i) -> 'a' + i
                    }
                    interface SerFunc<I,O> extends Serializable, Function<I,O> {}
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'C\\$_create_lambda1', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall(Ljava/lang/Integer;)Ljava/lang/Object;'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'C', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC apply()LC$SerFunc;',
                'java/lang/invoke/LambdaMetafactory.altMetafactory',
                'C$_create_lambda1.doCall(Ljava/lang/Integer;)Ljava/lang/Object;'
            ])
            assert outerBytecode.hasSequence(['CHECKCAST java/io/Serializable'])
            assert !outerBytecode.hasSequence(['NEW C$_create_lambda1'])
        }

        @Test
        void testNonCapturingLambdaWithExceptionInBody() {
            assertScript shell,  '''
                class C {
                    static void test() {
                        Function<String, Integer> f = (String s) -> {
                            if (s == null) throw new IllegalArgumentException('null input')
                            return s.length()
                        }
                        assert f.apply('hello') == 5
                        try {
                            f.apply(null)
                            assert false : 'should have thrown'
                        } catch (IllegalArgumentException e) {
                            assert e.message == 'null input'
                        }
                    }
                }
                C.test()
            '''
        }

        @Test
        void testAccessingThisObjectRemainsCapturing() {
            assertScript shell,  '''
                class C {
                    String name = 'test'
                    void test() {
                        Function<Integer, String> f = (Integer x) -> thisObject.name + x
                        assert f.apply(1) == 'test1'
                    }
                }
                new C().test()
            '''

            def bytecode = compileStaticBytecode(classNamePattern: 'C\\$_test_lambda1', method: 'doCall', '''
                @CompileStatic
                class C {
                    String name = 'test'
                    Function<Integer, String> test() {
                        (Integer x) -> thisObject.name + x
                    }
                }
            ''')
            assert bytecode.hasSequence(['public doCall(Ljava/lang/Integer;)Ljava/lang/Object;'])
            assert !bytecode.hasSequence(['public static doCall(Ljava/lang/Integer;)Ljava/lang/Object;'])
        }

        @Test
        void testInnerClassLambdaUsingOuterStaticMembersQualifiesStaticCalls() {
            assertScript shell, '''
                class Outer {
                    static String label = 'outer'
                    static boolean isReady() { true }
                    static String suffix() { '!' }
                    class Inner {
                        Supplier<String> create() {
                            () -> ready ? label.toUpperCase() + suffix() : 'never'
                        }
                    }
                }

                def left = new Outer().new Inner().create()
                def right = new Outer().new Inner().create()
                assert left.get() == 'OUTER!'
                assert right.get() == 'OUTER!'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    static String label = 'outer'
                    static boolean isReady() { true }
                    static String suffix() { '!' }
                    class Inner {
                        Supplier<String> create() {
                            () -> ready ? label.toUpperCase() + suffix() : 'never'
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC Outer.isReady ()Z',
                'INVOKESTATIC Outer.getLabel ()Ljava/lang/String;',
                'INVOKESTATIC Outer.suffix ()Ljava/lang/String;'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testSerializableLambdasInSameClassShareSingleDeserializeDispatcher() {
            assertScript shell, COMMON_IMPORTS + '''
                import java.io.ByteArrayInputStream
                import java.io.ByteArrayOutputStream

                @CompileStatic
                class C {
                    interface SerFunc<I, O> extends Serializable, Function<I, O> {}

                    static SerFunc<Integer, String> createLeft() {
                        (Integer i) -> 'a' + i
                    }

                    static SerFunc<Integer, String> createRight() {
                        (Integer i) -> 'b' + (i * 2)
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
                C.SerFunc<Integer, String> left = C.deserialize(C.serialize(C.createLeft()))
                C.SerFunc<Integer, String> right = C.deserialize(C.serialize(C.createRight()))
                assert left.apply(1) == 'a1'
                assert right.apply(2) == 'b4'
            '''
        }

        @Test
        void testInnerClassLambdaUsingOuterStaticBeanPropertiesStaysCaptureFree() {
            assertScript shell, COMMON_IMPORTS + '''
                class Outer {
                    static boolean isReady() { true }
                    static String getLabel() { 'outer' }
                    static String getSuffix() { '!' }

                    class Inner {
                        Supplier<String> create() {
                            () -> ready ? label.toUpperCase() + suffix : 'never'
                        }
                    }
                }

                def supplier = new Outer().new Inner().create()
                assert supplier.get() == 'OUTER!'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    static boolean isReady() { true }
                    static String getLabel() { 'outer' }
                    static String getSuffix() { '!' }

                    class Inner {
                        Supplier<String> create() {
                            () -> ready ? label.toUpperCase() + suffix : 'never'
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC Outer.isReady ()Z',
                'INVOKESTATIC Outer.getLabel ()Ljava/lang/String;',
                'INVOKESTATIC Outer.getSuffix ()Ljava/lang/String;'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testInnerClassLambdaUsingInterfaceStaticMembersStaysCaptureFree() {
            assertScript shell, COMMON_IMPORTS + '''
                interface Labels {
                    String LABEL = 'outer'
                    static boolean isReady() { true }
                    static String getSuffix() { '!' }
                }

                class Outer implements Labels {
                    class Inner {
                        Supplier<String> create() {
                            () -> ready ? LABEL.toUpperCase() + suffix : 'never'
                        }
                    }
                }

                def left = new Outer().new Inner().create()
                def right = new Outer().new Inner().create()
                assert left.is(right) : 'interface static members should keep the lambda capture-free'
                assert left.get() == 'OUTER!'
                assert right.get() == 'OUTER!'
            '''

            def script = '''
                @CompileStatic
                interface Labels {
                    String LABEL = 'outer'
                    static boolean isReady() { true }
                    static String getSuffix() { '!' }
                }

                @CompileStatic
                class Outer implements Labels {
                    class Inner {
                        Supplier<String> create() {
                            () -> ready ? LABEL.toUpperCase() + suffix : 'never'
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC Labels.isReady ()Z',
                'GETSTATIC Labels.LABEL :',
                'INVOKESTATIC Labels.getSuffix ()Ljava/lang/String;'
            ])
            assert !lambdaBytecode.hasSequence(['CHECKCAST groovy/lang/GroovyObject'])
            assert !lambdaBytecode.hasSequence(['INVOKEINTERFACE groovy/lang/GroovyObject.getProperty'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testInnerClassLambdaUsingInheritedStaticMembersStaysCaptureFree() {
            assertScript shell, COMMON_IMPORTS + '''
                class Base {
                    static boolean isReady() { true }
                    static String getLabel() { 'outer' }
                    static String getSuffix() { '!' }
                }

                class Outer extends Base {
                    class Inner {
                        Supplier<String> create() {
                            () -> ready ? label.toUpperCase() + suffix : 'never'
                        }
                    }
                }

                def left = new Outer().new Inner().create()
                def right = new Outer().new Inner().create()
                assert left.is(right) : 'inherited static members should keep the lambda capture-free'
                assert left.get() == 'OUTER!'
                assert right.get() == 'OUTER!'
            '''

            def script = '''
                @CompileStatic
                class Base {
                    static boolean isReady() { true }
                    static String getLabel() { 'outer' }
                    static String getSuffix() { '!' }
                }

                @CompileStatic
                class Outer extends Base {
                    class Inner {
                        Supplier<String> create() {
                            () -> ready ? label.toUpperCase() + suffix : 'never'
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC Base.isReady ()Z',
                'INVOKESTATIC Base.getLabel ()Ljava/lang/String;',
                'INVOKESTATIC Base.getSuffix ()Ljava/lang/String;'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testInnerClassLambdaUsingQualifiedOuterThisStaticMembersStaysCaptureFree() {
            assertScript shell, COMMON_IMPORTS + '''
                class Outer {
                    static String label = 'outer'
                    static String suffix() { '!' }

                    class Inner {
                        Supplier<String> create() {
                            () -> Outer.this.@label.toUpperCase() + Outer.this.suffix()
                        }
                    }
                }

                def left = new Outer().new Inner().create()
                def right = new Outer().new Inner().create()
                assert left.is(right) : 'qualified outer-this static access should stay capture-free'
                assert left.get() == 'OUTER!'
                assert right.get() == 'OUTER!'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    static String label = 'outer'
                    static String suffix() { '!' }

                    class Inner {
                        Supplier<String> create() {
                            () -> Outer.this.@label.toUpperCase() + Outer.this.suffix()
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'GETSTATIC Outer.label :',
                'INVOKESTATIC Outer.suffix ()Ljava/lang/String;'
            ])
            assert !lambdaBytecode.hasSequence(['CHECKCAST groovy/lang/GroovyObject'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testStaticNestedClassLambdaUsingOwnStaticMethodStaysNonCapturing() {
            assertScript shell, COMMON_IMPORTS + '''
                class Outer {
                    static class Nested {
                        static String helper() { 'nested' }

                        Supplier<String> create() {
                            () -> helper().toUpperCase()
                        }
                    }
                }

                def left = new Outer.Nested().create()
                def right = new Outer.Nested().create()
                assert left.is(right) : 'static nested-class helpers should not be mistaken for outer-instance capture'
                assert left.get() == 'NESTED'
                assert right.get() == 'NESTED'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    static class Nested {
                        static String helper() { 'nested' }

                        Supplier<String> create() {
                            () -> helper().toUpperCase()
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Nested\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC Outer$Nested.helper ()Ljava/lang/String;',
                'INVOKEVIRTUAL java/lang/String.toUpperCase ()Ljava/lang/String;'
            ])

            def nestedBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Nested', method: 'create', script)
            assert nestedBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Nested$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !nestedBytecode.hasSequence(['NEW Outer$Nested$_create_lambda1'])
        }

        @Test
        void testInnerClassLambdaUsingStaticallyImportedHelperMethodStaysNonCapturing() {
            assertScript shell, COMMON_IMPORTS + '''
                import static Helper.helper

                class Helper {
                    static String helper() { 'helper' }
                }

                class Outer {
                    class Inner {
                        Supplier<String> create() {
                            () -> helper().toUpperCase()
                        }
                    }
                }

                def left = new Outer().new Inner().create()
                def right = new Outer().new Inner().create()
                assert left.is(right) : 'statically imported helper methods should not be treated as captured outer state'
                assert left.get() == 'HELPER'
                assert right.get() == 'HELPER'
            '''

            def script = '''
                import static Helper.helper

                @CompileStatic
                class Helper {
                    static String helper() { 'helper' }
                }

                @CompileStatic
                class Outer {
                    class Inner {
                        Supplier<String> create() {
                            () -> helper().toUpperCase()
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC Helper.helper ()Ljava/lang/String;',
                'INVOKEVIRTUAL java/lang/String.toUpperCase ()Ljava/lang/String;'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testInnerClassLambdaUsingStaticallyImportedInterfaceMethodStaysNonCapturing() {
            assertScript shell, COMMON_IMPORTS + '''
                import static HelperApi.helper

                interface HelperApi {
                    static String helper() { 'iface' }
                }

                class Outer implements HelperApi {
                    class Inner {
                        Supplier<String> create() {
                            () -> helper().toUpperCase()
                        }
                    }
                }

                def left = new Outer().new Inner().create()
                def right = new Outer().new Inner().create()
                assert left.is(right) : 'statically imported interface methods should stay receiver-free inside inner lambdas'
                assert left.get() == 'IFACE'
                assert right.get() == 'IFACE'
            '''

            def script = '''
                import static HelperApi.helper

                @CompileStatic
                interface HelperApi {
                    static String helper() { 'iface' }
                }

                @CompileStatic
                class Outer implements HelperApi {
                    class Inner {
                        Supplier<String> create() {
                            () -> helper().toUpperCase()
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC HelperApi.helper ()Ljava/lang/String;',
                'INVOKEVIRTUAL java/lang/String.toUpperCase ()Ljava/lang/String;'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testInnerClassLambdaMutatingImplicitStaticPropertyStaysCaptureFree() {
            assertScript shell, COMMON_IMPORTS + '''
                class Outer {
                    private static String backing = 'outer'
                    static String getLabel() { backing }
                    static void setLabel(String value) { backing = value }

                    class Inner {
                        Supplier<String> create() {
                            () -> {
                                label += '!'
                                label
                            }
                        }
                    }
                }

                Outer.label = 'outer'
                def left = new Outer().new Inner().create()
                def right = new Outer().new Inner().create()
                assert left.is(right) : 'static property mutation should not force receiver capture'
                assert left.get() == 'outer!'
                Outer.label = 'outer'
                assert right.get() == 'outer!'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    private static String backing = 'outer'
                    static String getLabel() { backing }
                    static void setLabel(String value) { backing = value }

                    class Inner {
                        Supplier<String> create() {
                            () -> {
                                label += '!'
                                label
                            }
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC Outer.getLabel ()Ljava/lang/String;',
                'INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.setProperty'
            ])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testInnerClassLambdaMutatingQualifiedOuterThisStaticPropertyStaysCaptureFree() {
            assertScript shell, COMMON_IMPORTS + '''
                class Outer {
                    private static String backing = 'outer'
                    static String getLabel() { backing }
                    static void setLabel(String value) { backing = value }

                    class Inner {
                        Supplier<String> create() {
                            () -> {
                                Outer.this.label += '!'
                                Outer.this.label
                            }
                        }
                    }
                }

                Outer.label = 'outer'
                def left = new Outer().new Inner().create()
                def right = new Outer().new Inner().create()
                assert left.is(right) : 'qualified outer-this static property mutation should stay capture-free'
                assert left.get() == 'outer!'
                Outer.label = 'outer'
                assert right.get() == 'outer!'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    private static String backing = 'outer'
                    static String getLabel() { backing }
                    static void setLabel(String value) { backing = value }

                    class Inner {
                        Supplier<String> create() {
                            () -> {
                                Outer.this.label += '!'
                                Outer.this.label
                            }
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])
            assert lambdaBytecode.hasSequence([
                'INVOKESTATIC Outer.getLabel ()Ljava/lang/String;',
                'INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.setProperty'
            ])
            assert !lambdaBytecode.hasSequence(['CHECKCAST groovy/lang/GroovyObject'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'INVOKEDYNAMIC get()Ljava/util/function/Supplier;',
                'java/lang/invoke/LambdaMetafactory.metafactory',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
            assert !outerBytecode.hasSequence(['NEW Outer$Inner$_create_lambda1'])
        }

        @Test
        void testInnerClassLambdaReadingQualifiedOuterThisInstancePropertyRemainsCapturing() {
            assertScript shell, COMMON_IMPORTS + '''
                class Outer {
                    String name = 'outer'

                    class Inner {
                        Supplier<String> create() {
                            () -> Outer.this.name + ':' + Outer.this.name
                        }
                    }
                }

                def inner = new Outer().new Inner()
                def left = inner.create()
                def right = inner.create()
                assert !left.is(right) : 'qualified outer-instance property access must keep the lambda capturing'
                assert left.get() == 'outer:outer'
                assert right.get() == 'outer:outer'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    String name = 'outer'

                    class Inner {
                        Supplier<String> create() {
                            () -> Outer.this.name + ':' + Outer.this.name
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public doCall()Ljava/lang/Object;'])
            assert !lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'NEW Outer$Inner$_create_lambda1',
                'INVOKEDYNAMIC get(LOuter$Inner$_create_lambda1;)Ljava/util/function/Supplier;',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
        }

        @Test
        void testInnerClassReadingQualifiedOuterThisInstanceFieldRemainsCapturing() {
            assertScript shell, COMMON_IMPORTS + '''
                class Outer {
                    String name = 'outer'

                    class Inner {
                        Supplier<String> create() {
                            () -> Outer.this.@name + ':' + Outer.this.@name
                        }
                    }
                }

                def inner = new Outer().new Inner()
                def left = inner.create()
                def right = inner.create()
                assert !left.is(right) : 'qualified outer-instance field access must keep the lambda capturing'
                assert left.get() == 'outer:outer'
                assert right.get() == 'outer:outer'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    String name = 'outer'

                    class Inner {
                        Supplier<String> create() {
                            () -> Outer.this.@name + ':' + Outer.this.@name
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public doCall()Ljava/lang/Object;'])
            assert !lambdaBytecode.hasSequence(['public static doCall()Ljava/lang/Object;'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'NEW Outer$Inner$_create_lambda1',
                'INVOKEDYNAMIC get(LOuter$Inner$_create_lambda1;)Ljava/util/function/Supplier;',
                'Outer$Inner$_create_lambda1.doCall()Ljava/lang/Object;'
            ])
        }

        @Test
        void testInnerClassLambdaUsingImplicitOuterInstancePropertyRemainsCapturing() {
            assertScript shell, COMMON_IMPORTS + '''
                class Outer {
                    String name = 'outer'

                    class Inner {
                        Function<Integer, String> create() {
                            (Integer i) -> name + i
                        }
                    }
                }

                def fn = new Outer().new Inner().create()
                assert fn.apply(1) == 'outer1'
            '''

            def script = '''
                @CompileStatic
                class Outer {
                    String name = 'outer'

                    class Inner {
                        Function<Integer, String> create() {
                            (Integer i) -> name + i
                        }
                    }
                }
            '''
            def lambdaBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner\\$_create_lambda\\d+', method: 'doCall', script)
            assert lambdaBytecode.hasSequence(['public doCall(Ljava/lang/Integer;)Ljava/lang/Object;'])
            assert !lambdaBytecode.hasSequence(['public static doCall(Ljava/lang/Integer;)Ljava/lang/Object;'])

            def outerBytecode = compileStaticBytecode(classNamePattern: 'Outer\\$Inner', method: 'create', script)
            assert outerBytecode.hasSequence([
                'NEW Outer$Inner$_create_lambda1',
                'INVOKEDYNAMIC apply(LOuter$Inner$_create_lambda1;)Ljava/util/function/Function;',
                'Outer$Inner$_create_lambda1.doCall(Ljava/lang/Integer;)Ljava/lang/Object;'
            ])
        }

        private compileStaticBytecode(final Map options = [:], final String script) {
            compile(options, COMMON_IMPORTS + script)
        }

        private static final String COMMON_IMPORTS = '''\
            import groovy.transform.CompileStatic
            import java.io.Serializable
            import java.util.function.Consumer
            import java.util.function.Function
            import java.util.function.IntUnaryOperator
            import java.util.function.Supplier
            '''.stripIndent()
        private static final String SERIALIZED_LAMBDA_GET_CAPTURED_ARG = 'INVOKEVIRTUAL java/lang/invoke/SerializedLambda.getCapturedArg'
    }
}
