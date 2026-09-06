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
package org.apache.groovy.concurrent.java;

import groovy.concurrent.AsyncScope;
import groovy.concurrent.Awaitable;
import groovy.concurrent.ParallelScope;
import groovy.concurrent.Pool;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.groovy.runtime.async.AsyncSupport.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PoolTest {

    @Test
    void scopeRunsOnAPool() {
        try (Pool pool = Pool.cpu()) {
            String result = AsyncScope.withScope(pool, scope -> {
                Awaitable<String> a = scope.async(() -> "hello");
                Awaitable<String> b = scope.async(() -> "world");
                return await(a) + " " + await(b);
            });
            assertEquals("hello world", result);
        }
    }

    @Test
    void parallelScopeWithPool() {
        int result = ParallelScope.withPool(4, scope -> {
            List<Awaitable<Integer>> tasks = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                int n = i;
                tasks.add(scope.async(() -> n * 10));
            }
            int sum = 0;
            for (Awaitable<Integer> task : tasks) {
                sum += await(task);
            }
            return sum;
        });
        assertEquals(100, result);
    }
}
