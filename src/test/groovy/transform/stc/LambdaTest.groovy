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
                assert [2, 3, 4] == Stream.of(1, 2, 3).map(e -> e.plus 1).collect(Collectors.toList());
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
            assert [2, 3, 4] == Stream.of(1, 2, 3).map(e -> e + 1).collect(Collectors.toList());
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
                   assert 13 == Stream.of(1, 2, 3).reduce(7, (r, e) -> r + e);
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
                assert 13 == Stream.of(1, 2, 3).reduce(7, (r, e) -> r + e);
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
                Stream.of(1, 2, 3).forEach(e -> { System.out.println(e + 1); });
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
                assert ['#1', '#2', '#3'] == Stream.of(1, 2, 3).map(e -> x + e).collect(Collectors.toList());
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
                assert ['23#1', '23#2', '23#3'] == Stream.of(1, 2, 3).map(e -> '' + y + x + e).collect(Collectors.toList())
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
                assert ['yx1', 'yx2', 'yx3'] == Stream.of(1, 2, 3).map(e -> y + x + e).collect(Collectors.toList());
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
                assert ['Hello yx1', 'Hello yx2', 'Hello yx3'] == Stream.of(1, 2, 3).map(e -> hello() + y + x + e).collect(Collectors.toList());
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
                assert ['Hello yx1', 'Hello yx2', 'Hello yx3'] == Stream.of(1, 2, 3).map(e -> Test4.hello() + y + x + e).collect(Collectors.toList());
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
                assert ['Hello Jochen', 'Hello Daniel'] == Stream.of("Jochen", "Daniel").map(e -> hello() + e).collect(Collectors.toList());
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
                assert ['Hello Jochen', 'Hello Daniel'] == Stream.of("Jochen", "Daniel").map(e -> hello() + e).collect(Collectors.toList());
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
                assert ['Hello Jochen', 'Hello Daniel'] == Stream.of("Jochen", "Daniel").map(e -> this.hello() + e).collect(Collectors.toList());
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
                assert ['Hello Jochen', 'Hello Daniel'] == Stream.of("Jochen", "Daniel").map(e -> hello(e)).collect(Collectors.toList());
            }
        
            public String hello(String name) {
                return "Hello $name";
            }
        }
        '''
    }

    void testFunctionCall() {
        if (SKIP_ERRORS) return

        /* FIXME
        [Static type checking] - Cannot find matching method java.lang.Object#plus(int). Please check if the declared type is correct and if the method exists.
 @ line 13, column 35.
                   assert 2 == (e -> e + 1)(1)
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
                assert 2 == (e -> e + 1)(1)
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
                assert [2, 4, 7] == Stream.of(1, 2, 3).map(e -> i += e).collect(Collectors.toList())
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
                assert [2, 4, 7] == Stream.of(1, 2, 3).map(e -> i += e).collect(Collectors.toList())
                assert 7 == i
            }
        }
        '''
    }
}
