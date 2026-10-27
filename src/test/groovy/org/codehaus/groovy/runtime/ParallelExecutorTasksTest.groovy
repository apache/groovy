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

import groovy.concurrent.Pool
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.lang.reflect.UndeclaredThrowableException
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Predicate

import static groovy.test.GroovyAssert.shouldFail

/**
 * Direct coverage of the non-fork-join parallel-collection path.
 * Public behaviour is also exercised from {@code ParallelCollectionTest}.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
final class ParallelExecutorTasksTest {

    private Pool pool

    @BeforeEach
    void openPool() {
        pool = Pool.virtual()
    }

    @AfterEach
    void closePool() {
        pool?.close()
    }

    @Test
    void testIterationPreservesIndexes() {
        def slots = new String[3]
        def seen = ConcurrentHashMap.newKeySet()
        ParallelExecutorTasks.forEach(pool, ['a', 'b', 'c']) { value ->
            seen.add(value)
        }
        ParallelExecutorTasks.forEachIndexed(pool, ['a', 'b', 'c']) { value, index ->
            slots[index] = "$index:$value"
        }
        assert seen == ['a', 'b', 'c'] as Set
        assert slots.toList() == ['0:a', '1:b', '2:c']

        def linked = new LinkedList([1, 2, 3])
        def indexed = new Integer[3]
        ParallelExecutorTasks.forEachIndexed(pool, linked) { value, index ->
            indexed[index] = value
        }
        assert indexed.toList() == [1, 2, 3]
    }

    @Test
    void testMapFilterCountAndFlatMapPreserveOrder() {
        assert ParallelExecutorTasks.map(pool, ['a', 'b', 'c']) { it == 'b' ? null : it.toUpperCase() } == ['A', null, 'C']
        assert ParallelExecutorTasks.map(pool, []) { it } == []
        assert ParallelExecutorTasks.filter(pool, [1, 2, 3, 4]) { it % 2 == 0 } == [2, 4]
        assert ParallelExecutorTasks.filter(pool, []) { true } == []
        assert ParallelExecutorTasks.count(pool, [1, 2, 3, 4]) { it % 2 == 0 } == 2L
        assert ParallelExecutorTasks.count(pool, []) { true } == 0L
        assert ParallelExecutorTasks.flatMap(pool, [1, 2, 3]) { [it, it * 10] } == [1, 10, 2, 20, 3, 30]

        def thrown = shouldFail(NullPointerException) {
            ParallelExecutorTasks.flatMap(pool, [1, 2]) { it == 1 ? [it] : null }
        }
        assert thrown.message == 'transform returned null'

        def set = [1, 2, 3] as HashSet
        assert ParallelExecutorTasks.map(pool, set) { it * 2 }.toSet() == [2, 4, 6] as Set
    }

    @Test
    void testEmptyShortCircuits() {
        def ran = false
        ParallelExecutorTasks.forEach(pool, []) { ran = true }
        ParallelExecutorTasks.forEachIndexed(pool, []) { value, index -> ran = true }
        assert !ran
        assert !ParallelExecutorTasks.anyMatch(pool, []) { true }
        assert ParallelExecutorTasks.allMatch(pool, []) { false }
        assert ParallelExecutorTasks.findFirst(pool, []) { true } == null
        assert ParallelExecutorTasks.findAny(pool, []) { true } == null
    }

    @Test
    void testGroupByKeepsEncounterOrderAndNullKeys() {
        def grouped = ParallelExecutorTasks.groupBy(pool, [1, 2, 3, 4]) { it % 2 == 0 ? 'e' : null }
        assert grouped[null] == [1, 3]
        assert grouped.e == [2, 4]
        assert ParallelExecutorTasks.groupBy(pool, []) { it }.isEmpty()
    }

    @Test
    void testPartitionKeepsEncounterOrder() {
        def parts = ParallelExecutorTasks.partition(pool, [1, 2, 3, 4, 5]) { it % 2 == 0 }
        assert parts[0] == [2, 4]
        assert parts[1] == [1, 3, 5]
        def empty = ParallelExecutorTasks.partition(pool, []) { true }
        assert empty[0].isEmpty()
        assert empty[1].isEmpty()
    }

    @Test
    void testMinAndMax() {
        def unused = { a, b -> throw new AssertionError('comparator should not be called') }
        assert ParallelExecutorTasks.min(pool, [], unused) == null
        assert ParallelExecutorTasks.max(pool, [], unused) == null
        assert ParallelExecutorTasks.min(pool, ['only'], unused) == 'only'
        assert ParallelExecutorTasks.max(pool, ['only'], unused) == 'only'

        int n = Runtime.runtime.availableProcessors() * 3
        def descending = (0..<n).collect { n - it }
        assert ParallelExecutorTasks.min(pool, descending) { a, b -> a <=> b } == 1
        assert ParallelExecutorTasks.max(pool, descending) { a, b -> a <=> b } == n

        def ties = (0..<n).collect { new String('same') }
        assert ParallelExecutorTasks.min(pool, ties) { a, b -> a <=> b }.is(ties[0])
        assert ParallelExecutorTasks.max(pool, ties) { a, b -> a <=> b }.is(ties[0])
    }

    @Test
    void testFoldTreatsAnUnseenSideAsIdentity() {
        def unused = { a, b -> throw new AssertionError('identity must not call the operator') }
        def left = new ParallelCollectionExtensions.Fold<String>()
        left.absorb('a', unused)
        def emptyRight = new ParallelCollectionExtensions.Fold<String>()
        assert left.combine(emptyRight, unused).finish() == 'a'

        def right = new ParallelCollectionExtensions.Fold<String>()
        right.absorb('b', unused)
        def emptyLeft = new ParallelCollectionExtensions.Fold<String>()
        assert emptyLeft.combine(right, unused).finish() == 'b'

        def calls = new AtomicInteger()
        def first = new ParallelCollectionExtensions.Fold<String>()
        first.absorb('a', unused)
        def second = new ParallelCollectionExtensions.Fold<String>()
        second.absorb('b', unused)
        assert first.combine(second) { a, b ->
            calls.incrementAndGet()
            a + b
        }.finish() == 'ab'
        assert calls.get() == 1

        assert new ParallelCollectionExtensions.Fold<String>().finish() == null
    }

    @Test
    void testReduceAndInject() {
        def calls = new AtomicInteger()
        def unused = { a, b ->
            calls.incrementAndGet()
            throw new AssertionError('operator should not be called')
        }
        assert ParallelExecutorTasks.reduce(pool, [], unused) == null
        assert ParallelExecutorTasks.reduce(pool, ['only'], unused) == 'only'
        assert calls.get() == 0

        def pairCalls = new AtomicInteger()
        assert ParallelExecutorTasks.reduce(pool, ['a', 'b']) { a, b ->
            pairCalls.incrementAndGet()
            a + b
        } == 'ab'
        assert pairCalls.get() == 1

        int n = Runtime.runtime.availableProcessors() * 3
        def parts = (0..<n).collect { "v$it|" }
        def reductions = new AtomicInteger()
        assert ParallelExecutorTasks.reduce(pool, parts) { a, b ->
            reductions.incrementAndGet()
            a + b
        } == parts.join('')
        assert reductions.get() == n - 1

        def injectCalls = new AtomicInteger()
        assert ParallelExecutorTasks.inject(pool, [], 7) { a, b ->
            injectCalls.incrementAndGet()
            a + b
        } == 7
        assert injectCalls.get() == 0
        assert ParallelExecutorTasks.inject(pool, [4], 7) { a, b -> a + b } == 11
        assert ParallelExecutorTasks.inject(pool, (1..10).toList(), 0) { a, b -> a + b } == 55
    }

    @Test
    void testFindReturnsEncounterOrderOrAnyMatch() {
        assert ParallelExecutorTasks.findFirst(pool, [1, 2, 3, 4]) { it > 2 } == 3
        assert ParallelExecutorTasks.findFirst(pool, [1, 2, 3]) { it > 10 } == null
        def any = ParallelExecutorTasks.findAny(pool, [1, 2, 3, 4]) { it % 2 == 0 }
        assert any == 2 || any == 4
        assert ParallelExecutorTasks.findAny(pool, [1, 3]) { it % 2 == 0 } == null
        assert ParallelExecutorTasks.anyMatch(pool, [1, 2, 3]) { it == 2 }
        assert !ParallelExecutorTasks.anyMatch(pool, [1, 3]) { it % 2 == 0 }
        assert ParallelExecutorTasks.allMatch(pool, [2, 4, 6]) { it % 2 == 0 }
        assert !ParallelExecutorTasks.allMatch(pool, [2, 3, 4]) { it % 2 == 0 }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testFindFirstKeepsTheEarlierMatch() {
        def started = new CountDownLatch(2)
        def laterMatched = new CountDownLatch(1)
        def result = ParallelExecutorTasks.findFirst(pool, ['early', 'late']) { item ->
            started.countDown()
            assert started.await(5, TimeUnit.SECONDS)
            if (item == 'late') {
                laterMatched.countDown()
                return true
            }
            assert laterMatched.await(5, TimeUnit.SECONDS)
            return true
        }
        assert result == 'early'
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testFindFirstPrefixErrorFailsAndTailErrorDoesNot() {
        def prefix = shouldFail(IllegalStateException) {
            ParallelExecutorTasks.findFirst(pool, ['boom', 'yes']) { item ->
                if (item == 'boom') {
                    throw new IllegalStateException('boom')
                }
                return true
            }
        }
        assert prefix.message == 'boom'

        // The tail throws only after it has started, and the head waits until
        // that task has finished recording the error. The earlier match still wins.
        // A cached fallback pool keeps the worker after the task returns, so
        // completion is observed from the executor. Virtual threads still end
        // with the task, and that death is checked after the task has finished.
        def tailInside = new CountDownLatch(1)
        def releaseTail = new CountDownLatch(1)
        def tailThread = new AtomicReference<Thread>()
        def tailLeftCallback = new AtomicBoolean()
        def tailRecorded = new CountDownLatch(1)
        def observed = executorPool(new Executor() {
            @Override
            void execute(Runnable command) {
                pool.execute {
                    try {
                        command.run()
                    } finally {
                        if (tailLeftCallback.get()) {
                            tailRecorded.countDown()
                        }
                    }
                }
            }
        })
        def result = ParallelExecutorTasks.findFirst(observed, ['yes', 'boom']) { item ->
            if (item == 'boom') {
                tailThread.set(Thread.currentThread())
                try {
                    tailInside.countDown()
                    assert releaseTail.await(5, TimeUnit.SECONDS)
                    throw new IllegalStateException('boom')
                } finally {
                    tailLeftCallback.set(true)
                }
            }
            assert tailInside.await(5, TimeUnit.SECONDS)
            releaseTail.countDown()
            assert tailRecorded.await(5, TimeUnit.SECONDS)
            if (pool.usesVirtualThreads()) {
                tailThread.get().join(5_000)
                assert !tailThread.get().isAlive()
            }
            return true
        }
        assert result == 'yes'
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testAnyMatchInterruptsTheOtherTask() {
        assert runShortCircuit(0, true) { Predicate predicate ->
            ParallelExecutorTasks.anyMatch(pool, [0, 1], predicate)
        } == true
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testEveryMatchInterruptsTheOtherTask() {
        assert runShortCircuit(0, false) { Predicate predicate ->
            ParallelExecutorTasks.allMatch(pool, [0, 1], predicate)
        } == false
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testFindAnyInterruptsTheOtherTask() {
        assert runShortCircuit(0, true) { Predicate predicate ->
            ParallelExecutorTasks.findAny(pool, [0, 1], predicate)
        } == 1
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testFindFirstInterruptsALaterTask() {
        assert runShortCircuit(1, true) { Predicate predicate ->
            ParallelExecutorTasks.findFirst(pool, [0, 1], predicate)
        } == 0
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testAwaitRestoresTheInterruptFlag() {
        def started = new CountDownLatch(1)
        def done = new CountDownLatch(1)
        def failure = new AtomicReference<Throwable>()
        def flag = new AtomicBoolean()
        def caller = new Thread({
            try {
                ParallelExecutorTasks.forEach(pool, [1]) {
                    started.countDown()
                    Thread.sleep(30_000)
                }
            } catch (Throwable ex) {
                failure.set(ex)
                flag.set(Thread.interrupted())
            } finally {
                done.countDown()
            }
        })
        caller.start()
        assert started.await(5, TimeUnit.SECONDS)
        caller.interrupt()
        assert done.await(5, TimeUnit.SECONDS)
        assert failure.get() instanceof InterruptedException
        assert flag.get()
    }

    @Test
    void testFailedSynchronousTaskPrefersTheCallerInterrupt() {
        def sync = synchronousPool()
        def thrown = shouldFail(InterruptedException) {
            ParallelExecutorTasks.forEach(sync, [1]) {
                Thread.currentThread().interrupt()
                throw new IllegalStateException('boom')
            }
        }
        assert thrown instanceof InterruptedException
        assert Thread.interrupted()
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRejectionSettlesUnsubmittedIndexes() {
        def rejecting = rejectAfter(1)
        def thrown = shouldFail(RejectedExecutionException) {
            ParallelExecutorTasks.forEach(rejecting, [1, 2, 3]) { }
        }
        assert thrown.message == 'saturated'
    }

    @Test
    void testSynchronousExecutorKeepsACallerInterrupt() {
        def sync = synchronousPool()
        Thread.currentThread().interrupt()
        try {
            def seen = []
            ParallelExecutorTasks.forEach(sync, [1, 2]) { seen << it }
            assert seen == [1, 2]
            assert Thread.currentThread().isInterrupted()
            // The later indexes are abandoned on this same thread.
            assert ParallelExecutorTasks.anyMatch(sync, [1, 2, 3]) { it == 1 }
            assert Thread.currentThread().isInterrupted()
        } finally {
            Thread.interrupted()
        }
    }

    /**
     * The worker takes the next task from a plain list. It does not call
     * {@code Thread.interrupted} or block on an interruptible queue between
     * the two tasks, so the second callback sees a clear flag only because
     * the task entry clears a cancellation left by the previous task.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testReusedWorkerDropsAStaleInterrupt() {
        def queue = new LinkedList<Runnable>()
        def done = new AtomicBoolean()
        def failure = new AtomicReference<Throwable>()
        def worker = new Thread({
            try {
                while (true) {
                    Runnable task
                    synchronized (queue) {
                        while (queue.isEmpty() && !done.get()) {
                            queue.wait()
                        }
                        if (queue.isEmpty()) {
                            return
                        }
                        task = queue.removeFirst()
                    }
                    task.run()
                }
            } catch (Throwable thrown) {
                failure.set(thrown)
            }
        })
        worker.daemon = true
        worker.start()
        def submitted = new AtomicInteger()
        def handoff = new Executor() {
            @Override
            void execute(Runnable command) {
                synchronized (queue) {
                    queue.addLast(command)
                    submitted.incrementAndGet()
                    queue.notifyAll()
                }
            }
        }
        def bounded = sizedPool(handoff, 1)
        try {
            def first = false
            def second = true
            def sameThread = false
            def workerThread = new AtomicReference<Thread>()
            ParallelExecutorTasks.forEach(bounded, [1, 2]) { value ->
                if (value == 1) {
                    while (submitted.get() < 2) {
                        Thread.onSpinWait()
                    }
                    Thread.currentThread().interrupt()
                    first = Thread.currentThread().isInterrupted()
                    workerThread.set(Thread.currentThread())
                } else {
                    second = Thread.currentThread().isInterrupted()
                    sameThread = Thread.currentThread().is(workerThread.get())
                }
            }
            assert first
            assert !second
            assert sameThread
            assert failure.get() == null
        } finally {
            done.set(true)
            synchronized (queue) {
                queue.notifyAll()
            }
            worker.join(5_000)
        }
        assert !worker.isAlive()
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testNestedWorkOnABoundedExecutorRunsOnTheWorker() {
        def threads = daemonPool(1)
        def bounded = sizedPool(threads, 1)
        try {
            def nested = []
            def bound = []
            ParallelExecutorTasks.forEach(bounded, [1]) {
                bound << Pool.current()
                ParallelExecutorTasks.forEach(bounded, [2, 3]) {
                    bound << Pool.current()
                    nested << it
                }
            }
            assert nested == [2, 3]
            assert bound.every { it.is(bounded) }

            def ran = []
            ParallelExecutorTasks.forEach(bounded, [1]) {
                assert ParallelExecutorTasks.anyMatch(bounded, [4, 5, 6]) {
                    ran << it
                    it == 4
                }
            }
            assert ran == [4]

            def thrown = shouldFail(AssertionError) {
                ParallelExecutorTasks.forEach(bounded, [1]) {
                    ParallelExecutorTasks.forEach(bounded, [2, 3]) { value ->
                        if (value == 2) {
                            throw new AssertionError('nested')
                        }
                    }
                }
            }
            assert thrown.message == 'nested'
        } finally {
            threads.shutdownNow()
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testNestedVirtualWorkStaysOnThatPool() {
        def outer = new AtomicReference<Thread>()
        def inners = ConcurrentHashMap.newKeySet()
        ParallelExecutorTasks.forEach(pool, [1]) {
            outer.set(Thread.currentThread())
            ParallelExecutorTasks.forEach(pool, [2, 3, 4]) {
                inners.add(Thread.currentThread())
            }
        }
        assert !inners.isEmpty()
        assert inners.any { !it.is(outer.get()) }

        def other = Pool.virtual()
        try {
            def seen = new AtomicReference<Pool>()
            ParallelExecutorTasks.forEach(pool, [1]) {
                ParallelExecutorTasks.forEach(other, [2]) {
                    seen.set(Pool.current())
                }
            }
            assert seen.get().is(other)
        } finally {
            other.close()
        }
    }

    @Test
    void testSynchronousExecutorDoesNotDeadlock() {
        def sync = synchronousPool()
        def seen = []
        ParallelExecutorTasks.forEach(sync, [1, 2, 3]) { seen << it }
        assert seen == [1, 2, 3]

        def nested = []
        def bound = []
        ParallelExecutorTasks.forEach(sync, [1]) {
            bound << Pool.current()
            ParallelExecutorTasks.forEach(sync, [2, 3]) {
                bound << Pool.current()
                nested << it
            }
        }
        assert nested == [2, 3]
        assert bound.every { it.is(sync) }
    }

    @Test
    void testShortCircuitReportsACallbackException() {
        def any = shouldFail(IllegalStateException) {
            ParallelExecutorTasks.anyMatch(pool, [1, 2]) { throw new IllegalStateException('any') }
        }
        assert any.message == 'any'

        def every = shouldFail(IllegalStateException) {
            ParallelExecutorTasks.allMatch(pool, [1, 2]) { throw new IllegalStateException('every') }
        }
        assert every.message == 'every'

        def found = shouldFail(IllegalStateException) {
            ParallelExecutorTasks.findAny(pool, [1, 2]) { throw new IllegalStateException('found') }
        }
        assert found.message == 'found'

        def prefix = shouldFail(IllegalStateException) {
            ParallelExecutorTasks.findFirst(pool, ['boom', 'no']) { item ->
                if (item == 'boom') {
                    throw new IllegalStateException('boom')
                }
                return false
            }
        }
        assert prefix.message == 'boom'
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testOnlyTheFirstShortCircuitWins() {
        def findStarted = new CountDownLatch(2)
        def result = ParallelExecutorTasks.findFirst(pool, ['a', 'b']) { item ->
            awaitPeers(findStarted)
            return true
        }
        assert result == 'a'

        def anyStarted = new CountDownLatch(2)
        assert ParallelExecutorTasks.anyMatch(pool, [0, 1]) { index ->
            awaitPeers(anyStarted)
            return true
        }
        def everyStarted = new CountDownLatch(2)
        assert !ParallelExecutorTasks.allMatch(pool, [0, 1]) { index ->
            awaitPeers(everyStarted)
            return false
        }
        def foundStarted = new CountDownLatch(2)
        def any = ParallelExecutorTasks.findAny(pool, [0, 1]) { index ->
            awaitPeers(foundStarted)
            return true
        }
        assert any == 0 || any == 1
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testFindFirstDropsALaterMatchAfterAnEarlierOneIsPublished() {
        def lateInside = new CountDownLatch(1)
        def releaseLate = new CountDownLatch(1)
        def result = ParallelExecutorTasks.findFirst(pool, ['early', 'late']) { item ->
            if (item == 'late') {
                lateInside.countDown()
                try {
                    assert releaseLate.await(5, TimeUnit.SECONDS)
                    Thread.sleep(30_000)
                } catch (InterruptedException ignored) {
                    Thread.interrupted()
                }
                return true
            }
            assert lateInside.await(5, TimeUnit.SECONDS)
            releaseLate.countDown()
            return true
        }
        assert result == 'early'
    }

    @Test
    void testCallbackExceptionsPropagate() {
        def illegal = shouldFail(IllegalStateException) {
            ParallelExecutorTasks.map(pool, [1, 2]) { throw new IllegalStateException('nope') }
        }
        assert illegal.message == 'nope'

        def assertion = shouldFail(AssertionError) {
            ParallelExecutorTasks.filter(pool, [1]) { throw new AssertionError('bad') }
        }
        assert assertion.message == 'bad'

        def wrapped = shouldFail(IllegalStateException) {
            ParallelExecutorTasks.count(pool, [1]) {
                throw new CompletionException(new CompletionException(new IllegalStateException('deep')))
            }
        }
        assert wrapped.message == 'deep'

        def bare = shouldFail(CompletionException) {
            ParallelExecutorTasks.forEach(pool, [1]) {
                throw new CompletionException((Throwable) null)
            }
        }
        assert bare.cause == null

        def hidden = shouldFail(UndeclaredThrowableException) {
            ParallelExecutorTasks.forEach(pool, [1]) {
                throw new UndeclaredThrowableException(new IllegalStateException('hidden'))
            }
        }
        assert hidden.cause instanceof IllegalStateException
        assert hidden.cause.message == 'hidden'
    }

    /** Every peer counts the shared latch down before any of them continues. */
    private static void awaitPeers(CountDownLatch started) {
        started.countDown()
        assert started.await(5, TimeUnit.SECONDS)
    }

    /**
     * Index {@code slowIndex} sleeps until it is interrupted, then throws.
     * The other index waits until that sleep has started and returns {@code fastResult}.
     * The thrown exception must not replace the short-circuit result.
     */
    private Object runShortCircuit(int slowIndex, boolean fastResult, Closure operation) {
        def started = new CountDownLatch(2)
        def asleep = new CountDownLatch(1)
        def stopped = new CountDownLatch(1)
        def sawInterrupt = new AtomicBoolean()
        Predicate predicate = { Integer index ->
            started.countDown()
            assert started.await(5, TimeUnit.SECONDS)
            if (index == slowIndex) {
                try {
                    asleep.countDown()
                    Thread.sleep(30_000)
                    return !fastResult
                } catch (InterruptedException ex) {
                    sawInterrupt.set(true)
                    stopped.countDown()
                    throw new IllegalStateException('abandoned')
                }
            }
            assert asleep.await(5, TimeUnit.SECONDS)
            return fastResult
        } as Predicate
        def result = operation.call(predicate)
        assert stopped.await(5, TimeUnit.SECONDS)
        assert sawInterrupt.get()
        return result
    }

    private static Pool synchronousPool() {
        executorPool(new Executor() {
            @Override
            void execute(Runnable command) {
                command.run()
            }
        })
    }

    private static Pool rejectAfter(int accept) {
        def seen = new AtomicInteger()
        executorPool(new Executor() {
            @Override
            void execute(Runnable command) {
                if (seen.incrementAndGet() > accept) {
                    throw new RejectedExecutionException('saturated')
                }
                command.run()
            }
        })
    }

    private static ExecutorService daemonPool(int size) {
        Executors.newFixedThreadPool(size) { Runnable runnable ->
            def thread = new Thread(runnable)
            thread.daemon = true
            thread
        }
    }

    private static Pool executorPool(Executor executor) {
        sizedPool(executor, Integer.MAX_VALUE)
    }

    private static Pool sizedPool(Executor executor, int size) {
        new Pool() {
            @Override
            void execute(Runnable command) {
                executor.execute(command)
            }

            @Override
            int getPoolSize() {
                size
            }

            @Override
            int getActiveCount() {
                0
            }

            @Override
            boolean usesVirtualThreads() {
                false
            }

            @Override
            ForkJoinPool asForkJoinPool() {
                throw new UnsupportedOperationException('not a fork-join pool')
            }

            @Override
            void shutdown() { }
        }
    }
}
