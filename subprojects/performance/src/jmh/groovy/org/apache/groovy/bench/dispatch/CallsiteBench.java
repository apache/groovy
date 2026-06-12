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
package org.apache.groovy.bench.dispatch;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing dispatch performance under monomorphic, polymorphic,
 * and megamorphic call site conditions for Java, Groovy, and Groovy {@code @CompileStatic}.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CallsiteBench {

    /**
     * Monomorphic dispatch via Groovy dynamic.
     * @param state the monomorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_1_monomorphic_groovy(MonomorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

    /**
     * Monomorphic dispatch via Groovy {@code @CompileStatic}.
     * @param state the monomorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_1_monomorphic_groovyCS(MonomorphicState state, Blackhole bh) {
        Callsite.dispatchCS(state.receivers, bh);
    }

    /**
     * Monomorphic dispatch via Java baseline.
     * @param state the monomorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_1_monomorphic_java(MonomorphicState state, Blackhole bh) {
        JavaDispatch.dispatch(state.receivers, bh);
    }

    /**
     * Polymorphic dispatch (3 types) via Groovy dynamic.
     * @param state the polymorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_3_polymorphic_groovy(PolymorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

    /**
     * Polymorphic dispatch (3 types) via Groovy {@code @CompileStatic}.
     * @param state the polymorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_3_polymorphic_groovyCS(PolymorphicState state, Blackhole bh) {
        Callsite.dispatchCS(state.receivers, bh);
    }

    /**
     * Polymorphic dispatch (3 types) via Java baseline.
     * @param state the polymorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_3_polymorphic_java(PolymorphicState state, Blackhole bh) {
        JavaDispatch.dispatch(state.receivers, bh);
    }

    /**
     * Megamorphic dispatch (8 types) via Groovy dynamic.
     * @param state the megamorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_8_megamorphic_groovy(MegamorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

    /**
     * Megamorphic dispatch (8 types) via Groovy {@code @CompileStatic}.
     * @param state the megamorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_8_megamorphic_groovyCS(MegamorphicState state, Blackhole bh) {
        Callsite.dispatchCS(state.receivers, bh);
    }

    /**
     * Megamorphic dispatch (8 types) via Java baseline.
     * @param state the megamorphic receiver state
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void dispatch_8_megamorphic_java(MegamorphicState state, Blackhole bh) {
        JavaDispatch.dispatch(state.receivers, bh);
    }

    private static class JavaDispatch {
        static void dispatch(Object[] receivers, Blackhole bh) {
            for (Object receiver : receivers) {
                bh.consume(receiver.toString());
            }
        }
    }

    private static final int RECEIVER_COUNT = 64;

    private static final Object[] RECEIVERS = new Object[] {
            new Receiver1(), new Receiver2(), new Receiver3(), new Receiver4(),
            new Receiver5(), new Receiver6(), new Receiver7(), new Receiver8()
    };

    /**
     * State holder for monomorphic (single type) call sites.
     */
    @State(Scope.Thread)
    public static class MonomorphicState {
        /**
         * Receiver array that always contains the same target type.
         */
        Object[] receivers;

        /**
         * Populates the receiver array with a single repeated target instance.
         */
        @Setup(Level.Trial)
        public void setUp() {
            receivers = new Object[RECEIVER_COUNT];
            Arrays.fill(receivers, RECEIVERS[0]);
        }
    }

    /**
     * State holder for polymorphic (3 types) call sites.
     */
    @State(Scope.Thread)
    public static class PolymorphicState {
        /**
         * Source of randomized receiver selection for polymorphic dispatch.
         */
        final Random random = new Random();

        /**
         * Receiver array containing a mix of three target types.
         */
        Object[] receivers;

        /**
         * Rebuilds the receiver array for a three-type polymorphic distribution.
         */
        @Setup(Level.Iteration)
        public void setUp() {
            receivers = new Object[RECEIVER_COUNT];
            for (int i = 0; i < RECEIVER_COUNT; i++) {
                receivers[i] = RECEIVERS[random.nextInt(3)];
            }
        }
    }

    /**
     * State holder for megamorphic (8 types) call sites.
     */
    @State(Scope.Thread)
    public static class MegamorphicState {
        /**
         * Source of randomized receiver selection for megamorphic dispatch.
         */
        final Random random = new Random();

        /**
         * Receiver array containing a mix of all benchmark target types.
         */
        Object[] receivers;

        /**
         * Rebuilds the receiver array for an eight-type megamorphic distribution.
         */
        @Setup(Level.Iteration)
        public void setUp() {
            receivers = new Object[RECEIVER_COUNT];
            for (int i = 0; i < RECEIVER_COUNT; i++) {
                receivers[i] = RECEIVERS[random.nextInt(8)];
            }
        }
    }

    private static class Receiver1 {
        @Override public String toString() { return "receiver1"; }
    }

    private static class Receiver2 {
        @Override public String toString() { return "receiver2"; }
    }

    private static class Receiver3 {
        @Override public String toString() { return "receiver3"; }
    }

    private static class Receiver4 {
        @Override public String toString() { return "receiver4"; }
    }

    private static class Receiver5 {
        @Override public String toString() { return "receiver5"; }
    }

    private static class Receiver6 {
        @Override public String toString() { return "receiver6"; }
    }

    private static class Receiver7 {
        @Override public String toString() { return "receiver7"; }
    }

    private static class Receiver8 {
        @Override public String toString() { return "receiver8"; }
    }

}
