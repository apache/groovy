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
package org.apache.groovy.bench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks exact-final receiver call sites independently of the static-method benchmarks.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class FinalInstanceMethodCallIndyBench {

    private static final int SUM_N = 1000;
    private static final int FIB_N = 25;
    private static final int CHAIN_ITERATIONS = 1000;

    private FinalInstanceMethodCallIndy finalInstance;
    private StaticMethodCallIndy instance;

    @Setup
    public void setUp() {
        finalInstance = new FinalInstanceMethodCallIndy();
        instance = new StaticMethodCallIndy();
    }

    @Benchmark
    public int finalInstanceSum_groovy() {
        return finalInstance.instanceSum(SUM_N);
    }

    @Benchmark
    public int instanceSum_groovy() {
        return instance.instanceSum(SUM_N);
    }

    @Benchmark
    public int finalInstanceFib_groovy() {
        return finalInstance.instanceFib(FIB_N);
    }

    @Benchmark
    public int instanceFib_groovy() {
        return instance.instanceFib(FIB_N);
    }

    @Benchmark
    public int finalInstanceChain_groovy() {
        int result = 0;
        for (int i = 0; i < CHAIN_ITERATIONS; i++) {
            result += finalInstance.instanceChain(i);
        }
        return result;
    }

    @Benchmark
    public int instanceChain_groovy() {
        int result = 0;
        for (int i = 0; i < CHAIN_ITERATIONS; i++) {
            result += instance.instanceChain(i);
        }
        return result;
    }
}
