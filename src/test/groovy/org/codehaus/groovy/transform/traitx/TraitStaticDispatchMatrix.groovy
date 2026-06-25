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
// GEP-22 § Static members. Three plain-class baseline rows (A/B/C), the
// 12 design rows from GEP-22-progression-analysis.html (with row 11
// expanded to rows 11/11c/11esc covering the trait-shadows-superclass
// quadrant + the T.super.m() escape; row 9 paired with row 9acc covering
// the static-field non-goal and its accessor workaround), and four
// @Anchored-marker rows characterising the GROOVY-12093 proposal.
//
// Run via gradle: `./gradlew :test --tests '*.TraitStaticDispatchMatrix'`
//
// Two kinds of test:
//  * Matrix tests assert OBSERVED behaviour. They pass on every
//    currently-shipping Groovy that delivers the spec behaviour and turn
//    RED on releases that deviate (e.g. rows 1/10/12 on Groovy 5.0.0–
//    5.0.6 / 6.0.0-alpha-1, which carry the GROOVY-8854 regression
//    corrected by GROOVY-11985 in 5.0.7 / 6.0.0-alpha-2). That red is
//    the intended bug-detector signal, not a defect in the test.
//  * The four @Anchored rows (1a/7a/8a/13) assert the GROOVY-12093
//    behaviour now delivered by `groovy.transform.Anchored` (added in
//    this change); each references the annotation directly.
//
// Verdicts encoded here are PROPOSED PENDING TEAM REVIEW where the
// underlying design point is not yet ratified (the four @Anchored rows
// in particular).

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
    // only INSTANCE methods are polymorphic. Trait row 1's "override visible
    // to trait method body" is therefore a deliberate departure from this
    // baseline (the per-implementer $static$self mechanism); the row 2
    // closure carve-out keeps trait closure-context dispatch consistent
    // with this baseline. The simplified-model RULE wording "dispatch like
    // a plain class member" was an aspirational fiction — the empirical
    // truth is more nuanced and the baseline below records it.

    // ---- Baseline A — plain class static, this.foo() in METHOD BODY ----
    // The control for trait row 1. Plain Groovy: declarer-bound ('base').
    // Trait row 1 returns 'true' (override visible) — that's the trait
    // distinctive, NOT a plain-class semantic.
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
    // show: trait row 1's override-via-implementer in method body is a
    // deliberate Groovy-trait distinctive — neither plain statics nor plain
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
    // SPEC-NORMATIVE end-state (GEP-22 § Static members, item 4): override
    // visible to trait code. Holds on 4.x (always did) and on 5.0.7+ /
    // 6.0.0-alpha-2+ (where GROOVY-11985 corrects the GROOVY-8854 regression).
    // Red on 5.0.0–5.0.6 and 6.0.0-alpha-1 = bug detector for those releases.
    // Contrast baseline A (plain class same shape — declarer-bound, 'base'):
    // this row's polymorphic answer is the trait-machinery distinctive.
    @Test
    void row01_publicStatic_overrideSeenByTrait() {
        def r = ev '''
            trait V {
                static boolean defaultNullable() { false }
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
        assert r.overThis == true : "row1 this. : override must be visible to trait body, got ${r.overThis} — on 5.0.0–5.0.6 / 6.0.0-alpha-1 this is the GROOVY-8854 regression (fixed in 5.0.7 / 6.0.0-alpha-2 by GROOVY-11985)"
        assert r.overUnq  == true : "row1 unqual: override must be visible to trait body, got ${r.overUnq}"
    }

    // ---- Row 1a — @Anchored static, trait body sees trait's own copy ----
    // The dispatch half of the @Anchored marker. With the annotation,
    // `this.m()`/`m()` in the trait body should dispatch to the trait's own
    // copy regardless of any implementer override (the JVM/interface-static
    // model — Eric's "static implies final" use case). Unmet on every
    // shipping Groovy (annotation does not exist there); met on the spike.
    @Test
    void row01a_anchored_dispatchIsTraitAnchored() {
        def r = ev '''
            import groovy.transform.Anchored
            trait V {
                @Anchored
                static boolean defaultNullable() { false }
                static boolean seen() { this.defaultNullable() }
            }
            class Over implements V { static boolean defaultNullable() { true } }
            Over.seen()
        '''
        assert r == false : "row1a @Anchored: trait body should always see the trait's own copy (got ${r})"
    }

    // ---- Row 2 — same as #1 but the call is inside a closure ----
    // The closure carve-out. Helper-bound on every version (long-standing,
    // not a 5/6 regression). Matches baseline B (plain class same shape —
    // declarer-bound, 'base'): the carve-out aligns trait closure-context
    // dispatch with plain-Groovy semantics. Removing it would extend the
    // trait-machinery distinctive into closures, further from plain Groovy
    // not closer — the simplified-model "Move 3" proposal was retired
    // after the baseline analysis surfaced this.
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
        assert r == false : "row2 closure: expected false on every version (Groovy ${MAJOR}), got ${r} — carve-out aligns with baseline B"
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
    // Throws MissingMethodException on every version (the trait interface
    // carries no statics — GEP-22 § Static members, item 9). The desired
    // "force trait default" escape this could represent is provided by
    // @Anchored via interface promotion — see row 7a.
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
        println "row07 observed on Groovy ${MAJOR}: ${outcome}  (T.m() from trait body throws on every version; @Anchored fixes it — see row 7a)"
        assert outcome == 'THREW:MissingMethodException' : "row7 expected MissingMethodException: ${outcome}"
    }

    // ---- Row 7a — @Anchored: T.m() from trait body resolves via interface static ----
    // Interface promotion side-effect. With @Anchored, the trait static is
    // also added to the trait interface, so the "force trait default" escape
    // T.m() actually resolves (row 7 above throws on every shipping version).
    @Test
    void row07a_anchored_traitQualified_resolvesViaInterfaceStatic() {
        def r = ev '''
            import groovy.transform.Anchored
            trait T {
                @Anchored
                static String who() { 'T' }
                static String viaTrait() { T.who() }
            }
            class C implements T { static String who() { 'C' } }
            C.viaTrait()
        '''
        assert r == 'T' : "row7a @Anchored: T.m() from trait body should resolve via interface static (got ${r})"
    }

    // ---- Row 8 — external Impl.m() works; external T.m() unsupported ----
    @Test
    void row08_externalAccess() {
        assert ev('trait T { static String foo() { "ok" } }\nclass C implements T {}\nC.foo()') == 'ok'
        GroovyAssert.shouldFail {           // no statics on the trait interface
            ev 'trait T { static String foo() { "x" } }\nT.foo()'
        }
    }

    // ---- Row 8a — @Anchored: external Trait.m() resolves via interface static ----
    // Interface promotion main effect. Jochen's "the static method is not added
    // to the interface. That should be a bug." With @Anchored, external
    // T.m() resolves to the JVM-native interface static — the row 8 "external
    // T.m() unsupported" limitation no longer applies for marker-bearing methods.
    @Test
    void row08a_anchored_externalTraitDotM_works() {
        def r = ev '''
            import groovy.transform.Anchored
            trait T {
                @Anchored
                static String foo() { 'ok' }
            }
            class C implements T {}
            T.foo()
        '''
        assert r == 'ok' : "row8a @Anchored: external T.m() should resolve via interface static (got ${r})"
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
    // re-composition). Red on 5.0.0–5.0.6 / 6.0.0-alpha-1 — same regression
    // as row 1, same fix in 5.0.7 / 6.0.0-alpha-2 via GROOVY-11985.
    @Test
    void row10_subclassAnchorsOnDirectImplementer() {
        def r = ev '''
            trait T { static String m2() { 'T.m2' }; static String m1() { m2() } }
            class A implements T { static String m2() { 'A.m2' } }
            class B extends A    { static String m2() { 'B.m2' } }
            [ a: A.m1(), b: B.m1(), bDirect: B.m2() ]
        '''
        assert r.bDirect == 'B.m2'
        assert r.a == 'A.m2' : "row10 a: A's override must be visible to A.m1(), got ${r.a} — see row 1 note on the GROOVY-8854 regression"
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
    // A consequence of row 1's dispatch path: GROOVY-11985 also shifts this
    // overload case from '22' (pre-fix G5/G6 behaviour) back to '21' (G4's
    // long-standing answer, and the post-fix answer on 5.0.7 / 6.0.0-alpha-2).
    // Red on 5.0.0–5.0.6 / 6.0.0-alpha-1 — same regression boundary as
    // rows 1 and 10. Jochen flagged both G4 and G5 as "imho wrong" but the
    // team has not settled an alternative; this row locks the current
    // post-fix outcome so any further change is surfaced for discussion.
    @Test
    void row12_overloadUnderCompileStatic_observed() {
        def out = ev '''
            @groovy.transform.CompileStatic
            trait T { static int m(X1 x){ 2 }; static int call2(X1 x){ m(x) } }
            @groovy.transform.CompileStatic
            class A implements T { static int m(X2 x){ 1 } }
            class X1 {}
            class X2 extends X1 {}
            "" + A.call2(new X1()) + A.call2(new X2())
        '''
        println "row12 observed on Groovy ${MAJOR}: ${out} (expected '21' on 4.x and on 5.0.7+ / 6.0.0-alpha-2+)"
        assert out == '21' : "row12: expected '21' (post-GROOVY-11985), got '${out}' — on 5.0.0–5.0.6 / 6.0.0-alpha-1 this returns '22' (the regression boundary)"
    }

    // ---- Row 13 — @Anchored(inInterface=false) opt-out ----
    // The narrow escape: dispatch stays trait-anchored (like row 1a) but the
    // interface static is suppressed, so external T.m() continues to fail
    // (the row 8 behaviour persists for this method). Useful for soft-
    // deprecated trait-internal helpers that need to stay publicly callable
    // via the existing Impl.m() forwarder but should not gain a fresh
    // Java-visible interface API surface.
    @Test
    void row13_anchored_inInterfaceFalse_optsOutOfInterfacePromotion() {
        // Part 1: trait-body dispatch is still trait-anchored
        def r = ev '''
            import groovy.transform.Anchored
            trait V {
                @Anchored(inInterface=false)
                static String name() { 'trait' }
                static String seen() { this.name() }
            }
            class Impl implements V { static String name() { 'impl' } }
            Impl.seen()
        '''
        assert r == 'trait' : "row13 inInterface=false: dispatch should still be trait-anchored (got ${r})"

        // Part 2: external V.name() must still throw — no interface static generated
        boolean externalThrows = false
        try {
            ev '''
                import groovy.transform.Anchored
                trait V {
                    @Anchored(inInterface=false)
                    static String name() { 'trait' }
                }
                class Impl implements V {}
                V.name()
            '''
        } catch (Throwable ignored) {
            externalThrows = true
        }
        assert externalThrows : "row13 inInterface=false: external V.name() should still fail (no interface static)"
    }
}
