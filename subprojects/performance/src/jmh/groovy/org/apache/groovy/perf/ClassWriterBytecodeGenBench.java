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

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark measuring bytecode generation phase ({@link Phases#CLASS_GENERATION}) performance,
 * specifically targeting ASM {@code ClassWriter(COMPUTE_FRAMES)} and {@code getCommonSuperClass}
 * lookups at control-flow merge points (GROOVY-12288).
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ClassWriterBytecodeGenBench {

    private static final int CLASS_COUNT = 30;

    @Param({"mergeHeavy", "deepHierarchy", "polymorphicCollections", "staticCompileMerge", "closureHeavy", "exceptionHierarchy", "deeplyNestedBranches", "largeScaleClass"})
    private String scenario;

    private Map<String, String> corpus;

    @Setup(Level.Trial)
    public void generateCorpus() {
        corpus = new LinkedHashMap<>();
        for (int i = 0; i < CLASS_COUNT; i++) {
            corpus.put("GeneratedClass" + i, generateSource(i));
        }
    }

    @Benchmark
    public long compileBytecode() {
        CompilerConfiguration config = new CompilerConfiguration();
        CompilationUnit unit = new CompilationUnit(config);
        for (Map.Entry<String, String> source : corpus.entrySet()) {
            unit.addSource(source.getKey() + ".groovy", source.getValue());
        }
        unit.compile(Phases.CLASS_GENERATION);
        long totalBytes = 0;
        List<GroovyClass> classes = unit.getClasses();
        for (GroovyClass gc : classes) {
            totalBytes += gc.getBytes().length;
        }
        return totalBytes;
    }

    private String generateSource(final int index) {
        switch (scenario) {
            case "mergeHeavy":
                return generateMergeHeavySource(index);
            case "deepHierarchy":
                return generateDeepHierarchySource(index);
            case "polymorphicCollections":
                return generatePolymorphicCollectionsSource(index);
            case "staticCompileMerge":
                return generateStaticCompileMergeSource(index);
            case "closureHeavy":
                return generateClosureHeavySource(index);
            case "exceptionHierarchy":
                return generateExceptionHierarchySource(index);
            case "deeplyNestedBranches":
                return generateDeeplyNestedBranchesSource(index);
            case "largeScaleClass":
                return generateLargeScaleClassSource(index);
            default:
                throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }

    private String generateMergeHeavySource(final int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("package pkg").append(index).append("\n\n");
        sb.append("class BranchMergeClass").append(index).append(" {\n");
        for (int m = 0; m < 5; m++) {
            sb.append("    def method").append(m).append("(int x, boolean f1, boolean f2, boolean f3) {\n");
            sb.append("        def list = f1 ? new java.util.ArrayList() : new java.util.LinkedList()\n");
            sb.append("        def map = f2 ? new java.util.HashMap() : new java.util.TreeMap()\n");
            sb.append("        def seq = f3 ? new StringBuilder() : new StringBuffer()\n");
            sb.append("        for (int i = 0; i < 10; i++) {\n");
            sb.append("            def item = (i % 2 == 0) ? (f1 ? new java.util.ArrayList() : new java.util.LinkedList()) : (f2 ? new java.util.HashSet() : new java.util.TreeSet())\n");
            sb.append("            if (x > 5) {\n");
            sb.append("                def num = f3 ? Integer.valueOf(1) : Double.valueOf(2.0)\n");
            sb.append("            } else {\n");
            sb.append("                def s = f1 ? 'hello' : 'world'\n");
            sb.append("            }\n");
            sb.append("        }\n");
            sb.append("        try {\n");
            sb.append("            def stream = f1 ? new java.io.ByteArrayInputStream(new byte[0]) : new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(new byte[0]))\n");
            sb.append("            return stream\n");
            sb.append("        } catch (Exception e) {\n");
            sb.append("            return f2 ? new java.io.StringReader('a') : new java.io.CharArrayReader(new char[0])\n");
            sb.append("        }\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String generateDeepHierarchySource(final int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("package pkg").append(index).append("\n\n");
        sb.append("class BaseL0_").append(index).append(" {}\n");
        sb.append("class BaseL1_").append(index).append(" extends BaseL0_").append(index).append(" {}\n");
        sb.append("class BaseL2_").append(index).append(" extends BaseL1_").append(index).append(" {}\n");
        sb.append("class BaseL3_").append(index).append(" extends BaseL2_").append(index).append(" {}\n");
        sb.append("class BaseL4_").append(index).append(" extends BaseL3_").append(index).append(" {}\n");
        sb.append("class BaseL5_").append(index).append(" extends BaseL4_").append(index).append(" {}\n");
        sb.append("class LeafA_").append(index).append(" extends BaseL5_").append(index).append(" {}\n");
        sb.append("class LeafB_").append(index).append(" extends BaseL5_").append(index).append(" {}\n");
        sb.append("class LeafC_").append(index).append(" extends BaseL5_").append(index).append(" {}\n");
        sb.append("class LeafD_").append(index).append(" extends BaseL5_").append(index).append(" {}\n");
        sb.append("class DeepHierarchyClass").append(index).append(" {\n");
        for (int m = 0; m < 5; m++) {
            sb.append("    def testHierarchy").append(m).append("(boolean f1, boolean f2, boolean f3) {\n");
            sb.append("        def a = f1 ? new LeafA_").append(index).append("() : new LeafB_").append(index).append("()\n");
            sb.append("        def b = f2 ? new LeafC_").append(index).append("() : new LeafD_").append(index).append("()\n");
            sb.append("        def c = f3 ? a : b\n");
            sb.append("        for (int i = 0; i < 10; i++) {\n");
            sb.append("            def d = (i % 2 == 0) ? new LeafA_").append(index).append("() : (i % 3 == 0 ? new LeafC_").append(index).append("() : new LeafD_").append(index).append("())\n");
            sb.append("        }\n");
            sb.append("        return c\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String generatePolymorphicCollectionsSource(final int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("package pkg").append(index).append("\n\n");
        sb.append("class PolymorphicCollClass").append(index).append(" {\n");
        for (int m = 0; m < 5; m++) {
            sb.append("    def mergeCollections").append(m).append("(int mode, boolean flag) {\n");
            sb.append("        def c1 = flag ? new java.util.ArrayList() : new java.util.Vector()\n");
            sb.append("        def c2 = flag ? new java.util.HashSet() : new java.util.LinkedHashSet()\n");
            sb.append("        def c3 = flag ? new java.util.ArrayDeque() : new java.util.LinkedList()\n");
            sb.append("        def m1 = flag ? new java.util.HashMap() : new java.util.LinkedHashMap()\n");
            sb.append("        def m2 = flag ? new java.util.TreeMap() : new java.util.concurrent.ConcurrentHashMap()\n");
            sb.append("        def res = (mode == 0) ? c1 : ((mode == 1) ? c2 : c3)\n");
            sb.append("        def mapRes = flag ? m1 : m2\n");
            sb.append("        return [res, mapRes]\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String generateStaticCompileMergeSource(final int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("package pkg").append(index).append("\n\n");
        sb.append("@groovy.transform.CompileStatic\n");
        sb.append("class StaticCompileMergeClass").append(index).append(" {\n");
        for (int m = 0; m < 5; m++) {
            sb.append("    Object mergeMethod").append(m).append("(int x, boolean f1, boolean f2) {\n");
            sb.append("        Object c = f1 ? new java.util.ArrayList<String>() : new java.util.LinkedList<String>()\n");
            sb.append("        Object m = f2 ? new java.util.HashMap<String, Object>() : new java.util.TreeMap<String, Object>()\n");
            sb.append("        for (int i = 0; i < 10; i++) {\n");
            sb.append("            Object tmp = (i % 2 == 0) ? (f1 ? new java.util.HashSet<String>() : new java.util.TreeSet<String>()) : (f2 ? new java.util.ArrayList<String>() : new java.util.Vector<String>())\n");
            sb.append("        }\n");
            sb.append("        return f1 ? c : m\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String generateClosureHeavySource(final int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("package pkg").append(index).append("\n\n");
        sb.append("class ClosureHeavyClass").append(index).append(" {\n");
        for (int m = 0; m < 5; m++) {
            sb.append("    def runClosures").append(m).append("(boolean f1, boolean f2) {\n");
            sb.append("        def list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]\n");
            sb.append("        def c1 = { int val -> (val % 2 == 0) ? (f1 ? new java.util.ArrayList() : new java.util.LinkedList()) : (f2 ? new java.util.HashSet() : new java.util.TreeSet()) }\n");
            sb.append("        def c2 = { int val -> (val % 3 == 0) ? (f1 ? new java.util.HashMap() : new java.util.TreeMap()) : (f2 ? new java.util.ArrayDeque() : new java.util.LinkedList()) }\n");
            sb.append("        def c3 = { int val -> (val > 5) ? (f1 ? new StringBuilder() : new StringBuffer()) : (f2 ? 'foo' : 'bar') }\n");
            sb.append("        return list.collect(c1) + list.collect(c2) + list.collect(c3)\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String generateExceptionHierarchySource(final int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("package pkg").append(index).append("\n\n");
        sb.append("class ExceptionHierarchyClass").append(index).append(" {\n");
        for (int m = 0; m < 5; m++) {
            sb.append("    def handleErrors").append(m).append("(int code, boolean flag) {\n");
            sb.append("        def result = null\n");
            sb.append("        try {\n");
            sb.append("            if (code == 1) throw new java.io.FileNotFoundException('not found')\n");
            sb.append("            if (code == 2) throw new java.io.EOFException('eof')\n");
            sb.append("            if (code == 3) throw new java.net.SocketTimeoutException('timeout')\n");
            sb.append("            if (code == 4) throw new java.net.ConnectException('connect')\n");
            sb.append("            if (code == 5) throw new IllegalArgumentException('arg')\n");
            sb.append("            if (code == 6) throw new IllegalStateException('state')\n");
            sb.append("            result = flag ? new java.util.ArrayList() : new java.util.LinkedList()\n");
            sb.append("        } catch (java.io.FileNotFoundException | java.io.EOFException e) {\n");
            sb.append("            result = flag ? new java.io.ByteArrayInputStream(new byte[0]) : new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(new byte[0]))\n");
            sb.append("        } catch (java.net.SocketTimeoutException | java.net.ConnectException e) {\n");
            sb.append("            result = flag ? new java.io.StringReader('error') : new java.io.CharArrayReader(new char[0])\n");
            sb.append("        } catch (IllegalArgumentException | IllegalStateException e) {\n");
            sb.append("            result = flag ? new java.util.HashSet() : new java.util.TreeSet()\n");
            sb.append("        } catch (Exception e) {\n");
            sb.append("            result = flag ? new java.util.HashMap() : new java.util.TreeMap()\n");
            sb.append("        } finally {\n");
            sb.append("            def cleanup = flag ? new StringBuilder('done') : new StringBuffer('done')\n");
            sb.append("        }\n");
            sb.append("        return result\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String generateDeeplyNestedBranchesSource(final int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("package pkg").append(index).append("\n\n");
        sb.append("class NestedBranchesClass").append(index).append(" {\n");
        for (int m = 0; m < 5; m++) {
            sb.append("    def evaluateComplex").append(m).append("(int a, int b, int c, boolean flag) {\n");
            sb.append("        def res = (a > 0) ? (\n");
            sb.append("            (b > 0) ? (\n");
            sb.append("                (c > 0) ? (flag ? new java.util.ArrayList() : new java.util.LinkedList())\n");
            sb.append("                        : (flag ? new java.util.Vector() : new java.util.ArrayDeque())\n");
            sb.append("            ) : (\n");
            sb.append("                (c > 0) ? (flag ? new java.util.HashSet() : new java.util.TreeSet())\n");
            sb.append("                        : (flag ? new java.util.LinkedHashSet() : new java.util.concurrent.CopyOnWriteArraySet())\n");
            sb.append("            )\n");
            sb.append("        ) : (\n");
            sb.append("            (b > 0) ? (\n");
            sb.append("                (c > 0) ? (flag ? new java.util.HashMap() : new java.util.TreeMap())\n");
            sb.append("                        : (flag ? new java.util.LinkedHashMap() : new java.util.concurrent.ConcurrentHashMap())\n");
            sb.append("            ) : (\n");
            sb.append("                (c > 0) ? (flag ? new StringBuilder('A') : new StringBuffer('B'))\n");
            sb.append("                        : (flag ? new java.io.ByteArrayOutputStream() : new java.io.DataOutputStream(new java.io.ByteArrayOutputStream()))\n");
            sb.append("            )\n");
            sb.append("        )\n");
            sb.append("        return res\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String generateLargeScaleClassSource(final int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("package pkg").append(index).append("\n\n");
        sb.append("class LargeScaleClass").append(index).append(" {\n");
        for (int m = 0; m < 25; m++) {
            sb.append("    def method").append(m).append("(int x, boolean f1, boolean f2, boolean f3) {\n");
            sb.append("        def list = f1 ? new java.util.ArrayList() : new java.util.LinkedList()\n");
            sb.append("        def map = f2 ? new java.util.HashMap() : new java.util.TreeMap()\n");
            sb.append("        def seq = f3 ? new StringBuilder() : new StringBuffer()\n");
            sb.append("        for (int i = 0; i < 5; i++) {\n");
            sb.append("            def item = (i % 2 == 0) ? (f1 ? new java.util.ArrayList() : new java.util.LinkedList()) : (f2 ? new java.util.HashSet() : new java.util.TreeSet())\n");
            sb.append("            if (x > 5) {\n");
            sb.append("                def num = f3 ? Integer.valueOf(1) : Double.valueOf(2.0)\n");
            sb.append("            } else {\n");
            sb.append("                def s = f1 ? 'hello' : 'world'\n");
            sb.append("            }\n");
            sb.append("        }\n");
            sb.append("        return [list, map, seq]\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
