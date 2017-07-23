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

@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CallsiteBench {

    @Benchmark
    public void dispatch_1_monomorphic_groovy(MonomorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_1_monomorphic_java(MonomorphicState state, Blackhole bh) {
        JavaDispatch.dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_3_polymorphic_groovy(PolymorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_3_polymorphic_java(PolymorphicState state, Blackhole bh) {
        JavaDispatch.dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_8_megamorphic_groovy(MegamorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

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

    @State(Scope.Thread)
    public static class MonomorphicState {
        Object[] receivers;
        @Setup(Level.Trial)
        public void setUp() {
            receivers = new Object[RECEIVER_COUNT];
            Arrays.fill(receivers, RECEIVERS[0]);
        }
    }

    @State(Scope.Thread)
    public static class PolymorphicState {
        final Random random = new Random();
        Object[] receivers;
        @Setup(Level.Iteration)
        public void setUp() {
            receivers = new Object[RECEIVER_COUNT];
            for (int i = 0; i < RECEIVER_COUNT; i++) {
                receivers[i] = RECEIVERS[random.nextInt(3)];
            }
        }
    }

    @State(Scope.Thread)
    public static class MegamorphicState {
        final Random random = new Random();
        Object[] receivers;
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
