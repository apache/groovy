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
            import org.apache.groovy.runtime.async.AsyncSupport

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
            import org.apache.groovy.runtime.async.AsyncSupport

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
}
