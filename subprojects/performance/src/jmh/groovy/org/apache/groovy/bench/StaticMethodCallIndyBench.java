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

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for the early {@code setTarget} optimisation on static method
 * call sites in the invokedynamic dispatch path.
 * <p>
 * When the receiver of an {@code invokedynamic} call is a {@code Class} object
 * and the resolved method is {@code static}, the call site target is set
 * immediately — bypassing the normal hit-count threshold — so that the JIT
 * compiler can inline the target method handle sooner.
 * <p>
 * This benchmark compares:
 * <ul>
 *   <li><b>Java</b> — direct {@code invokestatic}, the theoretical optimum</li>
 *   <li><b>Groovy dynamic</b> — {@code invokedynamic} with early setTarget</li>
 *   <li><b>Groovy {@code @CompileStatic}</b> — direct {@code invokestatic}</li>
 *   <li><b>Groovy instance</b> — {@code invokedynamic} without early setTarget (control group)</li>
 * </ul>
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class StaticMethodCallIndyBench {

    private static final int SUM_N = 1000;
    private static final int FIB_N = 25;
    private static final int CHAIN_ITERATIONS = 1000;

    private StaticMethodCallIndy instance;

    /**
     * Creates the helper instance used by the instance-method benchmark variants.
     */
    @Setup
    public void setUp() {
        instance = new StaticMethodCallIndy();
    }

    // ---- loop with static call: sum(0..n) via repeated add ----

    /**
     * Java baseline: static sum via repeated add.
     * @return the computed sum
     */
    @Benchmark
    public int staticSum_java() {
        return JavaStaticMethods.sum(SUM_N);
    }

    /**
     * Groovy dynamic: static sum via repeated add.
     * @return the computed sum
     */
    @Benchmark
    public int staticSum_groovy() {
        return StaticMethodCallIndy.staticSum(SUM_N);
    }

    /**
     * Groovy {@code @CompileStatic}: static sum via repeated add.
     * @return the computed sum
     */
    @Benchmark
    public int staticSum_groovyCS() {
        return StaticMethodCallIndy.staticSumCS(SUM_N);
    }

    /**
     * Groovy dynamic: instance sum via repeated add (control group).
     * @return the computed sum
     */
    @Benchmark
    public int instanceSum_groovy() {
        return instance.instanceSum(SUM_N);
    }

    // ---- recursive static call: Fibonacci ----

    /**
     * Java baseline: recursive Fibonacci.
     * @return the computed Fibonacci value
     */
    @Benchmark
    public int staticFib_java() {
        return JavaStaticMethods.fib(FIB_N);
    }

    /**
     * Groovy dynamic: recursive static Fibonacci.
     * @return the computed Fibonacci value
     */
    @Benchmark
    public int staticFib_groovy() {
        return StaticMethodCallIndy.staticFib(FIB_N);
    }

    /**
     * Groovy {@code @CompileStatic}: recursive static Fibonacci.
     * @return the computed Fibonacci value
     */
    @Benchmark
    public int staticFib_groovyCS() {
        return StaticMethodCallIndy.staticFibCS(FIB_N);
    }

    /**
     * Groovy dynamic: instance Fibonacci (control group).
     * @return the computed Fibonacci value
     */
    @Benchmark
    public int instanceFib_groovy() {
        return instance.instanceFib(FIB_N);
    }

    // ---- chained static calls ----

    /**
     * Java baseline: chained static calls.
     * @return the accumulated result
     */
    @Benchmark
    public int staticChain_java() {
        int result = 0;
        for (int i = 0; i < CHAIN_ITERATIONS; i++) {
            result += JavaStaticMethods.chain(i);
        }
        return result;
    }

    /**
     * Groovy dynamic: chained static calls.
     * @return the accumulated result
     */
    @Benchmark
    public int staticChain_groovy() {
        int result = 0;
        for (int i = 0; i < CHAIN_ITERATIONS; i++) {
            result += StaticMethodCallIndy.staticChain(i);
        }
        return result;
    }

    /**
     * Groovy {@code @CompileStatic}: chained static calls.
     * @return the accumulated result
     */
    @Benchmark
    public int staticChain_groovyCS() {
        int result = 0;
        for (int i = 0; i < CHAIN_ITERATIONS; i++) {
            result += StaticMethodCallIndy.staticChainCS(i);
        }
        return result;
    }

    /**
     * Groovy dynamic: chained instance calls (control group).
     * @return the accumulated result
     */
    @Benchmark
    public int instanceChain_groovy() {
        int result = 0;
        for (int i = 0; i < CHAIN_ITERATIONS; i++) {
            result += instance.instanceChain(i);
        }
        return result;
    }

    // ---- Java baseline implementations ----

    private static class JavaStaticMethods {
        static int add(int a, int b) {
            return a + b;
        }

        static int sum(int n) {
            int s = 0;
            for (int i = 0; i < n; i++) {
                s = add(s, i);
            }
            return s;
        }

        static int fib(int n) {
            if (n < 2) return n;
            return fib(n - 1) + fib(n - 2);
        }

        static int square(int x) { return x * x; }
        static int increment(int x) { return x + 1; }
        static int doubleIt(int x) { return x * 2; }

        static int chain(int x) {
            return doubleIt(increment(square(x)));
        }
    }
}
