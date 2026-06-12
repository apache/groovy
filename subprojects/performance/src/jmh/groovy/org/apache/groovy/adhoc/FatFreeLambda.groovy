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
package org.apache.groovy.adhoc

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.groovy.util.Closures
import org.apache.groovy.util.Lambdas

import java.util.function.BiPredicate

/**
 * Helper class for benchmarking the "fat-free lambda" idioms that landed in
 * GROOVY-12043 / GROOVY-12044 / GROOVY-12054 against the existing closure /
 * {@code rcurry} idioms (see donraab.medium.com/fat-free-lambdas-in-java).
 * <p>
 * Unlike the original spike (which used hand-rolled {@code anyWith}/{@code curryWith}
 * stand-ins), every variant here calls the <em>shipped</em> APIs:
 * <ul>
 *   <li>the {@code java.util.function}-typed DGM overloads from
 *       <b>GROOVY-12054</b> — {@code find(Predicate)}, {@code find(BiPredicate, param)},
 *       {@code findAll(Predicate)}, {@code findAll(BiPredicate, param)};</li>
 *   <li>{@link org.apache.groovy.util.Lambdas#curryWith(BiPredicate, Object) Lambdas.curryWith}
 *       (plain {@link java.util.function.Predicate}) and
 *       {@link org.apache.groovy.util.Closures#curryWith(BiPredicate, Object) Closures.curryWith}
 *       (a {@code Closure} that also implements {@code Predicate}) from <b>GROOVY-12043</b>;</li>
 *   <li>the {@code rcurry} variants exercise the {@code CurriedClosure.call(Object)}
 *       fast path added in <b>GROOVY-12044</b>.</li>
 * </ul>
 * <p>
 * The two families mirror the original {@code any}/{@code count} pair but use the
 * real DGM methods that carry the full overload set: {@code find} (short-circuit)
 * and {@code findAll} (always full traversal). Because the rotating prefixes never
 * match any element, both families perform a full traversal in every variant —
 * {@code find} returns {@code null} and {@code findAll} returns an empty list — so
 * the result-collection cost is constant and only the closure / lambda machinery
 * differs.
 * <p>
 * Under {@code @CompileStatic}, the stateless BiPredicate lambdas and method
 * references (C/D) are emitted as static methods and shared as singletons by
 * {@code LambdaMetafactory} (the GROOVY-11905 optimisation), giving zero
 * per-call lambda allocation.
 */
@CompileStatic
class FatFreeLambda {

    /** Singleton BiPredicate (method ref hoisted via indy) used by the curryWith variants. */
    private static final BiPredicate<String, String> STARTS_WITH = String::startsWith

    /** F: hoisted-singleton closure + rcurry — upper bound for what a fat-free Closure could give. */
    private static final Closure<Boolean> SHARED_BI_PRED =
            { String s, String p -> s.startsWith(p) } as Closure<Boolean>

    // ===== find family (short-circuit; here always traverses fully — no match) =====
    // The closure variants are written in @CompileDynamic to reflect the typical
    // call-site idiom; static-compiling them does not change the closure allocation
    // pattern because the classic DGM.find takes a Closure, not a functional interface.

    /** A: capturing closure literal — the classic Groovy idiom. */
    @CompileDynamic
    static String findCaptureClosure(List<String> data, String prefix) {
        data.find { it.startsWith(prefix) }
    }

    /** B: closure literal + rcurry — Groovy's existing "don't capture" idiom (GROOVY-12044 fast path). */
    @CompileDynamic
    static String findRcurryClosure(List<String> data, String prefix) {
        Closure pred = { String s, String p -> s.startsWith(p) }
        data.find(pred.rcurry(prefix))
    }

    /** C: real DGM find(BiPredicate, param) + stateless BiPredicate lambda (singleton via indy). */
    static String findBiPredicateParam(List<String> data, String prefix) {
        BiPredicate<String, String> p = (String s, String x) -> s.startsWith(x)
        data.find(p, prefix)
    }

    /** D: real DGM find(BiPredicate, param) + unbound method reference (singleton via indy). */
    static String findMethodRefParam(List<String> data, String prefix) {
        BiPredicate<String, String> ref = String::startsWith
        data.find(ref, prefix)
    }

    /** E: baseline plain for-loop — lower bound. */
    static String findBaseline(List<String> data, String prefix) {
        for (String s : data) {
            if (s.startsWith(prefix)) return s
        }
        return null
    }

