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
            import org.apache.groovy.runtime.async.AsyncSupport

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
}
