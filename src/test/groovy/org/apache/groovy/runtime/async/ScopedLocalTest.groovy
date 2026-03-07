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
package org.apache.groovy.runtime.async

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.lang.invoke.MethodType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

import static org.junit.jupiter.api.Assertions.*

/**
 * Comprehensive unit tests for {@link ScopedLocal}.
 * <p>
 * These tests exercise both the {@code ThreadLocal}-based fallback
 * (JDK&nbsp;&lt;&nbsp;25) and the {@code ScopedValue}-based
 * implementation (JDK&nbsp;25+).  The actual backend is chosen at
 * class-load time; the tests verify the unified contract regardless
 * of which backend is active.
 *
 * @since 6.0.0
 */
@DisplayName("ScopedLocal")
@Timeout(10)
class ScopedLocalTest {

    // ------------------------------------------------------------------
    //  Backend detection and MethodHandle verification
    // ------------------------------------------------------------------

    @Test
    @DisplayName("ScopedValue backend is selected on JDK 25+")
    void testBackendSelection() {
        int jdkVersion = Runtime.version().feature()
        boolean expected = jdkVersion >= 25
        assertEquals(expected, ScopedLocal.SCOPED_VALUE_AVAILABLE,
                "SCOPED_VALUE_AVAILABLE should be $expected on JDK $jdkVersion")
    }

    @Test
    @DisplayName("MethodHandles are adapted for invokeExact on JDK 25+")
    void testMethodHandleTypesAdaptedForInvokeExact() {
        if (!ScopedLocal.SCOPED_VALUE_AVAILABLE) return

        // SV_NEW_INSTANCE: () → Object
        assertEquals(
            MethodType.methodType(Object),
                ScopedLocal.SV_NEW_INSTANCE.type(),
                "SV_NEW_INSTANCE must return Object (adapted from ScopedValue)")

        // SV_WHERE: (Object, Object) → Object
        assertEquals(
            MethodType.methodType(Object, Object, Object),
                ScopedLocal.SV_WHERE.type(),
                "SV_WHERE must accept (Object, Object) and return Object")

        // SV_GET: (Object) → Object
        assertEquals(
            MethodType.methodType(Object, Object),
                ScopedLocal.SV_GET.type(),
                "SV_GET must accept (Object) and return Object")

        // SV_IS_BOUND: (Object) → boolean
        assertEquals(
            MethodType.methodType(boolean, Object),
                ScopedLocal.SV_IS_BOUND.type(),
                "SV_IS_BOUND must accept (Object) and return boolean")

        // CARRIER_RUN: (Object, Runnable) → void
        assertEquals(
            MethodType.methodType(void, Object, Runnable),
                ScopedLocal.CARRIER_RUN.type(),
                "CARRIER_RUN must accept (Object, Runnable) and return void")
    }

    @Test
    @DisplayName("invokeExact calls work end-to-end through ScopedLocal API")
    void testInvokeExactEndToEnd() {
        // This test exercises every MethodHandle path in ScopedValueImpl:
        //   SV_NEW_INSTANCE  → newInstance() / withInitial()
        //   SV_IS_BOUND      → isBound() / get() / orElse()
        //   SV_GET           → get() / orElse()
        //   SV_WHERE         → bind() via Carrier
        //   CARRIER_RUN      → bind() via Carrier
        def sl = ScopedLocal.<String>newInstance()          // SV_NEW_INSTANCE
        assertFalse(sl.isBound())                           // SV_IS_BOUND (false path)
        assertEquals("fb", sl.orElse("fb"))                 // SV_IS_BOUND (false) + fallback

        ScopedLocal.where(sl, "hello").run {                // SV_WHERE + CARRIER_RUN
            assertTrue(sl.isBound())                        // SV_IS_BOUND (true path)
            assertEquals("hello", sl.get())                 // SV_GET
            assertEquals("hello", sl.orElse("other"))       // SV_GET via orElse
        }
    }

