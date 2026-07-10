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

import groovy.lang.GroovyClassLoader;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Steady-state closure invocation under closure packing (GROOVY-12151,
 * {@code groovy.target.closure.pack}) versus generated closure classes, for the
 * two hot closure shapes: a non-capturing {@code collect} body and a
 * written-capture {@code each} accumulator.
 * <p>
 * The flag is read at closure-emission time, so {@link #setup} compiles the
 * fixture classes with a fresh {@link GroovyClassLoader} after setting it from
 * the {@code pack} param — JMH runs each param value in its own forks, so the
 * two states never share a JVM or its profile. A vacuity guard asserts the flag
 * really flipped the emission (no {@code _closure} classes when packing, some
 * when not).
 * <p>
 * Each shape runs twice: {@code mono} always invokes one fixture class, the
 * best case for JIT inlining; {@code mega} rotates over {@link #CLASSES}
 * fixture classes, making the shared {@code PackedClosure} adapter's dispatcher
 * call site megamorphic (each class has its own hidden dispatcher class) — the
 * realistic many-classes regime, and the honest comparison against generated
 * closure classes, whose {@code call} sites in DGM are equally megamorphic.
 * Single-class microbenches overstate packed dispatch costs (a deep monomorphic
 * inline of the shared adapter trips the JIT's {@code InlineSmallCode}
 * heuristic); this pair brackets the real-world range instead.
 */
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ClosurePackBench {

    /** Implemented by every compiled fixture class, so invocation needs no reflection. */
    public interface PackWorkload {
        Object squared(List<Integer> xs);
        int sum(List<Integer> xs);
    }

    private static final int CLASSES = 16; // power of two: mega rotation uses a mask

    /** Compile-time flag: {@code true} packs eligible closures, {@code false} = closure classes. */
    @Param({"false", "true"})
    public String pack;

    private final PackWorkload[] fixtures = new PackWorkload[CLASSES];
    private List<Integer> xs;
    private int idx;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        System.setProperty("groovy.target.closure.pack", pack);
        try (GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())) {
            for (int i = 0; i < CLASSES; i += 1) {
                String source =
                        "@groovy.transform.CompileStatic\n" +
                        "class Fix" + i + " implements org.apache.groovy.bench.ClosurePackBench.PackWorkload {\n" +
                        "    Object squared(List<Integer> xs) { xs.collect { it * it } }\n" +
                        "    int sum(List<Integer> xs) { int s = 0; xs.each { Integer x -> s += x }; s }\n" +
                        "}\n";
                Class<?> c = loader.parseClass(source, "Fix" + i + ".groovy");
                fixtures[i] = (PackWorkload) c.getDeclaredConstructor().newInstance();
            }
            // vacuity guard: the flag must actually gate the emission
            boolean closureClasses = false;
            for (Class<?> c : loader.getLoadedClasses()) {
                if (c.getName().contains("_closure")) closureClasses = true;
            }
            if (Boolean.parseBoolean(pack) == closureClasses) {
                throw new IllegalStateException("closure.pack=" + pack + " but closure classes "
                        + (closureClasses ? "were" : "were not") + " generated");
            }
        }
        xs = new ArrayList<>();
        for (int i = 1; i <= 12; i += 1) xs.add(i);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        // Restore the global flag. With forking on (the default here) each trial owns its JVM,
        // so this only matters for unforked debug runs (-f 0), where a leaked =true would make
        // any later in-JVM Groovy compilation silently pack its closures.
        System.clearProperty("groovy.target.closure.pack");
    }

    private int next() {
        idx = (idx + 1) & (CLASSES - 1);
        return idx;
    }

    /**
     * Non-capturing {@code collect} body, one fixture class (monomorphic best case).
     * @return the collected list
     */
    @Benchmark
    public Object squared_mono() {
        return fixtures[0].squared(xs);
    }

    /**
     * Non-capturing {@code collect} body over rotating fixture classes (megamorphic).
     * @return the collected list
     */
    @Benchmark
    public Object squared_mega() {
        return fixtures[next()].squared(xs);
    }

    /**
     * Written-capture {@code each} accumulator, one fixture class (monomorphic best case).
     * @return the accumulated sum
     */
    @Benchmark
    public int sum_mono() {
        return fixtures[0].sum(xs);
    }

    /**
     * Written-capture {@code each} accumulator over rotating fixture classes (megamorphic).
     * @return the accumulated sum
     */
    @Benchmark
    public int sum_mega() {
        return fixtures[next()].sum(xs);
    }
}
