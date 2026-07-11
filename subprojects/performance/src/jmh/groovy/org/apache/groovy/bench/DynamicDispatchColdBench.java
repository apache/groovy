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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * Cold-start counterpart to the steady-state dispatch benchmarks — measures
 * fresh <em>instance</em>-dispatch call sites (dynamic method calls, property
 * access, closure invocation) before they reach steady state, complementing
 * {@link StaticMethodCallIndyColdBench} which covers only static calls.
 * <p>
 * Instance dispatch takes a different cold path than static dispatch: static
 * indy call sites get their target installed on the first hit (GROOVY-11935),
 * while instance sites stay on the bootstrap adapter until the hit-count
 * threshold ({@code groovy.indy.optimize.threshold}, default 1000) promotes
 * them. The {@code n} parameter probes both regimes:
 * <ul>
 *   <li>{@code n=500} — entirely below the threshold; every invocation runs
 *       the cached-handle lookup path,</li>
 *   <li>{@code n=20000} — well past it; the call site promotes early and the
 *       result approaches steady state.</li>
 * </ul>
 * {@link Mode#SingleShotTime} with zero warmup and a fresh JVM per fork; the
 * mean over forks estimates true cold cost. The Java and
 * {@code @CompileStatic} rows are direct-dispatch lower bounds. The fork
 * count is a compromise between sampling error and CI wall-clock (the suite
 * job has a 60-minute budget shared with the other cold bench).
 * <p>
 * Useful for guarding {@code IndyInterface}/{@code Selector}/
 * {@code CacheableCallSite} changes that alter cold instance dispatch —
 * polymorphic-inline-cache reworks, promotion-threshold changes, boot-handle
 * simplifications — which the throughput benches cannot see.
 */
@Warmup(iterations = 0)
@Measurement(iterations = 1, batchSize = 1)
@Fork(25)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class DynamicDispatchColdBench {

    /**
     * Iteration count probing below (500) and past (20000) the indy
     * hit-count promotion threshold.
     */
    @Param({"500", "20000"})
    public int n;

    /**
     * Monomorphic dynamic instance calls from a cold call site.
     * @return the computed sum
     */
    @Benchmark
    public int dynamicMono_groovy() {
        return new DynamicDispatchCold().monoSum(n);
    }

    /**
     * Polymorphic (3 receiver types) dynamic instance calls from a cold call site.
     * @return the computed sum
     */
    @Benchmark
    public int dynamicPoly_groovy() {
        return new DynamicDispatchCold().polySum(n);
    }

    /**
     * As {@link #dynamicMono_groovy} but with the reflective cold tier
     * explicitly disabled (GROOVY-12137). The tier is on by default, so the
     * plain variant measures the shipping (enabled) cold path and this is the
     * disabled baseline for the cold-dispatch A/B.
     * @return the computed sum
     */
    @Benchmark
    @Fork(value = 25, jvmArgsAppend = "-Dgroovy.indy.cold.reflection=false")
    public int dynamicMono_groovyColdReflect() {
        return new DynamicDispatchCold().monoSum(n);
    }

    /**
     * As {@link #dynamicPoly_groovy} but with the reflective cold tier
     * explicitly disabled (GROOVY-12137) — the disabled baseline for the
     * polymorphic cold-dispatch A/B (the tier is on by default).
     * @return the computed sum
     */
    @Benchmark
    @Fork(value = 25, jvmArgsAppend = "-Dgroovy.indy.cold.reflection=false")
    public int dynamicPoly_groovyColdReflect() {
        return new DynamicDispatchCold().polySum(n);
    }

    /**
     * Dynamic property reads from a cold call site.
     * @return the computed sum
     */
    @Benchmark
    public int property_groovy() {
        return new DynamicDispatchCold().propertySum(n);
    }

    /**
     * Dynamic closure invocation from a cold call site.
     * @return the computed sum
     */
    @Benchmark
    public int closure_groovy() {
        return new DynamicDispatchCold().closureSum(n);
    }

    /**
     * {@code @CompileStatic} lower bound for the monomorphic case.
     * @return the computed sum
     */
    @Benchmark
    public int dynamicMono_groovyCS() {
        return new DynamicDispatchCold().monoSumCS(n);
    }

    /**
     * Java lower bound (direct invokevirtual) for the monomorphic case.
     * @return the computed sum
     */
    @Benchmark
    public int dynamicMono_java() {
        return javaMonoSum(n);
    }

    private static int javaMonoSum(int n) {
        JavaAlpha r = new JavaAlpha();
        int s = 0;
        for (int i = 0; i < n; i++) {
            s += r.work(i);
        }
        return s;
    }

    static final class JavaAlpha {
        int work(int i) {
            return i + 1;
        }
    }
}