    @Test
    @DisplayName("invokeExact calls work with withInitial supplier")
    void testInvokeExactWithInitial() {
        def sl = ScopedLocal.withInitial { "default" }     // SV_NEW_INSTANCE
        assertTrue(sl.isBound())                            // SV_IS_BOUND → fallback path
        assertEquals("default", sl.get())                   // fallback get

        ScopedLocal.where(sl, "override").run {             // SV_WHERE + CARRIER_RUN
            assertEquals("override", sl.get())              // SV_IS_BOUND (true) + SV_GET
        }

        assertEquals("default", sl.get())                   // restored to fallback
    }

    // ------------------------------------------------------------------
    //  Accessors — get(), orElse(), isBound()
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Accessors (no initial supplier)")
    class AccessorsNoInitial {

        @Test
        @DisplayName("get() throws NoSuchElementException when unbound")
        void testGetUnbound() {
            def sl = ScopedLocal.<String>newInstance()
            assertThrows(NoSuchElementException) { sl.get() }
        }

        @Test
        @DisplayName("orElse() returns fallback when unbound")
        void testOrElseUnbound() {
            def sl = ScopedLocal.<String>newInstance()
            assertEquals("fallback", sl.orElse("fallback"))
        }

        @Test
        @DisplayName("orElse() returns null fallback when unbound")
        void testOrElseNullFallback() {
            def sl = ScopedLocal.<String>newInstance()
            assertNull(sl.orElse(null))
        }

        @Test
        @DisplayName("isBound() returns false when unbound")
        void testIsBoundUnbound() {
            def sl = ScopedLocal.<String>newInstance()
            assertFalse(sl.isBound())
        }

        @Test
        @DisplayName("get() returns bound value inside where()")
        void testGetBound() {
            def sl = ScopedLocal.<String>newInstance()
            def result = ScopedLocal.where(sl, "hello").call { sl.get() }
            assertEquals("hello", result)
        }

        @Test
        @DisplayName("orElse() returns bound value (not fallback) inside where()")
        void testOrElseBound() {
            def sl = ScopedLocal.<String>newInstance()
            def result = ScopedLocal.where(sl, "hello").call { sl.orElse("fallback") }
            assertEquals("hello", result)
        }

