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
        assert err.message =~ /No such property: x for class: MyClass/
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
        assert err.message =~ /No such property: x for class: MyClass/
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
        assert err.message =~ /No such property: x for class: MyClass/
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

    @Test
    void testAccessingVariableInTryCatchFinally_1() {
        def declareClass = { String lang ->
            """
            public class TryFinally${lang}Test {
                public String test() {
                    String result = "result: ";
                    try {
                        String x = "foo";
                        result += x;
                        throw new RuntimeException("expected");
                    } catch (RuntimeException e) {
                        result += "caught";
                        return result;
                    } finally {
                        result += "-finally";
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
            assert 'result: foocaught' == groovyResult
            assert new TryFinallyJavaTest().test() == groovyResult
        """
    }

    @Test
    void testNestedTryCatchFinally() {
        assertScript '''\
            class MyClass {
                def myMethod() {
                    String result = "result: "
                    try {
                        result += "outer-try;"
                        try {
                            result += "inner-try;"
                            throw new RuntimeException("inner")
                        } catch (RuntimeException e) {
                            result += "inner-catch;"
                        } finally {
                            result += "inner-finally;"
                        }
                        result += "after-inner;"
                    } finally {
                        result += "outer-finally"
                    }
                    return result
                }
            }
            assert 'result: outer-try;inner-try;inner-catch;inner-finally;after-inner;outer-finally' == new MyClass().myMethod()
        '''
    }

    @Test
    void testFinallyExecutesWithBreak() {
        assertScript '''\
            class MyClass {
                def myMethod() {
                    String result = "result: "
                    for (int i = 0; i < 3; i++) {
                        result += i + "("
                        try {
                            result += "try;"
                            if (i == 1) break
                            result += "after-break;"
                        } finally {
                            result += "finally);"
                        }
                    }
                    return result
                }
            }
            assert 'result: 0(try;after-break;finally);1(try;finally);' == new MyClass().myMethod()
        '''
    }

    @Test
    void testExceptionInFinally() {
        def declareClass = { String lang ->
            """
            public class TryFinally${lang}Test {
                public String test() {
                    String result = "result: ";
                    try {
                        return (result += "from-try;");
                    } finally {
                        throw new RuntimeException(result += "finally-error");
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

            final groovyErr = groovy.test.GroovyAssert.shouldFail(RuntimeException) {
                new TryFinallyGroovyTest().test()
            }
            final javaErr = groovy.test.GroovyAssert.shouldFail(RuntimeException) {
                new TryFinallyJavaTest().test()
            }

            assert 'result: from-try;finally-error' == groovyErr.message
            assert javaErr.message == groovyErr.message
        """
    }

    @Test
    void testMultipleCatchBlocks() {
        def declareClass = { String lang ->
            """
            public class TryFinally${lang}Test {
                public String test(int type) {
                    String result = "result: ";
                    try {
                        result += "try;";
                        switch (type) {
                            case 1: throw new IllegalArgumentException("IAE");
                            case 2: throw new NullPointerException("NPE");
                            case 3: throw new RuntimeException("RE");
                        }
                        result += "no-throw;";
                    } catch (IllegalArgumentException e) {
                        result += "catch-IAE;";
                    } catch (NullPointerException e) {
                        result += "catch-NPE;";
                    } catch (Exception e) {
                        result += "catch-other;";
                    } finally {
                        result += "finally";
                    }
                    return result;
                }
            }
            """
        }

        JavaShell js = new JavaShell()
        js.compile("tests.TryFinallyJavaTest", "package tests;\n${declareClass('Java')}")

        new GroovyShell(js.getClassLoader()).evaluate """\
            package tests;
            import tests.TryFinallyJavaTest

            ${declareClass('Groovy')}

            def groovy = new TryFinallyGroovyTest()
            def java = new TryFinallyJavaTest()

            assert 'result: try;catch-IAE;finally' == groovy.test(1)
            assert 'result: try;catch-NPE;finally' == groovy.test(2)
            assert 'result: try;catch-other;finally' == groovy.test(3)
            assert 'result: try;no-throw;finally' == groovy.test(0)

            assert java.test(1) == groovy.test(1)
            assert java.test(2) == groovy.test(2)
            assert java.test(3) == groovy.test(3)
            assert java.test(0) == groovy.test(0)
        """
    }

    @Test
    void testFinallyWithThrowExceptionInTry() {
        def err = shouldFail '''
            try {
                def msg = "from-try"
                throw new RuntimeException(msg)
            } finally {
                throw new RuntimeException("from-finally")
            }
        '''
        assert err.message =~ /from-finally/
    }
}