    /** F: hoisted-singleton closure + rcurry — isolates the CurriedClosure cost (GROOVY-12044). */
    @CompileDynamic
    static String findSharedRcurry(List<String> data, String prefix) {
        data.find(SHARED_BI_PRED.rcurry(prefix))
    }

    /** G: Lambdas.curryWith — one small capturing lambda per call, fed to find(Predicate). */
    static String findLambdasCurryWith(List<String> data, String prefix) {
        data.find(Lambdas.curryWith(STARTS_WITH, prefix))
    }

    /** H: Closures.curryWith — a pre-curried PredicateClosure (no CurriedClosure) via the Closure path. */
    static String findClosuresCurryWith(List<String> data, String prefix) {
        Closure<Boolean> c = Closures.curryWith(STARTS_WITH, prefix)
        data.find(c)
    }

    // ===== findAll family (full traversal — amplifies per-element dispatch cost) =====

    @CompileDynamic
    static List<String> findAllCaptureClosure(List<String> data, String prefix) {
        data.findAll { String s -> s.startsWith(prefix) }
    }

    @CompileDynamic
    static List<String> findAllRcurryClosure(List<String> data, String prefix) {
        Closure pred = { String s, String p -> s.startsWith(p) }
        data.findAll(pred.rcurry(prefix))
    }

    static List<String> findAllBiPredicateParam(List<String> data, String prefix) {
        BiPredicate<String, String> p = (String s, String x) -> s.startsWith(x)
        data.findAll(p, prefix)
    }

    static List<String> findAllMethodRefParam(List<String> data, String prefix) {
        BiPredicate<String, String> ref = String::startsWith
        data.findAll(ref, prefix)
    }

    static List<String> findAllBaseline(List<String> data, String prefix) {
        List<String> out = new ArrayList<>()
        for (String s : data) {
            if (s.startsWith(prefix)) out.add(s)
        }
        return out
    }

    @CompileDynamic
    static List<String> findAllSharedRcurry(List<String> data, String prefix) {
        data.findAll(SHARED_BI_PRED.rcurry(prefix))
    }

    static List<String> findAllLambdasCurryWith(List<String> data, String prefix) {
        data.findAll(Lambdas.curryWith(STARTS_WITH, prefix))
    }

    static List<String> findAllClosuresCurryWith(List<String> data, String prefix) {
        Closure<Boolean> c = Closures.curryWith(STARTS_WITH, prefix)
        data.findAll(c)
    }

    // ===== count family (full-traversal reducer; no result collection) =====
    // The real count(Predicate) / count(BiPredicate, param) overloads landed in
    // GROOVY-12054 (cont'd). Unlike findAll, count builds no result list, so its
    // only constant allocation is boxing the returned tally. Every variant returns
    // Number (boxing the int once) so that boxing cost is identical across them and
    // the only variable is the predicate / closure machinery.

    @CompileDynamic
    static Number countCaptureClosure(List<String> data, String prefix) {
        data.count { String s -> s.startsWith(prefix) }
    }

    @CompileDynamic
    static Number countRcurryClosure(List<String> data, String prefix) {
        Closure pred = { String s, String p -> s.startsWith(p) }
        data.count(pred.rcurry(prefix))
    }

    static Number countBiPredicateParam(List<String> data, String prefix) {
        BiPredicate<String, String> p = (String s, String x) -> s.startsWith(x)
        data.count(p, prefix)
    }

    static Number countMethodRefParam(List<String> data, String prefix) {
        BiPredicate<String, String> ref = String::startsWith
        data.count(ref, prefix)
    }

    static Number countBaseline(List<String> data, String prefix) {
        int n = 0
        for (String s : data) {
            if (s.startsWith(prefix)) n++
        }
        return n
    }

    @CompileDynamic
    static Number countSharedRcurry(List<String> data, String prefix) {
        data.count(SHARED_BI_PRED.rcurry(prefix))
    }

    static Number countLambdasCurryWith(List<String> data, String prefix) {
        data.count(Lambdas.curryWith(STARTS_WITH, prefix))
    }

    static Number countClosuresCurryWith(List<String> data, String prefix) {
        Closure<Boolean> c = Closures.curryWith(STARTS_WITH, prefix)
        data.count(c)
    }
}
