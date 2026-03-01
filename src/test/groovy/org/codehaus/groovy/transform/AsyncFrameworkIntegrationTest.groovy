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

import groovy.concurrent.AsyncStream
import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitableAdapter
import groovy.concurrent.AwaitableAdapterRegistry
import org.apache.groovy.runtime.async.GroovyPromise
import static groovy.concurrent.AsyncUtils.*
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

/**
 * Integration tests for Groovy async/await with third-party reactive
 * frameworks: Reactor (Spring WebFlux foundation), RxJava 3, and Spring-style
 * async patterns.
 * <p>
 * These tests demonstrate that the {@link AwaitableAdapter} SPI allows
 * transparent integration without coupling Groovy's core to any specific
 * reactive library.
 */
class AsyncFrameworkIntegrationTest {

    private ReactorAwaitableAdapter reactorAdapter
    private RxJavaAwaitableAdapter rxJavaAdapter

    @BeforeEach
    void registerAdapters() {
        reactorAdapter = new ReactorAwaitableAdapter()
        rxJavaAdapter = new RxJavaAwaitableAdapter()
        AwaitableAdapterRegistry.register(reactorAdapter)
        AwaitableAdapterRegistry.register(rxJavaAdapter)
    }

    @AfterEach
    void resetExecutor() {
        setExecutor(null)
        if (rxJavaAdapter != null) AwaitableAdapterRegistry.unregister(rxJavaAdapter)
        if (reactorAdapter != null) AwaitableAdapterRegistry.unregister(reactorAdapter)
    }

    // =====================================================================
    // Reactor (Spring WebFlux foundation) tests
    // =====================================================================

    @Test
    void testAwaitReactorMono() {
        def mono = Mono.just("reactor-value")
        def result = await(mono)
        assert result == "reactor-value"
    }

    @Test
    void testAwaitReactorMonoWithMap() {
        def mono = Mono.just(10).map { it * 3 }
        def result = await(mono)
        assert result == 30
    }

    @Test
    void testAwaitReactorMonoEmpty() {
        def mono = Mono.empty()
        def result = await(mono)
        assert result == null
    }

    @Test
    void testAwaitReactorMonoError() {
        def mono = Mono.error(new IllegalStateException("reactor error"))
        try {
            await(mono)
            assert false : "should have thrown"
        } catch (IllegalStateException e) {
            assert e.message == "reactor error"
        }
    }

    @Test
    void testAwaitReactorMonoDeferred() {
        def mono = Mono.fromSupplier { Thread.sleep(50); "deferred" }
        def result = await(mono)
        assert result == "deferred"
    }

    @Test
    void testAwaitReactorMonoChain() {
        def mono = Mono.just("hello")
            .map { it.toUpperCase() }
            .flatMap { s -> Mono.just("${s}!") }
        def result = await(mono)
        assert result == "HELLO!"
    }

    @Test
    void testForAwaitReactorFlux() {
        def flux = Flux.just(1, 2, 3, 4, 5)
        AsyncStream<Integer> stream = toAsyncStream(flux)
        def results = []
        while (await(stream.moveNext())) {
            results << stream.getCurrent()
        }
        assert results == [1, 2, 3, 4, 5]
    }

    @Test
    void testForAwaitReactorFluxWithOperators() {
        def flux = Flux.range(1, 10).filter { it % 2 == 0 }.map { it * 10 }
        AsyncStream<Integer> stream = toAsyncStream(flux)
        def results = []
        while (await(stream.moveNext())) {
            results << stream.getCurrent()
        }
        assert results == [20, 40, 60, 80, 100]
    }

    @Test
    void testForAwaitReactorFluxEmpty() {
        def flux = Flux.empty()
        AsyncStream stream = toAsyncStream(flux)
        def results = []
        while (await(stream.moveNext())) {
            results << stream.getCurrent()
        }
        assert results == []
    }

    @Test
    void testReactorMonoToAwaitableApi() {
        def mono = Mono.just(42)
        Awaitable<Integer> awaitable = AwaitableAdapterRegistry.toAwaitable(mono)
        assert awaitable.get() == 42
        assert awaitable.isDone()
    }

    @Test
    void testReactorMonoAwaitableThen() {
        def mono = Mono.just(5)
        Awaitable<Integer> a = AwaitableAdapterRegistry.toAwaitable(mono)
        Awaitable<Integer> doubled = a.then { it * 2 }
        assert doubled.get() == 10
    }

    // =====================================================================
    // RxJava 3 tests
    // =====================================================================

    @Test
    void testAwaitRxJavaSingle() {
        def single = Single.just("rxjava-value")
        def result = await(single)
        assert result == "rxjava-value"
    }

    @Test
    void testAwaitRxJavaSingleWithMap() {
        def single = Single.just(7).map { it * 6 }
        def result = await(single)
        assert result == 42
    }

    @Test
    void testAwaitRxJavaSingleError() {
        def single = Single.error(new RuntimeException("rx error"))
        try {
            await(single)
            assert false : "should have thrown"
        } catch (RuntimeException e) {
            assert e.message == "rx error"
        }
    }

