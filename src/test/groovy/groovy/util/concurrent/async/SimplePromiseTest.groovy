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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNotSame
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
            CompletableFuture<String> cf = CompletableFuture.completedFuture("test")
            Promise<String> promise = SimplePromise.of(cf)

            assertNotNull(promise)
            assertEquals("test", promise.join())
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
    @DisplayName("Completion Methods")
    class CompletionMethodsTest {
        @Test
        @DisplayName("should complete with value")
        void testComplete() {
            Promise<String> promise = SimplePromise.of()
            boolean result = promise.complete("value")

            assertTrue(result)
            assertTrue(promise.isDone())
            assertEquals("value", promise.join())
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

            assertEquals("second", promise.join())
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
            Promise<Integer> promise = SimplePromise.of(CompletableFuture.completedFuture(5))
            Promise<String> result = promise.thenApply(n -> "Number: " + n)

            assertEquals("Number: 5", result.join())
        }

        @Test
        @DisplayName("should apply function async with thenApplyAsync")
        void testThenApplyAsync() {
            Promise<Integer> promise = SimplePromise.of(CompletableFuture.completedFuture(10))
            Promise<Integer> result = promise.thenApplyAsync(n -> n * 2)

            assertEquals(20, result.join())
        }

        @Test
        @DisplayName("should compose with thenCompose")
        void testThenCompose() {
            Promise<Integer> promise = SimplePromise.of(CompletableFuture.completedFuture(3))
            Promise<Integer> result = promise.thenCompose(n ->
                SimplePromise.of(CompletableFuture.completedFuture(n * 3)))

            assertEquals(9, result.join())
        }

        @Test
        @DisplayName("should handle with bifunction")
        void testHandle() {
            Promise<Integer> promise = SimplePromise.of(CompletableFuture.completedFuture(5))
            Promise<String> result = promise.handle((val, ex) ->
                ex == null ? "Success: " + val : "Error")

            assertEquals("Success: 5", result.join())
        }

        @Test
        @DisplayName("should handle exception with handle")
        void testHandleWithException() {
            Promise<Integer> promise = SimplePromise.of()
            promise.completeExceptionally(new RuntimeException("error"))
            Promise<String> result = promise.handle((val, ex) ->
                ex != null ? "Handled: " + ex.getMessage() : "OK")

            assertEquals("Handled: error", result.join())
        }
    }

    @Nested
    @DisplayName("Consumer Methods")
    class ConsumerMethodsTest {
        @Test
        @DisplayName("should accept value with thenAccept")
        void testThenAccept() {
            AtomicReference<String> ref = new AtomicReference<>()
            Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("test"))
            Promise<Void> result = promise.thenAccept(ref::set)

            result.join()
            assertEquals("test", ref.get())
        }

        @Test
        @DisplayName("should run action with thenRun")
        void testThenRun() {
            AtomicBoolean executed = new AtomicBoolean(false)
            Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("test"))
            Promise<Void> result = promise.thenRun(() -> executed.set(true))

            result.join()
            assertTrue(executed.get())
        }

        @Test
        @DisplayName("should execute whenComplete")
        void testWhenComplete() {
            AtomicReference<String> ref = new AtomicReference<>()
            Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("value"))
            Promise<String> result = promise.whenComplete((val, ex) -> ref.set(val))

            assertEquals("value", result.join())
            assertEquals("value", ref.get())
        }
    }

    @Nested
    @DisplayName("Combination Methods")
    class CombinationMethodsTest {
        @Test
        @DisplayName("should combine two promises with thenCombine")
        void testThenCombine() {
            Promise<Integer> p1 = SimplePromise.of(CompletableFuture.completedFuture(5))
            Promise<Integer> p2 = SimplePromise.of(CompletableFuture.completedFuture(3))
            Promise<Integer> result = p1.thenCombine(p2, Integer::sum)

            assertEquals(8, result.join())
        }

        @Test
        @DisplayName("should accept both with thenAcceptBoth")
        void testThenAcceptBoth() {
            AtomicInteger sum = new AtomicInteger(0)
            Promise<Integer> p1 = SimplePromise.of(CompletableFuture.completedFuture(5))
            Promise<Integer> p2 = SimplePromise.of(CompletableFuture.completedFuture(7))
            Promise<Void> result = p1.thenAcceptBoth(p2, (a, b) -> sum.set(a + b))

            result.join()
            assertEquals(12, sum.get())
        }

        @Test
        @DisplayName("should run after both complete")
        void testRunAfterBoth() {
            AtomicBoolean executed = new AtomicBoolean(false)
            Promise<String> p1 = SimplePromise.of(CompletableFuture.completedFuture("a"))
            Promise<String> p2 = SimplePromise.of(CompletableFuture.completedFuture("b"))
            Promise<Void> result = p1.runAfterBoth(p2, () -> executed.set(true))

            result.join()
            assertTrue(executed.get())
        }

        @Test
        @DisplayName("should apply to either")
        void testApplyToEither() {
            Promise<Integer> p1 = SimplePromise.of(CompletableFuture.completedFuture(1))
            Promise<Integer> p2 = SimplePromise.of()
            Promise<Integer> result = p1.applyToEither(p2, n -> n * 10)

            assertEquals(10, result.join())
        }

        @Test
        @DisplayName("should accept either")
        void testAcceptEither() {
            AtomicInteger ref = new AtomicInteger(0)
            Promise<Integer> p1 = SimplePromise.of(CompletableFuture.completedFuture(42))
            Promise<Integer> p2 = SimplePromise.of()
            Promise<Void> result = p1.acceptEither(p2, ref::set)

            result.join()
            assertEquals(42, ref.get())
        }

        @Test
        @DisplayName("should run after either completes")
        void testRunAfterEither() {
            AtomicBoolean executed = new AtomicBoolean(false)
            Promise<String> p1 = SimplePromise.of(CompletableFuture.completedFuture("fast"))
            Promise<String> p2 = SimplePromise.of()
            Promise<Void> result = p1.runAfterEither(p2, () -> executed.set(true))

            result.join()
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

            assertEquals(-1, result.join())
        }

        @Test
        @DisplayName("should compose exception with exceptionallyCompose")
        void testExceptionallyCompose() {
            Promise<Integer> promise = SimplePromise.of()
            promise.completeExceptionally(new RuntimeException("error"))
            Promise<Integer> result = promise.exceptionallyCompose(ex ->
                SimplePromise.of(CompletableFuture.completedFuture(99)))

            assertEquals(99, result.join())
        }

        @Test
        @DisplayName("should handle exception async with exceptionallyAsync")
        void testExceptionallyAsync() {
            Promise<String> promise = SimplePromise.of()
            promise.completeExceptionally(new RuntimeException("async-error"))
            Promise<String> result = promise.exceptionallyAsync(ex -> "recovered")

            assertEquals("recovered", result.join())
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

            assertEquals("default", result.join())
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
            Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("value"))
            assertEquals("value", promise.get())
        }

        @Test
        @DisplayName("should get value with timeout")
        void testGetWithTimeout() throws Exception {
            Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("value"))
            assertEquals("value", promise.get(1, TimeUnit.SECONDS))
        }

        @Test
        @DisplayName("should join and return value")
        void testJoin() {
            Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("joined"))
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
            Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("awaited"))
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
            Promise<String> original = SimplePromise.of(CompletableFuture.completedFuture("original"))
            Promise<String> copy = original.copy()

            assertNotSame(original, copy)
            assertEquals("original", copy.join())
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
                Promise<Integer> promise = SimplePromise.of(CompletableFuture.completedFuture(5))
                Promise<String> result = promise.thenApplyAsync(n -> "Value: " + n, executor)

                assertEquals("Value: 5", result.join())
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
                Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("test"))
                Promise<Void> result = promise.thenAcceptAsync(ref::set, executor)

                result.join()
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
                Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("test"))
                Promise<Void> result = promise.thenRunAsync(() -> executed.set(true), executor)

                result.join()
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
                Promise<Integer> promise = SimplePromise.of(CompletableFuture.completedFuture(3))
                Promise<Integer> result = promise.thenComposeAsync(
                    n -> SimplePromise.of(CompletableFuture.completedFuture(n * 4)), executor)

                assertEquals(12, result.join())
            } finally {
                executor.shutdown()
            }
        }

        @Test
        @DisplayName("should handle async with executor")
        void testHandleAsyncWithExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor()
            try {
                Promise<Integer> promise = SimplePromise.of(CompletableFuture.completedFuture(7))
                Promise<String> result = promise.handleAsync(
                    (val, ex) -> ex == null ? "Result: " + val : "Error", executor)

                assertEquals("Result: 7", result.join())
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
                Promise<String> promise = SimplePromise.of(CompletableFuture.completedFuture("async"))
                Promise<String> result = promise.whenCompleteAsync((val, ex) -> ref.set(val), executor)

                assertEquals("async", result.join())
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
                Promise<Integer> p1 = SimplePromise.of(CompletableFuture.completedFuture(10))
                Promise<Integer> p2 = SimplePromise.of(CompletableFuture.completedFuture(20))
                Promise<Integer> result = p1.thenCombineAsync(p2, Integer::sum, executor)

                assertEquals(30, result.join())
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
                Promise<Integer> p1 = SimplePromise.of(CompletableFuture.completedFuture(15))
                Promise<Integer> p2 = SimplePromise.of(CompletableFuture.completedFuture(25))
                Promise<Void> result = p1.thenAcceptBothAsync(p2, (a, b) -> sum.set(a + b), executor)

                result.join()
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
                Promise<String> p1 = SimplePromise.of(CompletableFuture.completedFuture("x"))
                Promise<String> p2 = SimplePromise.of(CompletableFuture.completedFuture("y"))
                Promise<Void> result = p1.runAfterBothAsync(p2, () -> executed.set(true), executor)

                result.join()
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
                Promise<Integer> p1 = SimplePromise.of(CompletableFuture.completedFuture(100))
                Promise<Integer> p2 = SimplePromise.of()
                Promise<Integer> result = p1.applyToEitherAsync(p2, n -> n * 2, executor)

                assertEquals(200, result.join())
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
                Promise<Integer> p1 = SimplePromise.of(CompletableFuture.completedFuture(88))
                Promise<Integer> p2 = SimplePromise.of()
                Promise<Void> result = p1.acceptEitherAsync(p2, ref::set, executor)

                result.join()
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
                Promise<String> p1 = SimplePromise.of(CompletableFuture.completedFuture("first"))
                Promise<String> p2 = SimplePromise.of()
                Promise<Void> result = p1.runAfterEitherAsync(p2, () -> executed.set(true), executor)

                result.join()
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
                Promise<Integer> result = promise.exceptionallyComposeAsync(
                    ex -> SimplePromise.of(CompletableFuture.completedFuture(777)), executor)

                assertEquals(777, result.join())
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

                assertEquals("handled", result.join())
            } finally {
                executor.shutdown()
            }
        }
    }

}
