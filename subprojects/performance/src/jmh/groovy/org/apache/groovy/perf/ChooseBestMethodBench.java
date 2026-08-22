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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.codehaus.groovy.ast.ClassHelper.Boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Integer_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SET_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.chooseBestMethod;

/**
 * Microbenchmark measuring chooseBestMethod distance computation and overload resolution.
 * Isolates the performance and GC allocation impact of eliminating Parameter[] clones
 * on non-generic parameter lists.
 */
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ChooseBestMethodBench {

    private ClassNode owner;
    private List<MethodNode> nonGenericOverloads;
    private List<MethodNode> genericOverloads;
    private List<MethodNode> mixedOverloads;
    private List<MethodNode> singleNonGeneric;
    private List<MethodNode> heavyOverloads;

    private ClassNode[] nonGenericArgs;
    private ClassNode[] genericArgs;
    private ClassNode[] heavyArgs;

    @Setup
    public void setup() {
        owner = new ClassNode("TestService", Modifier.PUBLIC, OBJECT_TYPE);

        // 1. Non-generic overloads (4 candidates, concrete types)
        nonGenericOverloads = new ArrayList<>();
        nonGenericOverloads.add(method(owner, "process", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(Integer_TYPE, "i")));
        nonGenericOverloads.add(method(owner, "process", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(Long_TYPE, "l")));
        nonGenericOverloads.add(method(owner, "process", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(Double_TYPE, "d")));
        nonGenericOverloads.add(method(owner, "process", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(OBJECT_TYPE, "o")));

        // 2. Generic overloads (2 candidates with type parameters)
        ClassNode t = placeholder("T");
        ClassNode listOfT = parameterized(LIST_TYPE, t);
        ClassNode setOfT = parameterized(SET_TYPE, t);
        genericOverloads = new ArrayList<>();
        genericOverloads.add(method(owner, "handle", VOID_TYPE, new Parameter(listOfT, "items"), new Parameter(t, "sample")));
        genericOverloads.add(method(owner, "handle", VOID_TYPE, new Parameter(setOfT, "items"), new Parameter(t, "sample")));

        // 3. Mixed overloads (2 non-generic + 2 generic)
        ClassNode k = placeholder("K");
        ClassNode v = placeholder("V");
        ClassNode mapOfKV = parameterized(MAP_TYPE, k, v);
        mixedOverloads = new ArrayList<>();
        mixedOverloads.add(method(owner, "transform", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(OBJECT_TYPE, "o")));
        mixedOverloads.add(method(owner, "transform", VOID_TYPE, new Parameter(OBJECT_TYPE, "a"), new Parameter(OBJECT_TYPE, "b")));
        mixedOverloads.add(method(owner, "transform", VOID_TYPE, new Parameter(listOfT, "list"), new Parameter(t, "val")));
        mixedOverloads.add(method(owner, "transform", VOID_TYPE, new Parameter(mapOfKV, "map"), new Parameter(k, "key")));

        // 4. Single non-generic method
        singleNonGeneric = new ArrayList<>();
        singleNonGeneric.add(method(owner, "execute", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(Integer_TYPE, "i")));

        // 5. Heavy non-generic overloads (8 candidates with 3 parameters)
        heavyOverloads = new ArrayList<>();
        heavyOverloads.add(method(owner, "dispatch", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(Integer_TYPE, "i"), new Parameter(Boolean_TYPE, "b")));
        heavyOverloads.add(method(owner, "dispatch", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(Long_TYPE, "l"), new Parameter(Boolean_TYPE, "b")));
        heavyOverloads.add(method(owner, "dispatch", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(Double_TYPE, "d"), new Parameter(Boolean_TYPE, "b")));
        heavyOverloads.add(method(owner, "dispatch", VOID_TYPE, new Parameter(STRING_TYPE, "s"), new Parameter(OBJECT_TYPE, "o"), new Parameter(Boolean_TYPE, "b")));
        heavyOverloads.add(method(owner, "dispatch", VOID_TYPE, new Parameter(OBJECT_TYPE, "s"), new Parameter(Integer_TYPE, "i"), new Parameter(Boolean_TYPE, "b")));
        heavyOverloads.add(method(owner, "dispatch", VOID_TYPE, new Parameter(OBJECT_TYPE, "s"), new Parameter(Long_TYPE, "l"), new Parameter(Boolean_TYPE, "b")));
        heavyOverloads.add(method(owner, "dispatch", VOID_TYPE, new Parameter(OBJECT_TYPE, "s"), new Parameter(Double_TYPE, "d"), new Parameter(Boolean_TYPE, "b")));
        heavyOverloads.add(method(owner, "dispatch", VOID_TYPE, new Parameter(OBJECT_TYPE, "s"), new Parameter(OBJECT_TYPE, "o"), new Parameter(OBJECT_TYPE, "b")));

        nonGenericArgs = new ClassNode[]{STRING_TYPE, Integer_TYPE};
        genericArgs = new ClassNode[]{LIST_TYPE, STRING_TYPE};
        heavyArgs = new ClassNode[]{STRING_TYPE, Integer_TYPE, Boolean_TYPE};

        // Warm up invocation
        chooseBestMethod(owner, nonGenericOverloads, nonGenericArgs);
    }

    @Benchmark
    public List<MethodNode> chooseNonGenericOverloads() {
        return chooseBestMethod(owner, nonGenericOverloads, nonGenericArgs);
    }

    @Benchmark
    public List<MethodNode> chooseSingleNonGeneric() {
        return chooseBestMethod(owner, singleNonGeneric, nonGenericArgs);
    }

    @Benchmark
    public List<MethodNode> chooseMixedOverloads() {
        return chooseBestMethod(owner, mixedOverloads, genericArgs);
    }

    @Benchmark
    public List<MethodNode> chooseGenericOverloads() {
        return chooseBestMethod(owner, genericOverloads, genericArgs);
    }

    @Benchmark
    public List<MethodNode> chooseHeavyNonGenericOverloads() {
        return chooseBestMethod(owner, heavyOverloads, heavyArgs);
    }

    private static MethodNode method(ClassNode owner, String name, ClassNode returnType, Parameter... params) {
        MethodNode mn = new MethodNode(name, Modifier.PUBLIC, returnType, params, ClassNode.EMPTY_ARRAY, null);
        mn.setDeclaringClass(owner);
        return mn;
    }

    private static ClassNode parameterized(ClassNode raw, ClassNode... typeArgs) {
        ClassNode cn = raw.getPlainNodeReference();
        cn.setUsingGenerics(true);
        GenericsType[] gt = new GenericsType[typeArgs.length];
        for (int i = 0; i < typeArgs.length; i++) {
            gt[i] = new GenericsType(typeArgs[i]);
        }
        cn.setGenericsTypes(gt);
        return cn;
    }

    private static ClassNode placeholder(String name) {
        ClassNode t = makeWithoutCaching(name);
        t.setRedirect(OBJECT_TYPE);
        t.setGenericsPlaceHolder(true);
        t.setUsingGenerics(true);
        t.setGenericsTypes(new GenericsType[]{new GenericsType(t)});
        return t;
    }
}