    @Test
    void testAwaitRxJavaMaybe() {
        def maybe = Maybe.just("maybe-value")
        def result = await(maybe)
        assert result == "maybe-value"
    }

    @Test
    void testAwaitRxJavaMaybeEmpty() {
        def maybe = Maybe.empty()
        def result = await(maybe)
        assert result == null
    }

    @Test
    void testForAwaitRxJavaObservable() {
        def observable = Observable.just("a", "b", "c")
        AsyncStream<String> stream = toAsyncStream(observable)
        def results = []
        while (await(stream.moveNext())) {
            results << stream.getCurrent()
        }
        assert results == ["a", "b", "c"]
    }

    @Test
    void testForAwaitRxJavaObservableWithOperators() {
        def observable = Observable.range(1, 5).filter { it > 2 }.map { "item-${it}" }
        AsyncStream<String> stream = toAsyncStream(observable)
        def results = []
        while (await(stream.moveNext())) {
            results << stream.getCurrent()
        }
        assert results == ["item-3", "item-4", "item-5"]
    }

    @Test
    void testForAwaitRxJavaFlowable() {
        def flowable = Flowable.just(10, 20, 30)
        AsyncStream<Integer> stream = toAsyncStream(flowable)
        def results = []
        while (await(stream.moveNext())) {
            results << stream.getCurrent()
        }
        assert results == [10, 20, 30]
    }

    @Test
    void testRxJavaSingleToAwaitableApi() {
        def single = Single.just("adapted")
        Awaitable<String> awaitable = AwaitableAdapterRegistry.toAwaitable(single)
        assert awaitable.get() == "adapted"
        assert awaitable.isDone()
    }

    // =====================================================================
    // Spring-style async pattern tests
    //
    // Spring @Async methods return CompletableFuture (Spring 6+) or
    // CompletionStage. Spring WebFlux returns Reactor Mono/Flux.
    // These tests demonstrate seamless interoperability.
    // =====================================================================

    @Test
    void testSpringStyleCompletableFutureService() {
        // Simulates a Spring @Async service method returning CompletableFuture
        def springService = new SpringStyleService()
        def result = await(springService.fetchUser(1L))
        assert result == [id: 1L, name: "User1"]
    }

    @Test
    void testSpringStyleCompletionStageService() {
        // Simulates a Spring service returning CompletionStage
        def springService = new SpringStyleService()
        CompletionStage<String> stage = springService.processAsync("hello")
        def result = await(stage)
        assert result == "HELLO"
    }

    @Test
    void testSpringWebFluxMonoEndpoint() {
        // Simulates a Spring WebFlux reactive endpoint returning Mono
        def controller = new SpringWebFluxStyleController()
        def mono = controller.getUser(42L)
        def result = await(mono)
        assert result == [id: 42L, name: "User42"]
    }

    @Test
    void testSpringWebFluxFluxEndpoint() {
        // Simulates a Spring WebFlux reactive endpoint returning Flux
        def controller = new SpringWebFluxStyleController()
        def flux = controller.listUsers()
        AsyncStream stream = toAsyncStream(flux)
        def results = []
        while (await(stream.moveNext())) {
            results << stream.getCurrent()
        }
        assert results.size() == 3
        assert results[0].name == "User1"
        assert results[2].name == "User3"
    }

    @Test
    void testSpringStyleAsyncChainWithReactorMono() {
        // Simulates chaining Spring async calls with Reactor
        def userService = new SpringWebFluxStyleController()
        def enriched = userService.getUser(1L)
            .map { user -> user + [role: "admin"] }
        def result = await(enriched)
        assert result.name == "User1"
        assert result.role == "admin"
    }

    @Test
    void testSpringStyleMultipleAsyncCalls() {
        // Simulates parallel Spring async calls gathered with Groovy await
        def service = new SpringStyleService()
        def f1 = service.fetchUser(1L)
        def f2 = service.fetchUser(2L)
        def f3 = service.fetchUser(3L)
        def results = [await(f1), await(f2), await(f3)]
        assert results.collect { it.name } == ["User1", "User2", "User3"]
    }

    @Test
    void testSpringStyleCompletionStageAdapter() {
        // CompletionStage (Spring's return type) directly supported by built-in adapter
        CompletionStage<String> stage = CompletableFuture.supplyAsync { "stage-value" }
        Awaitable<String> awaitable = AwaitableAdapterRegistry.toAwaitable(stage)
        assert awaitable.get() == "stage-value"
    }

    // =====================================================================
    // Cross-framework interoperability
    // =====================================================================

    @Test
    void testMixedFrameworkAwait() {
        // Await values from different frameworks in the same context
        def mono = Mono.just(10)
        def single = Single.just(20)
        def cf = CompletableFuture.supplyAsync { 30 }

        def r1 = await(mono)
        def r2 = await(single)
        def r3 = await(cf)
        assert r1 + r2 + r3 == 60
    }

