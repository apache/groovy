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

class LambdaTest extends GroovyTestCase {
    private static final boolean SKIP_ERRORS = true;

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

    /**
     * Depends on fixing https://issues.apache.org/jira/browse/GROOVY-8445
     */
    void testBinaryOperator() {
        if (SKIP_ERRORS) return

        // the test can pass only in dynamic mode now, it can not pass static type checking...

        /* FIXME
TestScript0.groovy: 13: [Static type checking] - Cannot find matching method java.util.stream.Stream#reduce(int, groovy.lang.Closure). Please check if the declared type is correct and if the method exists.
 @ line 13, column 30.
                   assert 13 == [1, 2, 3].stream().reduce(7, (r, e) -> r + e);
                                ^

TestScript0.groovy: 13: [Static type checking] - Cannot find matching method java.lang.Object#plus(java.lang.Object). Please check if the declared type is correct and if the method exists.
 @ line 13, column 69.
   (1, 2, 3).reduce(7, (r, e) -> r + e);
                                 ^

2 errors
         */

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

    void testConsumer() {
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
                [1, 2, 3].stream().forEach(e -> { System.out.println(e + 1); });
            }
            
        }
        '''
    }

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

    /**
     * Depends on fixing https://issues.apache.org/jira/browse/GROOVY-8445
     */
    void testUnaryOperator() {
        if (SKIP_ERRORS) return

        /* FIXME
TestScript0.groovy: 14: [Static type checking] - Cannot find matching method java.util.List#replaceAll(groovy.lang.Closure). Please check if the declared type is correct and if the method exists.
 @ line 14, column 17.
                   list.replaceAll(e -> e + 10)
                   ^

TestScript0.groovy: 14: [Static type checking] - Cannot find matching method java.lang.Object#plus(int). Please check if the declared type is correct and if the method exists.
 @ line 14, column 38.
                   list.replaceAll(e -> e + 10)
                                        ^

2 errors
         */

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
                Function<Integer, Integer> f = (Integer e) -> (Integer) (e + 1) // Casting is required...  [Static type checking] - Incompatible generic argument types. Cannot assign java.util.function.Function <java.lang.Integer, int> to: java.util.function.Function <Integer, Integer>
                assert 2 == f(1)
            }
        }
        '''
    }

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
                Function<Integer, Integer> f = (Integer e) -> (Integer) (e + 1) // Casting is required...  [Static type checking] - Incompatible generic argument types. Cannot assign java.util.function.Function <java.lang.Integer, int> to: java.util.function.Function <Integer, Integer>
                assert 2 == f(1)
            }
        }
        '''
    }

    void testConsumerCall() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        import java.util.function.Consumer
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p();
            }
        
            public static void p() {
                int r = 1
                Consumer<Integer> c = (Integer e) -> { r += e }
                c(2)
                assert 3 == r
            }
        }
        '''
    }

    void testConsumerCall2() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        import java.util.function.Consumer
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                new Test1().p();
            }
        
            public void p() {
                int r = 1
                Consumer<Integer> c = (Integer e) -> { r += e }
                c(2)
                assert 3 == r
            }
        }
        '''
    }

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
                Function<Integer, String> f = (Integer e) -> 'a' + e // STC can not infer the type of `e`, so we have to specify the type `Integer` by ourselves
                assert ['a1', 'a2', 'a3'] == [1, 2, 3].stream().map(f).collect(Collectors.toList())
            }
        }
        
        '''
    }

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
}
