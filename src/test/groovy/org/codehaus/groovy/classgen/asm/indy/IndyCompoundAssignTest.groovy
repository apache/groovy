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
package org.codehaus.groovy.classgen.asm.indy

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * GEP-15: behaviour parity for the invokedynamic compound-assignment inline
 * cache. Exercises the dynamic path (indy is the default target), so each
 * {@code op=} routes through the {@code COMPOUND_ASSIGN} call site rather than
 * the uncached {@code ScriptBytecodeAdapter.compoundAssign} helper.
 */
final class IndyCompoundAssignTest {

    // Skip (don't silently pass) when indy is disabled: with the static-helper
    // path, GEP-15 semantics are identical, so these tests would pass without
    // exercising the COMPOUND_ASSIGN call site they exist to verify.
    @BeforeEach
    void requireIndy() {
        assumeTrue(CompilerConfiguration.DEFAULT.indyEnabled, 'indy disabled; COMPOUND_ASSIGN path not exercised')
    }

    private static Object ev(String src) { new GroovyShell().evaluate(src) }

    @Test
    void inPlaceBranch_mutatesReceiverAndReturnsSameInstance() {
        assert ev('''
            class Acc { int v = 0; def plusAssign(int x){ v += x; this } }
            def f = { a, b -> a += b; a }
            def acc = new Acc()
            def r = f(acc, 5)
            assert r === acc        // same instance, no reassignment
            assert acc.v == 5
            f(acc, 3)
            assert acc.v == 8       // accumulates in place
            true
        ''')
    }

    @Test
    void fallbackBranch_noAssignMethodUsesBaseOperator() {
        assert ev('''
            def f = { a, b -> a += b; a }
            assert f(10, 7) == 17        // Integer has no plusAssign -> plus
            assert f('x', 'y') == 'xy'   // String -> plus
            true
        ''')
    }

    @Test
    void monomorphicLoop_staysCorrectUnderRepetition() {
        assert ev('''
            class Acc { int v = 0; def plusAssign(int x){ v += x; this } }
            def f = { a, b -> a += b; a }
            def acc = new Acc()
            1000.times { f(acc, 1) }
            assert acc.v == 1000
            true
        ''')
    }

    @Test
    void polymorphicSite_alternatesShapesThroughOneCallSite() {
        assert ev('''
            class Acc { int v = 0; def plusAssign(int x){ v += x; this } }
            def f = { a, b -> a += b; a }     // single op= site, three receiver shapes
            def acc = new Acc()
            20.times {
                f(acc, 1)                      // Acc + int    -> plusAssign (in place)
                assert f(2, 2) == 4            // int + int    -> plus
                assert f('a', 'b') == 'ab'     // String       -> plus
            }
            assert acc.v == 20
            true
        ''')
    }

    @Test
    void argumentTypeIsPartOfTheKey_overloadedAssignStaysCorrect() {
        assert ev('''
            class Multi {
                String log = ''
                def plusAssign(int x)    { log += "i$x"; this }
                def plusAssign(String s) { log += "s$s"; this }
            }
            def f = { a, b -> a += b; a }
            def m = new Multi()
            f(m, 1)        // int overload
            f(m, 'x')      // String overload through the same site
            f(m, 2)        // back to int overload
            assert m.log == 'i1sxi2'
            true
        ''')
    }

    @Test
    void megamorphicSite_evictsViaLruButStaysCorrect() {
        // More distinct receiver classes than the CacheableCallSite LRU bound
        // (groovy.indy.callsite.cache.size, default 8) cycle through one op= site.
        assert ev('''
            def classes = (0..<15).collect { i ->
                new GroovyClassLoader().parseClass(
                    "class Acc${i} { int v = 0; def plusAssign(int x){ v += x; this } }")
            }
            def f = { a, b -> a += b; a }
            def accs = classes.collect { it.getDeclaredConstructor().newInstance() }
            200.times {
                accs.each { f(it, 1) }   // round-robin: forces eviction + relink each pass
            }
            assert accs.every { it.v == 200 }
            true
        ''')
    }

