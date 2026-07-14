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
package org.codehaus.groovy.runtime

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * {@code Closure.call}'s cached fast path is keyed on the subclass's {@code doCall}
 * declarations — "call selects a doCall" — rather than on {@code call} overloads,
 * with the GROOVY-11911 {@code call()}/{@code call(Object)} carve-out kept for
 * subclasses declaring no doCall at all. These tests pin the doCall keying for the
 * shapes the ecosystem actually writes (a hand-written subclass with a typed doCall
 * is the Gradle KotlinClosure0-3 pattern), the hierarchy walk, the vararg exclusion,
 * ambiguity, and that nested closure calls inside a dispatched body keep their own
 * fast path (the re-entry latch applies only to carve-out targets).
 */
final class ClosureCallDoCallKeyTest {

    @Test
    void typedDoCallAdapterIsServed() {
        // the KotlinClosure1 shape: hand-written subclass, typed doCall, no call overloads
        assertScript '''
            class Adapter extends Closure<Integer> {
                Adapter() { super(null) }
                Integer doCall(Integer x) { x + 100 }
            }
            def a = new Adapter()
            assert [1, 2].collect(a) == [101, 102]     // Java/GDK entry
            assert a(5) == 105                          // dynamic entry
            // guard fall-through: a non-Integer argument declines to the metaclass,
            // which rejects it exactly as before
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) {
                ['x'].collect(a)
            }
        '''
    }

    @Test
    void protectedDoCallIsServed() {
        // the MOP dispatches protected doCall declarations, so the cache must too
        assertScript '''
            class Guarded extends Closure<String> {
                Guarded() { super(null) }
                protected String doCall(String s) { 'p:' + s }
            }
            assert ['a', 'b'].collect(new Guarded()) == ['p:a', 'p:b']
        '''
    }

    @Test
    void varargDoCallStaysOnMetaclass() {
        // the Geb InvocationForwarding shape: an array-typed parameter is vararg
        // collection, which stays metaclass work — behaviour unchanged either way
        assertScript '''
            class Fwd extends Closure<Object> {
                Fwd() { super(null) }
                protected doCall(Object[] args) { args.toList() }
            }
            def f = new Fwd()
            assert f.call(1, 2) == [1, 2]
            assert [7].collect(f) == [[7]]
        '''
    }

    @Test
    void overriddenDoCallDispatchesMostDerived() {
        // the hierarchy walk dedupes by signature: an override wins over its parent
        assertScript '''
            class Base extends Closure<String> {
                Base() { super(null) }
                String doCall(Integer x) { 'base:' + x }
            }
            class Derived extends Base {
                @Override String doCall(Integer x) { 'derived:' + x }
            }
            assert [1].collect(new Derived()) == ['derived:1']
            assert [1].collect(new Base()) == ['base:1']
        '''
    }

    @Test
    void ambiguousSameArityDoCallsDeclineToMetaclass() {
        // two typed same-arity doCalls: selection is the metaclass's job; results must
        // match dynamic dispatch
        assertScript '''
            class Picky extends Closure<String> {
                Picky() { super(null) }
                String doCall(Integer x) { 'int' }
                String doCall(String x) { 'str' }
            }
            def p = new Picky()
            assert [1].collect(p) == ['int']
            assert ['a'].collect(p) == ['str']
        '''
    }

    @Test
    void nestedClosureCallsInsideDispatchedBodyKeepWorking() {
        // the re-entry latch applies only to 11911 carve-out targets: a doCall body that
        // itself drives closures through the GDK must behave identically (and keeps the
        // fast path — previously the thread-global latch silently disabled it)
        assertScript '''
            def inner = { it * 2 }
            def outer = { List xs -> xs.collect(inner).sum() }
            assert [[1, 2, 3]].collect(outer) == [12]
            // recursion through the cache is ordinary recursion
            def fact
            fact = { int n -> n <= 1 ? 1 : n * fact(n - 1) }
            assert fact(5) == 120
        '''
    }
}
