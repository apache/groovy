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
 * Tests the overhead of dynamic method invocation and dispatch in Groovy.
 * Covers instance and static method calls, parameter passing, overloaded
 * method resolution, monomorphic vs polymorphic call sites, interface
 * dispatch, and dynamically-typed dispatch.
 *
 * Property access is in {@link PropertyAccessBench}.
 * GString operations are in {@link GStringBench}.
 * Method references as closures are in {@link ClosureBench}.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class MethodInvocationBench {
    static final int ITERATIONS = 1_000_000

    // Simple fields for method tests
    int instanceField = 42
    static int staticField = 100

    // Simple instance method
    int simpleMethod() {
        instanceField
    }

    // Method with parameters
    int methodWithParams(int a, int b) {
        a + b
    }

    // Method with object parameter
    String methodWithObject(Object obj) {
        obj.toString()
    }

    // Overloaded methods to test dispatch
    String overloaded(String s) { "String: $s" }
    String overloaded(Integer i) { "Integer: $i" }
    String overloaded(Object o) { "Object: $o" }

    // Static methods
    static int staticMethod() {
        staticField
    }

    static int staticMethodWithParams(int a, int b) {
        a + b
    }

    /**
     * Benchmark: Simple instance method calls
     */
    @Benchmark
    void benchmarkSimpleMethodCalls(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += simpleMethod()
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Method calls with parameters
     */
    @Benchmark
    void benchmarkMethodWithParams(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += methodWithParams(i, 1)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Method calls with object parameter
     */
    @Benchmark
    void benchmarkMethodWithObject(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(methodWithObject(i))
        }
    }

    /**
     * Benchmark: Static method calls
     */
    @Benchmark
    void benchmarkStaticMethodCalls(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += staticMethod()
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Static method calls with parameters
     */
    @Benchmark
    void benchmarkStaticMethodWithParams(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += staticMethodWithParams(i, 1)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Monomorphic call site (same type every time)
     * This should be fast with proper indy optimization
     */
    @Benchmark
    void benchmarkMonomorphicCallSite(Blackhole bh) {
        String s = "test"
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(overloaded(s))
        }
    }

    /**
     * Benchmark: Polymorphic call site (different types)
     * This tests the call site cache effectiveness
     */
    @Benchmark
    void benchmarkPolymorphicCallSite(Blackhole bh) {
        Object[] args = ["string", 42, new Object(), "another", 100, [1, 2, 3]]

        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(overloaded(args[i % args.length]))
        }
    }

    /**
     * Benchmark: Method calls through interface
     */
    @Benchmark
    void benchmarkInterfaceMethodCalls(Blackhole bh) {
        List<Integer> list = [1, 2, 3, 4, 5]
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += list.size()
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Method calls on dynamically typed variable
     */
    @Benchmark
    void benchmarkDynamicTypedCalls(Blackhole bh) {
        def instance = this
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += instance.simpleMethod()
        }
        bh.consume(sum)
    }

}
