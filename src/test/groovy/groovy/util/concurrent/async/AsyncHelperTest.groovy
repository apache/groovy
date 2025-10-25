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


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

import static groovy.util.concurrent.async.AsyncHelper.async
import static groovy.util.concurrent.async.AsyncHelper.await
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

@DisplayName("AsyncHelper Tests")
class AsyncHelperTest {

    @Nested
    @DisplayName("Basic async/await operations")
    class BasicOperationsTest {

        @Test
        @DisplayName("should execute simple async operation")
        void testSimpleAsync() {
            Promise<Integer> promise = async(() -> 42)
            Integer result = await(promise)
            assertEquals(42, result)
        }

        @Test
        @DisplayName("should execute async operation with string")
        void testAsyncWithString() {
            Promise<String> promise = async(() -> "Hello, Async!")
            String result = await(promise)
            assertEquals("Hello, Async!", result)
        }

        @Test
        @DisplayName("should handle async operation returning null")
        void testAsyncReturningNull() {
            Promise<Object> promise = async(() -> null)
            Object result = await(promise)
            assertNull(result)
        }

        @Test
        @DisplayName("should execute multiple async operations")
        void testMultipleAsyncOperations() {
            Promise<Integer> p1 = async(() -> 10)
            Promise<Integer> p2 = async(() -> 20)
            Promise<Integer> p3 = async(() -> 30)

            assertEquals(10, await(p1))
            assertEquals(20, await(p2))
            assertEquals(30, await(p3))
        }

        @Test
        @DisplayName("should handle computation in async")
        void testAsyncComputation() {
            Promise<Integer> promise = async(() -> {
                int sum = 0
                for (int i = 1; i <= 100; i++) {
                    sum += i
                }
                return sum
            })
            assertEquals(5050, await(promise))
        }
    }

    @Nested
    @DisplayName("Custom executor operations")
    class CustomExecutorTest {
        private ExecutorService customExecutor

        @AfterEach
        void cleanup() {
            if (customExecutor != null) {
                customExecutor.shutdown()
            }
        }

        @Test
        @DisplayName("should use custom executor for async operation")
        void testAsyncWithCustomExecutor() {
            customExecutor = Executors.newSingleThreadExecutor()
            AtomicReference<String> threadName = new AtomicReference<>()

            Promise<Integer> promise = async(() -> {
                threadName.set(Thread.currentThread().getName())
                return 100
            }, customExecutor)

            assertEquals(100, await(promise))
            assertNotNull(threadName.get())
        }

        @Test
        @DisplayName("should handle multiple operations with custom executor")
        void testMultipleOperationsWithCustomExecutor() {
            customExecutor = Executors.newFixedThreadPool(2)

            Promise<Integer> p1 = async(() -> 1, customExecutor)
            Promise<Integer> p2 = async(() -> 2, customExecutor)
            Promise<Integer> p3 = async(() -> 3, customExecutor)

            assertEquals(6, await(p1) + await(p2) + await(p3))
        }

        @Test
        @DisplayName("should work with cached thread pool")
        void testWithCachedThreadPool() {
            customExecutor = Executors.newCachedThreadPool()
            List<Promise<Integer>> promises = new ArrayList<>()

            for (int i = 0; i < 10; i++) {
                final int value = i
                promises.add(async(() -> value * 2, customExecutor))
            }

            int sum = 0
            for (Promise<Integer> p : promises) {
                sum += await(p)
            }
            assertEquals(90, sum)
        }
    }

    @Nested
    @DisplayName("JavaScript-like async/await patterns")
    class JavaScriptPatternsTest {

        @Test
        @DisplayName("should chain async operations like Promise.then()")
        void testChainedAsync() {
            Promise<Integer> result = async(() -> 10)
                .thenApply(n -> n * 2)
                .thenApply(n -> n + 5)

            assertEquals(25, await(result))
        }

