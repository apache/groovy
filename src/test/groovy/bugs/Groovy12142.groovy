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
package bugs

import org.codehaus.groovy.reflection.GroovyClassValue
import org.junit.jupiter.api.Test

import java.lang.ref.WeakReference

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12142: Groovy must not pin container class loaders.
 * <ul>
 * <li>the parser's DFA-cache handling must not start per-manager threads
 *     (a never-terminating thread pins its defining loader forever)</li>
 * <li>the map-based {@code GroovyClassValue} fallback
 *     ({@code -Dgroovy.use.classvalue=false}) must hold classes weakly</li>
 * </ul>
 */
final class Groovy12142 {

    @Test
    void testNoLoaderPinningBackgroundThreadsAfterParseAndDispatch() {
        // exercise the lexer/parser ATN managers and indy call-site linking
        new GroovyShell().evaluate('[1, 2, 3].collect { it * 2 }.sum()')
        def rogue = Thread.allStackTraces.keySet().findAll {
            it.name.contains('DFA-cache-cleaner') || it.name.contains('PIC-Cleaner')
        }
        assertTrue(rogue.isEmpty(), "runtime spawned loader-pinning threads: ${rogue*.name}")
    }

    @Test
    void testMapBasedClassValueComputesOncePerClassAndSupportsRemove() {
        def computations = []
        def gcv = newMapBasedClassValue { Class<?> c -> computations << c; c.simpleName }
        assertEquals('String', gcv.get(String))
        assertEquals('String', gcv.get(String))
        assertEquals([String], computations, 'value must be computed once per class')
        gcv.remove(String)
        assertEquals('String', gcv.get(String))
        assertEquals([String, String], computations, 'remove must allow recompute')
        gcv.remove(Integer) // absent key: no-op
    }

    @Test
    void testMapBasedClassValueUsesIdentityAndIndependentValues() {
        def gcv = newMapBasedClassValue { Class<?> c -> new Object() }
        def v1 = gcv.get(String)
        assertSame(v1, gcv.get(String))
        assertNotSame(v1, gcv.get(Integer))
    }

    @Test
    void testMapBasedClassValueDoesNotPinCollectedClasses() {
        // the compute function must not touch the class dynamically: a dynamic
        // property access on the Class object would create a ClassValue-backed
        // ClassInfo for it in this JVM (default groovy.use.classvalue=true),
        // self-pinning the class via JDK-8136353 — the very bug under test
        def gcv = newMapBasedClassValue { Class<?> c -> 'associated' }
        def clsRef = loadThrowawayClass(gcv)
        boolean collected = false
        for (int i = 0; i < 100 && !collected; i++) {
            System.gc()
            if (i == 10) forceSoftReferenceClearing()
            collected = clsRef.get() == null
            if (!collected) Thread.sleep(10)
        }
        assertTrue(collected, 'map-based GroovyClassValue must not pin a discarded class')
    }

    @Test
    void testClassInfoRemoveSweepIsSafeToRunRepeatedly() {
        // the documented container-shutdown mitigation must not disturb a live runtime
        def infoClass = Class.forName('org.codehaus.groovy.reflection.ClassInfo')
        def all = new ArrayList(infoClass.getAllClassInfo())
        assertFalse(all.isEmpty())
        // remove and re-touch one entry for a stable class
        infoClass.remove(StringBuilder)
        assert new StringBuilder('a').append('b').toString() == 'ab' // metaclass re-created on demand
    }

    @Test
    void testScaffoldControl_classCollectsWithNoAssociationAtAll() {
        def clsRef = loadThrowawayClass(null)
        boolean collected = false
        for (int i = 0; i < 100 && !collected; i++) {
            System.gc()
            if (i == 10) forceSoftReferenceClearing()
            collected = clsRef.get() == null
            if (!collected) Thread.sleep(10)
        }
        assertTrue(collected, 'scaffolding itself pins the class — test harness problem, not impl')
    }

    /** Allocates until OutOfMemoryError so the JVM clears soft references first. */
    private static void forceSoftReferenceClearing() {
        try {
            def hog = []
            while (true) {
                hog << new byte[(int) Math.max(1024L, Runtime.runtime.freeMemory() >> 2)]
            }
        } catch (OutOfMemoryError expected) {
            // soft refs are guaranteed cleared before OOME
        }
    }

    private static GroovyClassValue newMapBasedClassValue(Closure compute) {
        def impl = Class.forName('org.codehaus.groovy.reflection.GroovyClassValueMapBased')
        def ctor = impl.getDeclaredConstructor(GroovyClassValue.ComputeValue)
        ctor.accessible = true
        ctor.newInstance(compute as GroovyClassValue.ComputeValue)
    }

    /**
     * Associates a value with a class from a throwaway loader, then drops the
     * loader. The class is defined from raw ASM-generated bytes so no Groovy
     * runtime machinery (ClassInfo, CachedClass) can retain it — only the
     * GroovyClassValue under test holds an association.
     */
    private static WeakReference<Class<?>> loadThrowawayClass(GroovyClassValue gcv) {
        def cw = new org.objectweb.asm.ClassWriter(0)
        // V11 (the Groovy 5 minimum): loadable on every JRE the test may run on
        cw.visit(org.objectweb.asm.Opcodes.V11, org.objectweb.asm.Opcodes.ACC_PUBLIC,
                'ThrowawayClassValueHost', null, 'java/lang/Object', null)
        cw.visitEnd()
        byte[] bytes = cw.toByteArray()
        def loader = new ClassLoader(Groovy12142.classLoader) {
            Class<?> define() {
                defineClass('ThrowawayClassValueHost', bytes, 0, bytes.length)
            }
        }
        Class<?> cls = loader.define()
        if (gcv != null) gcv.get(cls)
        return new WeakReference<Class<?>>(cls)
    }
}
