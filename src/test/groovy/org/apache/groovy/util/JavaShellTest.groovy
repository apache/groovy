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
package org.apache.groovy.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

class JavaShellTest {
    @Test
    void compileAll() {
        JavaShell js = new JavaShell()
        final mcn = "tests.Test1"
        final cn = "tests.TestHelper"
        Map<String, Class<?>> classes = js.compileAll(mcn, '''
            package tests;
            public class Test1 {}
            class TestHelper {}
        ''')

        assert 2 == classes.size()
        assert mcn == classes.get(mcn).getName()
        assert cn == classes.get(cn).getName()
    }

    @Test
    void compile() {
        JavaShell js = new JavaShell()
        final mcn = "tests.Test1"
        Class<?> c = js.compile(mcn, '''
            package tests;
            public class Test1 {
                public static String test() { return "Hello"; }
            }
        ''')

        Object result = c.getDeclaredMethod("test").invoke(null)
        assert "Hello" == result
    }

    @Test
    void run() {
        JavaShell js = new JavaShell()
        final mcn = "tests.Test1"
        try {
            js.run(mcn, '''
            package tests;
            public class Test1 {
                public static void main(String[] args) {
                    throw new RuntimeException(TestHelper.msg());
                }
            }
            class TestHelper {
                static String msg() { return "Boom"; }
            }
        ''')
        } catch (Throwable t) {
            assert t.getCause().getMessage().contains("Boom")
        }
    }

    @Test
    void compileAllTo_writesPackagedClassUnderPackageDir(@TempDir Path out) {
        JavaShell js = new JavaShell()
        final mcn = 'tests.Test1'
        Map<String, Path> written = js.compileAllTo(mcn, '''
            package tests;
            public class Test1 {
                public static String hello() { return "hi"; }
            }
        ''', out)

        assert written.size() == 1
        Path expected = out.resolve('tests/Test1.class')
        assert written[mcn] == expected
        assert Files.exists(expected)
        assert Files.size(expected) > 0
    }

    @Test
    void compileAllTo_writesDefaultPackageClassDirectlyUnderOutputDir(@TempDir Path out) {
        JavaShell js = new JavaShell()
        Map<String, Path> written = js.compileAllTo('Plain', '''
            public class Plain {}
        ''', out)

        assert written.size() == 1
        Path expected = out.resolve('Plain.class')
        assert written['Plain'] == expected
        assert Files.exists(expected)
    }

    @Test
    void compileAllTo_writesAuxiliaryAndNestedClasses(@TempDir Path out) {
        final mcn = 'tests.Outer'
        final src = '''
            package tests;
            public class Outer {
                public static class Inner {}
            }
            class Helper {}
        '''
        Map<String, Path> written = new JavaShell().compileAllTo(mcn, src, out)

        assert written.keySet() == ['tests.Outer', 'tests.Outer$Inner', 'tests.Helper'] as Set
        assert Files.exists(out.resolve('tests/Outer.class'))
        assert Files.exists(out.resolve('tests/Outer$Inner.class'))
        assert Files.exists(out.resolve('tests/Helper.class'))

        // contract: iteration order is stable across invocations with the same source
        // (the specific order is whatever javac emitted, which is JDK-dependent)
        Map<String, Path> written2 = new JavaShell().compileAllTo(mcn, src, out)
        assert new ArrayList<>(written2.keySet()) == new ArrayList<>(written.keySet())
    }

    @Test
    void compileAllTo_createsMissingIntermediateDirectories(@TempDir Path root) {
        Path out = root.resolve('does/not/exist/yet')
        assert !Files.exists(out)

        JavaShell js = new JavaShell()
        js.compileAllTo('a.b.c.Deep', '''
            package a.b.c;
            public class Deep {}
        ''', out)

        assert Files.exists(out.resolve('a/b/c/Deep.class'))
    }

    @Test
    void compileAllTo_overwritesExistingClassFile(@TempDir Path out) {
        Path target = out.resolve('pkg/Same.class')
        Files.createDirectories(target.getParent())
        Files.write(target, 'stale'.getBytes())
        long staleSize = Files.size(target)

        JavaShell js = new JavaShell()
        js.compileAllTo('pkg.Same', '''
            package pkg;
            public class Same { public static int v() { return 42; } }
        ''', out)

        assert Files.exists(target)
        assert Files.size(target) != staleSize          // replaced, not appended-to
        assert Files.readAllBytes(target)[0..3] == [(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE]
    }

    @Test
    void compileAllTo_classesAlsoAvailableViaClassLoader(@TempDir Path out) {
        JavaShell js = new JavaShell()
        final mcn = 'tests.Probe'
        js.compileAllTo(mcn, '''
            package tests;
            public class Probe { public static String tag() { return "loaded"; } }
        ''', out)

        Class<?> c = js.getClassLoader().loadClass(mcn)
        assert 'loaded' == c.getDeclaredMethod('tag').invoke(null)
    }

    @Test
    void compileAllTo_appliesProvidedCompilerOptions(@TempDir Path out) {
        JavaShell js = new JavaShell()
        // -parameters retains formal parameter names in the class file; without it they become arg0/arg1.
        js.compileAllTo('p.WithParams', ['-parameters'], '''
            package p;
            public class WithParams {
                public static String greet(String who) { return "hi " + who; }
            }
        ''', out)

        Class<?> c = js.getClassLoader().loadClass('p.WithParams')
        def param = c.getDeclaredMethod('greet', String).parameters[0]
        assert param.isNamePresent()
        assert param.name == 'who'
    }

    @Test
    void compileAllTo_noOptionsOverloadCompilesIdentically(@TempDir Path out) {
        JavaShell js = new JavaShell()
        Map<String, Path> written = js.compileAllTo('q.Q', '''
            package q;
            public class Q {}
        ''', out)
        assert written.keySet() == ['q.Q'] as Set
        assert Files.exists(out.resolve('q/Q.class'))
    }

    @Test
    void getClassLoader() {
        JavaShell js = new JavaShell()
        final mcn = "tests.Test1"
        js.compile(mcn, '''
            package tests;
            public class Test1 {
                public static String test() { return TestHelper.msg(); }
            }

            class TestHelper {
                public static String msg() { 
                    return "Hello, " + groovy.lang.GString.class.getSimpleName();
                }
            }
        ''')

        new GroovyShell(js.getClassLoader()).evaluate '''
            import tests.Test1

            assert 'Hello, GString' == Test1.test()
        '''
    }
}
