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
package groovy

class ClosureMethodCallTest extends GroovyTestCase {

    void testCallingClosureWithMultipleArguments() {
        def foo
        def closure = { a, b -> foo = "hello ${a} and ${b}".toString() }

        closure("james", "bob")

        assert foo == "hello james and bob"

        closure.call("sam", "james")

        assert foo == "hello sam and james"
    }

    void testClosureCallMethodWithObjectArray() {
        // GROOVY-2266
        def args = [1] as Object[]
        def closure = { x -> x[0] }
        assert closure.call(args) == 1
    }

    void testClosureWithStringArrayCastet() {
        def doSomething = { list -> list }

        String[] x = ["hello", "world"]
        String[] y = ["hello", "world"]

        assert doSomething(x as String[]) == x
        assert doSomething(y) == y
    }

    void testClosureAsLocalVar() {
        def local = { Map params -> params.x * params.y }
        assert local(x: 2, y: 3) == 6
    }

    void testClosureDirectly() {
        assert { Map params -> params.x * params.y }(x: 2, y: 3) == 6
    }

    def attribute

    void testClosureAsAttribute() {
        attribute = { Map params -> params.x * params.y }
        assert attribute(x: 2, y: 3) == 6
    }

    void testSystemOutPrintlnAsAClosure() {
        def closure = System.out.&println
        closure("Hello world")
    }

    //GROOVY-6819
    void test() {
        assertScript '''
            class Foo {
                static justcallme(Closure block) {
                    block()
                }
                static foo() {2}
                static bar(Closure block) {
                    this.justcallme {
                        this.foo()
                    }
                }
            }
            assert Foo.bar() {} == 2
        '''
    }

    //GROOVY-9140
    void testCorrectErrorForClassInstanceMethodReference() {
        assertScript '''
            class Y {
                def m() {1}
            }
            
            ref = Y.&m
            assert ref(new Y()) == 1
        '''
        shouldFail MissingMethodException, '''
            class Y {
                def m() {1}
            }

            ref = Y.&m
            assert ref(new Y()) == 1
            assert ref() == 1
        '''
        shouldFail MissingMethodException, '''
            class Y {
                def m() {1}
            }

            ref = Y.&m
            assert ref(1) == 1
        '''
    }
}
