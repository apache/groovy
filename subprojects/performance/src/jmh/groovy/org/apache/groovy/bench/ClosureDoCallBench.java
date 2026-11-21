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
import org.codehaus.groovy.control.CompilerConfiguration;
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
 * Dynamic call-site invocation of a packed closure ({@code c(x)} in dynamically compiled code)
 * versus a generated closure class. Unlike {@link ClosurePackBench}, whose {@code collect}/
 * {@code each} bodies enter through {@code Closure.call(Object)}, a dynamic {@code c(x)} call
 * site selects the closure's arity-true {@code doCall} through the metaclass, so these rows
 * exercise the fixed-arity family's {@code doCall} entry points ({@code Fixed1}, {@code FixedIt},
 * {@code Fixed2}); the {@code each} row is the {@code call(Object)} lane as a control.
 * <p>
 * The fixtures are dynamic Groovy with no free names in the closure bodies, so under
 * {@code groovy.target.closure.pack} they pack on the syntactic path. Bodies return their
 * argument, so the closure itself allocates nothing: under {@code -prof gc} the per-operation
 * allocation isolates the dispatch path's own allocations (plus a constant iterator per loop).
 * {@code mono} invokes one fixture class; {@code mega} rotates over {@link #CLASSES} of them,
 * making the shared adapter's call sites megamorphic.
 */
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ClosureDoCallBench {

    /** Implemented by every compiled fixture class, so invocation needs no reflection. */
    public interface DoCallWorkload {
        Object one(List<Integer> xs);
        Object implicitIt(List<Integer> xs);
        Object two(List<Integer> xs);
        Object each(List<Integer> xs);
    }

    private static final int CLASSES = 16; // power of two: mega rotation uses a mask

    /** Compile-time flag: {@code true} packs eligible closures, {@code false} = closure classes. */
    @Param({"false", "true"})
    public String pack;

    private final DoCallWorkload[] fixtures = new DoCallWorkload[CLASSES];
    private List<Integer> xs;
    private int idx;
    private String priorPackFlag;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        priorPackFlag = System.getProperty(CompilerConfiguration.CLOSURE_PACKING);
        System.setProperty(CompilerConfiguration.CLOSURE_PACKING, pack);
        try (GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())) {
            for (int i = 0; i < CLASSES; i += 1) {
                String source =
                        "class Fix" + i + " implements org.apache.groovy.bench.ClosureDoCallBench.DoCallWorkload {\n" +
                        "    Object one(List<Integer> xs)        { def c = { x -> x };    def r = null; for (v in xs) r = c(v); r }\n" +
                        "    Object implicitIt(List<Integer> xs) { def c = { it };        def r = null; for (v in xs) r = c(v); r }\n" +
                        "    Object two(List<Integer> xs)        { def c = { a, b -> a }; def r = null; for (v in xs) r = c(v, v); r }\n" +
                        "    Object each(List<Integer> xs)       { xs.each { x -> x } }\n" +
                        "}\n";
                Class<?> c = loader.parseClass(source, "Fix" + i + ".groovy");
                fixtures[i] = (DoCallWorkload) c.getDeclaredConstructor().newInstance();
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
        if (priorPackFlag != null) {
            System.setProperty(CompilerConfiguration.CLOSURE_PACKING, priorPackFlag);
        } else {
            System.clearProperty(CompilerConfiguration.CLOSURE_PACKING);
        }
    }

    private int next() {
        idx = (idx + 1) & (CLASSES - 1);
        return idx;
    }

    /** @return the last result of a one-parameter closure called through a dynamic call site */
    @Benchmark
    public Object one_mono() {
        return fixtures[0].one(xs);
    }

    /** @return as {@link #one_mono}, over rotating fixture classes */
    @Benchmark
    public Object one_mega() {
        return fixtures[next()].one(xs);
    }

    /** @return the last result of an implicit-{@code it} closure called through a dynamic call site */
    @Benchmark
    public Object it_mono() {
        return fixtures[0].implicitIt(xs);
    }

    /** @return the last result of a two-parameter closure called through a dynamic call site */
    @Benchmark
    public Object two_mono() {
        return fixtures[0].two(xs);
    }

    /** @return the list, after a one-parameter closure driven by {@code each} (control lane) */
    @Benchmark
    public Object each_mono() {
        return fixtures[0].each(xs);
    }
}
