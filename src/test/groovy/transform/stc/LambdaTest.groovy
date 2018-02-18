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

import org.codehaus.groovy.vmplugin.VMPluginFactory

class LambdaTest extends GroovyTestCase {
    private static final boolean SKIP_ERRORS = true;
    private static final boolean PRE_JAVA8 = VMPluginFactory.getPlugin().getVersion() < 8;

    void testFunction() {
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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

    void testBinaryOperator() {
        if (PRE_JAVA8) return;

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

    void testConsumer() {
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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

    void testUnaryOperator() {
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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

    void testFunctionCall3() {
        if (PRE_JAVA8) return;

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
                assert 2 == f.apply(1)
            }
        }
        '''
    }

    void testConsumerCall() {
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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

    void testConsumerCall3() {
        if (PRE_JAVA8) return;

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
                c.accept(2)
                assert 3 == r
            }
        }
        '''
    }

    void testSamCall() {
        if (PRE_JAVA8) return;

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

    void testSamCall2() {
        if (PRE_JAVA8) return;

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

    void testFunctionWithUpdatingLocalVariable() {
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
        if (PRE_JAVA8) return;

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
