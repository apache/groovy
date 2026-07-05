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
package org.apache.groovy.perf.metrics

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases

/**
 * Deterministic workload behind the {@code perf:dispatchMetrics} task.
 * <p>
 * Timing benchmarks on shared CI runners are dominated by hardware variance,
 * so regressions hide in the noise. This workload instead produces metrics
 * that are <em>exact</em> for a given JDK + Groovy build and therefore
 * directly comparable across runners and against published history:
 * <ul>
 *   <li>classes loaded while executing a fixed set of cold dynamic-dispatch
 *       scenarios (captured externally via {@code -Xlog:class+load} and
 *       counted by the Gradle side — total, {@code LambdaForm$*}, hidden,
 *       Groovy-runtime), a direct proxy for the one-time
 *       MethodHandle/LambdaForm cost of the indy implementation;</li>
 *   <li>total bytecode bytes and class count for compiling a fixed,
 *       dependency-free source corpus — a stable measure of generated-code
 *       size (inlining budgets are bytecode-size based).</li>
 * </ul>
 * Everything here must stay deterministic: fixed iteration counts, fixed
 * inputs, single-threaded execution, no time- or randomness-dependent
 * behaviour. Scenario iteration counts stay below the indy promotion
 * threshold ({@code groovy.indy.optimize.threshold}, default 1000) so the
 * dispatch machinery is exercised on its cold tier, where most real-world
 * script/CLI/test-bootstrap cost lives.
 * <p>
 * Usage: {@code DispatchMetricsWorkload <output-json> <corpus-file>...}
 * The output JSON fragment uses the same
 * {@code customSmallerIsBetter} entry schema as the final report so the two
 * can simply be concatenated.
 */
class DispatchMetricsWorkload {

    /** Iterations per dispatch scenario; kept below the indy promotion threshold. */
    static final int COLD_ITERATIONS = 512

    static void main(String[] args) {
        if (args.length < 2) {
            System.err.println 'usage: DispatchMetricsWorkload <output-json> <corpus-file>...'
            System.exit 1
        }
        File outFile = new File(args[0])
        List<File> corpus = args[1..-1].collect { new File(it) }

        long checksum = runDispatchScenarios()
        checksum += evaluateScript()

        long corpusBytes = 0
        int corpusClasses = 0
        for (File source : corpus) {
            def unit = new CompilationUnit(new CompilerConfiguration())
            unit.addSource(source)
            unit.compile(Phases.CLASS_GENERATION)
            unit.classes.each { generated ->
                corpusBytes += generated.bytes.length
                corpusClasses++
            }
        }

        outFile.parentFile?.mkdirs()
        outFile.text = """[
  { "name": "bytecode.corpus.bytes", "unit": "bytes", "value": ${corpusBytes} },
  { "name": "bytecode.corpus.classes", "unit": "classes", "value": ${corpusClasses} }
]
"""
        // the checksum keeps the scenario work observable (and un-eliminable)
        println "dispatch workload complete: checksum=${checksum}, corpus=${corpusClasses} classes / ${corpusBytes} bytes"
    }

    private static long runDispatchScenarios() {
        def scenarios = new Scenarios()
        long checksum = 0
        checksum += scenarios.monomorphicCalls(COLD_ITERATIONS)
        checksum += scenarios.polymorphicCalls(COLD_ITERATIONS)
        checksum += scenarios.propertyAccess(COLD_ITERATIONS)
        checksum += scenarios.closureCalls(COLD_ITERATIONS)
        checksum += scenarios.operatorCalls(COLD_ITERATIONS)
        checksum += scenarios.gstringsAndCollections(COLD_ITERATIONS >> 1)
        checksum += scenarios.constructorCalls(COLD_ITERATIONS >> 1)
        checksum
    }

    /** Compile-and-run cost of a small script, the groovy-CLI/test-bootstrap pattern. */
    private static long evaluateScript() {
        def shell = new GroovyShell()
        def result = shell.evaluate('''
            class Greeter {
                String name
                String greet() { "hello, ${name}" }
            }
            def acc = 0
            (1..64).each { acc += it }
            def g = new Greeter(name: 'groovy')
            acc + g.greet().size()
        ''')
        ((Number) result).longValue()
    }

    /**
     * Cold dynamic-dispatch scenarios. Deliberately dynamic Groovy: every
     * call below goes through the runtime's dispatch machinery.
     */
    static class Scenarios {

        static class Node {
            int weight
            int bump(int i) { weight + i }
        }

        static class HeavyNode extends Node {
            @Override
            int bump(int i) { weight + 2 * i }
        }

        static class LightNode extends Node {
            @Override
            int bump(int i) { weight - i }
        }

        long monomorphicCalls(int n) {
            def node = new Node(weight: 3)
            long s = 0
            for (int i = 0; i < n; i++) {
                s += node.bump(i)
            }
            s
        }

        long polymorphicCalls(int n) {
            def nodes = [new Node(weight: 1), new HeavyNode(weight: 2), new LightNode(weight: 3)]
            long s = 0
            for (int i = 0; i < n; i++) {
                s += nodes[i % 3].bump(i)
            }
            s
        }

        long propertyAccess(int n) {
            def node = new HeavyNode(weight: 5)
            long s = 0
            for (int i = 0; i < n; i++) {
                node.weight = i
                s += node.weight
            }
            s
        }

        long closureCalls(int n) {
            def twice = { int x -> 2 * x }
            def offset = 7
            def shifted = { int x -> x + offset }
            long s = 0
            for (int i = 0; i < n; i++) {
                s += twice(i) + shifted(i)
            }
            s
        }

        long operatorCalls(int n) {
            def total = BigInteger.ZERO
            def list = []
            for (int i = 0; i < n; i++) {
                total = total + BigInteger.valueOf(i)
                list << i
            }
            total.longValue() + list.size()
        }

        long gstringsAndCollections(int n) {
            long s = 0
            def words = (0..<n).collect { "item-${it}" }
            words.each { s += it.size() }
            s += words.findAll { it.endsWith('7') }.size()
            s
        }

        long constructorCalls(int n) {
            long s = 0
            for (int i = 0; i < n; i++) {
                def node = (i % 2 == 0) ? new Node(weight: i) : new HeavyNode(weight: i)
                s += node.weight
            }
            s
        }
    }
}
