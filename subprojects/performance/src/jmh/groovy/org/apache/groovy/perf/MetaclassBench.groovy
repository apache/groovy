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

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * Tests the overhead of dynamic method invocation and dispatch in Groovy in the presence of Metaclass changes.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class MetaclassBench {
    /** Number of metaclass mutation iterations used by each benchmark invocation. */
    static final int ITERATIONS = 100_000

    /**
     * Helper class for metaclass modification tests.
     */
    class Foo {
        /** Returns zero for one benchmark call-site variant. */
        int m0() { 0 }

        /** Returns one for one benchmark call-site variant. */
        int m1() { 1 }

        /** Returns two for one benchmark call-site variant. */
        int m2() { 2 }

        /** Returns three for one benchmark call-site variant. */
        int m3() { 3 }

        /** Returns four for one benchmark call-site variant. */
        int m4() { 4 }

        /** Returns five for one benchmark call-site variant. */
        int m5() { 5 }

        /** Returns six for one benchmark call-site variant. */
        int m6() { 6 }

        /** Returns seven for one benchmark call-site variant. */
        int m7() { 7 }

        /** Returns eight for one benchmark call-site variant. */
        int m8() { 8 }

        /** Returns nine for one benchmark call-site variant. */
        int m9() { 9 }

        /** Returns ten for one benchmark call-site variant. */
        int m10() { 10 }

        /** Returns eleven for one benchmark call-site variant. */
        int m11() { 11 }

        /** Returns twelve for one benchmark call-site variant. */
        int m12() { 12 }

        /** Returns thirteen for one benchmark call-site variant. */
        int m13() { 13 }

        /** Returns fourteen for one benchmark call-site variant. */
        int m14() { 14 }

        /** Returns fifteen for one benchmark call-site variant. */
        int m15() { 15 }
    }

    /**
     * Benchmark: method calls in the presence of metaclass modifications.
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    void methodCallsWithMetaclassChanges(Blackhole bh) {
        long sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            Foo.metaClass."meth${i % 16}" = { -> i }
            def foo = new Foo()
            sum += foo.m0() + foo.m1() + foo.m2() + foo.m3() + foo.m4() + foo.m5() + foo.m6() + foo.m7() +
                foo.m8() + foo.m9() + foo.m10() + foo.m11() + foo.m12() + foo.m13() + foo.m14() + foo.m15()
        }
        bh.consume(sum)
    }

}
