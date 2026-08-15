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
package org.codehaus.groovy.vmplugin.v8

import org.junit.jupiter.api.Test

import java.lang.invoke.MethodType

import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.sameClass
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.sameClasses
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Guards produced for monomorphic indy sites must reject a changed argument
 * class or a newly-null argument without allocating an {@code Object[]} collector
 * on the 1–4 arity hot path.
 */
final class IndySameClassesGuardTest {

    @Test
    void 'sameClass rejects null and class change'() {
        assertTrue sameClass(String, 'a')
        assertFalse sameClass(String, 1)
        assertFalse sameClass(String, null)
    }

    @Test
    void 'array sameClasses covers empty match null and mismatch'() {
        assertTrue sameClasses(new Class[0], new Object[0])
        assertTrue sameClasses([String, Integer] as Class[], ['a', 1] as Object[])
        assertFalse sameClasses([String, Integer] as Class[], ['a', null] as Object[])
        assertFalse sameClasses([String, Integer] as Class[], [null, 1] as Object[])
        assertFalse sameClasses([String, Integer] as Class[], ['a', 1L] as Object[])
        assertFalse sameClasses([String, Integer] as Class[], [1, 1] as Object[])
    }

    @Test
    void 'arity-2 sameClasses covers each conjunct'() {
        assertTrue sameClasses(String, Integer, 'a', 1)
        assertFalse sameClasses(String, Integer, null, 1)
        assertFalse sameClasses(String, Integer, 'a', null)
        assertFalse sameClasses(String, Integer, 1, 1)
        assertFalse sameClasses(String, Integer, 'a', 1L)
    }

    @Test
    void 'arity-3 sameClasses covers each conjunct'() {
        assertTrue sameClasses(String, Integer, Long, 'a', 1, 2L)
        assertFalse sameClasses(String, Integer, Long, null, 1, 2L)
        assertFalse sameClasses(String, Integer, Long, 'a', null, 2L)
        assertFalse sameClasses(String, Integer, Long, 'a', 1, null)
        assertFalse sameClasses(String, Integer, Long, 1, 1, 2L)
        assertFalse sameClasses(String, Integer, Long, 'a', 1L, 2L)
        assertFalse sameClasses(String, Integer, Long, 'a', 1, 2)
    }

    @Test
    void 'arity-4 sameClasses covers each conjunct'() {
        assertTrue sameClasses(String, Integer, Long, Double, 'a', 1, 2L, 3d)
        assertFalse sameClasses(String, Integer, Long, Double, null, 1, 2L, 3d)
        assertFalse sameClasses(String, Integer, Long, Double, 'a', null, 2L, 3d)
        assertFalse sameClasses(String, Integer, Long, Double, 'a', 1, null, 3d)
        assertFalse sameClasses(String, Integer, Long, Double, 'a', 1, 2L, null)
        assertFalse sameClasses(String, Integer, Long, Double, 1, 1, 2L, 3d)
        assertFalse sameClasses(String, Integer, Long, Double, 'a', 1L, 2L, 3d)
        assertFalse sameClasses(String, Integer, Long, Double, 'a', 1, 2, 3d)
        assertFalse sameClasses(String, Integer, Long, Double, 'a', 1, 2L, 3f)
    }

    @Test
    void 'sameClassesGuard arity 0 to 5 matches collector semantics'() {
        def g0 = Selector.sameClassesGuard(new Object[0], MethodType.methodType(Object))
        assertEquals(MethodType.methodType(boolean), g0.type())
        assertTrue((boolean) g0.invokeWithArguments())

        def g1 = Selector.sameClassesGuard(['recv'] as Object[], MethodType.methodType(Object, Object))
        assertEquals(MethodType.methodType(boolean, Object), g1.type())
        assertTrue((boolean) g1.invokeWithArguments('recv'))
        assertFalse((boolean) g1.invokeWithArguments(1))
        assertFalse((boolean) g1.invokeWithArguments((Object) null))

        def g2 = Selector.sameClassesGuard(['recv', 'arg'] as Object[], MethodType.methodType(Object, Object, Object))
        assertEquals(MethodType.methodType(boolean, Object, Object), g2.type())
        assertTrue((boolean) g2.invokeWithArguments('recv', 'arg'))
        assertFalse((boolean) g2.invokeWithArguments('recv', 1))
        assertFalse((boolean) g2.invokeWithArguments('recv', null))
        assertFalse((boolean) g2.invokeWithArguments(1, 'arg'))

        def g3 = Selector.sameClassesGuard(['r', 1, 2L] as Object[], MethodType.methodType(Object, Object, Object, Object))
        assertTrue((boolean) g3.invokeWithArguments('r', 1, 2L))
        assertFalse((boolean) g3.invokeWithArguments('r', 1, null))
        assertFalse((boolean) g3.invokeWithArguments('r', null, 2L))

        def g4 = Selector.sameClassesGuard(['r', 1, 2L, 3d] as Object[], MethodType.methodType(Object, Object, Object, Object, Object))
        assertTrue((boolean) g4.invokeWithArguments('r', 1, 2L, 3d))
        assertFalse((boolean) g4.invokeWithArguments('r', 1, 2L, 'x'))
        assertFalse((boolean) g4.invokeWithArguments(null, 1, 2L, 3d))

        def g5 = Selector.sameClassesGuard(['r', 1, 2L, 3d, 'z'] as Object[],
                MethodType.methodType(Object, Object, Object, Object, Object, Object))
        assertTrue((boolean) g5.invokeWithArguments('r', 1, 2L, 3d, 'z'))
        assertFalse((boolean) g5.invokeWithArguments('r', 1, 2L, 3d, 0))
        assertFalse((boolean) g5.invokeWithArguments('r', 1, 2L, 3d, null))
    }

    @Test
    void 'sameClassesGuard asTypes primitive parameters the way indy sites do'() {
        def g = Selector.sameClassesGuard(['r', 1] as Object[], MethodType.methodType(Object, Object, int))
        assertEquals(MethodType.methodType(boolean, Object, int), g.type())
        // Object[] form hits MethodHandle.invokeWithArguments(Object[]), which
        // asTypes/unboxes; the Groovy (Object...) call would go through the MOP.
        assertTrue((boolean) g.invokeWithArguments(['r', 1] as Object[]))
        assertFalse((boolean) g.invokeWithArguments([1, 1] as Object[]))
        assertFalse((boolean) g.invokeWithArguments([null, 1] as Object[]))
    }

    @Test
    void 'dynamic call site relinks when an argument class changes'() {
        def adder = new Adder()
        def acc = 0
        20.times { acc += adder.add(it, it + 1) }
        acc += adder.add(100L, 3L)
        assert acc == (0..19).sum { it + it + 1 } + 103L
    }

    static class Adder {
        def add(a, b) { a + b }
    }
}
