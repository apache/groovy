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
package org.apache.groovy.runtime.async

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Integration coverage for the upgraded async runtime through real Groovy
 * syntax and metaprogramming features.
 */
final class AsyncRuntimeEnhancementTest {

    @Test
    void testDeepAsyncExceptionChainsRetainCatchSemantics() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable
            import static groovy.test.GroovyAssert.shouldFail

            class Service {
                async leaf() {
                    await Awaitable.delay(10)
                    throw new IOException('leaf exploded')
                }

                async middle() {
                    await leaf()
                }

                async top() {
                    try {
                        await middle()
                    } catch (IOException ioe) {
                        throw new IllegalStateException("trace=${AsyncContext.current()['traceId']}", ioe)
                    }
                }
            }

            AsyncContext.with([traceId: 'req-42']) {
                def failure = shouldFail(IllegalStateException) {
                    await new Service().top()
                }

                assert failure.message == 'trace=req-42'
                assert failure.cause instanceof IOException
                assert failure.cause.message == 'leaf exploded'
            }
        '''
    }

    @Test
    void testDeferWaitsForReturnedAsyncCleanup() {
        assertScript '''
            import groovy.concurrent.Awaitable

            class Resource {
                final List<String> log = []

                Awaitable<Void> closeAsync(String name) {
                    Awaitable.go {
                        await Awaitable.delay(15)
                        log << "close:${name}"
                        null
                    }
                }
            }

            class Service {
                final Resource resource = new Resource()

                async work() {
                    defer { resource.closeAsync('outer') }
                    defer { resource.closeAsync('inner') }
                    resource.log << 'body'
                    return resource.log
                }
            }

            def service = new Service()
            assert await(service.work()) == ['body', 'close:inner', 'close:outer']
        '''
    }

    @Test
    void testGoChannelsAndMetaprogrammingRemainGroovyFriendly() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel

            String.metaClass.shout = { -> delegate.toUpperCase() + '!' }

            try {
                def result = AsyncScope.withScope { scope ->
                    def channel = AsyncChannel.create(1)

                    def producer = Awaitable.go {
                        await channel.send('groovy'.shout())
                        'produced'
                    }

                    def consumer = scope.async {
                        def prefix = 'hello'
                        def formatter = { value -> "${prefix} ${value}" }
                        formatter(await channel.receive())
                    }

                    def selected = await Awaitable.any(
                        consumer,
                        Awaitable.delay(500).then { 'timeout' }
                    )

                    [message: selected, producer: await producer, childCount: scope.childCount]
                }

                assert result.message == 'hello GROOVY!'
                assert result.producer == 'produced'
                assert result.childCount == 2
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(String)
            }
        '''
    }

    @Test
    void testAsyncContextPropagatesToAsyncMethodsAndGenerators() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            class ContextAwareService {
                async fetchWithContext() {
                    assert AsyncContext.current()['traceId'] == 'gen-trace'
                    await Awaitable.delay(10)
                    "value-${AsyncContext.current()['traceId']}"
                }

                async generateItems() {
                    for (int i = 0; i < 3; i++) {
                        assert AsyncContext.current()['traceId'] == 'gen-trace'
                        yield return "item-$i-${AsyncContext.current()['traceId']}"
                    }
                }
            }

            AsyncContext.with([traceId: 'gen-trace']) {
                def svc = new ContextAwareService()
                assert await(svc.fetchWithContext()) == 'value-gen-trace'

                def items = []
                for await (item in svc.generateItems()) {
                    items << item
                }
                assert items == ['item-0-gen-trace', 'item-1-gen-trace', 'item-2-gen-trace']
            }
        '''
    }

    @Test
    void testDeferPreservesAsyncContextAtRegistrationTime() {
        assertScript '''
            import groovy.concurrent.AsyncContext
            import groovy.concurrent.Awaitable

            class DeferContextService {
                final List<String> log = []

                async work() {
                    AsyncContext.current()['phase'] = 'init'
                    defer {
                        log << "cleanup-phase:${AsyncContext.current()['phase']}".toString()
                        null
                    }
                    AsyncContext.current()['phase'] = 'body'
                    log << "body-phase:${AsyncContext.current()['phase']}".toString()
                    return log
                }
            }

            def svc = new DeferContextService()
            def result = await svc.work()
            assert result.contains('body-phase:body')
            assert result.contains('cleanup-phase:init')
        '''
    }

    @Test
    void testHighConcurrencyChannelPipelineWithAsyncMethods() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Awaitable
            import groovy.concurrent.AsyncChannel
            import java.util.concurrent.CopyOnWriteArrayList

            class Pipeline {
                static async processPipeline(int itemCount) {
                    def stage1 = AsyncChannel.create(16)
                    def stage2 = AsyncChannel.create(16)
                    def results = new CopyOnWriteArrayList()

                    AsyncScope.withScope { scope ->
                        // Producer
                        scope.async {
                            for (int i = 0; i < itemCount; i++) {
                                await stage1.send(i)
                            }
                            null
                        }

                        // Transformer (2 workers)
                        2.times {
                            scope.async {
                                for (int i = 0; i < itemCount / 2; i++) {
                                    int value = await stage1.receive()
                                    await stage2.send(value * 10)
                                }
                                null
                            }
                        }

                        // Consumer
                        scope.async {
                            for (int i = 0; i < itemCount; i++) {
                                results.add(await stage2.receive())
                            }
                            null
                        }
                    }

                    results.sort()
                }
            }

            def results = await Pipeline.processPipeline(20)
            assert results.size() == 20
            assert results == (0..<20).collect { it * 10 }
        '''
    }
}
