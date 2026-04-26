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
 * Tests the overhead of repeated closure and method invocation within
 * tight loops. Focuses on loop-specific patterns: closure-in-loop vs
 * method-in-loop, nested iteration, and minimal vs complex loop bodies.
 *
 * Collection operation benchmarks (each/collect/findAll/inject on lists)
 * are in {@link ClosureBench}.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class LoopsBench {
    static final int LOOP_COUNT = 1_000_000

    /**
     * Loop with [1].each and toString() — exercises closure dispatch
     * and virtual method call on each iteration.
     */
    @Benchmark
    void originalEachToString(Blackhole bh) {
        for (int i = 0; i < LOOP_COUNT; i++) {
            [1].each { bh.consume(it.toString()) }
        }
    }

    /**
     * Minimal each loop — isolates closure dispatch overhead from toString() cost.
     */
    @Benchmark
    void eachIdentity(Blackhole bh) {
        for (int i = 0; i < LOOP_COUNT; i++) {
            [1].each { bh.consume(it) }
        }
    }

    /**
     * Reused closure invoked in a loop via .call() — tests call site caching
     * when the same closure is called repeatedly (no new closure allocation per iteration).
     */
    @Benchmark
    void reusedClosureInLoop(Blackhole bh) {
        Closure<?> c = { it.toString() }
        for (int i = 0; i < LOOP_COUNT; i++) {
            bh.consume(c.call(1))
        }
    }

    /**
     * Direct method call in a loop — baseline comparison against closure dispatch.
     * Shows the overhead of closure invocation vs plain method invocation.
     */
    @Benchmark
    void methodCallInLoop(Blackhole bh) {
        for (int i = 0; i < LOOP_COUNT; i++) {
            bh.consume(doSomething(1))
        }
    }

    static String doSomething(Object o) {
        o.toString()
    }

    /**
     * Nested loops with closures — tests call site behavior when multiple
     * closure call sites are active across nested iteration scopes.
     */
    @Benchmark
    void nestedLoopsWithClosure(Blackhole bh) {
        int count = (int) Math.sqrt(LOOP_COUNT)
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                [i, j].each { bh.consume(it.toString()) }
            }
        }
    }
}
