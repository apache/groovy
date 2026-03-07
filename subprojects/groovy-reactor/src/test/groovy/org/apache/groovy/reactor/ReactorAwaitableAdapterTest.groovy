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
 * Comprehensive integration tests for the {@link ReactorAwaitableAdapter},
 * verifying that Reactor's {@link Mono} and {@link Flux} types work
 * seamlessly with Groovy's {@code async}/{@code await} system.
 * <p>
 * The adapter is auto-discovered via {@link java.util.ServiceLoader}, so
 * these tests validate the end-to-end integration path — from classpath
 * discovery through to actual async/await execution.
 *
 * @see ReactorAwaitableAdapter
 */
class ReactorAwaitableAdapterTest {

    /** Shared import preamble for Reactor-based assertScript tests. */
    private static final String PREAMBLE = '''\
import groovy.concurrent.*
import org.apache.groovy.runtime.async.GroovyPromise
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
'''

    // =====================================================================
    // ServiceLoader auto-discovery
    // =====================================================================

    // tag::serviceloader_discovery[]
    @Test
    void testServiceLoaderDiscovery() {
        def adapters = ServiceLoader.load(AwaitableAdapter).toList()
        assert adapters.any { it instanceof ReactorAwaitableAdapter }
    }
    // end::serviceloader_discovery[]

    // tag::serviceloader_mono_await[]
    @Test
    void testAutoDiscoveredAdapterAwaitsMono() {
        assertScript PREAMBLE + '''
            def mono = Mono.just("auto-discovered")
            def result = await mono
            assert result == "auto-discovered"
        '''
    }
    // end::serviceloader_mono_await[]

    // =====================================================================
    // Mono → Awaitable (single-value await)
    // =====================================================================

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

    @Test
    void testAwaitReactorMonoFromCallable() {
        assertScript PREAMBLE + '''
            def mono = Mono.fromCallable { Thread.currentThread().name }
            def result = await mono
            assert result != null
            assert result instanceof String
        '''
    }

    @Test
    void testAwaitReactorMonoDelayed() {
        assertScript PREAMBLE + '''
            import java.time.Duration
            def mono = Mono.delay(Duration.ofMillis(50)).map { "delayed" }
            def result = await mono
            assert result == "delayed"
        '''
    }

    @Test
    void testAwaitReactorMonoZip() {
        assertScript PREAMBLE + '''
            def m1 = Mono.just(10)
            def m2 = Mono.just(20)
            def zipped = Mono.zip(m1, m2) { a, b -> a + b }
            def result = await zipped
            assert result == 30
        '''
    }

    // =====================================================================
    // Mono → Awaitable API (explicit conversion)
    // =====================================================================

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

    @Test
    void testAwaitableThenComposeWithReactor() {
        assertScript PREAMBLE + '''
            def mono = Mono.just(5)
            Awaitable<Integer> a = Awaitable.from(mono)
            Awaitable<Integer> composed = a.thenCompose { val ->
                GroovyPromise.of(CompletableFuture.supplyAsync { val * 10 })
            }
            assert composed.get() == 50
        '''
    }

    @Test
    void testAwaitableWhenCompleteWithReactor() {
        assertScript PREAMBLE + '''
            def mono = Mono.just("test")
            Awaitable<String> a = Awaitable.from(mono)
            def sideEffect = []
            Awaitable<String> result = a.whenComplete { val, err ->
                sideEffect << val
            }
            assert result.get() == "test"
            Thread.sleep(50)
            assert sideEffect == ["test"]
        '''
    }

    @Test
    void testAwaitableHandleWithReactor() {
        assertScript PREAMBLE + '''
            def mono = Mono.error(new RuntimeException("err"))
            Awaitable<String> a = Awaitable.from(mono)
            Awaitable<String> handled = a.handle { val, err ->
                err != null ? "handled: ${err.message}" : val
            }
            assert handled.get() == "handled: err"
        '''
    }

    @Test
    void testMonoToCompletableFutureInterop() {
        assertScript PREAMBLE + '''
            def mono = Mono.just("from-reactor")
            Awaitable<String> awaitable = Awaitable.from(mono)
            CompletableFuture<String> cf = awaitable.toCompletableFuture()
            assert cf.get() == "from-reactor"
        '''
    }

    @Test
    void testMonoIsDone() {
        assertScript PREAMBLE + '''
            def mono = Mono.just("done-check")
            Awaitable<String> awaitable = Awaitable.from(mono)
            awaitable.get()
            assert awaitable.isDone()
            assert !awaitable.isCancelled()
            assert !awaitable.isCompletedExceptionally()
        '''
    }

    @Test
    void testMonoIsCompletedExceptionally() {
        assertScript PREAMBLE + '''
            def mono = Mono.error(new RuntimeException("err"))
            Awaitable<String> awaitable = Awaitable.from(mono)
            Thread.sleep(50)
            assert awaitable.isCompletedExceptionally()
        '''
    }

