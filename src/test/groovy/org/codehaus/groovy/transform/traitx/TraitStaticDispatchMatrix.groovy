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

// Trait-static dispatch behaviour matrix — the executable companion to
// GEP-22 § Static members. Three plain-class baseline rows (A/B/C) and
// the 12 design rows from GEP-22-progression-analysis.html (with row 11
// expanded to rows 11/11c/11esc covering the trait-shadows-superclass
// quadrant + the T.super.m() escape; row 9 paired with row 9acc covering
// the static-field non-goal and its accessor workaround).
//
// Run via gradle: `./gradlew :test --tests '*.TraitStaticDispatchMatrix'`
//
// Matrix tests assert OBSERVED behaviour. Rows that depend on per-
// implementer override visibility (rows 1, 10, 12) opt in via
// `@groovy.transform.Virtual` on the relevant trait static — plain
// `static` is declarer-bound, `@Virtual` restores the override-via-
// implementer path (the dispatch shape in 4.x).

package org.codehaus.groovy.transform.traitx

import groovy.test.GroovyAssert
import org.junit.Test

class TraitStaticDispatchMatrix {

    /** Major version of the running Groovy: 4, 5, 6, ... */
    static final int MAJOR = GroovySystem.version.tokenize('.')[0] as int

    /** Compile+run a snippet with the *running* Groovy compiler. */
    private static Object ev(String src) { new GroovyShell().evaluate(src) }

    // =================== PLAIN-CLASS BASELINE (NO TRAITS) ===================
    // Establishes what plain Groovy does for the same dispatch shapes the
    // trait rows below test. Read these first — the trait rows characterise
    // how trait machinery DEPARTS from this baseline, not how it matches it.
    //
    // VERIFIED on 4.0.32: plain Groovy static dispatch is declarer-bound in
    // every context (method body, closure body, dynamic, @CompileStatic);
    // only INSTANCE methods are polymorphic. Public trait statics match
    // this baseline by default; the row 1 departure (override visible to
    // trait body) is the OPT-IN behaviour of `@groovy.transform.Virtual`,
    // which routes the call through the implementing class via the per-
    // implementer $static$self mechanism.

    // ---- Baseline A — plain class static, this.foo() in METHOD BODY ----
    // The control for trait row 1. Plain Groovy: declarer-bound ('base').
    // Trait row 1 returns 'true' (override visible) — that requires
    // @Virtual; without it the trait matches this baseline.
    @Test
    void baseline_A_plainStatic_methodBody_isDeclarerBound() {
        def r = ev '''
            class Base {
                static String foo() { 'base' }
                static String seen() { this.foo() }
            }
            class Sub extends Base { static String foo() { 'sub' } }
            Sub.seen()
        '''
        assert r == 'base' : "baseline A: plain-class static must be declarer-bound, got ${r}"
    }

    // ---- Baseline B — plain class static, this.foo() in CLOSURE BODY ----
    // The control for trait row 2 (the closure carve-out). Plain Groovy:
    // declarer-bound ('base') — SAME as baseline A. There is no plain-class
    // carve-out; static dispatch is uniformly declarer-bound. The trait
    // carve-out aligns trait closure-context with this baseline; removing
    // it (Move 3) would make traits MORE polymorphic than plain Groovy in
    // closure context, not less.
    @Test
    void baseline_B_plainStatic_closureBody_isDeclarerBound() {
        def r = ev '''
            class Base {
                static String foo() { 'base' }
                static String seen() { [1].collect { this.foo() }[0] }
            }
            class Sub extends Base { static String foo() { 'sub' } }
            Sub.seen()
        '''
        assert r == 'base' : "baseline B: plain-class static stays declarer-bound in closures too, got ${r}"
    }

    // ---- Baseline C — plain class INSTANCE method, this.foo() in CLOSURE BODY ----
    // Confirms ordinary instance dispatch IS polymorphic in closure context
    // (row 6 in the trait matrix is the instance-method analogue). Only
    // static dispatch falls back to declarer-bound. Together baselines A/B/C
    // show: row 1's override-via-implementer (when opted into with @Virtual)
    // is a Groovy-trait distinctive — neither plain statics nor plain
    // instance methods give that shape in closure context unaided.
    @Test
    void baseline_C_plainInstance_closureBody_isPolymorphic() {
        def r = ev '''
            class Base {
                String foo() { 'base' }
                String seen() { [1].collect { this.foo() }[0] }
            }
            class Sub extends Base { String foo() { 'sub' } }
            new Sub().seen()
        '''
        assert r == 'sub' : "baseline C: plain-class instance dispatch must be polymorphic everywhere, got ${r}"
    }

