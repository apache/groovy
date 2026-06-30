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

// Trait-generics behaviour matrix — the executable companion to the
// governing principle:
//
//   "Generics in traits are applied the same way as outside traits."
//
// That principle is SEMANTICALLY correct but, like GEP-22's P1' ("static
// dispatch like a plain class"), it is not self-enforcing: traits have no
// native generics, so the compiler RECONSTRUCTS ordinary generic semantics
// across a lowering that has no analogue outside traits — a trait interface
// (carries signatures), a static $Trait$Helper (each instance method m(args)
// becomes static m($self, args); each static method becomes static
// m($static$self, args)), remapped fields, synthetic bridges, and stackable
// T.super dispatch. Every one of those reconstruction points is a SEAM where
// the principle could silently fail. The static-dispatch side, under the
// analogous principle, regressed three times unnoticed (GROOVY-8854 ->
// GROOVY-11985, then GROOVY-12106, then GROOVY-12117) precisely because the
// seams were unpinned.
//
// Each baseline_* row records what plain Groovy does for a shape (a generic
// SUPERCLASS, not a bare generic interface — traits carry state and method
// bodies, so the superclass model is the right baseline). Each rowNN_* seam
// asserts the trait machinery reproduces that baseline. The rows characterise
// where trait generics MATCH ordinary generics through a non-ordinary
// mechanism; they pass on a faithful build and turn RED if a future change to
// helper lowering, field remapping, bridge generation, or the static-dispatch
// machinery breaks the reconstruction. That red is the intended bug-detector.
//
// Run via gradle: `./gradlew :test --tests '*.TraitGenericsMatrix'`

package org.codehaus.groovy.transform.traitx

import groovy.test.GroovyAssert
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

class TraitGenericsMatrix {

    /** Compile+run a snippet with the *running* Groovy compiler. */
    private static Object ev(String src) { new GroovyShell().evaluate(src) }

    // =================== OUTSIDE-TRAITS BASELINE ===================
    // What plain Groovy does for the same generic shapes the seam rows test.
    // Read these first — the seam rows assert that trait machinery reproduces
    // exactly this, not that it departs from it. The correct baseline is a
    // generic ABSTRACT SUPERCLASS: a trait contributes state and method bodies
    // to the implementer, so it behaves like `class Impl extends Base<Arg>`,
    // NOT like `class Impl implements Iface<Arg>` (which carries signatures
    // only).

