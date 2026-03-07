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

import groovy.concurrent.Awaitable
import groovy.concurrent.AwaitableAdapterRegistry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.util.ServiceLoader

import static groovy.test.GroovyAssert.assertScript

/**
 * Integration tests for Groovy async/await with the built-in adapter registry:
 * custom adapter lifecycle, blocking executor configuration, and
 * Spring-style {@code CompletableFuture}/{@code CompletionStage} patterns.
 * <p>
 * Framework-specific tests for Reactor and RxJava have been moved to the
 * dedicated {@code groovy-reactor} and {@code groovy-rxjava} subprojects
 * respectively, where the adapters are auto-discovered via
 * {@link ServiceLoader}.
 */
class AsyncFrameworkIntegrationTest {

    /** Shared import preamble for assertScript tests. */
    private static final String PREAMBLE = '''\
import groovy.concurrent.*
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
'''

    @AfterEach
    void resetExecutor() {
        Awaitable.setExecutor(null)
    }

    // =====================================================================
    // Spring-style async pattern tests
    //
    // Spring @Async methods return CompletableFuture (Spring 6+) or
    // CompletionStage. These tests demonstrate seamless interoperability.
    // =====================================================================

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
            def results = [await f1, await f2, await f3]
            assert results.collect { it.name } == ["User1", "User2", "User3"]
        '''
    }

    @Test
    void testSpringStyleCompletionStageAdapter() {
        assertScript PREAMBLE + '''
            CompletionStage<String> stage = CompletableFuture.supplyAsync { "stage-value" }
            Awaitable<String> awaitable = Awaitable.from(stage)
            assert awaitable.get() == "stage-value"
        '''
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

                // Create a plain Future
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
}
