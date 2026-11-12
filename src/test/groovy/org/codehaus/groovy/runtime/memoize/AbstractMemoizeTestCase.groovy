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
package org.codehaus.groovy.runtime.memoize


import groovy.transform.PackedClosures
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals


abstract class AbstractMemoizeTestCase {

    volatile int counter = 0

    AbstractMemoizeTestCase() {
        super()
    }

    @Test
    void testCorrectness() {
        Closure cl = { it * 2 }
        Closure mem = buildMemoizeClosure(cl)
        assert 10 == mem(5)
        assert 4 == mem(2)
    }

    // GROOVY-12673
    @Test
    void testDelegateAndThisObject() {
        def d = [name: 'd']
        Closure cl = { x -> name + x }
        cl.delegate = d
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        Closure mem = buildMemoizeClosure(cl)

        assert mem.owner === this
        assert mem.thisObject === this
        assert mem.delegate === d
        assert mem.resolveStrategy == Closure.DELEGATE_FIRST
        assert mem(1) == 'd1'
    }

    // GROOVY-12673
    @Test
    void testSetDelegateAndResolveStrategy() {
        def d = [name: 'd']
        Closure mem = buildMemoizeClosure { x -> name + x }
        mem.delegate = d
        mem.resolveStrategy = Closure.DELEGATE_ONLY

        assert mem.delegate === d
        assert mem.resolveStrategy == Closure.DELEGATE_ONLY
        assert mem(1) == 'd1'
    }

    // GROOVY-12673
    @Test
    void testWith() {
        Closure mem = buildMemoizeClosure { m -> name }

        assert [name: 'd'].with(mem) == 'd'
        assert mem.delegate === this
        assert mem.resolveStrategy == Closure.OWNER_FIRST
    }

    abstract Closure buildMemoizeClosure(Closure cl)

    @Test
    void testNullParams() {
        Closure cl = { 2 }
        Closure mem = cl.memoize()
        assert 2 == mem(5)
        assert 2 == mem(2)
        assert 2 == mem(null)
    }

    @Test
    void testNullResult() {
        Closure cl = { counter++; if (it == 5) return null else return 2 }
        Closure mem = cl.memoize()
        assert counter == 0
        assert null == mem(5)
        assert counter == 1
        assert 2 == mem(2)
        assert counter == 2
        assert null == mem(5)
        assert 2 == mem(2)
        assert counter == 2
    }

    @Test
    void testNoParams() {
        Closure cl = { -> 2 }
        Closure mem = cl.memoize()
        assert 2 == mem()
        assert 2 == mem()
    }

    @Test
    void testCaching() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoize()
        assert 10 == mem(5)
        assert flag
        flag = false
        assert 4 == mem(2)
        assert flag
        flag = false

        assert 4 == mem(2)
        assert 4 == mem(2)
        assert 10 == mem(5)
        assert !flag

