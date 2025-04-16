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
package org.codehaus.groovy.classgen.asm.sc

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class StaticCompileFlowTypingTest {

    @Test
    void testFlowTyping1() {
        assertScript '''
            @groovy.transform.CompileStatic
            Object m() {
                def o = 1
                def s = o.toString()
                o = 'string'
                println o.toUpperCase()
                o = '123'
                o = o.toInteger()
            }
            assert m() == 123
        '''
    }

    // GROOVY-9344
    @Test
    void testFlowTyping2() {
        assertScript '''
            class A {}
            class B {}

            @groovy.transform.CompileStatic
            String m() {
                def x = new A()
                def c = { ->
                    x = new B()
                    x.class.simpleName
                }
                c()
            }
            assert m() == 'B'
        '''
    }

    // GROOVY-9344
    @Test
    void testFlowTyping3() {
        assertScript '''
            class A {}
            class B {}

            @groovy.transform.CompileStatic
            String m() {
                def x = new A()
                def c = { ->
                    x = new B()
                }
                c()
                x.class.simpleName
            }
            assert m() == 'B'
        '''
    }

    // GROOVY-8946
    @Test
    void testFlowTyping4() {
        assertScript '''
            /*@GrabResolver(name='grails', root='https://repo.grails.org/grails/core')
            @Grapes([
                @Grab('javax.servlet:javax.servlet-api:3.0.1'),
                @Grab('org.grails.plugins:converters:3.3.+'),
                @Grab('org.grails:grails-web:3.3.+'),
                @Grab('org.slf4j:slf4j-nop:1.7.33')
            ])
            @GrabExclude('org.codehaus.groovy:*')
            import static grails.converters.JSON.parse
            */
            class JSONElement {
                def getProperty(String name) {
                    if (name == 'k') return [1,2]
                }
            }
            JSONElement parse(String json) {
                new JSONElement()
            }

            @groovy.transform.CompileStatic
            def test() {
                def json = parse('[{"k":1},{"k":2}]')
                def vals = json['k']
                assert vals == [1,2]
                boolean result = 'k'.tokenize('.').every { token -> // 'k' represents a path like 'a.b.c.d'
                    json = json[token]
                }
                assert result
                return json // Cannot cast object '[1, 2]' with class 'java.util.ArrayList' to class 'org.grails.web.json.JSONElement'
            }
            test()
        '''
    }

    @Test
    void testInstanceOf() {
        assertScript '''
            @groovy.transform.CompileStatic
            Object m(Object o) {
                if (o instanceof String) {
                    return o.toUpperCase()
                }
                return null
            }
            assert m('happy new year') == 'HAPPY NEW YEAR'
            assert m(123) == null
        '''
    }

    @Test
    void testMethodSelection() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                int foo(String o) { 1 }
                int foo(int x) { 2 }
            }
            A a = new A()
            assert a.foo('happy new year') == 1
            assert a.foo(123) == 2
        '''
    }

    @Test
    void testMethodSelectionDifferentFromDynamicGroovy() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                int foo(String o) { 1 }
                int foo(int x) { 2 }
                int foo(Object x) { 3 }
            }

            // if tests are not wrapped in a statically compiled section, method
            // selection is dynamic
            @groovy.transform.CompileStatic
            void performTests() {
                A a = new A()
                Object[] arr = [ 'happy new year', 123, new Object() ]
                assert a.foo(arr[0]) == 3
                assert a.foo(arr[1]) == 3
                assert a.foo(arr[2]) == 3
            }
            performTests()

            // tests that behaviour is different from regular Groovy
            A a = new A()
            Object[] arr = [ 'happy new year', 123, new Object() ]
            assert a.foo(arr[0]) == 1
            assert a.foo(arr[1]) == 2
            assert a.foo(arr[2]) == 3
        '''
    }
}