    // ============================ TRAIT MATRIX ============================

    // ---- Row 1 — public static, this./unqualified, impl overrides ----
    // Scenario: overridable static defaults (Grails Validateable.defaultNullable).
    // The trait static is marked @Virtual to opt into the per-implementer
    // override path; plain trait statics (no marker) are declarer-bound
    // and would return the trait's own copy regardless of any same-named
    // static on the implementer — see VirtualAnnotationTest for the
    // un-annotated control. Contrast baseline A (plain class same shape —
    // declarer-bound, 'base'): the @Virtual marker makes the trait answer
    // polymorphic at the spot a plain class is not.
    @Test
    void row01_publicStatic_overrideSeenByTrait() {
        // Per-implementer override visibility is opt-in via @Virtual.
        // Plain `static` is declarer-bound; @Virtual makes the trait body's
        // dispatch route through the implementing class.
        def r = ev '''
            import groovy.transform.Virtual
            trait V {
                @Virtual static boolean defaultNullable() { false }
                static boolean seenThis() { this.defaultNullable() }
                static boolean seenUnqualified() { defaultNullable() }
            }
            class Over implements V { static boolean defaultNullable() { true } }
            class Def  implements V { }
            [ overThis: Over.seenThis(), overUnq: Over.seenUnqualified(),
              defThis: Def.seenThis(), direct: Over.defaultNullable() ]
        '''
        assert r.direct == true            // direct call: override always wins
        assert r.defThis == false          // row 1': no override -> trait default
        assert r.overThis == true : "row1 this. : @Virtual override must be visible to trait body, got ${r.overThis}"
        assert r.overUnq  == true : "row1 unqual: @Virtual override must be visible to trait body, got ${r.overUnq}"
    }

    // ---- Row 2 — same as #1 but the call is inside a closure ----
    // The closure carve-out: a call from inside a closure body is
    // helper-bound regardless of @Virtual. Matches baseline B (plain
    // class same shape — declarer-bound, 'base'): the carve-out aligns
    // trait closure-context dispatch with plain-Groovy semantics.
    @Test
    void row02_publicStatic_insideClosure() {
        def r = ev '''
            trait V {
                static boolean defaultNullable() { false }
                static boolean seenInClosure() { [1].collect { this.defaultNullable() }[0] }
            }
            class Over implements V { static boolean defaultNullable() { true } }
            Over.seenInClosure()
        '''
        assert r == false : "row2 closure: expected false (Groovy ${MAJOR}), got ${r} — carve-out aligns with baseline B"
    }

    // ---- Row 3 — private trait static is NOT overridable by the impl ----
    @Test
    void row03_privateStatic_notOverridable() {
        def r = ev '''
            trait T {
                static String m1() { bar() }
                private static String bar() { 't' }
            }
            class C implements T { static String bar() { 'c' } }
            C.m1()
        '''
        assert r == 't' : "row3: private trait static must stay trait-internal, got ${r}"
    }

    // ---- Row 4 (dynamic) — name only on the implementer, not in T ----
    // Devil's-advocate outcome: dynamic Groovy permits this exactly like a
    // missing method on any plain class. Works on 4 AND 6.
    @Test
    void row04_dyn_nameNotInTrait_resolvesOnImplementer() {
        def r = ev '''
            trait T {
                private static String entry() { m2() }   // m2 not declared in T
                static String run() { entry() }
            }
            class A implements T { static String m2() { 'A.m2' } }
            A.run()
        '''
        assert r == 'A.m2' : "row4 dynamic: expected runtime resolution to A.m2, got ${r}"
    }

    // ---- Row 4 (@CompileStatic) — same, but must fail to compile ----
    // Ordinary @CompileStatic resolvability rule, NOT a trait-specific one.
    @Test
    void row04cs_compileStatic_nameNotInTrait_isCompileError() {
        GroovyAssert.shouldFail {
            ev '''
                @groovy.transform.CompileStatic
                trait T { static String run() { m2() } }   // m2 unresolved at compile time
                @groovy.transform.CompileStatic
                class A implements T { static String m2() { 'A' } }
                A.run()
            '''
        }
    }

