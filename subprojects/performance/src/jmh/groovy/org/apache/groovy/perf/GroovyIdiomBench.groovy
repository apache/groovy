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
 * Tests performance of Groovy-specific language idioms: safe navigation
 * (?.), spread-dot (*.), elvis (?:), with/tap scoping, range creation
 * and iteration, and 'as' type coercion.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class GroovyIdiomBench {
    static final int ITERATIONS = 1_000_000

    // Helper class for safe-nav / spread-dot / with tests
    static class Person {
        String name
        Address address
    }

    static class Address {
        String city
        String zip
    }

    // Pre-built test data
    Person personWithAddress
    Person personNullAddress
    List<Person> people

    @Setup(Level.Trial)
    void setup() {
        personWithAddress = new Person(name: "Alice", address: new Address(city: "Springfield", zip: "62704"))
        personNullAddress = new Person(name: "Bob", address: null)
        people = (1..100).collect { new Person(name: "Person$it", address: new Address(city: "City$it", zip: "${10000 + it}")) }
    }

    // ===== SAFE NAVIGATION (?.) =====

    /**
     * Safe navigation on non-null chain — obj?.prop?.prop.
     */
    @Benchmark
    void safeNavNonNull(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += personWithAddress?.address?.city?.length() ?: 0
        }
        bh.consume(sum)
    }

    /**
     * Safe navigation hitting null — tests the short-circuit path.
     */
    @Benchmark
    void safeNavNull(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += personNullAddress?.address?.city?.length() ?: 0
        }
        bh.consume(sum)
    }

    /**
     * Safe navigation vs normal access — baseline for comparison.
     */
    @Benchmark
    void normalNavBaseline(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += personWithAddress.address.city.length()
        }
        bh.consume(sum)
    }

    // ===== SPREAD-DOT (*.) =====

    /**
     * Spread-dot operator — list*.property collects a property from all elements.
     */
    @Benchmark
    void spreadDotProperty(Blackhole bh) {
        for (int i = 0; i < ITERATIONS / 100; i++) {
            bh.consume(people*.name)
        }
    }

    /**
     * Spread-dot with method call — list*.method().
     */
    @Benchmark
    void spreadDotMethod(Blackhole bh) {
        for (int i = 0; i < ITERATIONS / 100; i++) {
            bh.consume(people*.getName())
        }
    }

    /**
     * Spread-dot vs collect — baseline comparison.
     */
    @Benchmark
    void collectBaseline(Blackhole bh) {
        for (int i = 0; i < ITERATIONS / 100; i++) {
            bh.consume(people.collect { it.name })
        }
    }

    // ===== ELVIS (?:) =====

    /**
     * Elvis operator with non-null value — takes the left side.
     */
    @Benchmark
    void elvisNonNull(Blackhole bh) {
        String value = "hello"
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += (value ?: "default").length()
        }
        bh.consume(sum)
    }

    /**
     * Elvis operator with null value — takes the right side.
     */
    @Benchmark
    void elvisNull(Blackhole bh) {
        String value = null
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += (value ?: "default").length()
        }
        bh.consume(sum)
    }

    /**
     * Elvis with empty string (Groovy truth: empty string is falsy).
     */
    @Benchmark
    void elvisEmptyString(Blackhole bh) {
        String value = ""
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += (value ?: "default").length()
        }
        bh.consume(sum)
    }

    // ===== WITH / TAP =====

    /**
     * with {} — executes closure with object as delegate, returns closure result.
     */
    @Benchmark
    void withScope(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += personWithAddress.with {
                name.length() + address.city.length()
            }
        }
        bh.consume(sum)
    }

    /**
     * tap {} — executes closure with object as delegate, returns the object.
     */
    @Benchmark
    void tapScope(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(new Person().tap {
                name = "Test"
                address = new Address(city: "City", zip: "12345")
            })
        }
    }

    // ===== RANGE =====

    /**
     * Range creation — (1..N) creates an IntRange object.
     */
    @Benchmark
    void rangeCreation(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(1..100)
        }
    }

    /**
     * Range iteration with each — (1..N).each { }.
     */
    @Benchmark
    void rangeIteration(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS / 100; i++) {
            (1..100).each { sum += it }
        }
        bh.consume(sum)
    }

    /**
     * Range contains check — (val in range) uses Range.containsWithinBounds.
     */
    @Benchmark
    void rangeContains(Blackhole bh) {
        def range = 1..1000
        int count = 0
        for (int i = 0; i < ITERATIONS; i++) {
            if ((i % 1500) in range) count++
        }
        bh.consume(count)
    }

    // ===== AS TYPE COERCION =====

    /**
     * 'as' coercion: list as Set.
     */
    @Benchmark
    void asListToSet(Blackhole bh) {
        List<Integer> list = [1, 2, 3, 4, 5, 1, 2, 3]
        for (int i = 0; i < ITERATIONS; i++) {
            bh.consume(list as Set)
        }
    }

    /**
     * 'as' coercion: object as String.
     */
    @Benchmark
    void asToString(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += (i as String).length()
        }
        bh.consume(sum)
    }

    /**
     * 'as' coercion: String to Integer.
     */
    @Benchmark
    void asStringToInteger(Blackhole bh) {
        String[] values = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += values[i % 10] as Integer
        }
        bh.consume(sum)
    }
}
