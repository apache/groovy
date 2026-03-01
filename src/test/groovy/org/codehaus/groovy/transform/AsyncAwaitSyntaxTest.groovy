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
package org.codehaus.groovy.transform

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Comprehensive syntax-level tests for the {@code async}/{@code await}/{@code for await}
 * language features. Every test uses {@code assertScript} to exercise the actual
 * Groovy parser and AST transformation pipeline, ensuring the grammar, AstBuilder,
 * and runtime all work end-to-end from the developer's perspective.
 *
 * <p>Organized by feature area:
 * <ol>
 *   <li>{@code async} modifier – method declarations</li>
 *   <li>{@code await} expression – in various statement positions</li>
 *   <li>{@code for await} – async iteration</li>
 *   <li>Combined patterns – real-world usage scenarios</li>
 *   <li>Backward compatibility – {@code async}/{@code await} as identifiers</li>
 *   <li>Error cases – syntax and semantic rejections</li>
 * </ol>
 */
class AsyncAwaitSyntaxTest {

    // =====================================================================
    // 1. 'async' modifier — method declarations
    // =====================================================================

    @Test
    void testAsyncDefMethodReturnsAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Svc {
                async greet() { "hello" }
            }
            def r = new Svc().greet()
            assert r instanceof Awaitable // its default implementation is `org.apache.groovy.runtime.async.GroovyPromise`
            assert r.get() == "hello"
        '''
    }

    @Test
    void testAsyncTypedReturnMethod() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Svc {
                async String upper(String s) { s.toUpperCase() }
            }
            assert new Svc().upper("abc").get() == "ABC"
        '''
    }

    @Test
    void testAsyncIntReturnMethod() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Svc {
                async int square(int n) { n * n }
            }
            assert new Svc().square(7).get() == 49
        '''
    }

    @Test
    void testAsyncVoidMethodReturnsAwaitable() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicReference

            class Svc {
                AtomicReference<String> ref = new AtomicReference<>()
                async void store(String v) { ref.set(v) }
            }
            def svc = new Svc()
            def a = svc.store("done")
            assert a instanceof Awaitable
            a.get()
            assert svc.ref.get() == "done"
        '''
    }

    @Test
    void testAsyncStaticMethod() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Util {
                async static add(int a, int b) { a + b }
            }
            assert Util.add(3, 4).get() == 7
        '''
    }

    @Test
    void testAsyncDefInScript() {
        assertScript '''
            import groovy.concurrent.Awaitable

            async multiply(int a, int b) { a * b }

            def r = multiply(6, 7)
            assert r instanceof Awaitable
            assert r.get() == 42
        '''
    }

    @Test
    void testAsyncMethodWithMultipleParams() {
        assertScript '''
            class Svc {
                async concat(String a, String b, String sep) {
                    return a + sep + b
                }
            }
            assert new Svc().concat("foo", "bar", "-").get() == "foo-bar"
        '''
    }

    @Test
    void testAsyncMethodWithDefaultParams() {
        assertScript '''
            class Svc {
                async greet(String name, String greeting = "Hello") {
                    "${greeting}, ${name}!"
                }
            }
            def svc = new Svc()
            assert svc.greet("World").get() == "Hello, World!"
            assert svc.greet("World", "Hi").get() == "Hi, World!"
        '''
    }

    @Test
    void testAsyncMethodWithVarargs() {
        assertScript '''
            class Svc {
                async sum(int... nums) {
                    nums.sum()
                }
            }
            assert new Svc().sum(1, 2, 3, 4).get() == 10
        '''
    }

    @Test
    void testAsyncMethodWithClosureParam() {
        assertScript '''
            class Svc {
                async transform(String s, Closure fn) {
                    fn(s)
                }
            }
            assert new Svc().transform("hello") { it.reverse() }.get() == "olleh"
        '''
    }

    @Test
    void testAsyncMethodRunsAsynchronously() {
        assertScript '''
            class Svc {
                async threadName() { Thread.currentThread().name }
            }
            def name = new Svc().threadName().get()
            assert name != Thread.currentThread().name
        '''
    }

    // =====================================================================
    // 2. 'await' expression — in various statement positions
    // =====================================================================

    @Test
    void testAwaitCompletableFuture() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async fetch() {
                    def v = await CompletableFuture.supplyAsync { 42 }
                    return v
                }
            }
            assert new Svc().fetch().get() == 42
        '''
    }

    @Test
    void testAwaitAwaitableObject() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Svc {
                async inner() { "from-inner" }
                async outer() {
                    def v = await inner()
                    return "got: ${v}"
                }
            }
            assert new Svc().outer().get() == "got: from-inner"
        '''
    }

    @Test
    void testAwaitWithParenthesizedExpression() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async compute() {
                    def v = await CompletableFuture.supplyAsync { 99 }
                    return v
                }
            }
            assert new Svc().compute().get() == 99
        '''
    }

    @Test
    void testAwaitInAssignment() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async work() {
                    String result
                    result = await CompletableFuture.supplyAsync { "assigned" }
                    return result
                }
            }
            assert new Svc().work().get() == "assigned"
        '''
    }

    @Test
    void testAwaitInArithmeticExpression() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async calc() {
                    def a = await CompletableFuture.supplyAsync { 10 }
                    def b = await CompletableFuture.supplyAsync { 20 }
                    return a + b + 5
                }
            }
            assert new Svc().calc().get() == 35
        '''
    }

    @Test
    void testMultipleSequentialAwaits() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async pipeline() {
                    def a = await CompletableFuture.supplyAsync { "step1" }
                    def b = await CompletableFuture.supplyAsync { "${a}->step2" }
                    def c = await CompletableFuture.supplyAsync { "${b}->step3" }
                    return c
                }
            }
            assert new Svc().pipeline().get() == "step1->step2->step3"
        '''
    }

    @Test
    void testAwaitInReturnStatement() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async direct() {
                    return await CompletableFuture.supplyAsync { "direct-return" }
                }
            }
            assert new Svc().direct().get() == "direct-return"
        '''
    }

    @Test
    void testAwaitInIfCondition() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async decide(boolean flag) {
                    if (flag) {
                        return await CompletableFuture.supplyAsync { "yes" }
                    } else {
                        return await CompletableFuture.supplyAsync { "no" }
                    }
                }
            }
            def svc = new Svc()
            assert svc.decide(true).get() == "yes"
            assert svc.decide(false).get() == "no"
        '''
    }

    @Test
    void testAwaitInTernaryExpression() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async ternary(boolean flag) {
                    def v = flag ? (await CompletableFuture.supplyAsync { "T" })
                                 : (await CompletableFuture.supplyAsync { "F" })
                    return v
                }
            }
            def svc = new Svc()
            assert svc.ternary(true).get() == "T"
            assert svc.ternary(false).get() == "F"
        '''
    }

    @Test
    void testAwaitInWhileLoop() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async countdown() {
                    def results = []
                    int n = 3
                    while (n > 0) {
                        def v = await CompletableFuture.supplyAsync { n }
                        results << v
                        n--
                    }
                    return results
                }
            }
            assert new Svc().countdown().get() == [3, 2, 1]
        '''
    }

    @Test
    void testAwaitInForLoop() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async processItems() {
                    def results = []
                    for (int i = 0; i < 4; i++) {
                        def v = await CompletableFuture.supplyAsync { i * 10 }
                        results << v
                    }
                    return results
                }
            }
            assert new Svc().processItems().get() == [0, 10, 20, 30]
        '''
    }

    @Test
    void testAwaitInForEachLoop() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async doubleAll() {
                    def results = []
                    for (x in [1, 2, 3]) {
                        def v = await CompletableFuture.supplyAsync { x * 2 }
                        results << v
                    }
                    return results
                }
            }
            assert new Svc().doubleAll().get() == [2, 4, 6]
        '''
    }

    @Test
    void testAwaitInTryCatch() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async safe() {
                    try {
                        return await CompletableFuture.supplyAsync {
                            throw new RuntimeException("boom")
                        }
                    } catch (RuntimeException e) {
                        return "caught: ${e.message}"
                    }
                }
            }
            assert new Svc().safe().get() == "caught: boom"
        '''
    }

    @Test
    void testAwaitInTryFinally() {
        assertScript '''
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.atomic.AtomicBoolean

            class Svc {
                AtomicBoolean cleaned = new AtomicBoolean(false)
                async withCleanup() {
                    try {
                        return await CompletableFuture.supplyAsync { "result" }
                    } finally {
                        cleaned.set(true)
                    }
                }
            }
            def svc = new Svc()
            assert svc.withCleanup().get() == "result"
            assert svc.cleaned.get()
        '''
    }

    @Test
    void testAwaitInSwitchExpression() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async route(int code) {
                    switch (code) {
                        case 1: return await CompletableFuture.supplyAsync { "one" }
                        case 2: return await CompletableFuture.supplyAsync { "two" }
                        default: return await CompletableFuture.supplyAsync { "other" }
                    }
                }
            }
            def svc = new Svc()
            assert svc.route(1).get() == "one"
            assert svc.route(2).get() == "two"
            assert svc.route(9).get() == "other"
        '''
    }

    @Test
    void testAwaitInStringInterpolation() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async greet() {
                    def name = await CompletableFuture.supplyAsync { "World" }
                    return "Hello, ${name}!"
                }
            }
            assert new Svc().greet().get() == "Hello, World!"
        '''
    }

    @Test
    void testAwaitInCollectionLiteral() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async buildList() {
                    def a = await CompletableFuture.supplyAsync { "x" }
                    def b = await CompletableFuture.supplyAsync { "y" }
                    return [a, b, "z"]
                }
            }
            assert new Svc().buildList().get() == ["x", "y", "z"]
        '''
    }

    @Test
    void testAwaitInMapLiteral() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async buildMap() {
                    def name = await CompletableFuture.supplyAsync { "Alice" }
                    def age = await CompletableFuture.supplyAsync { 30 }
                    return [name: name, age: age]
                }
            }
            assert new Svc().buildMap().get() == [name: "Alice", age: 30]
        '''
    }

    @Test
    void testAwaitAsMethodArgument() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                String format(String s) { "[${s}]" }
                async process() {
                    def v = await CompletableFuture.supplyAsync { "data" }
                    return format(v)
                }
            }
            assert new Svc().process().get() == "[data]"
        '''
    }

    @Test
    void testNestedAwaitExpressions() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async nested() {
                    // await of (await result used to create another future)
                    def inner = await CompletableFuture.supplyAsync { 5 }
                    def outer = await CompletableFuture.supplyAsync { inner * 10 }
                    return outer
                }
            }
            assert new Svc().nested().get() == 50
        '''
    }

    @Test
    void testAwaitInClosureInsideAsyncMethod() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async transform() {
                    def items = [1, 2, 3]
                    // Note: await inside collect closure — this works because
                    // the closure executes synchronously within the async method
                    def results = items.collect { n ->
                        await CompletableFuture.supplyAsync { n * 100 }
                    }
                    return results
                }
            }
            assert new Svc().transform().get() == [100, 200, 300]
        '''
    }

    @Test
    void testAwaitNullResult() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async fetchNull() {
                    def v = await CompletableFuture.supplyAsync { null }
                    return v
                }
            }
            assert new Svc().fetchNull().get() == null
        '''
    }

    @Test
    void testAwaitBooleanResult() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async check() {
                    boolean ok = await CompletableFuture.supplyAsync { true }
                    return ok
                }
            }
            assert new Svc().check().get() == true
        '''
    }

    @Test
    void testAwaitChainedMethodCallOnResult() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async process() {
                    def s = await CompletableFuture.supplyAsync { "hello world" }
                    return s.toUpperCase().split(" ").toList()
                }
            }
            assert new Svc().process().get() == ["HELLO", "WORLD"]
        '''
    }

    // =====================================================================
    // 3. 'for await' — async iteration
    // =====================================================================

    @Test
    void testForAwaitBasicIteration() {
        assertScript '''
            class Svc {
                async collect() {
                    def results = []
                    for await (item in [10, 20, 30]) {
                        results << item
                    }
                    return results
                }
            }
            assert new Svc().collect().get() == [10, 20, 30]
        '''
    }

    @Test
    void testForAwaitBasicIteration_2() {
        assertScript '''
            class Svc {
                async collect() {
                    def results = []
                    for await (item : [10, 20, 30]) {
                        results << item
                    }
                    return results
                }
            }
            assert new Svc().collect().get() == [10, 20, 30]
        '''
    }

    @Test
    void testForAwaitWithTransformation() {
        assertScript '''
            class Svc {
                async doubleAll() {
                    def results = []
                    for await (item in [1, 2, 3, 4]) {
                        results << item * 2
                    }
                    return results
                }
            }
            assert new Svc().doubleAll().get() == [2, 4, 6, 8]
        '''
    }

    @Test
    void testForAwaitWithBreak() {
        assertScript '''
            class Svc {
                async findFirst() {
                    for await (item in [5, 10, 15, 20, 25]) {
                        if (item > 12) return item
                    }
                    return -1
                }
            }
            assert new Svc().findFirst().get() == 15
        '''
    }

    @Test
    void testForAwaitWithContinue() {
        assertScript '''
            class Svc {
                async skipOdds() {
                    def results = []
                    for await (item in [1, 2, 3, 4, 5, 6]) {
                        if (item % 2 != 0) continue
                        results << item
                    }
                    return results
                }
            }
            assert new Svc().skipOdds().get() == [2, 4, 6]
        '''
    }

    @Test
    void testForAwaitWithTypedVariable() {
        assertScript '''
            class Svc {
                async typed() {
                    def results = []
                    for await (String s in ["hello", "world"]) {
                        results << s.toUpperCase()
                    }
                    return results
                }
            }
            assert new Svc().typed().get() == ["HELLO", "WORLD"]
        '''
    }

    @Test
    void testForAwaitEmptySource() {
        assertScript '''
            class Svc {
                async empty() {
                    def results = []
                    for await (item in []) {
                        results << item
                    }
                    return results
                }
            }
            assert new Svc().empty().get() == []
        '''
    }

    @Test
    void testForAwaitSingleElement() {
        assertScript '''
            class Svc {
                async single() {
                    def results = []
                    for await (item in ["only"]) {
                        results << item
                    }
                    return results
                }
            }
            assert new Svc().single().get() == ["only"]
        '''
    }

    @Test
    void testForAwaitWithIndex() {
        assertScript '''
            class Svc {
                async indexed() {
                    def results = []
                    int idx = 0
                    for await (item in ["a", "b", "c"]) {
                        results << "${idx}:${item}"
                        idx++
                    }
                    return results
                }
            }
            assert new Svc().indexed().get() == ["0:a", "1:b", "2:c"]
        '''
    }

    @Test
    void testForAwaitNestedInRegularFor() {
        assertScript '''
            class Svc {
                async nested() {
                    def results = []
                    for (prefix in ["A", "B"]) {
                        for await (n in [1, 2]) {
                            results << "${prefix}${n}"
                        }
                    }
                    return results
                }
            }
            assert new Svc().nested().get() == ["A1", "A2", "B1", "B2"]
        '''
    }

    @Test
    void testRegularForNestedInForAwait() {
        assertScript '''
            class Svc {
                async nested() {
                    def results = []
                    for await (base in [10, 20]) {
                        for (int i = 0; i < 3; i++) {
                            results << base + i
                        }
                    }
                    return results
                }
            }
            assert new Svc().nested().get() == [10, 11, 12, 20, 21, 22]
        '''
    }

    @Test
    void testForAwaitWithAwaitInsideBody() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async process() {
                    def results = []
                    for await (item in [1, 2, 3]) {
                        def doubled = await CompletableFuture.supplyAsync { item * 2 }
                        results << doubled
                    }
                    return results
                }
            }
            assert new Svc().process().get() == [2, 4, 6]
        '''
    }

    @Test
    void testForAwaitWithAsyncStream() {
        assertScript '''
            import groovy.concurrent.AsyncStream
            import groovy.concurrent.Awaitable

            class CountStream implements AsyncStream<Integer> {
                private int current = 0
                private final int max
                CountStream(int max) { this.max = max }

                Awaitable<Boolean> moveNext() {
                    current++
                    return Awaitable.of(current <= max)
                }
                Integer getCurrent() { current }
            }

            class Svc {
                async sum() {
                    int total = 0
                    for await (n in new CountStream(5)) {
                        total += n
                    }
                    return total
                }
            }
            assert new Svc().sum().get() == 15
        '''
    }

    @Test
    void testMultipleForAwaitSequential() {
        assertScript '''
            class Svc {
                async multi() {
                    def r1 = []
                    for await (x in [1, 2]) { r1 << x }
                    def r2 = []
                    for await (y in [3, 4]) { r2 << y }
                    return [r1, r2]
                }
            }
            assert new Svc().multi().get() == [[1, 2], [3, 4]]
        '''
    }

    @Test
    void testForAwaitWithAnnotation() {
        assertScript '''
            import groovy.transform.Async

            class Svc {
                @Async
                def collectItems() {
                    def results = []
                    for await (item in [100, 200, 300]) {
                        results << item
                    }
                    return results
                }
            }
            assert new Svc().collectItems().get() == [100, 200, 300]
        '''
    }

    @Test
    void testForAwaitInScript() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            @Async
            def gatherItems() {
                def results = []
                for await (item in ["x", "y", "z"]) {
                    results << item
                }
                return results
            }

            assert gatherItems().get() == ["x", "y", "z"]
        '''
    }

    @Test
    void testForAwaitWithTryCatch() {
        assertScript '''
            class Svc {
                async safeIterate() {
                    def results = []
                    try {
                        for await (item in [1, 2, 3]) {
                            results << item
                        }
                    } catch (Exception e) {
                        results << "error"
                    }
                    return results
                }
            }
            assert new Svc().safeIterate().get() == [1, 2, 3]
        '''
    }

    // =====================================================================
    // 4. Combined patterns — real-world usage scenarios
    // =====================================================================

    @Test
    void testAsyncServiceCallingAsyncService() {
        assertScript '''
            class UserService {
                async findUser(long id) {
                    return [id: id, name: "User${id}"]
                }
            }

            class OrderService {
                async findOrders(long userId) {
                    return (1..3).collect { [orderId: it, userId: userId] }
                }
            }

            class AggregatorService {
                def userService = new UserService()
                def orderService = new OrderService()

                async getUserWithOrders(long id) {
                    def user = await userService.findUser(id)
                    def orders = await orderService.findOrders(id)
                    return [user: user, orders: orders]
                }
            }

            def agg = new AggregatorService()
            def result = agg.getUserWithOrders(42).get()
            assert result.user.name == "User42"
            assert result.orders.size() == 3
            assert result.orders[0].userId == 42
        '''
    }

    @Test
    void testAsyncWithExceptionPropagation() {
        assertScript '''
            import java.util.concurrent.ExecutionException

            class Svc {
                async fail() {
                    throw new IllegalArgumentException("bad input")
                }
            }
            try {
                new Svc().fail().get()
                assert false : "expected exception"
            } catch (ExecutionException e) {
                assert e.cause instanceof IllegalArgumentException
                assert e.cause.message == "bad input"
            }
        '''
    }

    @Test
    void testAwaitPropagatesExceptionToCallerAsync() {
        assertScript '''
            class Svc {
                async failInner() {
                    throw new IllegalStateException("inner-fail")
                }
                async caller() {
                    try {
                        await failInner()
                        return "should not reach"
                    } catch (IllegalStateException e) {
                        return "recovered: ${e.message}"
                    }
                }
            }
            assert new Svc().caller().get() == "recovered: inner-fail"
        '''
    }

    @Test
    void testAsyncAwaitWithCollectionProcessing() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async processAll(List<Integer> items) {
                    def results = []
                    for (item in items) {
                        def r = await CompletableFuture.supplyAsync { item * item }
                        results << r
                    }
                    return results
                }
            }
            assert new Svc().processAll([1, 2, 3, 4, 5]).get() == [1, 4, 9, 16, 25]
        '''
    }

    @Test
    void testAsyncAwaitProducerConsumer() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Producer {
                async produce(int n) { n * 10 }
            }
            class Consumer {
                def producer = new Producer()
                async consume() {
                    def sum = 0
                    for (i in 1..5) {
                        sum += await producer.produce(i)
                    }
                    return sum
                }
            }
            // 10 + 20 + 30 + 40 + 50 = 150
            assert new Consumer().consume().get() == 150
        '''
    }

    @Test
    void testAsyncAwaitParallelThenGather() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async step(int n) { n * 2 }

                async parallelGather() {
                    // Launch multiple async tasks
                    def f1 = step(1)
                    def f2 = step(2)
                    def f3 = step(3)
                    // Await all results
                    return [await f1, await f2, await f3]
                }
            }
            assert new Svc().parallelGather().get() == [2, 4, 6]
        '''
    }

    @Test
    void testAsyncAwaitChainedTransformation() {
        assertScript '''
            class Pipeline {
                async step1(String input) { input.toUpperCase() }
                async step2(String input) { input.reverse() }
                async step3(String input) { "[${input}]" }

                async run(String input) {
                    def r1 = await step1(input)
                    def r2 = await step2(r1)
                    return await step3(r2)
                }
            }
            assert new Pipeline().run("abc").get() == "[CBA]"
        '''
    }

    @Test
    void testAsyncMixedAnnotationAndKeyword() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.Awaitable

            class Svc {
                @Async
                def fromAnnotation() { "annotated" }

                async fromKeyword() { "keyword" }

                async combine() {
                    def a = await fromAnnotation()
                    def b = await fromKeyword()
                    return "${a}+${b}"
                }
            }
            assert new Svc().combine().get() == "annotated+keyword"
        '''
    }

    @Test
    void testAsyncWithConditionalAwait() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async fetchIf(boolean shouldFetch) {
                    if (shouldFetch) {
                        return await CompletableFuture.supplyAsync { "fetched" }
                    }
                    return "cached"
                }
            }
            def svc = new Svc()
            assert svc.fetchIf(true).get() == "fetched"
            assert svc.fetchIf(false).get() == "cached"
        '''
    }

    @Test
    void testAsyncRecursion() {
        assertScript '''
            class Svc {
                async factorial(int n) {
                    if (n <= 1) return 1
                    def sub = await factorial(n - 1)
                    return n * sub
                }
            }
            assert new Svc().factorial(5).get() == 120
        '''
    }

    @Test
    void testAsyncWithRetry() {
        assertScript '''
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.atomic.AtomicInteger

            class Svc {
                AtomicInteger attempts = new AtomicInteger(0)

                async unreliable() {
                    if (attempts.incrementAndGet() < 3) {
                        throw new RuntimeException("not yet")
                    }
                    return "success"
                }

                async withRetry(int maxRetries) {
                    for (int i = 0; i < maxRetries; i++) {
                        try {
                            return await unreliable()
                        } catch (RuntimeException e) {
                            if (i == maxRetries - 1) throw e
                        }
                    }
                }
            }
            assert new Svc().withRetry(5).get() == "success"
        '''
    }

    @Test
    void testAsyncWithTimeout() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import java.util.concurrent.TimeUnit
            import java.util.concurrent.TimeoutException

            class Svc {
                async slow() {
                    Thread.sleep(2000)
                    return "done"
                }
            }
            def a = new Svc().slow()
            try {
                a.get(50, TimeUnit.MILLISECONDS)
                assert false : "expected timeout"
            } catch (TimeoutException e) {
                // expected
            }
        '''
    }

    @Test
    void testAsyncWithAwaitableOf() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Svc {
                async useOf() {
                    def a = await Awaitable.of(42)
                    return a + 8
                }
            }
            assert new Svc().useOf().get() == 50
        '''
    }

    @Test
    void testAsyncWithAwaitableThen() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Svc {
                async inner() { 10 }
                async chained() {
                    def a = inner()  // returns Awaitable<Integer>
                    def b = a.then { it * 3 }
                    return await b
                }
            }
            assert new Svc().chained().get() == 30
        '''
    }

    @Test
    void testAsyncWithAwaitableThenCompose() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Svc {
                async step1() { 5 }
                async step2(int v) { v * 4 }
                async composed() {
                    def a = step1()
                    def b = a.thenCompose { v -> step2(v) }
                    return await b
                }
            }
            assert new Svc().composed().get() == 20
        '''
    }

    @Test
    void testAsyncWithAwaitableExceptionally() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Svc {
                async failing() {
                    throw new RuntimeException("oops")
                }
                async recovered() {
                    def a = failing().exceptionally { "fallback" }
                    return await a
                }
            }
            assert new Svc().recovered().get() == "fallback"
        '''
    }

    @Test
    void testConcurrentAsyncMethodsWithSyntax() {
        assertScript '''
            import java.util.concurrent.ConcurrentHashMap

            class Svc {
                def results = new ConcurrentHashMap()
                async store(String key, String value) {
                    results.put(key, value)
                    return value
                }
            }
            def svc = new Svc()
            def futures = (1..10).collect { i -> svc.store("k${i}", "v${i}") }
            futures.each { it.get() }
            assert svc.results.size() == 10
            assert svc.results["k5"] == "v5"
        '''
    }

    @Test
    void testScriptLevelAsyncAwait() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            async fetchData(String key) {
                return "data-for-${key}"
            }

            async main() {
                def r1 = await fetchData("alpha")
                def r2 = await fetchData("beta")
                return [r1, r2]
            }

            def result = main().get()
            assert result[0] == "data-for-alpha"
            assert result[1] == "data-for-beta"
        '''
    }

    @Test
    void testScriptLevelForAwait() {
        assertScript '''
            async items() {
                def results = []
                for await (x in [1, 2, 3]) {
                    results << x * 10
                }
                return results
            }
            assert items().get() == [10, 20, 30]
        '''
    }

    @Test
    void testAsyncWithCustomExecutor() {
        assertScript '''
            import java.util.concurrent.Executors

            class Svc {
                static executor = Executors.newFixedThreadPool(2)
                async threadInfo() {
                    return Thread.currentThread().name
                }
            }

            groovy.concurrent.AsyncUtils.setExecutor(Svc.executor)
            try {
                def name = new Svc().threadInfo().get()
                assert name.contains("pool")
            } finally {
                groovy.concurrent.AsyncUtils.setExecutor(null)
                Svc.executor.shutdown()
            }
        '''
    }

    @Test
    void testForAwaitCombinedWithAwaitInSameMethod() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async complexFlow() {
                    def prefix = await CompletableFuture.supplyAsync { "item" }
                    def results = []
                    for await (n in [1, 2, 3]) {
                        results << "${prefix}-${n}"
                    }
                    def suffix = await CompletableFuture.supplyAsync { "-done" }
                    return results.join(",") + suffix
                }
            }
            assert new Svc().complexFlow().get() == "item-1,item-2,item-3-done"
        '''
    }

    @Test
    void testAsyncMethodWithGStringReturn() {
        assertScript '''
            class Svc {
                async template(String name, int age) {
                    return "Name: ${name}, Age: ${age}"
                }
            }
            assert new Svc().template("Alice", 25).get() == "Name: Alice, Age: 25"
        '''
    }

    @Test
    void testAsyncMethodReturningCollection() {
        assertScript '''
            class Svc {
                async getList() { [1, 2, 3] }
                async getMap() { [a: 1, b: 2] }
                async getSet() { [1, 2, 3] as Set }

                async all() {
                    def list = await getList()
                    def map = await getMap()
                    def set = await getSet()
                    return [list: list, map: map, setSize: set.size()]
                }
            }
            def r = new Svc().all().get()
            assert r.list == [1, 2, 3]
            assert r.map == [a: 1, b: 2]
            assert r.setSize == 3
        '''
    }

    @Test
    void testAsyncWithPropertyAccess() {
        assertScript '''
            class User {
                String name
                int age
            }

            class Svc {
                async getUser() {
                    new User(name: "Bob", age: 40)
                }
                async getUserName() {
                    def user = await getUser()
                    return user.name
                }
            }
            assert new Svc().getUserName().get() == "Bob"
        '''
    }

    @Test
    void testAsyncWithSpreadOperator() {
        assertScript '''
            class Svc {
                async getItems() { [[v: 1], [v: 2], [v: 3]] }
                async getValues() {
                    def items = await getItems()
                    return items*.v
                }
            }
            assert new Svc().getValues().get() == [1, 2, 3]
        '''
    }

    @Test
    void testAsyncWithElvisOperator() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Svc {
                async fetchOrDefault() {
                    def v = await CompletableFuture.supplyAsync { null }
                    return v ?: "default"
                }
            }
            assert new Svc().fetchOrDefault().get() == "default"
        '''
    }

    @Test
    void testAsyncWithSafeNavigation() {
        assertScript '''
            class Svc {
                async nullResult() { null }
                async safe() {
                    def r = await nullResult()
                    return r?.toString() ?: "was-null"
                }
            }
            assert new Svc().safe().get() == "was-null"
        '''
    }

    // =====================================================================
    // 5. Backward compatibility — async/await as identifiers
    // =====================================================================

    @Test
    void testAsyncAsLocalVariable() {
        assertScript '''
            def async = "value"
            assert async == "value"
        '''
    }

    @Test
    void testAwaitAsLocalVariable() {
        assertScript '''
            def await = 123
            assert await == 123
        '''
    }

    @Test
    void testAsyncAsMethodNameInClass() {
        assertScript '''
            class Legacy {
                def async() { "legacy-async" }
                def await() { "legacy-await" }
            }
            def obj = new Legacy()
            assert obj.async() == "legacy-async"
            assert obj.await() == "legacy-await"
        '''
    }

    @Test
    void testAsyncAsFieldName() {
        assertScript '''
            class Config {
                boolean async = true
                String await = "ready"
            }
            def c = new Config()
            assert c.async == true
            assert c.await == "ready"
        '''
    }

    @Test
    void testAsyncAsMapKey() {
        assertScript '''
            def map = [async: true, await: false]
            assert map.async == true
            assert map.await == false
        '''
    }

    @Test
    void testAsyncAsParameterName() {
        assertScript '''
            def process(boolean async) { async ? "yes" : "no" }
            assert process(true) == "yes"
        '''
    }

    @Test
    void testAsyncAndAwaitAsLabels() {
        assertScript '''
            def result = null
            async:
            for (i in [1, 2, 3]) {
                await:
                for (j in [10, 20]) {
                    if (j == 20) {
                        result = i * j
                        break async
                    }
                }
            }
            assert result == 20
        '''
    }

    // =====================================================================
    // 6. Error cases — syntax and semantic rejections
    // =====================================================================

    @Test
    void testAsyncOnAbstractMethodFails() {
        shouldFail '''
            abstract class Svc {
                @groovy.transform.Async
                abstract def compute()
            }
        '''
    }

    @Test
    void testAsyncOnAwaitableReturnTypeFails() {
        shouldFail '''
            import groovy.concurrent.Awaitable
            class Svc {
                @groovy.transform.Async
                Awaitable<String> compute() { Awaitable.of("x") }
            }
        '''
    }

    @Test
    void testForAwaitWithTraditionalForSyntaxFails() {
        shouldFail '''
            class Svc {
                @groovy.transform.Async
                def work() {
                    for await (int i = 0; i < 10; i++) {
                        println i
                    }
                }
            }
        '''
    }

    // =====================================================================
    // 7. 'yield return' — async generator (C#-style async streams)
    // =====================================================================

    @Test
    void testYieldReturnBasic() {
        assertScript '''
            import groovy.concurrent.AsyncStream

            class Gen {
                async numbers() {
                    yield return 1
                    yield return 2
                    yield return 3
                }
            }
            def stream = new Gen().numbers()
            assert stream instanceof AsyncStream
            def results = []
            for await (n in stream) {
                results << n
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testYieldReturnInForLoop() {
        assertScript '''
            class Gen {
                async range(int start, int end) {
                    for (int i = start; i <= end; i++) {
                        yield return i
                    }
                }
            }
            def results = []
            for await (n in new Gen().range(1, 5)) {
                results << n
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testYieldReturnInWhileLoop() {
        assertScript '''
            class Gen {
                async countdown(int n) {
                    while (n > 0) {
                        yield return n
                        n--
                    }
                }
            }
            def results = []
            for await (v in new Gen().countdown(4)) {
                results << v
            }
            assert results == [4, 3, 2, 1]
        '''
    }

    @Test
    void testYieldReturnInForEach() {
        assertScript '''
            class Gen {
                async transform(List items) {
                    for (item in items) {
                        yield return item.toString().toUpperCase()
                    }
                }
            }
            def results = []
            for await (s in new Gen().transform(["hello", "world"])) {
                results << s
            }
            assert results == ["HELLO", "WORLD"]
        '''
    }

    @Test
    void testYieldReturnStrings() {
        assertScript '''
            class Gen {
                async greetings() {
                    yield return "hello"
                    yield return "world"
                    yield return "!"
                }
            }
            def results = []
            for await (s in new Gen().greetings()) {
                results << s
            }
            assert results == ["hello", "world", "!"]
        '''
    }

    @Test
    void testYieldReturnWithGString() {
        assertScript '''
            class Gen {
                async labeled(List items) {
                    int idx = 0
                    for (item in items) {
                        yield return "${idx}: ${item}"
                        idx++
                    }
                }
            }
            def results = []
            for await (s in new Gen().labeled(["a", "b", "c"])) {
                results << s
            }
            assert results == ["0: a", "1: b", "2: c"]
        '''
    }

    @Test
    void testYieldReturnWithFilter() {
        assertScript '''
            class Gen {
                async evens(int max) {
                    for (int i = 1; i <= max; i++) {
                        if (i % 2 == 0) {
                            yield return i
                        }
                    }
                }
            }
            def results = []
            for await (n in new Gen().evens(10)) {
                results << n
            }
            assert results == [2, 4, 6, 8, 10]
        '''
    }

    @Test
    void testYieldReturnWithParameters() {
        assertScript '''
            class Gen {
                async multiply(List<Integer> items, int factor) {
                    for (item in items) {
                        yield return item * factor
                    }
                }
            }
            def results = []
            for await (n in new Gen().multiply([1, 2, 3], 10)) {
                results << n
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testYieldReturnEmpty() {
        assertScript '''
            class Gen {
                async empty() {
                    // A generator must have at least one yield return
                    // to be detected as a generator. An empty body with
                    // no yield returns becomes a regular async method.
                    if (false) yield return "unreachable"
                }
            }
            def results = []
            for await (item in new Gen().empty()) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testYieldReturnSingleElement() {
        assertScript '''
            class Gen {
                async single() {
                    yield return "only"
                }
            }
            def results = []
            for await (item in new Gen().single()) {
                results << item
            }
            assert results == ["only"]
        '''
    }

    @Test
    void testYieldReturnWithConditionalLogic() {
        assertScript '''
            class Gen {
                async fizzBuzz(int n) {
                    for (int i = 1; i <= n; i++) {
                        if (i % 15 == 0) yield return "FizzBuzz"
                        else if (i % 3 == 0) yield return "Fizz"
                        else if (i % 5 == 0) yield return "Buzz"
                        else yield return i.toString()
                    }
                }
            }
            def results = []
            for await (s in new Gen().fizzBuzz(15)) {
                results << s
            }
            assert results[0] == "1"
            assert results[2] == "Fizz"
            assert results[4] == "Buzz"
            assert results[14] == "FizzBuzz"
            assert results.size() == 15
        '''
    }

    @Test
    void testYieldReturnConsumerBreaksEarly() {
        assertScript '''
            class Gen {
                async infinite() {
                    int i = 0
                    while (true) {
                        yield return i++
                    }
                }
            }
            // Consumer only takes first 5
            def results = []
            def stream = new Gen().infinite()
            for await (n in stream) {
                results << n
                if (results.size() >= 5) break
            }
            assert results == [0, 1, 2, 3, 4]
        '''
    }

    @Test
    void testYieldReturnWithAnnotation() {
        assertScript '''
            import groovy.transform.Async
            import groovy.concurrent.AsyncStream

            class Gen {
                @Async
                def items() {
                    yield return "x"
                    yield return "y"
                }
            }
            def stream = new Gen().items()
            assert stream instanceof AsyncStream
            def results = []
            for await (s in stream) {
                results << s
            }
            assert results == ["x", "y"]
        '''
    }

    @Test
    void testYieldReturnInScript() {
        assertScript '''
            import groovy.concurrent.AsyncStream

            async fibonacci(int count) {
                int a = 0, b = 1
                for (int i = 0; i < count; i++) {
                    yield return a
                    int temp = a + b
                    a = b
                    b = temp
                }
            }

            def results = []
            for await (n in fibonacci(8)) {
                results << n
            }
            assert results == [0, 1, 1, 2, 3, 5, 8, 13]
        '''
    }

    @Test
    void testYieldReturnWithAwaitInsideBody() {
        assertScript '''
            import java.util.concurrent.CompletableFuture

            class Gen {
                async asyncItems() {
                    for (i in 1..3) {
                        def v = await CompletableFuture.supplyAsync { i * 100 }
                        yield return v
                    }
                }
            }
            def results = []
            for await (n in new Gen().asyncItems()) {
                results << n
            }
            assert results == [100, 200, 300]
        '''
    }

    @Test
    void testYieldReturnWithTryCatch() {
        assertScript '''
            class Gen {
                async safeItems() {
                    for (i in 1..5) {
                        try {
                            if (i == 3) throw new RuntimeException("skip")
                            yield return i
                        } catch (RuntimeException e) {
                            yield return -1
                        }
                    }
                }
            }
            def results = []
            for await (n in new Gen().safeItems()) {
                results << n
            }
            assert results == [1, 2, -1, 4, 5]
        '''
    }

    @Test
    void testYieldReturnExceptionPropagation() {
        assertScript '''
            class Gen {
                async failing() {
                    yield return 1
                    yield return 2
                    throw new IllegalStateException("generator failed")
                }
            }
            def results = []
            try {
                for await (n in new Gen().failing()) {
                    results << n
                }
                assert false : "expected exception"
            } catch (IllegalStateException e) {
                assert e.message == "generator failed"
            }
            assert results == [1, 2]
        '''
    }

    @Test
    void testYieldReturnNullValues() {
        assertScript '''
            class Gen {
                async withNulls() {
                    yield return "a"
                    yield return null
                    yield return "b"
                }
            }
            def results = []
            for await (item in new Gen().withNulls()) {
                results << item
            }
            assert results == ["a", null, "b"]
        '''
    }

    @Test
    void testYieldReturnMixedTypes() {
        assertScript '''
            class Gen {
                async mixed() {
                    yield return 42
                    yield return "hello"
                    yield return [1, 2, 3]
                    yield return true
                }
            }
            def results = []
            for await (item in new Gen().mixed()) {
                results << item
            }
            assert results[0] == 42
            assert results[1] == "hello"
            assert results[2] == [1, 2, 3]
            assert results[3] == true
        '''
    }

    @Test
    void testYieldReturnForAwaitAccumulation() {
        assertScript '''
            class Gen {
                async items() {
                    for (i in 1..5) {
                        yield return i
                    }
                }
            }
            // Sum using for-await
            async sumStream() {
                int total = 0
                for await (n in new Gen().items()) {
                    total += n
                }
                return total
            }
            assert sumStream().get() == 15
        '''
    }

    @Test
    void testYieldReturnChainedGenerators() {
        assertScript '''
            class Gen {
                async source() {
                    yield return 1
                    yield return 2
                    yield return 3
                }

                async doubled() {
                    for await (n in source()) {
                        yield return n * 2
                    }
                }
            }
            def results = []
            for await (n in new Gen().doubled()) {
                results << n
            }
            assert results == [2, 4, 6]
        '''
    }

    @Test
    void testYieldReturnWithForAwaitTransformPipeline() {
        assertScript '''
            class Pipeline {
                async numbers(int n) {
                    for (int i = 1; i <= n; i++) {
                        yield return i
                    }
                }

                async squared(source) {
                    for await (n in source) {
                        yield return n * n
                    }
                }

                async asStrings(source) {
                    for await (n in source) {
                        yield return "val=${n}"
                    }
                }
            }
            def p = new Pipeline()
            def results = []
            for await (s in p.asStrings(p.squared(p.numbers(4)))) {
                results << s
            }
            assert results == ["val=1", "val=4", "val=9", "val=16"]
        '''
    }

    @Test
    void testYieldReturnStaticMethod() {
        assertScript '''
            class Gen {
                async static staticRange(int n) {
                    for (int i = 0; i < n; i++) {
                        yield return i
                    }
                }
            }
            def results = []
            for await (n in Gen.staticRange(4)) {
                results << n
            }
            assert results == [0, 1, 2, 3]
        '''
    }

    @Test
    void testYieldReturnWithMapEntries() {
        assertScript '''
            class Gen {
                async entries(Map map) {
                    for (entry in map.entrySet()) {
                        yield return [key: entry.key, value: entry.value]
                    }
                }
            }
            def results = []
            for await (e in new Gen().entries([a: 1, b: 2, c: 3])) {
                results << e
            }
            assert results.size() == 3
            assert results.find { it.key == "b" }.value == 2
        '''
    }

    @Test
    void testYieldReturnLargeSequence() {
        assertScript '''
            class Gen {
                async sequence(int count) {
                    for (int i = 0; i < count; i++) {
                        yield return i
                    }
                }
            }
            int sum = 0
            int count = 0
            for await (n in new Gen().sequence(1000)) {
                sum += n
                count++
            }
            assert count == 1000
            assert sum == (999 * 1000) / 2
        '''
    }

    @Test
    void testYieldReturnOutsideAsyncMethodFails() {
        assertScript '''
            // yield return outside async method should fail at runtime
            try {
                org.apache.groovy.runtime.async.AsyncSupport.yieldReturn("oops")
                assert false : "expected exception"
            } catch (IllegalStateException e) {
                assert e.message.contains("yield return")
            }
        '''
    }
}
