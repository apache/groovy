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

import groovy.lang.GroovySystem

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
 * Steady-state cost of scoped invokedynamic SwitchPoint invalidation (GROOVY-12191).
 * <p>
 * Complements the Grails-oriented {@code CallSiteInvalidationBench} (AverageTime,
 * grails suite) with a bench-suite Throughput regression set for the single
 * per-class SwitchPoint domain. Baseline / cross-type / same-type rows
 * intentionally overlap that bench so ratios are available under the same
 * {@code .bench.} include pattern; unique coverage here is category enter/leave
 * (bulk class-domain invalidation), parent-vs-child isolation (stock exact-class
 * — no hierarchy fan-out), and setup-isolated post-burst steady state:
 * <ul>
 *   <li><b>Per-class</b> — MetaClass changes on an unrelated type must not
 *       deoptimize a hot monomorphic site;</li>
 *   <li><b>Same-type</b> — MetaClass changes on the hot type must re-link;</li>
 *   <li><b>Category</b> — {@code use(Category)} enter/leave bulk-invalidates
 *       class SwitchPoints so category methods become visible;</li>
 *   <li><b>Parent churn</b> — parent MetaClass changes must <em>not</em>
 *       deoptimize subclass-linked sites (stock exact-class policy).</li>
 * </ul>
 * Compare {@link #hotLoop_afterUnrelatedBurst} / {@link #hotLoop_unrelatedMetaClassChurn}
 * against {@link #hotLoop_baseline} (should stay close after scoping) and against
 * {@link #hotLoop_sameTypeMetaClassChurn} (must pay re-link cost).
 *
 * @see <a href="https://issues.apache.org/jira/browse/GROOVY-12191">GROOVY-12191</a>
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class ScopedInvalidationBench {

    /** Iterations of the hot call loop per benchmark invocation. */
    static final int ITERATIONS = 100_000

    /** How often to perform a MetaClass mutation inside the churn loops. */
    static final int CHURN_EVERY = 1_000

    /**
     * Hot monomorphic call-site receiver.
     */
    static class Hot {
        int value = 42
        int compute() { value * 2 }
    }

    /**
     * Unrelated type used only for cross-type MetaClass churn.
     */
    static class Unrelated {
        String label = 'cold'
    }

    /**
     * Parent of {@link HierChild} for parent-vs-child isolation churn.
     */
    static class HierParent {
        int base() { 1 }
    }

    /**
     * Subclass receiver whose SwitchPoint must stay live when only the parent
     * MetaClass changes (stock exact-class policy).
     */
    static class HierChild extends HierParent {
        int leaf() { 2 }
    }

    /**
     * Category that decorates {@link Hot#compute()}.
     */
    static class HotCategory {
        static int compute(Hot self) { self.value * 3 }
    }

    Hot hot
    HierChild child

    /**
     * Resets MetaClasses and allocates fresh receivers each iteration so
     * SwitchPoint state from a previous sample does not bleed into the next.
     */
    @Setup(Level.Iteration)
    void setup() {
        GroovySystem.metaClassRegistry.removeMetaClass(Hot)
        GroovySystem.metaClassRegistry.removeMetaClass(Unrelated)
        GroovySystem.metaClassRegistry.removeMetaClass(HierParent)
        GroovySystem.metaClassRegistry.removeMetaClass(HierChild)
        hot = new Hot()
        child = new HierChild()
    }

    /**
     * Steady-state monomorphic calls with no MetaClass mutation.
     * @param bh blackhole for the accumulated result
     */
    @Benchmark
    void hotLoop_baseline(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hot.compute()
        }
        bh.consume(sum)
    }

    /**
     * Hot monomorphic loop with MetaClass changes on an <em>unrelated</em> type.
     * With scoped invalidation the hot site should keep its linked target;
     * residual cost vs baseline is mostly MetaClass mutation work, not deopt.
     * Prefer {@link #hotLoop_afterUnrelatedBurst} for a pure no-deopt check.
     * @param bh blackhole for the accumulated result
     */
    @Benchmark
    void hotLoop_unrelatedMetaClassChurn(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hot.compute()
            if (i % CHURN_EVERY == 0) {
                Unrelated.metaClass."dyn${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /**
     * Pure hot loop after a burst of unrelated MetaClass changes performed in
     * {@link AfterUnrelatedBurstState#setup()}. After scoping (GROOVY-12191),
     * this should approach {@link #hotLoop_baseline}.
     * @param state state that applied the unrelated MetaClass burst
     * @param bh blackhole for the accumulated result
     */
    @Benchmark
    void hotLoop_afterUnrelatedBurst(AfterUnrelatedBurstState state, Blackhole bh) {
        int sum = 0
        Hot h = state.hot
        for (int i = 0; i < ITERATIONS; i++) {
            sum += h.compute()
        }
        bh.consume(sum)
    }

    /**
     * Hot monomorphic loop with MetaClass changes on the <em>same</em> type —
     * each change must retire the class-domain SwitchPoint and re-link.
     * @param bh blackhole for the accumulated result
     */
    @Benchmark
    void hotLoop_sameTypeMetaClassChurn(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += hot.compute()
            if (i % CHURN_EVERY == 0) {
                Hot.metaClass."dyn${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /**
     * Category enter/leave interleaved with hot calls — exercises
     * {@code invalidateCategory()} bulk retirement of loaded class domains.
     * Uses a modest number of {@code use} blocks so category re-link cost
     * is visible without dominating wall-clock.
     * @param bh blackhole for the accumulated result
     */
    @Benchmark
    void hotLoop_categoryEnterLeave(Blackhole bh) {
        int sum = 0
        int callsPerUse = ITERATIONS / 10
        for (int b = 0; b < 10; b++) {
            use(HotCategory) {
                for (int i = 0; i < callsPerUse; i++) {
                    sum += hot.compute()
                }
            }
        }
        bh.consume(sum)
    }

    /**
     * Subclass receiver calls with periodic parent MetaClass changes. Under
     * stock exact-class policy this should stay close to
     * {@link #parentChild_baseline} (parent churn must not deopt the child).
     * @param bh blackhole for the accumulated result
     */
    @Benchmark
    void parentChild_parentMetaClassChurn(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += child.leaf()
            sum += child.base()
            if (i % CHURN_EVERY == 0) {
                HierParent.metaClass."dyn${i % 5}" = { -> i }
            }
        }
        bh.consume(sum)
    }

    /**
     * Subclass receiver calls with no MetaClass mutation (parent/child baseline).
     * @param bh blackhole for the accumulated result
     */
    @Benchmark
    void parentChild_baseline(Blackhole bh) {
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            sum += child.leaf()
            sum += child.base()
        }
        bh.consume(sum)
    }

    /**
     * Iteration state for {@link ScopedInvalidationBench#hotLoop_afterUnrelatedBurst}:
     * resets MetaClasses, then applies a burst of unrelated MetaClass mutations
     * <em>outside</em> the measured loop so residual deopt (if any) is isolated.
     */
    @State(Scope.Thread)
    static class AfterUnrelatedBurstState {
        /** Hot receiver used by the measured loop. */
        Hot hot

        /**
         * Resets MetaClasses, allocates a fresh hot receiver, and applies
         * unrelated MetaClass churn before the benchmark body runs.
         */
        @Setup(Level.Iteration)
        void setup() {
            GroovySystem.metaClassRegistry.removeMetaClass(Hot)
            GroovySystem.metaClassRegistry.removeMetaClass(Unrelated)
            hot = new Hot()
            for (int i = 0; i < 50; i++) {
                Unrelated.metaClass."startup${i % 10}" = { -> i }
            }
        }
    }
}
