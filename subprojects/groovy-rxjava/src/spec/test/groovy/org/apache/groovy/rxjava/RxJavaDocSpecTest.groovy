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

import java.util.concurrent.CompletableFuture

import static groovy.test.GroovyAssert.assertScript

/**
 * Specification tests for the {@code rxjava-userguide.adoc} documentation.
 * Each test method is tagged for inclusion in the Asciidoctor documentation
 * via {@code include::} directives with {@code [tags=...]} attributes.
 *
 * <p>These tests serve a dual purpose: they verify correctness and provide
 * the verified code examples rendered in the user guide.
 */
class RxJavaDocSpecTest {

    /** Shared import preamble for assertScript tests. */
    private static final String PREAMBLE = '''\
import groovy.concurrent.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
'''

    // ---- ServiceLoader discovery ----

    // tag::serviceloader_discovery[]
    @Test
    void testServiceLoaderDiscovery() {
        def adapters = ServiceLoader.load(AwaitableAdapter).toList()
        assert adapters.any { it instanceof RxJavaAwaitableAdapter }
    }
    // end::serviceloader_discovery[]

    // ---- Single → Awaitable ----

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

    // ---- Maybe → Awaitable ----

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

    // ---- Awaitable API ----

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

    // ---- Observable → AsyncStream (for await) ----

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

    // ---- Flowable → AsyncStream (for await with back-pressure) ----

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

    // ---- Async methods ----

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

    // ---- Cross-framework interop ----

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

    // ---- Awaitable combinators ----

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
}