    @Test
    void primitiveTypedOperands_semanticsPreserved() {
        // Compound-assignment on primitive-typed locals routes through the standard
        // (Object,Object)Object site; verify the store-back coercion still matches
        // Groovy semantics for every operator and narrow-on-assign.
        assert ev('''
            class C {
                static add(int i, int n)   { i += n; i }
                static loop()              { int s = 0; for (int k = 0; k < 1000; k++) s += k; s }
                static mul(int a)          { a *= 3; a }
                static divInt(int a)       { a /= 2; a }            // Groovy: (int)(5/2) == 2
                static rem(int a)          { a %= 3; a }
                static widen(int a, long b){ a += b; a }            // (int)(a+b)
                static dbl(double d)       { d += 0.5d; d }
                static byteNarrow(byte b)  { b += 200; b }          // (byte)(100+200) == 44
            }
            assert C.add(10, 5) == 15
            assert C.loop() == 499500
            assert C.mul(4) == 12
            assert C.divInt(5) == 2
            assert C.rem(7) == 1
            assert C.widen(1, 2L) == 3
            assert C.dbl(1.0d) == 1.5d
            assert C.byteNarrow((byte) 100) == 44
            true
        ''')
    }

    @Test
    void switchPointInvalidation_picksUpMetaClassChange() {
        // Cache the base (plus) path, then add plusAssign at runtime via EMC.
        // The shared MOP switch point must invalidate the site so the new
        // in-place method is picked up.
        assert ev('''
            class Box {
                int v = 0
                def plus(int x) { def b = new Box(); b.v = v + x; b }   // base op -> NEW instance
            }
            def f = { a, b -> a += b; a }
            def box = new Box()
            def r1 = f(box, 5)
            assert !r1.is(box) && r1.v == 5    // base path: reassigned to new instance

            Box.metaClass.plusAssign = { int x -> delegate.v = delegate.v + x; delegate }   // now in-place
            def box2 = new Box()
            def r2 = f(box2, 7)
            assert r2.is(box2) && box2.v == 7  // in-place path after invalidation
            true
        ''')
    }

    @Test
    void perInstanceMetaClassOverride_baseOp_pickedUpAfterCaching() {
        // The class-keyed cache must not mask a per-instance metaclass override
        // added AFTER the site cached: setting it fires the MOP switch point
        // (re-resolve) and marks the class uncacheable. The legacy helper got this
        // for free by probing every call; the cache must match that semantics.
        assert ev('''
            class M { int v = 0; M plus(int x){ def m = new M(); m.v = v + x; m } }
            def f = { a, b -> a += b; a }
            def m = new M()
            f(m, 1)                                                       // caches base plus by class M
            m.metaClass.plus = { int x -> def n = new M(); n.v = delegate.v + 100 * x; n }
            assert f(m, 1).v == 100                                       // override picked up, not stale 1
            true
        ''')
    }

    @Test
    void perInstanceMetaClassOverride_assign_pickedUpAfterCaching() {
        assert ev('''
            class A { String log = ''; def plusAssign(int x){ log += 'orig'; this } }
            def f = { a, b -> a += b; a }
            def a = new A()
            f(a, 1)                                                       // caches plusAssign by class A
            a.metaClass.plusAssign = { int x -> delegate.log += 'override'; delegate }
            a.log = ''
            f(a, 1)
            assert a.log == 'override'
            true
        ''')
    }

    @Test
    void neitherMethodResponds_raisesMissingMethod() {
        // No *Assign and no base operator -> the resolver declines and the legacy
        // helper raises the usual MissingMethodException (semantics preserved).
        def err = groovy.test.GroovyAssert.shouldFail(MissingMethodException) {
            ev('''
                class NoOps { }
                def f = { a, b -> a += b; a }
                f(new NoOps(), 1)
            ''')
        }
        assert err.method == 'plus'
    }
}
