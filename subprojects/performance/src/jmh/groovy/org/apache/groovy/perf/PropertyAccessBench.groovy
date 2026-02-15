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
package org.apache.groovy.perf

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * Tests the performance of Groovy property access patterns including
 * field read/write, getter/setter dispatch, dynamically-typed property
 * access, map bracket and dot-property notation, and chained property
 * resolution.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class PropertyAccessBench {
    static final int ITERATIONS = 1_000_000

    int instanceField = 42
    String stringProperty = "hello"

    // Explicit getter/setter for comparison
    private int _backingField = 10
    int getBackingField() { _backingField }
    void setBackingField(int value) { _backingField = value }

    /**
     * Read/write a public field — the simplest property access path.
     */
    @Benchmark
    void fieldReadWrite(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            instanceField = i
            sum += instanceField
        }
        bh.consume(sum)
    }

    /**
     * Read/write through explicit getter/setter methods —
     * tests the overhead of Groovy's property-to-getter/setter dispatch.
     */
    @Benchmark
    void getterSetterAccess(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            backingField = i
            sum += backingField
        }
        bh.consume(sum)
    }

    /**
     * Property access on a dynamically typed variable —
     * tests the cost when the compiler cannot statically resolve the property.
     */
    @Benchmark
    void dynamicTypedPropertyAccess(Blackhole bh) {
        def obj = this
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            obj.instanceField = i
            sum += obj.instanceField
        }
        bh.consume(sum)
    }

    /**
     * Map-style property access using bracket notation —
     * tests Groovy's map-like property access on a POGO.
     */
    @Benchmark
    void mapStyleAccess(Blackhole bh) {
        Map<String, Integer> map = [a: 1, b: 2, c: 3]
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            map['a'] = i
            sum += map['a']
        }
        bh.consume(sum)
    }

    /**
     * Dot-property access on a Map — Groovy allows map.key syntax.
     */
    @Benchmark
    void mapDotPropertyAccess(Blackhole bh) {
        Map<String, Integer> map = [a: 1, b: 2, c: 3]
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            map.a = i
            sum += map.a
        }
        bh.consume(sum)
    }

    /**
     * Chained property access — tests multiple property resolutions
     * in a single expression.
     */
    @Benchmark
    void chainedPropertyAccess(Blackhole bh) {
        List<String> list = ["hello", "world"]
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += list.first().length()
        }
        bh.consume(sum)
    }
}
