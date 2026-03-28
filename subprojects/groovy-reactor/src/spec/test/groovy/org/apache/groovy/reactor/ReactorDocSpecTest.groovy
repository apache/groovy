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
package org.apache.groovy.reactor

import groovy.concurrent.AsyncStream
import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitableAdapter
import groovy.concurrent.AwaitableAdapterRegistry
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.util.concurrent.CompletableFuture

import static groovy.test.GroovyAssert.assertScript

/**
 * Specification tests for the {@code reactor-userguide.adoc} documentation.
 * Each test method is tagged for inclusion in the Asciidoctor documentation
 * via {@code include::} directives with {@code [tags=...]} attributes.
 *
 * <p>These tests serve a dual purpose: they verify correctness and provide
 * the verified code examples rendered in the user guide.
 */
class ReactorDocSpecTest {

    /** Shared import preamble for assertScript tests. */
    private static final String PREAMBLE = '''\
import groovy.concurrent.*
import org.apache.groovy.runtime.async.GroovyPromise
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
'''

    /** Spring WebFlux simulation stubs. */
    private static final String SPRING_STUBS = '''
class SpringWebFluxStyleController {
    Mono<Map> getUser(Long id) {
        return Mono.just([id: id, name: "User${id}"])
    }
    Flux<Map> listUsers() {
        return Flux.just(
            [id: 1L, name: "User1"],
            [id: 2L, name: "User2"],
            [id: 3L, name: "User3"]
        )
    }
}
'''

    // ---- ServiceLoader discovery ----

    // tag::serviceloader_discovery[]
    @Test
    void testServiceLoaderDiscovery() {
        def adapters = ServiceLoader.load(AwaitableAdapter).toList()
        assert adapters.any { it instanceof ReactorAwaitableAdapter }
    }
    // end::serviceloader_discovery[]

    // ---- Mono → Awaitable ----

    // tag::await_mono_basic[]
    @Test
    void testAwaitReactorMono() {
        assertScript PREAMBLE + '''
            def mono = Mono.just("reactor-value")
            def result = await mono
            assert result == "reactor-value"
        '''
    }
    // end::await_mono_basic[]

    // tag::await_mono_map[]
    @Test
    void testAwaitReactorMonoWithMap() {
        assertScript PREAMBLE + '''
            def mono = Mono.just(10).map { it * 3 }
            def result = await mono
            assert result == 30
        '''
    }
    // end::await_mono_map[]

    // tag::await_mono_empty[]
    @Test
    void testAwaitReactorMonoEmpty() {
        assertScript PREAMBLE + '''
            def mono = Mono.empty()
            def result = await mono
            assert result == null
        '''
    }
    // end::await_mono_empty[]

    // tag::await_mono_error[]
    @Test
    void testAwaitReactorMonoError() {
        assertScript PREAMBLE + '''
            def mono = Mono.error(new IllegalStateException("reactor error"))
            try {
                await mono
                assert false : "should have thrown"
            } catch (IllegalStateException e) {
                assert e.message == "reactor error"
            }
        '''
    }
    // end::await_mono_error[]

    // tag::await_mono_deferred[]
    @Test
    void testAwaitReactorMonoDeferred() {
        assertScript PREAMBLE + '''
            def mono = Mono.fromSupplier { "deferred" }
            def result = await mono
            assert result == "deferred"
        '''
    }
    // end::await_mono_deferred[]

    // tag::await_mono_chain[]
    @Test
    void testAwaitReactorMonoChain() {
        assertScript PREAMBLE + '''
            def mono = Mono.just("hello")
                .map { it.toUpperCase() }
                .flatMap { s -> Mono.just("${s}!") }
            def result = await mono
            assert result == "HELLO!"
        '''
    }
    // end::await_mono_chain[]

    // ---- Awaitable API ----

    // tag::mono_to_awaitable_api[]
    @Test
    void testReactorMonoToAwaitableApi() {
        assertScript PREAMBLE + '''
            def mono = Mono.just(42)
            Awaitable<Integer> awaitable = Awaitable.from(mono)
            assert awaitable.get() == 42
            assert awaitable.isDone()
        '''
    }
    // end::mono_to_awaitable_api[]

    // tag::mono_awaitable_then[]
    @Test
    void testReactorMonoAwaitableThen() {
        assertScript PREAMBLE + '''
            def mono = Mono.just(5)
            Awaitable<Integer> a = Awaitable.from(mono)
            Awaitable<Integer> doubled = a.then { it * 2 }
            assert doubled.get() == 10
        '''
    }
    // end::mono_awaitable_then[]

    // tag::mono_awaitable_exceptionally[]
    @Test
    void testAwaitableExceptionallyWithReactor() {
        assertScript PREAMBLE + '''
            def mono = Mono.error(new RuntimeException("fail"))
            Awaitable<String> awaitable = Awaitable.from(mono)
            Awaitable<String> recovered = awaitable.exceptionally { "recovered" }
            assert recovered.get() == "recovered"
        '''
    }
    // end::mono_awaitable_exceptionally[]

    // ---- Flux → AsyncStream (for await) ----