    // ---- Baseline field — generic field type visible through inheritance ----
    // Control for row 4/row 9. A generic superclass field reads at the bound's
    // reified type in a subclass that fixed the parameter: value is String, so
    // value.length() type-checks under @CompileStatic.
    @Test
    void baseline_genericSuperclassFieldTypeVisibleUnderCS() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            abstract class Base<T> { T value }
            @CompileStatic
            class C extends Base<String> { int len() { value.length() } }
            def c = new C(); c.value = 'hello'
            c.len()
        '''
        assert r == 5 : "baseline: generic superclass field must type as the fixed argument under @CS (got ${r})"
    }

    // ---- Baseline self-type — F-bounded SELF round-trips ----
    // Control for row 7. `chain()` returns SELF; in C extends SelfBase<C> it
    // types as C, so a C-only method resolves off the result under @CS.
    @Test
    void baseline_genericSuperclassSelfTypeRoundTrips() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            abstract class SelfBase<S extends SelfBase<S>> { S me() { (S) this }; S chain() { me() } }
            @CompileStatic
            class C extends SelfBase<C> { String hi() { 'hi' } }
            @CompileStatic
            String go() { new C().chain().hi() }
            go()
        '''
        assert r == 'hi' : "baseline: F-bounded self-type must round-trip to the subtype under @CS (got ${r})"
    }

    // ---- Baseline conflict — same generic super implemented with two args ----
    // Control for row 11. A class reaching one generic type via two different
    // bindings is rejected with a message that names the binding conflict.
    @Test
    void baseline_interfaceConflictingBindingsRejectedWithClearMessage() {
        def err = GroovyAssert.shouldFail(MultipleCompilationErrorsException) {
            ev '''
                interface Box<T> {}
                interface StringBox extends Box<String> {}
                interface IntBox extends Box<Integer> {}
                class C implements StringBox, IntBox {}
                new C()
            '''
        }
        assert err.message.contains('different arguments') :
            "baseline: conflicting generic bindings must be rejected naming the conflict (got: ${err.message})"
    }

    // =================== TRAIT-GENERICS SEAMS ===================

    // ---- Row 1 — class type-param (with bounds) promoted onto helper static ----
    // SEAM: a trait's class-level type parameter cannot live on a static helper
    // method, so it is promoted to a METHOD-level parameter on each helper
    // static: `static <T extends Number & Comparable<T>> T max(Box<T> $self,
    // T a, T b)`. No code outside traits performs this class->method promotion.
    // The intersection bound AND the F-bound must both survive, or a.compareTo
    // fails to resolve under @CompileStatic.
    @Test
    void row01_classTypeParamWithBoundsPromotedToHelperStatic() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Box<T extends Number & Comparable<T>> {
                T max(T a, T b) { a.compareTo(b) >= 0 ? a : b }
            }
            class IntBox implements Box<Integer> {}
            new IntBox().max(3, 7)
        '''
        assert r == 7 : "row1: intersection/F-bound class param must survive promotion onto the helper static (got ${r})"
    }

    // ---- Row 2 — class<X> vs method<X> name collision when flattened ----
    // SEAM: an instance method's own type parameter sharing a name with the
    // trait's class parameter is ordinary shadowing on the trait, but BOTH land
    // on one flattened helper static, where two same-named parameters would
    // collide at the JVM level. The lowering must rename one. Both forms must
    // still resolve to the value-correct method.
    @Test
    void row02_classVsMethodTypeParamShadowingFlattened() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait T<X> {
                X classParam(X x) { x }
                def <X> X methodParam(X x) { x }
            }
            class C implements T<Number> {}
            def c = new C()
            [c.classParam(42), c.methodParam('hi')]
        '''
        assert r == [42, 'hi'] : "row2: class/method type-param shadowing must survive flattening onto one helper static (got ${r})"
    }

    // ---- Row 3 — interface signature and helper signature agree ----
    // SEAM: a generic trait method has two representations — the abstract
    // signature on the trait interface (what STC resolves against from an
    // implementer) and the lowered static on the helper (what trait-body calls
    // resolve against and what dispatches). They must agree on bounds/erasure,
    // or the same call type-checks from one site and not the other. Outside
    // traits there is a single method, so no agreement to maintain.
    @Test
    void row03_interfaceAndHelperSignatureAgree() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait T {
                <E extends CharSequence> int firstLen(List<E> xs) { xs.get(0).length() }
                int fromTraitBody() { firstLen(['ab', 'c']) }   // resolves via HELPER
            }
            @CompileStatic
            class C implements T {
                int fromImpl() { firstLen(['xyz', 'q']) }       // resolves via INTERFACE
            }
            def c = new C()
            [c.fromTraitBody(), c.fromImpl()]
        '''
        assert r == [2, 3] : "row3: trait body (helper) and implementer (interface) must resolve a generic method identically (got ${r})"
    }

    // ---- Row 4 — generic field type survives remapping under @CS ----
    // SEAM: a trait field is remapped onto the implementing class (public:
    // <Trait>__<field>; private: name-mangled) and exposed through generated
    // accessors. The fixed type argument must survive that remapping so a bare
    // `value` read in implementer code types as String, matching the generic-
    // superclass baseline above. The remapping itself is trait-only.
    @Test
    void row04_genericFieldTypeThroughRemapping() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Holder<T> { T value }
            @CompileStatic
            class C implements Holder<String> { int len() { value.length() } }
            def c = new C(); c.value = 'hello'
            c.len()
        '''
        assert r == 5 : "row4: generic trait field must type as the fixed argument through remapping under @CS (got ${r})"
    }

    // ---- Row 5 — generic static inherited across a sub-trait (sub-first) ----
    // SEAM: the static-dispatch machinery (the TraitStaticDispatchMatrix
    // subject) intersected with generics. A generic super-trait static, called
    // unqualified from a sub-trait body under @CompileStatic, must resolve via
    // the inherited-static rewrite — including when the sub-trait is declared
    // FIRST (GROOVY-12117 ordering) so the super-trait's helper is not yet
    // generated. The promoted method type var and the synthetic Class
    // $static$self must not defeat subtype-aware overload resolution.
    @Test
    void row05_genericStaticInheritedAcrossSubTrait_subFirst() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Q extends P {                 // sub-trait declared FIRST
                Integer use() { id(3) }         // inherited generic static; returns T
            }
            @CompileStatic
            trait P {
                static <T extends Number> T id(T x) { x }
            }
            class Impl implements Q {}
            new Impl().use()
        '''
        assert r == 3 : "row5: generic super-trait static must resolve from a sub-trait body regardless of declaration order (got ${r})"
    }

    // ---- Row 6 — covariant generic return: bridge composes with forwarder ----
    // SEAM: a sub-trait narrowing a generic return (Producer<String>.produce
    // returns String, overriding T produce()) forces a synthetic covariant
    // bridge. That bridge must compose with the trait's own forwarder so the
    // narrowed return type is visible under @CompileStatic. Bridge x forwarder
    // composition is trait-only; this area has carried bugs historically.
    @Test
    void row06_covariantGenericReturnBridgeComposesWithForwarder() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Producer<T> { T produce() { null } }
            @CompileStatic
            trait StringProducer extends Producer<String> { String produce() { 'made' } }
            class C implements StringProducer {}
            @CompileStatic
            String go() { new C().produce().toUpperCase() }
            go()
        '''
        assert r == 'MADE' : "row6: covariant generic return must stay visible through the bridge+forwarder (got ${r})"
    }

    // ---- Row 7 — self-type round-trips through the trait lowering ----
    // SEAM: the generic-superclass self-type baseline, but expressed as a
    // trait. `this` in trait code is the implementing instance, whose type the
    // trait does not statically know; the F-bounded SELF parameter carries it.
    // chain() must type as the implementer under @CS, matching the baseline.
    @Test
    void row07_selfTypeRoundTrips() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Self<SELF extends Self<SELF>> { SELF me() { (SELF) this }; SELF chain() { me() } }
            @CompileStatic
            class C implements Self<C> { String hi() { 'hi' } }
            @CompileStatic
            String go() { new C().chain().hi() }
            go()
        '''
        assert r == 'hi' : "row7: F-bounded self-type must round-trip to the implementer through the trait lowering (got ${r})"
    }

    // ---- Row 8 — stackable T.super.m() with generics ----
    // SEAM: stackable-trait super dispatch generates a renamed super-trait
    // bridge (getSuperTraitMethodName). With a generic method the bridge must
    // preserve the type parameter so A.super.transform(x) keeps T. No analogue
    // outside traits (classes have no stackable super).
    @Test
    void row08_stackableSuperWithGenerics() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait A<T> { T transform(T x) { x } }
            @CompileStatic
            trait B<T> extends A<T> { T transform(T x) { A.super.transform(x) } }
            class C implements B<String> {}
            new C().transform('z')
        '''
        assert r == 'z' : "row8: T.super.m() must preserve the generic type parameter (got ${r})"
    }

    // ---- Row 9 — generic collection field element type through remapping ----
    // SEAM: row 4 deepened — a List<T> field plus inherited generic methods.
    // The element type (T=String) must survive field remapping AND the helper
    // lowering of add(T)/firstItem() so firstItem().length() type-checks.
    @Test
    void row09_genericCollectionFieldElementType() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Repo<T> {
                List<T> items = []
                void add(T t) { items.add(t) }
                T firstItem() { items.get(0) }
            }
            @CompileStatic
            class C implements Repo<String> { int firstLen() { firstItem().length() } }
            def c = new C(); c.add('abc')
            c.firstLen()
        '''
        assert r == 3 : "row9: generic collection field element type must survive remapping + helper lowering under @CS (got ${r})"
    }

    // ---- Row 10 — @Virtual generic static, per-implementer override ----
    // SEAM: generics intersected with @Virtual per-implementer static dispatch.
    // A @Virtual generic static is overridden on the implementer; a trait-body
    // call must dispatch to the implementer's generic override (single-element
    // here), not the trait default.
    @Test
    void row10_virtualGenericStaticOverride() {
        def r = ev '''
            import groovy.transform.Virtual
            trait V {
                @Virtual static <T> List<T> wrap(T x) { [x, x] }
                static List describe(Object o) { wrap(o) }
            }
            class C implements V {
                static <T> List<T> wrap(T x) { [x] }
            }
            C.describe('a')
        '''
        assert r == ['a'] : "row10: @Virtual generic static must dispatch to the implementer's override from trait code (got ${r})"
    }

    // ---- Row 11 — conflicting generic super-trait bindings: outcome parity ----
    // SEAM + KNOWN DIAGNOSTIC GAP. Composing two traits that bind the same
    // generic super-trait to different arguments is REJECTED, matching the
    // interface baseline's outcome (the principle holds for accept/reject).
    // It is asserted as rejection only, NOT on message text: the trait path
    // currently reports a SYMPTOM at the synthesized accessor ("return type of
    // Integer getVal() ... incompatible with String") rather than naming the
    // binding conflict the way the baseline does. That diagnostic divergence is
    // the open gap; if a future change makes the message match the baseline,
    // this test should keep passing (hence no message assertion).
    @Test
    void row11_conflictingGenericBindingsRejected_outcomeParity() {
        GroovyAssert.shouldFail(MultipleCompilationErrorsException) {
            ev '''
                trait Box<T> { T val }
                trait StringBox extends Box<String> {}
                trait IntBox extends Box<Integer> {}
                class C implements StringBox, IntBox {}
                new C()
            '''
        }
    }

    // ---- Row 12 — bounded params + generic super-trait binding + generic fields ----
    // COMBINED seam (PR #2646 edge case 4b): a child trait with TWO type
    // parameters, one bounded (N extends CharSequence), binds a generic
    // super-trait (extends Pair<N, V>) whose state is two generic fields
    // (A first, B second). This exercises row 1 (bounded class param promoted
    // onto helper statics), row 3 (super-trait interface<->helper agreement)
    // and row 4 (generic field remapping) in a single shape — the place all
    // three seams meet. Matches the generic-superclass baseline.
    @Test
    void row12_boundedParamsGenericSuperTraitAndFieldsCombined() {
        def r = ev '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Pair<A,B> {
                A first
                B second
                String describe() { "${first}::${second}" }
            }
            @CompileStatic
            trait NamedPair<N extends CharSequence, V> extends Pair<N, V> {
                String formatted() { "name=${first}, value=${second}" }
            }
            class Impl implements NamedPair<String, Integer> { }
            def i = new Impl(first: 'count', second: 42)
            [i.describe(), i.formatted()]
        '''
        assert r == ['count::42', 'name=count, value=42'] :
            "row12: bounded param + generic super-trait binding + generic fields must compose (got ${r})"
    }
}
