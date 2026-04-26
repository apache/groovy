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
 * Tests closure performance including creation, reuse, multi-parameter
 * invocation, variable capture, delegation, nesting, method references,
 * currying, composition, spread operator, trampoline recursion, and
 * collection operations (each/collect/findAll/inject).
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class ClosureBench {
    static final int ITERATIONS = 1_000_000

    String instanceProperty = "instance"

    /**
     * Benchmark: Simple closure creation and invocation
     */
    @Benchmark
    void simpleClosureCreation(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            Closure c = { it * 2 }
            bh.consume(c(i))
        }
    }

    /**
     * Benchmark: Reuse same closure (no creation overhead)
     */
    @Benchmark
    void closureReuse(Blackhole bh) {
        Closure c = { it * 2 }
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += c(i)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Closure with multiple parameters
     */
    @Benchmark
    void closureMultiParams(Blackhole bh) {
        Closure c = { a, b, x -> a + b + x }
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += c(i, i + 1, i + 2)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Closure accessing local variable (captured variable)
     */
    @Benchmark
    void closureWithCapture(Blackhole bh) {
        int captured = 100
        Closure c = { it + captured }
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += c(i)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Closure modifying captured variable
     */
    @Benchmark
    void closureModifyCapture(Blackhole bh) {
        int counter = 0
        Closure c = { counter++ }
        for (int i = 0; i < ITERATIONS; i++) {
            c()
        }
        bh.consume(counter)
    }

    /**
     * Benchmark: Closure with owner delegation
     */
    @Benchmark
    void closureDelegation(Blackhole bh) {
        Closure c = { instanceProperty.length() }
        c.delegate = this
        c.resolveStrategy = Closure.DELEGATE_FIRST

        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += c()
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Nested closures
     */
    @Benchmark
    void nestedClosures(Blackhole bh) {
        Closure outer = { x ->
            Closure inner = { y -> x + y }
            inner(x)
        }

        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += outer(i)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Method reference as closure
     */
    @Benchmark
    void methodReference(Blackhole bh) {
        List<Integer> list = [1, 2, 3, 4, 5]
        Closure sizeRef = list.&size

        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += sizeRef()
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Curried closure
     */
    @Benchmark
    void curriedClosure(Blackhole bh) {
        Closure add = { a, b -> a + b }
        Closure addFive = add.curry(5)

        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += addFive(i)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Right curried closure
     */
    @Benchmark
    void rightCurriedClosure(Blackhole bh) {
        Closure subtract = { a, b -> a - b }
        Closure subtractFive = subtract.rcurry(5)

        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += subtractFive(i)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Closure composition (rightShift >>)
     */
    @Benchmark
    void closureComposition(Blackhole bh) {
        Closure double_ = { it * 2 }
        Closure addOne = { it + 1 }
        Closure composed = double_ >> addOne

        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += composed(i)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Closure as method parameter
     */
    @Benchmark
    void closureAsParameter(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += applyOperation(i) { it * 2 }
        }
        bh.consume(sum)
    }

    static int applyOperation(int value, Closure<Integer> operation) {
        operation(value)
    }

    /**
     * Benchmark: Closure with spread operator
     */
    @Benchmark
    void closureSpread(Blackhole bh) {
        Closure sum3 = { a, b, c -> a + b + c }
        List<Integer> args = [1, 2, 3]

        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += sum3(*args)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Closure call vs doCall
     */
    @Benchmark
    void closureCallMethod(Blackhole bh) {
        Closure c = { it * 2 }
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += c.call(i)
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: Closure with trampoline (for recursion)
     */
    @Benchmark
    void closureTrampoline(Blackhole bh) {
        Closure factorial
        factorial = { n, acc = 1G ->
            n <= 1 ? acc : factorial.trampoline(n - 1, n * acc)
        }.trampoline()

        // Smaller iteration count due to computation cost
        for (int i = 0; i < ITERATIONS / 100; i++) {
            bh.consume(factorial(20))
        }
    }

    /**
     * Benchmark: each with closure (common pattern)
     */
    @Benchmark
    void eachWithClosure(Blackhole bh) {
        List<Integer> list = (1..10).toList()
        int sum = 0
        for (int i = 0; i < ITERATIONS / 10; i++) {
            list.each { sum += it }
        }
        bh.consume(sum)
    }

    /**
     * Benchmark: collect with closure
     */
    @Benchmark
    void collectWithClosure(Blackhole bh) {
        List<Integer> list = (1..10).toList()
        for (int i = 0; i < ITERATIONS / 10; i++) {
            bh.consume(list.collect { it * 2 })
        }
    }

    /**
     * Benchmark: findAll with closure
     */
    @Benchmark
    void findAllWithClosure(Blackhole bh) {
        List<Integer> list = (1..10).toList()
        for (int i = 0; i < ITERATIONS / 10; i++) {
            bh.consume(list.findAll { it > 5 })
        }
    }

    /**
     * Benchmark: inject/reduce with closure
     */
    @Benchmark
    void injectWithClosure(Blackhole bh) {
        List<Integer> list = (1..10).toList()
        int sum = 0
        for (int i = 0; i < ITERATIONS / 10; i++) {
            sum += list.inject(0) { acc, val -> acc + val }
        }
        bh.consume(sum)
    }

}
