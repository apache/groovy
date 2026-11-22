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
package bugs

import org.junit.jupiter.api.Test

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * GROOVY-9656: {@code @Delegate} of an {@code ExecutorService} must select
 * {@code submit(Callable)} when a closure is cast to {@code Callable},
 * matching a direct executor service (historically it selected the
 * {@code Runnable} overload and returned {@code null}).
 */
final class Groovy9656 {

    static class MyService {
        @Delegate ExecutorService delegateExecutorService = Executors.newCachedThreadPool()
    }

    @Test
    void testDelegatedSubmitPrefersCallableForCastedClosure() {
        def delegated = new MyService()
        def actual = Executors.newCachedThreadPool()
        try {
            def delegatedMap = delegated.submit([call: { 42 }] as Callable).get()
            def delegatedClosure = delegated.submit({ 42 } as Callable).get()
            def actualMap = actual.submit([call: { 42 }] as Callable).get()
            def actualClosure = actual.submit({ 42 } as Callable).get()
            assert delegatedMap == 42
            assert delegatedClosure == 42
            assert actualMap == 42
            assert actualClosure == 42
        } finally {
            delegated.shutdownNow()
            actual.shutdownNow()
        }
    }
}
