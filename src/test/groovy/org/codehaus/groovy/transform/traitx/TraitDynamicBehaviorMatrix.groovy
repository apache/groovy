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

// Trait dynamic-behaviour matrix — the runtime/MOP companion to the static
// and generics matrices. Its governing principle has two halves:
//
//   1. Trait-contributed members participate in the runtime MOP exactly as
//      the implementing class's OWN declared members (methodMissing,
//      propertyMissing, invokeMethod, respondsTo, hasProperty, metaClass
//      override).
//   2. Traits ADD two runtime-only capabilities with no plain-class analogue:
//      `as` coercion (decorate an existing instance) and `withTraits`
//      (apply a trait to an instance at runtime).
//
// This surface is DOCUMENTED in src/spec/doc/_traits.adoc ("Duck typing and
// traits", "Dynamic methods in a trait", "Runtime implementation of traits")
// but, unlike the static-dispatch surface, is barely covered by regression
// tests (respondsTo / invokeMethod / ExpandoMetaClass / coercion-identity had
// effectively zero unit coverage). This matrix is the regression NET under
// that already-specified behaviour: the rows assert OBSERVED runtime
// behaviour, turn RED if the trait-lowering x MOP seam breaks, and the
// baseline_* rows record what plain Groovy does for the MOP-participation
// shapes so a row characterises a MATCH, not a departure.
//
// Everything here runs dynamically (no @CompileStatic) — that IS the subject.
// The dynamic-vs-static axis for ordinary trait shapes is already covered by
// TraitStaticDispatchMatrix's dyn rows and TraitASTTransformationTest's
// @CompileModesTest; this file does NOT re-run those.
//
// Run via gradle: `./gradlew :test --tests '*.TraitDynamicBehaviorMatrix'`

package org.codehaus.groovy.transform.traitx

import org.junit.jupiter.api.Test

class TraitDynamicBehaviorMatrix {

    /** Compile+run a snippet with the *running* Groovy compiler, dynamically. */
    private static Object ev(String src) { new GroovyShell().evaluate(src) }

    // =================== PLAIN-CLASS MOP BASELINE ===================
    // What plain Groovy does for the MOP-participation shapes. The trait rows
    // below assert the same behaviour reached through trait machinery. (The
    // runtime-capability rows — `as` / withTraits — have no plain-class
    // baseline; they are trait-only features.)

    // ---- Baseline — plain class methodMissing ----
    @Test
    void baseline_plainClassMethodMissing() {
        def r = ev '''
            class Duck { def methodMissing(String name, args) { "quack:$name" } }
            new Duck().fly()
        '''
        assert r == 'quack:fly' : "baseline: plain-class methodMissing handles a missing call (got ${r})"
    }

    // ---- Baseline — plain class respondsTo / hasProperty ----
    @Test
    void baseline_plainClassRespondsToAndHasProperty() {
        def r = ev '''
            class C { String foo() { 'f' }; String bar = 'b' }
            def c = new C()
            [c.respondsTo('foo') as boolean, c.hasProperty('bar') as boolean]
        '''
        assert r == [true, true] : "baseline: respondsTo/hasProperty see a plain class's own members (got ${r})"
    }

    // =================== MOP PARTICIPATION (trait members behave like own) ===================

    // ---- Row 1 — trait-defined methodMissing handles the implementer's misses ----
    // SEAM: a trait implementing the MOP hook methodMissing is woven onto the
    // implementer so a missing call on the implementer is handled by the trait,
    // matching the plain-class baseline. (_traits.adoc "Dynamic methods in a trait".)
    @Test
    void row01_traitMethodMissing() {
        def r = ev '''
            trait DynamicTrait { def methodMissing(String name, args) { "missing:$name" } }
            class C implements DynamicTrait {}
            new C().anyMethod()
        '''
        assert r == 'missing:anyMethod' : "row1: trait methodMissing must handle the implementer's missing calls (got ${r})"
    }