        @Test
        @DisplayName("isBound() returns true inside where()")
        void testIsBoundInside() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, "x").run { assertTrue(sl.isBound()) }
        }

        @Test
        @DisplayName("get() throws again after where() scope exits")
        void testGetAfterScopeExit() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, "temp").run { /* no-op */ }
            assertThrows(NoSuchElementException) { sl.get() }
        }
    }

    // ------------------------------------------------------------------
    //  Accessors — withInitial
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Accessors (with initial supplier)")
    class AccessorsWithInitial {

        @Test
        @DisplayName("get() returns initial value when unbound")
        void testGetInitial() {
            def sl = ScopedLocal.withInitial { "default" }
            assertEquals("default", sl.get())
        }

        @Test
        @DisplayName("orElse() returns initial value (not fallback) when unbound")
        void testOrElseInitial() {
            def sl = ScopedLocal.withInitial { "default" }
            assertEquals("default", sl.orElse("fallback"))
        }

        @Test
        @DisplayName("isBound() returns true when initial supplier is present")
        void testIsBoundInitial() {
            def sl = ScopedLocal.withInitial { "default" }
            assertTrue(sl.isBound())
        }

        @Test
        @DisplayName("get() returns bound value, not initial, inside where()")
        void testGetBoundOverridesInitial() {
            def sl = ScopedLocal.withInitial { "default" }
            def result = ScopedLocal.where(sl, "override").call { sl.get() }
            assertEquals("override", result)
        }

        @Test
        @DisplayName("get() restores to initial value after where() exits")
        void testGetRestoredToInitial() {
            def sl = ScopedLocal.withInitial { "default" }
            ScopedLocal.where(sl, "override").run { /* no-op */ }
            assertEquals("default", sl.get())
        }

        @Test
        @DisplayName("initial supplier is lazily invoked")
        void testLazyInitialization() {
            def counter = new AtomicInteger(0)
            def sl = ScopedLocal.withInitial {
                counter.incrementAndGet()
                "lazy"
            }
            assertEquals(0, counter.get(), "supplier must not be called at creation")
            assertEquals("lazy", sl.get())
            assertEquals(1, counter.get(), "supplier called on first access")
        }

        @Test
        @DisplayName("initial value is cached after first access")
        void testInitialValueCached() {
            def counter = new AtomicInteger(0)
            def sl = ScopedLocal.withInitial {
                counter.incrementAndGet()
                "cached"
            }
            sl.get()
            sl.get()
            sl.get()
            assertEquals(1, counter.get(), "supplier should only be called once per thread")
        }

        @Test
        @DisplayName("withInitial(null) throws NullPointerException")
        void testWithInitialNull() {
            assertThrows(NullPointerException) {
                ScopedLocal.withInitial(null)
            }
        }

        @Test
        @DisplayName("initial supplier is per-thread")
        void testPerThreadInitial() {
            def threadName = new AtomicReference<String>()
            def sl = ScopedLocal.withInitial { Thread.currentThread().name }

            def latch = new CountDownLatch(1)
            def thread = Thread.start("worker-initial") {
                threadName.set(sl.get())
                latch.countDown()
            }
            latch.await(5, TimeUnit.SECONDS)

            def mainValue = sl.get()
            assertTrue(mainValue.contains(Thread.currentThread().name) || mainValue != null)
            assertEquals("worker-initial", threadName.get())
        }
    }

    // ------------------------------------------------------------------
    //  Null bindings
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Null bindings")
    class NullBindings {

        @Test
        @DisplayName("where(null) makes get() return null")
        void testBindNull() {
            def sl = ScopedLocal.<String>newInstance()
            def result = ScopedLocal.where(sl, null).call { sl.get() }
            assertNull(result)
        }

        @Test
        @DisplayName("isBound() returns true for null binding")
        void testIsBoundNull() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, null).run {
                assertTrue(sl.isBound())
            }
        }

        @Test
        @DisplayName("orElse() returns null (not fallback) for null binding")
        void testOrElseNull() {
            def sl = ScopedLocal.<String>newInstance()
            def result = ScopedLocal.where(sl, null).call { sl.orElse("fallback") }
            assertNull(result)
        }

        @Test
        @DisplayName("where(null) with withInitial returns null, not initial value")
        void testNullOverridesInitial() {
            def sl = ScopedLocal.withInitial { "default" }
            def result = ScopedLocal.where(sl, null).call { sl.get() }
            assertNull(result, "null binding must override the initial value")
        }

        @Test
        @DisplayName("after where(null) exits, withInitial restores to initial value")
        void testNullScopeRestoresInitial() {
            def sl = ScopedLocal.withInitial { "default" }
            ScopedLocal.where(sl, null).run { /* no-op */ }
            assertEquals("default", sl.get())
        }
    }

    // ------------------------------------------------------------------
    //  Nested bindings
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Nested bindings")
    class NestedBindings {

        @Test
        @DisplayName("inner where() shadows outer where()")
        void testNestedShadowing() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, "outer").run {
                assertEquals("outer", sl.get())
                ScopedLocal.where(sl, "inner").run {
                    assertEquals("inner", sl.get())
                }
                assertEquals("outer", sl.get())
            }
        }

        @Test
        @DisplayName("three levels of nesting restore correctly")
        void testTripleNesting() {
            def sl = ScopedLocal.<Integer>newInstance()
            def trace = []
            ScopedLocal.where(sl, 1).run {
                trace << sl.get()
                ScopedLocal.where(sl, 2).run {
                    trace << sl.get()
                    ScopedLocal.where(sl, 3).run {
                        trace << sl.get()
                    }
                    trace << sl.get()
                }
                trace << sl.get()
            }
            assertEquals([1, 2, 3, 2, 1], trace)
        }

        @Test
        @DisplayName("null binding nested inside non-null binding")
        void testNullNestedInNonNull() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, "outer").run {
                assertEquals("outer", sl.get())
                ScopedLocal.where(sl, null).run {
                    assertNull(sl.get())
                }
                assertEquals("outer", sl.get())
            }
        }

        @Test
        @DisplayName("non-null binding nested inside null binding")
        void testNonNullNestedInNull() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, null).run {
                assertNull(sl.get())
                ScopedLocal.where(sl, "inner").run {
                    assertEquals("inner", sl.get())
                }
                assertNull(sl.get())
            }
        }
    }

    // ------------------------------------------------------------------
    //  Carrier API
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Carrier")
    class CarrierTests {

        @Test
        @DisplayName("Carrier.run() executes action with binding")
        void testCarrierRun() {
            def sl = ScopedLocal.<String>newInstance()
            def seen = new AtomicReference<String>()
            ScopedLocal.where(sl, "via-carrier").run {
                seen.set(sl.get())
            }
            assertEquals("via-carrier", seen.get())
        }

        @Test
        @DisplayName("Carrier.call() returns supplier result")
        void testCarrierCall() {
            def sl = ScopedLocal.<String>newInstance()
            def result = ScopedLocal.where(sl, "hello").call { sl.get().toUpperCase() }
            assertEquals("HELLO", result)
        }

        @Test
        @DisplayName("Carrier.call() can return null")
        void testCarrierCallReturnsNull() {
            def sl = ScopedLocal.<String>newInstance()
            def result = ScopedLocal.where(sl, "x").call { null }
            assertNull(result)
        }

        @Test
        @DisplayName("chained where() binds multiple ScopedLocals")
        void testCarrierChaining() {
            def sl1 = ScopedLocal.<String>newInstance()
            def sl2 = ScopedLocal.<Integer>newInstance()
            def result = ScopedLocal.where(sl1, "alpha")
                    .where(sl2, 42)
                    .call { sl1.get() + ":" + sl2.get() }
            assertEquals("alpha:42", result)
        }

        @Test
        @DisplayName("chained where() with same key uses last binding")
        void testCarrierChainSameKey() {
            def sl = ScopedLocal.<String>newInstance()
            def result = ScopedLocal.where(sl, "first")
                    .where(sl, "second")
                    .call { sl.get() }
            assertEquals("second", result)
        }

        @Test
        @DisplayName("chained bindings are all restored after scope exits")
        void testCarrierChainingRestoration() {
            def sl1 = ScopedLocal.<String>newInstance()
            def sl2 = ScopedLocal.<String>newInstance()

            ScopedLocal.where(sl1, "A").where(sl2, "B").run { /* no-op */ }

            assertFalse(sl1.isBound())
            assertFalse(sl2.isBound())
        }

        @Test
        @DisplayName("where(key, null) throws NullPointerException for null key")
        void testCarrierNullKey() {
            assertThrows(NullPointerException) {
                ScopedLocal.where(null, "value")
            }
        }

        @Test
        @DisplayName("Carrier.run(null) throws NullPointerException")
        void testCarrierRunNull() {
            def sl = ScopedLocal.<String>newInstance()
            assertThrows(NullPointerException) {
                ScopedLocal.where(sl, "x").run(null)
            }
        }

        @Test
        @DisplayName("Carrier.call(null) throws NullPointerException")
        void testCarrierCallNull() {
            def sl = ScopedLocal.<String>newInstance()
            assertThrows(NullPointerException) {
                ScopedLocal.where(sl, "x").call(null)
            }
        }
    }

    // ------------------------------------------------------------------
    //  Convenience instance methods
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Convenience instance methods")
    class ConvenienceMethods {

        @Test
        @DisplayName("instance where(value, Supplier) works")
        void testInstanceWhereSupplier() {
            def sl = ScopedLocal.<String>newInstance()
            def result = sl.where("val", { sl.get() } as Supplier)
            assertEquals("val", result)
        }

        @Test
        @DisplayName("instance where(value, Runnable) works")
        void testInstanceWhereRunnable() {
            def sl = ScopedLocal.<String>newInstance()
            def seen = new AtomicReference<String>()
            sl.where("val", { seen.set(sl.get()) } as Runnable)
            assertEquals("val", seen.get())
        }
    }

    // ------------------------------------------------------------------
    //  Exception propagation
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Exception propagation")
    class ExceptionPropagation {

        @Test
        @DisplayName("RuntimeException propagates transparently from run()")
        void testRuntimeExceptionRun() {
            def sl = ScopedLocal.<String>newInstance()
            def ex = assertThrows(IllegalStateException) {
                ScopedLocal.where(sl, "x").run {
                    throw new IllegalStateException("boom")
                }
            }
            assertEquals("boom", ex.message)
        }

        @Test
        @DisplayName("RuntimeException propagates transparently from call()")
        void testRuntimeExceptionCall() {
            def sl = ScopedLocal.<String>newInstance()
            def ex = assertThrows(IllegalArgumentException) {
                ScopedLocal.where(sl, "x").call {
                    throw new IllegalArgumentException("bad arg")
                }
            }
            assertEquals("bad arg", ex.message)
        }

        @Test
        @DisplayName("Error propagates transparently")
        void testErrorPropagation() {
            def sl = ScopedLocal.<String>newInstance()
            assertThrows(StackOverflowError) {
                ScopedLocal.where(sl, "x").run {
                    throw new StackOverflowError("overflow")
                }
            }
        }

        @Test
        @DisplayName("checked exception propagates via sneaky throw")
        void testCheckedExceptionSneakyThrow() {
            def sl = ScopedLocal.<String>newInstance()
            try {
                ScopedLocal.where(sl, "x").run {
                    throw new IOException("io error")
                }
                fail("should have thrown")
            } catch (IOException e) {
                assertEquals("io error", e.message)
            }
        }

        @Test
        @DisplayName("binding is restored after exception in run()")
        void testBindingRestoredAfterException() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, "outer").run {
                try {
                    ScopedLocal.where(sl, "inner").run {
                        throw new RuntimeException("fail")
                    }
                } catch (RuntimeException ignored) {}
                assertEquals("outer", sl.get(), "outer binding must be restored")
            }
        }

        @Test
        @DisplayName("binding is restored after exception in call()")
        void testBindingRestoredAfterExceptionCall() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, "outer").run {
                try {
                    ScopedLocal.where(sl, "inner").call {
                        throw new RuntimeException("fail")
                    }
                } catch (RuntimeException ignored) {}
                assertEquals("outer", sl.get())
            }
        }

        @Test
        @DisplayName("exception from chained carrier propagates and restores all bindings")
        void testChainedCarrierException() {
            def sl1 = ScopedLocal.<String>newInstance()
            def sl2 = ScopedLocal.<Integer>newInstance()

            assertThrows(RuntimeException) {
                ScopedLocal.where(sl1, "a").where(sl2, 1).run {
                    throw new RuntimeException("chained fail")
                }
            }

            assertFalse(sl1.isBound())
            assertFalse(sl2.isBound())
        }
    }

    // ------------------------------------------------------------------
    //  Thread isolation
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Thread isolation")
    class ThreadIsolation {

        @Test
        @DisplayName("bindings are thread-local, not shared")
        void testThreadIsolation() {
            def sl = ScopedLocal.<String>newInstance()
            def barrier = new CyclicBarrier(2)
            def otherSeen = new AtomicReference<Boolean>()

            ScopedLocal.where(sl, "main-value").run {
                def thread = Thread.start {
                    otherSeen.set(sl.isBound())
                    barrier.await(5, TimeUnit.SECONDS)
                }
                barrier.await(5, TimeUnit.SECONDS)
                thread.join(5000)
            }

            assertFalse(otherSeen.get(), "binding must not leak to another thread")
        }

        @Test
        @DisplayName("concurrent where() on same ScopedLocal from different threads")
        void testConcurrentBindings() {
            def sl = ScopedLocal.<Integer>newInstance()
            int threadCount = 8
            def barrier = new CyclicBarrier(threadCount)
            def results = new AtomicReference<List>(Collections.synchronizedList([]))
            def latch = new CountDownLatch(threadCount)

            (0..<threadCount).each { int i ->
                Thread.start {
                    ScopedLocal.where(sl, i).run {
                        barrier.await(5, TimeUnit.SECONDS)
                        // All threads read concurrently
                        results.get() << sl.get()
                        latch.countDown()
                    }
                }
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS))
            def sorted = results.get().sort()
            assertEquals((0..<threadCount).toList(), sorted,
                    "each thread must see its own bound value")
        }

        @Test
        @DisplayName("withInitial creates independent instances per thread")
        void testWithInitialPerThread() {
            def counter = new AtomicInteger(0)
            def sl = ScopedLocal.withInitial { counter.incrementAndGet() }

            int threadCount = 4
            def latch = new CountDownLatch(threadCount)
            def values = Collections.synchronizedList([])

            (0..<threadCount).each {
                Thread.start {
                    values << sl.get()
                    latch.countDown()
                }
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS))
            // Each thread should get a unique value from the counter
            assertEquals(threadCount, values.toSet().size(),
                    "each thread must get an independent initial value")
        }
    }

    // ------------------------------------------------------------------
    //  Edge cases
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("where() with same value as current is a no-op semantically")
        void testRebindSameValue() {
            def sl = ScopedLocal.<String>newInstance()
            ScopedLocal.where(sl, "same").run {
                def result = ScopedLocal.where(sl, "same").call { sl.get() }
                assertEquals("same", result)
            }
        }

        @Test
        @DisplayName("empty Runnable in run() does not throw")
        void testEmptyRunnable() {
            def sl = ScopedLocal.<String>newInstance()
            // Just run it — if it throws, the test fails
            ScopedLocal.where(sl, "x").run { /* empty */ }
        }

        @Test
        @DisplayName("Carrier is immutable — original not affected by chaining")
        void testCarrierImmutability() {
            def sl1 = ScopedLocal.<String>newInstance()
            def sl2 = ScopedLocal.<Integer>newInstance()

            def carrier1 = ScopedLocal.where(sl1, "a")
            def carrier2 = carrier1.where(sl2, 42)

            // carrier1 should only bind sl1
            carrier1.run {
                assertEquals("a", sl1.get())
                assertFalse(sl2.isBound())
            }

            // carrier2 should bind both
            carrier2.run {
                assertEquals("a", sl1.get())
                assertEquals(42, sl2.get())
            }
        }

        @Test
        @DisplayName("deeply nested binding and restoration")
        void testDeeplyNested() {
            def sl = ScopedLocal.<Integer>newInstance()
            int depth = 100
            Runnable innermost = {
                assertEquals(depth, sl.get())
            }

            Runnable current = innermost
            for (int i = depth; i >= 1; i--) {
                int val = i
                Runnable next = current
                current = {
                    ScopedLocal.where(sl, val).run {
                        assertEquals(val, sl.get())
                        next.run()
                        assertEquals(val, sl.get())
                    }
                }
            }
            current.run()
            assertFalse(sl.isBound())
        }

        @Test
        @DisplayName("withInitial supplier returning null is handled correctly")
        void testWithInitialReturningNull() {
            def sl = ScopedLocal.withInitial { null }
            assertNull(sl.get(), "initial supplier returning null should yield null")
            assertTrue(sl.isBound(), "should be considered bound with initial supplier")
        }

        @Test
        @DisplayName("call() result is returned even when value is bound to null")
        void testCallWithNullBinding() {
            def sl = ScopedLocal.<String>newInstance()
            def result = ScopedLocal.where(sl, null).call { "computed" }
            assertEquals("computed", result)
        }
    }
}
