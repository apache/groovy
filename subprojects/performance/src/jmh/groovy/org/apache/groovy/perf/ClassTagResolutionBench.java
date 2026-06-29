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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Compile-time cost of {@code @ClassTag} preemption matching (GROOVY-12115) under
 * {@code @CompileStatic}. Preemption matching runs on every resolved method call once a token-less
 * overload has matched; this benchmark measures whether that cost is visible over a corpus of
 * realistic calls, and how far the global opt-out (which short-circuits all preemption) lowers it.
 * <p>
 * The {@code preemption} parameter is the controlled comparison: {@code active} is the shipping
 * behaviour, {@code disabled} sets {@code classTagPreemptionDisabled=true} so the preemption path
 * is never entered - functionally the pre-feature floor for a corpus whose calls all resolve
 * (additive injection only fires on otherwise-unresolved calls, which this corpus does not
 * contain). {@code active - disabled} is therefore the cost of active preemption matching.
 * <p>
 * The {@code mix} parameter selects the call shape: {@code dgmHeavy} exercises the
 * extension-incumbent path (the one the gate targets), {@code instanceHeavy} the
 * instance-incumbent path (a per-receiver overload scan, not gated), and {@code withDefault} the
 * happy path that actually preempts and rewrites (must not regress).
 * <p>
 * Recorded outcome (60 classes x 80 calls, avgt ms/op, 2 forks x 5+5 iterations; the pre-gate
 * column predates the {@code ExtensionMethodCache} name-set gate, and "floor" was then spelled
 * {@code vetoAll} via the since-removed selector-set veto - functionally identical to
 * {@code disabled}):
 * <pre>
 *   mix            active(pre-gate)  active(gated)   disabled(floor)
 *   dgmHeavy            121.8            108.1            110.3     -- regression eliminated
 *   instanceHeavy       52.8             54.6             51.4      -- ~6% residual, instance path not gated
 *   withDefault        263.1            269.7            227.0      -- unchanged; withDefault does real preempt work
 * </pre>
 * The extension-incumbent gate ({@code ExtensionMethodCache.getPreemptiveNames}) returns the
 * gated {@code active} to the {@code disabled} floor for ordinary DGM-heavy code. The
 * {@code instanceHeavy} residual is the deferred per-receiver overload scan; {@code withDefault}
 * is unaffected by the gate (it is in the preemptive-names set) and reflects the cost of the
 * feature actually firing on every call.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ClassTagResolutionBench {

    private static final int CLASSES = 60;
    private static final int CALLS_PER_CLASS = 80;

    @Param({"dgmHeavy", "instanceHeavy", "withDefault"})
    private String mix;

    @Param({"active", "disabled"})
    private String preemption;

    private Map<String, String> corpus;

    @Setup(Level.Trial)
    public void generateCorpus() {
        corpus = new LinkedHashMap<>();
        for (int c = 0; c < CLASSES; c += 1) {
            corpus.put("Gen" + c, sourceFor(c));
        }
    }

    @Benchmark
    public int compileCorpus() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic.class));
        if ("disabled".equals(preemption)) {
            config.setClassTagPreemptionDisabled(true);
        }
        CompilationUnit unit = new CompilationUnit(config);
        for (Map.Entry<String, String> source : corpus.entrySet()) {
            unit.addSource(source.getKey() + ".groovy", source.getValue());
        }
        unit.compile(Phases.INSTRUCTION_SELECTION);
        return unit.getAST().getModules().size();
    }

    private String sourceFor(final int index) {
        switch (mix) {
            case "dgmHeavy":      return dgmHeavyClass(index);
            case "instanceHeavy": return instanceHeavyClass(index);
            case "withDefault":   return withDefaultClass(index);
            default: throw new IllegalArgumentException("unknown mix: " + mix);
        }
    }

    // resolving DGM calls on typed receivers: each call binds a token-less extension overload, so
    // preemption matching runs (extension incumbent -> same-owner candidate scan + intent check)
    private String dgmHeavyClass(final int index) {
        String[] templates = {
            "xs.each { sink(it) }",
            "sink(xs.collect { it.length() })",
            "sink(xs.findAll { it.size() > 0 })",
            "sink(xs.join(','))",
            "sink(xs.max())",
            "sink(xs.reverse())",
            "ys.each { sink(it) }",
            "sink(ys.collect { it.toUpperCase() })",
            "m.each { k, v -> sink(v) }",
            "sink(m.collect { k, v -> v })",
            "sink(m.findAll { k, v -> v > 0 })",
            "sink(nums.sum())",
            "sink(nums.findAll { it > 1 })",
            "sink(nums.collect { it * 2 })",
        };
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < CALLS_PER_CLASS; i += 1) {
            body.append("        ").append(templates[i % templates.length]).append('\n');
        }
        return "class Gen" + index + " {\n"
             + "    void sink(Object o) {}\n"
             + "    void run(List<String> xs, Set<String> ys, Map<String,Integer> m, List<Integer> nums) {\n"
             + body
             + "    }\n"
             + "}\n";
    }

    // resolving calls to overloaded instance methods declared in the same class: preemption
    // matching runs the instance-incumbent path (a same-name overload scan over the receiver)
    private String instanceHeavyClass(final int index) {
        String[] templates = {
            "proc('a')",
            "proc(1)",
            "proc(2L)",
            "proc(['x'])",
            "proc(3.0d)",
            "combine('a', 1)",
            "combine(1, 'a')",
        };
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < CALLS_PER_CLASS; i += 1) {
            body.append("        sink(").append(templates[i % templates.length]).append(")\n");
        }
        return "class Gen" + index + " {\n"
             + "    void sink(Object o) {}\n"
             + "    String proc(String s) { s }\n"
             + "    String proc(Integer i) { i.toString() }\n"
             + "    String proc(Long l) { l.toString() }\n"
             + "    String proc(Double d) { d.toString() }\n"
             + "    String proc(List<String> l) { l.isEmpty() ? '' : l[0] }\n"
             + "    String combine(String s, Integer i) { s + i }\n"
             + "    String combine(Integer i, String s) { i + s }\n"
             + "    void run() {\n"
             + body
             + "    }\n"
             + "}\n";
    }

    // calls that actually preempt: a statically-typed map receiver whose withDefault{...} is
    // upgraded to the key/value-checked overload (token synthesis + argument rewrite)
    private String withDefaultClass(final int index) {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < CALLS_PER_CLASS; i += 1) {
            body.append("        Map<Number,String> base").append(i).append(" = [:]\n");
            body.append("        Map<Number,String> r").append(i).append(" = base").append(i).append(".withDefault { 'n/a' }\n");
            body.append("        sink(r").append(i).append(")\n");
        }
        return "class Gen" + index + " {\n"
             + "    void sink(Object o) {}\n"
             + "    void run() {\n"
             + body
             + "    }\n"
             + "}\n";
    }

    // kept for potential ad-hoc invocation without the JMH harness
    static List<String> sample(final ClassTagResolutionBench bench) {
        List<String> out = new ArrayList<>();
        for (int c = 0; c < 2; c += 1) out.add(bench.sourceFor(c));
        return out;
    }
}
