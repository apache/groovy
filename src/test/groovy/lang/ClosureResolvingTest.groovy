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
package groovy.lang

import groovy.test.GroovyTestCase

/**
 * Tests how closures resolve to either a delegate or an owner for a given resolveStrategy
 *
 * @since 1.5
 */

class ClosureResolvingTest extends GroovyTestCase {

    def foo = "bar"
    def bar = "foo"

    protected void tearDown() {
        Closure.metaClass = null
    }

    void testResolveToSelf() {
        def c = { foo }
        assertEquals "bar", c.call()

        c.resolveStrategy = Closure.TO_SELF

        shouldFail {
            c.call()
        }

        def metaClass = c.class.metaClass
        metaClass.getFoo = {-> "hello!" }

        c.metaClass = metaClass

        assertEquals "hello!", c.call()

        c = { doStuff() }
        c.resolveStrategy = Closure.TO_SELF
        shouldFail {
            c.call()
        }
        metaClass = c.class.metaClass
        metaClass.doStuff = {-> "hello" }
        c.metaClass = metaClass

        assertEquals "hello", c.call()
    }

    def doStuff() { "stuff" }

    void testResolveDelegateFirst() {

        def c = { foo }

        assertEquals "bar", c.call()

        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c.delegate = [foo: "hello!"]

        assertEquals "hello!", c.call()


        c = { doStuff() }
        c.setResolveStrategy(Closure.DELEGATE_FIRST)

        assertEquals "stuff", c.call()
        c.delegate = new TestResolve1()
        assertEquals "foo", c.call()

    }

    void testResolveOwnerFirst() {
        def c = { foo }

        assertEquals "bar", c.call()

        c.delegate = [foo: "hello!"]

        assertEquals "bar", c.call()

        c = { doStuff() }
        c.delegate = new TestResolve1()
        assertEquals "stuff", c.call()
    }

    void testResolveDelegateOnly() {
        def c = { foo + bar }

        assertEquals "barfoo", c.call()

        c.resolveStrategy = Closure.DELEGATE_FIRST

        c.delegate = new TestResolve1()

        assertEquals "hellofoo", c.call()

        c.resolveStrategy = Closure.DELEGATE_ONLY
        shouldFail {
            c.call()
        }

        c.delegate = new TestResolve2()

        assertEquals "helloworld", c.call()

        c = { doStuff() }
        c.resolveStrategy = Closure.DELEGATE_ONLY
        c.delegate = new TestResolve1()
        assertEquals "foo", c.call()

    }

    void testResolveOwnerOnly() {
        def c = { foo + bar }

        assertEquals "barfoo", c.call()
        c.resolveStrategy = Closure.OWNER_ONLY

        c.delegate = new TestResolve2()
        assertEquals "barfoo", c.call()

        c = { doStuff() }
        assertEquals "stuff", c.call()
        c.resolveStrategy = Closure.OWNER_ONLY
        c.delegate = new TestResolve1()
        assertEquals "stuff", c.call()

    }

    void testOwnerDelegateChain() {
        def outerdel = new TestResolve3(del: "outer delegate")
        def innerdel = new TestResolve3(del: "inner delegate")

        def cout = {
            assert delegate == outerdel
            assert delegate.whoisThis() == outerdel
            assert delegate.del == "outer delegate"
            assert delegate.met() == "I'm the method inside 'outer delegate'"

            assert whoisThis() == outerdel
            assert del == "outer delegate"
            assert met() == "I'm the method inside 'outer delegate'"

            def cin = {
                assert delegate == innerdel
                assert delegate.whoisThis() == innerdel
                assert delegate.del == "inner delegate"
                assert delegate.met() == "I'm the method inside 'inner delegate'"

                assert whoisThis() == outerdel
                assert del == "outer delegate"
                assert met() == "I'm the method inside 'outer delegate'"

            }

            cin.delegate = innerdel
            cin()
        }

        cout.delegate = outerdel
        cout()
    }

}

class TestResolve1 {
    def foo = "hello"

    def doStuff() { "foo" }
}
class TestResolve2 {
    def foo = "hello"
    def bar = "world"

    def doStuff() { "bar" }
}

class TestResolve3 {
    def del;

    String toString() {del}

    def whoisThis() { return this }

    def met() { return "I'm the method inside '" + del + "'" }
}

