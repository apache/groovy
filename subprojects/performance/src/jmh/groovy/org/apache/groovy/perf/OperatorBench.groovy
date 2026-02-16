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
 * Tests the performance of Groovy operator overloading. In Groovy every
 * operator (+, -, *, /, [], <<, ==, <=>) compiles to a method call
 * (plus, minus, multiply, div, getAt, leftShift, equals, compareTo)
 * dispatched through invokedynamic.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class OperatorBench {
    static final int ITERATIONS = 1_000_000

    /**
     * Integer addition — dispatches to Integer.plus(Integer).
     */
    @Benchmark
    void integerPlus(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum = sum + i
        }
        bh.consume(sum)
    }

    /**
     * Integer multiplication — dispatches to Integer.multiply(Integer).
     * Uses modulo to keep operands small and avoid overflow to zero.
     */
    @Benchmark
    void integerMultiply(Blackhole bh) {
        int product = 1
        for (int i = 1; i < ITERATIONS; i++) {
            product = (i % 100) * (i % 50)
        }
        bh.consume(product)
    }

    /**
     * BigDecimal arithmetic — common in financial/Grails apps,
     * all operations go through operator method dispatch.
     */
    @Benchmark
    void bigDecimalArithmetic(Blackhole bh) {
        BigDecimal sum = 0.0
        for (int i = 0; i < ITERATIONS; i++) {
            sum = sum + 1.5
        }
        bh.consume(sum)
    }

    /**
     * String multiply (repeat) — "abc" * 3 dispatches to String.multiply(Integer).
     */
    @Benchmark
    void stringMultiply(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += ("x" * 5).length()
        }
        bh.consume(sum)
    }

    /**
     * List subscript read — list[i] dispatches to List.getAt(int).
     */
    @Benchmark
    void listGetAt(Blackhole bh) {
        List<Integer> list = (0..99).toList()
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += list[i % 100]
        }
        bh.consume(sum)
    }

    /**
     * List subscript write — list[i] = val dispatches to List.putAt(int, Object).
     */
    @Benchmark
    void listPutAt(Blackhole bh) {
        List<Integer> list = (0..99).toList()
        for (int i = 0; i < ITERATIONS; i++) {
            list[i % 100] = i
        }
        bh.consume(list)
    }

    /**
     * Map subscript read/write — map[key] dispatches to getAt/putAt.
     */
    @Benchmark
    void mapGetAtPutAt(Blackhole bh) {
        Map<String, Integer> map = [a: 1, b: 2, c: 3, d: 4, e: 5]
        String[] keys = ['a', 'b', 'c', 'd', 'e']
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            map[keys[i % 5]] = i
            sum += map[keys[i % 5]]
        }
        bh.consume(sum)
    }

    /**
     * Left shift operator — list << item dispatches to List.leftShift(Object).
     */
    @Benchmark
    void listLeftShift(Blackhole bh) {
        List<Integer> list = []
        for (int i = 0; i < ITERATIONS; i++) {
            if (i % 1000 == 0) list = []
            list << i
        }
        bh.consume(list)
    }

    /**
     * Equals operator — == dispatches to Object.equals(Object) in Groovy
     * (not reference equality like Java).
     */
    @Benchmark
    void equalsOperator(Blackhole bh) {
        String a = "hello"
        String b = "hello"
        int count = 0
        for (int i = 0; i < ITERATIONS; i++) {
            if (a == b) count++
        }
        bh.consume(count)
    }

    /**
     * Spaceship operator — <=> dispatches to Comparable.compareTo().
     */
    @Benchmark
    void spaceshipOperator(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += (i <=> (i + 1))
        }
        bh.consume(sum)
    }

    /**
     * Comparison operators — <, >, <=, >= dispatch through compareTo().
     */
    @Benchmark
    void comparisonOperators(Blackhole bh) {
        int count = 0
        for (int i = 0; i < ITERATIONS; i++) {
            if (i > 0 && i < ITERATIONS && i >= 0 && i <= ITERATIONS) count++
        }
        bh.consume(count)
    }

    /**
     * Unary minus — dispatches to Number.unaryMinus().
     */
    @Benchmark
    void unaryMinus(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += (-i)
        }
        bh.consume(sum)
    }

    /**
     * In operator — (item in collection) dispatches to Collection.isCase(Object).
     */
    @Benchmark
    void inOperator(Blackhole bh) {
        List<Integer> list = (0..99).toList()
        int count = 0
        for (int i = 0; i < ITERATIONS; i++) {
            if ((i % 100) in list) count++
        }
        bh.consume(count)
    }
}
