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

import groovy.test.GroovyTestCase

import java.util.concurrent.Callable

/**
 * Tests calling methods with Closures as parameters.
 */
class ClosureAsParamTest extends GroovyTestCase {

    int x

    void testSimpleBlockCall() {
        assert 'hello!' == assertClosure({ owner -> owner })
    }

    void testRunnable() {
        x = 0
        assertRunnable({ x = 1 })
        assert x == 1
    }

    void testCallable() {
        x = 0
        assert 'Callable' == assertCallable({ x = 2; 'Callable' })
        assert x == 2
    }

    void testRunnablePreferred() {
        x = 0
        assertRunnableAndCallable({ x = 3; 'result' })
        assert x == 6
    }

    void testCallableCoercion() {
        x = 0
        assert 'result' == assertRunnableAndCallable({ x = 3; 'result' } as Callable)
        assert x == 9
    }

    def assertClosure(Closure block) {
        assert block != null
        block.call("hello!")
    }

    def assertRunnable(Runnable r) {
        r.run()
    }

    def assertCallable(Callable c) {
        c.call()
    }

    def assertRunnableAndCallable(Runnable r) {
        r.run()
        x *= 2
    }

    def assertRunnableAndCallable(Callable c) {
        def result = c.call()
        x *= 3
        result
    }

}
