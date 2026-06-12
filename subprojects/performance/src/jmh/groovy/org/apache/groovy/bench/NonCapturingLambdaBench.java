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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for non-capturing lambda optimization (GROOVY-11905).
 * <p>
 * Non-capturing lambdas in {@code @CompileStatic} code are emitted as
 * static methods and cached by {@code LambdaMetafactory}, achieving
 * zero per-call allocation.  These benchmarks quantify the benefit.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NonCapturingLambdaBench {

    /**
     * Benchmark non-capturing lambda application.
     * @return the result value
     */
    @Benchmark
    public int nonCapturingLambdaApply() {
        return NonCapturingLambda.applyNonCapturingLambda(42);
    }

    /**
     * Benchmark capturing lambda application.
     * @return the result value
     */
    @Benchmark
    public int capturingLambdaApply() {
        return NonCapturingLambda.applyCapturingLambda(42);
    }

    /**
     * Benchmark stream map with non-capturing lambda.
     * @param bh the blackhole for consuming results
     */
    @Benchmark
    public void streamMapNonCapturing(Blackhole bh) {
        bh.consume(NonCapturingLambda.streamMapNonCapturing(List.of(1, 2, 3, 4, 5)));
    }

    /**
     * Benchmark stream reduce with non-capturing lambda.
     * @return the sum
     */
    @Benchmark
    public int streamReduceNonCapturing() {
        return NonCapturingLambda.streamReduceNonCapturing(100);
    }
}
