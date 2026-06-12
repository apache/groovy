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

import groovy.lang.ExpandoMetaClass
import groovy.lang.GroovySystem

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * Per-instance metaclass variation overhead (GORM domain class enhancement pattern).
 *
 * @see <a href="https://issues.apache.org/jira/browse/GROOVY-10307">GROOVY-10307</a>
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class MetaclassVariationBench {
    /** Number of iterations per benchmark. */
    static final int ITERATIONS = 100_000
    /** Number of domain instances per test. */
    static final int INSTANCE_COUNT = 20

    /**
     * Simulates a GORM domain class.
     */
    static class DomainEntity {
        /** Entity ID. */
        Long id
        /** Entity name. */
        String name
        /** Email address. */
        String email
        /** Active status. */
        boolean active = true
        /** Version for optimistic locking. */
        int version = 0

        /** Returns full name or 'Unknown'. */
        String getFullName() { name ?: 'Unknown' }
        /** Returns active status. */
        boolean isActive() { active }
        /** Returns version number. */
        int getVersion() { version }

        /** Saves the entity. */
        DomainEntity save() {
            version++
            if (id == null) id = System.nanoTime()
            this
        }

        /** Converts to map representation. */
        Map toMap() {
            [id: id, name: name, email: email, active: active, version: version]
        }
    }

    /**
     * Second domain class type for multi-type testing.
     */
    static class DomainTypeB {
        /** Label field. */
        String label = "dept"
        /** Count field. */
        int count = 5
        /** Returns the count. */
        int getCount() { count }
    }

    /**
     * Third domain class type for multi-type testing.
     */
    static class DomainTypeC {
        /** Status field. */
        String status = "ACTIVE"
        /** Budget field. */
        BigDecimal budget = 100000.0
        /** Returns the status. */
        String getStatus() { status }
    }

    /**
     * Fourth domain class type for multi-type testing.
     */
    static class DomainTypeD {
        /** Priority level. */
        int priority = 5
        /** Assignee name. */
        String assignee = "unassigned"
        /** Returns the priority. */
        int getPriority() { priority }
    }

    /**
     * Unrelated type for cross-type invalidation.
     */
    static class ServiceType {
        /** Configuration value. */
        String config = "default"
    }

    /** Instances sharing default class metaclass. */
    List<DomainEntity> sharedMetaclassInstances
    /** Instances with per-instance ExpandoMetaClass. */
    List<DomainEntity> perInstanceMetaclassInstances
    /** DomainTypeB instance for benchmarking. */
    DomainTypeB typeB
    /** DomainTypeC instance for benchmarking. */
    DomainTypeC typeC
    /** DomainTypeD instance for benchmarking. */
    DomainTypeD typeD

    /** Sets up domain instances with different metaclass configurations. */
    @Setup(Level.Iteration)
    void setup() {
        GroovySystem.metaClassRegistry.removeMetaClass(DomainEntity)
        GroovySystem.metaClassRegistry.removeMetaClass(DomainTypeB)
        GroovySystem.metaClassRegistry.removeMetaClass(DomainTypeC)
        GroovySystem.metaClassRegistry.removeMetaClass(DomainTypeD)
        GroovySystem.metaClassRegistry.removeMetaClass(ServiceType)

        // Shared default class metaclass
        sharedMetaclassInstances = (1..INSTANCE_COUNT).collect { i ->
            new DomainEntity(id: i, name: "User$i", email: "user${i}@test.com")
        }

        // Per-instance ExpandoMetaClass (GORM trait pattern)
        perInstanceMetaclassInstances = (1..INSTANCE_COUNT).collect { i ->
            def entity = new DomainEntity(id: i, name: "Enhanced$i", email: "e${i}@test.com")
            def emc = new ExpandoMetaClass(DomainEntity, false, true)
            // GORM-injected methods
            emc.validate = { -> delegate.name != null && delegate.email != null }
            emc.delete = { -> delegate.id = null; delegate }
            emc.addToDependencies = { item -> delegate }
            emc.initialize()
            entity.metaClass = emc
            entity
        }

        typeB = new DomainTypeB()
        typeC = new DomainTypeC()
        typeD = new DomainTypeD()
    }

    /** Method calls on instances sharing default class metaclass. */
    @Benchmark
    void baselineSharedMetaclass(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def entity = sharedMetaclassInstances[i % INSTANCE_COUNT]
            sum += entity.getFullName().length()
            sum += entity.getVersion()
        }
        bh.consume(sum)
    }

    /** Method calls on instances each with their own ExpandoMetaClass. */
    @Benchmark
    void perInstanceMetaclass(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def entity = perInstanceMetaclassInstances[i % INSTANCE_COUNT]
            sum += entity.getFullName().length()
            sum += entity.getVersion()
        }
        bh.consume(sum)
    }

    /** Calling GORM-injected methods on per-instance EMC objects. */
    @Benchmark
    void perInstanceInjectedMethodCalls(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def entity = perInstanceMetaclassInstances[i % INSTANCE_COUNT]
            boolean valid = entity.validate()
            sum += valid ? 1 : 0
        }
        bh.consume(sum)
    }

    /** GORM startup: enhance 4 domain types then steady-state calls. */
    @Benchmark
    void multiClassStartupThenSteadyState(Blackhole bh) {
        // Phase 1: Enhance 4 domain class types
        DomainEntity.metaClass.static.findAllByName = { String n -> [] }
        DomainEntity.metaClass.static.countByActive = { boolean a -> 0 }

        DomainTypeB.metaClass.static.findAllByLabel = { String l -> [] }
        DomainTypeB.metaClass.static.countByCount = { int c -> 0 }

        DomainTypeC.metaClass.static.findAllByStatus = { String s -> [] }
        DomainTypeC.metaClass.static.findByBudgetGreaterThan = { BigDecimal b -> null }

        DomainTypeD.metaClass.static.findAllByPriority = { int p -> [] }
        DomainTypeD.metaClass.static.findByAssignee = { String a -> null }

        // Phase 2: Steady-state calls
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def entity = sharedMetaclassInstances[i % INSTANCE_COUNT]
            sum += entity.getFullName().length()
            sum += typeB.getCount()
            sum += typeC.getStatus().length()
            sum += typeD.getPriority()
        }
        bh.consume(sum)
    }

    /** Baseline: same steady-state work without preceding metaclass enhancements. */
    @Benchmark
    void baselineMultiClassNoStartup(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def entity = sharedMetaclassInstances[i % INSTANCE_COUNT]
            sum += entity.getFullName().length()
            sum += typeB.getCount()
            sum += typeC.getStatus().length()
            sum += typeD.getPriority()
        }
        bh.consume(sum)
    }

    /** Injection + invocation of dynamic finders via static metaclass (models per-request GORM cost). */
    @Benchmark
    void dynamicFinderCalls(Blackhole bh) {
        // Inject dynamic finders
        DomainEntity.metaClass.static.findByName = { String n ->
            [new DomainEntity(name: n)]
        }
        DomainEntity.metaClass.static.findAllByActive = { boolean a ->
            [new DomainEntity(active: a)]
        }

        for (int i = 0; i < ITERATIONS / 10; i++) {
            def result1 = DomainEntity.findByName("User${i % 10}")
            def result2 = DomainEntity.findAllByActive(true)
            bh.consume(result1)
            bh.consume(result2)
        }
    }

    /** Mixed compiled method calls and dynamic finder injection + invocation (models per-request GORM cost). */
    @Benchmark
    void mixedCompiledAndDynamicFinders(Blackhole bh) {
        DomainEntity.metaClass.static.findByName = { String n ->
            [new DomainEntity(name: n)]
        }

        int sum = 0
        for (int i = 0; i < ITERATIONS / 10; i++) {
            // Dynamic finder
            def found = DomainEntity.findByName("User${i % 10}")
            // Compiled methods
            def entity = sharedMetaclassInstances[i % INSTANCE_COUNT]
            sum += entity.getFullName().length()
            sum += entity.getVersion()
            bh.consume(found)
        }
        bh.consume(sum)
    }

    /** Per-instance EMC access with ongoing cross-type metaclass churn. */
    @Benchmark
    void perInstanceWithOngoingChurn(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def entity = perInstanceMetaclassInstances[i % INSTANCE_COUNT]
            sum += entity.getFullName().length()
            sum += entity.getVersion()
            if (i % 1000 == 0) {
                ServiceType.metaClass."helper${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }
}
