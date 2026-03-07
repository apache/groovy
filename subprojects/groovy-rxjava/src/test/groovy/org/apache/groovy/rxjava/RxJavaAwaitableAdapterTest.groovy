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
package org.apache.groovy.rxjava

import groovy.concurrent.AsyncStream
import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitableAdapter
import groovy.concurrent.AwaitableAdapterRegistry
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Comprehensive integration tests for the {@link RxJavaAwaitableAdapter},
 * verifying that RxJava 3's {@link Single}, {@link Maybe}, {@link Observable},
 * and {@link Flowable} types work seamlessly with Groovy's
 * {@code async}/{@code await} system.
 * <p>
 * The adapter is auto-discovered via {@link java.util.ServiceLoader}, so
 * these tests validate the end-to-end integration path — from classpath
 * discovery through to actual async/await execution.
 *
 * @see RxJavaAwaitableAdapter
 */
class RxJavaAwaitableAdapterTest {

    /** Shared import preamble for RxJava-based assertScript tests. */
    private static final String PREAMBLE = '''\
import groovy.concurrent.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
'''

    // =====================================================================
    // ServiceLoader auto-discovery
    // =====================================================================

    // tag::serviceloader_discovery[]
    @Test
    void testServiceLoaderDiscovery() {
        def adapters = ServiceLoader.load(AwaitableAdapter).toList()
        assert adapters.any { it instanceof RxJavaAwaitableAdapter }
    }
    // end::serviceloader_discovery[]

    // tag::serviceloader_single_await[]
    @Test
    void testAutoDiscoveredAdapterAwaitsSingle() {
        assertScript PREAMBLE + '''
            def single = Single.just("auto-discovered")
            def result = await single
            assert result == "auto-discovered"
        '''
    }
    // end::serviceloader_single_await[]

    // =====================================================================
    // Single → Awaitable (single-value await)
    // =====================================================================

    // tag::await_single_basic[]
    @Test
    void testAwaitRxJavaSingle() {
        assertScript PREAMBLE + '''
            def single = Single.just("rxjava-value")
            def result = await single
            assert result == "rxjava-value"
        '''
    }
    // end::await_single_basic[]

    // tag::await_single_map[]
    @Test
    void testAwaitRxJavaSingleWithMap() {
        assertScript PREAMBLE + '''
            def single = Single.just(7).map { it * 6 }
            def result = await single
            assert result == 42
        '''
    }
    // end::await_single_map[]

    // tag::await_single_error[]
    @Test
    void testAwaitRxJavaSingleError() {
        assertScript PREAMBLE + '''
            def single = Single.error(new RuntimeException("rx error"))
            try {
                await single
                assert false : "should have thrown"
            } catch (RuntimeException e) {
                assert e.message == "rx error"
            }
        '''
    }
    // end::await_single_error[]

    @Test
    void testAwaitRxJavaSingleFromCallable() {
        assertScript PREAMBLE + '''
            def single = Single.fromCallable { "from-callable" }
            def result = await single
            assert result == "from-callable"
        '''
    }

    @Test
    void testAwaitRxJavaSingleDeferred() {
        assertScript PREAMBLE + '''
            def single = Single.defer { Single.just("deferred") }
            def result = await single
            assert result == "deferred"
        '''
    }

    @Test
    void testAwaitRxJavaSingleZip() {
        assertScript PREAMBLE + '''
            def s1 = Single.just(10)
            def s2 = Single.just(20)
            def zipped = Single.zip(s1, s2) { a, b -> a + b }
            def result = await zipped
            assert result == 30
        '''
    }

    @Test
    void testAwaitRxJavaSingleFlatMap() {
        assertScript PREAMBLE + '''
            def single = Single.just("hello")
                .map { it.toUpperCase() }
                .flatMap { s -> Single.just("${s}!") }
            def result = await single
            assert result == "HELLO!"
        '''
    }

    // =====================================================================
    // Maybe → Awaitable
    // =====================================================================

    // tag::await_maybe_basic[]
    @Test
    void testAwaitRxJavaMaybe() {
        assertScript PREAMBLE + '''
            def maybe = Maybe.just("maybe-value")
            def result = await maybe
            assert result == "maybe-value"
        '''
    }
    // end::await_maybe_basic[]

