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
package org.apache.groovy.perf.grails

import groovy.lang.GroovySystem

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * SwitchPoint invalidation overhead for Grails-like metaclass change patterns.
 *
 * @see <a href="https://issues.apache.org/jira/browse/GROOVY-10307">GROOVY-10307</a>
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class CallSiteInvalidationBench {
    /** Number of iterations per benchmark. */
    static final int ITERATIONS = 100_000

    /**
     * Target class for hot method call sites.
     */
    static class HotTarget {
        /** Test value for computations. */
        int value = 42
        /** Computes double of the value. */
        int compute() { value * 2 }
        /** Returns a string description of the value. */
        String describe() { "v=$value" }
    }

    /**
     * Second target class for multi-type call sites.
     */
    static class HotTargetB {
        /** Count value for testing. */
        int count = 10
        /** Returns the count. */
        int getCount() { count }
    }

    /**
     * Third target class for multi-type call sites.
     */
    static class HotTargetC {
        /** List of items for testing. */
        List items = [1, 2, 3]
        /** Returns the number of items. */
        int itemCount() { items.size() }
    }

    /**
     * Cold type for cross-type metaclass invalidation.
     */
    static class ColdType {
        /** Label for cold type. */
        String label = "cold"
    }

    /** Instance of HotTarget for benchmarking. */
    HotTarget hotTarget
    /** Instance of HotTargetB for benchmarking. */
    HotTargetB hotTargetB
    /** Instance of HotTargetC for benchmarking. */
    HotTargetC hotTargetC
    /** Sample list for size() benchmarks. */
    List<Integer> sampleList

    /** Sets up fresh instances before each iteration. */
    @Setup(Level.Iteration)
    void setup() {
        GroovySystem.metaClassRegistry.removeMetaClass(HotTarget)
        GroovySystem.metaClassRegistry.removeMetaClass(HotTargetB)
        GroovySystem.metaClassRegistry.removeMetaClass(HotTargetC)
        GroovySystem.metaClassRegistry.removeMetaClass(ColdType)
        hotTarget = new HotTarget()
        hotTargetB = new HotTargetB()
        hotTargetC = new HotTargetC()
        sampleList = [1, 2, 3, 4, 5]
    }

    /** Single method call in tight loop, no invalidation. */
    @Benchmark
    void baselineHotLoop(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
        }
        bh.consume(sum)
    }

    /** list.size() in tight loop, no invalidation. */
    @Benchmark
    void baselineListSize(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += sampleList.size()
        }
        bh.consume(sum)
    }

    /** Cross-type invalidation every 1000 calls. */
    @Benchmark
    void crossTypeInvalidationEvery1000(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
            if (i % 1000 == 0) {
                ColdType.metaClass."dynamic${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /** Cross-type invalidation every 100 calls. */
    @Benchmark
    void crossTypeInvalidationEvery100(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
            if (i % 100 == 0) {
                ColdType.metaClass."dynamic${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /** Cross-type invalidation every 10000 calls. */
    @Benchmark
    void crossTypeInvalidationEvery10000(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
            if (i % 10000 == 0) {
                ColdType.metaClass."dynamic${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /** list.size() with cross-type invalidation every 1000 calls. */
    @Benchmark
    void listSizeWithCrossTypeInvalidation(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += sampleList.size()
            if (i % 1000 == 0) {
                ColdType.metaClass."dynamic${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /** Same-type invalidation every 1000 calls. */
    @Benchmark
    void sameTypeInvalidationEvery1000(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
            if (i % 1000 == 0) {
                HotTarget.metaClass."dynamic${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /** Five method calls across three types, no invalidation. */
    @Benchmark
    void baselineMultipleCallSites(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
            sum += hotTarget.describe().length()
            sum += hotTargetB.getCount()
            sum += hotTargetC.itemCount()
            sum += sampleList.size()
        }
        bh.consume(sum)
    }

    /** Five call sites with cross-type invalidation every 1000 calls. */
    @Benchmark
    void multipleCallSitesWithInvalidation(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
            sum += hotTarget.describe().length()
            sum += hotTargetB.getCount()
            sum += hotTargetC.itemCount()
            sum += sampleList.size()
            if (i % 1000 == 0) {
                ColdType.metaClass."dynamic${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /** 100 metaclass changes then steady-state method calls. */
    @Benchmark
    void burstThenSteadyState(Blackhole bh) {
        // Phase 1: Burst of metaclass changes (framework startup)
        for (int i = 0; i < 100; i++) {
            ColdType.metaClass."startup${i % 20}" = { -> i }
        }

        // Phase 2: Steady-state method calls (request handling)
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
            sum += hotTargetB.getCount()
            sum += sampleList.size()
        }
        bh.consume(sum)
    }

    /** Steady-state method calls with no preceding burst. */
    @Benchmark
    void baselineSteadyStateNoBurst(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hotTarget.compute()
            sum += hotTargetB.getCount()
            sum += sampleList.size()
        }
        bh.consume(sum)
    }
}
