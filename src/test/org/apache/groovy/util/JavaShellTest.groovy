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

import org.junit.Test

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
    void runMain() {
        JavaShell js = new JavaShell()
        final mcn = "tests.Test1"
        try {
            js.runMain(mcn, '''
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
