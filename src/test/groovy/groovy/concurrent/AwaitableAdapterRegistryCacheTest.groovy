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
package groovy.concurrent

import org.junit.jupiter.api.Test

import java.lang.ref.SoftReference
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-12280: the adapter cache's {@code ClassValue} associations live as long as their
 * key classes, and the common keys are platform classes, so the cached adapter is held
 * through a {@link SoftReference} and re-resolved when the reference has been cleared.
 * These tests drive the cleared-reference protocol deterministically by clearing the
 * references directly rather than simulating memory pressure.
 */
final class AwaitableAdapterRegistryCacheTest {

    private static ClassValue<SoftReference<?>> cache() {
        def field = AwaitableAdapterRegistry.getDeclaredField('awaitableCache')
        field.accessible = true
        (field.get(null) as AtomicReference<ClassValue<SoftReference<?>>>).get()
    }

    @Test
    void testCacheIsPublishedThroughAnAtomicReference() {
        def field = AwaitableAdapterRegistry.getDeclaredField('awaitableCache')
        field.accessible = true
        assert field.get(null) instanceof AtomicReference
        assert cache() instanceof ClassValue
    }

    @Test
    void testCachedValueIsHeldThroughASoftReference() {
        AwaitableAdapterRegistry.toAwaitable(CompletableFuture.completedFuture(1))
        def entry = cache().get(CompletableFuture)
        assert entry instanceof SoftReference
        assert entry.get() instanceof AwaitableAdapter
    }

    @Test
    void testClearedReferenceIsReResolved() {
        def future = CompletableFuture.completedFuture(42)
        assert AwaitableAdapterRegistry.toAwaitable(future).get() == 42

        cache().get(CompletableFuture).clear()

        assert AwaitableAdapterRegistry.toAwaitable(future).get() == 42
    }

    @Test
    void testUnsupportedTypeStillFailsAfterItsReferenceIsCleared() {
        // "no adapter" must not be confused with "reference cleared": both before and after
        // clearing, an unsupported type resolves to no adapter and fails the same way
        def err = shouldFail(IllegalArgumentException) {
            AwaitableAdapterRegistry.toAwaitable(new Object())
        }
        assert err.message.contains('No Awaitable adapter found')

        cache().get(Object).clear()

        err = shouldFail(IllegalArgumentException) {
            AwaitableAdapterRegistry.toAwaitable(new Object())
        }
        assert err.message.contains('No Awaitable adapter found')
    }

    @Test
    void testRegistrationStillRebuildsTheCache() {
        def marker = new AwaitableAdapter() {
            @Override
            boolean supportsAwaitable(Class<?> type) { type == StringBuilder }

            @Override
            def <T> Awaitable<T> toAwaitable(Object source) { Awaitable.of(source.toString()) }
        }
        try {
            AwaitableAdapterRegistry.register(marker)
            assert AwaitableAdapterRegistry.toAwaitable(new StringBuilder('sb')).get() == 'sb'
        } finally {
            AwaitableAdapterRegistry.unregister(marker)
        }
        shouldFail(IllegalArgumentException) {
            AwaitableAdapterRegistry.toAwaitable(new StringBuilder('sb'))
        }
    }
}
