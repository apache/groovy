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
package org.apache.groovy.perf;

import groovy.transform.CompileStatic;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Macrobenchmark measuring static type checking (STC) compilation throughput and latency
 * under @CompileStatic across diverse extension method lookups and method overload resolutions.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class StcCompilePerfBench {

    private static final int CLASSES = 50;
    private static final int CALLS_PER_CLASS = 60;

    @Param({"dgmDense", "overloadDense", "mixedDense"})
    private String suite;

    private Map<String, String> corpus;

    @Setup(Level.Trial)
    public void generateCorpus() {
        corpus = new LinkedHashMap<>();
        for (int c = 0; c < CLASSES; c += 1) {
            corpus.put("StcGen" + c, sourceFor(c));
        }
    }

    @Benchmark
    public int compileStcCorpus() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic.class));
        CompilationUnit unit = new CompilationUnit(config);
        for (Map.Entry<String, String> source : corpus.entrySet()) {
            unit.addSource(source.getKey() + ".groovy", source.getValue());
        }
        unit.compile(Phases.INSTRUCTION_SELECTION);
        return unit.getAST().getModules().size();
    }

    private String sourceFor(final int index) {
        switch (suite) {
            case "dgmDense":      return dgmDenseClass(index);
            case "overloadDense": return overloadDenseClass(index);
            case "mixedDense":    return mixedDenseClass(index);
            default: throw new IllegalArgumentException("Unknown suite: " + suite);
        }
    }

    private String dgmDenseClass(final int index) {
        String[] templates = {
            "xs.each { sink(it) }",
            "sink(xs.collect { it.length() })",
            "sink(xs.findAll { it.length() > 0 })",
            "sink(xs.find { it == 'a' })",
            "sink(xs.join(','))",
            "sink(xs.max())",
            "sink(xs.min())",
            "sink(xs.reverse())",
            "sink(xs.groupBy { it.length() })",
            "sink(xs.inject('') { acc, val -> acc + val })",
            "sink(nums.sum())",
            "sink(nums.findAll { it > 0 })",
            "sink(nums.collect { it * 2 })",
            "m.each { k, v -> sink(v) }",
            "sink(m.collect { k, v -> k + v })",
            "sink(m.findAll { k, v -> v > 0 })",
            "sink(str.padLeft(10))",
            "sink(str.padRight(10))"
        };
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < CALLS_PER_CLASS; i += 1) {
            body.append("        ").append(templates[i % templates.length]).append('\n');
        }
        return "class StcGen" + index + " {\n"
             + "    void sink(Object o) {}\n"
             + "    void run(List<String> xs, Map<String, Integer> m, List<Integer> nums, String str) {\n"
             + body
             + "    }\n"
             + "}\n";
    }

    private String overloadDenseClass(final int index) {
        String[] templates = {
            "sink(dispatch('a', 1, true))",
            "sink(dispatch('a', 2L, false))",
            "sink(dispatch('a', 3.0d, true))",
            "sink(dispatch('a', new Object(), false))",
            "sink(dispatch(1, 'a', true))",
            "sink(dispatch(2L, 'a', false))",
            "sink(dispatch(3.0d, 'a', true))"
        };
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < CALLS_PER_CLASS; i += 1) {
            body.append("        ").append(templates[i % templates.length]).append('\n');
        }
        return "class StcGen" + index + " {\n"
             + "    void sink(Object o) {}\n"
             + "    String dispatch(String s, Integer i, Boolean b) { s + i + b }\n"
             + "    String dispatch(String s, Long l, Boolean b) { s + l + b }\n"
             + "    String dispatch(String s, Double d, Boolean b) { s + d + b }\n"
             + "    String dispatch(String s, Object o, Boolean b) { s + o + b }\n"
             + "    String dispatch(Integer i, String s, Boolean b) { i + s + b }\n"
             + "    String dispatch(Long l, String s, Boolean b) { l + s + b }\n"
             + "    String dispatch(Double d, String s, Boolean b) { d + s + b }\n"
             + "    void run() {\n"
             + body
             + "    }\n"
             + "}\n";
    }

    private String mixedDenseClass(final int index) {
        String[] templates = {
            "xs.each { sink(dispatch(it, 1, true)) }",
            "sink(xs.collect { dispatch(it, 2L, false) })",
            "sink(xs.findAll { it.length() > 0 })",
            "sink(m.collect { k, v -> dispatch(k, v, true) })",
            "sink(nums.sum())",
            "sink(dispatch('str', nums.size(), false))",
            "sink(str.padLeft(10))"
        };
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < CALLS_PER_CLASS; i += 1) {
            body.append("        ").append(templates[i % templates.length]).append('\n');
        }
        return "class StcGen" + index + " {\n"
             + "    void sink(Object o) {}\n"
             + "    String dispatch(String s, Integer i, Boolean b) { s + i + b }\n"
             + "    String dispatch(String s, Long l, Boolean b) { s + l + b }\n"
             + "    String dispatch(String s, Double d, Boolean b) { s + d + b }\n"
             + "    String dispatch(String s, Object o, Boolean b) { s + o + b }\n"
             + "    void run(List<String> xs, Map<String, Integer> m, List<Integer> nums, String str) {\n"
             + body
             + "    }\n"
             + "}\n";
    }
}
