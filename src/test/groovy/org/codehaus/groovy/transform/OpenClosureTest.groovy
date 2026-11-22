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
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GEP-27 <b>open closures</b> (Groovy 7, enabled by {@code groovy.closure.open}): free-name closures — which GROOVY-12151 packing must
 * decline — hoist with their free reads/calls rewritten into calls on a leading
 * {@code Resolver} parameter, and a {@code ClassicResolver} restores the full
 * owner/delegate/resolveStrategy contract. The headline assertions: classic-identical
 * behaviour (parity is checked against the classed compilation of the same source, not
 * against hardcoded expectations), no per-literal class, and the SAME per-class dispatch
 * table serving packed and open bodies side by side.
 */
final class OpenClosureTest {

    private static final String OPEN = 'groovy.closure.open'

    // ---------------------------------------------------------------------------------------

    /** Runs the same source classed (open closures off) and open (on) and returns both results. */
    private static List bothWays(String src, String tail) {
        [evalWith(false, src, tail), evalWith(true, src, tail)]
    }

    private static Object evalWith(boolean open, String src, String tail) {
        String previous = System.getProperty(OPEN)
        if (open) System.setProperty(OPEN, 'true') else System.clearProperty(OPEN)
        try {
            new GroovyShell().evaluate(src + '\n' + tail)
        } catch (Throwable t) {
            t.getClass().simpleName // exception parity: compare by type
        } finally {
            if (previous != null) System.setProperty(OPEN, previous) else System.clearProperty(OPEN)
        }
    }

