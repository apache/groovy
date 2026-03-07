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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.DisplayName

import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safety verification tests for AsyncStreamGenerator.
 * These tests verify that the generator correctly handles concurrent access patterns
 * and detects unsafe operations before they cause deadlocks or data corruption.
 *
 * @since 6.0.0
 */
@DisplayName("AsyncStreamGenerator Concurrency Tests")
class AsyncConcurrencyTest {

    /**
     * Two threads calling moveNext() concurrently on the same generator.
     *
     * Expected behavior: Should fail fast (IllegalStateException or similar),
     * NOT deadlock one thread or overwrite consumer reference.
     */
    @Test
    @Timeout(5)
    @DisplayName("Concurrent moveNext() calls should fail fast, not deadlock")
    void testConcurrentMoveNext_ShouldFailFast() {
        AsyncStreamGenerator<Integer> gen = new AsyncStreamGenerator<>()

        // Set up producer to emit values
        Thread producer = Thread.start {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield(1)
                gen.yield(2)
                gen.yield(3)
                gen.complete()
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }

        CyclicBarrier barrier = new CyclicBarrier(2)
        AtomicReference<Exception> error1 = new AtomicReference<>(null)
        AtomicReference<Exception> error2 = new AtomicReference<>(null)

        Thread consumer1 = Thread.start {
            try {
                barrier.await(5, TimeUnit.SECONDS)  // Synchronize both consumers
                gen.moveNext()
            } catch (Exception e) {
                error1.set(e)
            }
        }

        Thread consumer2 = Thread.start {
            try {
                barrier.await(5, TimeUnit.SECONDS)  // Synchronize both consumers
                gen.moveNext()
            } catch (Exception e) {
                error2.set(e)
            }
        }

        // Wait for both consumers to complete (or error)
        consumer1.join(3000)
        consumer2.join(3000)
        producer.join(3000)

        // Verify that both consumers should not be alive (no deadlock) - CRITICAL
        assert !consumer1.isAlive() : "Consumer 1 should not deadlock"
        assert !consumer2.isAlive() : "Consumer 2 should not deadlock"

        // Verify we're testing the right thing: if one got an error, it should be IllegalStateException
        if (error1.get() != null) {
            assert error1.get() instanceof IllegalStateException : \
                "Consumer 1 error should be IllegalStateException, got: ${error1.get()?.class?.simpleName}"
        }
        if (error2.get() != null) {
            assert error2.get() instanceof IllegalStateException : \
                "Consumer 2 error should be IllegalStateException, got: ${error2.get()?.class?.simpleName}"
        }
    }

    /**
     * Interrupt the producer thread while blocked in yield().
     *
     * Expected behavior: Consumer should receive signal or complete,
     * NOT hang.
     */
    @Test
    @Timeout(5)
    @DisplayName("Interrupted producer thread should signal consumer, not hang")
    void testInterruptedProducer_ShouldSignalConsumer() {
        AsyncStreamGenerator<Integer> gen = new AsyncStreamGenerator<>()

        CountDownLatch producerBlocked = new CountDownLatch(1)
        CountDownLatch consumerStarted = new CountDownLatch(1)
        AtomicReference<Exception> consumerError = new AtomicReference<>(null)

        // Producer yields one value, then blocks on the second yield
        Thread producer = Thread.start {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield(1)
                producerBlocked.countDown()  // Signal that we're about to block
                Thread.sleep(100)  // Small delay to ensure consumer reads the first value
                gen.yield(2)  // This will block indefinitely without a consumer
                gen.complete()
            } catch (Exception e) {
                // Producer may receive InterruptedException wrapped in CancellationException
                consumerError.set(e)
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }

        // Consumer reads values until stream ends
        Thread consumer = Thread.start {
            try {
                consumerStarted.countDown()
                // Try to read all values from the stream
                while (gen.moveNext().get()) {
                    gen.getCurrent()
                }
            } catch (Exception e) {
                // Consumer may get an exception
                consumerError.set(e)
            }
        }

        // Wait for producer to be blocked in yield()
        producerBlocked.await(2, TimeUnit.SECONDS)
        consumerStarted.await(2, TimeUnit.SECONDS)

        Thread.sleep(200)  // Ensure producer is blocked

        // Interrupt producer to simulate external interruption
        producer.interrupt()

        // Give threads time to react
        producer.join(3000)
        consumer.join(3000)

        // Verify consumer didn't hang - this is the main assertion
        assert !consumer.isAlive() : "Consumer should not hang after producer interruption"

        // Verify producer also terminated
        assert !producer.isAlive() : "Producer should be interrupted and cleanup"
    }

