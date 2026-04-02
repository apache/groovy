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

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import static groovy.test.GroovyAssert.assertScript

final class ReactorAwaitableAdapterTest {

    @Test
    void testAwaitMono() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def mono = Mono.just('hello')
            def result = await Awaitable.from(mono)
            assert result == 'hello'
        '''
    }

    @Test
    void testAwaitMonoEmpty() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def mono = Mono.empty()
            def result = await Awaitable.from(mono)
            assert result == null
        '''
    }

    @Test
    void testForAwaitFlux() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.just(1, 2, 3)
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAdapterDiscovery() {
        def adapter = new ReactorAwaitableAdapter()
        assert adapter.supportsAwaitable(Mono)
        assert !adapter.supportsAwaitable(Flux)
        assert adapter.supportsIterable(Flux)
        assert !adapter.supportsIterable(Mono)
    }

    @Test
    void testAwaitMonoWithMap() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def mono = Mono.just(7).map { it * 6 }
            def result = await Awaitable.from(mono)
            assert result == 42
        '''
    }

    @Test
    void testAwaitMonoError() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def mono = Mono.error(new RuntimeException('mono-err'))
            try {
                await Awaitable.from(mono)
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'mono-err'
            }
        '''
    }

    @Test
    void testAwaitMonoDeferred() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def mono = Mono.defer { Mono.just('deferred') }
            def result = await Awaitable.from(mono)
            assert result == 'deferred'
        '''
    }

    @Test
    void testAwaitMonoFromCallable() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def mono = Mono.fromCallable { 'callable-result' }
            def result = await Awaitable.from(mono)
            assert result == 'callable-result'
        '''
    }

    @Test
    void testAwaitMonoChain() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def mono = Mono.just('hello')
                .map { it.toUpperCase() }
                .flatMap { s -> Mono.just(s + '!') }
            def result = await Awaitable.from(mono)
            assert result == 'HELLO!'
        '''
    }

    @Test
    void testAwaitMonoZipWith() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def m1 = Mono.just(10)
            def m2 = Mono.just(20)
            def zipped = Mono.zip(m1, m2).map { it.t1 + it.t2 }
            def result = await Awaitable.from(zipped)
            assert result == 30
        '''
    }

    @Test
    void testMonoToAwaitableApi() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.from(Mono.just('reactor'))
            assert a.isDone()
            assert await(a) == 'reactor'
        '''
    }

    @Test
    void testMonoAwaitableThen() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.from(Mono.just('hello'))
            Awaitable<String> transformed = a.then { it.toUpperCase() }
            assert await(transformed) == 'HELLO'
        '''
    }

    @Test
    void testAwaitableExceptionallyWithReactor() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.from(Mono.error(new RuntimeException('fail')))
            Awaitable<String> recovered = a.exceptionally { 'recovered' }
            assert await(recovered) == 'recovered'
        '''
    }

    @Test
    void testAwaitableThenComposeWithReactor() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            Awaitable<Integer> a = Awaitable.from(Mono.just(5))
            Awaitable<Integer> composed = a.thenCompose { Awaitable.of(it * 10) }
            assert await(composed) == 50
        '''
    }

    @Test
    void testAwaitableWhenCompleteWithReactor() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable
            import java.util.concurrent.atomic.AtomicReference

            def observed = new AtomicReference()
            Awaitable<String> a = Awaitable.from(Mono.just('hello'))
            a.whenComplete { val, err -> observed.set(val) }
            Thread.sleep(100)
            assert observed.get() == 'hello'
        '''
    }

    @Test
    void testAwaitableHandleWithReactor() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.from(Mono.just('ok'))
            Awaitable<String> handled = a.handle { val, err ->
                err == null ? "handled: $val" : "error"
            }
            assert await(handled) == 'handled: ok'
        '''
    }

    @Test
    void testMonoToCompletableFutureInterop() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def cf = Awaitable.from(Mono.just(42)).toCompletableFuture()
            assert cf instanceof CompletableFuture
            assert cf.get() == 42
        '''
    }

    @Test
    void testMonoIsDone() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def a = Awaitable.from(Mono.just('done'))
            assert a.isDone()
        '''
    }

    @Test
    void testMonoIsCompletedExceptionally() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def a = Awaitable.from(Mono.error(new RuntimeException('fail')))
            assert a.isCompletedExceptionally()
        '''
    }

    @Test
    void testForAwaitFluxWithOperators() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.just(1, 2, 3, 4, 5).filter { it % 2 == 0 }.map { it * 10 }
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [20, 40]
        '''
    }

    @Test
    void testForAwaitFluxEmpty() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.empty()
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testForAwaitFluxRange() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.range(1, 5)
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitFluxWithFlatMap() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.just(1, 2).flatMap { n ->
                Flux.just(n, n * 10)
            }
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results.sort() == [1, 2, 10, 20]
        '''
    }

    @Test
    void testForAwaitFluxWithTake() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.range(1, 100).take(3)
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testForAwaitFluxSingleElement() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.just('only')
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == ['only']
        '''
    }

    @Test
    void testForAwaitFluxWithDistinct() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.just(1, 2, 2, 3, 3, 3).distinct()
            def results = []
            for await (item in flux) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testForAwaitFluxEarlyBreak() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.range(1, 1000)
            def results = []
            for await (item in flux) {
                if (item > 5) break
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitFluxWithAsyncProcessing() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.just(1, 2, 3)
            def results = []
            for await (item in flux) {
                def doubled = await async { item * 2 }
                results << doubled
            }
            assert results == [2, 4, 6]
        '''
    }

    @Test
    void testAdapterToAwaitableDirectly() {
        def adapter = new ReactorAwaitableAdapter()
        def result = adapter.toAwaitable(Mono.just(42))
        assert result.get() == 42
    }

    @Test
    void testAdapterToAwaitableEmptyMono() {
        def adapter = new ReactorAwaitableAdapter()
        def result = adapter.toAwaitable(Mono.empty())
        assert result.get() == null
    }

    @Test
    void testAdapterToAwaitableErrorMono() {
        def adapter = new ReactorAwaitableAdapter()
        def result = adapter.toAwaitable(Mono.error(new RuntimeException('fail')))
        assert result.isCompletedExceptionally()
    }

    @Test
    void testAdapterToBlockingIterableFlux() {
        def adapter = new ReactorAwaitableAdapter()
        def iter = adapter.toBlockingIterable(Flux.just(1, 2, 3))
        assert iter.collect() == [1, 2, 3]
    }

    @Test
    void testAdapterToBlockingIterableEmptyFlux() {
        def adapter = new ReactorAwaitableAdapter()
        def iter = adapter.toBlockingIterable(Flux.empty())
        assert iter.collect() == []
    }

    @Test
    void testAdapterToAwaitableUnsupportedType() {
        def adapter = new ReactorAwaitableAdapter()
        try {
            adapter.toAwaitable('not-reactor')
            assert false : 'should have thrown'
        } catch (IllegalArgumentException e) {
            assert e.message.contains('Cannot convert')
        }
    }

    @Test
    void testAdapterToBlockingIterableUnsupportedType() {
        def adapter = new ReactorAwaitableAdapter()
        try {
            adapter.toBlockingIterable('not-reactor')
            assert false : 'should have thrown'
        } catch (IllegalArgumentException e) {
            assert e.message.contains('Cannot convert')
        }
    }

    @Test
    void testAwaitableAllWithMonos() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def m1 = Awaitable.from(Mono.just(1))
            def m2 = Awaitable.from(Mono.just(2))
            def m3 = Awaitable.from(Mono.just(3))
            def results = await Awaitable.all(m1, m2, m3)
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAwaitableAnyWithMonos() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def m1 = Awaitable.from(Mono.just('first'))
            def m2 = Awaitable.from(Mono.just('second'))
            def result = await Awaitable.any(m1, m2)
            assert result == 'first' || result == 'second'
        '''
    }

    @Test
    void testAwaitableAllSettledWithMonos() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def m1 = Awaitable.from(Mono.just('ok'))
            def m2 = Awaitable.from(Mono.error(new RuntimeException('fail')))
            def results = await Awaitable.allSettled(m1, m2)
            assert results[0].success && results[0].value == 'ok'
            assert results[1].failure && results[1].error.message == 'fail'
        '''
    }

    @Test
    void testMonoWithOrTimeout() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def a = Awaitable.from(Mono.just('fast'))
            def result = await a.orTimeoutMillis(5000)
            assert result == 'fast'
        '''
    }

    @Test
    void testMonoWithCompleteOnTimeout() {
        assertScript '''
            import reactor.core.publisher.Mono
            import groovy.concurrent.Awaitable

            def a = Awaitable.from(Mono.just('fast'))
            def result = await a.completeOnTimeoutMillis('fallback', 5000)
            assert result == 'fast'
        '''
    }

    @Test
    void testAwaitMonoDirectlyViaAwaitObject() {
        assertScript '''
            import reactor.core.publisher.Mono

            def result = await Mono.just('direct')
            assert result == 'direct'
        '''
    }

    @Test
    void testForAwaitFluxColonNotation() {
        assertScript '''
            import reactor.core.publisher.Flux

            def flux = Flux.just(10, 20, 30)
            def results = []
            for await (item : flux) {
                results << item
            }
            assert results == [10, 20, 30]
        '''
    }

}