    private static List<String> classNamesWith(boolean open, String src) {
        String previous = System.getProperty(OPEN)
        if (open) System.setProperty(OPEN, 'true') else System.clearProperty(OPEN)
        try {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('Src.groovy', src)
            cu.compile(Phases.CLASS_GENERATION)
            cu.classes.collect { it.name }.sort()
        } finally {
            if (previous != null) System.setProperty(OPEN, previous) else System.clearProperty(OPEN)
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
        // an open closure is an ordinary fixed-arity family member (here implicit it)
        assertEquals('org.codehaus.groovy.runtime.PackedClosure$FixedIt',
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
            'TO_SELF -> exception parity': '''new Host().run { c ->
                c.resolveStrategy = Closure.TO_SELF
                c.delegate = new Expando(greeting: 'hi', greet: { -> 'HI' })
            }''',
        ].each { desc, tail ->
            def (classed, open) = bothWays(HOST, tail)
            assertEquals(classed, open, "parity: $desc")
        }
    }

    @Test
    void nestedClosuresDeclineTheOpenPathWithParity() {
        // a body containing a closure literal is not open-rewritable, and a closure
        // nested in another stays classed (owner retargeting is later work). Both closures here
        // would otherwise qualify, so this pins the decline; the rows check that the declined
        // shape still behaves as classic -- inner free names resolving through the outer closure's
        // own strategy and delegate -- and become real open-path parity rows once nesting packs.
        String src = '''
            class N {
                def greeting = 'hello'
                def greet() { greeting + '!' }
                def run(Closure outerSetup = null, Closure innerSetup = null) {
                    def outer = { ->
                        def inner = { greet() + ' ' + greeting }
                        if (innerSetup) innerSetup(inner)
                        greeting + ': ' + inner()
                    }
                    if (outerSetup) outerSetup(outer)
                    outer()
                }
            }
        '''
        def names = classNamesWith(true, src)
        assertTrue(names.any { it.endsWith('_run_closure1') } && names.any { it.endsWith('_closure2') },
                "nested closures stay classed with open closures enabled: $names")
        String hi = "new Expando(greeting: 'hi', greet: { -> 'HI' })"
        String bye = "new Expando(greeting: 'bye', greet: { -> 'BYE' })"
        [
            'plain owner resolution'            : 'new N().run()',
            'outer DELEGATE_FIRST'              : "new N().run({ c -> c.resolveStrategy = Closure.DELEGATE_FIRST; c.delegate = $hi })",
            'outer DELEGATE_ONLY'               : "new N().run({ c -> c.resolveStrategy = Closure.DELEGATE_ONLY; c.delegate = $hi })",
            'outer delegate, default strategy'  : "new N().run({ c -> c.delegate = $hi })",
            'inner DELEGATE_FIRST'              : "new N().run(null, { c -> c.resolveStrategy = Closure.DELEGATE_FIRST; c.delegate = $bye })",
            'inner OWNER_ONLY, outer DELEGATE_FIRST': """new N().run(
                { c -> c.resolveStrategy = Closure.DELEGATE_FIRST; c.delegate = $hi },
                { c -> c.resolveStrategy = Closure.OWNER_ONLY; c.delegate = $bye })""",
            'inner DELEGATE_ONLY missing -> exception parity':
                    "new N().run(null, { c -> c.resolveStrategy = Closure.DELEGATE_ONLY; c.delegate = new Expando() })",
        ].each { desc, tail ->
            def (classed, open) = bothWays(src, tail)
            assertEquals(classed, open, "nested parity: $desc")
        }
    }

    @Test
    void missingHandlerParity() {
        // methodMissing/propertyMissing on the owner and on the delegate take part in resolution
        // exactly as they do for a generated closure class
        String src = '''
            class M {
                def methodMissing(String name, args) { "owner.$name" }
                def propertyMissing(String name) { "owner.$name" }
                def run(Closure setup = null) {
                    def c = { shout() + ' ' + colour }
                    if (setup) setup(c)
                    c()
                }
            }
            class D {
                def methodMissing(String name, args) { "delegate.$name" }
                def propertyMissing(String name) { "delegate.$name" }
            }
            class Plain {}
        '''
        assertPacksOpen(src)
        [
            'owner handlers'                                : 'new M().run()',
            'OWNER_FIRST: owner handler wins over delegate' : 'new M().run { c -> c.delegate = new D() }',
            'DELEGATE_FIRST: delegate handler'              : 'new M().run { c -> c.resolveStrategy = Closure.DELEGATE_FIRST; c.delegate = new D() }',
            'DELEGATE_FIRST: no delegate handler -> owner'  : 'new M().run { c -> c.resolveStrategy = Closure.DELEGATE_FIRST; c.delegate = new Plain() }',
            'DELEGATE_ONLY: delegate handler'               : 'new M().run { c -> c.resolveStrategy = Closure.DELEGATE_ONLY; c.delegate = new D() }',
            'DELEGATE_ONLY: no delegate handler -> exception parity':
                    'new M().run { c -> c.resolveStrategy = Closure.DELEGATE_ONLY; c.delegate = new Plain() }',
            'OWNER_ONLY: owner handler despite delegate'    : 'new M().run { c -> c.resolveStrategy = Closure.OWNER_ONLY; c.delegate = new D() }',
        ].each { desc, tail ->
            def (classed, open) = bothWays(src, tail)
            assertEquals(classed, open, "missing-handler parity: $desc")
        }
    }

    @Test
    void scriptOwnerParity() {
        // a script as the owner: free names reach script methods and binding variables, for
        // reads and writes (go, not run, which would clash with Script.run())
        String src = '''
            greeting = 'hello'
            def greet() { greeting + '!' }
            def go(Closure setup = null) {
                def c = { greet() + ' ' + greeting }
                if (setup) setup(c)
                c()
            }
            def rename() { def w = { greeting = 'bye' }; w(); greeting }
        '''
        assertPacksOpen(src)
        [
            'plain owner resolution'  : 'go()',
            'binding write'           : 'rename()',
            'DELEGATE_FIRST intercept': "go { c -> c.resolveStrategy = Closure.DELEGATE_FIRST; c.delegate = new Expando(greeting: 'hi', greet: { -> 'HI' }) }",
            'DELEGATE_ONLY'           : "go { c -> c.resolveStrategy = Closure.DELEGATE_ONLY; c.delegate = new Expando(greeting: 'hi', greet: { -> 'HI' }) }",
            'OWNER_ONLY under delegate': "go { c -> c.resolveStrategy = Closure.OWNER_ONLY; c.delegate = new Expando(greeting: 'hi', greet: { -> 'HI' }) }",
            'DELEGATE_ONLY missing -> exception parity': 'go { c -> c.resolveStrategy = Closure.DELEGATE_ONLY; c.delegate = new Expando() }',
        ].each { desc, tail ->
            def (classed, open) = bothWays(src, tail)
            assertEquals(classed, open, "script-owner parity: $desc")
        }
    }

    /** Guards the parity rows against passing trivially: every closure in {@code src} must pack open. */
    private static void assertPacksOpen(String src) {
        def names = classNamesWith(true, src)
        assertTrue(names.every { !it.contains('_closure') }, "every closure should pack open: $names")
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
        String previous = System.getProperty(OPEN)
        System.setProperty(OPEN, 'true')
        try {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('Mixed.groovy', src)
            cu.compile(Phases.CLASS_GENERATION)
            def names = cu.classes.collect { it.name }.sort()
            assertEquals(['Mixed'], names, "both closures should hoist, no classes: $names")
            byte[] bytes = cu.classes.find { it.name == 'Mixed' }.bytes
            def methods = []
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                MethodVisitor visitMethod(int a, String n, String d, String sg, String[] e) {
                    methods << n; null
                }
            }, ClassReader.SKIP_CODE)
            assertEquals(2, methods.count { it.startsWith('$packed$closure$') },
                    "two hoisted bodies in one class: $methods")
            assertEquals(1, methods.count { it == '$packedDispatch$' },
                    "ONE shared dispatch table serves both: $methods")
        } finally {
            if (previous != null) System.setProperty(OPEN, previous) else System.clearProperty(OPEN)
        }
    }

    @Test
    void openClosureIsARealClosure() {
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
    void loneObjectArrayArgumentIsNotSpread() {
        // a single Object[] argument is ONE argument, as on a generated closure class: an open
        // closure is an ordinary fixed-arity family member, with no varargs doCall to spread it
        String src = '''
            class A {
                def suffix = '!'
                def run() {
                    def c = { x -> x.getClass().simpleName + suffix }   // free name -> open
                    Object[] arr = ['a', 'b'] as Object[]
                    [c(arr), c.call(arr)]
                }
            }
        '''
        def (classed, open) = bothWays(src, 'new A().run()')
        assertEquals(['Object[]!', 'Object[]!'], classed)
        assertEquals(classed, open)
    }

    @Test
    void openCreationSiteReferencesOnlyTheResolverInterface() {
        // the open closure is built by ScriptBytecodeAdapter.openClosure, so the host class names
        // neither OpenClosure's runtime classes nor the PackedClosure family -- only the Resolver
        // interface its hoisted body takes as its leading parameter
        String previous = System.getProperty(OPEN)
        System.setProperty(OPEN, 'true')
        try {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('Src.groovy', HOST)
            cu.compile(Phases.CLASS_GENERATION)
            String pool = new String(cu.classes.find { it.name == 'Host' }.bytes, 'ISO-8859-1')
            assertTrue(pool.contains('openClosure'), 'creation goes through the factory')
            assertTrue(pool.contains('org/codehaus/groovy/runtime/OpenClosure$Resolver'))
            ['OpenClosure$ClassicResolver', 'PackedClosure', 'GeneratedDispatcher$Bundle;I'].each {
                assertTrue(!pool.contains(it), "host class must not reference $it")
            }
        } finally {
            if (previous != null) System.setProperty(OPEN, previous) else System.clearProperty(OPEN)
        }
    }

    @Test
    void noFreeNameClosuresKeepTheOrdinaryPackedPath() {
        // the open-closure property alone must not change the no-free-name subset's treatment
        String src = 'class P { def m(List<Integer> xs) { xs.collect { it * 2 } } }'
        def names = classNamesWith(true, src)
        assertTrue(names.any { it.contains('_closure') },
                "without the pack flag, a no-free-name closure stays classed even with open closures enabled: $names")
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
        // ++/-- need temp-value plumbing open closures do not carry; those closures stay classed
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
        String previous = System.getProperty(OPEN)
        System.setProperty(OPEN, 'true')
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
            if (previous != null) System.setProperty(OPEN, previous) else System.clearProperty(OPEN)
        }
    }
}
