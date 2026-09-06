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
import groovy.concurrent.DataflowVariable;
import org.apache.groovy.runtime.async.AsyncSupport;
import org.junit.jupiter.api.Test;

import static org.apache.groovy.runtime.async.AsyncSupport.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataflowTest {

    @Test
    void dataflowVariablesBindInAnyOrder() {
        DataflowVariable<Integer> x = new DataflowVariable<>();
        DataflowVariable<Integer> y = new DataflowVariable<>();

        Awaitable<Integer> z = Awaitable.go(() -> await(x) + await(y));

        AsyncSupport.getExecutor().execute(() -> x.bind(10));
        AsyncSupport.getExecutor().execute(() -> y.bind(5));

        assertEquals(15, await(z));
    }

    @Test
    void structuredConcurrencyWithScope() {
        int result = AsyncScope.withScope(scope -> {
            Awaitable<Integer> a = scope.async(() -> 10);
            Awaitable<Integer> b = scope.async(() -> 20);
            return await(a) + await(b);
        });
        assertEquals(30, result);
    }
}