    // tag::await_maybe_empty[]
    @Test
    void testAwaitRxJavaMaybeEmpty() {
        assertScript PREAMBLE + '''
            def maybe = Maybe.empty()
            def result = await maybe
            assert result == null
        '''
    }
    // end::await_maybe_empty[]

    @Test
    void testAwaitRxJavaMaybeError() {
        assertScript PREAMBLE + '''
            def maybe = Maybe.error(new IllegalStateException("maybe error"))
            try {
                await maybe
                assert false : "should have thrown"
            } catch (IllegalStateException e) {
                assert e.message == "maybe error"
            }
        '''
    }

    @Test
    void testAwaitRxJavaMaybeFromCallable() {
        assertScript PREAMBLE + '''
            def maybe = Maybe.fromCallable { "from-callable" }
            def result = await maybe
            assert result == "from-callable"
        '''
    }

    @Test
    void testAwaitRxJavaMaybeWithMap() {
        assertScript PREAMBLE + '''
            def maybe = Maybe.just(5).map { it * 10 }
            def result = await maybe
            assert result == 50
        '''
    }

    // =====================================================================
    // Single/Maybe → Awaitable API (explicit conversion)
    // =====================================================================

    // tag::single_to_awaitable_api[]
    @Test
    void testRxJavaSingleToAwaitableApi() {
        assertScript PREAMBLE + '''
            def single = Single.just("adapted")
            Awaitable<String> awaitable = Awaitable.from(single)
            assert awaitable.get() == "adapted"
            assert awaitable.isDone()
        '''
    }
    // end::single_to_awaitable_api[]

    @Test
    void testRxJavaMaybeToAwaitableApi() {
        assertScript PREAMBLE + '''
            def maybe = Maybe.just(42)
            Awaitable<Integer> awaitable = Awaitable.from(maybe)
            assert awaitable.get() == 42
            assert awaitable.isDone()
        '''
    }

    @Test
    void testAwaitableThenWithSingle() {
        assertScript PREAMBLE + '''
            def single = Single.just(5)
            Awaitable<Integer> a = Awaitable.from(single)
            Awaitable<Integer> doubled = a.then { it * 2 }
            assert doubled.get() == 10
        '''
    }

    // tag::single_exceptionally[]
    @Test
    void testAwaitableExceptionallyWithSingle() {
        assertScript PREAMBLE + '''
            def single = Single.error(new RuntimeException("fail"))
            Awaitable<String> awaitable = Awaitable.from(single)
            Awaitable<String> recovered = awaitable.exceptionally { "recovered" }
            assert recovered.get() == "recovered"
        '''
    }
    // end::single_exceptionally[]

    @Test
    void testAwaitableThenComposeWithSingle() {
        assertScript PREAMBLE + '''
            def single = Single.just(5)
            Awaitable<Integer> a = Awaitable.from(single)
            Awaitable<Integer> composed = a.thenCompose { val ->
                Awaitable.of(val * 10)
            }
            assert composed.get() == 50
        '''
    }

    @Test
    void testAwaitableHandleWithSingle() {
        assertScript PREAMBLE + '''
            def single = Single.error(new RuntimeException("err"))
            Awaitable<String> a = Awaitable.from(single)
            Awaitable<String> handled = a.handle { val, err ->
                err != null ? "handled: ${err.message}" : val
            }
            assert handled.get() == "handled: err"
        '''
    }

    @Test
    void testSingleToCompletableFutureInterop() {
        assertScript PREAMBLE + '''
            def single = Single.just("from-rxjava")
            Awaitable<String> awaitable = Awaitable.from(single)
            CompletableFuture<String> cf = awaitable.toCompletableFuture()
            assert cf.get() == "from-rxjava"
        '''
    }

    @Test
    void testSingleIsCompletedExceptionally() {
        assertScript PREAMBLE + '''
            def single = Single.error(new RuntimeException("err"))
            Awaitable<String> awaitable = Awaitable.from(single)
            Thread.sleep(50)
            assert awaitable.isCompletedExceptionally()
        '''
    }

    // =====================================================================
    // Observable → AsyncStream (for await)
    // =====================================================================

    // tag::for_await_observable_basic[]
    @Test
    void testForAwaitRxJavaObservable() {
        assertScript PREAMBLE + '''
            def observable = Observable.just("a", "b", "c")
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == ["a", "b", "c"]
        '''
    }
    // end::for_await_observable_basic[]

