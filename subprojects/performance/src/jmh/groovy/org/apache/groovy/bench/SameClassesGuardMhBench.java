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

import org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures;
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.TimeUnit;

/**
 * Isolated MethodHandle combinator cost of GROOVY-12284.
 * <p>
 * Reconstructs the two guard shapes {@link org.codehaus.groovy.vmplugin.v8.Selector}
 * actually installs, then invokes them with {@code invokeExact} so MOP
 * selection, method bodies, and Groovy call-site infrastructure are not in
 * the picture:
 * <ul>
 *   <li><b>collector</b> — {@code sameClasses(Class[], Object[])} bound to
 *       the expected classes and adapted with
 *       {@code asCollector(Object[].class, n)}, the pre-GROOVY-12284 shape
 *       for every arity;</li>
 *   <li><b>specialised</b> — {@code sameClass}/{@code sameClasses} overloads
 *       with expected classes bound by a single
 *       {@code MethodHandles.insertArguments}, the production GROOVY-12284
 *       shape for arities 1–4.</li>
 * </ul>
 * Compiles against any Groovy that has the public {@code sameClasses}
 * overloads; {@code @Setup} fails with {@code NoSuchMethodException} on a
 * tree that predates GROOVY-12284. Compare collector vs specialised on the
 * <em>same</em> JVM (intra-HEAD), then confirm the end-to-end effect with
 * {@link SameClassesGuardBench} against the parent commit.
 * <p>
 * {@code specialised_bindTo_arity4} is the previous chained-{@code bindTo}
 * combinator, kept so a regression back to four adapters is visible.
 * <p>
 * Arity 1 / 2 / 4 cover receiver-only, the common {@code recv.foo(x)} shape,
 * and the largest specialised shape. There is no specialised arity-5
 * overload; that path stays on the collector in production.
 *
 * @see <a href="https://issues.apache.org/jira/browse/GROOVY-12284">GROOVY-12284</a>
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class SameClassesGuardMhBench {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    Object a;
    Object b;
    Object c;
    Object d;

    MethodHandle collector1;
    MethodHandle specialised1;
    MethodHandle collector2;
    MethodHandle specialised2;
    MethodHandle collector4;
    MethodHandle specialised4;
    MethodHandle specialisedBindTo4;

    /**
     * Builds collector and specialised handles that match {@code Selector.sameClassesGuard}
     * for arities 1, 2 and 4, using the public {@link IndyGuardsFiltersAndSignatures}
     * methods the runtime binds.
     *
     * @throws ReflectiveOperationException if a handle cannot be resolved
     */
    @Setup(Level.Trial)
    public void setup() throws ReflectiveOperationException {
        a = new Object();
        b = new Object();
        c = new Object();
        d = new Object();

        MethodHandle arraySame = LOOKUP.findStatic(
                IndyGuardsFiltersAndSignatures.class,
                "sameClasses",
                MethodType.methodType(boolean.class, Class[].class, Object[].class));
        MethodHandle sameClass = LOOKUP.findStatic(
                IndyGuardsFiltersAndSignatures.class,
                "sameClass",
                MethodType.methodType(boolean.class, Class.class, Object.class));
        MethodHandle same2 = LOOKUP.findStatic(
                IndyGuardsFiltersAndSignatures.class,
                "sameClasses",
                MethodType.methodType(boolean.class, Class.class, Class.class, Object.class, Object.class));
        MethodHandle same4 = LOOKUP.findStatic(
                IndyGuardsFiltersAndSignatures.class,
                "sameClasses",
                MethodType.methodType(boolean.class,
                        Class.class, Class.class, Class.class, Class.class,
                        Object.class, Object.class, Object.class, Object.class));

        Class<?> ca = a.getClass();
        Class<?> cb = b.getClass();
        Class<?> cc = c.getClass();
        Class<?> cd = d.getClass();

        MethodType t1 = MethodType.methodType(boolean.class, Object.class);
        MethodType t2 = MethodType.methodType(boolean.class, Object.class, Object.class);
        MethodType t4 = MethodType.methodType(boolean.class, Object.class, Object.class, Object.class, Object.class);

        collector1 = arraySame.bindTo(new Class<?>[]{ca}).asCollector(Object[].class, 1).asType(t1);
        specialised1 = MethodHandles.insertArguments(sameClass, 0, ca).asType(t1);

        collector2 = arraySame.bindTo(new Class<?>[]{ca, cb}).asCollector(Object[].class, 2).asType(t2);
        specialised2 = MethodHandles.insertArguments(same2, 0, ca, cb).asType(t2);

        collector4 = arraySame.bindTo(new Class<?>[]{ca, cb, cc, cd}).asCollector(Object[].class, 4).asType(t4);
        specialised4 = MethodHandles.insertArguments(same4, 0, ca, cb, cc, cd).asType(t4);
        specialisedBindTo4 = same4.bindTo(ca).bindTo(cb).bindTo(cc).bindTo(cd).asType(t4);
    }

    /** Pre-GROOVY-12284 arity-1 guard: collector of a one-element array. */
    @Benchmark
    public boolean collector_arity1() throws Throwable {
        return (boolean) collector1.invokeExact(a);
    }

    /** Production arity-1 guard: {@code insertArguments} of the receiver class. */
    @Benchmark
    public boolean specialised_arity1() throws Throwable {
        return (boolean) specialised1.invokeExact(a);
    }

    /** Pre-GROOVY-12284 arity-2 guard. */
    @Benchmark
    public boolean collector_arity2() throws Throwable {
        return (boolean) collector2.invokeExact(a, b);
    }

    /** Production arity-2 guard: one {@code insertArguments}. */
    @Benchmark
    public boolean specialised_arity2() throws Throwable {
        return (boolean) specialised2.invokeExact(a, b);
    }

    /** Pre-GROOVY-12284 arity-4 guard. */
    @Benchmark
    public boolean collector_arity4() throws Throwable {
        return (boolean) collector4.invokeExact(a, b, c, d);
    }

    /** Production arity-4 guard: one {@code insertArguments}. */
    @Benchmark
    public boolean specialised_arity4() throws Throwable {
        return (boolean) specialised4.invokeExact(a, b, c, d);
    }

    /** Pre-review arity-4 guard: four chained {@code bindTo} adapters. */
    @Benchmark
    public boolean specialised_bindTo_arity4() throws Throwable {
        return (boolean) specialisedBindTo4.invokeExact(a, b, c, d);
    }
}
