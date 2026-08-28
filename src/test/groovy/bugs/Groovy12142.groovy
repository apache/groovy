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

    /**
     * The parse-count threshold and the GC canary must compose. They were mutually
     * exclusive, so setting {@code groovy.antlr4.cache.threshold} to bound the cache
     * silently switched off the only mechanism that responds to memory pressure.
     */
    @Test
    void testThresholdAndGcCanaryCompose() {
        // default: canary only, no deterministic ceiling
        assertTrue(gcCanaryEnabled(0L), 'canary must be on by default')
        assertEquals(0L, counterThreshold(0L), 'counter must be off by default')

        // a positive threshold adds a ceiling and must NOT disable the canary
        assertTrue(gcCanaryEnabled(25L), 'a threshold must not switch off the GC canary')
        assertEquals(25L, counterThreshold(25L))

        // a negative threshold remains the explicit "never clear" escape hatch
        assertFalse(gcCanaryEnabled(-1L), 'a negative threshold must disable clearing entirely')
        assertEquals(0L, counterThreshold(-1L), 'a negative threshold must not clear on a counter')
    }

    /**
     * With the default configuration the parser DFA cache must actually be released once
     * the JVM has cleared soft references — the state a heap-constrained build reaches
     * immediately before OutOfMemoryError.
     */
    @Test
    void testParserDfaCacheIsReleasedAfterSoftReferenceClearing() {
        (1..12).each { n -> parseVariedSource(n) }
        int populated = parserDfaStateCount()
        assertTrue(populated > 0, 'precondition: parsing must populate the parser DFA cache')

        forceSoftReferenceClearing()
        new GroovyShell().parse('1') // the clear happens on the next parse

        int afterClear = parserDfaStateCount()
        assertTrue(afterClear < populated,
                "parser DFA cache must shrink after soft references are cleared, was $populated now $afterClear")
    }

    /**
     * End-to-end guard for the composition, in a forked JVM because the threshold is read
     * into a static final at class-init. With a threshold set but not yet reached, the only
     * thing that can clear the cache is the GC canary — which a threshold used to disable.
     */
    @Test
    void testThresholdSetStillAllowsGcCanaryToClearCache() {
        def javaBin = new File(System.getProperty('java.home'), 'bin/java').absolutePath
        def proc = [javaBin, '-Xmx256m', '-Dgroovy.antlr4.cache.threshold=25',
                    '-cp', System.getProperty('java.class.path'),
                    'bugs.Groovy12142$ThresholdCanaryProbe'].execute()
        def out = new StringBuilder(), err = new StringBuilder()
        proc.waitForProcessOutput(out, err)
        assertEquals(0, proc.exitValue(),
                "a set threshold must not disable the GC canary (exit=${proc.exitValue()})\nout: $out\nerr: $err")
    }

    /**
     * Runs in a forked JVM with {@code groovy.antlr4.cache.threshold=25}. Parses fewer times
     * than the threshold, so the deterministic counter cannot fire; any shrink is the canary.
     * Exit 0 = cache released, 1 = not released, 2 = scaffolding failed to populate it.
     */
    static class ThresholdCanaryProbe {
        static void main(String[] args) {
            (1..12).each { n -> parseVariedSource(n) }
            int populated = parserDfaStateCount()
            if (populated <= 0) System.exit(2)

            forceSoftReferenceClearing()
            new GroovyShell().parse('1') // the canary is observed on the next parse

            int afterClear = parserDfaStateCount()
            System.err.println("populated=$populated afterClear=$afterClear")
            System.exit(afterClear < populated ? 0 : 1)
        }
    }

    /** Counts live states in the shared parser DFA cache. */
    private static int parserDfaStateCount() {
        def atn = org.apache.groovy.parser.antlr4.GroovyParser._ATN
        def dfas = atn.decisionToDFA.findAll { it != null }
        dfas.isEmpty() ? 0 : (int) dfas.sum { it.states.size() }
    }

    private static void parseVariedSource(int n) {
        new GroovyShell().parse """
            class Varied$n {
                static final Map<String, List<Integer>> SEED = [a: [1, 2, $n], b: [$n, 5]]
                def run$n(input) {
                    def acc = new HashMap<String, List<Integer>>()
                    def cl = { String k, List<Integer> xs -> xs.collect { it * $n }.findAll { it % 2 == 0 } }
                    def (first, second) = [acc.size(), SEED.size()]
                    def out = SEED.collectEntries { k, xs -> [(k): cl(k, xs)] }
                    def msg = "n=\${first} m=\${second} -> \${out?.size() ?: 0}"
                    def branch = switch (second) {
                        case 0 -> 'zero'
                        case 1..5 -> "small \$msg"
                        default -> out.keySet().sort()
                    }
                    return first > $n ? out*.key : [*out.values(), *SEED.b].flatten()
                }
            }
        """
    }

    private static boolean gcCanaryEnabled(long raw) {
        (boolean) invokeAtnManagerPolicy('gcCanaryEnabled', raw)
    }

    private static long counterThreshold(long raw) {
        (long) invokeAtnManagerPolicy('counterThreshold', raw)
    }

    private static Object invokeAtnManagerPolicy(String name, long raw) {
        def m = Class.forName('org.apache.groovy.parser.antlr4.internal.atnmanager.AtnManager')
                .getDeclaredMethod(name, long)
        m.accessible = true
        m.invoke(null, raw)
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