        @Test
        @DisplayName("should handle sequential async operations")
        void testSequentialAsync() {
            Promise<String> step1 = async(() -> "Step1")
            String result1 = await(step1)

            Promise<String> step2 = async(() -> result1 + "-Step2")
            String result2 = await(step2)

            Promise<String> step3 = async(() -> result2 + "-Step3")
            String result3 = await(step3)

            assertEquals("Step1-Step2-Step3", result3)
        }

        @Test
        @DisplayName("should handle parallel async operations like Promise.all()")
        @Timeout(2)
        void testParallelAsync() {
            Promise<Integer> p1 = async(() -> {
                sleep(100)
                return 1
            })

            Promise<Integer> p2 = async(() -> {
                sleep(100)
                return 2
            })

            Promise<Integer> p3 = async(() -> {
                sleep(100)
                return 3
            })

            Promise<Void> all = SimplePromise.allOf(p1, p2, p3)
            await(all)

            assertEquals(1, await(p1))
            assertEquals(2, await(p2))
            assertEquals(3, await(p3))
        }

        @Test
        @DisplayName("should handle race conditions like Promise.race()")
        @Timeout(1)
        void testAsyncRace() {
            Promise<String> slow = async(() -> {
                sleep(500)
                return "slow"
            })

            Promise<String> fast = async(() -> {
                sleep(50)
                return "fast"
            })

            Promise<String> winner = SimplePromise.anyOf(fast, slow)
            String result = await(winner)

            assertEquals("fast", result)
        }

        @Test
        @DisplayName("should handle async/await with exception handling")
        void testAsyncWithExceptionHandling() {
            Promise<Integer> promise = async(() -> {
                throw new RuntimeException("Simulated error")
            })

            Promise<Integer> recovered = promise.exceptionally(ex -> {
                assertTrue(ex.getCause() instanceof RuntimeException)
                return -1
            })

            assertEquals(-1, await(recovered))
        }

        @Test
        @DisplayName("should handle async/await with data transformation pipeline")
        void testAsyncDataPipeline() {
            Promise<List<Integer>> result = async(() -> List.of(1, 2, 3, 4, 5))
                .thenApply(list -> {
                    List<Integer> doubled = new ArrayList<>()
                    for (Integer n : list) {
                        doubled.add(n * 2)
                    }
                    return doubled
                })
                .thenApply(list -> {
                    List<Integer> filtered = new ArrayList<>()
                    for (Integer n : list) {
                        if (n > 5) {
                            filtered.add(n)
                        }
                    }
                    return filtered
                })

            List<Integer> expected = List.of(6, 8, 10)
            assertEquals(expected, await(result))
        }

        @Test
        @DisplayName("should handle nested async operations")
        void testNestedAsync() {
            Promise<Integer> outer = async(() -> {
                Promise<Integer> inner = async(() -> 5)
                return await(inner) * 2
            })

            assertEquals(10, await(outer))
        }
    }

    @Nested
    @DisplayName("Advanced async patterns")
    class AdvancedPatternsTest {

        @Test
        @DisplayName("should handle async retry pattern")
        void testAsyncRetryPattern() {
            AtomicInteger attempts = new AtomicInteger(0)

            Promise<String> result = async(() -> {
                int count = attempts.incrementAndGet()
                if (count < 3) {
                    throw new RuntimeException("Not ready yet")
                }
                return "Success after " + count + " attempts"
            }).exceptionallyCompose(ex -> {
                sleep(50)
                return async(() -> {
                    int count = attempts.incrementAndGet()
                    return "Success after " + count + " attempts"
                })
            })

            String finalResult = await(result)
            assertTrue(finalResult.contains("Success"))
            assertTrue(attempts.get() >= 2)
        }

        @Test
        @DisplayName("should handle async timeout pattern")
        @Timeout(1)
        void testAsyncTimeoutPattern() throws Exception {
            Promise<String> slowTask = async(() -> {
                sleep(5000)
                return "completed"
            })

            CompletableFuture<String> timeoutFuture = slowTask.toCompletableFuture()
                .orTimeout(200, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> "timeout")

            assertEquals("timeout", timeoutFuture.get())
        }

