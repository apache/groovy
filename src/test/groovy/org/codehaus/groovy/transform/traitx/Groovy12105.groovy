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
 * Unqualified {@code super.m(...)} from a static trait method is rejected
 * at compile time (GROOVY-12105). Without the reject, the call previously
 * compiled successfully and threw {@code MissingMethodException} at
 * runtime — the trait helper does not walk the trait chain for statics.
 * The supported explicit form is {@code T.super.m(...)} (GEP-22 § this,
 * super, and stackable traits item 3).
 *
 * <p>This restriction applies to <em>trait</em> static methods only.
 * Groovy continues to permit unqualified {@code super.m(...)} in static
 * methods of plain classes, where it dispatches to the superclass's
 * static method (Java-conventional behaviour for class hierarchies) —
 * see {@link #unqualifiedSuper_inPlainClassStatic_stillWorks()}.
 */
final class Groovy12105 {

    /** Unqualified super in static trait method → compile error. */
    @Test
    void unqualifiedSuper_inStaticTraitMethod_isCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                trait Base {
                    static String m() { 'Base' }
                }
                trait V extends Base {
                    static String m() { 'V' }
                    static callSuper() { super.m() }   // <-- rejected
                }
                class Impl implements V { }
                Impl.callSuper()
            '''
        }
        assert err.message.contains("'super' is not allowed in a static trait method")
        assert err.message.contains('V.super.m(')
    }

    /** Unqualified super in static trait method, no super-trait → compile error. */
    @Test
    void unqualifiedSuper_inStaticTraitMethod_noSuperTrait_isCompileError() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                trait V {
                    static String m() { 'V' }
                    static callSuper() { super.m() }   // <-- rejected even without a super-trait
                }
                class Impl implements V { }
                Impl.callSuper()
            '''
        }
        assert err.message.contains("'super' is not allowed in a static trait method")
    }

    /** The supported explicit form T.super.m() still works for static methods. */
    @Test
    void traitQualifiedSuper_inStaticTraitMethod_works() {
        assertScript '''
            trait V {
                static String m() { 'V' }
                static callTSuper() { V.super.m() }    // explicit, supported
            }
            class Impl implements V { }
            assert Impl.callTSuper() == 'V'
        '''
    }

    /** Unqualified super in an INSTANCE trait method is unaffected (regression guard). */
    @Test
    void unqualifiedSuper_inInstanceTraitMethod_stillWorks() {
        assertScript '''
            trait Base {
                def m() { 'Base' }
            }
            trait V extends Base {
                def m() { 'V' }
                def callSuper() { super.m() }   // instance method, walks chain → 'Base'
            }
            class Impl implements V { }
            assert new Impl().callSuper() == 'Base'
        '''
    }

    /** Plain (non-trait) class static super.m() is unaffected (the reject is trait-only). */
    @Test
    void unqualifiedSuper_inPlainClassStatic_stillWorks() {
        assertScript '''
            class Base {
                static String m() { 'Base' }
            }
            class Sub extends Base {
                static String m() { 'Sub' }
                static String callSuper() { super.m() }
            }
            assert Sub.callSuper() == 'Base'
        '''
    }

    /**
     * Explicit class-qualified static calls in a trait static body — even
     * to the trait's own declared superclass — must NOT be rejected. The
     * reject heuristic relies on {@code isImplicitThis()} being true (set
     * by the resolver when it rewrites {@code super.m()} into a
     * synthesised {@code ClassExpression}); explicit user-written
     * {@code ClassName.m()} calls have {@code isImplicitThis()} false and
     * are left alone.
     */
    @Test
    void explicitObjectStaticCall_inTraitStatic_isAllowed() {
        // Object.toString() is technically a method-not-found at runtime (it's
        // an instance method), but the static-analysis surface that matters
        // here is whether the call is REJECTED BY THE TRAIT TRANSFORM. It
        // must reach the runtime, where MOP behaviour takes over.
        assertScript '''
            trait V {
                static String describe() {
                    try { Object.toString() } catch (any) { 'mop-not-found' }
                }
            }
            class Impl implements V { }
            // No compile error — the heuristic distinguishes this from
            // a rewritten super.m() call via mce.isImplicitThis().
            assert Impl.describe() in ['java.lang.Object', 'class java.lang.Object', 'mop-not-found']
        '''
    }
}
