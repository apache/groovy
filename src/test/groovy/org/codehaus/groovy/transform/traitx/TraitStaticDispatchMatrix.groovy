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
// the static-field non-goal and its accessor workaround).
//
// Run via gradle: `./gradlew :test --tests '*.TraitStaticDispatchMatrix'`
//
// Matrix tests assert OBSERVED behaviour. They pass on every
// currently-shipping Groovy that delivers the spec behaviour and turn
// RED on releases that deviate (e.g. rows 1/10/12 on Groovy 5.0.0–
// 5.0.6 / 6.0.0-alpha-1, which carry the GROOVY-8854 regression
// corrected by GROOVY-11985 in 5.0.7 / 6.0.0-alpha-2). That red is
// the intended bug-detector signal, not a defect in the test.

package org.codehaus.groovy.transform.traitx

import groovy.test.GroovyAssert
import groovy.test.NotYetImplemented
import org.codehaus.groovy.control.MultipleCompilationErrorsException
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

    // ---- Baseline D — plain class INHERITED static, subtype arg, in METHOD BODY ----
    // The control for rows 16/16b (GROOVY-12106). A child CLASS extends a parent class
    // and its instance-method body calls an inherited `static` with an argument whose
    // static type is a PROPER SUBTYPE of the declared parameter type, under @CompileStatic.
    // Plain Groovy resolves this cleanly in every call form (unqualified / this. /
    // qualified) and on every version (verified 4.0.32, 5.0.7-SNAPSHOT, 6.0.0-SNAPSHOT),
    // compile AND runtime. Rows 16/16b show the trait-extends-trait analogue REGRESSED in
    // the 5.x/6.x line — so that failure is a trait static-helper dispatch defect (the
    // synthetic `java.lang.Class $self` helper receiver), NOT a general STC inherited-static
    // or subtype-argument problem. This baseline makes that trait-vs-class contrast executable.
    @Test
    void baseline_D_plainClass_inheritedStatic_subtypeArg_resolves() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Parent {
                static String withDelegate(Closure cl, Object target) {
                    cl.call()
                    'target=' + target.class.simpleName
                }
            }
            final class SimpleArgument { }
            @CompileStatic
            class Child extends Parent {
                String run(SimpleArgument arg) { withDelegate({ -> }, arg) }   // unqualified, subtype arg
            }
            new Child().run(new SimpleArgument())
        '''
        assert r == 'target=SimpleArgument' : "baseline D: plain class must resolve inherited static with a subtype argument (got ${r}); contrast trait rows 16/16b"
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

    // ---- Row 7 — T.m() trait-qualified from inside trait body ----
    // Resolves at the JVM level via the trait interface's static method
    // (public trait statics are promoted onto the trait interface). The
    // trait's own copy wins; any same-named static on the implementer is
    // an independent method, not an override (declarer-bound dispatch).
    @Test
    void row07_traitQualified_resolvesViaInterfaceStatic() {
        def r = ev '''
            trait T {
                static String who() { 'T' }
                static String viaTrait() { T.who() }
            }
            class C implements T { static String who() { 'C' } }
            C.viaTrait()
        '''
        assert r == 'T' : "row7: T.m() from trait body must resolve to trait's own copy via interface static (got ${r})"
    }

    // ---- Row 8 — external Impl.m() works AND external Trait.m() works ----
    // Public trait statics are JVM-native interface statics, so both
    // forms of external access resolve cleanly.
    @Test
    void row08_externalAccess() {
        assert ev('trait T { static String foo() { "ok" } }\nclass C implements T {}\nC.foo()') == 'ok'
        assert ev('trait T { static String foo() { "ok" } }\nT.foo()') == 'ok'
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
        // m2 needs @Virtual for A's override to be visible to T's m1
        // (per-implementer override visibility is opt-in).
        def r = ev '''
            import groovy.transform.Virtual
            trait T {
                @Virtual static String m2() { 'T.m2' }
                static String m1() { m2() }
            }
            class A implements T { static String m2() { 'A.m2' } }
            class B extends A    { static String m2() { 'B.m2' } }
            [ a: A.m1(), b: B.m1(), bDirect: B.m2() ]
        '''
        assert r.bDirect == 'B.m2'
        assert r.a == 'A.m2' : "row10 a: A's override must be visible to A.m1() (m2 is @Virtual), got ${r.a}"
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
        println "row12 observed on Groovy ${MAJOR}: ${out} (expected '21' on 4.x and on 5.0.7+ / 6.0.0-alpha-2+)"
        assert out == '21' : "row12: expected '21' (post-GROOVY-11985), got '${out}' — on 5.0.0–5.0.6 / 6.0.0-alpha-1 this returns '22' (the regression boundary)"
    }

    // ---- Row 14 — T.this.* qualifier in trait code (proposed compile error) ----
    // Latent bug surfaced during the GROOVY-12093 alternatives discussion.
    // `T.this.m()` inside trait code currently produces invalid bytecode
    // (VerifyError on 4.x: "Class not assignable to Closure"; ClassCastException
    // at runtime on 5.x/6.x). GEP-22 § "this, super, and stackable traits"
    // item 4 proposes rejecting the syntax at compile time. NYI until the
    // fix lands; flips red on a build that implements the rejection.
    @Test
    void row14_T_this_qualifier_inTrait_isCompileError() {
        GroovyAssert.shouldFail(MultipleCompilationErrorsException) {
            ev '''
                trait V {
                    static boolean defaultNullable() { false }
                    static seen() { V.this.defaultNullable() }
                }
                class Over implements V { static boolean defaultNullable() { true } }
                Over.seen()
            '''
        }
    }

    // ---- Row 15 — unqualified super.m() from trait STATIC method (GROOVY-12105) ----
    // Unqualified `super.m()` inside a trait instance method walks the trait
    // chain (spec item 2); from a trait STATIC method the chain is not
    // walked. GROOVY-12105 rejects the static-context use at compile time,
    // pointing at `T.super.m()` as the explicit form. See Groovy12105.groovy
    // for focused coverage (regression-guards for the instance-method and
    // plain-class super-call cases). This row is the matrix-level anchor.
    @Test
    void row15_unqualified_static_super_inTrait_isCompileError() {
        GroovyAssert.shouldFail(MultipleCompilationErrorsException) {
            ev '''
                trait Base { static String m() { 'Base' } }
                trait V extends Base {
                    static String m() { 'V' }
                    static callSuper() { super.m() }   // unqualified, from static
                }
                class Impl implements V { }
                Impl.callSuper()
            '''
        }
    }

    // ---- Row 16 — child trait resolves an INHERITED parent-trait static (GROOVY-12106) ----
    // Scenario: a child trait `extends` a parent trait under @CompileStatic and its
    // body calls an inherited parent-trait `static` with an argument whose static type
    // is a PROPER SUBTYPE of the declared parameter type (SimpleArgument for the Object
    // parameter). This is the Grails GraphQL DSL helper shape (ExecutesClosures /
    // Arguable). SPEC-NORMATIVE expectation: the inherited static resolves, exactly as a
    // plain unqualified intra-trait static call would.
    //
    // OBSERVED: works on 4.0.x (compiles AND runs). Regressed in the 5.0.x line —
    // STC misroutes the call to the CHILD trait's helper with a synthetic
    // `java.lang.Class` $self first argument:
    //   Cannot find matching method Arguable$Trait$Helper#withDelegate(java.lang.Class, Closure, SimpleArgument)
    // instead of resolving the inherited ExecutesClosures$Trait$Helper. Still red on
    // 5.0.6 / 5.0.7-SNAPSHOT AND on 6.0.0-SNAPSHOT (master): the recent trait-static
    // dispatch work fixed the *qualified* `Parent.m(...)` form on master only, but the
    // unqualified (this row) and `this.`-qualified (row 16b) forms remain broken
    // everywhere in 5.x/6.x and are NOT covered by GROOVY-12104 (T.this.* rejection)
    // or GROOVY-12105 (unqualified static super rejection). An exact-type (Object)
    // argument masks the bug — which is why GROOVY-12106 was first closed
    // "Cannot Reproduce". @NotYetImplemented until the resolution fix lands (and is
    // backported to GROOVY_5_0_X, the line Grails 5.0.7 will consume); flips red when fixed.
    @NotYetImplemented
    @Test
    void row16_childTrait_inheritedParentStatic_subtypeArg_unqualified() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait ExecutesClosures {
                static String withDelegate(Closure cl, Object target) {
                    cl.call()
                    'target=' + target.class.simpleName
                }
            }
            final class SimpleArgument { }
            @CompileStatic
            trait Arguable extends ExecutesClosures {
                String run(SimpleArgument arg) { withDelegate({ -> }, arg) }   // unqualified, subtype arg
            }
            class C implements Arguable { }
            new C().run(new SimpleArgument())
        '''
        assert r == 'target=SimpleArgument' : "row16: child trait must resolve inherited parent-trait static with a subtype argument (got ${r})"
    }

    // ---- Row 16b — same as row 16 but via `this.` (GROOVY-12106) ----
    // `this.m(...)` is the form GROOVY-12104 steers users toward as the supported
    // alternative to the rejected `T.this.m(...)`. For this inherited-parent-trait
    // static case it fails identically to the unqualified form (same misrouted
    // Helper#... receiver), so that escape hatch does not cover case (b). Same
    // version profile and same fix/backport expectation as row 16.
    @NotYetImplemented
    @Test
    void row16b_childTrait_inheritedParentStatic_subtypeArg_thisQualified() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait ExecutesClosures {
                static String withDelegate(Closure cl, Object target) {
                    cl.call()
                    'target=' + target.class.simpleName
                }
            }
            final class SimpleArgument { }
            @CompileStatic
            trait Arguable extends ExecutesClosures {
                String run(SimpleArgument arg) { this.withDelegate({ -> }, arg) }   // this.-qualified, subtype arg
            }
            class C implements Arguable { }
            new C().run(new SimpleArgument())
        '''
        assert r == 'target=SimpleArgument' : "row16b: this.-qualified inherited parent-trait static must resolve with a subtype argument (got ${r})"
    }

    // ---- Row 16q — qualified Parent.m(...) form of rows 16/16b (GROOVY-12106 case (a)) ----
    // The trait-qualified counterpart: the child trait calls the inherited parent-trait
    // static via `ExecutesClosures.withDelegate(...)` with a subtype argument. Unlike the
    // unqualified/`this.` forms (rows 16/16b, broken everywhere in 5.x/6.x), THIS form is
    // already FIXED on master (6.0.0-SNAPSHOT) by the recent trait-static dispatch work,
    // but is NOT in the GROOVY_5_0_X line — it fails to compile on 5.0.6 / 5.0.7-SNAPSHOT
    // (STC: `java.lang.Class#withDelegate`). It is therefore a plain spec assertion (like
    // rows 1/10/12), NOT @NotYetImplemented: green on master, RED when run against 5.0.x —
    // the intended detector for the missing backport (GROOVY-12106 case (a)).
    //
    // Runtime nuance (why this is a real fix, not just a regression): the qualified form
    // COMPILES but throws MissingMethodException at RUNTIME on 4.0.x, fails to COMPILE on
    // 5.0.x, and only compiles AND runs on master. This row asserts the compile+runtime
    // success state, so it tracks the genuinely-correct end-state rather than any prior
    // partial behaviour.
    @Test
    void row16q_childTrait_inheritedParentStatic_subtypeArg_qualified() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait ExecutesClosures {
                static String withDelegate(Closure cl, Object target) {
                    cl.call()
                    'target=' + target.class.simpleName
                }
            }
            final class SimpleArgument { }
            @CompileStatic
            trait Arguable extends ExecutesClosures {
                String run(SimpleArgument arg) { ExecutesClosures.withDelegate({ -> }, arg) }   // qualified, subtype arg
            }
            class C implements Arguable { }
            new C().run(new SimpleArgument())
        '''
        assert r == 'target=SimpleArgument' : "row16q: qualified inherited parent-trait static must resolve+run with a subtype argument (got ${r}) — RED on 5.0.x marks the missing backport (GROOVY-12106 case a)"
    }
}