    // ---- Row 6 — instance method baseline (override always wins) ----
    // Matches baseline C (plain class instance — polymorphic, 'sub'):
    // ordinary instance dispatch is polymorphic in trait code too.
    @Test
    void row06_instanceMethod_overrideWins() {
        def r = ev '''
            trait T { String which() { 'trait' }; String greet() { which() } }
            class C implements T { String which() { 'class' } }
            class D implements T { }
            [ c: new C().greet(), d: new D().greet() ]
        '''
        assert r.c == 'class' && r.d == 'trait'
    }

    // ---- Row 7 — T.m() trait-qualified inside trait body ----
    // Throws MissingMethodException on every shipping 5.x (the trait
    // interface carries no statics — GEP-22 § Static members, item 9).
    @Test
    void row07_traitQualified_observed() {
        def outcome
        try {
            outcome = ev '''
                trait T {
                    static String who() { 'T' }
                    static String viaTrait() { T.who() }
                }
                class C implements T { static String who() { 'C' } }
                C.viaTrait()
            '''
        } catch (Throwable t) {
            outcome = "THREW:${t.class.simpleName}"
        }
        println "row07 observed on Groovy ${MAJOR}: ${outcome}"
        assert outcome == 'THREW:MissingMethodException' : "row7 expected MissingMethodException: ${outcome}"
    }

    // ---- Row 8 — external Impl.m() works; external T.m() unsupported ----
    @Test
    void row08_externalAccess() {
        assert ev('trait T { static String foo() { "ok" } }\nclass C implements T {}\nC.foo()') == 'ok'
        GroovyAssert.shouldFail {           // no statics on the trait interface
            ev 'trait T { static String foo() { "x" } }\nT.foo()'
        }
    }

    // ---- Row 9 — static FIELD override NOT seen by trait (documented non-goal) ----
    // A bare static-field reference in a trait body reads the trait's
    // template field, not any same-named static on the implementing class.
    // Documented as a non-goal in GEP-22 § Static members item 7; the
    // workaround is to use an accessor method, exercised by row 9acc below.
    // Matches plain-Groovy semantics (no static-field inheritance — see the
    // baseline rows) so this is a deliberate alignment with JVM convention,
    // not a TODO.
    @Test
    void row09_staticFieldOverride_notSeenByTrait_observed() {
        def r = ev '''
            trait V {
                static String origin = 'trait'
                static String seen() { origin }
            }
            class Over implements V { static String origin = 'class' }
            Over.seen()
        '''
        assert r == 'trait' : "row9: trait body must read the trait's template field (got ${r}) — if 'class', the field-override non-goal has been reversed; see GEP-22 § Static members item 7"
    }

    // ---- Row 9acc — accessor pattern: per-implementer overridable defaults ----
    // The supported pattern for per-implementer overridable static defaults
    // (GEP-22 § Static members item 7's worked example): declare a static
    // accessor in the trait, optionally override with a same-name static
    // accessor on the implementer; trait-body property access resolves
    // through the getter chain and follows row 1's dispatch path. This is
    // the recommended replacement for the field-override pattern row 9
    // documents as a non-goal.
    @Test
    void row09acc_accessorPattern_overrideVisibleToTraitBody() {
        def r = ev '''
            trait V {
                static String getOrigin() { 'trait' }
                static String describe() { "origin is ${origin}" }
            }
            class Over implements V {
                static String getOrigin() { 'class' }
            }
            Over.describe()
        '''
        assert r == 'origin is class' : "row9acc: accessor pattern must dispatch through implementer like row 1 (got ${r})"
    }

    // ---- Row 10 — B extends A implements T: dispatch anchors on A ----
    // Composition is per direct implementer (GEP-22 § Static members,
    // item 8): the trait's forwarder is baked into A; B does not re-compose,
    // so B.m1() still dispatches m2 through A's forwarder. Row 1's
    // override-visible dispatch flows through here: A's override of m2 IS
    // visible to m1 (returns 'A.m2'); B's own m2 is NOT (it would require
    // re-composition).
    @Test
    void row10_subclassAnchorsOnDirectImplementer() {
        // The override-visible part requires @Virtual on the callee (m2);
        // without it m1's call to m2 is declarer-bound and returns 'T.m2'.
        def r = ev '''
            import groovy.transform.Virtual
            trait T { @Virtual static String m2() { 'T.m2' }; static String m1() { m2() } }
            class A implements T { static String m2() { 'A.m2' } }
            class B extends A    { static String m2() { 'B.m2' } }
            [ a: A.m1(), b: B.m1(), bDirect: B.m2() ]
        '''
        assert r.bDirect == 'B.m2'
        assert r.a == 'A.m2' : "row10 a: A's override must be visible to A.m1(), got ${r.a}"
        assert r.b == 'A.m2' : "row10 b: B.m1() must anchor on A (the direct implementer), got ${r.b}"
        assert r.b != 'B.m2' : "row10 load-bearing: B's own m2 must never win — dispatch anchors on direct implementer A, not the receiver class"
    }

