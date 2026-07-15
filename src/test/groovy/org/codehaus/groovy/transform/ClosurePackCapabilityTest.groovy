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
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * The GEP-27 capability analysis: under {@code @CompileStatic} a closure the type checker resolved
 * against the owner (no {@code DELEGATION_METADATA}) is provably delegate-independent, so it is
 * packed <em>automatically</em> — no {@code @PackedClosures} annotation — when the {@code
 * groovy.target.closure.pack} flag is set. Closures resolved against a delegate ({@code @DelegatesTo})
 * are proven delegate-dependent and kept as classes; dynamic closures (no static resolution) are
 * never auto-packed. The flag is read per compilation, so it is toggled per test.
 */
final class ClosurePackCapabilityTest {

    private static final String PROP = 'groovy.target.closure.pack'

    private static <T> T withFlag(boolean on, Closure<T> work) {
        String previous = System.getProperty(PROP)
        if (on) System.setProperty(PROP, 'true') else System.clearProperty(PROP)
        try {
            work.call()
        } finally {
            if (previous != null) System.setProperty(PROP, previous) else System.clearProperty(PROP)
        }
    }

    private static List<String> classNames(String src, boolean flag) {
        withFlag(flag) {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('Src.groovy', src)
            cu.compile(Phases.CLASS_GENERATION)
            cu.classes.collect { it.name }.sort()
        }
    }

    private static int closureClassCount(List<String> names) {
        names.count { it.contains('_closure') }
    }

    private static Object eval(String src, String tail, boolean flag) {
        withFlag(flag) { new GroovyShell().evaluate(src + '\n' + tail) }
    }

    private static final String STATIC_PLAIN = '''import groovy.transform.CompileStatic
        @CompileStatic
        class Plain {
            List<Integer> doubled(List<Integer> xs) { xs.collect { Integer it -> it * 2 } }
            int total(List<Integer> xs)            { int t = 0; xs.each { Integer it -> t += it }; t }
        }'''

    @Test
    void staticDelegateIndependentClosuresAutoPackWhenFlagOn() {
        // No @PackedClosures anywhere: the flag alone drives packing of the proven-safe closures.
        assertEquals(0, closureClassCount(classNames(STATIC_PLAIN, true)),
                'delegate-independent @CompileStatic closures should auto-pack')
        def p = eval(STATIC_PLAIN, 'new Plain()', true)
        assertEquals([2, 4, 6], p.doubled([1, 2, 3]))     // behaviour unchanged
        assertEquals(6, p.total([1, 2, 3]))               // captured write, Reference-threaded
    }

    @Test
    void nothingAutoPacksWhenFlagOff() {
        // Default: no automatic packing, so @CompileStatic code is byte-for-byte as today.
        assertTrue(closureClassCount(classNames(STATIC_PLAIN, false)) >= 2,
                'with the flag off no closure should be auto-packed')
        assertEquals([2, 4, 6], eval(STATIC_PLAIN, 'new Plain().doubled([1, 2, 3])', false))
    }

    @Test
    void delegateResolvedClosuresAreProvenDependentAndKeptAsClasses() {
        // The builder's closure resolves tag()/nest against the @DelegatesTo delegate, so STC marks it
        // with DELEGATION_METADATA. The capability analysis must NOT auto-pack it (that would rebind
        // the calls to the owner and break the DSL), and delegate resolution must still work.
        String src = '''import groovy.transform.CompileStatic
            class Builder {
                List<String> tags = []
                void tag(String t) { tags << t }
                List<String> run(@DelegatesTo(Builder) Closure c) {
                    c.delegate = this; c.resolveStrategy = Closure.DELEGATE_FIRST; c.call(); tags
                }
            }
            @CompileStatic
            class UsesDsl {
                List<String> make() { new Builder().run { tag('a'); tag('b') } }
            }'''
        def names = classNames(src, true)
        assertTrue(closureClassCount(names) >= 1, "delegate DSL closure must stay a class: $names")
        assertEquals(['a', 'b'], eval(src, 'new UsesDsl().make()', true))
    }

