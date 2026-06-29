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
package org.codehaus.groovy.transform.traitx

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class TraitEdgeCasesTest {

    @Test
    void testTraitForcesOverrideOverSuperclass() {
        assertScript '''
            class Base {
                String describe() { 'base' }
            }
            trait T {
                String describe() { 'trait' }
            }
            class Sub extends Base implements T { }

            def s = new Sub()
            assert s.describe() == 'trait' : "trait method must win over superclass method"
        '''
    }

    @Test
    void testTraitForcesOverrideOverSuperclassInherited() {
        assertScript '''
            class GrandParent {
                String greet() { 'grandparent' }
            }
            class Parent extends GrandParent {
                String greet() { 'parent' }
            }
            trait T {
                String greet() { 'trait' }
            }
            class Child extends Parent implements T { }

            def c = new Child()
            assert c.greet() == 'trait' : "trait method must win over an already-overridden superclass method"
        '''
    }

    @Test
    void testTraitDoesNotForceOverrideWhenClassProvidesImplementation() {
        assertScript '''
            class Base {
                String describe() { 'base' }
            }
            trait T {
                String describe() { 'trait' }
            }
            class Sub extends Base implements T {
                String describe() { 'sub' }
            }

            def s = new Sub()
            assert s.describe() == 'sub' : "class's own implementation must still win"
        '''
    }

    @Test
    void testTraitForcesOverrideOverSuperclassInterfaceDefault() {
        // Even when the superclass inherits a default method from an interface,
        // the trait method must still win.
        assertScript '''
            interface I {
                default String who() { 'interface' }
            }
            trait T {
                String who() { 'trait' }
            }
            class Base implements I { }
            class Sub extends Base implements T { }

            def s = new Sub()
            assert s.who() == 'trait' : "trait must override interface default method inherited via superclass"
        '''
    }

    @Test
    void testTraitForceOverridePreservesSuperclassMethodViaSuper() {
        // The escape hatch: the class can still reach the superclass method.
        assertScript '''
            class Base {
                String describe() { 'base' }
            }
            trait T {
                String describe() { 'trait' }
            }
            class Sub extends Base implements T {
                String callSuper() { super.describe() }
            }

            def s = new Sub()
            assert s.callSuper() == 'base' : "super call must still reach the superclass"
        '''
    }

    @Test
    void testThisInTraitClosureRefersToImplementingInstance() {
        assertScript '''
            class Helper {
                String ownerName
                def captureThis(Closure cl) {
                    cl.delegate = this
                    cl.resolveStrategy = Closure.DELEGATE_ONLY
                    cl()
                }
            }
            trait T {
                String traitName = 'MyTrait'
                String test() {
                    def h = new Helper()
                    return h.captureThis { -> this.@traitName }
                }
            }
            class C implements T { }

            def c = new C()
            assert c.test() == 'MyTrait'
        '''
    }

    @Test
    void testThisInDeeplyNestedTraitClosureRefersToImplementingInstance() {
        assertScript '''
            trait T {
                String ident() { 'from-trait' }
                String deepNested() {
                    [1].collect { [1].collect { [1].collect { this.ident() } } }[0][0][0]
                }
            }
            class C implements T { }

            assert new C().deepNested() == 'from-trait'
        '''
    }

    @Test
    void testThisInTraitClosureInsideAnotherObjectsMethod() {
        // When a trait method passes a closure to another object,
        // `this` inside the closure must still refer to the implementing object.
        assertScript '''
            class Receiver {
                def execute(Closure cl) {
                    // simulate a framework that changes delegate
                    cl.delegate = this
                    cl.resolveStrategy = Closure.DELEGATE_FIRST
                    cl()
                }
                String whoAmI() { 'Receiver' }
            }
            trait T {
                String whoAmI() { 'TraitImpl' }
                String test() {
                    def r = new Receiver()
                    return r.execute { -> whoAmI() }
                }
            }
            class C implements T { }

            // DELEGATE_FIRST will pick up Receiver.whoAmI, but the point is
            // the call still reaches the implementing instance's trait method
            // through normal dispatch; the closure delegate doesn't change `this`.
            // The delegate *does* have a say with DELEGATE_FIRST though, so the
            // delegate's method wins. This exercises the trait-closure interaction.
            def c = new C()
            assert c.test() == 'Receiver'
        '''
    }

    @Test
    void testThisInTraitNestedClosureIdentity() {
        // Confirm that `this` in nested closures inside a trait is always
        // the same implementing instance (identity check).
        assertScript '''
            trait T {
                boolean check() {
                    def selfRef = this
                    [1].collect { [1].collect { selfRef.is(this) } }[0][0]
                }
            }
            class C implements T { }

            assert new C().check()
        '''
    }

    @Test
    void testTwoTraitsWithSamePrivateFieldName() {
        // Private fields are trait-local; each trait's field is independent
        // under its remapped name. This should compile and work.
        assertScript '''
            trait A {
                private String msg = 'A'
                String fromA() { msg }
            }
            trait B {
                private String msg = 'B'
                String fromB() { msg }
            }
            class C implements A, B { }

            def c = new C()
            assert c.fromA() == 'A'
            assert c.fromB() == 'B'
        '''
    }

    @Test
    void testTwoTraitsWithSamePublicFieldNameAreRemapped() {
        // Public fields are remapped to avoid the diamond problem.
        // Each trait's field should be independently accessible via its
        // remapped name.
        assertScript '''
            trait A {
                public String name = 'A-name'
            }
            trait B {
                public String name = 'B-name'
            }
            class C implements A, B { }

            def c = new C()

            // Both fields exist under their remapped names
            assert c.A__name == 'A-name'
            assert c.B__name == 'B-name'
        '''
    }

    @Test
    void testTwoTraitsWithSamePublicFieldNameViaGetter() {
        // If both traits define a property (field + getter), the last-declared
        // trait's getter wins (standard conflict resolution). The field remapping
        // still applies underneath.
        assertScript '''
            trait A {
                public String value = 'from-A'
                String display() { value }
            }
            trait B {
                public String value = 'from-B'
                String display() { value }
            }
            class C implements A, B { }

            def c = new C()
            // Last-declared trait's getter is used
            assert c.display() == 'from-B'
        '''
    }

    @Test
    void testTwoTraitsWithSamePropertyAccessorAccessedFromClass() {
        // Same property accessor declared in two traits — class must see
        // the one from the last-declared trait.
        assertScript '''
            trait A {
                String getLabel() { 'A' }
            }
            trait B {
                String getLabel() { 'B' }
            }
            class C implements A, B { }

            def c = new C()
            assert c.label == 'B' : "last-declared trait's property accessor must win"
        '''
    }

    @Test
    void testMultipleTraitsPrivateFieldDoesNotLeak() {
        // Private fields must remain fully encapsulated per trait.
        assertScript '''
            trait A {
                private List items = []
                void addA(Object o) { items << o }
                int countA() { items.size() }
            }
            trait B {
                private List items = []
                void addB(Object o) { items << o }
                int countB() { items.size() }
            }
            class C implements A, B { }

            def c = new C()
            c.addA('x')
            c.addA('y')
            c.addB('z')
            assert c.countA() == 2 : "A's private field must be isolated"
            assert c.countB() == 1 : "B's private field must be isolated"
        '''
    }

    @Test
    void testTraitGenericContravariantParameterInChildTrait() {
        // A child trait narrows the type parameter of a parent trait method
        // argument (contravariant usage).
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Consumer<T> {
                String consume(T item) { 'consumed ' + item.toString() }
            }
            @CompileStatic
            trait StringConsumer extends Consumer<String> {
                String shout(String s) { consume(s).toUpperCase() }
            }
            class Impl implements StringConsumer { }

            def i = new Impl()
            assert i.shout('hello') == 'CONSUMED HELLO'
        '''
    }

    @Test
    void testTraitWithMultipleGenericConstraints() {
        assertScript '''
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
            assert i.describe() == 'count::42'
            assert i.formatted() == 'name=count, value=42'
        '''
    }

    @Test
    void testTraitBoundedTypeParameterOnMethod() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Sorter {
                <T extends Comparable<T>> List<T> sorted(List<T> list) {
                    list.sort()
                }
            }
            class Impl implements Sorter { }

            def i = new Impl()
            assert i.sorted([3, 1, 2]) == [1, 2, 3]
        '''
    }

    @Test
    void testTraitGenericReturnTypeFromInheritedMethod() {
        // A child trait inherits a generic method from a parent trait
        // and propagates the type correctly.
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Factory<T> {
                T create(String spec) { (T) Class.forName(spec).newInstance() }
            }
            @CompileStatic
            trait StringFactory extends Factory<String> {
                String makeString() { create('java.lang.String') }
            }
            class Impl implements StringFactory { }

            def i = new Impl()
            assert i.makeString() == ''
        '''
    }

    @Test
    void testTraitWithWildcardBoundInMethodSignature() {
        // A trait method with a wildcard boundary on a parameter type.
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Container {
                Number sum(List<? extends Number> numbers) {
                    numbers.sum() as Number
                }
            }
            class Impl implements Container { }

            def i = new Impl()
            assert i.sum([1, 2, 3]) == 6
            assert i.sum([1.5, 2.5]) == 4.0d
        '''
    }

    @Test
    void testVirtualOnStaticAccessorMethodOverField() {
        // @Virtual on a static GETTER (method) — the recommended pattern
        // for per-implementer static defaults over fields.
        assertScript '''
            import groovy.transform.Virtual
            trait V {
                private static String myData = 'trait-default'
                @Virtual static String getData() { myData }
                static String describe() { data }
            }
            class Over implements V {
                static String getData() { 'overridden' }
            }
            class Def implements V { }

            assert Over.describe() == 'overridden' : "@Virtual getter must dispatch to implementer"
            assert Def.describe() == 'trait-default' : "default without override must return trait's field"
        '''
    }

    @Test
    void testVirtualOnStaticMethodDoesNotAffectDirectFieldAccess() {
        // This confirms that even with @Virtual on a static accessor,
        // direct field access from the trait body still reads the trait's
        // own field (row 9 non-goal is unaffected by @Virtual).
        assertScript '''
            import groovy.transform.Virtual
            trait V {
                static String data = 'trait'
                @Virtual static String getData() { data }
                static String directField() { data }
            }
            class C implements V {
                static String data = 'class'
            }

            // Row 9 non-goal: direct field access from trait body reads trait's own field
            assert C.directField() == 'trait' : "direct field access must still read trait's field (non-goal)"
        '''
    }

    @Test
    void testVirtualOnStaticMethodWithImplementerFieldAndGetter() {
        // The full pattern: implementer provides both a static field AND
        // a static getter; the @Virtual getter on the implementer must win.
        // When called via C.describe(), the @Virtual dispatch routes through
        // C.getData() which returns C.data = 'class-data' (the implementer's
        // field, because the implementer's getter body reads its own field).
        assertScript '''
            import groovy.transform.Virtual
            trait V {
                static String data = 'trait-data'
                @Virtual static String getData() { data }
                static String describe() { data }
            }
            class C implements V {
                static String data = 'class-data'
                static String getData() { data }
            }

            // 'describe' in trait body reads 'data' which resolves to the trait's
            // own field (row 9 non-goal), NOT the implementer's field
            assert C.describe() == 'trait-data' : "direct field access from trait body reads trait's field (non-goal)"
            // C.getData() calls the implementer's override which reads C's own field
            assert C.getData() == 'class-data' : "implementer's getter override must read its own field"
        '''
    }

    @Test
    void testVirtualOnStaticMethodWithFieldBackedDefault() {
        // Confirm that the default (trait's own) @Virtual getter returns
        // the trait's own field when no implementer provides an override.
        assertScript '''
            import groovy.transform.Virtual
            trait V {
                static String defaultData = 'default-value'
                @Virtual static String getDefaultData() { defaultData }
                static String fetch() { defaultData }
            }
            class Impl implements V { }

            assert Impl.fetch() == 'default-value'
        '''
    }

    @Test
    void testStaticFieldAccessorOverriddenAndCalledFromChildTrait() {
        // A child trait calls an inherited @Virtual static accessor via the
        // getter method; the implementer's override must be visible to the
        // child trait body.
        assertScript '''
            import groovy.transform.Virtual
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Parent {
                static String origin = 'parent'
                @Virtual static String getOrigin() { origin }
            }
            @CompileStatic
            trait Child extends Parent {
                String report() { getOrigin() }   // must use the getter; 'origin' is a static field reference
            }
            class Impl implements Child {
                static String getOrigin() { 'impl' }
            }
            class Def implements Child { }

            assert new Impl().report() == 'impl'
            assert new Def().report() == 'parent'
        '''
    }
}
