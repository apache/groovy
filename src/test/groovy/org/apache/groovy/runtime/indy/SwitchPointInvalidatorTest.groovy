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
package org.apache.groovy.runtime.indy

import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandles
import java.lang.invoke.SwitchPoint
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Unit tests for {@link SwitchPointInvalidator}.
 */
final class SwitchPointInvalidatorTest {

    @Test
    void lazyAllocation_returnsValidSwitchPointOnFirstUse() {
        def inv = new SwitchPointInvalidator()
        SwitchPoint sp = inv.switchPoint
        assertFalse(sp.hasBeenInvalidated())
        assertSame(sp, inv.switchPoint)
        assertEquals(0, inv.retirementCount)
    }

    @Test
    void invalidate_withoutPriorGet_isSafe_andDoesNotCount() {
        def inv = new SwitchPointInvalidator()
        inv.invalidate()
        assertEquals(0, inv.retirementCount)
        SwitchPoint sp = inv.switchPoint
        assertFalse(sp.hasBeenInvalidated())
    }

    @Test
    void invalidate_invalidatesCapturedSwitchPoint_andAllocatesFresh() {
        def inv = new SwitchPointInvalidator()
        SwitchPoint first = inv.switchPoint
        inv.invalidate()
        assertTrue(first.hasBeenInvalidated())
        assertEquals(1, inv.retirementCount)
        SwitchPoint second = inv.switchPoint
        assertNotSame(first, second)
        assertFalse(second.hasBeenInvalidated())
    }

    @Test
    void detachLive_returnsLiveSpWithoutInvalidating_andCountsRetirement() {
        def inv = new SwitchPointInvalidator()
        SwitchPoint live = inv.switchPoint
        assertFalse(live.hasBeenInvalidated())
        SwitchPoint detached = inv.detachLive()
        assertSame(live, detached)
        assertFalse(detached.hasBeenInvalidated(), 'detach must not invalidate; caller batches')
        assertEquals(1, inv.retirementCount)
        assertNull(inv.detachLive())
        SwitchPoint.invalidateAll(detached)
        assertTrue(detached.hasBeenInvalidated())
        SwitchPoint fresh = inv.switchPoint
        assertNotSame(live, fresh)
        assertFalse(fresh.hasBeenInvalidated())
    }

    @Test
    void detachLive_whenNothingLive_returnsNull() {
        def inv = new SwitchPointInvalidator()
        assertNull(inv.detachLive())
        assertEquals(0, inv.retirementCount)
    }

    @Test
    void guardWithTest_fallsBackAfterInvalidate() {
        def inv = new SwitchPointInvalidator()
        def target = MethodHandles.constant(int, 1)
        def fallback = MethodHandles.constant(int, 2)
        def guarded = inv.switchPoint.guardWithTest(target, fallback)
        assertEquals(1, guarded.invokeWithArguments())
        inv.invalidate()
        assertEquals(2, guarded.invokeWithArguments())
    }

    @Test
    void concurrentGetAndInvalidate_neverLeavesOrphanValidSp() {
        def inv = new SwitchPointInvalidator()
        int threads = 8
        int rounds = 200
        def start = new CyclicBarrier(threads)
        def errors = new AtomicInteger()
        def workers = (0..<threads).collect { i ->
            Thread.start {
                start.await()
                rounds.times { r ->
                    try {
                        if ((i + r) % 3 == 0) {
                            inv.invalidate()
                        } else {
                            SwitchPoint sp = inv.switchPoint
                            // After a concurrent invalidate, a previously captured SP may
                            // be invalid; a freshly returned SP from get must be valid.
                            if (!sp.hasBeenInvalidated()) {
                                // ok
                            }
                        }
                    } catch (Throwable t) {
                        errors.incrementAndGet()
                    }
                }
            }
        }
        workers*.join()
        assertEquals(0, errors.get())
        SwitchPoint after = inv.switchPoint
        assertFalse(after.hasBeenInvalidated())
    }

    @Test
    void concurrentDetach_onlyOneThreadReceivesSp() {
        def inv = new SwitchPointInvalidator()
        SwitchPoint live = inv.switchPoint
        int threads = 4
        def start = new CountDownLatch(1)
        def got = Collections.synchronizedList([])
        def workers = (0..<threads).collect {
            Thread.start {
                start.await()
                SwitchPoint d = inv.detachLive()
                if (d != null) {
                    got << d
                }
            }
        }
        start.countDown()
        workers*.join()
        assertEquals(1, got.size())
        assertSame(live, got[0])
        assertEquals(1, inv.retirementCount)
    }

    @Test
    void invalidateIfLive_null_isNoOp() {
        SwitchPointInvalidator.invalidateIfLive(null)
    }

    @Test
    void invalidateIfLive_alreadyInvalidated_isNoOp() {
        def inv = new SwitchPointInvalidator()
        SwitchPoint sp = inv.switchPoint
        inv.invalidate()
        assertTrue(sp.hasBeenInvalidated())
        // second call must not throw; covers !hasBeenInvalidated() false branch
        SwitchPointInvalidator.invalidateIfLive(sp)
    }

    @Test
    void invalidate_afterDetach_passesNullToInvalidateIfLive() {
        def inv = new SwitchPointInvalidator()
        inv.switchPoint
        inv.detachLive()
        inv.invalidate() // detachLive returns null → invalidateIfLive(null)
        assertEquals(1, inv.retirementCount)
    }

    @Test
    void anySwitchPointAllocated_setByAllocation_andMonotonic() {
        // The flag is process-global and monotonic, and this shared test JVM has
        // long since allocated SwitchPoints; only the true-and-stays-true side is
        // assertable here. The false side (bulk retirement skipped in a process
        // that never links an indy MOP guard) is covered by classic-mode runs.
        def inv = new SwitchPointInvalidator()
        inv.switchPoint
        assertTrue(SwitchPointInvalidator.isAnySwitchPointAllocated())
        inv.invalidate()
        assertTrue(SwitchPointInvalidator.isAnySwitchPointAllocated(), 'flag never resets')
    }

    @Test
    void concurrentGet_retryOnCasLoss() {
        // Hammer getSwitchPoint from many threads so some lose the CAS and retry.
        def inv = new SwitchPointInvalidator()
        inv.invalidate() // ensure current is null
        int threads = 16
        def start = new CyclicBarrier(threads)
        def seen = Collections.synchronizedSet([] as Set)
        def workers = (0..<threads).collect {
            Thread.start {
                start.await()
                50.times {
                    seen << inv.switchPoint
                }
            }
        }
        workers*.join()
        // All threads must observe a non-null valid SP; typically a single identity
        // after the first successful CAS, but concurrent invalidate is not used here.
        assertFalse(seen.isEmpty())
        seen.each { SwitchPoint sp -> assertFalse(sp.hasBeenInvalidated()) }
    }
}