    // tag::for_await_observable_operators[]
    @Test
    void testForAwaitRxJavaObservableWithOperators() {
        assertScript PREAMBLE + '''
            def observable = Observable.range(1, 5).filter { it > 2 }.map { "item-${it}" }
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == ["item-3", "item-4", "item-5"]
        '''
    }
    // end::for_await_observable_operators[]

    @Test
    void testForAwaitRxJavaObservableEmpty() {
        assertScript PREAMBLE + '''
            def observable = Observable.empty()
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testForAwaitRxJavaObservableRange() {
        assertScript PREAMBLE + '''
            def observable = Observable.range(1, 100)
            def sum = 0
            for await (item in observable) {
                sum += item
            }
            assert sum == 5050
        '''
    }

    @Test
    void testForAwaitRxJavaObservableFlatMap() {
        assertScript PREAMBLE + '''
            def observable = Observable.just(1, 2, 3).flatMap { n ->
                Observable.just(n, n * 10)
            }
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results.sort() == [1, 2, 3, 10, 20, 30]
        '''
    }

    @Test
    void testForAwaitRxJavaObservableDistinct() {
        assertScript PREAMBLE + '''
            def observable = Observable.just(1, 2, 2, 3, 3, 3, 4).distinct()
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == [1, 2, 3, 4]
        '''
    }

    @Test
    void testForAwaitRxJavaObservableTake() {
        assertScript PREAMBLE + '''
            def observable = Observable.range(1, 1000).take(5)
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitRxJavaObservableError() {
        assertScript PREAMBLE + '''
            def observable = Observable.concat(
                Observable.just(1, 2),
                Observable.error(new RuntimeException("observable error"))
            )
            def results = []
            def caught = null
            try {
                for await (item in observable) {
                    results << item
                }
                assert false : "should have thrown"
            } catch (Exception e) {
                caught = e
            }
            assert caught != null : "error should have been caught"
            // Observable has no backpressure, so the error may be consumed
            // before all values are delivered; verify collected values are valid
            assert results.every { it in [1, 2] }
            // RxJava may wrap the error; find the root cause
            def rootCause = caught
            while (rootCause.cause != null && rootCause.cause != rootCause) {
                rootCause = rootCause.cause
            }
            assert rootCause.message == "observable error" || caught.message == "observable error"
        '''
    }

    // =====================================================================
    // Flowable → AsyncStream (for await with back-pressure)
    // =====================================================================

    // tag::for_await_flowable_basic[]
    @Test
    void testForAwaitRxJavaFlowable() {
        assertScript PREAMBLE + '''
            def flowable = Flowable.just(10, 20, 30)
            def results = []
            for await (item in flowable) {
                results << item
            }
            assert results == [10, 20, 30]
        '''
    }
    // end::for_await_flowable_basic[]

    // tag::for_await_flowable_generate[]
    @Test
    void testForAwaitConsumingRxJavaFlowable() {
        assertScript PREAMBLE + '''
            def flowable = Flowable.range(1, 5).map { "item-${it}" }
            def results = []
            for await (item in flowable) {
                results << item
            }
            assert results == ['item-1', 'item-2', 'item-3', 'item-4', 'item-5']
        '''
    }
    // end::for_await_flowable_generate[]

    // tag::for_await_observable_consume[]
    @Test
    void testForAwaitConsumingRxJavaObservable() {
        assertScript PREAMBLE + '''
            def observable = Observable.just('alpha', 'beta', 'gamma')
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == ['alpha', 'beta', 'gamma']
        '''
    }
    // end::for_await_observable_consume[]

    @Test
    void testForAwaitRxJavaFlowableEmpty() {
        assertScript PREAMBLE + '''
            def flowable = Flowable.empty()
            def results = []
            for await (item in flowable) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testForAwaitRxJavaFlowableRange() {
        assertScript PREAMBLE + '''
            def flowable = Flowable.range(1, 100)
            def sum = 0
            for await (item in flowable) {
                sum += item
            }
            assert sum == 5050
        '''
    }

    @Test
    void testForAwaitRxJavaFlowableWithBackPressure() {
        assertScript PREAMBLE + '''
            def flowable = Flowable.range(1, 50).onBackpressureBuffer()
            def results = []
            for await (item in flowable) {
                results << item
            }
            assert results.size() == 50
            assert results == (1..50).toList()
        '''
    }

    @Test
    void testForAwaitRxJavaFlowableError() {
        assertScript PREAMBLE + '''
            def flowable = Flowable.concat(
                Flowable.just(1, 2),
                Flowable.error(new RuntimeException("flowable error"))
            )
            def results = []
            def caught = null
            try {
                for await (item in flowable) {
                    results << item
                }
                assert false : "should have thrown"
            } catch (Exception e) {
                caught = e
            }
            // Some elements from the successful portion are collected before the error
            assert !results.isEmpty()
            assert results.every { it in [1, 2] }
            // RxJava may wrap the error; find the root cause
            def rootCause = caught
            while (rootCause.cause != null && rootCause.cause != rootCause) {
                rootCause = rootCause.cause
            }
            assert rootCause.message == "flowable error" || caught.message == "flowable error"
        '''
    }

    @Test
    void testForAwaitFlowableEarlyBreak() {
        assertScript PREAMBLE + '''
            def flowable = Flowable.range(1, 100)
            def results = []
            for await (item in flowable) {
                if (item > 3) break
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    // =====================================================================
    // Async processing inside for await
    // =====================================================================

    @Test
    void testForAwaitObservableWithAsyncProcessing() {
        assertScript PREAMBLE + '''
            def observable = Observable.just(1, 2, 3)
            def results = []
            for await (val in observable) {
                def doubled = await CompletableFuture.supplyAsync { val * 2 }
                results << doubled
            }
            assert results == [2, 4, 6]
        '''
    }

    // =====================================================================
    // Adapter direct API tests
    // =====================================================================

    @Test
    void testAdapterSupportsAwaitableSingle() {
        def adapter = new RxJavaAwaitableAdapter()
        assert adapter.supportsAwaitable(Single)
        assert adapter.supportsAwaitable(Maybe)
        assert !adapter.supportsAwaitable(Observable)
        assert !adapter.supportsAwaitable(Flowable)
        assert !adapter.supportsAwaitable(String)
    }

    @Test
    void testAdapterSupportsAsyncStreamObservable() {
        def adapter = new RxJavaAwaitableAdapter()
        assert adapter.supportsAsyncStream(Observable)
        assert adapter.supportsAsyncStream(Flowable)
        assert !adapter.supportsAsyncStream(Single)
        assert !adapter.supportsAsyncStream(Maybe)
        assert !adapter.supportsAsyncStream(String)
    }

    @Test
    void testAdapterToAwaitableSingleDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        Awaitable<String> awaitable = adapter.toAwaitable(Single.just("direct"))
        assert awaitable.get() == "direct"
    }

    @Test
    void testAdapterToAwaitableMaybeDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        Awaitable<String> awaitable = adapter.toAwaitable(Maybe.just("maybe-direct"))
        assert awaitable.get() == "maybe-direct"
    }

    @Test
    void testAdapterToAwaitableMaybeEmptyDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        Awaitable<Object> awaitable = adapter.toAwaitable(Maybe.empty())
        assert awaitable.get() == null
    }

