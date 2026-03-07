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
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.apache.groovy.runtime.async.GroovyPromise
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import static groovy.test.GroovyAssert.assertScript

/**
 * Integration tests for Groovy async/await with third-party reactive
 * frameworks: Reactor (Spring WebFlux foundation), RxJava 3, and Spring-style
 * async patterns.
 * <p>
 * All test logic is compiled via {@link groovy.test.GroovyAssert#assertScript}
 * to verify that the async/await syntax works correctly from the developer's
 * perspective.  The {@link AwaitableAdapter} SPI adapters are registered in
 * {@link #registerAdapters()} so that the globally shared
 * {@link AwaitableAdapterRegistry} makes Reactor and RxJava types transparent
 * to each script.
 */
class AsyncFrameworkIntegrationTest {

    private ReactorAwaitableAdapter reactorAdapter
    private RxJavaAwaitableAdapter rxJavaAdapter

    /** Shared import preamble for Reactor-based assertScript tests. */
    private static final String REACTOR_PREAMBLE = '''\
import groovy.concurrent.*
import org.apache.groovy.runtime.async.GroovyPromise
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
'''

    /** Shared import preamble for RxJava-based assertScript tests. */
    private static final String RXJAVA_PREAMBLE = '''\
import groovy.concurrent.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Flowable
'''

    /** Shared import preamble including both Reactor and RxJava. */
    private static final String ALL_PREAMBLE = '''\
import groovy.concurrent.*
import org.apache.groovy.runtime.async.GroovyPromise
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
'''

    /** Spring-style service stubs defined inline for assertScript tests. */
    private static final String SPRING_STUBS = '''
class SpringStyleService {
    CompletableFuture<Map> fetchUser(long id) {
        CompletableFuture.supplyAsync { [id: id, name: "User${id}"] }
    }
    CompletionStage<String> processAsync(String input) {
        CompletableFuture.supplyAsync { input.toUpperCase() }
    }
}
class SpringWebFluxStyleController {
    Mono<Map> getUser(long id) {
        Mono.just([id: id, name: "User${id}"])
    }
    Flux<Map> listUsers() {
        Flux.just([id: 1L, name: "User1"], [id: 2L, name: "User2"], [id: 3L, name: "User3"])
    }
}
'''

    @BeforeEach
    void registerAdapters() {
        reactorAdapter = new ReactorAwaitableAdapter()
        rxJavaAdapter = new RxJavaAwaitableAdapter()
        AwaitableAdapterRegistry.register(reactorAdapter)
        AwaitableAdapterRegistry.register(rxJavaAdapter)
    }

    @AfterEach
    void resetExecutor() {
        Awaitable.setExecutor(null)
        if (rxJavaAdapter != null) AwaitableAdapterRegistry.unregister(rxJavaAdapter)
        if (reactorAdapter != null) AwaitableAdapterRegistry.unregister(reactorAdapter)
    }

    // =====================================================================
    // Reactor (Spring WebFlux foundation) tests
    // =====================================================================

