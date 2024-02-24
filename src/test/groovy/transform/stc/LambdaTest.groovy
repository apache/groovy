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
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class LambdaTest {

    private static final imports = '''\
        import groovy.transform.CompileStatic
        import java.util.function.*
        import java.util.stream.*
    '''

    @Test
    void testFunction() {
        assertScript imports + '''
            @CompileStatic f() {
                [1, 2, 3].stream().map(e -> e + 1).collect(Collectors.toList())
            }
            assert f() == [2, 3, 4]
        '''
    }

    @Test
    void testFunction2() {
        assertScript imports + '''
            @CompileStatic f() {
                [1, 2, 3].stream().map(e -> e.plus(1)).collect(Collectors.toList())
            }
            assert f() == [2, 3, 4]
        '''
    }

    @Test
    void testFunctionWithTypeArgument() {
        assertScript imports + '''
            @CompileStatic f() {
                [1, 2, 3].stream().<String>map(i -> null).collect(Collectors.toList())
            }
            assert f() == [null, null, null]
        '''
    }

    @Test
    void testBinaryOperator() {
        assertScript imports + '''
            @CompileStatic f() {
                [1, 2, 3].stream().reduce(7, (Integer r, Integer e) -> r + e)
            }
            assert f() == 13
        '''
    }

    @Test // GROOVY-8917
    void testBinaryOperatorWithoutExplicitTypes() {
        assertScript imports + '''
            @CompileStatic f() {
                [1, 2, 3].stream().reduce(7, (r, e) -> r + e)
            }
            assert f() == 13
        '''
    }

    @Test
    void testBinaryOperatorWithoutExplicitTypes2() {
        assertScript imports + '''
            @CompileStatic f() {
                BinaryOperator<Integer> accumulator = (r, e) -> r + e
                return [1, 2, 3].stream().reduce(7, accumulator)
            }
            assert f() == 13
        '''
    }

    @Test // GROOVY-10282
    void testBiFunctionAndBinaryOperatorWithSharedTypeParameter() {
        assertScript imports + '''
            @CompileStatic f() {
                IntStream.range(0, 10).boxed().reduce('', (s, i) -> s + '-', String::concat)
            }
            assert f() == '----------'
        '''
    }

    @Test
    void testPredicate1() {
        assertScript imports + '''
            @CompileStatic f() {
                def list = ['ab', 'bc', 'de']
                list.removeIf(e -> e.startsWith('a'))
                list
            }
            assert f() == ['bc', 'de']
        '''
    }

    @Test
    void testPredicate2() {
        assertScript imports + '''
            @CompileStatic f() {
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
        assertScript imports + '''
            @CompileStatic f() {
                def list = [1, 2, 3]
                list.replaceAll(e -> e + 10)
                list
            }
            assert f() == [11, 12, 13]
        '''
    }

    @Test
    void testBiConsumer() {
        assertScript imports + '''
            @CompileStatic f() {
                def map = [a: 1, b: 2, c: 3], list = []
                map.forEach((k, v) -> list.add(k + v))
                list
            }
            assert f() == ['a1', 'b2', 'c3']
        '''
    }

    @Test
    void testComparator() {
        assertScript imports + '''
            @CompileStatic class T {
                Comparator<Integer> c = (Integer a, Integer b) -> Integer.compare(a, b)
            }
            def t = new T()
            assert t.c.compare(0,0) == 0
        '''
    }

    @Test // GROOVY-10372
    void testComparator2() {
        def err = shouldFail imports + '''
            @CompileStatic class T {
                Comparator<Integer> c = (int a, String b) -> 42
            }
        '''
        assert err =~ /Cannot assign java.util.Comparator <java.lang.String> to: java.util.Comparator <Integer>/
    }

    @Test // GROOVY-9977
    void testComparator3() {
        assertScript imports + '''
            @CompileStatic class T {
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

    @Test // GROOVY-9997
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
            assertScript imports + """
                @CompileStatic f() {
                    def set = ['a', 'b', 'c'] as Set<String>
                    def map = set.stream().collect(Collectors.${spec}toMap(e -> e, e -> e))
                }
                assert f() == [a: 'a', b: 'b', c: 'c']
            """
        }
    }

    @Test
    void testCollectors2() {
        for (spec in ['', '<String,String,String>']) {
            assertScript imports + """
                @CompileStatic f() {
                    def set = ['a', 'b', 'c'] as Set<String>
                    def map = set.stream().collect(Collectors.${spec}toMap(e -> e, e -> e, (v, w) -> w))
                }
                assert f() == [a: 'a', b: 'b', c: 'c']
            """
        }
    }

    @Test // GROOVY-11304
    void testCollectors3() {
        assertScript imports + '''
            @CompileStatic f() {
                List<String> list = ['a', 'b', 'c']
                Map<String, Integer> map = list.stream().collect(
                    Collectors.toMap(Function.identity(), (k) -> 1, (v, w) -> v + w)
                )
            }
            assert f() == [a: 1, b: 1, c: 1]
        '''
    }

    @Test
    void testFunctionWithLocalVariables() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    String x = "#"
                    assert ['#1', '#2', '#3'] == [1, 2, 3].stream().map(e -> x + e).collect(Collectors.toList());
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    String x = "#"
                    Integer y = 23
                    assert ['23#1', '23#2', '23#3'] == [1, 2, 3].stream().map(e -> '' + y + x + e).collect(Collectors.toList())
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables3() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    String x = "x";
                    StringBuilder y = new StringBuilder("y");
                    assert ['yx1', 'yx2', 'yx3'] == [1, 2, 3].stream().map(e -> y + x + e).collect(Collectors.toList());
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables4() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    Function<Integer, String> f = p();
                    assert '#1' == f(1)
                }
                static Function<Integer, String> p() {
                    String x = "#"
                    Function<Integer, String> f = (Integer e) -> x + e
                    return f
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables5() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    Function<Integer, String> f = newInstance().p()
                    assert '#1' == f(1)
                }
                Function<Integer, String> p() {
                    String x = "#"
                    Function<Integer, String> f = (Integer e) -> x + e
                    return f
                }
            }
        '''
    }

    @Test
    void testFunctionWithStaticMethodCall() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    String x = "x"
                    StringBuilder y = new StringBuilder("y")
                    assert ['Hello yx1', 'Hello yx2', 'Hello yx3'] == [1, 2, 3].stream().map(e -> hello() + y + x + e).collect(Collectors.toList())
                }
                static String hello() {
                    return "Hello "
                }
            }
        '''
    }

    @Test
    void testFunctionWithStaticMethodCall2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    String x = "x"
                    StringBuilder y = new StringBuilder("y")
                    assert ['Hello yx1', 'Hello yx2', 'Hello yx3'] == [1, 2, 3].stream().map(e -> Tester.hello() + y + x + e).collect(Collectors.toList())
                }
                static String hello() {
                    return "Hello "
                }
            }
        '''
    }

    @Test
    void testFunctionWithInstanceMethodCall() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    newInstance().p()
                }
                void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ["Jochen", "Daniel"].stream().map(e -> hello() + e).collect(Collectors.toList());
                }
                String hello() {
                    return "Hello ";
                }
            }
        '''
    }

    @Test
    void testFunctionInConstructor() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    newInstance()
                }
                Tester() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ["Jochen", "Daniel"].stream().map(e -> hello() + e).collect(Collectors.toList());
                }
                String hello() {
                    return "Hello ";
                }
            }
        '''
    }

    @Test
    void testFunctionWithInstanceMethodCall2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    newInstance().p()
                }
                void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ["Jochen", "Daniel"].stream().map(e -> this.hello() + e).collect(Collectors.toList());
                }
                String hello() {
                    return "Hello ";
                }
            }
        '''
    }

    @Test
    void testFunctionWithInstanceMethodCall3() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    newInstance().p()
                }
                void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ["Jochen", "Daniel"].stream().map(e -> hello(e)).collect(Collectors.toList())
                }
                String hello(String name) {
                    return "Hello $name"
                }
            }
        '''
    }

    @Test
    void testFunctionCall() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    Function<Integer, Integer> f = (Integer e) -> (Integer) (e + 1)
                    assert 2 == f(1)
                }
            }
        '''
    }

    @Test
    void testFunctionCallWithoutExplicitTypeDef() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    Function<Integer, Integer> f = e -> e + 1
                    assert 2 == f(1)
                }
            }
        '''
    }

    @Test
    void testFunctionCall2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    Function<Integer, Integer> f = (Integer e) -> (Integer) (e + 1)
                    assert 2 == f(1)
                }
            }
        '''
    }

    @Test
    void testFunctionCall3() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    Function<Integer, Integer> f = (Integer e) -> (Integer) (e + 1)
                    assert 2 == f.apply(1)
                }
            }
        '''
    }

    @Test
    void testConsumer1() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                int a = 1
                java.util.function.Consumer<Integer> c = i -> { a += i }
                c.accept(2)
                assert a == 3
            }
            p()
        '''
    }

    @Test
    void testConsumer2() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                int a = 1
                java.util.function.Consumer<Integer> c = (i) -> { a += i }
                c.accept(2)
                assert a == 3
            }
            p()
        '''
    }

    @Test
    void testConsumer3() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                int a = 1
                java.util.function.Consumer<Integer> c = (Integer i) -> { a += i }
                c.accept(2)
                assert a == 3
            }
            p()
        '''
    }

    @Test
    void testConsumer4() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Tester {
                static main(args) {
                    int a = 1
                    java.util.function.Consumer<Integer> c = e -> { a += e }
                    c.accept(2)
                    assert a == 3
                }
            }
        '''
    }

    @Test
    void testConsumer5() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Tester {
                static main(args) {
                    int a = 1
                    java.util.function.Consumer<Integer> c = (Integer e) -> { a += e }
                    c.accept(2)
                    assert a == 3
                }
            }
        '''
    }

    @Test
    void testConsumer6() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Tester {
                static main(args) {
                    int a = 1
                    java.util.function.Consumer<Integer> c = (Integer e) -> { a += e }
                    c(2)
                    assert a == 3
                }
            }
        '''
    }

    @Test // GROOVY-9347
    void testConsumer7() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                int sum = 0
                java.util.function.Consumer<? super Integer> add = i -> sum += i

                [1, 2, 3].forEach(add)
                assert sum == 6
            }
            p()
        '''
    }

    @Test // GROOVY-9340
    void testConsumer8() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Tester {
                static main(args) {
                    java.util.function.Consumer<Tester> c = t -> null
                    c.accept(newInstance())
                }
            }
        '''
    }

    @Test
    void testConsumer9() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Tester {
                static main(args) {
                    [1, 2, 3].stream().forEach(e -> { System.out.println(e + 1) })
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
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    SamCallable c = (int e) -> e
                    assert 1 == c(1)
                }
            }

            interface SamCallable {
                int call(int p)
            }
        '''
    }

    @Test
    void testFunctionalInterface2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    SamCallable c = e -> e
                    assert 1 == c(1)
                }
            }

            interface SamCallable {
                int call(int p)
            }
        '''
    }

    @Test
    void testFunctionalInterface3() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            class Tester {
                static main(args) {
                    SamCallable c = (int e) -> e // This is actually a closure(not a native lambda), because "Functional interface SamCallable is not an interface"
                    assert 1 == c(1)
                }
            }

            abstract class SamCallable {
                abstract int call(int p)
            }
        '''
    }

    @NotYetImplemented
    @Test // GROOVY-9881
    void testFunctionalInterface4() {
        assertScript '''
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

            @groovy.transform.CompileStatic test() {
                assert new Value<>(123).replace(() -> 'foo').toString() == 'foo'
                assert new Value<>(123).replace((Integer v) -> 'bar').toString() == 'bar'
            }
            test()
        '''
    }

    @NotYetImplemented
    @Test // GROOVY-10372
    void testFunctionalInterface5() {
        def err = shouldFail '''
            interface I {
                def m(List<String> strings)
            }

            @groovy.transform.CompileStatic test() {
                I face = (List<Object> list) -> null
            }
        '''
        assert err =~ /Expected type java.util.List<java.lang.String> for lambda parameter: list/
    }

    @Test // GROOVY-11013
    void testFunctionalInterface6() {
        assertScript '''
            interface I<T> {
                def m(List<T> list_of_t)
            }

            @groovy.transform.CompileStatic test() {
                I<String> face = (List<String> list) -> null
            }
            test()
        '''
    }

    @Test // GROOVY-11072
    void testFunctionalInterface7() {
        assertScript '''
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

            @groovy.transform.CompileStatic test() {
                TestTable table = new TestTable()
                table.getAll((List<TestModel> list) ->
                    list.each { TestModel tm -> println(tm.id) }
                )
            }
            test()
        '''
    }

    @Test
    void testFunctionWithUpdatingLocalVariable() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
                    int i = 1
                    assert [2, 4, 7] == [1, 2, 3].stream().map(e -> i += e).collect(Collectors.toList())
                    assert 7 == i
                }
            }
        '''
    }

    @Test
    void testFunctionWithUpdatingLocalVariable2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    new Test1().p();
                }

                public void p() {
                    int i = 1
                    assert [2, 4, 7] == [1, 2, 3].stream().map(e -> i += e).collect(Collectors.toList())
                    assert 7 == i
                }
            }
        '''
    }

    @Test
    void testFunctionWithVariableDeclaration() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p()
                }

                public static void p() {
                    Function<Integer, String> f = (Integer e) -> 'a' + e
                    assert ['a1', 'a2', 'a3'] == [1, 2, 3].stream().map(f).collect(Collectors.toList())
                }
            }
        '''
    }

    @Test
    void testFunctionWithMixingVariableDeclarationAndMethodInvocation() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p()
                }

                public static void p() {
                    String x = "#"
                    Integer y = 23
                    assert ['23#1', '23#2', '23#3'] == [1, 2, 3].stream().map(e -> '' + y + x + e).collect(Collectors.toList())

                    Function<Integer, String> f = (Integer e) -> 'a' + e
                    assert ['a1', 'a2', 'a3'] == [1, 2, 3].stream().map(f).collect(Collectors.toList())

                    assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e.plus(1)).collect(Collectors.toList());
                }
            }
        '''
    }

    @Test
    void testFunctionWithNestedLambda() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p()
                }

                public static void p() {
                    [1, 2].stream().forEach(e -> {
                        def list = ['a', 'b'].stream().map(f -> f + e).toList()
                        if (1 == e) {
                            assert ['a1', 'b1'] == list
                        } else if (2 == e) {
                            assert ['a2', 'b2'] == list
                        }
                    })
                }
            }
        '''
    }

    @Test
    void testFunctionWithNestedLambda2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p()
                }

                public static void p() {
                    def list = ['a', 'b'].stream()
                    .map(e -> {
                        [1, 2].stream().map(f -> e + f).toList()
                    }).toList()

                    assert ['a1', 'a2'] == list[0]
                    assert ['b1', 'b2'] == list[1]
                }
            }
        '''
    }

    @Test
    void testFunctionWithNestedLambda3() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p()
                }

                public static void p() {
                    def list = ['a', 'b'].stream()
                    .map(e -> {
                        Function<Integer, String> x = (Integer f) -> e + f
                        [1, 2].stream().map(x).toList()
                    }).toList()

                    assert ['a1', 'a2'] == list[0]
                    assert ['b1', 'b2'] == list[1]
                }
            }
        '''
    }

    @Test
    void testMixingLambdaAndMethodReference() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                assert ['1', '2', '3'] == [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList())
                assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e.plus(1)).collect(Collectors.toList())
                assert ['1', '2', '3'] == [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList())
            }

            p()
        '''
    }

    @Test
    void testInitializeBlocks() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
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

    @Test
    void testNestedLambdaAccessingInstanceFields() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Test1 {
                private List<String> strList = ['a', 'e', 'f']
                private Map<String, List<String>> strListHolder = ['strList': strList]
                private String b = 'b'
                def p() {
                    ['abc', 'def', 'ghi'].stream().filter(e -> strList.stream().anyMatch(c -> e.contains(c + b))).toList()
                }
                def p2() {
                    ['abc', 'def', 'ghi'].stream().filter(e -> strListHolder.strList.stream().anyMatch(c -> e.contains(c + b))).toList()
                }
            }

            assert ['abc'] == new Test1().p()
            assert ['abc'] == new Test1().p2()
        '''
    }

    @Test // GROOVY-9332
    void testStaticInitializeBlocks1() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Test1 {
                static list
                static final int one = 1
                static { list = [1, 2, 3].stream().map(e -> e + one).toList() }
            }

            assert [2, 3, 4] == Test1.list
        '''
    }

    @Test // GROOVY-9347
    void testStaticInitializeBlocks2() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Test1 {
                static int acc = 1
                static { [1, 2, 3].forEach(e -> acc += e) }
            }
            assert Test1.acc == 7
        '''
    }

    @Test // GROOVY-9342
    void testStaticInitializeBlocks3() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Test1 {
                static int acc = 1
                static { [1, 2, 3].forEach((Integer i) -> acc += i) }
            }
            assert Test1.acc == 7
        '''
    }

    @Test
    void testAccessingThis1() {
        assertScript '''
            @groovy.transform.CompileStatic
            class ThisTest {
                private final ThisTest that = this

                void m() {
                    java.util.function.Predicate<ThisTest> p = (ThisTest t) -> {
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
        assertScript '''
            @groovy.transform.CompileStatic
            class ThisTest {
                private final ThisTest that = this

                void m() {
                    java.util.function.Predicate<ThisTest> p1 = (ThisTest t1) -> {
                        java.util.function.Predicate<ThisTest> p2 = (ThisTest t2) -> {
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
        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            @groovy.transform.CompileStatic
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
        assertScript '''
            interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
            }

            @groovy.transform.CompileStatic
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
        def err = shouldFail NotSerializableException, '''
            import java.util.function.Function

            @groovy.transform.CompileStatic
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

        assert err.message.contains('$Lambda$')
    }

    @Test
    void testDeserialize1() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize2() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize3() {
        def err = shouldFail NotSerializableException, '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
        assert err.message.contains('tests.lambda.C')
    }

    @Test
    void testDeserialize4() {
        assertScript '''
            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize5() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize6() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize7() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize8() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize9() {
        def err = shouldFail NotSerializableException, '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''

        assert err.message.contains('tests.lambda.C')
    }

    @Test
    void testDeserialize10() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize11() {
        def err = shouldFail NotSerializableException, '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''

        assert err.message.contains('tests.lambda.C')
    }

    @Test
    void testDeserialize12() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
                }
            }
        '''
    }

    @Test
    void testDeserialize13() {
        assertScript '''
            package tests.lambda

            @groovy.transform.CompileStatic
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

                interface SerializableFunction<I,O> extends Serializable, java.util.function.Function<I,O> {
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

    @Test // GROOVY-9146
    void testScriptWithExistingMainCS() {
        assertScript '''
            @groovy.transform.CompileStatic
            static void main(args) {
                java.util.function.Function<String, String> lower = String::toLowerCase
                assert lower.toString().contains('$$Lambda$')
            }
        '''
    }

    @Test // GROOVY-9770
    void testLambdaClassIsntSynthetic() {
        assertScript imports + '''
            class Foo {
                @CompileStatic bar(String arg) {
                    Function<String, String> fun = (String s) -> s.toUpperCase()
                    fun.apply(arg)
                }
            }

            assert new Foo().bar('hello') == 'HELLO'
            assert this.class.classLoader.loadClass('Foo$_bar_lambda1').modifiers == 17 // public(1) + final(16)
        '''
    }
}
