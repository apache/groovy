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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.runtime.OpenClosure
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GEP-27 <b>OpenClosure spike</b> (Groovy 7 reference implementation, behind
 * {@code groovy.spike.openclosure}): free-name closures — which GROOVY-12151 packing must
 * decline — hoist with their free reads/calls rewritten into calls on a leading
 * {@code Resolver} parameter, and a {@code ClassicResolver} restores the full
 * owner/delegate/resolveStrategy contract. The headline assertions: classic-identical
 * behaviour (parity is checked against the classed compilation of the same source, not
 * against hardcoded expectations), no per-literal class, and the SAME per-class dispatch
 * table serving packed and open bodies side by side.
 */
final class OpenClosureSpikeTest {

    private static final String SPIKE = 'groovy.spike.openclosure'

    // ---------------------------------------------------------------------------------------

    /** Runs the same source classed (spike off) and open (spike on) and returns both results. */
    private static List bothWays(String src, String tail) {
        [evalWith(false, src, tail), evalWith(true, src, tail)]
    }

    private static Object evalWith(boolean spike, String src, String tail) {
        String previous = System.getProperty(SPIKE)
        if (spike) System.setProperty(SPIKE, 'true') else System.clearProperty(SPIKE)
        try {
            new GroovyShell().evaluate(src + '\n' + tail)
        } catch (Throwable t) {
            t.getClass().simpleName // exception parity: compare by type
        } finally {
            if (previous != null) System.setProperty(SPIKE, previous) else System.clearProperty(SPIKE)
        }
    }

    private static List<String> classNamesWith(boolean spike, String src) {
        String previous = System.getProperty(SPIKE)
        if (spike) System.setProperty(SPIKE, 'true') else System.clearProperty(SPIKE)
        try {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('Src.groovy', src)
            cu.compile(Phases.CLASS_GENERATION)
            cu.classes.collect { it.name }.sort()
        } finally {
            if (previous != null) System.setProperty(SPIKE, previous) else System.clearProperty(SPIKE)
        }
    }

    private static final String HOST = '''
        class Host {
            def greeting = 'hello'
            def greet() { greeting + '!' }
            def kind() { def c = { greet() }; c.getClass().name }
            def run(Closure setup = null) {
                def c = { greet() + ' ' + greeting }
                if (setup) setup(c)
                c()
            }
        }
    '''

    @Test
    void freeNameClosurePacksOpenWithNoClosureClass() {
        def names = classNamesWith(true, HOST)
        assertTrue(names.every { !it.contains('_closure') },
                "the free-name closures should pack open (no closure classes): $names")
        assertEquals('org.codehaus.groovy.runtime.OpenClosure$AsClosure',
                evalWith(true, HOST, 'new Host().kind()'))
    }

    @Test
    void classicParityAcrossStrategiesAndDelegates() {
        // the parity matrix: every scenario compares open vs classed on the SAME source
        [
            'plain owner resolution'  : 'new Host().run()',
            'DELEGATE_FIRST intercept': '''new Host().run { c ->
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.delegate = new Expando(greeting: 'hi', greet: { -> 'HI' })
            }''',
            'DELEGATE_FIRST partial (method only on delegate)': '''new Host().run { c ->
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.delegate = new Expando(greet: { -> 'HI' })
            }''',
            'DELEGATE_ONLY'           : '''new Host().run { c ->
                c.resolveStrategy = Closure.DELEGATE_ONLY
                c.delegate = new Expando(greeting: 'hi', greet: { -> 'HI' })
            }''',
            'DELEGATE_ONLY missing -> exception parity': '''new Host().run { c ->
                c.resolveStrategy = Closure.DELEGATE_ONLY
                c.delegate = new Expando()
            }''',
            'OWNER_ONLY under delegate': '''new Host().run { c ->
                c.resolveStrategy = Closure.OWNER_ONLY
                c.delegate = new Expando(greeting: 'hi', greet: { -> 'HI' })
            }''',
        ].each { desc, tail ->
            def (classed, open) = bothWays(HOST, tail)
            assertEquals(classed, open, "parity: $desc")
        }
    }

