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

import groovy.lang.GroovySystem

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * Tests the performance of Groovy's dynamic method dispatch mechanisms:
 * {@code methodMissing}, {@code propertyMissing}, {@code invokeMethod},
 * and {@link GroovyInterceptable}. These are the building blocks of
 * Grails' convention-based programming model.
 *
 * Grails uses these patterns extensively:
 * <ul>
 *   <li>{@code methodMissing} - dynamic finders (findByName, findAllByAge)</li>
 *   <li>{@code propertyMissing} - dynamic property injection (params, session)</li>
 *   <li>{@code invokeMethod} - method interception for transactions, security</li>
 *   <li>ExpandoMetaClass runtime injection - adding methods at framework startup</li>
 * </ul>
 *
 * These patterns interact with the invokedynamic call site cache differently
 * than normal method calls: methodMissing/propertyMissing cause cache misses
 * on every distinct method name, while invokeMethod intercepts all calls
 * regardless of caching.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class DynamicDispatchBench {
    static final int ITERATIONS = 100_000

    // Class with methodMissing - like Grails domain class dynamic finders
    static class DynamicFinder {
        Map storage = [:]

        def methodMissing(String name, args) {
            if (name.startsWith('findBy')) {
                String field = name.substring(6).toLowerCase()
                return storage[field]
            }
            if (name.startsWith('saveTo')) {
                String field = name.substring(6).toLowerCase()
                storage[field] = args[0]
                return args[0]
            }
            throw new MissingMethodException(name, DynamicFinder, args)
        }
    }

    // Class with propertyMissing - like Grails controller params/session
    static class DynamicProperties {
        Map attributes = [name: "test", age: 25, active: true, role: "admin"]

        def propertyMissing(String name) {
            attributes[name]
        }

        def propertyMissing(String name, value) {
            attributes[name] = value
        }
    }

    // Class with invokeMethod override - like Grails interceptors
    static class MethodInterceptor implements GroovyInterceptable {
        int callCount = 0
        int realValue = 42

        def invokeMethod(String name, args) {
            callCount++
            def metaMethod = metaClass.getMetaMethod(name, args)
            if (metaMethod) {
                return metaMethod.invoke(this, args)
            }
            return null
        }

        int compute() { realValue * 2 }
        String describe() { "value=$realValue" }
    }

    // Plain class for baseline comparison
    static class PlainService {
        int value = 42
        int compute() { value * 2 }
    }

    DynamicFinder finder
    DynamicProperties props
    MethodInterceptor interceptor
    PlainService plain

    @Setup(Level.Iteration)
    void setup() {
        GroovySystem.metaClassRegistry.removeMetaClass(DynamicFinder)
        GroovySystem.metaClassRegistry.removeMetaClass(DynamicProperties)
        GroovySystem.metaClassRegistry.removeMetaClass(MethodInterceptor)
        GroovySystem.metaClassRegistry.removeMetaClass(PlainService)
        // Inject expando method once per iteration for injected-method benchmarks
        PlainService.metaClass.injectedMethod = { -> delegate.value * 3 }
        finder = new DynamicFinder()
        finder.storage = [name: "Alice", age: 30, city: "Springfield"]
        props = new DynamicProperties()
        interceptor = new MethodInterceptor()
        plain = new PlainService()
    }

    // ===== BASELINE =====

    /**
     * Baseline: normal method calls on a plain class.
     * Control for all dynamic dispatch variants.
     */
    @Benchmark
    void baselinePlainMethodCalls(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += plain.compute()
        }
        bh.consume(sum)
    }

    // ===== methodMissing =====

    /**
     * Single dynamic finder name called repeatedly.
     * The call site sees the same method name every time, but
     * methodMissing must still handle it since there is no real
     * method to cache.
     */
    @Benchmark
    void methodMissingSingleName(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(finder.findByName())
        }
    }

    /**
     * Rotating dynamic finder names - exercises call site cache with
     * multiple missing method names at the same call site.
     * Simulates Grails code calling different dynamic finders in
     * sequence: findByName, findByAge, findByCity.
     */
    @Benchmark
    void methodMissingRotatingNames(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            switch (i % 3) {
                case 0: bh.consume(finder.findByName()); break
                case 1: bh.consume(finder.findByAge()); break
                case 2: bh.consume(finder.findByCity()); break
            }
        }
    }

    /**
     * methodMissing for write operations - save pattern.
     */
    @Benchmark
    void methodMissingSavePattern(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(finder.saveToName("name_$i"))
        }
    }

    /**
     * Mix of methodMissing and real method calls.
     * Simulates Grails service code that mixes dynamic finders
     * with normal method calls on the same object.
     */
    @Benchmark
    void methodMissingMixedWithReal(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            if (i % 2 == 0) {
                bh.consume(finder.findByName())
            } else {
                bh.consume(finder.storage.size())
            }
        }
    }

    // ===== propertyMissing =====

    /**
     * Single dynamic property access repeated.
     */
    @Benchmark
    void propertyMissingSingleName(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(props.name)
        }
    }

    /**
     * Rotating dynamic property names - multiple property accesses
     * at the same call site location.
     */
    @Benchmark
    void propertyMissingRotatingNames(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            switch (i % 4) {
                case 0: bh.consume(props.name); break
                case 1: bh.consume(props.age); break
                case 2: bh.consume(props.active); break
                case 3: bh.consume(props.role); break
            }
        }
    }

    /**
     * Dynamic property read/write cycle.
     */
    @Benchmark
    void propertyMissingReadWrite(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            props.name = "user_$i"
            bh.consume(props.name)
        }
    }

    // ===== invokeMethod (GroovyInterceptable) =====

    /**
     * Method calls through invokeMethod interception.
     * Every call, even to existing methods, goes through invokeMethod.
     * This is the pattern used by Grails for transactional services
     * and security interceptors.
     */
    @Benchmark
    void invokeMethodInterception(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += interceptor.compute()
        }
        bh.consume(sum)
    }

    /**
     * Alternating method calls through invokeMethod.
     * Different method names at the same interception point.
     */
    @Benchmark
    void invokeMethodAlternating(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            if (i % 2 == 0) {
                bh.consume(interceptor.compute())
            } else {
                bh.consume(interceptor.describe())
            }
        }
    }

    // ===== EXPANDO METACLASS RUNTIME INJECTION =====

    /**
     * Calling a method that was injected at runtime via ExpandoMetaClass.
     * Grails injects many methods at startup (save, delete, validate,
     * dynamic finders) that are then called frequently during request
     * processing.
     */
    @Benchmark
    void expandoInjectedMethodCall(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += plain.injectedMethod()
        }
        bh.consume(sum)
    }

    /**
     * Mix of real and expando-injected method calls.
     * This is the typical Grails runtime pattern: domain classes have
     * both compiled methods and dynamically injected GORM methods.
     */
    @Benchmark
    void mixedRealAndInjectedCalls(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            if (i % 2 == 0) {
                sum += plain.compute()        // real method
            } else {
                sum += plain.injectedMethod()  // injected method
            }
        }
        bh.consume(sum)
    }

    // ===== DYNAMIC DISPATCH ON def-TYPED REFERENCES =====

    /**
     * Method calls on {@code def}-typed variable - the compiler
     * cannot statically resolve the method, forcing full dynamic
     * dispatch through invokedynamic on every call.
     */
    @Benchmark
    void defTypedDispatch(Blackhole bh) {
        def service = plain
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += service.compute()
        }
        bh.consume(sum)
    }

    /**
     * Polymorphic dispatch through {@code def}-typed variable.
     * Different receiver types flow through the same call site,
     * testing the LRU cache effectiveness.
     */
    @Benchmark
    void defTypedPolymorphicDispatch(Blackhole bh) {
        Object[] services = [plain, interceptor]
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += services[i % 2].compute()
        }
        bh.consume(sum)
    }
}
