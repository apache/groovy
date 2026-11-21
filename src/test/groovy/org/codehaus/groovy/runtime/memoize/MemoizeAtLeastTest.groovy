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

import org.junit.jupiter.api.Test

import java.util.concurrent.atomic.AtomicInteger

class MemoizeAtLeastTest extends AbstractMemoizeTestCase {

    Closure buildMemoizeClosure(Closure cl) {
        cl.memoizeAtLeast(100)
    }

    @Test
    void testZeroCache() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoizeAtLeast(0)
        [1, 2, 3, 4, 5, 6].each { mem(it) }
        assert flag
    }

    // GROOVY-12669
    @Test
    void testRehydratedCopyKeepsTheProtectedCacheSize() {
        def o = new Named('o')
        def p = new Named('p')
        Closure mem = o.closure().memoizeAtLeast(2)
        [1, 2, 3].each { mem(it) }

        Closure copy = mem.rehydrate(null, p, p)
        assert copy.@cache instanceof ConcurrentSoftCache
        assert !copy.@cache.is(mem.@cache)
        assert copy.@protectedCacheSize == 2
        assert copy.@lruProtectionStorage.@maxSize == 2
        assert !copy.@lruProtectionStorage.is(mem.@lruProtectionStorage)
        assert !copy.@queue.is(mem.@queue)
        [1, 2, 3].each { assert copy(it) == "p$it" }
        assert p.calls == 3

        Closure unprotected = new Named('o').closure().memoizeAtLeast(0).dehydrate()
        assert unprotected.@protectedCacheSize == 0
        assert unprotected.@lruProtectionStorage instanceof NullProtectionStorage
    }

    // GROOVY-12669
    @Test
    void testCopyOfClosureMemoizedWithSuppliedCacheSharesTheCache() {
        def o = new Named('o')
        def p = new Named('p')
        Closure mem = Memoize.buildSoftReferenceMemoizeFunction(2, new ConcurrentSoftCache<Object, Object>(), o.closure())
        assert mem(1) == 'o1'

        Closure copy = mem.rehydrate(null, p, p)
        assert copy.@cache.is(mem.@cache)
        assert copy.@lruProtectionStorage.is(mem.@lruProtectionStorage)
        assert copy.@queue.is(mem.@queue)
        assert copy(2) == 'p2'
        assert p.calls == 1
    }

    @Test
    void testMemoizeAtLeastConcurrently() {
        AtomicInteger cnt = new AtomicInteger(0)
        Closure cl = {
            cnt.incrementAndGet()
            it * 2
        }
        Closure mem = cl.memoizeAtLeast(3)
        [4, 5, 6, 4, 5, 6, 4, 5, 6].collect { num -> Thread.start { mem(num) } }*.join()

        assert 3 == cnt.get()
    }

    // GROOVY-12674
    @Test
    void testSerializedCopyDropsCachedValues() {
        [0, 2].each { protectedCacheSize ->
            Closure mem = new Counting().closure().memoizeAtLeast(protectedCacheSize)
            assert mem(1) == 2
            assert mem(null) == null

            Closure copy = roundTrip(mem)
            Counting owner = copy.owner
            assert owner.calls == 2
            assert copy(1) == 2
            assert copy(null) == null
            assert owner.calls == 4
            assert copy(1) == 2
            assert copy(null) == null
            assert owner.calls == 4
            assert mem(1) == 2
            assert mem.owner.calls == 2
        }
    }

    // GROOVY-12674
    @Test
    void testSerializedCopyKeepsProtectedCacheSize() {
        Closure copy = roundTrip(new Counting().closure().memoizeAtLeast(2))
        [1, 2, 3].each { copy(it) }
        assert copy.@lruProtectionStorage.size() == 2
    }
}
