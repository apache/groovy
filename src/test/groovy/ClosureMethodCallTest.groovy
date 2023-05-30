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

import org.junit.Test

import java.util.concurrent.Executors

import static groovy.test.GroovyAssert.assertScript

final class ClosureMethodCallTest {

    @Test
    void testCallingClosureWithMultipleArguments() {
        def foo
        def closure = { a, b -> foo = "hello ${a} and ${b}".toString() }

        closure('james', 'bob')

        assert foo == 'hello james and bob'

        closure.call('sam', 'james')

        assert foo == 'hello sam and james'
    }

    @Test // GROOVY-2266
    void testClosureCallMethodWithObjectArray() {
        def args = [1] as Object[]
        def closure = { x -> x[0] }
        assert closure.call(args) == 1
    }

    @Test
    void testClosureWithStringArrayCastet() {
        def doSomething = { list -> list }

        String[] x = ['hello', 'world']
        String[] y = ['hello', 'world']

        assert doSomething(x as String[]) == x
        assert doSomething(y) == y
    }

    @Test
    void testClosureAsLocalVar() {
        def local = { Map params -> params.x * params.y }
        assert local(x: 2, y: 3) == 6
    }

    @Test
    void testClosureDirectly() {
        assert { Map params -> params.x * params.y }(x: 2, y: 3) == 6
    }

    def attribute

    @Test
    void testClosureAsAttribute() {
        attribute = { Map params -> params.x * params.y }
        assert attribute(x: 2, y: 3) == 6
    }

    @Test // GROOVY-6819
    void testFixForIncompatibleClassChangeError() {
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

    @Test // GROOVY-9397
    void testRespondsToIsThreadSafe() {
        def executor = Executors.newCachedThreadPool()
        try {
            final Closure action = { -> }
            // ensure that executing the closure and calling respondsTo
            // concurrently doesn't throw an exception.
            for (i in 1..500) {
                executor.execute(action)
                executor.execute(() -> action.respondsTo('test'))
            }
        } finally {
            executor.shutdownNow()
        }
    }
}
