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
package groovy.transform.stc

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests for the opt-in {@code groovy.target.lambda.hoist} optimization: a non-capturing, non-serializable
 * lambda targeting a functional interface has its body compiled as a {@code private static} method on the
 * enclosing class (with LambdaMetafactory bootstrapped directly against it), so no per-lambda class is
 * generated. Capturing, instance-accessing, serializable and nested cases fall back to a generated lambda
 * class. The property is read per lambda, so it can be toggled per compilation here.
 */
final class LambdaHoistTest {

    private static final String PROP = 'groovy.target.lambda.hoist'

    /** Compiles the source with hoisting on/off and returns the sorted generated class names. */
    private static List<String> classNames(String src, boolean hoist) {
        withHoist(hoist) {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('L.groovy', src)
            cu.compile(Phases.CLASS_GENERATION)
            cu.classes.collect { it.name }.sort()
        }
    }

    /** Evaluates the source (with a trailing expression) with hoisting on/off. */
    private static Object eval(String src, String tail, boolean hoist) {
        withHoist(hoist) { new GroovyShell().evaluate(src + '\n' + tail) }
    }

    private static <T> T withHoist(boolean hoist, Closure<T> work) {
        String previous = System.getProperty(PROP)
        if (hoist) System.setProperty(PROP, 'true') else System.clearProperty(PROP)
        try {
            work.call()
        } finally {
            if (previous != null) System.setProperty(PROP, previous) else System.clearProperty(PROP)
        }
    }

    private static int lambdaClassCount(List<String> names) {
        names.count { it.contains('_lambda') }
    }

    private static final String SRC = '''import groovy.transform.CompileStatic
        import java.util.function.*
        @CompileStatic
        class L {
            Function<Integer,Integer> nonCap()      { (Integer x) -> x * 2 }
            Function<Integer,Integer> cap(int base) { (Integer x) -> x + base }
            static Function<Integer,Integer> inStatic() { (Integer x) -> x * 3 }
            Function<Integer,Integer> callsStatic() { (Integer x) -> helper(x) }
            static int helper(int n) { n + 1000 }
        }'''

    @Test
    void nonCapturingLambdaIsHoistedWhenEnabled() {
        def names = classNames(SRC, true)
        // Only the capturing lambda keeps its class; nonCap/inStatic/callsStatic are hoisted onto L.
        assertEquals(1, lambdaClassCount(names), "expected only the capturing lambda class, got: $names")
        // The hoisted impls are private static $lambda$ methods on L.
        def hoisted = eval(SRC, 'new L()', true).getClass().declaredMethods.findAll { it.name.startsWith('$lambda$') }
        assertTrue(hoisted.size() >= 3, "expected hoisted \$lambda\$ methods on L, got: ${hoisted*.name}")
    }

    @Test
    void hoistedLambdasBehaveIdenticallyAndStaySingletons() {
        def l = eval(SRC, 'new L()', true)
        assertEquals(42, l.nonCap().apply(21))
        assertEquals(105, l.cap(100).apply(5))           // capturing path preserved
        assertEquals(12, l.class.inStatic().apply(4))    // lambda in a static method
        assertEquals(1005, l.callsStatic().apply(5))     // qualified outer static call
        // non-capturing metafactory instance is a zero-alloc singleton
        assertTrue(l.nonCap().is(l.nonCap()))
    }

    @Test
    void disabledByDefaultKeepsTheLambdaClass() {
        def names = classNames(SRC, false)
        // With hoisting off, every lambda (incl. the non-capturing ones) generates a class, as today.
        assertTrue(lambdaClassCount(names) >= 4, "expected lambda classes when disabled, got: $names")
        // ...and still behaves correctly.
        assertEquals(42, eval(SRC, 'new L().nonCap().apply(21)', false))
    }

    @Test
    void instanceAccessingLambdaIsNotHoisted() {
        String src = '''import groovy.transform.CompileStatic
            import java.util.function.IntSupplier
            @CompileStatic
            class Counter { int count = 7; IntSupplier supplier() { () -> count } }'''
        def names = classNames(src, true)
        assertEquals(1, lambdaClassCount(names), "instance-accessing lambda must keep its class: $names")
        assertEquals(7, eval(src, 'new Counter().supplier().getAsInt()', true))
    }

    @Test
    void serializableNonCapturingLambdaFallsBackAndRoundTrips() {
        String src = '''import groovy.transform.CompileStatic
            import java.util.function.Function
            @CompileStatic
            class S {
                static Function<Integer,Integer> make() {
                    (Function<Integer,Integer> & Serializable) (Integer x) -> x * 2
                }
            }'''
        def names = classNames(src, true)
        assertEquals(1, lambdaClassCount(names), "serializable lambda must keep its class: $names")
        def roundTripped = eval(src, '''
            def f = S.make()
            def out = new ByteArrayOutputStream()
            new ObjectOutputStream(out).writeObject(f)
            def f2 = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject()
            ((java.util.function.Function) f2).apply(10)
        ''', true)
        assertEquals(20, roundTripped)
    }

    @Test
    void lambdaContainingNestedFunctionIsNotHoisted() {
        String src = '''import groovy.transform.CompileStatic
            import java.util.function.Supplier
            @CompileStatic
            class N {
                Supplier<List<Integer>> outer() { () -> [1, 2, 3].collect { it * 2 } }
            }'''
        def names = classNames(src, true)
        // The outer lambda nests a closure, so it is not hoisted (kept flat for the first step).
        assertFalse(lambdaClassCount(names) == 0, "nested-function case should keep a lambda class: $names")
        assertEquals([2, 4, 6], eval(src, 'new N().outer().get()', true))
    }
}
