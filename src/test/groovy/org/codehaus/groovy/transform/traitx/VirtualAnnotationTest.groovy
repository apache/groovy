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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Exercises the {@code @groovy.transform.Virtual} marker on trait static
 * methods.
 *
 * <p>Public trait static methods are promoted onto the generated trait
 * interface as JVM-native interface statics and dispatched declarer-bound
 * by default. Per-implementer override visibility is opt-in via
 * {@code @Virtual}:
 *
 * <pre>
 *   plain  static m()              -&gt; declarer-bound + on the interface (default)
 *   {@literal @}Virtual static m()  -&gt; virtual via implementer
 * </pre>
 */
final class VirtualAnnotationTest {

    // ============ Default (no annotations) — declarer-bound ============

    /** Plain {@code static}: trait body sees the trait's own copy, not the impl override. */
    @Test
    void plainStatic_isDeclarerBound_byDefault() {
        assertScript '''
            trait V {
                static boolean defaultNullable() { false }
                static boolean seen() { this.defaultNullable() }
            }
            class Over implements V { static boolean defaultNullable() { true } }

            // Direct call: implementer's same-named static is independent.
            assert Over.defaultNullable() == true

            // Trait body: now sees only the trait's own copy (declarer-bound).
            assert Over.seen() == false
        '''
    }

    // ============ @Virtual — virtual dispatch via implementer ============

    /** {@code @Virtual}: trait body dispatches through the implementer. */
    @Test
    void virtualStatic_overrideVisibleToTraitBody() {
        assertScript '''
            import groovy.transform.Virtual

            trait V {
                @Virtual
                static boolean defaultNullable() { false }
                static boolean seen() { this.defaultNullable() }   // trait-body call
            }
            class Over implements V { static boolean defaultNullable() { true } }
            class Def  implements V { }

            // Direct call: override always wins
            assert Over.defaultNullable() == true

            // Trait body sees the implementer's override
            assert Over.seen() == true
            assert Def.seen()  == false   // no override → trait default
        '''
    }

    /**
     * Worked example: a framework trait declares a {@code @Virtual} static
     * default that user classes override by declaring a same-signature
     * static. Trait-internal logic that consumes the value sees the
     * implementer's override.
     */
    @Test
    void virtual_overridableStaticDefault_workedExample() {
        assertScript '''
            import groovy.transform.Virtual

            trait Validateable {
                @Virtual
                static boolean defaultNullable() { false }

                // Trait-internal method that consumes the (potentially overridden) value
                static boolean defaultNullableSeenByTrait() {
                    this.defaultNullable()
                }
            }

            class MyNullableCommand implements Validateable {
                static boolean defaultNullable() { true }
            }
            class MyOtherCommand implements Validateable { /* uses default */ }

            assert MyNullableCommand.defaultNullable() == true
            assert MyNullableCommand.defaultNullableSeenByTrait() == true   // override visible
            assert MyOtherCommand.defaultNullableSeenByTrait() == false     // trait default
        '''
    }

    /** {@code @Virtual} is keyed on the callee, not the caller — same discipline as private static. */
    @Test
    void virtual_isKeyedOnCallee() {
        assertScript '''
            import groovy.transform.Virtual

            trait V {
                static       boolean fixed()        { false }   // plain → declarer-bound
                @Virtual
                static       boolean overridable()  { false }   // virtual
                static List<Boolean> bothSeen() {
                    [this.fixed(), this.overridable()]
                }
            }
            class Over implements V {
                static boolean fixed()       { true }
                static boolean overridable() { true }
            }
            assert Over.bothSeen() == [false, true]   // fixed stays trait's, virtual sees override
        '''
    }

    /** {@code @Virtual} under @CompileStatic also dispatches virtually. */
    @Test
    void virtualStatic_under_CompileStatic() {
        assertScript '''
            import groovy.transform.Virtual

            @groovy.transform.CompileStatic
            trait V {
                @Virtual
                static String name() { 'trait' }
                static String seen() { this.name() }
            }
            @groovy.transform.CompileStatic
            class Impl implements V { static String name() { 'impl' } }

            assert Impl.seen() == 'impl'
        '''
    }

    // ============ Validation ============

    /** {@code @Virtual} on an instance method → compile error. */
    @Test
    void virtual_onInstanceMethod_isCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                import groovy.transform.Virtual
                trait V {
                    @Virtual
                    def m() { 'V' }          // instance, not static
                }
                new Object()
            '''
        }
        assert err.message.contains('@Virtual can only be applied to public static trait methods')
        assert err.message.contains('is not static')
    }

    /** {@code @Virtual} on a private static → compile error. */
    @Test
    void virtual_onPrivateStatic_isCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                import groovy.transform.Virtual
                trait V {
                    @Virtual
                    private static m() { 'V' }
                }
                new Object()
            '''
        }
        assert err.message.contains('@Virtual can only be applied to public static trait methods')
        assert err.message.contains('is private')
    }


    // ============ Closure asymmetry (unchanged from current spec) ============

    /**
     * Closure carve-out: a static call inside a closure body inside a trait method
     * is helper-bound, even when the callee is {@code @Virtual}. This is the
     * long-standing closure-context asymmetry; the {@code @Virtual} marker
     * does not extend into closure scope.
     */
    @Test
    void virtual_insideClosure_stillHelperBound() {
        assertScript '''
            import groovy.transform.Virtual

            trait V {
                @Virtual
                static boolean defaultNullable() { false }
                static boolean seenInClosure() {
                    [1].collect { this.defaultNullable() }[0]   // closure body
                }
            }
            class Over implements V { static boolean defaultNullable() { true } }

            // The closure carve-out keeps the call helper-bound even with @Virtual.
            // Same asymmetry as the current spec's row 2; documented as deliberate.
            assert Over.seenInClosure() == false
        '''
    }

    // ============ Instance method dispatch is unaffected (regression guard) ============

    /** Instance methods dispatch polymorphically — the {@code @Virtual} marker is for trait statics only. */
    @Test
    void instanceMethods_dispatchUnchanged() {
        assertScript '''
            trait V {
                def which() { 'trait' }
                def greet() { this.which() }
            }
            class C implements V { def which() { 'class' } }
            class D implements V { }

            assert new C().greet() == 'class'   // override wins (standard polymorphism)
            assert new D().greet() == 'trait'   // trait default
        '''
    }
}
