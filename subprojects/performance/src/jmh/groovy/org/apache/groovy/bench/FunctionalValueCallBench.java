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

import groovy.lang.Closure;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Benchmarks for call syntax on functional values in dynamic code: {@code f(x)}
 * on a lambda and on a coercion proxy, against the call of the abstract method
 * by its name and against the call of a closure.
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class FunctionalValueCallBench {

    private final Function<Integer, Integer> lambda = FunctionalValueCall.lambda();
    private final Function<Integer, Integer> proxy = FunctionalValueCall.proxy();
    private final Closure<Integer> closure = FunctionalValueCall.closure();
    private final Integer argument = 42;

    /**
     * Benchmark {@code f(x)} on a lambda.
     * @return the result value
     */
    @Benchmark
    public Object lambdaCallSyntax() {
        return FunctionalValueCall.callSyntax(lambda, argument);
    }

    /**
     * Benchmark {@code f.apply(x)} on a lambda.
     * @return the result value
     */
    @Benchmark
    public Object lambdaApply() {
        return FunctionalValueCall.apply(lambda, argument);
    }

    /**
     * Benchmark {@code f(x)} on a lambda through the meta-class.
     * @return the result value
     */
    @Benchmark
    public Object lambdaCallThroughMetaClass() {
        return FunctionalValueCall.callThroughMetaClass(lambda, argument);
    }

    /**
     * Benchmark {@code f(x)} on a coercion proxy.
     * @return the result value
     */
    @Benchmark
    public Object proxyCallSyntax() {
        return FunctionalValueCall.callSyntax(proxy, argument);
    }

    /**
     * Benchmark {@code f.apply(x)} on a coercion proxy.
     * @return the result value
     */
    @Benchmark
    public Object proxyApply() {
        return FunctionalValueCall.apply(proxy, argument);
    }

    /**
     * Benchmark {@code f(x)} on a closure.
     * @return the result value
     */
    @Benchmark
    public Object closureCall() {
        return FunctionalValueCall.callSyntax(closure, argument);
    }
}
