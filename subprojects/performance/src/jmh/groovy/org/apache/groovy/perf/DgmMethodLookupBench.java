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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;

/**
 * Microbenchmark for DGM extension method lookup across various receiver types and methods.
 * Measures the algorithmic speedup of name-indexed cache lookup vs linear scanning.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class DgmMethodLookupBench {

    private ClassLoader loader;
    private ClassNode[] closureArgs;
    private ClassNode stringArrayType;
    private ClassNode intArrayType;

    @Setup
    public void setup() {
        loader = getClass().getClassLoader();
        closureArgs = new ClassNode[]{CLOSURE_TYPE};
        stringArrayType = STRING_TYPE.makeArray();
        intArrayType = int_TYPE.makeArray();
        // Warm up the extension method cache
        StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, OBJECT_TYPE, "with");
    }

    @Benchmark
    public Set<MethodNode> lookupObjectHit() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, OBJECT_TYPE, "with");
    }

    @Benchmark
    public Set<MethodNode> lookupObjectMiss() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, OBJECT_TYPE, "definitelyNotADgmMethod");
    }

    @Benchmark
    public Set<MethodNode> lookupListHit() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, LIST_TYPE, "each");
    }

    @Benchmark
    public Set<MethodNode> lookupListMiss() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, LIST_TYPE, "definitelyNotADgmMethod");
    }

    @Benchmark
    public Set<MethodNode> lookupStringHit() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, STRING_TYPE, "padLeft");
    }

    @Benchmark
    public Set<MethodNode> lookupStringMiss() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, STRING_TYPE, "definitelyNotADgmMethod");
    }

    @Benchmark
    public Set<MethodNode> lookupMapHit() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, MAP_TYPE, "collect");
    }

    @Benchmark
    public Set<MethodNode> lookupArrayHit() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, stringArrayType, "getAt");
    }

    @Benchmark
    public Set<MethodNode> lookupPrimitiveArrayHit() {
        return StaticTypeCheckingSupport.findDGMMethodsForClassNode(loader, intArrayType, "getAt");
    }

    @Benchmark
    public List<MethodNode> lookupByNameAndArgs() {
        return StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments(loader, LIST_TYPE, "each", closureArgs);
    }
}
