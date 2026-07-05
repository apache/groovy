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

/**
 * Dynamic-dispatch fixture for {@link DynamicDispatchColdBench}. All methods
 * except the {@code CS} variant are deliberately dynamic so each call site
 * goes through the full indy (or classic call-site) machinery from a cold
 * start: bootstrap, method selection, guard construction, and — depending on
 * {@code n} — promotion past the hit-count threshold.
 */
class DynamicDispatchCold {

    static class Alpha {
        int work(int i) { i + 1 }
        int getVal() { 1 }
    }

    static class Beta extends Alpha {
        int work(int i) { i + 2 }
        int getVal() { 2 }
    }

    static class Gamma extends Alpha {
        int work(int i) { i + 3 }
        int getVal() { 3 }
    }

    /** Monomorphic dynamic instance calls: one receiver type at the call site. */
    int monoSum(int n) {
        def r = new Alpha()
        int s = 0
        for (int i = 0; i < n; i++) {
            s += r.work(i)
        }
        s
    }

    /** Polymorphic dynamic instance calls: three receiver types rotate at one call site. */
    int polySum(int n) {
        def a = new Alpha()
        def b = new Beta()
        def c = new Gamma()
        int s = 0
        for (int i = 0; i < n; i++) {
            def r = (i % 3 == 0) ? a : ((i % 3 == 1) ? b : c)
            s += r.work(i)
        }
        s
    }

    /** Dynamic property reads through the MOP. */
    int propertySum(int n) {
        def r = new Beta()
        int s = 0
        for (int i = 0; i < n; i++) {
            s += r.val
        }
        s
    }

    /** Dynamic closure invocation. */
    int closureSum(int n) {
        def f = { int i -> i + 1 }
        int s = 0
        for (int i = 0; i < n; i++) {
            s += f(i)
        }
        s
    }

    /** Statically-compiled lower bound for {@link #monoSum}. */
    @CompileStatic
    int monoSumCS(int n) {
        Alpha r = new Alpha()
        int s = 0
        for (int i = 0; i < n; i++) {
            s += r.work(i)
        }
        s
    }
}
