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
package bugs

import org.apache.groovy.util.JavaShell
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy4721 {
    @Test
    void testAccessingVariableInFinallyBlock_1() {
        def err = shouldFail '''\
            class MyClass {
                def myMethod() {
                    try {
                        def x = 'foo'
                    }
                    finally {
                        println 'x: ' + x
                    }
                }
            }
            println new MyClass().myMethod()
        '''
        assert err =~ /No such property: x for class: MyClass/
    }

    @Test
    void testAccessingVariableInFinallyBlock_2() {
        def err = shouldFail '''\
            class MyClass {
                def myMethod() {
                    try {
                        def x = 'foo'
                        return x
                    }
                    finally {
                        println 'x: ' + x
                    }
                }
            }
            println new MyClass().myMethod()
        '''
        assert err =~ /No such property: x for class: MyClass/
    }

    @Test
    void testAccessingVariableInFinallyBlock_3() {
        def err = shouldFail '''\
            class MyClass {
                def myMethod() {
                    try {
                        def x = 'foo'
                        return
                    }
                    finally {
                        println 'x: ' + x
                    }
                }
            }
            println new MyClass().myMethod()
        '''
        assert err =~ /No such property: x for class: MyClass/
    }

    @Test
    void testAccessingVariableInFinallyBlock_4() {
        assertScript '''\
            class MyClass {
                def myMethod() {
                    try {
                        def x = 'foo'
                    }
                    finally {
                        assert true
                    }
                }
            }
            assert 'foo' == new MyClass().myMethod()
        '''
    }

    @Test
    void testAccessingVariableInFinallyBlock_5() {
        assertScript '''\
            class MyClass {
                def myMethod() {
                    try {
                        def x = 'foo'
                        return x
                    }
                    finally {
                        assert true
                    }
                }
            }
            assert 'foo' == new MyClass().myMethod()
        '''
    }

    @Test
    void testAccessingVariableInFinallyBlock_6() {
        def declareClass = { String lang ->
            """
            import java.util.List;
            import java.util.ArrayList;
            import java.util.function.Function;
            public class TryFinally${lang}Test {
                public String test() {
                    String result = "result: ";
                    try {
                        String x = "foo";
                        result += x;
                        return result;
                    } finally {
                        String y = "bar";
                        result += y;
                    }
                }
            }
            """
        }

        JavaShell js = new JavaShell()
        final mcn = "tests.TryFinallyJavaTest"
        js.compile(mcn, "package tests;\n${declareClass('Java')}")

        new GroovyShell(js.getClassLoader()).evaluate """\
            package tests;
            import tests.TryFinallyJavaTest

            ${declareClass('Groovy')}

            final groovyResult = new TryFinallyGroovyTest().test()
            assert 'result: foo' == groovyResult
            assert new TryFinallyJavaTest().test() == groovyResult
        """
    }

    @Test
    void testAccessingVariableInFinallyBlock_7() {
        def declareClass = { String lang ->
            """
            import java.util.List;
            import java.util.ArrayList;
            import java.util.function.Function;
            public class TryFinally${lang}Test {
                public String test() {
                    String result = "result: ";
                    try {
                        String x = "foo";
                        result += x;
                        return result;
                    } finally {
                        String y = "bar";
                        result += y;
                        return result;
                    }
                }
            }
            """
        }

        JavaShell js = new JavaShell()
        final mcn = "tests.TryFinallyJavaTest"
        js.compile(mcn, "package tests;\n${declareClass('Java')}")

        new GroovyShell(js.getClassLoader()).evaluate """\
            package tests;
            import tests.TryFinallyJavaTest

            ${declareClass('Groovy')}

            final groovyResult = new TryFinallyGroovyTest().test()
            assert 'result: foobar' == groovyResult
            assert new TryFinallyJavaTest().test() == groovyResult
        """
    }

    @Test
    void testAccessingVariableInFinallyBlock_8() {
        def declareClass = { String lang ->
            """
            import java.util.List;
            import java.util.ArrayList;
            import java.util.function.Function;
            public class TryFinally${lang}Test {
                public List<String> test() {
                    List<String> resultList = new ArrayList<>();
                    String result = "result: ";
                    try {
                        String x = "foo";
                        result += x;
                        resultList.add(result);
                        Function<String, List<String>> f = (String r) -> {
                            resultList.add(r);
                            return resultList;
                        };
                        return f.apply(result);
                    } finally {
                        String y = "bar";
                        result += y;
                        resultList.add(result);
                    }
                }
            }
            """
        }

        JavaShell js = new JavaShell()
        final mcn = "tests.TryFinallyJavaTest"
        js.compile(mcn, "package tests;\n${declareClass('Java')}")

        new GroovyShell(js.getClassLoader()).evaluate """\
            package tests;
            import tests.TryFinallyJavaTest

            ${declareClass('Groovy')}

            final groovyResult = new TryFinallyGroovyTest().test()
            assert ['result: foo', 'result: foo', 'result: foobar'] == groovyResult
            assert new TryFinallyJavaTest().test() == groovyResult
        """
    }

    @Test
    void testAccessingVariableInFinallyBlock_9() {
        def declareClass = { String lang ->
            """
            import java.util.List;
            import java.util.ArrayList;
            import java.util.function.Function;
            public class TryFinally${lang}Test {
                public List<String> test() {
                    List<String> resultList = new ArrayList<>();
                    String result = "result: ";
                    try {
                        String x = "foo";
                        result += x;
                        resultList.add(result);
                        Function<String, List<String>> f = (String r) -> {
                            resultList.add(r);
                            return resultList;
                        };
                        return f.apply(result);
                    } finally {
                        String y = "bar";
                        result += y;
                        resultList.add(result);
                        return resultList;
                    }
                }
            }
            """
        }

        JavaShell js = new JavaShell()
        final mcn = "tests.TryFinallyJavaTest"
        js.compile(mcn, "package tests;\n${declareClass('Java')}")

        new GroovyShell(js.getClassLoader()).evaluate """\
            package tests;
            import tests.TryFinallyJavaTest

            ${declareClass('Groovy')}

            final groovyResult = new TryFinallyGroovyTest().test()
            assert ['result: foo', 'result: foo', 'result: foobar'] == groovyResult
            assert new TryFinallyJavaTest().test() == groovyResult
        """
    }
}