    // ---- Row 2 — trait-defined propertyMissing handles the implementer's misses ----
    @Test
    void row02_traitPropertyMissing() {
        def r = ev '''
            trait PropTrait { def propertyMissing(String name) { "prop:$name" } }
            class C implements PropTrait {}
            new C().anything
        '''
        assert r == 'prop:anything' : "row2: trait propertyMissing must handle the implementer's missing properties (got ${r})"
    }

    // ---- Row 3 — trait invokeMethod intercepts MISSING calls; real calls are direct ----
    // SEAM + clarification: a trait's invokeMethod (without GroovyInterceptable)
    // acts as a missing-method fallback, NOT a blanket interceptor — real() is
    // dispatched directly, ghost() routes through invokeMethod. This mirrors
    // plain-class invokeMethod semantics.
    @Test
    void row03_traitInvokeMethodInterceptsMissingOnly() {
        def r = ev '''
            trait T { Object invokeMethod(String name, Object args) { "invoked:$name" }; String real() { 'real' } }
            class C implements T {}
            def c = new C()
            [c.real(), c.ghost()]
        '''
        assert r == ['real', 'invoked:ghost'] : "row3: trait invokeMethod intercepts only missing calls; real methods dispatch directly (got ${r})"
    }

    // ---- Row 4 — a real trait method wins over the implementer's methodMissing ----
    // SEAM: precedence. A woven trait method is a real method on the implementer,
    // so it resolves normally; only genuinely missing names reach methodMissing.
    @Test
    void row04_realTraitMethodWinsOverImplementerMethodMissing() {
        def r = ev '''
            trait T { String foo() { 'trait-foo' } }
            class C implements T { def methodMissing(String name, args) { "missing:$name" } }
            def c = new C()
            [c.foo(), c.bar()]
        '''
        assert r == ['trait-foo', 'missing:bar'] : "row4: a real trait method must win over the implementer's methodMissing (got ${r})"
    }

    // ---- Row 5 — respondsTo / hasProperty see trait-contributed members ----
    @Test
    void row05_respondsToAndHasPropertySeeTraitMembers() {
        def r = ev '''
            trait T { String foo() { 'f' }; String bar = 'b' }
            class C implements T {}
            def c = new C()
            [c.respondsTo('foo') as boolean, c.hasProperty('bar') as boolean]
        '''
        assert r == [true, true] : "row5: introspection must see trait-contributed members like the implementer's own (got ${r})"
    }

    // ---- Row 6 — ExpandoMetaClass override wins over a trait method at runtime ----
    // SEAM: a trait method is a normal instance method on the implementer, so a
    // runtime metaClass override shadows it exactly as it would shadow a
    // class-declared method. Confirms trait methods are not privileged against
    // the MOP.
    @Test
    void row06_expandoMetaClassOverridesTraitMethod() {
        def r = ev '''
            trait T { String greet() { 'trait' } }
            class C implements T {}
            C.metaClass.greet = { -> 'emc' }
            new C().greet()
        '''
        assert r == 'emc' : "row6: a runtime metaClass override must win over a trait method (got ${r})"
    }

    // ---- Row 7 — remapped public trait fields are reachable dynamically ----
    // SEAM: two traits' same-named public fields are remapped to <Trait>__<field>
    // (the diamond-avoidance scheme); both must be reachable at runtime by name,
    // via property and subscript access.
    @Test
    void row07_remappedPublicFieldDynamicAccess() {
        def r = ev '''
            trait A { public String name = 'A-name' }
            trait B { public String name = 'B-name' }
            class C implements A, B {}
            def c = new C()
            [c.A__name, c.B__name, c['A__name']]
        '''
        assert r == ['A-name', 'B-name', 'A-name'] : "row7: remapped public trait fields must be dynamically reachable by name (got ${r})"
    }

    // =================== RUNTIME-ONLY CAPABILITIES (no plain-class analogue) ===================

