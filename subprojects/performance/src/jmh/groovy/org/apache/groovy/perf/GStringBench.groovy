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
 * Tests the performance of GString creation, interpolation, and
 * calling methods on GString results — including simple and multi-value
 * interpolation, comparison against plain String concatenation, use as
 * Map keys, and repeated toString() evaluation.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class GStringBench {
    static final int ITERATIONS = 1_000_000

    /**
     * Simple GString with one interpolated value and a method call on the result.
     */
    @Benchmark
    void simpleInterpolation(Blackhole bh) {
        String base = "Hello"
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += "${base}${i}".length()
        }
        bh.consume(sum)
    }

    /**
     * Multi-value GString with method call — tests cost of multiple
     * interpolation expressions and a follow-on method dispatch.
     */
    @Benchmark
    void multiValueInterpolation(Blackhole bh) {
        String a = "A"
        String b = "B"
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume("${a}-${i}-${b}".toUpperCase())
        }
    }

    /**
     * GString compared to plain String concatenation — baseline to
     * isolate the GString-specific overhead.
     */
    @Benchmark
    void stringConcatBaseline(Blackhole bh) {
        String base = "Hello"
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += (base + i).length()
        }
        bh.consume(sum)
    }

    /**
     * GString used as a Map key — triggers toString() and hashCode(),
     * testing lazy evaluation and method dispatch on the resulting String.
     */
    @Benchmark
    void gstringAsMapKey(Blackhole bh) {
        Map<String, Integer> map = [:]
        String prefix = "key"
        for (int i = 0; i < ITERATIONS; i++) {
            map["${prefix}${i % 100}"] = i
        }
        bh.consume(map)
    }

    /**
     * Repeated toString() on the same GString — tests whether the
     * GString caches its string representation.
     */
    @Benchmark
    void repeatedToString(Blackhole bh) {
        String name = "World"
        GString gs = "Hello ${name}!"
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += gs.toString().length()
        }
        bh.consume(sum)
    }
}
