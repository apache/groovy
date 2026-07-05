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
package org.apache.groovy.calibration;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * Host-speed calibration "rulers": pure-Java benchmarks that are entirely
 * independent of Groovy, so any change in their results reflects the CI
 * runner (hardware, JDK build, neighbours) rather than the code under test.
 * <p>
 * The per-commit summary ({@code dashboard/jmh-summary.py}) compares each
 * run against the trailing 90-day history published on gh-pages. Those
 * historical numbers come from <em>different runner hardware</em>, which is
 * the dominant noise source in per-PR comparisons. The rulers measure that
 * hardware delta directly: the geomean of their current-vs-history ratios is
 * the run's <em>calibration factor</em>, used to (a) normalise the Groovy
 * benchmark ratios and (b) flag low-confidence runs whose hardware deviates
 * too far from the historical baseline.
 * <p>
 * Three rulers cover the dimensions on which shared runners differ most:
 * <ul>
 *   <li>{@link #cpuIntegerOps()} — scalar integer throughput (core speed),</li>
 *   <li>{@link #memoryPointerChase()} — dependent loads over a 1&nbsp;MiB
 *       permutation (cache/memory subsystem),</li>
 *   <li>{@link #allocationChurn()} — small-object allocation pressure
 *       (allocation/GC speed).</li>
 * </ul>
 * All work is deterministic (fixed seeds, fixed iteration counts) and, being
 * plain Java, produces identical bytecode in the indy and classic suites.
 * <p>
 * Each CI suite job runs on its own runner, so each suite needs its own
 * ruler. The concrete subclasses exist only to place one copy inside every
 * suite's {@code benchInclude} pattern (see {@code .github/workflows/groovy-jmh*.yml}):
 * <ul>
 *   <li>{@code org.apache.groovy.bench.CalibrationBench} — suite {@code bench} ({@code \.bench\.})</li>
 *   <li>{@code org.apache.groovy.perf.CalibrationBench} — suite {@code core-ag} ({@code \.perf\.[A-G]})</li>
 *   <li>{@code org.apache.groovy.perf.HostCalibrationBench} — suite {@code core-hz} ({@code \.perf\.[H-Z]})</li>
 *   <li>{@code org.apache.groovy.perf.grails.CalibrationBench} — suite {@code grails-ad} ({@code \.perf\.grails\.[A-D]})</li>
 *   <li>{@code org.apache.groovy.perf.grails.HostCalibrationBench} — suite {@code grails-ez} ({@code \.perf\.grails\.[E-Z]})</li>
 * </ul>
 * Keep "Calibration" in any subclass name: the summary tooling identifies
 * rulers by that marker and excludes them from the Groovy speedup geomeans.
 * The reduced warmup/measurement keeps the cost near one minute per suite;
 * rulers need collective, not individual, precision.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(2)
@State(Scope.Thread)
public abstract class AbstractCalibrationBench {

    private static final int CHAIN_SIZE = 1 << 18; // 1 MiB of ints: larger than typical L2
    private static final int HOLD_SLOTS = 32;

    private int[] chain;
    private Object[] holder;

    /**
     * Builds a single-cycle permutation (Sattolo's algorithm with a fixed
     * LCG) so the pointer chase visits all elements without short cycles.
     */
    @Setup(Level.Trial)
    public void setup() {
        chain = new int[CHAIN_SIZE];
        for (int i = 0; i < CHAIN_SIZE; i++) {
            chain[i] = i;
        }
        long seed = 0x9E3779B97F4A7C15L;
        for (int i = CHAIN_SIZE - 1; i > 0; i--) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int j = (int) Math.floorMod(seed >>> 33, (long) i);
            int tmp = chain[i];
            chain[i] = chain[j];
            chain[j] = tmp;
        }
        holder = new Object[HOLD_SLOTS];
    }

    /**
     * Scalar integer throughput ruler (xorshift64).
     * @return the mixed state, to defeat dead-code elimination
     */
    @Benchmark
    public long cpuIntegerOps() {
        long x = 0xDEADBEEFCAFEBABEL;
        for (int i = 0; i < 200_000; i++) {
            x ^= x << 13;
            x ^= x >>> 7;
            x ^= x << 17;
        }
        return x;
    }

    /**
     * Memory-subsystem ruler: dependent loads through a 1 MiB permutation.
     * @return the final chase index, to defeat dead-code elimination
     */
    @Benchmark
    public int memoryPointerChase() {
        int[] c = chain;
        int idx = 0;
        for (int i = 0; i < 100_000; i++) {
            idx = c[idx];
        }
        return idx;
    }

    /**
     * Allocation-pressure ruler: small arrays escaping into a rotating
     * holder so escape analysis cannot scalar-replace them.
     * @return a value derived from the allocations, to defeat dead-code elimination
     */
    @Benchmark
    public long allocationChurn() {
        long s = 0;
        Object[] h = holder;
        for (int i = 0; i < 10_000; i++) {
            int[] a = new int[16];
            a[i & 15] = i;
            h[i & (HOLD_SLOTS - 1)] = a;
            s += a[i & 15];
        }
        return s;
    }
}
