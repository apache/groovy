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
package org.apache.groovy.bench

import groovy.transform.CompileStatic
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * End-to-end cost of the invokedynamic same-class guard (GROOVY-12284).
 * <p>
 * Linked monomorphic sites with all-non-null arguments install a same-class
 * guard. Before GROOVY-12284 that guard was always
 * {@code SAME_CLASSES.bindTo(classes).asCollector(Object[].class, n)}, which
 * allocates a fresh {@code Object[]} on every invocation. After the change,
 * arities 1–4 (receiver plus 0–3 parameters) bind specialised handles and
 * arity 5+ keeps the collector.
 * <p>
 * Each {@code @Benchmark} method is a single call so {@code gc.alloc.rate.norm}
 * (run with {@code -PjmhProfilers=gc}) is bytes per invocation, not per inner
 * loop. Arguments are pre-allocated heap objects: boxing must not be part of
 * the signal. Receivers and arguments are non-final Groovy types; dynamic
 * indy sites have {@code Object} parameter types, so the same-class guard is
 * installed on every {@code dynamic_*} row.
 * <p>
 * How to read the rows:
 * <ul>
 *   <li>{@code dynamic_arity1}–{@code dynamic_arity4} — treatment: specialised
 *       on HEAD, collector on the parent. Expect HEAD faster and/or fewer
 *       bytes/op.</li>
 *   <li>{@code dynamic_arity5} — negative control: collector on both sides.
 *       Expect parity. A HEAD win here is not GROOVY-12284.</li>
 *   <li>{@code cs_arity2} / {@code cs_arity5} — negative control:
 *       {@code @CompileStatic} emits {@code invokevirtual}, no MOP
 *       same-class guard. Expect parity at both arities.</li>
 * </ul>
 * Pair with {@link SameClassesGuardMhBench} for an isolated MethodHandle
 * combinator comparison (collector vs specialised) that does not go through
 * MOP selection.
 *
 * @see <a href="https://issues.apache.org/jira/browse/GROOVY-12284">GROOVY-12284</a>
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class SameClassesGuardBench {

    /**
     * Receiver whose methods are selected through the MOP on the dynamic rows.
     * Bodies are constant so remaining cost is dispatch plus the same-class
     * guard, not arithmetic.
     */
    static class Target {
        int zero() { 1 }
        int one(Object a) { 1 }
        int two(Object a, Object b) { 1 }
        int three(Object a, Object b, Object c) { 1 }
        int four(Object a, Object b, Object c, Object d) { 1 }
    }

    /** Non-final argument type so a class-identity guard is meaningful. */
    static class Arg {
        int n = 1
    }

    Target target
    Arg a
    Arg b
    Arg c
    Arg d

    /**
     * Allocates one receiver and four arguments for the trial. Fresh instances
     * per trial keep the linked site monomorphic without MetaClass churn.
     */
    @Setup(Level.Trial)
    void setup() {
        target = new Target()
        a = new Arg()
        b = new Arg()
        c = new Arg()
        d = new Arg()
    }

    /** Receiver-only dynamic call: arity 1, specialised on HEAD. */
    @Benchmark
    void dynamic_arity1(Blackhole bh) {
        bh.consume(target.zero())
    }

    /** Receiver plus one argument: arity 2, specialised on HEAD. */
    @Benchmark
    void dynamic_arity2(Blackhole bh) {
        bh.consume(target.one(a))
    }

    /** Receiver plus two arguments: arity 3, specialised on HEAD. */
    @Benchmark
    void dynamic_arity3(Blackhole bh) {
        bh.consume(target.two(a, b))
    }

    /** Receiver plus three arguments: arity 4, specialised on HEAD. */
    @Benchmark
    void dynamic_arity4(Blackhole bh) {
        bh.consume(target.three(a, b, c))
    }

    /** Receiver plus four arguments: arity 5, collector on both sides. */
    @Benchmark
    void dynamic_arity5(Blackhole bh) {
        bh.consume(target.four(a, b, c, d))
    }

    /**
     * {@code @CompileStatic} arity 2: {@code invokevirtual}, no same-class guard.
     */
    @CompileStatic
    @Benchmark
    void cs_arity2(Blackhole bh) {
        bh.consume(target.one(a))
    }

    /**
     * {@code @CompileStatic} arity 5: {@code invokevirtual}, no same-class guard.
     */
    @CompileStatic
    @Benchmark
    void cs_arity5(Blackhole bh) {
        bh.consume(target.four(a, b, c, d))
    }
}
