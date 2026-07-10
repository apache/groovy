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
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests for {@link groovy.transform.PackedClosures}: eligible closure literals are
 * hoisted into synthetic methods on the enclosing class and replaced by a single shared
 * {@code PackedClosure} adapter, so no per-closure inner class is generated. Ineligible
 * closures are left exactly as they are today.
 */
final class PackedClosuresTransformTest {

    /** Compiles the source and returns the sorted names of every generated class. */
    private static List<String> generatedClassNames(String src) {
        def cu = new CompilationUnit(new CompilerConfiguration())
        cu.addSource('Sample.groovy', src)
        cu.compile(Phases.CLASS_GENERATION)
        cu.classes.collect { it.name }.sort()
    }

    private static int closureClassCount(List<String> names) {
        names.count { it.contains('_closure') }
    }

    /** Evaluates the source (which must end by yielding an instance) and returns it. */
    private static Object instance(String src) {
        new GroovyShell().evaluate(src)
    }

    private static final String SAMPLE = '''
        @groovy.transform.PackedClosures
        class Sample {
            String simple()          { [1, 2, 3].collect { it * 2 }.join(',') }
            String nested(Map m) {
                def out = []
                m.each { k, v -> [1, 2].each { i -> out << "${k}${v}${i}".toString() } }
                out.join(',')
            }
            int readCapture(List<Integer> xs, int base) { (xs.collect { it + base }.sum()) as int }
        }
    '''

    @Test
    void eligibleClosuresGenerateNoClosureClasses() {
        def names = generatedClassNames(SAMPLE)
        // Without @PackedClosures this same source produces 4 closure classes, including the
        // deeply-nested Sample$_nested_closure2$_closure4.
        assertEquals(['Sample'], names, "only the owner class should be generated, got: $names")
        assertEquals(0, closureClassCount(names))
    }

    @Test
    void packedClosuresBehaveIdenticallyToPlainOnes() {
        def plain = instance(SAMPLE.replace('@groovy.transform.PackedClosures', '') + '\n new Sample()')
        def packed = instance(SAMPLE + '\n new Sample()')

        assertEquals(plain.simple(), packed.simple())
        assertEquals('2,4,6', packed.simple())

        assertEquals(plain.nested([a: 1, b: 2]), packed.nested([a: 1, b: 2]))
        assertEquals('a11,a12,b21,b22', packed.nested([a: 1, b: 2])) // nested closures + captured k, v

        assertEquals(plain.readCapture([10, 20, 30], 5), packed.readCapture([10, 20, 30], 5))
        assertEquals(75, packed.readCapture([10, 20, 30], 5)) // captured 'base' read by value
    }

    @Test
    void writerModePacksMemoizeAndCapturedWriteButDeclinesDelegate() {
        // Each class mixes an always-eligible closure with a second one. In the writer path a
        // captured write is packed by threading a Reference, and memoize/curry operate on the shared
        // adapter (a real Closure), so those pack too - only a closure that needs a real delegate is
        // declined and kept as a class. Behavior is unchanged in every case.
        def cases = [
            [name: 'delegate (declines: needs a real delegate)',
             keptClasses: 1,
             src : '''@groovy.transform.PackedClosures
                      class X {
                        def eligible()   { [1, 2].collect { it + 1 } }
                        def ineligible() { def sb = new StringBuilder(); def c = { delegate.append('hi') }
                                           c.delegate = sb; c(); sb.toString() }
                      }''',
             expect: 'hi'],
            [name: 'memoize (packs: adapter is a real Closure)',
             keptClasses: 0,
             src : '''@groovy.transform.PackedClosures
                      class X {
                        def eligible()   { [1, 2].collect { it + 1 } }
                        def ineligible() { def c = { int n -> n * n }.memoize(); c(3) + c(3) }
                      }''',
             expect: 18],
            [name: 'capturedWrite (packs: Reference-threaded)',
             keptClasses: 0,
             src : '''@groovy.transform.PackedClosures
                      class X {
                        def eligible()   { [1, 2].collect { it + 1 } }
                        def ineligible() { int total = 0; [1, 2, 3].each { total += it }; total }
                      }''',
             expect: 6],
        ]
        cases.each { c ->
            def names = generatedClassNames(c.src)
            assertEquals(c.keptClasses, closureClassCount(names), "[${c.name}] closure classes kept: $names")
            def obj = instance(c.src + '\n new X()')
            assertEquals([2, 3], obj.eligible(), "[${c.name}] eligible closure result")
            assertEquals(c.expect, obj.ineligible(), "[${c.name}] second closure result")
        }
    }