    // tag::for_await_flux_basic[]
    @Test
    void testForAwaitReactorFlux() {
        assertScript PREAMBLE + '''
            def flux = Flux.just(1, 2, 3, 4, 5)
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }
    // end::for_await_flux_basic[]

    // tag::for_await_flux_operators[]
    @Test
    void testForAwaitReactorFluxWithOperators() {
        assertScript PREAMBLE + '''
            def flux = Flux.range(1, 10).filter { it % 2 == 0 }.map { it * 10 }
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [20, 40, 60, 80, 100]
        '''
    }
    // end::for_await_flux_operators[]

    // tag::for_await_flux_empty[]
    @Test
    void testForAwaitReactorFluxEmpty() {
        assertScript PREAMBLE + '''
            def flux = Flux.empty()
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == []
        '''
    }
    // end::for_await_flux_empty[]

    // tag::for_await_flux_generate[]
    @Test
    void testForAwaitConsumingReactorFlux() {
        assertScript PREAMBLE + '''
            def flux = Flux.generate(
                { 0 },
                { state, sink ->
                    sink.next("3 x ${state} = ${3 * state}")
                    if (state == 4) sink.complete()
                    return state + 1
                }
            )
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [
                '3 x 0 = 0', '3 x 1 = 3', '3 x 2 = 6',
                '3 x 3 = 9', '3 x 4 = 12'
            ]
        '''
    }
    // end::for_await_flux_generate[]

    // tag::for_await_flux_async_processing[]
    @Test
    void testForAwaitReactorFluxWithAsyncProcessing() {
        assertScript PREAMBLE + '''
            def flux = Flux.just(1, 2, 3)
            def results = []
            for await (val in flux) {
                def doubled = await CompletableFuture.supplyAsync { val * 2 }
                results << doubled
            }
            assert results == [2, 4, 6]
        '''
    }
    // end::for_await_flux_async_processing[]

    // tag::for_await_flux_defer_cleanup[]
    @Test
    void testForAwaitReactorFluxWithDeferCleanup() {
        assertScript PREAMBLE + '''
            import java.util.concurrent.SubmissionPublisher

            class RPPatternTest {
                static log = []

                async processStreamWithCleanup() {
                    defer { log << 'cleanup-done' }
                    def publisher = new SubmissionPublisher<Integer>()
                    Thread.start {
                        Thread.sleep(50)
                        (1..3).each { publisher.submit(it) }
                        publisher.close()
                    }
                    def sum = 0
                    for await (item in publisher) {
                        sum += item
                    }
                    log << "sum=$sum"
                    return sum
                }
            }

            def result = await new RPPatternTest().processStreamWithCleanup()
            assert result == 6
            assert RPPatternTest.log == ['sum=6', 'cleanup-done']
        '''
    }
    // end::for_await_flux_defer_cleanup[]

    // ---- Async methods ----

    // tag::async_method_reactor[]
    @Test
    void testAsyncMethodWithReactor() {
        assertScript PREAMBLE + '''
            class ReactorService {
                async fetchData() {
                    def mono = Mono.just("data-from-reactor")
                    return await mono
                }
            }
            def service = new ReactorService()
            def result = await service.fetchData()
            assert result == "data-from-reactor"
        '''
    }
    // end::async_method_reactor[]

    // ---- Spring WebFlux patterns ----

    // tag::spring_webflux_mono[]
    @Test
    void testSpringWebFluxMonoEndpoint() {
        assertScript PREAMBLE + SPRING_STUBS + '''
            def controller = new SpringWebFluxStyleController()
            def mono = controller.getUser(42L)
            def result = await mono
            assert result == [id: 42L, name: "User42"]
        '''
    }
    // end::spring_webflux_mono[]

    // tag::spring_webflux_flux[]
    @Test
    void testSpringWebFluxFluxEndpoint() {
        assertScript PREAMBLE + SPRING_STUBS + '''
            def controller = new SpringWebFluxStyleController()
            def flux = controller.listUsers()
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results.size() == 3
            assert results[0].name == "User1"
            assert results[2].name == "User3"
        '''
    }
    // end::spring_webflux_flux[]

    // ---- Cross-framework interop ----

    // tag::reactor_cf_interop[]
    @Test
    void testReactorWithCompletableFutureInterop() {
        assertScript PREAMBLE + '''
            def mono = Mono.just(10)
            def cf = CompletableFuture.supplyAsync { 20 }
            def r1 = await mono
            def r2 = await cf
            assert r1 + r2 == 30
        '''
    }
    // end::reactor_cf_interop[]

    // ---- Awaitable combinators ----

    // tag::reactor_awaitable_all[]
    @Test
    void testAwaitableAllWithMonos() {
        assertScript PREAMBLE + '''
            def m1 = Mono.just(1)
            def m2 = Mono.just(2)
            def m3 = Mono.just(3)
            def results = await Awaitable.all(m1, m2, m3)
            assert results == [1, 2, 3]
        '''
    }
    // end::reactor_awaitable_all[]

    // tag::reactor_awaitable_allsettled[]
    @Test
    void testAwaitableAllSettledWithMonos() {
        assertScript PREAMBLE + '''
            def m1 = Mono.just("ok")
            def m2 = Mono.error(new RuntimeException("fail"))
            def results = await Awaitable.allSettled(m1, m2)
            assert results[0].isSuccess()
            assert results[0].value == "ok"
            assert results[1].isFailure()
            assert results[1].error.message == "fail"
        '''
    }
    // end::reactor_awaitable_allsettled[]
}