    @Test
    void autoPackedBodyIsStaticallyCompiled() {
        // The hoisted body reuses the expressions StaticCompilationVisitor already annotated, and the
        // read-only capture is passed as a typed leading parameter -- so the body compiles with static
        // dispatch (zero invokedynamic), unlike the Reference-boxed closure-class form.
        String src = '''import groovy.transform.CompileStatic
            @CompileStatic
            class T {
                List<String> tag(List<String> xs, String p) { xs.collect { String s -> p + s } }
            }'''
        byte[] bytes = withFlag(true) {
            def cu = new CompilationUnit(new CompilerConfiguration())
            cu.addSource('T.groovy', src)
            cu.compile(Phases.CLASS_GENERATION)
            cu.classes.find { it.name == 'T' }.bytes
        }
        def hoisted = [:]
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exc) {
                if (!name.startsWith('$packed$')) return null
                hoisted[name] = [desc: desc, indy: 0]
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    void visitInvokeDynamicInsn(String n, String d, Handle bsm, Object... args) {
                        hoisted[name].indy++
                    }
                }
            }
        }, 0)
        assertEquals(1, hoisted.size(), "expected one hoisted method, got: ${hoisted.keySet()}")
        def name = hoisted.keySet().iterator().next()
        def info = hoisted[name]
        assertTrue(info.desc.startsWith('(Ljava/lang/String;Ljava/lang/String;'),
                "captured value should be a typed String parameter: ${info.desc}")
        assertEquals(0, info.indy, "hoisted body should be statically dispatched, got ${info.indy} indy in $name")
        assertEquals(['p-x'], eval(src, 'new T().tag(["x"], "p-")', true))
    }

    @Test
    void writtenCapturesPackViaSharedReferenceHolder() {
        // The shared Reference is passed straight into the hoisted method as a holder parameter, so
        // the untouched body compiles with the implicit get()/set() the ASM generator already emits
        // for closure classes. That supports every write form -- compound ops beyond +/-, and postfix
        // increments with correct value-in-expression semantics.
        String src = '''import groovy.transform.CompileStatic
            @CompileStatic
            class W {
                int total(List<Integer> xs)  { int t = 0; xs.each { t += it }; t }
                long lshift(List<Integer> xs){ long m = 1; xs.each { m <<= it }; m }     // beyond +/- compound
                List<Integer> olds(List<Integer> xs) {
                    int c = 10
                    List<Integer> seen = []
                    xs.each { seen << c++ }                                              // postfix VALUE used
                    seen << c
                    seen
                }
            }'''
        assertEquals(0, closureClassCount(classNames(src, true)), 'all write forms should pack')
        def w = eval(src, 'new W()', true)
        assertEquals(6, w.total([1, 2, 3]))
        assertEquals(32L, w.lshift([2, 3]))
        assertEquals([10, 11, 12], w.olds([0, 0]))   // c++ yields the OLD value each time, then final c
    }

    @Test
    void packedClosureDestructuresSingleListArgument() {
        // Real closures destructure a single List/Tuple argument across a multi-param signature;
        // the shared adapter must preserve that.
        String src = '''import groovy.transform.CompileStatic
            @CompileStatic
            class D {
                List<String> pairs(List<Tuple2<String, Integer>> ts) {
                    List<String> out = []
                    ts.each { String s, Integer n -> out << "$s=$n".toString() }
                    out
                }
            }'''
        assertEquals(0, closureClassCount(classNames(src, true)))
        assertEquals(['a=1', 'b=2'],
                eval(src, 'new D().pairs([Tuple.tuple("a", 1), Tuple.tuple("b", 2)])', true))
    }

    @Test
    void dynamicFreeNameClosuresAreNotAutoPacked() {
        // No @CompileStatic AND a free name (the implicit-this call helper(), which the runtime
        // resolves through the owner/delegate chain): delegate-independence is unproven, so the
        // automatic path must not fire even with the flag on -- only @PackedClosures (trust +
        // runtime guard) could pack these.
        String src = '''class Dyn {
                def helper(x) { x }
                List<Integer> doubled(List<Integer> xs) { xs.collect { helper(it) * 2 } }
            }'''
        assertTrue(closureClassCount(classNames(src, true)) >= 1,
                'a dynamic closure with a free name must not be auto-packed')
        assertEquals([2, 4, 6], eval(src, 'new Dyn().doubled([1, 2, 3])', true))
    }

    @Test
    void dynamicDelegateIndependentClosuresAutoPackBySyntax() {
        // GROOVY-12151 dynamic syntactic path: a closure with NO free name -- only its parameter,
        // a constant, and a parameter receiver -- cannot be affected by any caller-set delegate, so
        // it is delegate-independent by syntax alone (no types needed) and the flag auto-packs it
        // even under dynamic compilation.
        String src = '''class Dyn {
                List<Integer> doubled(List<Integer> xs) { xs.collect { it * 2 } }
            }'''
        assertEquals(0, closureClassCount(classNames(src, true)),
                'a syntactically delegate-independent dynamic closure should auto-pack under the flag')
        assertEquals([2, 4, 6], eval(src, 'new Dyn().doubled([1, 2, 3])', true))
    }

    @Test
    void syntacticSubsetCoversTheCommonShapes() {
        // the everyday delegate-independent shapes: implicit it, captured local, explicit/param
        // receiver, multi-arg comparator -- all pack dynamically under the flag with no annotation
        String src = '''class Dyn {
                def implicitIt(xs)   { xs.collect { it * 2 } }
                def capture(xs, k)   { xs.collect { x -> x + k } }
                def paramReceiver(xs){ xs.collect { it.toString().length() } }
                def comparator(xs)   { xs.sort(false) { a, b -> a <=> b } }
            }'''
        assertEquals(0, closureClassCount(classNames(src, true)),
                'the common no-free-name shapes should all pack under the flag')
        def d = eval(src, 'new Dyn()', true)
        assertEquals([2, 4, 6], d.implicitIt([1, 2, 3]))
        assertEquals([11, 12], d.capture([1, 2], 10))
        assertEquals([2, 1], d.paramReceiver(['ab', 'c']))
        assertEquals([1, 2, 3], d.comparator([3, 1, 2]))
    }

    @Test
    void syntacticallyPackedClosureTreatsDelegateAsHarmlessNoOp() {
        // the non-strict guard: a syntactically-packed closure has no free name, so a caller-set
        // delegate must be IGNORED, not rejected -- exactly as a normal closure behaves. (The
        // annotation trust path, by contrast, throws, since it cannot prove independence.)
        Object result = eval('', '''
            Closure c = { x -> x + 1 }
            assert c.getClass().name == 'org.codehaus.groovy.runtime.PackedClosure'
            c.delegate = new Object()
            c.resolveStrategy = Closure.DELEGATE_FIRST
            c(41)
        ''', true)
        assertEquals(42, result)
    }

    @Test
    void dynamicLambdaOfTheSubsetIsPackedToo() {
        // a dynamically compiled lambda is compiled as a closure, so the same syntactic path
        // body-hoists the equivalent dynamic lambda subset (it just does not reach the
        // LambdaMetafactory singleton form, which stays @CompileStatic-only)
        String src = '''import java.util.function.Function
            class Dyn {
                def use() {
                    Function<Integer,Integer> f = (Integer x) -> x + 1
                    f.apply(41)
                }
            }'''
        assertEquals(0, closureClassCount(classNames(src, true)),
                'a syntactically-independent dynamic lambda should auto-pack under the flag')
        assertEquals(42, eval(src, 'new Dyn().use()', true))
    }
}