    @Test
    void escapingClosuresAreDeclinedAtCompileTime() {
        // A packed closure is bound to the owner; a shared adapter fails fast if a delegate is later
        // set on it. To avoid that surprise for the clearest cases, a closure that visibly escapes the
        // method - stored into a field/property/index, returned, appended, or placed in a collection
        // literal - is declined at compile time and kept as a normal closure class. Owner-local,
        // non-escaping closures still pack. Behavior is unchanged either way.
        String src = '''@groovy.transform.PackedClosures
            class E {
                Closure field
                void toProperty(Map attrs) { attrs.optionValue = { it * 2 } }   // property/index store
                void toField()             { field = { it + 1 } }               // field store
                Closure returned()         { return { it - 1 } }                // returned
                void appended(List sink)   { sink << { it } }                   // appended
                int usesEach(List xs)      { int t = 0; xs.each { t += it }; t } // owner-local -> packs
                List maps(List xs)         { xs.collect { it * 3 } }            // owner-local -> packs
            }'''
        def names = generatedClassNames(src)
        // the four escaping closures are kept as classes; the two owner-local ones are packed away
        assertEquals(4, closureClassCount(names), "escaping closures should be declined: $names")

        def e = instance(src + '\n new E()')
        def attrs = [:]; e.toProperty(attrs)
        assertEquals(42, attrs.optionValue(21))      // declined closure still works
        assertEquals(9, e.returned()(10))
        assertEquals(6, e.usesEach([1, 2, 3]))       // packed closure still works
        assertEquals([3, 6, 9], e.maps([1, 2, 3]))
    }

    /**
     * Under {@code @CompileStatic} the transform applies a cast-to-context heuristic: the hoisted
     * method is marked {@code @CompileStatic(SKIP)} (so its Object-typed body is checked dynamically),
     * and where a packed result flows into a syntactically-declared target — a typed method return or
     * a typed local declaration — the result is cast to that type so static type checking still infers
     * correctly (e.g. {@code collect(...)} → {@code List<Integer>}). This covers a large fraction of
     * real usage; the boundary is exercised by {@link #compileStaticBoundaryHasNoDeclaredTarget}.
     */
    @Test
    void compileStaticPacksResultsWithDeclaredTargets() {
        String src = '''import groovy.transform.CompileStatic
            @CompileStatic
            @groovy.transform.PackedClosures
            class S {
                List<Integer> doubled(List<Integer> xs)     { xs.collect { Integer it -> it * 2 } }          // implicit-return target
                List<String>  tag(List<Integer> xs, String p){ xs.collect { Integer n -> p + n } }
                String        join(List<Integer> xs)        { List<String> ss = xs.collect { Integer it -> "v$it".toString() }; ss.join(',') } // typed-local target
                int           total(List<Integer> xs, int base){ int t = 0; xs.each { t += it + base }; t }   // captured write, packed via Reference
            }'''
        // every closure packs, including the captured-write one (Reference-threaded): no class remains
        assertEquals(['S'], generatedClassNames(src))

        def s = instance(src + '\n new S()')
        assertEquals([2, 4, 6], s.doubled([1, 2, 3]))
        assertEquals(['x1', 'x2'], s.tag([1, 2], 'x'))
        assertEquals('v1,v2,v3', s.join([1, 2, 3]))
        assertEquals(36, s.total([1, 2, 3], 10))
    }

    /**
     * A packed result passed straight into a typed argument (no syntactic target declaration). The
     * writer path infers through the {@code collect(...)} call, so this shape packs and behaves
     * correctly. It is kept as a regression guard: the general {@code @CompileStatic} soundness story
     * (reading already-computed inferred types rather than a cast-to-context heuristic) is still the
     * Groovy 7 work per GEP-27, but this common no-declared-target shape must not regress into a
     * compile error.
     */
    @Test
    void compileStaticPacksResultPassedAsArgument() {
        String passedAsArg = '''import groovy.transform.CompileStatic
            @CompileStatic
            @groovy.transform.PackedClosures
            class Z {
                int need(List<Integer> ys) { ys.sum() as int }
                int m(List<Integer> xs)    { need(xs.collect { Integer it -> it * 2 }) }
            }'''
        assertEquals(['Z'], generatedClassNames(passedAsArg))   // closure packs, no class remains
        assertEquals(12, instance(passedAsArg + '\n new Z()').m([1, 2, 3]))
    }