    // =====================================================================
    // Flux → AsyncStream (for await)
    // =====================================================================

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

    @Test
    void testForAwaitReactorFluxRange() {
        assertScript PREAMBLE + '''
            def flux = Flux.range(1, 100)
            def sum = 0
            for await (item in flux) {
                sum += item
            }
            assert sum == 5050
        '''
    }

    @Test
    void testForAwaitReactorFluxWithFlatMap() {
        assertScript PREAMBLE + '''
            def flux = Flux.just(1, 2, 3).flatMap { n ->
                Flux.just(n, n * 10)
            }
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results.sort() == [1, 2, 3, 10, 20, 30]
        '''
    }

    @Test
    void testForAwaitReactorFluxWithTake() {
        assertScript PREAMBLE + '''
            def flux = Flux.range(1, 1000).take(5)
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitReactorFluxSingleElement() {
        assertScript PREAMBLE + '''
            def flux = Flux.just("only-one")
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == ["only-one"]
        '''
    }

    @Test
    void testForAwaitReactorFluxWithDistinct() {
        assertScript PREAMBLE + '''
            def flux = Flux.just(1, 2, 2, 3, 3, 3, 4).distinct()
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [1, 2, 3, 4]
        '''
    }

    @Test
    void testForAwaitReactorFluxError() {
        assertScript PREAMBLE + '''
            def flux = Flux.concat(
                Flux.just(1, 2),
                Flux.error(new RuntimeException("flux error"))
            )
            def results = []
            def caught = null
            try {
                for await (item in flux) {
                    results << item
                }
                assert false : "should have thrown"
            } catch (Exception e) {
                caught = e
            }
            // Some elements from the successful portion are collected before the error
            assert !results.isEmpty()
            assert results.every { it in [1, 2] }
            // Reactor may wrap the error; find the root cause
            def rootCause = caught
            while (rootCause.cause != null && rootCause.cause != rootCause) {
                rootCause = rootCause.cause
            }
            assert rootCause.message == "flux error" || caught.message == "flux error"
        '''
    }

    // =====================================================================
    // Async processing inside for await
    // =====================================================================

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

    @Test
    void testForAwaitFluxEarlyBreak() {
        assertScript PREAMBLE + '''
            def flux = Flux.range(1, 100)
            def results = []
            for await (item in flux) {
                if (item > 3) break
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    // =====================================================================
    // Spring-style patterns (Reactor is the foundation of Spring WebFlux)
    // =====================================================================

    private static final String SPRING_STUBS = '''
class SpringStyleService {
    java.util.concurrent.CompletableFuture<Map> fetchUser(long id) {
        java.util.concurrent.CompletableFuture.supplyAsync { [id: id, name: "User${id}"] }
    }
    java.util.concurrent.CompletionStage<String> processAsync(String input) {
        java.util.concurrent.CompletableFuture.supplyAsync { input.toUpperCase() }
    }
}
class SpringWebFluxStyleController {
    reactor.core.publisher.Mono<Map> getUser(long id) {
        reactor.core.publisher.Mono.just([id: id, name: "User${id}"])
    }
    reactor.core.publisher.Flux<Map> listUsers() {
        reactor.core.publisher.Flux.just(
            [id: 1L, name: "User1"],
            [id: 2L, name: "User2"],
            [id: 3L, name: "User3"]
        )
    }
}
'''

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

    @Test
    void testSpringStyleAsyncChainWithReactorMono() {
        assertScript PREAMBLE + SPRING_STUBS + '''
            def userService = new SpringWebFluxStyleController()
            def enriched = userService.getUser(1L).map { user -> user + [role: "admin"] }
            def result = await enriched
            assert result.name == "User1"
            assert result.role == "admin"
        '''
    }

    @Test
    void testSpringStyleCompletableFutureService() {
        assertScript PREAMBLE + SPRING_STUBS + '''
            def springService = new SpringStyleService()
            def result = await springService.fetchUser(1L)
            assert result == [id: 1L, name: "User1"]
        '''
    }

    @Test
    void testSpringStyleCompletionStageService() {
        assertScript PREAMBLE + SPRING_STUBS + '''
            def springService = new SpringStyleService()
            CompletionStage<String> stage = springService.processAsync("hello")
            def result = await stage
            assert result == "HELLO"
        '''
    }

    @Test
    void testSpringStyleMultipleAsyncCalls() {
        assertScript PREAMBLE + SPRING_STUBS + '''
            def service = new SpringStyleService()
            def f1 = service.fetchUser(1L)
            def f2 = service.fetchUser(2L)
            def f3 = service.fetchUser(3L)
            def results = [await(f1), await(f2), await(f3)]
            assert results.collect { it.name } == ["User1", "User2", "User3"]
        '''
    }

    // =====================================================================
    // Cross-framework interoperability
    // =====================================================================

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

    // =====================================================================
    // Adapter direct API tests
    // =====================================================================

    @Test
    void testAdapterSupportsAwaitableMono() {
        def adapter = new ReactorAwaitableAdapter()
        assert adapter.supportsAwaitable(Mono)
        assert !adapter.supportsAwaitable(Flux)
        assert !adapter.supportsAwaitable(String)
        assert !adapter.supportsAwaitable(CompletableFuture)
    }

    @Test
    void testAdapterSupportsAsyncStreamFlux() {
        def adapter = new ReactorAwaitableAdapter()
        assert adapter.supportsAsyncStream(Flux)
        assert !adapter.supportsAsyncStream(Mono)
        assert !adapter.supportsAsyncStream(String)
        assert !adapter.supportsAsyncStream(List)
    }

    @Test
    void testAdapterToAwaitableDirectly() {
        def adapter = new ReactorAwaitableAdapter()
        Awaitable<String> awaitable = adapter.toAwaitable(Mono.just("direct"))
        assert awaitable.get() == "direct"
    }

    @Test
    void testAdapterToAsyncStreamDirectly() {
        def adapter = new ReactorAwaitableAdapter()
        AsyncStream<Integer> stream = adapter.toAsyncStream(Flux.just(1, 2, 3))
        def results = []
        while (stream.moveNext().get()) {
            results << stream.getCurrent()
        }
        assert results == [1, 2, 3]
    }

    @Test
    void testAdapterToAsyncStreamEmpty() {
        def adapter = new ReactorAwaitableAdapter()
        AsyncStream<Object> stream = adapter.toAsyncStream(Flux.empty())
        assert !stream.moveNext().get()
    }

    @Test
    void testAdapterToAwaitableEmptyMono() {
        def adapter = new ReactorAwaitableAdapter()
        Awaitable<Object> awaitable = adapter.toAwaitable(Mono.empty())
        assert awaitable.get() == null
    }

    @Test
    void testAdapterToAwaitableErrorMono() {
        def adapter = new ReactorAwaitableAdapter()
        def error = new RuntimeException("test error")
        Awaitable<Object> awaitable = adapter.toAwaitable(Mono.error(error))
        try {
            awaitable.get()
            assert false : "should have thrown"
        } catch (java.util.concurrent.ExecutionException e) {
            assert e.cause.message == "test error"
        }
    }

    // =====================================================================
    // Awaitable combinators with Reactor types
    // =====================================================================

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

    @Test
    void testAwaitableAnyWithMonos() {
        assertScript PREAMBLE + '''
            def m1 = Mono.just("first")
            def m2 = Mono.just("second")
            def result = await Awaitable.any(m1, m2)
            assert result in ["first", "second"]
        '''
    }

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

    // =====================================================================
    // Timeout with Reactor
    // =====================================================================

    @Test
    void testMonoWithOrTimeout() {
        assertScript PREAMBLE + '''
            def mono = Mono.just("fast")
            Awaitable<String> a = Awaitable.from(mono)
            Awaitable<String> withTimeout = a.orTimeoutMillis(5000)
            assert withTimeout.get() == "fast"
        '''
    }

    @Test
    void testMonoWithCompleteOnTimeout() {
        assertScript PREAMBLE + '''
            import java.time.Duration
            def mono = Mono.delay(Duration.ofSeconds(10)).map { "too-slow" }
            Awaitable<String> a = Awaitable.from(mono)
            Awaitable<String> withFallback = Awaitable.completeOnTimeoutMillis(a, "fallback", 100)
            assert withFallback.get() == "fallback"
        '''
    }

    // =====================================================================
    // Async method returning Awaitable from Reactor
    // =====================================================================

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

    @Test
    void testAsyncMethodMultipleMonoAwaits() {
        assertScript PREAMBLE + '''
            class MultiMonoService {
                async computeTotal() {
                    def a = await Mono.just(10)
                    def b = await Mono.just(20)
                    def c = await Mono.just(30)
                    return a + b + c
                }
            }
            def result = await new MultiMonoService().computeTotal()
            assert result == 60
        '''
    }

    @Test
    void testAsyncMethodWithFluxIteration() {
        assertScript PREAMBLE + '''
            class FluxProcessor {
                async sumFlux() {
                    def flux = Flux.range(1, 10)
                    def sum = 0
                    for await (item in flux) {
                        sum += item
                    }
                    return sum
                }
            }
            def result = await new FluxProcessor().sumFlux()
            assert result == 55
        '''
    }

    // =====================================================================
    // Colon notation for await
    // =====================================================================

    @Test
    void testForAwaitColonNotation() {
        assertScript PREAMBLE + '''
            def flux = Flux.just("x", "y", "z")
            def results = []
            for await (item : flux) {
                results << item
            }
            assert results == ["x", "y", "z"]
        '''
    }
}
