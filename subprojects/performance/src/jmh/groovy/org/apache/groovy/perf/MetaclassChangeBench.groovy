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

import groovy.lang.ExpandoMetaClass
import groovy.lang.GroovySystem

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * Tests the performance impact of metaclass modifications on invokedynamic
 * call sites. These benchmarks exercise the key pain point identified in
 * GROOVY-10307: when any metaclass changes, the global SwitchPoint is
 * invalidated, causing ALL call sites across the application to fall back
 * and re-link their method handles.
 *
 * In Grails applications, metaclass modifications happen frequently during
 * framework startup (loading controllers, services, domain classes) and
 * during request processing (dynamic finders, runtime mixins). This causes
 * severe performance degradation under invokedynamic because every metaclass
 * change triggers a global invalidation cascade.
 *
 * Compare baseline benchmarks (no metaclass changes) against the metaclass
 * modification variants to measure the invalidation overhead.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class MetaclassChangeBench {
    static final int ITERATIONS = 100_000

    // Helper classes for benchmarks - each represents a different
    // component that might have its metaclass modified
    static class ServiceA {
        int value = 42
        int compute() { value * 2 }
    }

    static class ServiceB {
        String name = "test"
        int nameLength() { name.length() }
    }

    static class ServiceC {
        List items = [1, 2, 3]
        int itemCount() { items.size() }
    }

    ServiceA serviceA
    ServiceB serviceB
    ServiceC serviceC

    @Setup(Level.Iteration)
    void setup() {
        // Reset metaclasses to avoid accumulation across iterations
        GroovySystem.metaClassRegistry.removeMetaClass(ServiceA)
        GroovySystem.metaClassRegistry.removeMetaClass(ServiceB)
        GroovySystem.metaClassRegistry.removeMetaClass(ServiceC)
        serviceA = new ServiceA()
        serviceB = new ServiceB()
        serviceC = new ServiceC()
    }

    // ===== BASELINE (no metaclass changes) =====

    /**
     * Baseline: method calls with no metaclass changes.
     * Establishes the cost of normal invokedynamic dispatch when
     * call sites remain stable. Compare against metaclass-modifying
     * benchmarks to measure invalidation overhead.
     */
    @Benchmark
    void baselineNoMetaclassChanges(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += serviceA.compute()
        }
        bh.consume(sum)
    }

    /**
     * Baseline: multi-class method calls with no metaclass changes.
     * Control for {@link #multiClassMetaclassChurn}.
     */
    @Benchmark
    void baselineMultiClassNoChanges(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += serviceA.compute()
            sum += serviceB.nameLength()
            sum += serviceC.itemCount()
        }
        bh.consume(sum)
    }

    // ===== EXPANDO METACLASS MODIFICATIONS =====

    /**
     * ExpandoMetaClass method addition interleaved with method calls.
     * Every 1000 calls, a method is added to the metaclass, triggering
     * SwitchPoint invalidation.
     *
     * This simulates the Grails startup pattern where metaclasses are
     * modified as controllers, services, and domain classes are loaded
     * while other call sites are already active.
     */
    @Benchmark
    void expandoMethodAddition(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += serviceA.compute()
            if (i % 1000 == 0) {
                // Add method via ExpandoMetaClass - triggers invalidateSwitchPoints()
                // Reuse a small set of names to avoid unbounded metaclass growth
                ServiceA.metaClass."dynamic${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /**
     * Frequent metaclass changes - every 100 calls instead of 1000.
     * Simulates frameworks that modify metaclasses more aggressively
     * during request processing.
     */
    @Benchmark
    void frequentExpandoChanges(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += serviceA.compute()
            if (i % 100 == 0) {
                ServiceA.metaClass."frequent${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    // ===== METACLASS REPLACEMENT =====

    /**
     * Repeated metaclass replacement - the most extreme invalidation
     * pattern. Replacing the entire metaclass triggers a full
     * invalidation cycle each time.
     */
    @Benchmark
    void metaclassReplacement(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += serviceA.compute()
            if (i % 1000 == 0) {
                def mc = new ExpandoMetaClass(ServiceA, false, true)
                mc.initialize()
                ServiceA.metaClass = mc
            }
        }
        bh.consume(sum)
    }

    // ===== MULTI-CLASS INVALIDATION CASCADE =====

    /**
     * Multi-class metaclass modification - simulates Grails loading
     * multiple components, each triggering metaclass changes that
     * invalidate call sites for ALL classes, not just the modified one.
     *
     * This is the core Grails pain point: changing ServiceA's metaclass
     * invalidates call sites for ServiceB and ServiceC too.
     */
    @Benchmark
    void multiClassMetaclassChurn(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += serviceA.compute()
            sum += serviceB.nameLength()
            sum += serviceC.itemCount()

            if (i % 1000 == 0) {
                // Rotate metaclass changes across different classes
                switch (i % 3000) {
                    case 0:
                        ServiceA.metaClass."dynamic${i % 3}" = { -> i }
                        break
                    case 1000:
                        ServiceB.metaClass."dynamic${i % 3}" = { -> i }
                        break
                    case 2000:
                        ServiceC.metaClass."dynamic${i % 3}" = { -> i }
                        break
                }
            }
        }
        bh.consume(sum)
    }

    // ===== BURST THEN STEADY STATE =====

    /**
     * Burst metaclass changes followed by steady-state calls.
     * Simulates Grails application startup (many metaclass changes
     * during bootstrap) followed by request handling (stable dispatch).
     * Measures how quickly call sites recover after invalidation stops.
     */
    @Benchmark
    void burstThenSteadyState(Blackhole bh) {
        // Phase 1: Burst of metaclass changes (startup/bootstrap)
        for (int i = 0; i < 50; i++) {
            ServiceA.metaClass."startup${i % 10}" = { -> i }
        }

        // Phase 2: Steady-state method calls (request handling)
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += serviceA.compute()
        }
        bh.consume(sum)
    }

    // ===== PROPERTY ACCESS UNDER METACLASS CHURN =====

    /**
     * Property access interleaved with metaclass changes.
     * Property get/set dispatches through invokedynamic and is also
     * invalidated by SwitchPoint changes. Grails uses extensive
     * property access for domain class fields, controller parameters,
     * and service injection.
     */
    @Benchmark
    void propertyAccessDuringMetaclassChurn(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            serviceA.value = i
            sum += serviceA.value
            if (i % 1000 == 0) {
                ServiceA.metaClass."prop${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /**
     * Baseline: property access with no metaclass changes.
     * Control for {@link #propertyAccessDuringMetaclassChurn}.
     */
    @Benchmark
    void baselinePropertyAccessNoChanges(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            serviceA.value = i
            sum += serviceA.value
        }
        bh.consume(sum)
    }

    // ===== CLOSURE DISPATCH UNDER METACLASS CHURN =====

    /**
     * Closure dispatch during metaclass changes.
     * Closure call sites are also invalidated by SwitchPoint changes.
     * Grails uses closures extensively in GORM criteria queries,
     * controller actions, and configuration DSLs.
     */
    @Benchmark
    void closureDispatchDuringMetaclassChurn(Blackhole bh) {
        Closure compute = { int x -> x * 2 }
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += compute(i)
            if (i % 1000 == 0) {
                ServiceA.metaClass."cl${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /**
     * Baseline: closure dispatch with no metaclass changes.
     * Control for {@link #closureDispatchDuringMetaclassChurn}.
     */
    @Benchmark
    void baselineClosureDispatchNoChanges(Blackhole bh) {
        Closure compute = { int x -> x * 2 }
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += compute(i)
        }
        bh.consume(sum)
    }
}