    @Test
    void testAdapterToAwaitableUnsupportedType() {
        def adapter = new RxJavaAwaitableAdapter()
        try {
            adapter.toAwaitable("not-rxjava")
            assert false : "should have thrown"
        } catch (IllegalArgumentException e) {
            assert e.message.contains("Unsupported RxJava type")
        }
    }

    @Test
    void testAdapterToAsyncStreamObservableDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        AsyncStream<Integer> stream = adapter.toAsyncStream(Observable.just(1, 2, 3))
        def results = []
        while (stream.moveNext().get()) {
            results << stream.getCurrent()
        }
        assert results == [1, 2, 3]
    }

    @Test
    void testAdapterToAsyncStreamFlowableDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        AsyncStream<Integer> stream = adapter.toAsyncStream(Flowable.just(10, 20, 30))
        def results = []
        while (stream.moveNext().get()) {
            results << stream.getCurrent()
        }
        assert results == [10, 20, 30]
    }

    @Test
    void testAdapterToAsyncStreamEmptyObservable() {
        def adapter = new RxJavaAwaitableAdapter()
        AsyncStream<Object> stream = adapter.toAsyncStream(Observable.empty())
        assert !stream.moveNext().get()
    }

    @Test
    void testAdapterToAsyncStreamUnsupportedType() {
        def adapter = new RxJavaAwaitableAdapter()
        try {
            adapter.toAsyncStream("not-rxjava")
            assert false : "should have thrown"
        } catch (IllegalArgumentException e) {
            assert e.message.contains("Unsupported RxJava stream type")
        }
    }

    // =====================================================================
    // Awaitable combinators with RxJava types
    // =====================================================================

    // tag::rxjava_awaitable_all[]
    @Test
    void testAwaitableAllWithSingles() {
        assertScript PREAMBLE + '''
            def s1 = Single.just(1)
            def s2 = Single.just(2)
            def s3 = Single.just(3)
            def results = await Awaitable.all(s1, s2, s3)
            assert results == [1, 2, 3]
        '''
    }
    // end::rxjava_awaitable_all[]

    @Test
    void testAwaitableAnyWithSingles() {
        assertScript PREAMBLE + '''
            def s1 = Single.just("first")
            def s2 = Single.just("second")
            def result = await Awaitable.any(s1, s2)
            assert result in ["first", "second"]
        '''
    }

    // tag::rxjava_awaitable_allsettled[]
    @Test
    void testAwaitableAllSettledWithSingles() {
        assertScript PREAMBLE + '''
            def s1 = Single.just("ok")
            def s2 = Single.error(new RuntimeException("fail"))
            def results = await Awaitable.allSettled(s1, s2)
            assert results[0].isSuccess()
            assert results[0].value == "ok"
            assert results[1].isFailure()
            assert results[1].error.message == "fail"
        '''
    }
    // end::rxjava_awaitable_allsettled[]

    // =====================================================================
    // Cross-framework interoperability
    // =====================================================================

    // tag::rxjava_cf_interop[]
    @Test
    void testRxJavaWithCompletableFutureInterop() {
        assertScript PREAMBLE + '''
            def single = Single.just(10)
            def cf = CompletableFuture.supplyAsync { 20 }
            def r1 = await single
            def r2 = await cf
            assert r1 + r2 == 30
        '''
    }
    // end::rxjava_cf_interop[]

    // =====================================================================
    // Async method returning Awaitable from RxJava
    // =====================================================================

    // tag::async_method_rxjava[]
    @Test
    void testAsyncMethodWithRxJava() {
        assertScript PREAMBLE + '''
            class RxJavaService {
                async fetchData() {
                    def single = Single.just("data-from-rxjava")
                    return await single
                }
            }
            def service = new RxJavaService()
            def result = await service.fetchData()
            assert result == "data-from-rxjava"
        '''
    }
    // end::async_method_rxjava[]

    @Test
    void testAsyncMethodMultipleSingleAwaits() {
        assertScript PREAMBLE + '''
            class MultiSingleService {
                async computeTotal() {
                    def a = await Single.just(10)
                    def b = await Single.just(20)
                    def c = await Maybe.just(30)
                    return a + b + c
                }
            }
            def result = await new MultiSingleService().computeTotal()
            assert result == 60
        '''
    }

    @Test
    void testAsyncMethodWithObservableIteration() {
        assertScript PREAMBLE + '''
            class ObservableProcessor {
                async sumObservable() {
                    def observable = Observable.range(1, 10)
                    def sum = 0
                    for await (item in observable) {
                        sum += item
                    }
                    return sum
                }
            }
            def result = await new ObservableProcessor().sumObservable()
            assert result == 55
        '''
    }

    // =====================================================================
    // Timeout with RxJava
    // =====================================================================

    @Test
    void testSingleWithOrTimeout() {
        assertScript PREAMBLE + '''
            def single = Single.just("fast")
            Awaitable<String> a = Awaitable.from(single)
            Awaitable<String> withTimeout = a.orTimeoutMillis(5000)
            assert withTimeout.get() == "fast"
        '''
    }

    // =====================================================================
    // Colon notation for await
    // =====================================================================

    @Test
    void testForAwaitColonNotation() {
        assertScript PREAMBLE + '''
            def observable = Observable.just("x", "y", "z")
            def results = []
            for await (item : observable) {
                results << item
            }
            assert results == ["x", "y", "z"]
        '''
    }

    @Test
    void testForAwaitFlowableColonNotation() {
        assertScript PREAMBLE + '''
            def flowable = Flowable.just(1, 2, 3)
            def results = []
            for await (item : flowable) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }
}
