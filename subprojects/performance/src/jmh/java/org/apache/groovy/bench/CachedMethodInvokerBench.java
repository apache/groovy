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
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Pipeline-split microbench for {@code CachedMethod.invoke}: Java direct call,
 * reflective MOP invoke, and the generated {@code DirectInvoker} trampoline.
 * <p>
 * The generated path is the <em>monomorphic</em> best case (one
 * {@code CachedMethod}, one trampoline class). Real {@code MetaClassImpl}
 * dispatch across many types is megamorphic at the
 * {@code DirectInvoker.invoke} call site; the {@code mega} rows exercise that.
 * Guard ratios, not absolute nanoseconds. Run with
 * {@code :perf:jmh -PbenchInclude=CachedMethodInvoker}.
 * <p>
 * Fork JVM args pin the generator: {@code threshold=0} installs on first
 * invoke; {@code disable=true} stays on {@code Method.invoke}.
 */
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class CachedMethodInvokerBench {

    private static final String RECEIVER = "abcdef";
    private static final String PREFIX = "abc";

    private CachedMethod startsWith;
    private CachedMethod[] mega;
    private String[] megaReceivers;

    /**
     * Resolves the {@code CachedMethod}s used by the reflective / generated rows.
     */
    @Setup
    public void setUp() throws Exception {
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
        startsWith.invoke(RECEIVER, new Object[]{PREFIX});
        mega[0].invoke(megaReceivers[0], new Object[]{PREFIX});
        mega[1].invoke(megaReceivers[1], new Object[]{"z"});
        mega[2].invoke(megaReceivers[2], new Object[]{"oo"});
        mega[3].invoke(megaReceivers[3], new Object[0]);
        mega[4].invoke(Integer.valueOf(7), new Object[0]);
    }

    /**
     * Java baseline: {@code String.startsWith}.
     *
     * @return whether the prefix matches
     */
    @Benchmark
    public boolean startsWith_java() {
        return RECEIVER.startsWith(PREFIX);
    }

    /**
     * {@code CachedMethod.invoke} with generation disabled.
     *
     * @return boxed {@code Boolean}
     */
    @Benchmark
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.disable=true")
    public Object startsWith_reflective() {
        return startsWith.invoke(RECEIVER, new Object[]{PREFIX});
    }

    /**
     * {@code CachedMethod.invoke} after first-hit generation.
     *
     * @return boxed {@code Boolean}
     */
    @Benchmark
    @Fork(value = 1, jvmArgsAppend = "-Dgroovy.cachedmethod.invoker.threshold=0")
    public Object startsWith_generated() {
        return startsWith.invoke(RECEIVER, new Object[]{PREFIX});
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
        bh.consume(mega[0].invoke(megaReceivers[0], new Object[]{PREFIX}));
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
        bh.consume(mega[0].invoke(megaReceivers[0], new Object[]{PREFIX}));
        bh.consume(mega[1].invoke(megaReceivers[1], new Object[]{"z"}));
        bh.consume(mega[2].invoke(megaReceivers[2], new Object[]{"oo"}));
        bh.consume(mega[3].invoke(megaReceivers[3], new Object[0]));
        bh.consume(mega[4].invoke(Integer.valueOf(7), new Object[0]));
    }
}
