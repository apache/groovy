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
 * Cold-start counterpart to {@link StaticMethodCallIndyBench} — measures the
 * cost of a fresh {@code invokedynamic} call site before it reaches
 * steady state.
 * <p>
 * The sibling {@code Mode.Throughput} benchmark warms up for several
 * seconds before measuring, by which time the call site's hit-count
 * optimisation path ({@code INDY_OPTIMIZE_THRESHOLD = 1000}) has already
 * relinked the target. That makes it blind to anything that happens in
 * the first ~1000 invocations — which is where a lot of real-world
 * Groovy cost lives (scripts, short-lived CLIs, {@code groovysh}
 * startup, test bootstrap, Gradle task graphs that repeatedly invoke
 * Groovy code). This benchmark fills that gap.
 * <p>
 * The bench uses {@link Mode#SingleShotTime} with a fresh JVM per fork
 * and zero warmup. The {@code n} parameter sweeps three regimes around
 * {@code INDY_OPTIMIZE_THRESHOLD}:
 * <ul>
 *   <li>{@code n=500} — entirely below the threshold; the call site
 *       stays on its bootstrap adapter for the whole measurement.</li>
 *   <li>{@code n=2000} — straddles the threshold; the hit-count path
 *       relinks about halfway through.</li>
 *   <li>{@code n=20000} — well past the threshold; the call site is
 *       relinked early and behaves close to steady state.</li>
 * </ul>
 * The Java and {@code @CompileStatic} rows give absolute lower bounds
 * (direct {@code invokestatic}); the instance row is a control that
 * exercises the same dispatch path without the {@code Class}-receiver
 * fast path.
 * <p>
 * Useful for:
 * <ul>
 *   <li>guarding against regressions in {@code IndyInterface} /
 *       {@code Selector} / {@code CacheableCallSite} that hurt cold-path
 *       dispatch (the throughput bench would not catch them),</li>
 *   <li>quantifying improvements that reduce time-to-inline for static
 *       indy call sites (e.g. lowering the relink threshold, smarter
 *       bootstrap handles, early target installation),</li>
 *   <li>tracking the remaining gap between Groovy dynamic and
 *       {@code @CompileStatic}/Java on cold paths.</li>
 * </ul>
 */
@Warmup(iterations = 0)
@Measurement(iterations = 1, batchSize = 1)
@Fork(80)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class StaticMethodCallIndyColdBench {

    @Param({"500", "2000", "20000"})
    /**
     * Input size used to exercise different cold-start dispatch regimes.
     */
    public int n;

    /**
     * Java baseline for cold-start sum.
     * @return the computed sum
     */
    @Benchmark
    public int staticSum_java() {
        return javaSum(n);
    }

    /**
     * Groovy dynamic cold-start static sum.
     * @return the computed sum
     */
    @Benchmark
    public int staticSum_groovy() {
        return StaticMethodCallIndy.staticSum(n);
    }

    /**
     * Groovy {@code @CompileStatic} cold-start static sum.
     * @return the computed sum
     */
    @Benchmark
    public int staticSum_groovyCS() {
        return StaticMethodCallIndy.staticSumCS(n);
    }

    /**
     * Groovy instance cold-start sum (control group).
     * @return the computed sum
     */
    @Benchmark
    public int instanceSum_groovy() {
        return new StaticMethodCallIndy().instanceSum(n);
    }

    private static int javaSum(int n) {
        int s = 0;
        for (int i = 0; i < n; i++) {
            s = s + i;
        }
        return s;
    }
}