    /**
     * The non-{@code @CompileStatic} counterpart to the two {@code compileStatic*} tests above (the
     * {@code SAMPLE}-based tests already cover dynamic compilation generally). The same owner-local
     * closure shapes — a returned result, a result passed straight as an argument, and a captured
     * write — also pack under dynamic compilation via the {@code @PackedClosures} trust path, leaving
     * no closure class and behaving identically.
     */
    @Test
    void dynamicCompilationPacksTheSameClosureShapes() {
        String src = '''@groovy.transform.PackedClosures
            class D {
                def doubled(List xs)         { xs.collect { it * 2 } }                    // result returned
                int need(List ys)            { ys.sum() }
                int passedAsArg(List xs)     { need(xs.collect { it * 2 }) }              // result passed as arg
                int total(List xs, int base) { int t = 0; xs.each { t += it + base }; t } // captured write
            }'''
        assertEquals(['D'], generatedClassNames(src), 'every closure should pack: no class remains')
        def d = instance(src + '\n new D()')
        assertEquals([2, 4, 6], d.doubled([1, 2, 3]))
        assertEquals(12, d.passedAsArg([1, 2, 3]))
        assertEquals(36, d.total([1, 2, 3], 10))
    }

    @Test
    void inheritedPackedClosureResolvesToItsOwnDeclaringClass() {
        // A superclass's packed closure must not dispatch to a same-named hoisted method that a
        // subclass happens to declare (GROOVY-12151 review): the adapter's MethodHandle is an
        // invokespecial constant bound to the exact private method, so it is invoked exactly, not
        // virtually. Both classes' first packed closure is $packed$closure$0, so a naive by-name
        // resolution on the runtime class would run B's body for A's closure.
        String src = '''@groovy.transform.PackedClosures
            class A { def foo() { [1].collect { it + 1 } } }
            @groovy.transform.PackedClosures
            class B extends A { def bar() { [1].collect { it + 100 } } }'''
        def b = instance(src + '\n new B()')
        assertEquals([2], b.foo())    // A's closure body (it + 1), not B's (it + 100)
        assertEquals([101], b.bar())
    }

    @Test
    void capturedLocalShadowingAFieldIsReboundToTheLocalNotTheField() {
        // The captured 'base' is a local that shadows the field of the same name (the one form of
        // shadowing Groovy allows). Rebinding matches the captured variable by identity, so the
        // hoisted method reads the captured local, not the field.
        String src = '''@groovy.transform.PackedClosures
            class C {
                int base = 999
                List m() { int base = 100; [1, 2].collect { it + base } }
            }'''
        assertEquals(['C'], generatedClassNames(src))          // the closure packs (no closure class)
        assertEquals([101, 102], instance(src + '\n new C()').m())  // captured local 100, not field 999
    }

    /** Source with one closure that packs and one that declines (a with{} delegate DSL), at the given mode. */
    private static String modeSrc(String mode) {
        """import groovy.transform.CompileStatic
           import groovy.transform.PackedClosures
           import groovy.transform.PackedClosures.PackMode
           @CompileStatic @PackedClosures(mode = PackMode.$mode)
           class M {
               List<String> ok(List<String> xs) { xs.collect { String s -> s.toUpperCase() } }
               String dsl() { new StringBuilder().with { append('x'); toString() } }
           }"""
    }

    /** Compiles the source and returns its collected warnings, or throws on a compile error. */
    private static List warnings(String src) {
        def cu = new CompilationUnit(new CompilerConfiguration())
        cu.addSource('M.groovy', src)
        cu.compile(Phases.CLASS_GENERATION)
        cu.errorCollector.warnings ?: []
    }

    @Test
    void modeLenientIsSilent() {
        assertEquals(0, warnings(modeSrc('LENIENT')).size(), 'LENIENT must not warn on a decline')
    }

    @Test
    void modeWarnReportsEachDeclineWithReason() {
        def ws = warnings(modeSrc('WARN'))
        assertEquals(1, ws.size(), "expected one warning for the declined with{} closure, got: ${ws*.message}")
        assertTrue(ws[0].message.contains('not packed') && ws[0].message.contains('delegate'),
                "warning should name the reason: ${ws[0].message}")
    }

