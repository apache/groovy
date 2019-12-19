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
import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class LambdaTest {

    @Test
    void testFunction() {
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
                    assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e.plus 1).collect(Collectors.toList());
                }
            }
        '''
    }

    @Test
    void testFunction2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            public class Test1 {
                public static void main(String[] args) {
                    p();
                }

                @CompileStatic
                public static void p() {
                    assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e.plus 1).collect(Collectors.toList());
                }
            }
        '''
    }

    @Test
    void testFunctionScript() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            void p() {
                assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e + 1).collect(Collectors.toList());
            }

            p()
        '''
    }

    @Test
    void testFunctionScript2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            void p() {
                assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e.plus 1).collect(Collectors.toList());
            }

            p()
        '''
    }

    @Test
    void testBinaryOperator() {
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
                    assert 13 == [1, 2, 3].stream().reduce(7, (Integer r, Integer e) -> r + e);
                }
            }
        '''
    }

    @Test // GROOVY-8917: Failed to infer parameter type of some SAM, e.g. BinaryOperator
    void testBinaryOperatorWithoutExplicitTypeDef() {
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
                    assert 13 == [1, 2, 3].stream().reduce(7, (r, e) -> r + e);
                }
            }
        '''
    }

    @Test
    void testBinaryOperatorWithoutExplicitTypeDef2() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.BinaryOperator

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
                    BinaryOperator<Integer> accumulator = (r, e) -> r + e
                    assert 13 == [1, 2, 3].stream().reduce(7, accumulator);
                }
            }
        '''
    }

    @Test
    void testConsumer() {
        assertScript '''
            @groovy.transform.CompileStatic
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

    @Test @NotYetImplemented // GROOVY-9340
    void testConsumerWithSelfType() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    java.util.function.Consumer<Test1> c = t -> null
                    c.accept(this.newInstance())
                }
            }
        '''
    }

    @Test
    void testPredicate() {
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
                    def list = ['ab', 'bc', 'de']
                    list.removeIf(e -> e.startsWith("a"))
                    assert ['bc', 'de'] == list
                }
            }
        '''
    }

    @Test
    void testPredicateWithoutExplicitTypeDef() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function
            import java.util.function.Predicate

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p()
                }

                public static void p() {
                    List<String> myList = Arrays.asList("a1", "a2", "b2", "b1", "c2", "c1")
                    Predicate<String> predicate = s -> s.startsWith("b")
                    Function<String, String> mapper = s -> s.toUpperCase()

                    List<String> result =
                            myList
                                .stream()
                                .filter(predicate)
                                .map(mapper)
                                .sorted()
                                .collect(Collectors.toList())

                    assert ['B1', 'B2'] == result
                }
            }
        '''
    }

    @Test
    void testUnaryOperator() {
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
                    def list = [1, 2, 3]
                    list.replaceAll(e -> e + 10)
                    assert [11, 12, 13] == list
                }
            }
        '''
    }

    @Test
    void testBiConsumer() {
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
                    def map = [a: 1, b: 2, c: 3]
                    map.forEach((k, v) -> System.out.println(k + ":" + v));
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables() {
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
            public class Test1 {
                public static void main(String[] args) {
                    new Test1().p();
                }

                public void p() {
                    String x = "#"
                    Integer y = 23
                    assert ['23#1', '23#2', '23#3'] == [1, 2, 3].stream().map(e -> '' + y + x + e).collect(Collectors.toList())
                }
            }
        '''
    }

    void testFunctionWithLocalVariables4() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream

            @CompileStatic
            public class Test4 {
                public static void main(String[] args) {
                    new Test4().p();
                }

                public void p() {
                    String x = "x";
                    StringBuilder y = new StringBuilder("y");
                    assert ['yx1', 'yx2', 'yx3'] == [1, 2, 3].stream().map(e -> y + x + e).collect(Collectors.toList());
                }
            }
        '''
    }

    @Test
    void testFunctionWithLocalVariables5() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
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
    void testFunctionWithLocalVariables6() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    Function<Integer, String> f = new Test1().p();
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
            public class Test4 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
                    String x = "x";
                    StringBuilder y = new StringBuilder("y");
                    assert ['Hello yx1', 'Hello yx2', 'Hello yx3'] == [1, 2, 3].stream().map(e -> hello() + y + x + e).collect(Collectors.toList());
                }

                public static String hello() {
                    return "Hello ";
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
            public class Test4 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
                    String x = "x";
                    StringBuilder y = new StringBuilder("y");
                    assert ['Hello yx1', 'Hello yx2', 'Hello yx3'] == [1, 2, 3].stream().map(e -> Test4.hello() + y + x + e).collect(Collectors.toList());
                }

                public static String hello() {
                    return "Hello ";
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
            public class Test4 {
                public static void main(String[] args) {
                    new Test4().p();
                }

                public void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ["Jochen", "Daniel"].stream().map(e -> hello() + e).collect(Collectors.toList());
                }

                public String hello() {
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
            public class Test4 {
                public static void main(String[] args) {
                    new Test4();
                }

                public Test4() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ["Jochen", "Daniel"].stream().map(e -> hello() + e).collect(Collectors.toList());
                }

                public String hello() {
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
            public class Test4 {
                public static void main(String[] args) {
                    new Test4().p();
                }

                public void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ["Jochen", "Daniel"].stream().map(e -> this.hello() + e).collect(Collectors.toList());
                }

                public String hello() {
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
            public class Test4 {
                public static void main(String[] args) {
                    new Test4().p();
                }

                public void p() {
                    assert ['Hello Jochen', 'Hello Daniel'] == ["Jochen", "Daniel"].stream().map(e -> hello(e)).collect(Collectors.toList());
                }

                public String hello(String name) {
                    return "Hello $name";
                }
            }
        '''
    }

    @Test
    void testFunctionCall() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
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
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
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
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    new Test1().p();
                }

                public void p() {
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
            import java.util.stream.Collectors
            import java.util.stream.Stream
            import java.util.function.Function

            @CompileStatic
            public class Test1 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
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
            void m() {
                int a = 1
                java.util.function.Consumer<Integer> c = i -> { a += i }
                c.accept(2)
                assert a == 3
            }
            m()
        '''
    }

    @Test
    void testConsumer2() {
        assertScript '''
            @groovy.transform.CompileStatic
            void m() {
                int a = 1
                java.util.function.Consumer<Integer> c = (i) -> { a += i }
                c.accept(2)
                assert a == 3
            }
            m()
        '''
    }

    @Test
    void testConsumer3() {
        assertScript '''
            @groovy.transform.CompileStatic
            void m() {
                int a = 1
                java.util.function.Consumer<Integer> c = (Integer i) -> { a += i }
                c.accept(2)
                assert a == 3
            }
            m()
        '''
    }

    @Test
    void testConsumer4() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
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
            class Test1 {
                static main(args) {
                    new Test1().p()
                }

                void p() {
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
            class Test1 {
                static main(args) {
                    p()
                }

                static void p() {
                    int a = 1
                    java.util.function.Consumer<Integer> c = (Integer e) -> { a += e }
                    c(2)
                    assert a == 3
                }
            }
        '''
    }

    @Test
    void testFunctionalInterface1() {
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
                    SamCallable c = (int e) -> e
                    assert 1 == c(1)
                }
            }

            @CompileStatic
            interface SamCallable {
                int call(int p);
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
            public class Test1 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
                    SamCallable c = e -> e
                    assert 1 == c(1)
                }
            }

            @CompileStatic
            interface SamCallable {
                int call(int p);
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
            public class Test1 {
                public static void main(String[] args) {
                    p();
                }

                public static void p() {
                    SamCallable c = (int e) -> e // This is actually a closure(not a native lambda), because "Functional interface SamCallable is not an interface"
                    assert 1 == c(1)
                }
            }

            @CompileStatic
            abstract class SamCallable {
                abstract int call(int p);
            }
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

                    assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e.plus 1).collect(Collectors.toList());
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
                assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e.plus 1).collect(Collectors.toList())
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
    void testStaticInitializeBlocks() {
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

    @Test @NotYetImplemented // GROOVY-9342
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
            import java.util.function.Function

            interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}

            @groovy.transform.CompileStatic
            class Test1 {
                def p() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        SerializableFunction<Integer, String> f = ((Integer e) -> 'a' + e)
                        it.writeObject(f)
                    }
                    return out.toByteArray()
                }
            }

            assert new Test1().p().length > 0
        '''
    }

    @Test
    void testSerialize2() {
        def err = shouldFail NotSerializableException, '''
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                def p() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        Function<Integer, String> f = ((Integer e) -> 'a' + e)
                        it.writeObject(f)
                    }
                    return out.toByteArray()
                }
            }

            new Test1().p()
        '''

        assert err.message.contains('$Lambda$')
    }

    @Test
    void testDeserialize1() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                byte[] p() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        SerializableFunction<Integer, String> f = ((Integer e) -> 'a' + e)
                        it.writeObject(f)
                    }
                    return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(new Test1().p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize1_InitializeBlock() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 implements Serializable {
                private static final long serialVersionUID = -1L;
                String a = 'a'
                SerializableFunction<Integer, String> f

                {
                    f = ((Integer e) -> a + e)
                }

                byte[] p() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }

                    return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(new Test1().p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize1_InitializeBlockShouldFail() {
        def err = shouldFail NotSerializableException, '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                String a = 'a'
                SerializableFunction<Integer, String> f

                {
                    f = ((Integer e) -> a + e)
                }

                byte[] p() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        it.writeObject(f)
                    }

                    return out.toByteArray()
                }

                static void main(String[] args) {
                    new Test1().p()
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''

        assert err.message.contains('tests.lambda.Test1')
    }

    @Test
    void testDeserialize2() {
        assertScript '''
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                static byte[] p() {
                        def out = new ByteArrayOutputStream()
                        out.withObjectOutputStream {
                            SerializableFunction<Integer, String> f = ((Integer e) -> 'a' + e)
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(Test1.p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize3() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                byte[] p() {
                        def out = new ByteArrayOutputStream()
                        out.withObjectOutputStream {
                            String c = 'a'
                            SerializableFunction<Integer, String> f = (Integer e) -> c + e
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(new Test1().p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize4() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                byte[] p() {
                        def out = new ByteArrayOutputStream()
                        String c = 'a'
                        SerializableFunction<Integer, String> f = (Integer e) -> c + e
                        out.withObjectOutputStream {
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(new Test1().p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize5() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                static byte[] p() {
                        def out = new ByteArrayOutputStream()
                        String c = 'a'
                        SerializableFunction<Integer, String> f = (Integer e) -> c + e
                        out.withObjectOutputStream {
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(Test1.p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize6_InstanceFields() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 implements Serializable {
                private static final long serialVersionUID = -1L;
                private String c = 'a'

                byte[] p() {
                        def out = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f = (Integer e) -> c + e
                        out.withObjectOutputStream {
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(new Test1().p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize6_InstanceFieldsShouldFail() {
        def err = shouldFail NotSerializableException, '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                private String c = 'a'

                byte[] p() {
                        def out = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f = (Integer e) -> c + e
                        out.withObjectOutputStream {
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(new Test1().p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''

        assert err.message.contains('tests.lambda.Test1')
    }

    @Test
    void testDeserialize6_InstanceMethods() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 implements Serializable {
                private static final long serialVersionUID = -1L;
                private String c() { 'a' }

                byte[] p() {
                        def out = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f = (Integer e) -> c() + e
                        out.withObjectOutputStream {
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(new Test1().p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize6_InstanceMethodsShouldFail() {
        def err = shouldFail NotSerializableException, '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                private String c() { 'a' }

                byte[] p() {
                        def out = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f = (Integer e) -> c() + e
                        out.withObjectOutputStream {
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(new Test1().p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''

        assert err.message.contains('tests.lambda.Test1')
    }

    @Test
    void testDeserialize7_StaticFields() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                private static final String c = 'a'
                static byte[] p() {
                        def out = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f = (Integer e) -> c + e
                        out.withObjectOutputStream {
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(Test1.p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserialize7_StaticMethods() {
        assertScript '''
            package tests.lambda
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Test1 {
                private static String c() { 'a' }
                static byte[] p() {
                        def out = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f = (Integer e) -> c() + e
                        out.withObjectOutputStream {
                            it.writeObject(f)
                        }

                        return out.toByteArray()
                }

                static void main(String[] args) {
                    new ByteArrayInputStream(Test1.p()).withObjectInputStream(Test1.class.classLoader) {
                        SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                        assert 'a1' == f.apply(1)
                    }
                }

                interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}
            }
        '''
    }

    @Test
    void testDeserializeNestedLambda1() {
        assertScript '''
            import java.util.function.Function

            interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}

            @groovy.transform.CompileStatic
            class Test1 {
                def p() {
                        def out1 = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f1 = (Integer e) -> 'a' + e
                        out1.withObjectOutputStream {
                            it.writeObject(f1)
                        }
                        def result1 = out1.toByteArray()

                        def out2 = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f2 = (Integer e) -> 'b' + e
                        out2.withObjectOutputStream {
                            it.writeObject(f2)
                        }
                        def result2 = out2.toByteArray()

                        // nested lambda expression
                        def out3 = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f3 = (Integer e) -> {
                            SerializableFunction<Integer, String> nf = ((Integer ne) -> 'n' + ne)
                            'c' + nf(e)
                        }
                        out3.withObjectOutputStream {
                            it.writeObject(f3)
                        }
                        def result3 = out3.toByteArray()

                        return [result1, result2, result3]
                }
            }

            def (byte[] serializedLambdaBytes1, byte[] serializedLambdaBytes2, byte[] serializedLambdaBytes3) = new Test1().p()
            new ByteArrayInputStream(serializedLambdaBytes1).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'a1' == f.apply(1)
            }

            new ByteArrayInputStream(serializedLambdaBytes2).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'b1' == f.apply(1)
            }

            new ByteArrayInputStream(serializedLambdaBytes3).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'cn1' == f.apply(1)
            }
        '''
    }

    @Test
    void testDeserializeNestedLambda2() {
        assertScript '''
            import java.util.function.Function

            interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}

            @groovy.transform.CompileStatic
            class Test1 {
                def p() {
                        def out1 = new ByteArrayOutputStream()
                        out1.withObjectOutputStream {
                            SerializableFunction<Integer, String> f = ((Integer e) -> 'a' + e)
                            it.writeObject(f)
                        }
                        def result1 = out1.toByteArray()

                        def out2 = new ByteArrayOutputStream()
                        out2.withObjectOutputStream {
                            SerializableFunction<Integer, String> f = ((Integer e) -> 'b' + e)
                            it.writeObject(f)
                        }
                        def result2 = out2.toByteArray()

                        // nested lambda expression
                        def out3 = new ByteArrayOutputStream()
                        out3.withObjectOutputStream {
                            SerializableFunction<Integer, String> f = ((Integer e) -> {
                                SerializableFunction<Integer, String> nf = ((Integer ne) -> 'n' + ne)
                                'c' + nf(e)
                            })
                            it.writeObject(f)
                        }
                        def result3 = out3.toByteArray()

                        return [result1, result2, result3]
                }
            }

            def (byte[] serializedLambdaBytes1, byte[] serializedLambdaBytes2, byte[] serializedLambdaBytes3) = new Test1().p()
            new ByteArrayInputStream(serializedLambdaBytes1).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'a1' == f.apply(1)
            }

            new ByteArrayInputStream(serializedLambdaBytes2).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'b1' == f.apply(1)
            }

            new ByteArrayInputStream(serializedLambdaBytes3).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'cn1' == f.apply(1)
            }
        '''
    }

    @Test
    void testDeserializeNestedLambda3() {
        assertScript '''
            import java.util.function.Function

            interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}

            @groovy.transform.CompileStatic
            class Test1 {
                static p() {
                        def out1 = new ByteArrayOutputStream()
                        out1.withObjectOutputStream {
                            SerializableFunction<Integer, String> f = ((Integer e) -> 'a' + e)
                            it.writeObject(f)
                        }
                        def result1 = out1.toByteArray()

                        def out2 = new ByteArrayOutputStream()
                        out2.withObjectOutputStream {
                            SerializableFunction<Integer, String> f = ((Integer e) -> 'b' + e)
                            it.writeObject(f)
                        }
                        def result2 = out2.toByteArray()

                        // nested lambda expression
                        def out3 = new ByteArrayOutputStream()
                        out3.withObjectOutputStream {
                            SerializableFunction<Integer, String> f = ((Integer e) -> {
                                SerializableFunction<Integer, String> nf = ((Integer ne) -> 'n' + ne)
                                'c' + nf(e)
                            })
                            it.writeObject(f)
                        }
                        def result3 = out3.toByteArray()

                        return [result1, result2, result3]
                }
            }

            def (byte[] serializedLambdaBytes1, byte[] serializedLambdaBytes2, byte[] serializedLambdaBytes3) = Test1.p()
            new ByteArrayInputStream(serializedLambdaBytes1).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'a1' == f.apply(1)
            }

            new ByteArrayInputStream(serializedLambdaBytes2).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'b1' == f.apply(1)
            }

            new ByteArrayInputStream(serializedLambdaBytes3).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'cn1' == f.apply(1)
            }
        '''
    }

    @Test
    void testDeserializeNestedLambda4() {
        assertScript '''
            import java.util.function.Function

            interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}

            @groovy.transform.CompileStatic
            class Test1 {
                static p() {
                        def out1 = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f1 = (Integer e) -> 'a' + e
                        out1.withObjectOutputStream {
                            it.writeObject(f1)
                        }
                        def result1 = out1.toByteArray()

                        def out2 = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f2 = (Integer e) -> 'b' + e
                        out2.withObjectOutputStream {
                            it.writeObject(f2)
                        }
                        def result2 = out2.toByteArray()

                        // nested lambda expression
                        def out3 = new ByteArrayOutputStream()
                        SerializableFunction<Integer, String> f3 = (Integer e) -> {
                            SerializableFunction<Integer, String> nf = ((Integer ne) -> 'n' + ne)
                            'c' + nf(e)
                        }
                        out3.withObjectOutputStream {
                            it.writeObject(f3)
                        }
                        def result3 = out3.toByteArray()

                        return [result1, result2, result3]
                }
            }

            def (byte[] serializedLambdaBytes1, byte[] serializedLambdaBytes2, byte[] serializedLambdaBytes3) = Test1.p()
            new ByteArrayInputStream(serializedLambdaBytes1).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'a1' == f.apply(1)
            }

            new ByteArrayInputStream(serializedLambdaBytes2).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'b1' == f.apply(1)
            }

            new ByteArrayInputStream(serializedLambdaBytes3).withObjectInputStream(Test1.class.classLoader) {
                SerializableFunction<Integer, String> f = (SerializableFunction<Integer, String>) it.readObject()
                assert 'cn1' == f.apply(1)
            }
        '''
    }
}