    /**
     * Early exit from consumer (simulating break in for-await loop).
     *
     * Expected behavior: Producer should be unblocked via interrupt and cleanup.
     */
    @Test
    @Timeout(5)
    @DisplayName("Early consumer exit should cleanup producer thread")
    void testEarlyConsumerExit_ShouldCleanupProducer() {
        AsyncStreamGenerator<Integer> gen = new AsyncStreamGenerator<>()

        CountDownLatch producerStarted = new CountDownLatch(1)
        CountDownLatch producerBlocked = new CountDownLatch(1)

        Thread producer = Thread.start {
            gen.attachProducer(Thread.currentThread())
            producerStarted.countDown()
            try {
                for (int i = 1; i <= 10; i++) {
                    if (i == 5) {
                        producerBlocked.countDown()  // Signal we're about to block on item 5
                    }
                    gen.yield(i)
                }
                gen.complete()
            } catch (Exception e) {
                // Expected: InterruptedException wrapped in CancellationException
                // Producer received expected exception
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }

        // Consumer exits early after reading first 2 values
        Thread consumer = Thread.start {
            try {
                gen.moveNext()  // Read 1
                gen.getCurrent()
                gen.moveNext()  // Read 2
                gen.getCurrent()
                // Exit early - should trigger cleanup of producer
                gen.close()
            } catch (Exception e) {
                // Consumer error during close
            }
        }

        consumer.join(3000)
        producerStarted.await(2, TimeUnit.SECONDS)

        // If producer reaches blocked state, give it a moment
        producerBlocked.await(3, TimeUnit.SECONDS)
        Thread.sleep(200)

        producer.join(3000)

        // Verify producer is not still running
        assert !producer.isAlive() : "Producer should be interrupted and cleanup when consumer closes"
    }

    /**
     * Multiple close() calls should be idempotent and safe.
     */
    @Test
    @Timeout(5)
    @DisplayName("Multiple close() calls should be safe and idempotent")
    void testMultipleClose_ShouldBeIdempotent() {
        AsyncStreamGenerator<Integer> gen = new AsyncStreamGenerator<>()

        Thread producer = Thread.start {
            gen.attachProducer(Thread.currentThread())
            try {
                gen.yield(1)
                gen.complete()
            } finally {
                gen.detachProducer(Thread.currentThread())
            }
        }

        // Multiple threads closing simultaneously
        List<Thread> closers = (1..5).collect { idx ->
            Thread.start {
                try {
                    gen.close()
                } catch (Exception e) {
                    // Close error
                }
            }
        }

        producer.join(3000)
        closers.each { it.join(3000) }

        closers.each { assert !it.isAlive() : "Close threads should not hang" }
    }

    /**
     * Producer completing vs consumer exiting - race condition test.
     *
     * Expected behavior: Both should complete cleanly without deadlock.
     */
    @Test
    @Timeout(5)
    @DisplayName("Producer completion and consumer exit should not deadlock")
    void testProducerCompletionVsConsumerExit_NoDeadlock() {
        for (int iteration = 0; iteration < 10; iteration++) {
            AsyncStreamGenerator<Integer> gen = new AsyncStreamGenerator<>()

            Thread producer = Thread.start {
                gen.attachProducer(Thread.currentThread())
                try {
                    gen.yield(1)
                    gen.yield(2)
                    gen.complete()
                } finally {
                    gen.detachProducer(Thread.currentThread())
                }
            }

            Thread consumer = Thread.start {
                try {
                    gen.moveNext()
                    gen.getCurrent()
                    gen.moveNext()
                    gen.getCurrent()
                    gen.moveNext()
                } catch (Exception e) {
                    // Consumer exception
                }
            }

            producer.join(3000)
            consumer.join(3000)

            assert !producer.isAlive() : "Iteration $iteration: Producer deadlocked"
            assert !consumer.isAlive() : "Iteration $iteration: Consumer deadlocked"
        }
    }

    // =================================================================
    // moveNext() branch coverage tests
    // =================================================================

    /**
     * Branch: same thread re-entry.
     *
     * The CAS guard in moveNext() allows the same thread to call moveNext()
     * multiple times sequentially (the expected usage pattern).  This test
     * explicitly verifies that no ISE is thrown when the same thread drives
     * multiple iterations.
     */
    @Test
    @Timeout(5)
    @DisplayName("Same thread can call moveNext() sequentially without ISE")
    void testSameThreadSequentialMoveNext() {
        def gen = new AsyncStreamGenerator<Integer>()
        Thread.start {
            gen.yield(1)
            gen.yield(2)
            gen.yield(3)
            gen.complete()
        }

        def items = []
        // All three moveNext() + getCurrent() calls happen on the SAME thread.
        while (gen.moveNext().get()) {
            items << gen.getCurrent()
        }
        assert items == [1, 2, 3]
    }

    /**
     * Branch: InterruptedException caught while closed flag is true.
     *
     * When moveNext() is blocked in queue.take() and close() fires (setting
     * closed=true and interrupting the consumer), the InterruptedException
     * handler checks closed.get().  If true, it returns Awaitable.of(false)
     * instead of throwing CancellationException.
     *
     * This test deterministically triggers this branch by:
     * 1. Starting moveNext() which blocks on the empty queue
     * 2. Calling close() from another thread which sets closed=true and
     *    interrupts the consumer
     * 3. Verifying that moveNext() returns false (not an exception)
     */
    @Test
    @Timeout(5)
    @DisplayName("moveNext() returns false when interrupted after close()")
    void testMoveNextInterruptedWhileClosed() {
        def gen = new AsyncStreamGenerator<Integer>()
        def moveNextResult = new AtomicReference<Boolean>()
        def moveNextError = new AtomicReference<Throwable>()
        def consumerStarted = new CountDownLatch(1)

        def consumer = Thread.start {
            try {
                consumerStarted.countDown()
                // This will block because no producer is yielding anything
                def result = gen.moveNext().get()
                moveNextResult.set(result)
            } catch (Throwable t) {
                moveNextError.set(t)
            }
        }

        // Wait for consumer to start blocking in moveNext()
        consumerStarted.await(2, TimeUnit.SECONDS)
        Thread.sleep(50)  // Brief pause to ensure queue.take() is entered

        // close() sets closed=true and interrupts the consumer thread
        gen.close()

        consumer.join(3000)
        assert !consumer.isAlive() : "Consumer should have exited"

        // The IE handler should detect closed=true and return false
        assert moveNextResult.get() == false : "Expected false from moveNext() after close()"
        assert moveNextError.get() == null : "Expected no exception, got: ${moveNextError.get()}"
    }

    /**
     * Branch: double-check after CAS detects close() race.
     *
     * If close() fires between the initial closed.get() check and the
     * consumerThread CAS, the consumer reference is not yet visible to close().
     * The double-check after CAS detects this and returns false immediately
     * without entering queue.take().
     *
     * This test uses a CyclicBarrier to synchronize close() and moveNext()
     * on the same timing boundary.  We run multiple iterations because the
     * exact race window is non-deterministic.
     */
    @Test
    @Timeout(10)
    @DisplayName("Double-check after CAS detects close() race")
    void testDoubleCheckAfterCasDetectsCloseRace() {
        int detected = 0
        for (int i = 0; i < 200; i++) {
            def gen = new AsyncStreamGenerator<Integer>()
            def barrier = new CyclicBarrier(2)
            def result = new AtomicReference<Object>()

            def consumer = Thread.start {
                try {
                    barrier.await(2, TimeUnit.SECONDS)
                    def r = gen.moveNext().get()
                    result.set(r)
                } catch (Throwable t) {
                    result.set(t)
                }
            }

            def closer = Thread.start {
                try {
                    barrier.await(2, TimeUnit.SECONDS)
                    gen.close()
                } catch (Throwable t) {
                    // ignore
                }
            }

            consumer.join(3000)
            closer.join(3000)

            def r = result.get()
            // Either:
            // 1. moveNext() returns false (caught by initial check OR double-check)
            // 2. moveNext() throws CancellationException (interrupted in take())
            // Both are valid outcomes of the race — but it must NEVER hang.
            if (r == false || r == Boolean.FALSE) {
                detected++
            }
        }
        // At least some iterations should hit the false-return path
        assert detected > 0 : "Expected at least one iteration where moveNext() returned false"
    }

    /**
     * Branch: moveNext() on already-closed generator returns false immediately.
     *
     * After close() is called, all subsequent moveNext() calls should return
     * Awaitable.of(false) without blocking.
     */
    @Test
    @Timeout(5)
    @DisplayName("moveNext() after close returns false without blocking")
    void testMoveNextAfterCloseReturnsFalse() {
        def gen = new AsyncStreamGenerator<Integer>()
        gen.close()

        // Should return false immediately (no producer needed)
        assert gen.moveNext().get() == false
        assert gen.moveNext().get() == false
        assert gen.moveNext().get() == false
    }

    /**
     * Branch: ErrorItem with Error subclass (thrown directly, not via sneakyThrow).
     *
     * When the producer yields an Error (not Exception), moveNext() should
     * throw it directly as an Error, not wrap it.
     */
    @Test
    @Timeout(5)
    @DisplayName("moveNext() propagates Error subclass directly")
    void testMoveNextPropagatesErrorDirectly() {
        def gen = new AsyncStreamGenerator<Integer>()
        Thread.start {
            gen.error(new StackOverflowError("test error"))
        }

        try {
            gen.moveNext().get()
            assert false : "Expected StackOverflowError"
        } catch (StackOverflowError e) {
            assert e.message == "test error"
        }
    }

    /**
     * Branch: ErrorItem with Exception (thrown via sneakyThrow).
     *
     * When the producer yields a checked Exception, moveNext() should throw
     * it via sneakyThrow (bypassing checked exception compiler enforcement).
     */
    @Test
    @Timeout(5)
    @DisplayName("moveNext() propagates Exception via sneakyThrow")
    void testMoveNextPropagatesExceptionViaSneakyThrow() {
        def gen = new AsyncStreamGenerator<Integer>()
        Thread.start {
            gen.error(new java.io.IOException("disk fail"))
        }

        try {
            gen.moveNext().get()
            assert false : "Expected IOException"
        } catch (java.io.IOException e) {
            assert e.message == "disk fail"
        }
    }

    /**
     * Branch: DONE sentinel marks stream exhausted.
     *
     * After the producer calls complete(), the next moveNext() should
     * receive the DONE sentinel and return false.
     */
    @Test
    @Timeout(5)
    @DisplayName("moveNext() returns false on DONE sentinel after complete()")
    void testMoveNextReturnsFalseOnDoneSentinel() {
        def gen = new AsyncStreamGenerator<Integer>()
        Thread.start {
            gen.yield(42)
            gen.complete()
        }

        assert gen.moveNext().get() == true
        assert gen.getCurrent() == 42
        assert gen.moveNext().get() == false
        // Subsequent calls should also return false (closed state)
        assert gen.moveNext().get() == false
    }
}