    @Test
    void testReactorToRxJavaInterop() {
        // Convert between frameworks via Awaitable abstraction
        def mono = Mono.just("from-reactor")
        Awaitable<String> awaitable = AwaitableAdapterRegistry.toAwaitable(mono)

        // Use Awaitable's CompletableFuture interop to bridge to any framework
        CompletableFuture<String> cf = awaitable.toCompletableFuture()
        assert cf.get() == "from-reactor"
    }

    @Test
    void testAwaitableExceptionallyWithReactor() {
        def mono = Mono.error(new RuntimeException("fail"))
        Awaitable<String> awaitable = AwaitableAdapterRegistry.toAwaitable(mono)
        Awaitable<String> recovered = awaitable.exceptionally { "recovered" }
        assert recovered.get() == "recovered"
    }

    @Test
    void testAwaitableThenComposeAcrossFrameworks() {
        def mono = Mono.just(5)
        Awaitable<Integer> a = AwaitableAdapterRegistry.toAwaitable(mono)
        Awaitable<Integer> composed = a.thenCompose { val ->
            // Chain into another Awaitable from a different source
            GroovyPromise.of(CompletableFuture.supplyAsync { val * 10 })
        }
        assert composed.get() == 50
    }

    // =====================================================================
    // Adapter implementation classes
    // =====================================================================

    /**
     * Reactor adapter: supports {@link Mono} as {@link Awaitable} and
     * {@link Flux} as {@link AsyncStream}. This is the pattern Spring
     * WebFlux users would register.
     */
    static class ReactorAwaitableAdapter implements AwaitableAdapter {

        @Override
        boolean supportsAwaitable(Class<?> type) {
            return Mono.isAssignableFrom(type)
        }

        @Override
        Awaitable toAwaitable(Object source) {
            return new GroovyPromise<>(((Mono) source).toFuture())
        }

        @Override
        boolean supportsAsyncStream(Class<?> type) {
            return Flux.isAssignableFrom(type)
        }

        @Override
        AsyncStream toAsyncStream(Object source) {
            def iter = ((Flux) source).toIterable().iterator()
            return new AsyncStream() {
                private Object current

                @Override
                Awaitable<Boolean> moveNext() {
                    boolean hasNext = iter.hasNext()
                    if (hasNext) current = iter.next()
                    return Awaitable.of(hasNext)
                }

                @Override
                Object getCurrent() { return current }
            }
        }
    }

    /**
     * RxJava 3 adapter: supports {@link Single}/{@link Maybe} as
     * {@link Awaitable} and {@link Observable}/{@link Flowable} as
     * {@link AsyncStream}.
     */
    static class RxJavaAwaitableAdapter implements AwaitableAdapter {

        @Override
        boolean supportsAwaitable(Class<?> type) {
            return Single.isAssignableFrom(type) || Maybe.isAssignableFrom(type)
        }

        @Override
        Awaitable toAwaitable(Object source) {
            if (source instanceof Single) {
                return new GroovyPromise<>(((Single) source).toCompletionStage().toCompletableFuture())
            }
            if (source instanceof Maybe) {
                return new GroovyPromise<>(((Maybe) source).toCompletionStage(null).toCompletableFuture())
            }
            throw new IllegalArgumentException("Unsupported RxJava type: ${source.class}")
        }

        @Override
        boolean supportsAsyncStream(Class<?> type) {
            return Observable.isAssignableFrom(type) || Flowable.isAssignableFrom(type)
        }

        @Override
        AsyncStream toAsyncStream(Object source) {
            Iterator iter
            if (source instanceof Observable) {
                iter = ((Observable) source).blockingIterable().iterator()
            } else if (source instanceof Flowable) {
                iter = ((Flowable) source).blockingIterable().iterator()
            } else {
                throw new IllegalArgumentException("Unsupported RxJava type: ${source.class}")
            }
            return new AsyncStream() {
                private Object current

                @Override
                Awaitable<Boolean> moveNext() {
                    boolean hasNext = iter.hasNext()
                    if (hasNext) current = iter.next()
                    return Awaitable.of(hasNext)
                }

                @Override
                Object getCurrent() { return current }
            }
        }
    }

    // =====================================================================
    // Spring-style service stubs (simulate Spring @Async / WebFlux)
    // =====================================================================

    /** Simulates a Spring {@code @Async} service returning CompletableFuture. */
    static class SpringStyleService {
        CompletableFuture<Map> fetchUser(long id) {
            CompletableFuture.supplyAsync { [id: id, name: "User${id}"] }
        }

        CompletionStage<String> processAsync(String input) {
            CompletableFuture.supplyAsync { input.toUpperCase() }
        }
    }

    /** Simulates a Spring WebFlux controller returning Reactor types. */
    static class SpringWebFluxStyleController {
        Mono<Map> getUser(long id) {
            Mono.just([id: id, name: "User${id}"])
        }

        Flux<Map> listUsers() {
            Flux.just(
                [id: 1L, name: "User1"],
                [id: 2L, name: "User2"],
                [id: 3L, name: "User3"]
            )
        }
    }
}