    @Test
    void testAwaitReactorMono() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.just("reactor-value")
            def result = await(mono)
            assert result == "reactor-value"
        '''
    }

    @Test
    void testAwaitReactorMonoWithMap() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.just(10).map { it * 3 }
            def result = await(mono)
            assert result == 30
        '''
    }

    @Test
    void testAwaitReactorMonoEmpty() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.empty()
            def result = await(mono)
            assert result == null
        '''
    }

    @Test
    void testAwaitReactorMonoError() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.error(new IllegalStateException("reactor error"))
            try {
                await(mono)
                assert false : "should have thrown"
            } catch (IllegalStateException e) {
                assert e.message == "reactor error"
            }
        '''
    }

    @Test
    void testAwaitReactorMonoDeferred() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.fromSupplier { "deferred" }
            def result = await(mono)
            assert result == "deferred"
        '''
    }

    @Test
    void testAwaitReactorMonoChain() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.just("hello")
                .map { it.toUpperCase() }
                .flatMap { s -> Mono.just("${s}!") }
            def result = await(mono)
            assert result == "HELLO!"
        '''
    }

    @Test
    void testForAwaitReactorFlux() {
        assertScript REACTOR_PREAMBLE + '''
            def flux = Flux.just(1, 2, 3, 4, 5)
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitReactorFluxWithOperators() {
        assertScript REACTOR_PREAMBLE + '''
            def flux = Flux.range(1, 10).filter { it % 2 == 0 }.map { it * 10 }
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [20, 40, 60, 80, 100]
        '''
    }

    @Test
    void testForAwaitReactorFluxEmpty() {
        assertScript REACTOR_PREAMBLE + '''
            def flux = Flux.empty()
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testReactorMonoToAwaitableApi() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.just(42)
            Awaitable<Integer> awaitable = Awaitable.from(mono)
            assert awaitable.get() == 42
            assert awaitable.isDone()
        '''
    }

    @Test
    void testReactorMonoAwaitableThen() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.just(5)
            Awaitable<Integer> a = Awaitable.from(mono)
            Awaitable<Integer> doubled = a.then { it * 2 }
            assert doubled.get() == 10
        '''
    }

    // =====================================================================
    // RxJava tests
    // =====================================================================

    @Test
    void testAwaitRxJavaSingle() {
        assertScript RXJAVA_PREAMBLE + '''
            def single = Single.just("rxjava-value")
            def result = await(single)
            assert result == "rxjava-value"
        '''
    }

    @Test
    void testAwaitRxJavaSingleWithMap() {
        assertScript RXJAVA_PREAMBLE + '''
            def single = Single.just(7).map { it * 6 }
            def result = await(single)
            assert result == 42
        '''
    }

    @Test
    void testAwaitRxJavaSingleError() {
        assertScript RXJAVA_PREAMBLE + '''
            def single = Single.error(new RuntimeException("rx error"))
            try {
                await(single)
                assert false : "should have thrown"
            } catch (RuntimeException e) {
                assert e.message == "rx error"
            }
        '''
    }

    @Test
    void testAwaitRxJavaMaybe() {
        assertScript RXJAVA_PREAMBLE + '''
            def maybe = Maybe.just("maybe-value")
            def result = await(maybe)
            assert result == "maybe-value"
        '''
    }

    @Test
    void testAwaitRxJavaMaybeEmpty() {
        assertScript RXJAVA_PREAMBLE + '''
            def maybe = Maybe.empty()
            def result = await(maybe)
            assert result == null
        '''
    }

    @Test
    void testForAwaitRxJavaObservable() {
        assertScript RXJAVA_PREAMBLE + '''
            def observable = Observable.just("a", "b", "c")
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == ["a", "b", "c"]
        '''
    }

    @Test
    void testForAwaitRxJavaObservableWithOperators() {
        assertScript RXJAVA_PREAMBLE + '''
            def observable = Observable.range(1, 5).filter { it > 2 }.map { "item-${it}" }
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == ["item-3", "item-4", "item-5"]
        '''
    }

    @Test
    void testForAwaitRxJavaFlowable() {
        assertScript RXJAVA_PREAMBLE + '''
            def flowable = Flowable.just(10, 20, 30)
            def results = []
            for await (item in flowable) {
                results << item
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testRxJavaSingleToAwaitableApi() {
        assertScript RXJAVA_PREAMBLE + '''
            def single = Single.just("adapted")
            Awaitable<String> awaitable = Awaitable.from(single)
            assert awaitable.get() == "adapted"
            assert awaitable.isDone()
        '''
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
        assertScript REACTOR_PREAMBLE + SPRING_STUBS + '''
            def springService = new SpringStyleService()
            def result = await(springService.fetchUser(1L))
            assert result == [id: 1L, name: "User1"]
        '''
    }

    @Test
    void testSpringStyleCompletionStageService() {
        assertScript REACTOR_PREAMBLE + SPRING_STUBS + '''
            def springService = new SpringStyleService()
            CompletionStage<String> stage = springService.processAsync("hello")
            def result = await(stage)
            assert result == "HELLO"
        '''
    }

    @Test
    void testSpringWebFluxMonoEndpoint() {
        assertScript REACTOR_PREAMBLE + SPRING_STUBS + '''
            def controller = new SpringWebFluxStyleController()
            def mono = controller.getUser(42L)
            def result = await(mono)
            assert result == [id: 42L, name: "User42"]
        '''
    }

    @Test
    void testSpringWebFluxFluxEndpoint() {
        assertScript REACTOR_PREAMBLE + SPRING_STUBS + '''
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

    @Test
    void testSpringStyleAsyncChainWithReactorMono() {
        assertScript REACTOR_PREAMBLE + SPRING_STUBS + '''
            def userService = new SpringWebFluxStyleController()
            def enriched = userService.getUser(1L).map { user -> user + [role: "admin"] }
            def result = await(enriched)
            assert result.name == "User1"
            assert result.role == "admin"
        '''
    }

    @Test
    void testSpringStyleMultipleAsyncCalls() {
        assertScript REACTOR_PREAMBLE + SPRING_STUBS + '''
            def service = new SpringStyleService()
            def f1 = service.fetchUser(1L)
            def f2 = service.fetchUser(2L)
            def f3 = service.fetchUser(3L)
            def results = [await(f1), await(f2), await(f3)]
            assert results.collect { it.name } == ["User1", "User2", "User3"]
        '''
    }

    @Test
    void testSpringStyleCompletionStageAdapter() {
        assertScript REACTOR_PREAMBLE + '''
            CompletionStage<String> stage = CompletableFuture.supplyAsync { "stage-value" }
            Awaitable<String> awaitable = Awaitable.from(stage)
            assert awaitable.get() == "stage-value"
        '''
    }

    // =====================================================================
    // Cross-framework interoperability
    // =====================================================================

    @Test
    void testMixedFrameworkAwait() {
        assertScript ALL_PREAMBLE + '''
            def mono = Mono.just(10)
            def single = Single.just(20)
            def cf = CompletableFuture.supplyAsync { 30 }
            def r1 = await(mono)
            def r2 = await(single)
            def r3 = await(cf)
            assert r1 + r2 + r3 == 60
        '''
    }

    @Test
    void testReactorToRxJavaInterop() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.just("from-reactor")
            Awaitable<String> awaitable = Awaitable.from(mono)
            CompletableFuture<String> cf = awaitable.toCompletableFuture()
            assert cf.get() == "from-reactor"
        '''
    }

    @Test
    void testAwaitableExceptionallyWithReactor() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.error(new RuntimeException("fail"))
            Awaitable<String> awaitable = Awaitable.from(mono)
            Awaitable<String> recovered = awaitable.exceptionally { "recovered" }
            assert recovered.get() == "recovered"
        '''
    }

    @Test
    void testAwaitableThenComposeAcrossFrameworks() {
        assertScript REACTOR_PREAMBLE + '''
            def mono = Mono.just(5)
            Awaitable<Integer> a = Awaitable.from(mono)
            Awaitable<Integer> composed = a.thenCompose { val ->
                GroovyPromise.of(CompletableFuture.supplyAsync { val * 10 })
            }
            assert composed.get() == 50
        '''
    }

    // =====================================================================
    // Reactive Programming patterns (Jochen's examples from GROOVY-9381)
    // =====================================================================

    @Test
    void testForAwaitConsumingReactorFlux() {
        assertScript REACTOR_PREAMBLE + '''
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

    @Test
    void testForAwaitConsumingRxJavaFlowable() {
        assertScript RXJAVA_PREAMBLE + '''
            def flowable = Flowable.range(1, 5).map { "item-${it}" }
            def results = []
            for await (item in flowable) {
                results << item
            }
            assert results == ['item-1', 'item-2', 'item-3', 'item-4', 'item-5']
        '''
    }

    @Test
    void testForAwaitConsumingRxJavaObservable() {
        assertScript RXJAVA_PREAMBLE + '''
            def observable = Observable.just('alpha', 'beta', 'gamma')
            def results = []
            for await (item in observable) {
                results << item
            }
            assert results == ['alpha', 'beta', 'gamma']
        '''
    }

    @Test
    void testForAwaitReactorFluxWithAsyncProcessing() {
        assertScript REACTOR_PREAMBLE + '''
            def flux = Flux.just(1, 2, 3)
            def results = []
            for await (val in flux) {
                def doubled = await CompletableFuture.supplyAsync { val * 2 }
                results << doubled
            }
            assert results == [2, 4, 6]
        '''
    }

    @Test
    void testForAwaitReactorFluxWithDeferCleanup() {
        assertScript REACTOR_PREAMBLE + '''
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

    // =====================================================================
    // Adapter implementation classes (used by @BeforeEach/@AfterEach)
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

    // ===== Adapter registry tests =====

    @Test
    void testAdapterRegistryRegisterAndUnregister() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.AwaitableAdapterRegistry

            // Create a custom adapter for a custom type
            class CustomResult { String data }

            def adapter = new AwaitableAdapter() {
                boolean supportsAwaitable(Class<?> type) {
                    CustomResult.isAssignableFrom(type)
                }
                def <T> Awaitable<T> toAwaitable(Object source) {
                    Awaitable.of(((CustomResult) source).data)
                }
            }

            // Register and verify it works
            def handle = AwaitableAdapterRegistry.register(adapter)
            try {
                def result = Awaitable.from(new CustomResult(data: "hello"))
                assert await(result) == "hello"
            } finally {
                handle.close()
            }

            // After close, the adapter should be removed
            try {
                Awaitable.from(new CustomResult(data: "fail"))
                assert false : "Should throw"
            } catch (IllegalArgumentException e) {
                // expected
            }
        '''
    }

    @Test
    void testAdapterRegistryExplicitUnregister() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapter
            import groovy.concurrent.AwaitableAdapterRegistry

            def adapter = new AwaitableAdapter() {
                boolean supportsAwaitable(Class<?> type) { false }
                def <T> Awaitable<T> toAwaitable(Object source) { null }
            }

            AwaitableAdapterRegistry.register(adapter)
            assert AwaitableAdapterRegistry.unregister(adapter) == true
            assert AwaitableAdapterRegistry.unregister(adapter) == false
        '''
    }

    @Test
    void testAdapterRegistrySetBlockingExecutor() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AwaitableAdapterRegistry
            import java.util.concurrent.Executors
            import java.util.concurrent.FutureTask

            def pool = Executors.newSingleThreadExecutor()
            try {
                // Set blocking executor so Future adaptation uses it
                AwaitableAdapterRegistry.setBlockingExecutor(pool)

                // Create a plain java.util.concurrent.Future
                def future = new FutureTask<String>({ "from-blocking-future" })
                pool.submit(future)

                def aw = Awaitable.from(future)
                assert await(aw) == "from-blocking-future"
            } finally {
                AwaitableAdapterRegistry.setBlockingExecutor(null)
                pool.shutdown()
            }
        '''
    }

    // ================================================================
    // AsyncStream: empty()
    // ================================================================

}