    @Test
    void modeStrictFailsCompilationOnADecline() {
        def e = assertThrows(MultipleCompilationErrorsException) { warnings(modeSrc('STRICT')) }
        assertTrue(e.message.contains('not packed'), "error should explain the decline: $e.message")
    }

    @Test
    void modeAppliesOnlyToTheAnnotation_flagPathStaysLenient() {
        // same declining closure, but driven by the flag with no annotation -> no diagnostics
        String src = '''import groovy.transform.CompileStatic
            @CompileStatic
            class F { String dsl() { new StringBuilder().with { append('x'); toString() } } }'''
        String prev = System.getProperty('groovy.target.closure.pack')
        System.setProperty('groovy.target.closure.pack', 'true')
        try {
            assertEquals(0, warnings(src).size(), 'the automatic flag path must stay lenient (no warnings)')
        } finally {
            if (prev != null) System.setProperty('groovy.target.closure.pack', prev)
            else System.clearProperty('groovy.target.closure.pack')
        }
    }

    @Test
    void modeDisabledMethodOptsOutOfAPackedClass() {
        // the class opts in, but the DISABLED method is excluded -- most-specific wins
        String src = '''import groovy.transform.CompileStatic
            import groovy.transform.PackedClosures
            import groovy.transform.PackedClosures.PackMode
            @CompileStatic @PackedClosures
            class C {
                List<String> a(List<String> xs) { xs.collect { String s -> s.toUpperCase() } }
                @PackedClosures(mode = PackMode.DISABLED)
                List<String> b(List<String> xs) { xs.collect { String s -> s.toLowerCase() } }
            }'''
        // a() packs (no class); only b()'s closure remains
        assertEquals(1, closureClassCount(generatedClassNames(src)),
                'only the DISABLED method should keep its closure class')
        def c = instance(src + '\n new C()')
        assertEquals(['X'], c.a(['x']))
        assertEquals(['x'], c.b(['X']))
    }

    @Test
    void modeDisabledOverridesTheAutomaticFlag() {
        String src = '''import groovy.transform.CompileStatic
            import groovy.transform.PackedClosures
            import groovy.transform.PackedClosures.PackMode
            @CompileStatic @PackedClosures(mode = PackMode.DISABLED)
            class C { List<String> a(List<String> xs) { xs.collect { String s -> s.toUpperCase() } } }'''
        String prev = System.getProperty('groovy.target.closure.pack')
        System.setProperty('groovy.target.closure.pack', 'true')
        try {
            assertEquals(1, closureClassCount(generatedClassNames(src)),
                    'DISABLED must override the flag: the closure stays a class')
        } finally {
            if (prev != null) System.setProperty('groovy.target.closure.pack', prev)
            else System.clearProperty('groovy.target.closure.pack')
        }
    }

    @Test
    void dispatchLaneRouting() {
        // One shape per dispatch lane: the array-free per-arity tables (dispatch1 for one value
        // beyond the receiver, dispatch2 for two — captures count as values) and the general
        // array table for everything else. The adapter routes by captured-plus-argument count;
        // a mis-route lands on a table's default case, which throws GroovyBugError — so correct
        // results ARE the routing assertion.
        def c = instance('''
            @groovy.transform.PackedClosures
            class Lanes {
                def a1c0(List xs)            { xs.collect { it * 2 } }                              // dispatch1
                def a1c1(List xs, int b)     { xs.collect { it + b } }                              // dispatch2 (read capture)
                def a1w1(List xs)            { int s = 0; xs.each { s += it }; s }                  // dispatch2 (written capture: Reference)
                def a1c2(List xs, int b, int f) { xs.collect { (it + b) * f } }                     // array (three values)
                def a2c0(Map m)              { def out = []; m.each { k, v -> out << "$k$v".toString() }; out } // dispatch2
                def a2c1(Map m, String p)    { def out = []; m.each { k, v -> out << "$p$k$v".toString() }; out } // array
                def a0c0()                   { def cl = { -> 42 }; cl() }                           // array (receiver only)
                def a3c0(Map m)              { m.inject(0) { acc, k, v -> acc + v } }               // array (three values)
                static a1static(List xs)     { xs.collect { it + 1 } }                              // dispatch1, Class owner
            }
            new Lanes()
        ''')
        assertEquals([2, 4, 6], c.a1c0([1, 2, 3]))
        assertEquals([11, 12], c.a1c1([1, 2], 10))
        assertEquals(6, c.a1w1([1, 2, 3]))
        assertEquals([22, 24], c.a1c2([1, 2], 10, 2))
        assertEquals(['a1', 'b2'], c.a2c0([a: 1, b: 2]))
        assertEquals(['-a1', '-b2'], c.a2c1([a: 1, b: 2], '-'))
        assertEquals(42, c.a0c0())
        assertEquals(3, c.a3c0([a: 1, b: 2]))
        assertEquals([2, 3], c.class.a1static([1, 2]))
    }