    // ---- Row 11 — trait static vs inherited instance method ----
    // Documented behaviour (GEP-22 § Static members): the trait member
    // shadows the inherited class-hierarchy member at any call site where
    // it is reachable. Here the trait member is static, the inherited
    // member is instance; at the instance call site `new D().m()` the
    // trait static still wins because Groovy lets statics be called via
    // an instance receiver. Disambiguate by declaring m on D — see
    // row11esc.
    @Test
    void row11_traitStaticVsInheritedInstance_observed() {
        def r = ev '''
            trait T { static m() { 'T' } }
            class C { def m() { 'C' } }
            class D extends C implements T { }
            new D().m()
        '''
        assert r == 'T' : "row11: trait static must shadow inherited instance method (got ${r}) — see GEP-22 § Static members and row11esc for disambiguation"
    }

    // ---- Row 11c — trait instance vs inherited class static (mirror of row 11) ----
    // The mirror direction of row 11. At an instance call site, the trait
    // instance member wins (reachable); at a static call site, the
    // inherited class static wins (the trait instance member is not
    // reachable via a static call). Together with rows 6, 11 and the
    // pure-static case this completes the four-cell trait-shadows-class
    // quadrant.
    @Test
    void row11c_traitInstanceVsInheritedStatic_observed() {
        def r = ev '''
            trait T { def m() { 'T' } }
            class C { static m() { 'C' } }
            class D extends C implements T { }
            [ inst: new D().m(), stat: D.m() ]
        '''
        assert r.inst == 'T' : "row11c instance call: trait instance must shadow inherited class static (got ${r.inst})"
        assert r.stat == 'C' : "row11c static call: trait instance is unreachable via static call; inherited class static wins (got ${r.stat})"
    }

    // ---- Row 11esc — disambiguation escape via T.super.m() / super.m() ----
    // The canonical workaround (per GEP-22 § Static members) for any
    // trait-vs-superclass collision: declare m on the implementing class
    // and from its body invoke super.m() for the superclass version and
    // T.super.m() for the trait version. T.super.m() works uniformly for
    // both static and instance trait members.
    @Test
    void row11esc_traitSuperEscape_disambiguatesBothDirections() {
        def r = ev '''
            trait T { static m() { 'T' } }
            class C { def m() { 'C' } }
            class D extends C implements T {
                def m() { "D->C:${super.m()},T:${T.super.m()}" }
            }
            new D().m()
        '''
        assert r == 'D->C:C,T:T' : "row11esc: T.super.m() must reach trait static and super.m() must reach class instance (got ${r})"
    }

    // ---- Row 12 — overload across trait/impl under @CompileStatic ----
    // A consequence of row 1's dispatch path: when the callee is @Virtual
    // the trait-side overload set is widened by what the implementer
    // declares, so A.call2(X2) resolves to A.m(X2) rather than T.m(X1).
    // Without @Virtual the trait static is declarer-bound and both calls
    // would return '2'. Jochen flagged the @Virtual outcome here as
    // "imho wrong" but the team has not settled an alternative; this row
    // locks the current outcome so any further change is surfaced for
    // discussion.
    @Test
    void row12_overloadUnderCompileStatic_observed() {
        // Overload dispatch through the implementer requires @Virtual;
        // without it the trait's m(X1) is declarer-bound and always wins.
        def out = ev '''
            import groovy.transform.Virtual
            @groovy.transform.CompileStatic
            trait T {
                @Virtual static int m(X1 x){ 2 }
                static int call2(X1 x){ m(x) }
            }
            @groovy.transform.CompileStatic
            class A implements T { static int m(X2 x){ 1 } }
            class X1 {}
            class X2 extends X1 {}
            "" + A.call2(new X1()) + A.call2(new X2())
        '''
        assert out == '21' : "row12 @Virtual: expected '21', got '${out}'"
    }

}
