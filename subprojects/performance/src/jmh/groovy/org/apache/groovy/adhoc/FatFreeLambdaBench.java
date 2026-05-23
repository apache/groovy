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
package org.apache.groovy.adhoc;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks comparing the shipped "fat-free" idioms (GROOVY-12043 /
 * GROOVY-12044 / GROOVY-12054) against the existing closure / {@code rcurry}
 * idioms for predicate-style iteration.
 * <p>
 * Variants (see {@link FatFreeLambda}):
 * <ul>
 *   <li><b>A</b> {@code data.find { it.startsWith(prefix) }} — capturing closure literal</li>
 *   <li><b>B</b> {@code data.find(pred.rcurry(prefix))} — closure + rcurry wrap (GROOVY-12044 fast path)</li>
 *   <li><b>C</b> {@code data.find((s,p) -> s.startsWith(p), prefix)} — real DGM BiPredicate+param overload + indy lambda</li>
 *   <li><b>D</b> {@code data.find(String::startsWith, prefix)} — real DGM BiPredicate+param overload + method ref</li>
 *   <li><b>E</b> hand-written {@code for} loop — baseline</li>
 *   <li><b>F</b> hoisted-singleton closure {@code + rcurry} — isolates the CurriedClosure cost</li>
 *   <li><b>G</b> {@code data.find(Lambdas.curryWith(STARTS_WITH, prefix))} — plain-functional curryWith (one small capturing lambda)</li>
 *   <li><b>H</b> {@code data.find(Closures.curryWith(STARTS_WITH, prefix))} — Closure-typed curryWith (pre-curried PredicateClosure)</li>
 * </ul>
 * <p>
 * The same A&ndash;H variants are applied across three DGM shapes, each carrying
 * the full overload set ({@code Predicate} and {@code BiPredicate}+param):
 * {@code find} (short-circuit element), {@code findAll} (builds a result list),
 * and {@code count} (a reducer that builds no collection — its {@code Predicate}
 * / {@code BiPredicate}+param overloads landed in the GROOVY-12054 follow-up).
 * <p>
 * All variants perform a full traversal (prefixes never match) and identical
 * per-element work; only the closure / lambda machinery differs.  Prefix
 * values rotate through a pre-built array so capture sites must keep
 * allocating, but the rotation cost is constant across variants.
 * <p>
 * To collect allocation stats, add {@code profilers = ['gc']} to the
 * {@code jmh{}} block in {@code build-logic/src/main/groovy/org.apache.groovy-performance.gradle}
 * before running.  Run with:
 * <pre>
 *   ./gradlew :perf:jmh -PbenchInclude=FatFreeLambda
 * </pre>
 * Or repeat with {@code -Pindy=false} to compare with classic call sites.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class FatFreeLambdaBench {

    /** Workload size — how many elements the predicate is applied to per call. */
    @Param({"100", "1000", "10000"})
    private int size;

    private List<String> data;
    private String[] prefixes;
    private int idx;

    @Setup(Level.Trial)
    public void setup() {
        data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            data.add("word_" + i);
        }
        // Prefixes that never match any element — forces full traversal in every variant.
        // Eight values so the rotate index is a cheap mask-and-increment.
        prefixes = new String[]{"z_4", "z_70", "z_300", "z_99", "z_500", "z_7", "z_888", "z_1"};
    }

    private String nextPrefix() {
        return prefixes[(idx++) & 7];
    }

    // ----- find (short-circuit; here always traverses fully because no match) -----

    @Benchmark public String findA_captureClosure()      { return FatFreeLambda.findCaptureClosure(data, nextPrefix()); }
    @Benchmark public String findB_rcurryClosure()        { return FatFreeLambda.findRcurryClosure(data, nextPrefix()); }
    @Benchmark public String findC_biPredicateParam()     { return FatFreeLambda.findBiPredicateParam(data, nextPrefix()); }
    @Benchmark public String findD_methodRefParam()       { return FatFreeLambda.findMethodRefParam(data, nextPrefix()); }
    @Benchmark public String findE_baseline()             { return FatFreeLambda.findBaseline(data, nextPrefix()); }
    @Benchmark public String findF_sharedRcurry()         { return FatFreeLambda.findSharedRcurry(data, nextPrefix()); }
    @Benchmark public String findG_lambdasCurryWith()     { return FatFreeLambda.findLambdasCurryWith(data, nextPrefix()); }
    @Benchmark public String findH_closuresCurryWith()    { return FatFreeLambda.findClosuresCurryWith(data, nextPrefix()); }

    // ----- findAll (full traversal — amplifies per-element dispatch differences) ---

    @Benchmark public List<String> findAllA_captureClosure()   { return FatFreeLambda.findAllCaptureClosure(data, nextPrefix()); }
    @Benchmark public List<String> findAllB_rcurryClosure()    { return FatFreeLambda.findAllRcurryClosure(data, nextPrefix()); }
    @Benchmark public List<String> findAllC_biPredicateParam() { return FatFreeLambda.findAllBiPredicateParam(data, nextPrefix()); }
    @Benchmark public List<String> findAllD_methodRefParam()   { return FatFreeLambda.findAllMethodRefParam(data, nextPrefix()); }
    @Benchmark public List<String> findAllE_baseline()         { return FatFreeLambda.findAllBaseline(data, nextPrefix()); }
    @Benchmark public List<String> findAllF_sharedRcurry()     { return FatFreeLambda.findAllSharedRcurry(data, nextPrefix()); }
    @Benchmark public List<String> findAllG_lambdasCurryWith() { return FatFreeLambda.findAllLambdasCurryWith(data, nextPrefix()); }
    @Benchmark public List<String> findAllH_closuresCurryWith(){ return FatFreeLambda.findAllClosuresCurryWith(data, nextPrefix()); }

    // ----- count (full traversal reducer; builds no result collection) ------------

    @Benchmark public Number countA_captureClosure()    { return FatFreeLambda.countCaptureClosure(data, nextPrefix()); }
    @Benchmark public Number countB_rcurryClosure()     { return FatFreeLambda.countRcurryClosure(data, nextPrefix()); }
    @Benchmark public Number countC_biPredicateParam()  { return FatFreeLambda.countBiPredicateParam(data, nextPrefix()); }
    @Benchmark public Number countD_methodRefParam()    { return FatFreeLambda.countMethodRefParam(data, nextPrefix()); }
    @Benchmark public Number countE_baseline()          { return FatFreeLambda.countBaseline(data, nextPrefix()); }
    @Benchmark public Number countF_sharedRcurry()      { return FatFreeLambda.countSharedRcurry(data, nextPrefix()); }
    @Benchmark public Number countG_lambdasCurryWith()  { return FatFreeLambda.countLambdasCurryWith(data, nextPrefix()); }
    @Benchmark public Number countH_closuresCurryWith() { return FatFreeLambda.countClosuresCurryWith(data, nextPrefix()); }
}