        assert 6 == mem(3)
        assert flag
        flag = false
        assert 6 == mem(3)
        assert !flag
    }

    @Test
    void testComplexParameter() {
        def callFlag = []

        Closure cl = { a, b, c ->
            callFlag << true
            c
        }
        Closure mem = cl.memoize()
        checkParams(mem, callFlag, [1, 2, 3], 3)
        checkParams(mem, callFlag, [1, 2, 4], 4)
        checkParams(mem, callFlag, [1, [2], 4], 4)
        checkParams(mem, callFlag, [[1: '1'], [2], 4], 4)
        checkParams(mem, callFlag, [[1, 2], 2, 4], 4)
        checkParams(mem, callFlag, [[1, 2], null, 4], 4)
        checkParams(mem, callFlag, [null, null, 4], 4)
        checkParams(mem, callFlag, [null, null, null], null)
        checkParams(mem, callFlag, [null, [null], null], null)
    }

    def checkParams(Closure mem, callFlag, args, desiredResult) {
        assertEquals desiredResult, mem( * args )
        assert !callFlag.empty
        callFlag.clear()
        assert desiredResult == mem(*args)
        assert callFlag.empty
    }

    // GROOVY-12669
    @Test
    void testRehydrateCallsTheRehydratedClosure() {
        def o = new Named('o')
        def p = new Named('p')
        Closure mem = buildMemoizeClosure(o.closure())
        assert mem(1) == 'o1'

        Closure copy = mem.rehydrate(null, p, p)
        assert copy.owner.is(p)
        assert copy(1) == 'p1'
        assert copy(2) == 'p2'
        assert copy(1) == 'p1'
        assert p.calls == 2

        assert mem(1) == 'o1'
        assert mem(2) == 'o2'
        assert o.calls == 2
    }

    // GROOVY-12669
    @Test
    void testRehydrateUsesTheNewDelegate() {
        Closure cl = { x -> bar + x }
        cl.delegate = [bar: 'a']
        Closure mem = buildMemoizeClosure(cl)
        assert mem(1) == 'a1'

        Closure copy = mem.rehydrate([bar: 'b'], this, this)
        assert copy(1) == 'b1'
        assert mem(1) == 'a1'
    }

    // GROOVY-12669
    @Test
    void testDehydrateClearsTheMemoizedClosure() {
        def o = new Named('o')
        def p = new Named('p')
        Closure mem = buildMemoizeClosure(o.closure())
        assert mem(1) == 'o1'

        Closure copy = mem.dehydrate()
        assert copy.owner == null
        shouldFail(MissingMethodException) {
            copy(1)
        }
        assert copy.rehydrate(null, p, p)(1) == 'p1'
        assert o.calls == 1
    }

    static class Named {
        final String name
        int calls

        Named(String name) {
            this.name = name
        }

        String foo() {
            calls++
            name
        }

        Closure closure() {
            return { x -> foo() + x }
        }
    }

    // GROOVY-12674
    @Test
    void testSerializedCopyStillMemoizes() {
        Closure mem = buildMemoizeClosure(new Counting().closure())
        assert mem(1) == 2

        Closure copy = roundTrip(mem)
        Counting owner = copy.owner
        assert owner.calls == 1
        assert copy(1) == 2
        assert copy(2) == 4
        assert copy(3) == 6
        int calls = owner.calls
        assert copy(2) == 4
        assert copy(3) == 6
        assert owner.calls == calls
    }

    // GROOVY-12674
    @Test
    void testSerializedCopyOfDehydratedClosure() {
        Closure mem = buildMemoizeClosure(Counting.dehydrated())
        assert mem(1) == 2

        Closure copy = roundTrip(mem)
        assert copy.owner == null
        assert copy(1) == 2
        assert copy(2) == 4
        assert mem(2) == 4
    }

    // GROOVY-12669: the serialized form names cache factory classes, not compiler-generated lambda methods,
    // which a reader on another version or build of Groovy cannot resolve
    @Test
    void testSerializedFormHoldsNoLambda() {
        Closure mem = buildMemoizeClosure(Counting.dehydrated())
        def out = new ByteArrayOutputStream()
        out.withObjectOutputStream { it.writeObject(mem) }
        assert !new String(out.toByteArray(), 'ISO-8859-1').contains('SerializedLambda')
    }

    static Closure roundTrip(Closure closure) {
        def out = new ByteArrayOutputStream()
        out.withObjectOutputStream { it.writeObject(closure) }
        new ByteArrayInputStream(out.toByteArray()).withObjectInputStream(AbstractMemoizeTestCase.classLoader) {
            (Closure) it.readObject()
        }
    }

    // serialized fixtures: the closures have to compile as classes
    @PackedClosures(mode = PackedClosures.PackMode.DISABLED)
    static class Counting implements Serializable {
        int calls

        Closure closure() {
            return { x -> calls++; x == null ? null : x * 2 }
        }

        static Closure dehydrated() {
            return { x -> x * 2 }.dehydrate()
        }
    }
}
