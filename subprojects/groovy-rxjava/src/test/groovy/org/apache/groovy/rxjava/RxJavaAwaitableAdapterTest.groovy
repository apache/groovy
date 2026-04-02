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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class RxJavaAwaitableAdapterTest {

    @Test
    void testAwaitSingle() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def single = Single.just('hello')
            def result = await Awaitable.from(single)
            assert result == 'hello'
        '''
    }

    @Test
    void testAwaitMaybe() {
        assertScript '''
            import io.reactivex.rxjava3.core.Maybe
            import groovy.concurrent.Awaitable

            def maybe = Maybe.just(42)
            def result = await Awaitable.from(maybe)
            assert result == 42
        '''
    }

    @Test
    void testAwaitMaybeEmpty() {
        assertScript '''
            import io.reactivex.rxjava3.core.Maybe
            import groovy.concurrent.Awaitable

            def maybe = Maybe.empty()
            def result = await Awaitable.from(maybe)
            assert result == null
        '''
    }

    @Test
    void testForAwaitObservable() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.just(1, 2, 3)
            def results = []
            for await (item in obs) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testForAwaitFlowable() {
        assertScript '''
            import io.reactivex.rxjava3.core.Flowable

            def flow = Flowable.just('a', 'b', 'c')
            def results = []
            for await (item in flow) {
                results << item
            }
            assert results == ['a', 'b', 'c']
        '''
    }

    @Test
    void testAdapterDiscovery() {
        def adapter = new RxJavaAwaitableAdapter()
        assert adapter.supportsAwaitable(Single)
        assert adapter.supportsAwaitable(Maybe)
        assert adapter.supportsAwaitable(Completable)
        assert !adapter.supportsAwaitable(Observable)
        assert adapter.supportsIterable(Observable)
        assert adapter.supportsIterable(Flowable)
        assert !adapter.supportsIterable(Single)
    }

    @Test
    void testAwaitSingleWithMap() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def single = Single.just(7).map { it * 6 }
            def result = await Awaitable.from(single)
            assert result == 42
        '''
    }

    @Test
    void testAwaitSingleError() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def single = Single.error(new RuntimeException('rx error'))
            try {
                await Awaitable.from(single)
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'rx error'
            }
        '''
    }

    @Test
    void testAwaitSingleFromCallable() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def single = Single.fromCallable { 'from-callable' }
            def result = await Awaitable.from(single)
            assert result == 'from-callable'
        '''
    }

    @Test
    void testAwaitSingleDeferred() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def single = Single.defer { Single.just('deferred') }
            def result = await Awaitable.from(single)
            assert result == 'deferred'
        '''
    }

    @Test
    void testAwaitSingleZip() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def s1 = Single.just(10)
            def s2 = Single.just(20)
            def zipped = Single.zip(s1, s2) { a, b -> a + b }
            def result = await Awaitable.from(zipped)
            assert result == 30
        '''
    }

    @Test
    void testAwaitSingleFlatMap() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def single = Single.just('hello')
                .map { it.toUpperCase() }
                .flatMap { s -> Single.just(s + '!') }
            def result = await Awaitable.from(single)
            assert result == 'HELLO!'
        '''
    }

    @Test
    void testAwaitMaybeError() {
        assertScript '''
            import io.reactivex.rxjava3.core.Maybe
            import groovy.concurrent.Awaitable

            def maybe = Maybe.error(new RuntimeException('maybe-err'))
            try {
                await Awaitable.from(maybe)
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'maybe-err'
            }
        '''
    }

    @Test
    void testAwaitMaybeFromCallable() {
        assertScript '''
            import io.reactivex.rxjava3.core.Maybe
            import groovy.concurrent.Awaitable

            def maybe = Maybe.fromCallable { 'callable-result' }
            def result = await Awaitable.from(maybe)
            assert result == 'callable-result'
        '''
    }

    @Test
    void testAwaitMaybeWithMap() {
        assertScript '''
            import io.reactivex.rxjava3.core.Maybe
            import groovy.concurrent.Awaitable

            def maybe = Maybe.just('hello').map { it.toUpperCase() }
            def result = await Awaitable.from(maybe)
            assert result == 'HELLO'
        '''
    }

    @Test
    void testAwaitCompletable() {
        assertScript '''
            import io.reactivex.rxjava3.core.Completable
            import groovy.concurrent.Awaitable

            def ran = false
            def completable = Completable.fromAction { ran = true }
            await Awaitable.from(completable)
            assert ran == true
        '''
    }

    @Test
    void testAwaitCompletableError() {
        assertScript '''
            import io.reactivex.rxjava3.core.Completable
            import groovy.concurrent.Awaitable

            def completable = Completable.error(new RuntimeException('comp-err'))
            try {
                await Awaitable.from(completable)
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'comp-err'
            }
        '''
    }

    @Test
    void testSingleToAwaitableApi() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.from(Single.just('rx'))
            assert a.isDone()
            assert await(a) == 'rx'
        '''
    }

    @Test
    void testMaybeToAwaitableApi() {
        assertScript '''
            import io.reactivex.rxjava3.core.Maybe
            import groovy.concurrent.Awaitable

            Awaitable<Integer> a = Awaitable.from(Maybe.just(99))
            assert a.isDone()
            assert await(a) == 99
        '''
    }

    @Test
    void testAwaitableThenWithSingle() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.from(Single.just('hello'))
            Awaitable<String> transformed = a.then { it.toUpperCase() }
            assert await(transformed) == 'HELLO'
        '''
    }

    @Test
    void testAwaitableExceptionallyWithSingle() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.from(Single.error(new RuntimeException('fail')))
            Awaitable<String> recovered = a.exceptionally { 'recovered' }
            assert await(recovered) == 'recovered'
        '''
    }

    @Test
    void testAwaitableThenComposeWithSingle() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            Awaitable<Integer> a = Awaitable.from(Single.just(5))
            Awaitable<Integer> composed = a.thenCompose { Awaitable.of(it * 10) }
            assert await(composed) == 50
        '''
    }

    @Test
    void testAwaitableHandleWithSingle() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            Awaitable<String> a = Awaitable.from(Single.just('ok'))
            Awaitable<String> handled = a.handle { val, err ->
                err == null ? "handled: $val" : "error"
            }
            assert await(handled) == 'handled: ok'
        '''
    }

    @Test
    void testSingleToCompletableFutureInterop() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable
            import java.util.concurrent.CompletableFuture

            def cf = Awaitable.from(Single.just(42)).toCompletableFuture()
            assert cf instanceof CompletableFuture
            assert cf.get() == 42
        '''
    }

    @Test
    void testSingleIsCompletedExceptionally() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def a = Awaitable.from(Single.error(new RuntimeException('fail')))
            assert a.isCompletedExceptionally()
        '''
    }

    @Test
    void testForAwaitObservableWithOperators() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.just(1, 2, 3, 4, 5).filter { it % 2 == 0 }.map { it * 10 }
            def results = []
            for await (item in obs) {
                results << item
            }
            assert results == [20, 40]
        '''
    }

    @Test
    void testForAwaitObservableEmpty() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.empty()
            def results = []
            for await (item in obs) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testForAwaitObservableRange() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.range(1, 5)
            def results = []
            for await (item in obs) {
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitObservableFlatMap() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.just(1, 2).flatMap { n ->
                Observable.just(n, n * 10)
            }
            def results = []
            for await (item in obs) {
                results << item
            }
            assert results.sort() == [1, 2, 10, 20]
        '''
    }

    @Test
    void testForAwaitObservableDistinct() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.just(1, 2, 2, 3, 3, 3).distinct()
            def results = []
            for await (item in obs) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testForAwaitObservableTake() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.range(1, 100).take(3)
            def results = []
            for await (item in obs) {
                results << item
            }
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testForAwaitFlowableEmpty() {
        assertScript '''
            import io.reactivex.rxjava3.core.Flowable

            def flow = Flowable.empty()
            def results = []
            for await (item in flow) {
                results << item
            }
            assert results == []
        '''
    }

    @Test
    void testForAwaitFlowableRange() {
        assertScript '''
            import io.reactivex.rxjava3.core.Flowable

            def flow = Flowable.range(1, 5)
            def results = []
            for await (item in flow) {
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitFlowableWithBackPressure() {
        assertScript '''
            import io.reactivex.rxjava3.core.Flowable

            def flow = Flowable.range(1, 100)
            def results = []
            for await (item in flow) {
                results << item
            }
            assert results.size() == 100
            assert results[0] == 1
            assert results[99] == 100
        '''
    }

    @Test
    void testForAwaitFlowableEarlyBreak() {
        assertScript '''
            import io.reactivex.rxjava3.core.Flowable

            def flow = Flowable.range(1, 1000)
            def results = []
            for await (item in flow) {
                if (item > 5) break
                results << item
            }
            assert results == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void testForAwaitObservableWithAsyncProcessing() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.just(1, 2, 3)
            def results = []
            for await (item in obs) {
                def doubled = await async { item * 2 }
                results << doubled
            }
            assert results == [2, 4, 6]
        '''
    }

    @Test
    void testAdapterToAwaitableDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        def result = adapter.toAwaitable(Single.just(42))
        assert result.get() == 42
    }

    @Test
    void testAdapterToAwaitableMaybeDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        def result = adapter.toAwaitable(Maybe.just('hi'))
        assert result.get() == 'hi'
    }

    @Test
    void testAdapterToAwaitableMaybeEmptyDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        def result = adapter.toAwaitable(Maybe.empty())
        assert result.get() == null
    }

    @Test
    void testAdapterToAwaitableCompletableDirectly() {
        def adapter = new RxJavaAwaitableAdapter()
        def result = adapter.toAwaitable(Completable.complete())
        assert result.get() == null
    }

    @Test
    void testAdapterToAwaitableUnsupportedType() {
        def adapter = new RxJavaAwaitableAdapter()
        try {
            adapter.toAwaitable('not-rx')
            assert false : 'should have thrown'
        } catch (IllegalArgumentException e) {
            assert e.message.contains('Cannot convert')
        }
    }

    @Test
    void testAdapterToBlockingIterableObservable() {
        def adapter = new RxJavaAwaitableAdapter()
        def iter = adapter.toBlockingIterable(Observable.just(1, 2, 3))
        assert iter.collect() == [1, 2, 3]
    }

    @Test
    void testAdapterToBlockingIterableFlowable() {
        def adapter = new RxJavaAwaitableAdapter()
        def iter = adapter.toBlockingIterable(Flowable.just('a', 'b'))
        assert iter.collect() == ['a', 'b']
    }

    @Test
    void testAdapterToBlockingIterableUnsupportedType() {
        def adapter = new RxJavaAwaitableAdapter()
        try {
            adapter.toBlockingIterable('not-rx')
            assert false : 'should have thrown'
        } catch (IllegalArgumentException e) {
            assert e.message.contains('Cannot convert')
        }
    }

    @Test
    void testAwaitableAllWithSingles() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def s1 = Awaitable.from(Single.just(1))
            def s2 = Awaitable.from(Single.just(2))
            def s3 = Awaitable.from(Single.just(3))
            def results = await Awaitable.all(s1, s2, s3)
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testAwaitableAnyWithSingles() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def s1 = Awaitable.from(Single.just('first'))
            def s2 = Awaitable.from(Single.just('second'))
            def result = await Awaitable.any(s1, s2)
            assert result == 'first' || result == 'second'
        '''
    }

    @Test
    void testAwaitableAllSettledWithSingles() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def s1 = Awaitable.from(Single.just('ok'))
            def s2 = Awaitable.from(Single.error(new RuntimeException('fail')))
            def results = await Awaitable.allSettled(s1, s2)
            assert results[0].success && results[0].value == 'ok'
            assert results[1].failure && results[1].error.message == 'fail'
        '''
    }

    @Test
    void testAwaitSingleDirectlyViaAwaitObject() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single

            def result = await Single.just('direct')
            assert result == 'direct'
        '''
    }

    @Test
    void testForAwaitFlowableColonNotation() {
        assertScript '''
            import io.reactivex.rxjava3.core.Flowable

            def flow = Flowable.just(10, 20, 30)
            def results = []
            for await (item : flow) {
                results << item
            }
            assert results == [10, 20, 30]
        '''
    }

    @Test
    void testForAwaitObservableColonNotation() {
        assertScript '''
            import io.reactivex.rxjava3.core.Observable

            def obs = Observable.just('x', 'y', 'z')
            def results = []
            for await (item : obs) {
                results << item
            }
            assert results == ['x', 'y', 'z']
        '''
    }

    @Test
    void testSingleWithOrTimeout() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def a = Awaitable.from(Single.just('fast'))
            def result = await a.orTimeoutMillis(5000)
            assert result == 'fast'
        '''
    }

    @Test
    void testSingleWithCompleteOnTimeout() {
        assertScript '''
            import io.reactivex.rxjava3.core.Single
            import groovy.concurrent.Awaitable

            def a = Awaitable.from(Single.just('fast'))
            def result = await a.completeOnTimeoutMillis('fallback', 5000)
            assert result == 'fast'
        '''
    }

}