    @Test
    void chunkedArityTablesRouteAllIds() {
        // More same-arity targets than one switch may hold (DISPATCH_CHUNK = 8) forces the
        // two-level arity table: an entry method selecting the chunk by id range, then a
        // per-chunk lookupswitch. Every closure must still reach ITS body — an id landing in
        // the wrong chunk or case throws GroovyBugError or returns the wrong offset.
        def methods = (0..<10).collect { "def m$it(List xs) { xs.collect { it + $it } }" }.join('\n')
        def c = instance("""
            @groovy.transform.PackedClosures
            class Chunky {
                $methods
            }
            new Chunky()
        """)
        (0..<10).each { i ->
            assertEquals([i, i + 1], c."m$i"([0, 1]), "closure $i dispatched to the wrong target")
        }
    }

    @Test
    void visiblySerializationBoundClosuresDecline() {
        // a literal cast/coerced to a Serializable type, or passed directly to writeObject, is
        // visibly serialization-bound: it declines (keeps its class, so serialization works as
        // before) while sibling closures still pack; transitive routes stay with the runtime guard
        String src = '''
            @groovy.transform.PackedClosures
            class Ser implements Serializable {
                def direct() {
                    def baos = new ByteArrayOutputStream()
                    new ObjectOutputStream(baos).writeObject({ it * 2 })
                    baos.size() > 0
                }
                def coerced() { ({ it * 3 } as Serializable) != null }
                def packedStill(List xs) { xs.collect { it + 1 } }
            }'''
        def names = generatedClassNames(src)
        assertEquals(2, closureClassCount(names),
                "the two serialization-bound literals keep their classes: $names")
        def s = instance(src + '\n new Ser()')
        assertTrue(s.direct())                      // classed closure serializes (owner Serializable)
        assertTrue(s.coerced())
        assertEquals([2, 3], s.packedStill([1, 2])) // the control closure still packs and works
    }

    @Test
    void strictModeReportsSerializationBoundDecline() {
        String src = '''import groovy.transform.PackedClosures
            import groovy.transform.PackedClosures.PackMode
            @PackedClosures(mode = PackMode.STRICT)
            class X { def m(ObjectOutputStream out) { out.writeObject({ it }) } }'''
        def e = assertThrows(MultipleCompilationErrorsException) { generatedClassNames(src) }
        assertTrue(e.message.contains('serialization-bound'), e.message)
    }

    @Test
    void serializationFailsFastWithActionableMessage() {
        // A packed closure is not serializable (its dispatch state is bound to hidden classes of
        // the hosting module) and this cannot be detected at compile time. Rather than the cryptic
        // hidden-class NotSerializableException the default field walk would produce, the adapter
        // fails fast naming the hoisted body and the opt-out. The classed escape hatch —
        // dehydrate() then serialize — is also pinned: it works for a closure class but cannot
        // help a packed closure, which is why the message points at excluding the scope instead.
        def result = instance('''
            @groovy.transform.PackedClosures
            class S {
                def m() {
                    def c = { it * 2 }
                    assert c.getClass() == org.codehaus.groovy.runtime.PackedClosure
                    try {
                        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(c.dehydrate())
                        'serialized'
                    } catch (java.io.NotSerializableException e) {
                        e.message
                    }
                }
            }
            new S().m()
        ''')
        assertTrue(result.contains('$packed$closure$'), "message should name the hoisted body: $result")
        assertTrue(result.contains('PackedClosures'), "message should name the opt-out: $result")

        // contrast: the same closure as a class serializes via the standard dehydrate() route
        def classed = instance('''
            class T {
                def m() {
                    def c = { it * 2 }
                    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(c.dehydrate())
                    'serialized'
                }
            }
            new T().m()
        ''')
        assertEquals('serialized', classed)
    }
}