    // ---- Row 8 — `as` coercion: new instance, original interfaces preserved ----
    // CAPABILITY: coercing an instance to a trait decorates it. Per _traits.adoc
    // ("Implementing a trait at runtime") the result is NOT the same instance,
    // yet implements both the trait AND the original's interfaces.
    @Test
    void row08_asCoercionIsNewInstancePreservingInterfaces() {
        def r = ev '''
            interface Named { String name() }
            trait Extra { String extra() { 'extra' } }
            class Something implements Named { String name() { 'orig' } }
            def s = new Something()
            def e = s as Extra
            [e instanceof Extra, e instanceof Named, e.extra(), e.name(), s.is(e)]
        '''
        assert r == [true, true, 'extra', 'orig', false] : "row8: `as` coercion yields a new instance implementing trait + original interfaces (got ${r})"
    }

    // ---- Row 9 — `as` coercion: trait method decorates; original is untouched ----
    // CAPABILITY: on the coerced proxy the trait's method overrides the original
    // object's same-named method; the original instance is unchanged.
    @Test
    void row09_asCoercionTraitMethodOverridesOriginalOnProxyOnly() {
        def r = ev '''
            interface Speaker { String speak() }
            trait Loud { String speak() { 'LOUD' } }
            class Quiet implements Speaker { String speak() { 'quiet' } }
            def q = new Quiet()
            def loud = q as Loud
            [loud.speak(), q.speak()]
        '''
        assert r == ['LOUD', 'quiet'] : "row9: trait method decorates the coerced proxy; the original instance is untouched (got ${r})"
    }

    // ---- Row 10 — withTraits applies a trait to an instance at runtime ----
    // CAPABILITY: withTraits returns a new object implementing the trait, with
    // the trait's methods and field defaults available; the original is not it.
    @Test
    void row10_withTraitsAppliesTraitAtRuntime() {
        def r = ev '''
            trait T { String hello() { 'hi-' + greetee }; String greetee = 'world' }
            class C {}
            def base = new C()
            def c = base.withTraits(T)
            [c.hello(), c instanceof T, base.is(c)]
        '''
        assert r == ['hi-world', true, false] : "row10: withTraits yields a new trait-implementing object with trait state available (got ${r})"
    }

    // ---- Row 11 — withTraits conflict: last-declared trait wins ----
    // CAPABILITY: the same last-declared-wins resolution as static composition,
    // applied to the runtime-assembled object.
    @Test
    void row11_withTraitsLastDeclaredWinsOnConflict() {
        def r = ev '''
            trait A { String who() { 'A' } }
            trait B { String who() { 'B' } }
            class C {}
            new C().withTraits(A, B).who()
        '''
        assert r == 'B' : "row11: withTraits conflict resolves to the last-declared trait (got ${r})"
    }

    // ---- Row 12 — Map coercion to a trait with an abstract method ----
    // CAPABILITY: a Map of name->Closure coerced to a trait satisfies the
    // trait's abstract methods, like interface/SAM map coercion.
    @Test
    void row12_mapCoercionToTraitAbstractMethod() {
        def r = ev '''
            trait Greeter { abstract String greet() }
            def g = [greet: { 'hi' }] as Greeter
            [g instanceof Greeter, g.greet()]
        '''
        assert r == [true, 'hi'] : "row12: a Map coerced to a trait must satisfy its abstract methods (got ${r})"
    }

    // ---- Row 13 — SAM coercion: a Closure coerced to a single-abstract-method trait ----
    // CAPABILITY (GEP-22 § SAM coercion): a trait with exactly one abstract
    // method is a SAM type; a Closure coerced to it becomes that method's
    // implementation. (Distinct from row 12's Map coercion.)
    @Test
    void row13_closureSamCoercionToTrait() {
        def r = ev '''
            trait Greeter { abstract String greet() }
            def g = { 'hi' } as Greeter
            [g instanceof Greeter, g.greet()]
        '''
        assert r == [true, 'hi'] : "row13: a Closure coerced to a single-abstract-method trait must implement that method (got ${r})"
    }
}
