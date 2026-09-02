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

import org.codehaus.groovy.reflection.CachedMethod;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Pipeline-split microbench for {@code CachedMethod.invoke}.
 * <p>
 * <b>Baseline</b> is reflective {@code CachedMethod.invoke}
 * ({@code groovy.cachedmethod.invoker.disable=true}) — the path this change
 * replaces. {@code startsWith_java} is a lower bound, not the baseline.
 * Generated rows are {@code threshold=0} after a warmup invoke so measurement
 * is steady-state. {@code generate_*} rows use a fresh {@code CachedMethod}
 * per invocation to price hidden-class definition. {@code burst_*} rows walk
 * the default threshold (100) so the break-even vs reflective is visible.
 * <p>
 * Guard ratios, not absolute nanoseconds. Run with
 * {@code :performance:jmh -PbenchInclude=CachedMethodInvoker}.
 */
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class CachedMethodInvokerBench {

    /** Non-constant so HotSpot cannot fold {@code startsWith} to {@code true}. */
    @Param("abcdef")
    public String receiver;

    @Param("abc")
    public String prefix;

    private CachedMethod startsWith;
    private CachedMethod[] mega;
    private String[] megaReceivers;
    private Object[] prefixArgs;

    /**
     * Resolves the {@code CachedMethod}s used by the reflective / generated rows.
     *
     * @throws Exception if {@code String.startsWith} cannot be resolved
     */
    @Setup
    public void setUp() throws Exception {
        prefixArgs = new Object[]{prefix};
        startsWith = new CachedMethod(String.class.getMethod("startsWith", String.class));
        mega = new CachedMethod[]{
                new CachedMethod(String.class.getMethod("startsWith", String.class)),
                new CachedMethod(String.class.getMethod("endsWith", String.class)),
                new CachedMethod(String.class.getMethod("contains", CharSequence.class)),
                new CachedMethod(String.class.getMethod("isEmpty")),
                new CachedMethod(Integer.class.getMethod("toString")),
        };
        megaReceivers = new String[]{"abcdef", "xyz", "foo", ""};
        // Warm generation when threshold=0 so measurement is steady-state.
        startsWith.invoke(receiver, prefixArgs);
        mega[0].invoke(megaReceivers[0], prefixArgs);
        mega[1].invoke(megaReceivers[1], new Object[]{"z"});
        mega[2].invoke(megaReceivers[2], new Object[]{"oo"});
        mega[3].invoke(megaReceivers[3], new Object[0]);
        mega[4].invoke(Integer.valueOf(7), new Object[0]);
    }

    /**
     * Lower bound: Java {@code String.startsWith}. Not the change's baseline.
     *
     * @return whether the prefix matches
     */
    @Benchmark
    public boolean startsWith_java() {
        return receiver.startsWith(prefix);
    }

    /**
     * Baseline: {@code CachedMethod.invoke} with generation disabled.
     *
     * @return boxed {@code Boolean}
     */
    @Benchmark
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.disable=true")
    public Object startsWith_reflective() {
        return startsWith.invoke(receiver, prefixArgs);
    }

    /**
     * Steady-state {@code CachedMethod.invoke} after first-hit generation.
     *
     * @return boxed {@code Boolean}
     */
    @Benchmark
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.threshold=0")
    public Object startsWith_generated() {
        return startsWith.invoke(receiver, prefixArgs);
    }

    /**
     * Many distinct {@code CachedMethod}s through one {@code invoke} site,
     * generation disabled.
     *
     * @param bh consumes every result so JIT cannot drop unused invokes
     */
    @Benchmark
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.disable=true")
    public void mega_reflective(final Blackhole bh) {
        bh.consume(mega[0].invoke(megaReceivers[0], prefixArgs));
        bh.consume(mega[1].invoke(megaReceivers[1], new Object[]{"z"}));
        bh.consume(mega[2].invoke(megaReceivers[2], new Object[]{"oo"}));
        bh.consume(mega[3].invoke(megaReceivers[3], new Object[0]));
        bh.consume(mega[4].invoke(Integer.valueOf(7), new Object[0]));
    }

    /**
     * Many distinct {@code CachedMethod}s through one {@code invoke} site,
     * after first-hit generation.
     *
     * @param bh consumes every result so JIT cannot drop unused invokes
     */
    @Benchmark
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.threshold=0")
    public void mega_generated(final Blackhole bh) {
        bh.consume(mega[0].invoke(megaReceivers[0], prefixArgs));
        bh.consume(mega[1].invoke(megaReceivers[1], new Object[]{"z"}));
        bh.consume(mega[2].invoke(megaReceivers[2], new Object[]{"oo"}));
        bh.consume(mega[3].invoke(megaReceivers[3], new Object[0]));
        bh.consume(mega[4].invoke(Integer.valueOf(7), new Object[0]));
    }

    /**
     * Fresh {@code CachedMethod} per invocation so generation is not amortized.
     */
    @State(Scope.Thread)
    public static class FreshMethod {
        @Param("abcdef")
        public String receiver;

        @Param("abc")
        public String prefix;

        CachedMethod method;
        Object[] args;

        /**
         * New {@code CachedMethod} each invocation: generation is not reused.
         *
         * @throws Exception if {@code String.startsWith} cannot be resolved
         */
        @Setup(Level.Invocation)
        public void fresh() throws Exception {
            method = new CachedMethod(String.class.getMethod("startsWith", String.class));
            args = new Object[]{prefix};
        }
    }

    /**
     * One invoke on a never-seen {@code CachedMethod}, generation disabled.
     * Compare with {@link #generate_then_invoke} for hidden-class define cost.
     *
     * @param state fresh {@code CachedMethod}
     * @return boxed {@code Boolean}
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.disable=true")
    public Object generate_reflective_fresh(final FreshMethod state) {
        return state.method.invoke(state.receiver, state.args);
    }

    /**
     * One invoke on a never-seen {@code CachedMethod} with {@code threshold=0}:
     * includes hidden-class definition.
     *
     * @param state fresh {@code CachedMethod}
     * @return boxed {@code Boolean}
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.threshold=0")
    public Object generate_then_invoke(final FreshMethod state) {
        return state.method.invoke(state.receiver, state.args);
    }

    /**
     * Burst of invokes on a fresh {@code CachedMethod}. {@code calls=100} stays
     * under the default threshold; {@code calls=250} inflates at hit 101.
     */
    @State(Scope.Thread)
    public static class Burst {
        @Param({"100", "250"})
        public int calls;

        @Param("abcdef")
        public String receiver;

        @Param("abc")
        public String prefix;

        CachedMethod method;
        Object[] args;

        /**
         * New {@code CachedMethod} so the hit counter starts at zero.
         *
         * @throws Exception if {@code String.startsWith} cannot be resolved
         */
        @Setup(Level.Invocation)
        public void fresh() throws Exception {
            method = new CachedMethod(String.class.getMethod("startsWith", String.class));
            args = new Object[]{prefix};
        }
    }

    /**
     * Default threshold (100), generation enabled. Break-even vs
     * {@link #burst_reflective}.
     *
     * @param state burst length and fresh {@code CachedMethod}
     * @param bh    consumes every result
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void burst_defaultThreshold(final Burst state, final Blackhole bh) {
        final CachedMethod method = state.method;
        final String receiver = state.receiver;
        final Object[] args = state.args;
        for (int i = 0, n = state.calls; i < n; i++) {
            bh.consume(method.invoke(receiver, args));
        }
    }

    /**
     * Same burst with generation disabled — reflective baseline for break-even.
     *
     * @param state burst length and fresh {@code CachedMethod}
     * @param bh    consumes every result
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.disable=true")
    public void burst_reflective(final Burst state, final Blackhole bh) {
        final CachedMethod method = state.method;
        final String receiver = state.receiver;
        final Object[] args = state.args;
        for (int i = 0, n = state.calls; i < n; i++) {
            bh.consume(method.invoke(receiver, args));
        }
    }
}