    @Test
    void oneDispatchTableServesPackedAndOpenBodiesSideBySide() {
        // the GROOVY-12151 alignment proof in bytecode: a no-free-name closure (ordinary packed,
        // needs the pack flag) and a free-name closure (open) hoist into the SAME class table
        String src = '''
            @groovy.transform.PackedClosures
            class Mixed {
                def pure(List<Integer> xs) { xs.collect { it * 2 } }
                def greetingWord = 'hey'
                def open() { def c = { greetingWord }; c() }
            }
        '''
        String previous = System.getProperty(SPIKE)
        System.setProperty(SPIKE, 'true')
        try {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('Mixed.groovy', src)
            cu.compile(Phases.CLASS_GENERATION)
            def names = cu.classes.collect { it.name }.sort()
            assertEquals(['Mixed'], names, "both closures should hoist, no classes: $names")
            byte[] bytes = cu.classes.find { it.name == 'Mixed' }.bytes
            def methods = []
            new org.objectweb.asm.ClassReader(bytes).accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
                org.objectweb.asm.MethodVisitor visitMethod(int a, String n, String d, String sg, String[] e) {
                    methods << n; null
                }
            }, org.objectweb.asm.ClassReader.SKIP_CODE)
            assertEquals(2, methods.count { it.startsWith('$packed$closure$') },
                    "two hoisted bodies in one class: $methods")
            assertEquals(1, methods.count { it == '$packedDispatch$' },
                    "ONE shared dispatch table serves both: $methods")
        } finally {
            if (previous != null) System.setProperty(SPIKE, previous) else System.clearProperty(SPIKE)
        }
    }

    @Test
    void asClosureIsARealClosure() {
        // GDK iteration drives it; curry works; it is Closure-typed
        String src = '''
            class H {
                def suffix() { '!' }
                def run() {
                    def c = { x -> x + suffix() }          // free call -> open
                    def curried = c.curry('hi')
                    [['a','b'].collect(c), curried(), c instanceof Closure]
                }
            }
        '''
        def (classed, open) = bothWays(src, 'new H().run()')
        assertEquals(classed, open)
    }

    @Test
    void noFreeNameClosuresKeepTheOrdinaryPackedPath() {
        // the spike property alone must not change the no-free-name subset's treatment
        String src = 'class P { def m(List<Integer> xs) { xs.collect { it * 2 } } }'
        def names = classNamesWith(true, src)
        assertTrue(names.any { it.contains('_closure') },
                "without the pack flag, a no-free-name closure stays classed even under the spike: $names")
    }

    @Test
    void freeNameWriteParity() {
        // the resolver's setProperty leg: plain and compound writes to free names, resolved
        // per strategy exactly as classic closures do (read-then-write for compounds), with
        // assignment-expression value semantics preserved
        String src = '''
            class H {
                def tally = 0
                def log = ''
                def run(Closure setup = null) {
                    def c = { it -> tally += it; log = log + "[$it]" }
                    if (setup) setup(c)
                    [1, 2, 3].each(c)
                    [tally, log]
                }
                def exprValue() { def c = { (tally = 5) }; c() }
            }
        '''
        [
            'owner writes'            : 'new H().run()',
            'assignment expression'   : 'new H().exprValue()',
            'DELEGATE_FIRST writes land on the delegate': '''
                def h = new H()
                def del = new Expando(tally: 100, log: 'D')
                h.run { c -> c.resolveStrategy = Closure.DELEGATE_FIRST; c.delegate = del }
                [del.tally, del.log, h.tally, h.log]
            ''',
            'DELEGATE_ONLY missing write -> exception parity': '''
                new H().run { c ->
                    c.resolveStrategy = Closure.DELEGATE_ONLY
                    c.delegate = new Object()
                }
            ''',
        ].each { desc, tail ->
            def (classed, open) = bothWays(src, tail)
            assertEquals(classed, open, "write parity: $desc")
        }
        // and the write shape genuinely takes the open path
        def names = classNamesWith(true, src)
        assertTrue(names.every { !it.contains('_closure') }, "write closures should pack open: $names")
    }

    @Test
    void incrementOnFreeNameStillDeclines() {
        // ++/-- need temp-value plumbing the spike does not carry; those closures stay classed
        String src = 'class H { def n = 0; def run() { def c = { n++ }; c(); n } }'
        def names = classNamesWith(true, src)
        assertTrue(names.any { it.contains('_closure') }, "increment on a free name stays classed: $names")
        def (classed, open) = bothWays(src, 'new H().run()')
        assertEquals(classed, open)
    }

    @Test
    void pureCoreIsATemplateOverResolvers() {
        // the "free variables as input" reading: the SAME hoisted body under two different
        // resolvers means two different things -- the dynamic part supplied from outside
        String src = '''
            class T {
                def open() { def c = { -> greetingWord }; c() }   // compiled only to host the body
            }
        '''
        String previous = System.getProperty(SPIKE)
        System.setProperty(SPIKE, 'true')
        try {
            def shell = new GroovyShell()
            def clazz = shell.evaluate(src + '\nT')
            def accessor = clazz.getDeclaredMethod('$getPackedDispatchers$')
            accessor.accessible = true
            def bundle = accessor.invoke(null)
            def t = clazz.getDeclaredConstructor().newInstance()
            def english = [property: { n -> 'hello' }, invoke: { n, a -> null }] as OpenClosure.Resolver
            def french  = [property: { n -> 'bonjour' }, invoke: { n, a -> null }] as OpenClosure.Resolver
            def core1 = new OpenClosure(t, bundle, 0, [english] as Object[], new Class[0])
            def core2 = new OpenClosure(t, bundle, 0, [french] as Object[], new Class[0])
            assertEquals('hello', core1.call())
            assertEquals('bonjour', core2.call())
        } finally {
            if (previous != null) System.setProperty(SPIKE, previous) else System.clearProperty(SPIKE)
        }
    }
}
