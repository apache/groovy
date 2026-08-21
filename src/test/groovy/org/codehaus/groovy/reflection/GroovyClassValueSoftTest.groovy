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
package org.codehaus.groovy.reflection

import org.junit.jupiter.api.Test

import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12281: soft-value {@code GroovyClassValue} with resurrection.
 * The forced-clear tests simulate GC clearing of the memoized SoftReference;
 * a manual {@code clear()} is a conservative superset of what a collector can
 * do (a collector never clears a reference to a strongly reachable object),
 * so correctness under these tests implies correctness under any GC schedule.
 */
final class GroovyClassValueSoftTest {

    private static final class Holder {
        final Class<?> type
        Holder(Class<?> type) { this.type = type }
    }

    private static GroovyClassValueSoft<Holder> newSubject(AtomicInteger counter) {
        new GroovyClassValueSoft<Holder>({ Class<?> type ->
            counter.incrementAndGet()
            new Holder(type)
        } as GroovyClassValue.ComputeValue<Holder>)
    }

    /** The current slot content for {@code type}: a SoftReference (reclaimable) or the value itself (pinned). */
    private static Object slotContent(GroovyClassValueSoft<?> subject, Class<?> type) {
        def storeField = GroovyClassValueSoft.getDeclaredField('store')
        storeField.accessible = true
        ClassValue store = storeField.get(subject)
        store.get(type).get()
    }

    /** Clears the memoized SoftReference for {@code type}, as a GC would. A pinned slot has none. */
    private static void forceClear(GroovyClassValueSoft<?> subject, Class<?> type) {
        def content = slotContent(subject, type)
        assert content instanceof SoftReference : 'a pinned (strong) slot cannot be cleared by GC'
        ((SoftReference) content).clear()
    }

    private static boolean survivesGc(WeakReference<?> ref) {
        for (int i = 0; i < 5 && ref.get() != null; i++) {
            System.gc()
            Thread.sleep(10)
        }
        return ref.get() != null
    }

    private static boolean awaitCollected(WeakReference<?> ref) {
        for (int i = 0; i < 100 && ref.get() != null; i++) {
            System.gc()
            byte[][] pressure = new byte[64][]
            for (int j = 0; j < pressure.length; j++) {
                pressure[j] = new byte[1 << 16]
            }
            Thread.sleep(10)
        }
        return ref.get() == null
    }

    @Test
    void memoizesLikeAnyClassValue() {
        def counter = new AtomicInteger()
        def subject = newSubject(counter)
        def first = subject.get(String)
        assertSame(first, subject.get(String))
        assertEquals(1, counter.get())
        subject.get(Integer)
        assertEquals(2, counter.get())
    }

    @Test
    void clearedValueIsResurrectedWhileAlive_sameIdentity_noRecompute() {
        def counter = new AtomicInteger()
        def subject = newSubject(counter)
        def survivor = subject.get(String)   // strong local ref: "captured by a call site"
        assertEquals(1, counter.get())

        forceClear(subject, String)
        def resurrected = subject.get(String)

        assertSame(survivor, resurrected, 'a live value must be resurrected, never replaced')
        assertEquals(1, counter.get(), 'resurrection must not invoke computeValue')
    }

    @Test
    void deadValueIsRecreatedFresh() {
        def counter = new AtomicInteger()
        def subject = newSubject(counter)
        def weak = new WeakReference<Holder>(subject.get(String))
        assertEquals(1, counter.get())

        forceClear(subject, String)
        assertTrue(awaitCollected(weak), 'unreferenced value should be collectable once cleared')

        def fresh = subject.get(String)
        assertEquals(2, counter.get(), 'a truly dead value is recomputed')
        assertSame(fresh, subject.get(String))
    }

    @Test
    void removeIsAHardDetach_evenWhileOldValueAlive() {
        def counter = new AtomicInteger()
        def subject = newSubject(counter)
        def old = subject.get(String)
        subject.remove(String)
        def fresh = subject.get(String)
        assertNotSame(old, fresh, 'remove() must forget identity (undeploy semantics)')
        assertEquals(2, counter.get())
    }

    @Test
    void valuesAreReclaimableByContract() {
        assertTrue(newSubject(new AtomicInteger()).valuesReclaimable())
    }

    @Test
    void pinHoldsTheValueInItsOwnSlotUntilUnpin() {
        def counter = new AtomicInteger()
        def subject = newSubject(counter)
        def value = subject.get(String)
        subject.pin(String, value)
        assertSame(value, slotContent(subject, String), 'a pinned slot holds the value itself, not a wrapper')

        def weak = new WeakReference<Holder>(value)
        value = null
        assertTrue(survivesGc(weak), 'a pinned value must not be collected')
        def pinned = weak.get()
        assertSame(pinned, subject.get(String))
        assertEquals(1, counter.get(), 'pin must not disturb identity or recompute')

        subject.unpin(String, pinned)
        assertTrue(slotContent(subject, String) instanceof SoftReference, 'unpin restores the reclaimable wrapper')
        assertSame(pinned, subject.get(String), 'unpin keeps the same live instance')
        pinned = null
        forceClear(subject, String)
        assertTrue(awaitCollected(weak), 'an unpinned value is reclaimable again')
    }

    @Test
    void removeReleasesThePin() {
        def counter = new AtomicInteger()
        def subject = newSubject(counter)
        def value = subject.get(String)
        subject.pin(String, value)
        def weak = new WeakReference<Holder>(value)
        value = null
        subject.remove(String)
        assertTrue(awaitCollected(weak), 'remove() must drop the pin with the association')
        subject.get(String)
        assertEquals(2, counter.get())
    }

    @Test
    void unpinOfAForeignValueIsANoop() {
        def counter = new AtomicInteger()
        def subject = newSubject(counter)
        def value = subject.get(String)
        subject.pin(String, value)
        subject.unpin(String, new Holder(String))   // not the pinned value
        assertSame(value, slotContent(subject, String), 'foreign unpin must not downgrade the slot')
    }

    @Test
    void getIfPresentNeverCreates() {
        def counter = new AtomicInteger()
        def subject = newSubject(counter)
        assertNull(subject.getIfPresent(String))
        assertEquals(0, counter.get())
        def value = subject.get(String)
        assertSame(value, subject.getIfPresent(String))
        assertEquals(1, counter.get())
    }
}
