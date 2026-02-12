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
package groovy.util.concurrent.async

import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

@CompileStatic
class SimplePromiseTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTest {
        @Test
        @DisplayName("should create promise from CompletableFuture")
        void testOfWithCompletableFuture() {
            Promise<String> promise = SimplePromise.of(() -> "test")

            assertNotNull(promise)
            assertEquals("test", promise.await())
        }

        @Test
        @DisplayName("should create empty promise")
        void testOfEmpty() {
            Promise<String> promise = SimplePromise.of()

            assertNotNull(promise)
            assertFalse(promise.isDone())
        }
    }

    @Nested
    @DisplayName("CompletedPromise")
    class SimplePromiseCompletedPromiseTest {
        @Test
        void testCompletedWithValue() throws Exception {
            Promise<Integer> p = SimplePromise.completed(42)

            assertTrue(p.isDone(), "promise should be done")
            assertFalse(p.isCancelled(), "should not be cancelled")
            assertFalse(p.isCompletedExceptionally(), "should not be completed exceptionally")

            // blocking and non-blocking retrievals
            assertEquals(42, p.await())
            assertEquals(42, p.await())
            assertEquals(42, p.get())
            assertEquals(42, p.getNow(0))
            assertEquals(42, p.toCompletableFuture().get())
            assertEquals(42, p.get(1, TimeUnit.SECONDS))

            // attempting to cancel or complete an already-completed promise returns false
            assertFalse(p.cancel(true))
            assertFalse(p.complete(99))
        }

        @Test
        void testCompletedWithNull() throws Exception {
            Promise<Object> p = SimplePromise.completed(null)

            assertTrue(p.isDone())
            assertFalse(p.isCompletedExceptionally())
            assertNull(p.await())
            assertNull(p.await())
            assertNull(p.getNow("absent"))
            assertNull(p.toCompletableFuture().get())
        }

        @Test
        void testThenApplyOnCompleted() {
            Promise<Integer> p = SimplePromise.completed(5)

            Promise<Integer> mapped = p.thenApply(x -> x * 11)

            // mapping should produce the expected result immediately
            assertEquals(55, mapped.await())
            assertFalse(mapped.isCompletedExceptionally())
        }

        @Test
        void testCompleteReturnsFalseAndObtrudeValueOverrides() {
            Promise<Integer> p = SimplePromise.completed(1)

            // cannot complete again
            assertFalse(p.complete(2))
            assertEquals(1, p.await())

            // obtrudeValue forcibly changes the stored value
            p.obtrudeValue(2)
            assertEquals(2, p.await())
        }

        @Test
        void testObtrudeExceptionMakesPromiseExceptional() {
            Promise<Integer> p = SimplePromise.completed(3)

            p.obtrudeException(new IllegalStateException("boom"))

            assertTrue(p.isCompletedExceptionally())

            // join throws CompletionException
            assertThrows(CompletionException.class, p::join)

            // await wraps thrown exception in AwaitException
            assertThrows(AwaitException.class, p::await)
        }
    }

    @Nested
    @DisplayName("allOf and anyOf Methods")
    class SimplePromiseAllAnyTest {

        private ExecutorService executor = Executors.newSingleThreadExecutor()

        @AfterEach
        void tearDown() {
            executor.shutdownNow()
        }

        @Test
        void testAllOfCompletesWhenAllComplete() throws Exception {
            Promise<Integer> p1 = SimplePromise.completed(1)
            Promise<Integer> p2 = SimplePromise.completed(2)
            Promise<Integer> p3 = SimplePromise.of()

            // complete p3 asynchronously after a short delay
            executor.submit({
                Thread.sleep(50)
                p3.complete(3)
            } )

            Promise<Void> all = SimplePromise.allOf(p1, p2, p3)

            // should complete when the last promise completes
            assertDoesNotThrow((Executable) () -> { all.await() })
            assertTrue(all.isDone())
            // allOf semantics mirror CompletableFuture.allOf (no aggregated result), expect null from join
            assertNull(all.await())
        }

        @Test
        void testAllOfPropagatesExceptionIfAnyFail() {
            // make the successful promise already completed so allOf can evaluate immediately
            Promise<Integer> good = SimplePromise.completed(7)
            Promise<Integer> bad = SimplePromise.of()
            bad.completeExceptionally(new RuntimeException("fail"))

            Promise<Void> all = SimplePromise.allOf(good, bad)

            assertTrue(all.isDone())
            assertThrows(AwaitException.class, { all.await() })
        }

        @Test
        void testAnyOfCompletesWithFirstValue() throws Exception {
            Promise<String> slow = SimplePromise.of()
            Promise<String> fast = SimplePromise.of()

            // fast completes earlier
            executor.submit({
                Thread.sleep(30)
                fast.complete("fast")
            } )

            executor.submit({
                Thread.sleep(80)
                slow.complete("slow")
            } )

            Promise<Object> any = SimplePromise.anyOf(slow, fast)

            assertEquals("fast", any.await())
            assertTrue(any.isDone())
        }

        @Test
        void testAnyOfCompletesImmediatelyIfOneAlreadyCompleted() {
            Promise<Integer> completed = SimplePromise.completed(10)
            Promise<Integer> pending = SimplePromise.of()

            Promise<Object> any = SimplePromise.anyOf(completed, pending)

            assertEquals(10, any.await())
            assertTrue(any.isDone())
        }

        @Test
        void testAnyOfPropagatesFirstExceptionIfItOccursFirst() throws Exception {
            Promise<String> exc = SimplePromise.of()
            Promise<String> value = SimplePromise.of()

            // exception happens first
            executor.submit({
                Thread.sleep(20)
                exc.completeExceptionally(new IllegalStateException("boom"))
            } )

            // value completes slightly later
            executor.submit({
                Thread.sleep(60)
                value.complete("ok")
            } )

            Promise<Object> any = SimplePromise.anyOf(exc, value)

            // first completion is exceptional -> any should be exceptional
            assertThrows(AwaitException.class, { any.await() })
        }
    }

    @Nested
    @DisplayName("Completion Methods")
    class CompletionMethodsTest {
        @Test
        @DisplayName("should complete with value")
        void testComplete() {
            Promise<String> promise = SimplePromise.of()
            boolean result = promise.complete("value")

            assertTrue(result)
            assertTrue(promise.isDone())
            assertEquals("value", promise.await())
        }

        @Test
        @DisplayName("should complete exceptionally")
        void testCompleteExceptionally() {
            Promise<String> promise = SimplePromise.of()
            Exception ex = new RuntimeException("error")
            boolean result = promise.completeExceptionally(ex)

            assertTrue(result)
            assertTrue(promise.isCompletedExceptionally())
            assertThrows(CompletionException.class, promise::join)
        }

        @Test
        @DisplayName("should complete async with supplier")
        void testCompleteAsync() throws Exception {
            Promise<String> promise = SimplePromise.of()
            promise.completeAsync(() -> "async-value")

            Thread.sleep(100)
            assertEquals("async-value", promise.get())
        }

        @Test
        @DisplayName("should complete async with supplier and executor")
        void testCompleteAsyncWithExecutor() throws Exception {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<String> promise = SimplePromise.of()
                promise.completeAsync(() -> "executor-value", executor)

                assertEquals("executor-value", promise.get(1, TimeUnit.SECONDS))
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should obtrude value")
        void testObtrudeValue() {
            Promise<String> promise = SimplePromise.of()
            promise.complete("first")
            promise.obtrudeValue("second")

            assertEquals("second", promise.await())
        }

        @Test
        @DisplayName("should obtrude exception")
        void testObtrudeException() {
            Promise<String> promise = SimplePromise.of()
            promise.complete("value")
            RuntimeException ex = new RuntimeException("obtruded")
            promise.obtrudeException(ex)

            CompletionException thrown = assertThrows(CompletionException.class, promise::join)
            assertEquals(ex, thrown.getCause())
        }
    }

    @Nested
    @DisplayName("Transformation Methods")
    class TransformationMethodsTest {
        @Test
        @DisplayName("should apply function with thenApply")
        void testThenApply() {
            Promise<Integer> promise = SimplePromise.completed(5)
            Promise<String> result = promise.thenApply(n -> "Number: " + n)

            assertEquals("Number: 5", result.await())
        }

        @Test
        @DisplayName("should apply function async with thenApplyAsync")
        void testThenApplyAsync() {
            Promise<Integer> promise = SimplePromise.completed(10)
            Promise<Integer> result = promise.thenApplyAsync(n -> n * 2)

            assertEquals(20, result.await())
        }

        @Test
        @DisplayName("should compose with thenCompose")
        void testThenCompose() {
            Promise<Integer> promise = SimplePromise.completed(3)
            Promise<Integer> result = promise.thenCompose(n -> SimplePromise.completed(n * 3))

            assertEquals(9, result.await())
        }

        @Test
        @DisplayName("should handle with bifunction")
        void testHandle() {
            Promise<Integer> promise = SimplePromise.completed(5)
            Promise<String> result = promise.handle((val, ex) -> ex == null ? "Success: " + val : "Error")

            assertEquals("Success: 5", result.await())
        }

        @Test
        @DisplayName("should handle exception with handle")
        void testHandleWithException() {
            Promise<Integer> promise = SimplePromise.of()
            promise.completeExceptionally(new RuntimeException("error"))
            Promise<String> result = promise.handle((val, ex) -> ex != null ? "Handled: " + ex.getMessage() : "OK")

            assertEquals("Handled: error", result.await())
        }
    }

    @Nested
    @DisplayName("Consumer Methods")
    class ConsumerMethodsTest {
        @Test
        @DisplayName("should accept value with thenAccept")
        void testThenAccept() {
            AtomicReference<String> ref = new AtomicReference<>()
            Promise<String> promise = SimplePromise.completed("test")
            Promise<Void> result = promise.thenAccept(ref::set)

            result.await()
            assertEquals("test", ref.get())
        }

        @Test
        @DisplayName("should run action with thenRun")
        void testThenRun() {
            AtomicBoolean executed = new AtomicBoolean(false)
            Promise<String> promise = SimplePromise.completed("test")
            Promise<Void> result = promise.thenRun(() -> executed.set(true))

            result.await()
            assertTrue(executed.get())
        }

        @Test
        @DisplayName("should execute whenComplete")
        void testWhenComplete() {
            AtomicReference<String> ref = new AtomicReference<>()
            Promise<String> promise = SimplePromise.completed("value")
            Promise<String> result = promise.whenComplete((val, ex) -> ref.set(val))

            assertEquals("value", result.await())
            assertEquals("value", ref.get())
        }
    }

    @Nested
    @DisplayName("Combination Methods")
    class CombinationMethodsTest {
        @Test
        @DisplayName("should combine two promises with thenCombine")
        void testThenCombine() {
            Promise<Integer> p1 = SimplePromise.completed(5)
            Promise<Integer> p2 = SimplePromise.completed(3)
            Promise<Integer> result = p1.thenCombine(p2, Integer::sum)

            assertEquals(8, result.await())
        }

        @Test
        @DisplayName("should accept both with thenAcceptBoth")
        void testThenAcceptBoth() {
            AtomicInteger sum = new AtomicInteger(0)
            Promise<Integer> p1 = SimplePromise.completed(5)
            Promise<Integer> p2 = SimplePromise.completed(7)
            Promise<Void> result = p1.thenAcceptBoth(p2, (a, b) -> sum.set(a + b))

            result.await()
            assertEquals(12, sum.get())
        }

        @Test
        @DisplayName("should run after both complete")
        void testRunAfterBoth() {
            AtomicBoolean executed = new AtomicBoolean(false)
            Promise<String> p1 = SimplePromise.completed("a")
            Promise<String> p2 = SimplePromise.completed("b")
            Promise<Void> result = p1.runAfterBoth(p2, () -> executed.set(true))

            result.await()
            assertTrue(executed.get())
        }

        @Test
        @DisplayName("should apply to either")
        void testApplyToEither() {
            Promise<Integer> p1 = SimplePromise.completed(1)
            Promise<Integer> p2 = SimplePromise.of()
            Promise<Integer> result = p1.applyToEither(p2, n -> n * 10)

            assertEquals(10, result.await())
        }

        @Test
        @DisplayName("should accept either")
        void testAcceptEither() {
            AtomicInteger ref = new AtomicInteger(0)
            Promise<Integer> p1 = SimplePromise.completed(42)
            Promise<Integer> p2 = SimplePromise.of()
            Promise<Void> result = p1.acceptEither(p2, ref::set)

            result.await()
            assertEquals(42, ref.get())
        }

        @Test
        @DisplayName("should run after either completes")
        void testRunAfterEither() {
            AtomicBoolean executed = new AtomicBoolean(false)
            Promise<String> p1 = SimplePromise.completed("fast")
            Promise<String> p2 = SimplePromise.of()
            Promise<Void> result = p1.runAfterEither(p2, () -> executed.set(true))

            result.await()
            assertTrue(executed.get())
        }
    }

    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandlingTest {
        @Test
        @DisplayName("should handle exception with exceptionally")
        void testExceptionally() {
            Promise<Integer> promise = SimplePromise.of()
            promise.completeExceptionally(new RuntimeException("error"))
            Promise<Integer> result = promise.exceptionally(ex -> -1)

            assertEquals(-1, result.await())
        }

        @Test
        @DisplayName("should compose exception with exceptionallyCompose")
        void testExceptionallyCompose() {
            Promise<Integer> promise = SimplePromise.of()
            promise.completeExceptionally(new RuntimeException("error"))
            Promise<Integer> result = promise.exceptionallyCompose(ex -> SimplePromise.completed(99))

            assertEquals(99, result.await())
        }

        @Test
        @DisplayName("should handle exception async with exceptionallyAsync")
        void testExceptionallyAsync() {
            Promise<String> promise = SimplePromise.of()
            promise.completeExceptionally(new RuntimeException("async-error"))
            Promise<String> result = promise.exceptionallyAsync(ex -> "recovered")

            assertEquals("recovered", result.await())
        }
    }

    @Nested
    @DisplayName("Timeout and Cancellation")
    class TimeoutAndCancellationTest {
        @Test
        @DisplayName("should timeout with orTimeout")
        void testOrTimeout() {
            Promise<String> promise = SimplePromise.of()
            Promise<String> result = promise.orTimeout(100, TimeUnit.MILLISECONDS)

            assertThrows(CompletionException.class, result::join)
        }

        @Test
        @DisplayName("should complete on timeout with completeOnTimeout")
        void testCompleteOnTimeout() {
            Promise<String> promise = SimplePromise.of()
            Promise<String> result = promise.completeOnTimeout("default", 100, TimeUnit.MILLISECONDS)

            assertEquals("default", result.await())
        }

        @Test
        @DisplayName("should cancel promise")
        void testCancel() {
            Promise<String> promise = SimplePromise.of()
            boolean cancelled = promise.cancel(true)

            assertTrue(cancelled)
            assertTrue(promise.isCancelled())
            assertTrue(promise.isDone())
        }
    }

    @Nested
    @DisplayName("Retrieval Methods")
    class RetrievalMethodsTest {
        @Test
        @DisplayName("should get value with blocking get")
        void testGet() throws Exception {
            Promise<String> promise = SimplePromise.completed("value")
            assertEquals("value", promise.get())
        }

        @Test
        @DisplayName("should get value with timeout")
        void testGetWithTimeout() throws Exception {
            Promise<String> promise = SimplePromise.completed("value")
            assertEquals("value", promise.get(1, TimeUnit.SECONDS))
        }

        @Test
        @DisplayName("should join and return value")
        void testJoin() {
            Promise<String> promise = SimplePromise.completed("joined")
            assertEquals("joined", promise.join())
        }

        @Test
        @DisplayName("should get now or default value")
        void testGetNow() {
            Promise<String> promise = SimplePromise.of()
            assertEquals("default", promise.getNow("default"))

            promise.complete("actual")
            assertEquals("actual", promise.getNow("default"))
        }

        @Test
        @DisplayName("should await and return value")
        void testAwait() {
            Promise<String> promise = SimplePromise.of(() -> "awaited")
            assertEquals("awaited", promise.await())
        }

        @Test
        @DisplayName("should throw AwaitException on await failure")
        void testAwaitException() {
            Promise<String> promise = SimplePromise.of()
            promise.completeExceptionally(new RuntimeException("error"))

            assertThrows(AwaitException.class, promise::await)
        }
    }

    @Nested
    @DisplayName("Status and Conversion")
    class StatusAndConversionTest {
        @Test
        @DisplayName("should check if done")
        void testIsDone() {
            Promise<String> promise = SimplePromise.of()
            assertFalse(promise.isDone())

            promise.complete("value")
            assertTrue(promise.isDone())
        }

        @Test
        @DisplayName("should check if cancelled")
        void testIsCancelled() {
            Promise<String> promise = SimplePromise.of()
            assertFalse(promise.isCancelled())

            promise.cancel(false)
            assertTrue(promise.isCancelled())
        }

        @Test
        @DisplayName("should check if completed exceptionally")
        void testIsCompletedExceptionally() {
            Promise<String> promise = SimplePromise.of()
            assertFalse(promise.isCompletedExceptionally())

            promise.completeExceptionally(new RuntimeException())
            assertTrue(promise.isCompletedExceptionally())
        }

        @Test
        @DisplayName("should convert to CompletableFuture")
        void testToCompletableFuture() {
            CompletableFuture<String> cf = CompletableFuture.completedFuture("test")
            Promise<String> promise = SimplePromise.of(cf)

            assertSame(cf, promise.toCompletableFuture())
        }

        @Test
        @DisplayName("should convert to Promise")
        void testToPromise() {
            Promise<String> promise = SimplePromise.of()
            assertSame(promise, promise.toPromise())
        }

        @Test
        @DisplayName("should copy promise")
        void testCopy() {
            Promise<String> original = SimplePromise.completed("original")
            Promise<String> copy = original.copy()

            assertNotSame(original, copy)
            assertEquals("original", copy.await())
        }

        @Test
        @DisplayName("should get number of dependents")
        void testGetNumberOfDependents() {
            Promise<String> promise = SimplePromise.of()
            assertEquals(0, promise.getNumberOfDependents())

            promise.thenApply(String::toUpperCase)
            assertTrue(promise.getNumberOfDependents() > 0)
        }

        @Test
        @DisplayName("should get default executor")
        void testDefaultExecutor() {
            Promise<String> promise = SimplePromise.of()
            assertNotNull(promise.defaultExecutor())
        }
    }

    @Nested
    @DisplayName("Additional Coverage Tests")
    class AdditionalCoverageTest {

        @Test
        @DisplayName("should apply function async with executor")
        void testThenApplyAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<Integer> promise = SimplePromise.completed(5)
                Promise<String> result = promise.thenApplyAsync(n -> "Value: " + n, executor)

                assertEquals("Value: 5", result.await())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should accept value async with executor")
        void testThenAcceptAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            AtomicReference<String> ref = new AtomicReference<>()
            try {
                Promise<String> promise = SimplePromise.completed("test")
                Promise<Void> result = promise.thenAcceptAsync(ref::set, executor)

                result.await()
                assertEquals("test", ref.get())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should run action async with executor")
        void testThenRunAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            AtomicBoolean executed = new AtomicBoolean(false)
            try {
                Promise<String> promise = SimplePromise.completed("test")
                Promise<Void> result = promise.thenRunAsync(() -> executed.set(true), executor)

                result.await()
                assertTrue(executed.get())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should compose async with executor")
        void testThenComposeAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<Integer> promise = SimplePromise.completed(3)
                Promise<Integer> result = promise.thenComposeAsync(n -> SimplePromise.completed((n * 4)), executor)

                assertEquals(12, result.await())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should handle async with executor")
        void testHandleAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<Integer> promise = SimplePromise.completed(7)
                Promise<String> result = promise.handleAsync((val, ex) -> ex == null ? "Result: " + val : "Error", executor)

                assertEquals("Result: 7", result.await())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should execute whenCompleteAsync with executor")
        void testWhenCompleteAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            AtomicReference<String> ref = new AtomicReference<>()
            try {
                Promise<String> promise = SimplePromise.completed("async")
                Promise<String> result = promise.whenCompleteAsync((val, ex) -> ref.set(val), executor)

                assertEquals("async", result.await())
                assertEquals("async", ref.get())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should combine async with executor")
        void testThenCombineAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<Integer> p1 = SimplePromise.completed(10)
                Promise<Integer> p2 = SimplePromise.completed(20)
                Promise<Integer> result = p1.thenCombineAsync(p2, Integer::sum, executor)

                assertEquals(30, result.await())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should accept both async with executor")
        void testThenAcceptBothAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            AtomicInteger sum = new AtomicInteger(0)
            try {
                Promise<Integer> p1 = SimplePromise.completed(15)
                Promise<Integer> p2 = SimplePromise.completed(25)
                Promise<Void> result = p1.thenAcceptBothAsync(p2, (a, b) -> sum.set(a + b), executor)

                result.await()
                assertEquals(40, sum.get())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should run after both async with executor")
        void testRunAfterBothAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            AtomicBoolean executed = new AtomicBoolean(false)
            try {
                Promise<String> p1 = SimplePromise.completed("x")
                Promise<String> p2 = SimplePromise.completed("y")
                Promise<Void> result = p1.runAfterBothAsync(p2, () -> executed.set(true), executor)

                result.await()
                assertTrue(executed.get())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should apply to either async with executor")
        void testApplyToEitherAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<Integer> p1 = SimplePromise.completed(100)
                Promise<Integer> p2 = SimplePromise.of()
                Promise<Integer> result = p1.applyToEitherAsync(p2, n -> n * 2, executor)

                assertEquals(200, result.await())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should accept either async with executor")
        void testAcceptEitherAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            AtomicInteger ref = new AtomicInteger(0)
            try {
                Promise<Integer> p1 = SimplePromise.completed(88)
                Promise<Integer> p2 = SimplePromise.of()
                Promise<Void> result = p1.acceptEitherAsync(p2, ref::set, executor)

                result.await()
                assertEquals(88, ref.get())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should run after either async with executor")
        void testRunAfterEitherAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            AtomicBoolean executed = new AtomicBoolean(false)
            try {
                Promise<String> p1 = SimplePromise.completed("first")
                Promise<String> p2 = SimplePromise.of()
                Promise<Void> result = p1.runAfterEitherAsync(p2, () -> executed.set(true), executor)

                result.await()
                assertTrue(executed.get())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should handle exception async with exceptionallyComposeAsync with executor")
        void testExceptionallyComposeAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<Integer> promise = SimplePromise.of()
                promise.completeExceptionally(new RuntimeException("error"))
                Promise<Integer> result = promise.exceptionallyComposeAsync(ex -> SimplePromise.completed(777), executor)

                assertEquals(777, result.await())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should handle exception with exceptionallyAsync with executor")
        void testExceptionallyAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<String> promise = SimplePromise.of()
                promise.completeExceptionally(new RuntimeException("error"))
                Promise<String> result = promise.exceptionallyAsync(ex -> "handled", executor)

                assertEquals("handled", result.await())
            } finally {
                executor.shutdown()
            }
        }
    }

    @Nested
    @DisplayName("Advanced Async Ops")
    class SimplePromiseAdvancedAsyncOpsTest {

        private final ExecutorService executor = Executors.newFixedThreadPool(4)

        @AfterEach
        void tearDown() {
            executor.shutdownNow()
        }

        @Test
        void testExceptionallyComposeAsync_recoversFromException() {
            Promise<Integer> p = SimplePromise.of()
            p.completeExceptionally(new RuntimeException("boom"))

            Promise<Integer> recovered = p.exceptionallyComposeAsync({ Throwable ex -> SimplePromise.completed(99) }, executor)

            assertEquals(99, recovered.await())
            assertTrue(recovered.isDone())
            assertFalse(recovered.isCompletedExceptionally())
        }

        @Test
        void testExceptionallyComposeAsync_noExceptionPassesThrough() {
            Promise<String> p = SimplePromise.completed("ok")

            Promise<String> result = p.exceptionallyComposeAsync({ Throwable ex -> SimplePromise.completed("fallback") },
                executor)

            assertEquals("ok", result.await())
        }

        @Test
        void testThenAcceptBothAsync_consumerRunsWithBothValues() {
            Promise<Integer> left = SimplePromise.completed(2)
            Promise<Integer> right = SimplePromise.of({ 3 }, executor)

            AtomicInteger sum = new AtomicInteger()
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Void> done = left.thenAcceptBothAsync(right,
                { Integer a, Integer b ->
                    sum.set(a + b)
                    latch.countDown()
                },
                executor)

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            done.await()
            assertEquals(5, sum.get())
        }

        @Test
        void testRunAfterEitherAsync_runsWhenEitherCompletes() {
            Promise<String> fast = SimplePromise.of()
            Promise<String> slow = SimplePromise.of()

            AtomicBoolean ran = new AtomicBoolean(false)
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Void> r = fast.runAfterEitherAsync(slow,
                {
                    ran.set(true)
                    latch.countDown()
                },
                executor)

            fast.complete("first")

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            r.await()
            assertTrue(ran.get())
        }

        @Test
        void testThenCombineAsync_combinesResultsToNewValue() {
            Promise<Integer> a = SimplePromise.completed(6)
            Promise<Integer> b = SimplePromise.of({ 7 }, executor)

            Promise<Integer> combined = a.thenCombineAsync(b,
                { Integer x, Integer y -> x * y },
                executor)

            assertEquals(42, combined.await())
        }

        @Test
        void testHandleAsync_handlesSuccessCase() {
            Promise<Integer> ok = SimplePromise.completed(10)

            Promise<?> handled = ok.handleAsync({ Integer v, Throwable ex ->
                if (ex != null) return "err"
                return "v:$v" as String
            }, executor)

            assertEquals("v:10", handled.await())
        }

        @Test
        void testHandleAsync_handlesExceptionCase() {
            Promise<Integer> exc = SimplePromise.of()
            exc.completeExceptionally(new IllegalStateException("fail"))

            Promise<?> recovered = exc.handleAsync({ Integer v, Throwable ex ->
                if (ex != null) return "recovered"
                return "v:$v"
            }, executor)

            assertEquals("recovered", recovered.await())
        }

        @Test
        void testAcceptEitherAsync_usesFirstCompleted() {
            Promise<String> slow = SimplePromise.of()
            Promise<String> fast = SimplePromise.of()

            AtomicReference<String> accepted = new AtomicReference<>()
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Void> acceptPromise = fast.acceptEitherAsync(slow,
                { String val ->
                    accepted.set(val)
                    latch.countDown()
                },
                executor)

            executor.submit({
                Thread.sleep(30)
                fast.complete("fast")
            } )

            executor.submit({
                Thread.sleep(80)
                slow.complete("slow")
            } )

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            acceptPromise.await()
            assertEquals("fast", accepted.get())
        }

        @Test
        void testApplyToEitherAsync_transformsFirstCompleted() {
            Promise<String> already = SimplePromise.completed("ready")
            Promise<String> pending = SimplePromise.of()

            Promise<String> applied = already.applyToEitherAsync(pending,
                { String val -> "applied:$val" as String }, executor)

            String result = applied.await()
            assertTrue(result.startsWith("applied:"))
            assertTrue(result.contains("ready"))
        }

        @Test
        void testWhenCompleteAsync_actionSeesValueAndNoThrowable() {
            Promise<Integer> p = SimplePromise.completed(7)
            AtomicReference<Integer> valueRef = new AtomicReference<>()
            AtomicReference<Throwable> exRef = new AtomicReference<>()
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Integer> after = p.whenCompleteAsync({ Integer v, Throwable ex ->
                valueRef.set(v)
                exRef.set(ex)
                latch.countDown()
            }, executor)

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            assertEquals(7, after.await())
            assertEquals(7, valueRef.get())
            assertNull(exRef.get())
        }

        @Test
        void testWhenCompleteAsync_actionSeesException() {
            Promise<Integer> p = SimplePromise.of()
            p.completeExceptionally(new IllegalArgumentException("bad"))

            AtomicReference<Throwable> exRef = new AtomicReference<>()
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Integer> after = p.whenCompleteAsync({ Integer v, Throwable ex ->
                exRef.set(ex)
                latch.countDown()
            }, executor)

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            assertNotNull(exRef.get())
            assertThrows(AwaitException, { after.await() })
        }

        @Test
        void testThenRunAsync_runnableExecutesAfterCompletion() {
            Promise<Integer> p = SimplePromise.of()
            AtomicBoolean ran = new AtomicBoolean(false)
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Void> r = p.thenRunAsync({
                ran.set(true)
                latch.countDown()
            }, executor)

            p.complete(100)

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            r.await()
            assertTrue(ran.get())
        }

        @Test
        void testThenComposeAsync_chainsToAnotherPromise() {
            Promise<Integer> source = SimplePromise.completed(4)

            Promise<Integer> composed = source.thenComposeAsync({ Integer v -> SimplePromise.completed(v * 3) }, executor)

            assertEquals(12, composed.await())
        }

        @Test
        void testThenComposeAsync_chainsWithAsyncExecution() {
            Promise<Integer> source = SimplePromise.of({ 5 }, executor)

            Promise<String> composed = source.thenComposeAsync({ Integer v -> SimplePromise.of({ "result:$v" as String }, executor)}, executor)

            assertEquals("result:5", composed.await())
        }

        @Test
        void testThenAcceptAsync_consumerRunsWithValue() {
            Promise<String> p = SimplePromise.completed("hello")
            AtomicReference<String> seen = new AtomicReference<>()
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Void> r = p.thenAcceptAsync({ String val ->
                seen.set(val)
                latch.countDown()
            },
                executor)

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            r.await()
            assertEquals("hello", seen.get())
        }

        @Test
        void testRunAfterBothAsync_runsWhenBothComplete() {
            Promise<Void> a = SimplePromise.of()
            Promise<Void> b = SimplePromise.of()

            AtomicBoolean ran = new AtomicBoolean(false)
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Void> after = a.runAfterBothAsync(b,
                {
                    ran.set(true)
                    latch.countDown()
                }, executor)

            a.complete(null)
            b.complete(null)

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            after.await()
            assertTrue(ran.get())
        }

        @Test
        void testRunAfterBothAsync_waitsForBothToComplete() {
            Promise<Integer> slow = SimplePromise.of()
            Promise<Integer> fast = SimplePromise.completed(1)

            AtomicBoolean ran = new AtomicBoolean(false)
            CountDownLatch latch = new CountDownLatch(1)

            Promise<Void> after = fast.runAfterBothAsync(slow,
                {
                    ran.set(true)
                    latch.countDown()
                }, executor)

            assertFalse(ran.get())

            slow.complete(2)

            assertTrue(latch.await(2, TimeUnit.SECONDS))
            after.await()
            assertTrue(ran.get())
        }
    }

}