        @Test
        @DisplayName("should handle async map-reduce pattern")
        void testAsyncMapReducePattern() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5)

            List<Promise<Integer>> squarePromises = new ArrayList<>()
            for (Integer n : numbers) {
                def tmpN = n
                squarePromises.add(async(() -> tmpN * tmpN))
            }

            int total = 0
            for (Promise<Integer> p : squarePromises) {
                total += await(p)
            }

            println "done!"

            assertEquals(55, total)
        }

        @Test
        @DisplayName("should handle async combine operations")
        void testAsyncCombine() {
            Promise<Integer> p1 = async(() -> 10)
            Promise<Integer> p2 = async(() -> 20)

            Promise<Integer> combined = p1.thenCombine(p2, Integer::sum)

            assertEquals(30, await(combined))
        }

        @Test
        @DisplayName("should handle async compose operations")
        void testAsyncCompose() {
            Promise<Integer> initial = async(() -> 5)

            Promise<Integer> composed = initial.thenCompose(n ->
                async(() -> n * 3)
            )

            assertEquals(15, await(composed))
        }
    }

    @Nested
    @DisplayName("Real-world scenarios")
    class RealWorldScenariosTest {

        @Test
        @DisplayName("should simulate API call with data transformation")
        void testSimulateApiCall() {
            Promise<String> apiCall = async(() -> {
                sleep(50)
                return "{\"userId\":1,\"name\":\"John\"}"
            })

            Promise<String> transformed = apiCall.thenApply(json ->
                json.replace("John", "Jane")
            )

            String result = await(transformed)
            assertTrue(result.contains("Jane"))
            assertFalse(result.contains("John"))
        }

        @Test
        @DisplayName("should handle multiple parallel API calls")
        @Timeout(1)
        void testMultipleParallelApiCalls() {
            Promise<String> userApi = async(() -> {
                sleep(100)
                return "User data"
            })

            Promise<String> orderApi = async(() -> {
                sleep(100)
                return "Order data"
            })

            Promise<String> productApi = async(() -> {
                sleep(100)
                return "Product data"
            })

            Promise<String> combined = userApi.thenCombine(orderApi, (u, o) -> u + ", " + o)
                .thenCombine(productApi, (uo, p) -> uo + ", " + p)

            String result = await(combined)
            assertEquals("User data, Order data, Product data", result)
        }

        @Test
        @DisplayName("should handle async cache pattern")
        void testAsyncCachePattern() {
            AtomicReference<String> cache = new AtomicReference<>()
            AtomicInteger fetchCount = new AtomicInteger(0)

            Promise<String> getCachedData = async(() -> {
                if (cache.get() != null) {
                    return cache.get()
                }
                fetchCount.incrementAndGet()
                sleep(50)
                String data = "Fresh data"
                cache.set(data)
                return data
            })

            String firstCall = await(getCachedData)
            assertEquals("Fresh data", firstCall)
            assertEquals(1, fetchCount.get())

            Promise<String> secondCall = async(() -> cache.get())
            assertEquals("Fresh data", await(secondCall))
            assertEquals(1, fetchCount.get())
        }

        @Test
        @DisplayName("should handle async queue processing")
        @Timeout(2)
        void testAsyncQueueProcessing() {
            List<Integer> queue = List.of(10, 20, 30, 40, 50)
            AtomicInteger processed = new AtomicInteger(0)

            List<Promise<Void>> tasks = new ArrayList<>()
            for (Integer item : queue) {
                def tmp = item
                tasks.add(async(() -> {
                    sleep(50)
                    processed.addAndGet(tmp)
                    return
                }))
            }

            Promise<Void> allProcessed = SimplePromise.allOf(tasks.toArray(new Promise[0]))
            await(allProcessed)

            assertEquals(150, processed.get())
        }

        @Test
        @DisplayName("should handle async batch processing")
        void testAsyncBatchProcessing() {
            List<Integer> batch = List.of(1, 2, 3, 4, 5)

            Promise<Integer> batchSum = async(() -> {
                int sum = 0
                for (Integer item : batch) {
                    sum += await(async(() -> item * 2))
                }
                return sum
            })

            assertEquals(30, await(batchSum))
        }
    }

    @Nested
    @DisplayName("Error handling scenarios")
    class ErrorHandlingTest {

        @Test
        @DisplayName("should propagate exceptions in async chain")
        void testExceptionPropagation() {
            Promise<Integer> promise = async(() -> 10)
                .thenApply(n -> {
                    throw new RuntimeException("Chain error")
                })

            assertThrows(AwaitException.class, () -> await(promise))
        }

        @Test
        @DisplayName("should handle exception in async operation")
        void testAsyncException() {
            Promise<Integer> promise = async(() -> {
                throw new IllegalStateException("Async error")
            })

            assertThrows(AwaitException.class, () -> await(promise))
        }

        @Test
        @DisplayName("should recover from exception with fallback")
        void testExceptionRecovery() {
            Promise<String> promise = async(() -> {
                throw new RuntimeException("Error")
            }).handle((result, ex) -> {
                if (ex != null) {
                    return "Fallback value"
                }
                return result
            })

            assertEquals("Fallback value", await(promise))
        }

        @Test
        @DisplayName("should handle exception with exceptionally")
        void testExceptionallyHandler() {
            Promise<Integer> promise = async(() -> {
                throw new RuntimeException("Error")
            }).exceptionally(ex -> {
                assertTrue(ex.getCause() instanceof RuntimeException)
                return 999
            })

            assertEquals(999, await(promise))
        }

        @Test
        @DisplayName("should handle null pointer exception")
        void testNullPointerException() {
            Promise<String> promise = async(() -> {
                String s = null
                return s.length() + ""
            })

            assertThrows(AwaitException.class, () -> await(promise))
        }
    }

    @Nested
    @DisplayName("Thread safety and concurrency")
    class ConcurrencyTest {

        @Test
        @DisplayName("should handle concurrent async operations")
        @Timeout(2)
        void testConcurrentOperations() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0)
            CountDownLatch latch = new CountDownLatch(100)

            for (int i = 0; i < 100; i++) {
                async(() -> {
                    counter.incrementAndGet()
                    latch.countDown()
                    return null
                })
            }

            latch.await(1, TimeUnit.SECONDS)
            assertEquals(100, counter.get())
        }

        @Test
        @DisplayName("should handle shared state correctly")
        void testSharedState() {
            AtomicInteger shared = new AtomicInteger(0)

            List<Promise<Void>> promises = new ArrayList<>()
            for (int i = 0; i < 10; i++) {
                promises.add(async(() -> {
                    shared.incrementAndGet()
                    return null
                }))
            }

            for (Promise<Void> p : promises) {
                await(p)
            }

            assertEquals(10, shared.get())
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("should handle empty result")
        void testEmptyResult() {
            Promise<Void> promise = async(() -> null)
            assertNull(await(promise))
        }

        @Test
        @DisplayName("should handle boolean results")
        void testBooleanResults() {
            Promise<Boolean> truePromise = async(() -> true)
            Promise<Boolean> falsePromise = async(() -> false)

            assertTrue(await(truePromise))
            assertFalse(await(falsePromise))
        }

        @Test
        @DisplayName("should handle long running tasks")
        @Timeout(2)
        void testLongRunningTask() {
            Promise<String> promise = async(() -> {
                sleep(500)
                return "Long task completed"
            })

            assertEquals("Long task completed", await(promise))
        }

        @Test
        @DisplayName("should handle immediate completion")
        void testImmediateCompletion() {
            long start = System.currentTimeMillis()
            Promise<String> promise = async(() -> "Immediate")
            String result = await(promise)
            long duration = System.currentTimeMillis() - start

            assertEquals("Immediate", result)
            assertTrue(duration < 100)
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt()
        }
    }
}
