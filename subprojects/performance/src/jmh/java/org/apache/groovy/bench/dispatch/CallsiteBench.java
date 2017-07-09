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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class CallsiteBench {

    @Benchmark
    public void dispatch_1_monomorphic_groovy(MonomorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_1_monomorphic_java(MonomorphicState state, Blackhole bh) {
        dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_3_polymorphic_groovy(PolymorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_3_polymorphic_java(PolymorphicState state, Blackhole bh) {
        dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_8_megamorphic_groovy(MegamorphicState state, Blackhole bh) {
        Callsite.dispatch(state.receivers, bh);
    }

    @Benchmark
    public void dispatch_8_megamorphic_java(MegamorphicState state, Blackhole bh) {
        dispatch(state.receivers, bh);
    }

    private void dispatch(Object[] receivers, Blackhole bh) {
        for (Object receiver : receivers) {
            bh.consume(receiver.toString());
        }
    }

    private static final int RECEIVER_COUNT = 1024;

    @State(Scope.Thread)
    public static class MonomorphicState {
        Object[] receivers;
        @Setup(Level.Trial)
        public void setUp() {
            receivers = new Object[RECEIVER_COUNT];
            for (int i = 0; i < RECEIVER_COUNT; i++) {
                receivers[i] = i;
            }
        }
    }

    @State(Scope.Thread)
    public static class PolymorphicState {
        Object[] receivers;
        @Setup(Level.Trial)
        public void setUp() {
            receivers = new Object[RECEIVER_COUNT];
            for (int i = 0; i < RECEIVER_COUNT; i++) {
                switch (i % 3) {
                    case 0:
                        receivers[i] = 7;
                        break;
                    case 1:
                        receivers[i] = new ArrayList<>();
                        break;
                    case 2:
                        receivers[i] = "Test String";
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        }
    }

    @State(Scope.Thread)
    public static class MegamorphicState {
        Object[] receivers;
        @Setup(Level.Trial)
        public void setUp() {
            Object[] suspects = new Object[] {
                    9,
                    new Object(),
                    "Hello World",
                    new ArrayList<>(),
                    123.456f,
                    new HashMap<>(),
                    true,
                    new Date()
            };
            receivers = new Object[RECEIVER_COUNT];
            for (int i = 0; i < RECEIVER_COUNT; i++) {
                receivers[i] = suspects[i % suspects.length];
            }
        }
    }

}
